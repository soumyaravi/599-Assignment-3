import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ner.NERecogniser;
import org.apache.tika.parser.ner.corenlp.CoreNLPNERecogniser;
import org.apache.tika.parser.ner.opennlp.OpenNLPNERecogniser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by minhpham on 5/1/16.
 */
public class CompositeNERAgreementParser extends AbstractParser {
    private static final String MD_KEY_PREFIX = "NER_";
    private Map<String, NERecogniser> nerChain = null;
    private volatile boolean initialized = false;
    private volatile boolean available = false;

    private OpenNLPNERecogniser openNLPNERecogniser = null;
    private CoreNLPNERecogniser coreNLPNERecogniser = null;
    private GrobidNERecogniser grobidNERecogniser = null;
    private NLTKNERecogniser nltkneRecogniser = null;

    public Tika secondaryParser;

    @Override
    public Set<MediaType> getSupportedTypes(ParseContext parseContext) {
        return null;
    }

    @Override
    public void parse(InputStream inputStream, ContentHandler contentHandler, Metadata metadata, ParseContext parseContext) throws IOException, SAXException, TikaException {
        if (!initialized) {
            initialize(parseContext);
        }
        if (!available) {
            return;
        }

        Reader reader = MediaType.TEXT_PLAIN.toString()
                .equals(metadata.get(Metadata.CONTENT_TYPE))
                ? new InputStreamReader(inputStream, StandardCharsets.UTF_8)
                : secondaryParser.parse(inputStream);

        String text = IOUtils.toString(reader);
        IOUtils.closeQuietly(reader);

        for (String key : nerChain.keySet()) {
            NERecogniser ner = nerChain.get(key);
            Map<String, Set<String>> names = ner.recognise(text);
            if (names != null) {
                for (Map.Entry<String, Set<String>> entry : names.entrySet()) {
                    if (entry.getValue() != null) {
                        String mdKey = key + "!" + MD_KEY_PREFIX + entry.getKey();
                        for (String name : entry.getValue()) {
                            metadata.add(mdKey, name);
                        }
                    }
                }
            }
        }
    }

    private void initialize(ParseContext parseContext) {
        if (initialized) {
            return;
        }
        initialized = true;

        nerChain = new HashMap<>();

        openNLPNERecogniser = new OpenNLPNERecogniser();
        coreNLPNERecogniser = new CoreNLPNERecogniser();
        grobidNERecogniser = new GrobidNERecogniser();
        nltkneRecogniser = new NLTKNERecogniser();

        nerChain.put("open", openNLPNERecogniser);
        nerChain.put("core", coreNLPNERecogniser);
        nerChain.put("grobid", grobidNERecogniser);
        nerChain.put("nltk", nltkneRecogniser);

        try {
            TikaConfig config = new TikaConfig();
            this.secondaryParser = new Tika(config);
            this.available = !nerChain.isEmpty();
        } catch (Exception e) {
            this.available = false;
        }
    }
}
