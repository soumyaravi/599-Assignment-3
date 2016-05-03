import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;

/**
 * Created by minhpham on 5/1/16.
 */
public class Test {
    private String from = "/Users/minhpham/projects/PolarData/text-plain";
    private CompositeNERAgreementParser parser = new CompositeNERAgreementParser();
    private static String[] nerTypes = new String[]{"grobid", "open", "core"};
    private static String[] entityTypes = new String[]{"NER_ORGANIZATION", "NER_PERSON", "NER_LOCATION", "NER_DATE",
            "NER_MONEY", "NER_TIME", "NER_PERCENTAGE", "NER_MEASUREMENT_NUMBERS", "NER_MEASUREMENT_UNITS", "NER_MEASUREMENTS",
            "NER_NORMALIZED_MEASUREMENTS", "NER_NAMES"};


    public Test() throws IOException {
        final File folder = new File(from);

        EnumSet<FileVisitOption> opts = EnumSet.of(FOLLOW_LINKS);

        Files.walkFileTree(folder.toPath(), opts, 10000, new ClassifyFileVisitor());

    }


    public static void main(String[] args) throws IOException, TikaException, SAXException {
        Test test = new Test();
    }

    private class ClassifyFileVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            File fileEntry = file.toFile();

            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            BodyContentHandler handler = new BodyContentHandler();


            try {
                parser.parse(new FileInputStream(fileEntry), handler, metadata, context);
            } catch (SAXException e) {
                e.printStackTrace();
                return FileVisitResult.CONTINUE;
            } catch (TikaException e) {
                e.printStackTrace();
                return FileVisitResult.CONTINUE;
            }

            HashMap<String, Set<String>> nerResultMap = new HashMap<>();
            nerResultMap.put("core", new HashSet<>());
            nerResultMap.put("open", new HashSet<>());
            nerResultMap.put("grobid", new HashSet<>());


            for (String nerType : nerTypes) {
                FileWriter writer = new FileWriter(nerType + ".txt", true);

                for (String key : entityTypes) {
                    for (String value : metadata.getValues(nerType + "!" + key)) {

                        try {
                            value = value.split("[^0-9a-zA-Z$]")[0];
                        } catch (Exception e) {
                            continue;
                        }

                        nerResultMap.get(nerType).add(value);
                    }
                }
                writer.write(String.join(",", nerResultMap.get(nerType)) + "\n");

                writer.close();
            }

            FileWriter writer = new FileWriter("result.txt", true);

            nerResultMap.get("core").retainAll(nerResultMap.get("open"));

            writer.write(String.join(",", nerResultMap.get("core")) + "\n");
            writer.close();

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}

