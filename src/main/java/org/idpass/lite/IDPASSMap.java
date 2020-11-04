package org.idpass.lite;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IDPASSMap {
    public static List<String> lang;
    @JsonProperty("UIN")
    private FieldDesc UIN;
    private FieldDesc gender;
    private FieldDesc givenName;
    private FieldDesc surName;
    private FieldDesc placeOfBirth;
    private FieldDesc dateOfBirth;
    private FieldDesc address;

    private String f = "";

    public IDPASSMap() {
    }

    public IDPASSMap(List<String> lang, FieldDesc UIN, FieldDesc gender, FieldDesc givenName, FieldDesc surName, FieldDesc placeOfBirth, FieldDesc dateOfBirth, FieldDesc address) {
        this.lang = lang;
        this.UIN = UIN;
        this.gender = gender;
        this.givenName = givenName;
        this.surName = surName;
        this.placeOfBirth = placeOfBirth;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public List<String> getLang() {
        return lang;
    }

    public void setLang(List<String> lang) {
        this.lang = lang;
    }

    public FieldDesc getUIN() {
        return UIN;
    }

    public void setUIN(FieldDesc UIN) {
        this.UIN = UIN;
    }

    public FieldDesc getGender() {
        return gender;
    }

    public void setGender(FieldDesc gender) {
        this.gender = gender;
    }

    public FieldDesc getGivenName() {
        f = "givenName";
        return givenName;
    }

    public void setGivenName(FieldDesc givenName) {
        this.givenName = givenName;
    }

    public FieldDesc getSurName() {
        f = "surName";
        return surName;
    }

    public void setSurName(FieldDesc surName) {
        this.surName = surName;
    }

    public FieldDesc getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(FieldDesc placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public FieldDesc getDateOfBirth() {
        f = "dateOfBirth";
        return dateOfBirth;
    }

    public void setDateOfBirth(FieldDesc dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public FieldDesc getAddress() {
        return address;
    }

    public void setAddress(FieldDesc address) {
        this.address = address;
    }

    public String get(String data) {
        String ret = "";
        if (data == null) return ret;
        else ret = data;

        String arr[];

        try {
            switch (f) {
                case "surName":
                    arr = ret.split("\\s*(,|\\s)\\s*");
                    if (ret.contains(",")) {
                        ret = arr[0];
                    } else {
                        List<String> L = Arrays.asList(arr);
                        ret = L.get(L.size() - 1);
                    }
                   break;

                case "givenName":
                    arr = ret.split("\\s*(,|\\s)\\s*");
                    if (ret.contains(",")) {
                        List<String> L = Arrays.asList(arr);
                        ret = L.stream().skip(1).collect(Collectors.joining(" "));
                    } else {
                        ret = arr[0];
                    }
                    break;

                case "dateOfBirth":
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/d");
                    LocalDate dob = LocalDate.parse(ret, formatter);
                    break;
            }

        } catch (ArrayIndexOutOfBoundsException | DateTimeParseException e) {
            System.out.println("parse error");
        }

        f = "";
        return ret;
    }

    public static IDPASSMap getInstance() {
        InputStream is = IDPASSMap.class.getClassLoader().getResourceAsStream("idpass-lite-map.json");
        String json = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        IDPASSMap instance = null;
        try {
            instance = mapper.readValue(json, IDPASSMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return instance;
    }
}
