package org.idpass.lite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.mosip.print.dto.JsonValue;

import java.io.IOException;
import java.lang.reflect.Field;
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

    private Map<String, Object> parsedFields = new HashMap<>();

    // TODO: will be moved to a json config 
    List<String> prefLangs = Arrays.asList("eng", "fra");

    List<String> fieldsOfInterest = new ArrayList<>();

    public IdentFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fieldsOfInterest.add(fields[i].getName());
        }
    }

    public static IdentFieldsConstraint parse(String jsonstr, Class<?> clazz)
            throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jnode = objectMapper.readTree(jsonstr);

        IdentFields idf = new IdentFields(clazz);
        idf.traverse(jnode);

        IdentFieldsConstraint idfc = new IdentFieldsConstraint(idf.parsedFields);
        return idfc;
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
                    JsonValue localizedValue = null;

                    try {
                        localizedValue = mapper.treeToValue(elem, JsonValue.class);
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
                // still need to check if the string is a json string
                try {
                    JsonNode jsonnode = new ObjectMapper().readTree(node.asText());
                    traverse(jsonnode);
                } catch (Exception e) {
                    System.out.println(node.asText());
                    addKeyValue(keyname, node.asText());
                }
                break;

            case NUMBER:
                addKeyValue(keyname, node.numberValue());
                break;

            case BOOLEAN:
                addKeyValue(keyname, node.booleanValue());
                break;

            case NULL:
                addKeyValue(keyname, "");
                break;
        }
    }

    private void addKeyValue(String key, Object value) {
        if (fieldsOfInterest.contains(key)) {
            parsedFields.put(key, value);
        }
    }
}
