package org.idpass.lite;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

public class IdentFields {
    private String UIN;
    private String gender;
    private String givenName;
    private String surName;
    private String placeOfBirth;
    private String dateOfBirth;
    private String address;

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
