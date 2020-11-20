package org.idpass.lite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import java.io.IOException;
import java.util.*;

/**
 * The IdentFields::parse() method will walk into the json tree
 * and extracts out the value of fields that we are interested.
 *
 * A second pass is then performed on the data that is gathered on the
 * first pass: 1) Must comply to mandatory attributes 2) Date must be valid
 * date 3) Total sum of bytes must fit QR code 4) Language preference localization
 *
 * Lastly, a preset fields is populated with their values.
 *
 */

public class IdentFields {

    /**
     * The LocalizedValue class is used in case MOSIP formats a value
     * as a list of several language encoding.
     */

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

    /**
     * The stack stk variable keeps track of nested key paths
     * as the json tree is being traversed. It's possible future
     * use is to be able to express json full path key name for
     * extraction, such as:
     *
     * Record.Person.Id
     *
     * To specifically and unambiguously extract the 'Id' field.
     */

    private Stack<String> stk = new Stack<>();

    /**
     * The parseFields contains the gathered fields based on the
     * configured list of fields of interest.
     */

    private Map<String, String> parsedFields = new HashMap<>();

    // TODO: will be moved to a json config 
    List<String> prefLangs = Arrays.asList("eng", "fra");

    // TODO: will be moved to a json config 
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
