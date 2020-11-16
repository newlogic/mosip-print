package org.idpass.lite;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

/**
 * The associated deserializer of IdentFields
 */

public class IdentFieldsDeserializer extends StdDeserializer<IdentFields> {

    public IdentFieldsDeserializer() {
        this(null);
    }

    public IdentFieldsDeserializer(Class<?> vc) {
        super(vc);
    }

    /**
     * Deserialization implementation. Loads credential subject input json into a tree,
     * and extracts the field values map defined in idpass-map-lite.json.
     *
     * @param parser Standard parser
     * @param deserializationContext Standard context
     * @return Returns an IdentFields type with populated fields
     * @throws IOException Standard exception
     * @throws JsonProcessingException Standard exception
     */

    @Override
    public IdentFields deserialize(JsonParser parser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException
    {
        // Loads idpass-map-lite.json into idpassMap object
        IDPASSMap idpassMap = IDPASSMap.getInstance();

        // Prepare returned value
        IdentFields ret = new IdentFields();

        // Loads credential subject input json
        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);

        /*
        UIN
        gender
        givenName
        surName
        placeOfBirth
        dateOfBirth
        address
        */

        /* Extract the mapped fields */

        String address = idpassMap.get(idpassMap.getAddress().from(node));
        String UIN = idpassMap.get(idpassMap.getUIN().from(node));
        String gender = idpassMap.get(idpassMap.getGender().from(node));

        String surName = idpassMap.get(idpassMap.getSurName().from(node));
        String givenName = idpassMap.get(idpassMap.getGivenName().from(node));

        String placeOfBirth = idpassMap.get(idpassMap.getPlaceOfBirth().from(node));
        String dateOfBirth = idpassMap.get(idpassMap.getDateOfBirth().from(node));

        /* Populate fields into return value */
        ret.setGivenName(givenName);
        ret.setSurName(surName);
        ret.setUIN(UIN);
        ret.setGender(gender);
        ret.setPlaceOfBirth(placeOfBirth);
        ret.setDateOfBirth(dateOfBirth);
        ///ret.setAddress(address); /// TODO

        return ret;
    }
}
