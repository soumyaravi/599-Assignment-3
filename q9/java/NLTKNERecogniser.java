import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.tika.parser.ner.NERecogniser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by minhpham on 5/3/16.
 */
public class NLTKNERecogniser implements NERecogniser {

    public static final Set<String> ENTITY_TYPES = new HashSet<String>(){{
        add("NAMES");
    }};

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Set<String> getEntityTypes() {
        return ENTITY_TYPES;
    }

    @Override
    public Map<String, Set<String>> recognise(String text) {
        Map<String, Set<String>> entities = new HashMap<>();
        try {
            String url = "http://localhost:8888/nltk";
            Response response = WebClient.create(url).accept(MediaType.TEXT_HTML).post(text);
            int responseCode = response.getStatus();
            if (responseCode == 200) {
                String result = response.readEntity(String.class);
                JSONParser parser = new JSONParser();
                JSONObject j = (JSONObject) parser.parse(result);
                Iterator<?> keys = j.keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (!key.equals("result")) {
                        ENTITY_TYPES.add(key);
                        entities.put(key.toUpperCase(Locale.ENGLISH), new HashSet((Collection) j.get(key)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return entities;
    }
}
