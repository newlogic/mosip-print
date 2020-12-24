package org.idpass.lite;

import org.api.proto.Dat;
import org.api.proto.Ident;
import org.api.proto.KV;
import org.idpass.lite.proto.PostalAddress;

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

    private Ident.Builder idb;

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
    private String city;
    private String postalCode;
    private Number age;
    private String phone;
    private String email;
    private Number cnieNumber;
    private String localAdministrativeAuthority;
    private String parentOrGuardianName;
    private Number parentOrGuardianRIDorUIN;
    private Number id;

    private String lastName;
    private String firstName;

    public Ident getIDent() {
        return idb.build();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Additional pieces of information not in the
     * protobuf message CardDetails definition,
     * shall be stored as a key/value pair of
     * extra information inside the QR code.
     *
     * The default visibility of these extra pieces of
     * information is controlled by its associated
     * boolean flag.
     *
     * Protobuf definitions:
     * https://github.com/idpass/idpass-lite/blob/newprotofields/lib/src/proto/idpasslite.proto
     */

    enum Extras {
        AGE("Age",false),
        PHONE("Phone",false),
        CNIENUMBER("CNIE Number",false),
        GUARDIANNAME("Guardian Name",false),
        GUARDIANID("Guardian UIN",false),
        ID("ID",true),
        EMAIL("Email",false);

        private final String text;
        private final boolean visible;

        Extras(String text, boolean visible) {
            this.text = text;
            this.visible = visible;
        }

        @Override
        public String toString() {
            return text;
        }

        public boolean isVisible() {
            return visible;
        }
    }

    /**
     * This maps a member fields of this class to an associated
     * display string value in the map. For example,
     * "CNIE Number" is the key and its value is from the "cnieNumber"
     * member field of this class instance.
     *
     * This is just to avoid the long repeated 'if' checks, when
     * done manually.
     */

    private static Map<String, Extras> mbuilde = new HashMap<>() {{
        put("age", Extras.AGE);
        put("phone",Extras.PHONE);
        put("cnieNumber",Extras.CNIENUMBER);
        put("parentOrGuardianName",Extras.GUARDIANNAME);
        put("parentOrGuardianRIDorUIN",Extras.GUARDIANID);
        put("id",Extras.ID);
        put("email",Extras.EMAIL);
    }};

    public Number getId() {
        return id;
    }

    public void setId(Number id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Number getAge() {
        return age;
    }

    public void setAge(Number age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Number getCnieNumber() {
        return cnieNumber;
    }

    public void setCnieNumber(Number cnieNumber) {
        this.cnieNumber = cnieNumber;
    }

    public String getLocalAdministrativeAuthority() {
        return localAdministrativeAuthority;
    }

    public void setLocalAdministrativeAuthority(String localAdministrativeAuthority) {
        this.localAdministrativeAuthority = localAdministrativeAuthority;
    }

    public String getParentOrGuardianName() {
        return parentOrGuardianName;
    }

    public void setParentOrGuardianName(String parentOrGuardianName) {
        this.parentOrGuardianName = parentOrGuardianName;
    }

    public Number getParentOrGuardianRIDorUIN() {
        return parentOrGuardianRIDorUIN;
    }

    public void setParentOrGuardianRIDorUIN(Number parentOrGuardianRIDorUIN) {
        this.parentOrGuardianRIDorUIN = parentOrGuardianRIDorUIN;
    }

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
                        String v = new String(melem.getValue().toString());
                        kfield.set(this, v);
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

    /**
     * Creates an Ident.Builder instance.
     *
     * @return Returns a populated Ident.Builder instance
     */

    public Ident.Builder newIdentBuilder()
    {
        idb = Ident.newBuilder();

        List<String> addressLines = new ArrayList<>();

        if (addressLine1 != null) {
            addressLines.add(addressLine1);
        }
        if (addressLine2 != null) {
            addressLines.add(addressLine2);
        }
        if (addressLine3 != null) {
            addressLines.add(addressLine3);
        }

        PostalAddress.Builder postalAddressBuilder = PostalAddress.newBuilder()
                .setLanguageCode("en") /// TODO
                .addAllAddressLines(addressLines);

        if (region != null) {
            postalAddressBuilder.setRegionCode(region);
        }

        if (province != null) {
            postalAddressBuilder.setAdministrativeArea(province);
        }

        if (postalCode != null) {
            postalAddressBuilder.setPostalCode(postalCode);
        }

        if (localAdministrativeAuthority != null) {
            postalAddressBuilder.setAdministrativeArea(localAdministrativeAuthority);
        }

        if (city != null) {
            postalAddressBuilder.setLocality(city);
        }

        PostalAddress postalAddress = postalAddressBuilder.build();

        if (UIN != null) {
            idb.setUIN(UIN.toString());
        }
        if (fullName != null) {
            idb.setFullName(fullName);
        }

        idb.setPostalAddress(postalAddress);
        idb.setGender(getGender());

        if (givenName != null) {
            idb.setGivenName(givenName);
        }
        if (surName != null) {
            idb.setSurName(surName);
        }
        if (firstName != null) {
            idb.setGivenName(firstName);
        }
        if (lastName != null) {
            idb.setSurName(lastName);
        }
        if (placeOfBirth != null) {
            idb.setPlaceOfBirth(placeOfBirth);
        }

        if (dateOfBirth != null) {
            Dat dobProto = Dat.newBuilder()
                    .setYear(dateOfBirth.getYear())
                    .setMonth(dateOfBirth.getMonthValue())
                    .setDay(dateOfBirth.getDayOfMonth())
                    .build();
            idb.setDateOfBirth(dobProto);
        }

        try {

            for (Map.Entry<String, Extras> melem : mbuilde.entrySet()) {
                String fieldName = melem.getKey();
                Extras extra = melem.getValue();
                Field field = IdentFieldsConstraint.class.getDeclaredField(fieldName);
                Object fieldValue = field.get(this);

                if (fieldValue != null) {
                    if (extra.isVisible()) {
                        idb.addPubExtra(KV.newBuilder().setKey(extra.toString()).setValue(fieldValue.toString()));
                    } else {
                        idb.addPrivExtra(KV.newBuilder().setKey(extra.toString()).setValue(fieldValue.toString()));
                    }
                }
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            // only goes here if there is incorrectly spelled
            // member field
        }

        return idb;
    }
}
