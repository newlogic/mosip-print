package org.idpass.lite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

/**
 * The IdentFields class represents the collection of fields
 * which are inputs to the Ident protobuf message object.
 *
 * Through its associated IdentFieldsDeserializer class, and together with
 * idpass-lite-map.json configuration, the IdentFields::getInstance(String cs)
 * can accept an input json, and then extracts from this input json in order
 * to populate the fields of IdentFields.
 *
 * The idpass-lite-map.json describes the source field(s) from where to extract
 * data from the input json. In most cases, the source field name and the
 * destination field name are the same. For example, to populate the
 * IdentFields::gender field, the idpass-lite-map.json describes it as:
 *
 * "gender": {
 *     "value": "gender",
 *     "isMandatory": false
 * }
 *
 * In general representation,
 *
 * "P": {
 *     "value": "Q[,R,S,...]"
 *     "isMandatory": false|true
 * }
 *
 * where P corresponds to a field in IdentFields class, and the value can be
 * a comma-separated list of source fields. For example, the IdentFields::address
 * field is the aggregatation of several fields from the input json.
 *
 */

public class IdentFields {
    /**
     * Only fields defined in IdentFields are valid input fields
     */
    private String UIN;
    private String gender;
    private String givenName;
    private String surName;
    private String placeOfBirth;
    private String dateOfBirth;
    private String address;

    /**
     * Accepts a credential subject input json to construct a
     * constrained IdentFields object.
     * @param cs Credential subject json string
     * @return Returns an IdentFields type
     */
    public static IdentFields getInstance(String cs) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(IdentFields.class, new IdentFieldsDeserializer());
        mapper.registerModule(module);

        IdentFields idf = null;

        try {
            idf = mapper.readValue(cs, IdentFields.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return idf;
    }

    /* Auto-generated getters/setters and constructors */

    public IdentFields() {
    }

    public IdentFields(String UIN, String gender, String givenName, String surName, String placeOfBirth, String dateOfBirth, String address) {
        this.UIN = UIN;
        this.gender = gender;
        this.givenName = givenName;
        this.surName = surName;
        this.placeOfBirth = placeOfBirth;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public String getUIN() {
        return UIN;
    }

    public void setUIN(String UIN) {
        this.UIN = UIN;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
