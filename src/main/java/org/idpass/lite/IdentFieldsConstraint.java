package org.idpass.lite;

import java.lang.reflect.Field;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * The IdentFieldConstraint class is the input class
 * to the Ident class. The getter methods to each field
 * of interest should return the type expected by Ident
 * class. For example, 'gender' here is a String, but from the
 * perspective of usage as input into the 'Ident' builder
 * class the gender is an int.
 *
 * The list of member fields in this class are the fields
 * of interest to look for.
 */

public class IdentFieldsConstraint {

    /**
     * These fields list names are the fields of interests to search for
     * in the input json. Its corresponding field type is the type
     * constraint for that field.
     */

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
            throws IllegalAccessException, NoSuchFieldException
    {
        for (Map.Entry<String, Object> melem : m.entrySet()) {

            String k = melem.getKey(); // k comes from m

            Field kfield = IdentFieldsConstraint.class.getDeclaredField(k);
            String ktyp = kfield.getType().getCanonicalName(); // ktyp comes from IdentFieldsConstraint field

            try {
                switch (ktyp) {
                    case "java.lang.String":
                        kfield.set(this, melem.getValue());
                        break;

                    case "java.lang.Number":
                        Number num = (Number) melem.getValue();
                        kfield.set(this, num);
                        break;

                    case "java.time.LocalDate":
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/d"); /// TODO: move to config? or list of possible combinations
                        LocalDate dob = LocalDate.parse(melem.getValue().toString(), formatter);
                        kfield.set(this, dob);
                        break;
                }

            } catch (DateTimeException e) {
                // Continue parsing the fields because the error fields could be
                // optional. The final check of field presence/absence is checked
                // within isValid() method
            }
        }
    }

    /**
     * Checks if the constraints defined in identfieldsconstraint.json is
     * satisfied.
     *
     * These constraints are of the following:
     * - presence/absence of a field value (in json)
     * - type of the field value (in member fields)
     * - total bytes count must fit QR code capacity (sum total during parsing)
     *
     * That is, some fields are required mandatory to be present.
     * Some fields are optional. This presence/absence constraint
     * is to be declared in the accompanying identfieldsconstraint.json
     * file.
     *
     * The field value should render to its target type. This type
     * constraint is according to the member field type within
     * IdentFieldsConstraint class.
     *
     * @return True if the constraint is satisfied. Returns false, otherwise.
     */

    public boolean isValid() {
        /// TODO: read json file, check if field is mandatory

        /*if (dateOfBirth == null) {
            return false;
        }*/

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

    /**
     * Returns the gender as numeric int for a particular language
     * as set in the json configuration.
     * @return Returns 1 for female, 2 for male
     */

    public int getGender() {
        String lang = IdentFields.prefLangs.get(0);
        List<String> male = IdentFields.genderMap.get(lang).male;
        List<String> female = IdentFields.genderMap.get(lang).female;

        for (String s : female) {
            if (s.equals(gender)) {
                return 1;
            }
        }

        for (String s : male) {
            if (s.equals(gender)) {
                return 2;
            }
        }

        return 0;
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
