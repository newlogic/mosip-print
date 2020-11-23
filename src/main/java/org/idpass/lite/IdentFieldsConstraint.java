package org.idpass.lite;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * The IdentFieldConstraint class is the input class
 * to the Ident class. The getter methods to each field
 * of interest should return the type expected by Ident
 * class.
 *
 * The list of member fields in this class are the fields
 * of interest to look for.
 */

public class IdentFieldsConstraint {

    private String fullName;
    private String surName;
    private String givenName;
    private Number UIN;
    private String gender;
    private String placeOfBirth;
    private LocalDate dateOfBirth;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String region;
    private String province;
    private String postalCode;

    /**
     * The only constructor that accepts a map of found key/value pairs
     * when the input json is traversed and using this class to provide the
     * fields of interest.
     * @param m Is a map of found key/value pairs from an input json
     */

    public IdentFieldsConstraint(Map<String, Object> m)
    {
        for (Map.Entry<String, Object> melem : m.entrySet()) {

            String k = melem.getKey();

            try {
                Field f = IdentFieldsConstraint.class.getDeclaredField(k);

                if (f.getType().isAssignableFrom(LocalDate.class)) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/d"); /// TODO: move to config? or list of possible combinations
                    LocalDate dob = LocalDate.parse(melem.getValue().toString(), formatter);
                    f.set(this, dob);
                } else if(f.getType().isAssignableFrom(Number.class)) {
                    Number num = (Number)melem.getValue();
                    f.set(this, num);
                } else{
                    f.set(this, melem.getValue());
                }

            } catch (NoSuchFieldException | IllegalAccessException e) {

            }
        }
    }

    public boolean isValid() {
        /// TODO: read json file, check if field is mandatory
        return true;
    }

    /* Getters methods */

    public String getFullName() {
        return fullName;
    }

    public String getSurName() {
        return surName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getUIN() {
        return UIN != null ? UIN.toString() : null;
    }

    public int getGender() {
        switch (gender) {
            case "Male": /// TODO: Generalized for multiple languages?
                return 2;
            case "Female":
                return 1;
            default:
                return 3;
        }
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public String getRegion() {
        return region;
    }

    public String getProvince() {
        return province;
    }

    public String getPostalCode() {
        return postalCode;
    }
}
