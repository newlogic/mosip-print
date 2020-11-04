package org.idpass.lite;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class IdentFieldsDeserializer extends StdDeserializer<IdentFields> {

    public IdentFieldsDeserializer() {
        this(null);
    }

    public IdentFieldsDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public IdentFields deserialize(JsonParser parser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException
    {
        IDPASSMap idpassMap = IDPASSMap.getInstance();

        IdentFields ret = new IdentFields();
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

        String address = idpassMap.get(idpassMap.getAddress().from(node));
        String UIN = idpassMap.get(idpassMap.getUIN().from(node));
        String gender = idpassMap.get(idpassMap.getGender().from(node));

        String surName = idpassMap.get(idpassMap.getSurName().from(node));
        String givenName = idpassMap.get(idpassMap.getGivenName().from(node));

        String placeOfBirth = idpassMap.get(idpassMap.getPlaceOfBirth().from(node));
        String dateOfBirth = idpassMap.get(idpassMap.getDateOfBirth().from(node));

        ret.setGivenName(givenName);
        ret.setSurName(surName);
        ret.setUIN(UIN);
        ret.setGender(gender);
        ret.setPlaceOfBirth(placeOfBirth);
        ret.setDateOfBirth(dateOfBirth);
        ret.setAddress(address);

        return ret;
    }
}
