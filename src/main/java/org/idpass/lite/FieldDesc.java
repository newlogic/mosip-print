package org.idpass.lite;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class FieldDesc {
    private String value;
    private boolean isMandatory;

    // addressLine1,addressLine2,addressLine3,region,province,postalCode

    public String from(JsonNode jnode)
            throws IOException
    {
        String ret = null;

        if (value.equals("id") || value.equals("UIN") || value.equals("dateOfBirth")) {
            ret = jnode.get(value).asText();
        } else {
            StringBuilder sb = new StringBuilder();

            for (String v : value.split(",")) {

                if (jnode.has(v)) {
                    String t = jnode.get(v).asText();
                    ObjectMapper m = new ObjectMapper();
                    List<SourceDesc> L = m.readValue(t, new TypeReference<List<SourceDesc>>(){});
                    here:
                    for (String x : IDPASSMap.lang) {
                        for (SourceDesc s : L) {
                            if (s.language.equals(x)) {
                                sb.append(s.value + " ");
                                break here;
                            }
                        }
                    }
                }
            }

            ret = sb.toString();
        }

        if (isMandatory) {
            if (ret == null || ret.isBlank() || ret.isEmpty()) {
                throw new IOException();
            }
        }

        return ret;
    }

    public FieldDesc() {
    }

    public FieldDesc(String value, boolean isMandatory) {
        this.value = value;
        this.isMandatory = isMandatory;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }
}
