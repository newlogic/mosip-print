package org.idpass.lite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import java.io.IOException;
import java.util.*;

public class IdentFields {

    static class LocalizedValue {
        private String language;
        private String value;

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private Stack<String> stk = new Stack<>();
    private Map<String, String> parsedFields = new HashMap<>();

    List<String> prefLangs = Arrays.asList("eng", "fra");

    List<String> fieldsOfInterest = Arrays.asList(
            "fullName", "surName", "givenName", "UIN",
            "gender", "placeOfBirth", "dateOfBirth",
            "addressLine1", "addressLine2", "addressLine3", "region",
            "province", "postalCode");

    public Map<String, String> parse(String jsonstr)
            throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jnode = objectMapper.readTree(jsonstr);
        traverse(jnode);

        return parsedFields;
    }

    private void traverse(JsonNode node)
            throws JsonProcessingException
    {
        String keyname = "";

        if (!stk.empty()) {
            Iterator<String> iter = stk.listIterator();
            List<String> keynames = new ArrayList<>();
            iter.forEachRemaining(keynames::add);
            keyname = keynames.get(keynames.size() - 1);
        }

        switch (node.getNodeType())
        {
            case OBJECT:
                Iterator<Map.Entry<String, JsonNode>> kv = node.fields();
                while (kv.hasNext()) {
                    Map.Entry<String, JsonNode> obj = kv.next();
                    String key = obj.getKey();
                    System.out.println(key);
                    stk.push(key);
                    traverse(obj.getValue());
                    stk.pop();
                }

                break;

            case ARRAY:
                outer:
                for (JsonNode elem: node) {
                    ObjectMapper mapper = new ObjectMapper();
                    LocalizedValue localizedValue = null;
                    try {
                        localizedValue = mapper.treeToValue(elem, LocalizedValue.class);
                    } catch (MismatchedInputException e) {
                        continue;
                    }

                    for (String pref : prefLangs) {
                        if (localizedValue.getLanguage().equals(pref)) {
                            addKeyValue(keyname, localizedValue.getValue());
                            break outer;
                        }
                    }
                }
                break;

            case STRING:
                System.out.println(node.asText());
                addKeyValue(keyname, node.asText());
                break;

            case NULL:
                addKeyValue(keyname, "");
                break;
        }
    }

    private void addKeyValue(String key, String value) {
        if (fieldsOfInterest.contains(key)) {
            parsedFields.put(key, value);
        }
    }
}
