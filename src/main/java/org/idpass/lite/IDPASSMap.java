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

/**
 * The IDPASSMap class is the java rendetion of idpass-lite-map.json.
 * It is used in the IdentFieldsDeserializer deserialization to map a
 * source field(s) to a destination field.
 */

public class IDPASSMap {
    /**
     * These fields correspond to the idpass-lite-map.json
     */
    public static List<String> lang;
    @JsonProperty("UIN")
    private FieldDesc UIN;
    private FieldDesc gender;
    private FieldDesc givenName;
    private FieldDesc surName;
    private FieldDesc placeOfBirth;
    private FieldDesc dateOfBirth;
    private FieldDesc address;

    /* This is a temporary field used to hold the current field being parsed */
    private String currentField = "";

    /* Auto-generated getters/setters and constructors */

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
        currentField = givenName.getValue();
        return givenName;
    }

    public void setGivenName(FieldDesc givenName) {
        this.givenName = givenName;
    }

    public FieldDesc getSurName() {
        currentField = surName.getValue();
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
        currentField = dateOfBirth.getValue();
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

    /**
     * For most fields which are directly mapped such as "gender", the get() method is just
     * a pass-through. But for fields with no direct mapping, such as the surName field, this method
     * parses the input data argument which might contain "Doe, John"
     * and extracts the "Doe" as the surName. The purpose of this method
     * is to construct a surName from a fullName.
     * @param data Source field value. For example, fullName = "Doe, John"
     * @return Returns the best-effort attempt to extract the field value
     */

    public String get(String data) {
        String ret = "";
        if (data == null) return ret;
        else ret = data;

        String arr[];

        try {
            if (currentField.equals(surName.getValue())) {
                arr = ret.split("\\s*(,|\\s)\\s*");
                if (ret.contains(",")) {
                    ret = arr[0];
                } else {
                    List<String> L = Arrays.asList(arr);
                    ret = L.get(L.size() - 1);
                }
            } else if(currentField.equals(givenName.getValue())) {
                arr = ret.split("\\s*(,|\\s)\\s*");
                if (ret.contains(",")) {
                    List<String> L = Arrays.asList(arr);
                    ret = L.stream().skip(1).collect(Collectors.joining(" "));
                } else {
                    ret = arr[0];
                }
            } else if (currentField.equals(dateOfBirth.getValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/d");
                LocalDate dob = LocalDate.parse(ret, formatter);
            }

        } catch (ArrayIndexOutOfBoundsException | DateTimeParseException e) {
            System.out.println("parse error");
        }

        currentField = "";
        return ret;
    }

    /**
     * Loads the configuration json into the object to help in
     * the deserialization of the input json.
     * @return
     */

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
