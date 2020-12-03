package org.idpass.lite;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The SessionData member fields are the stateful
 * data values per request. A SessionData object
 * instance get passed across calls to IDPassReaderComponent
 */

public class SessionData {

    private boolean result;

    private byte[] svg;
    private IdentFieldsConstraint idfc = null;

    private String issuanceDate;
    private String facePhotob64;
    private byte[] qrCodeBytes;

    /* getter/setter functions */

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getIssuanceDate(DateTimeFormatter formatter, int years) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(issuanceDate);
            LocalDate ld = zdt.toLocalDate().plusYears(years);
            return ld.format(formatter);
        } catch (Exception e){

        }
        return "";
    }

    public String getIssuanceDate(DateTimeFormatter formatter) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(issuanceDate);
            LocalDate ld = zdt.toLocalDate();
            return ld.format(formatter);
        } catch (Exception e){

        }
        return "";
    }

    public String getIssuanceDate() {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(issuanceDate);
            LocalDate ld = zdt.toLocalDate();
            System.out.println(ld.format(DateTimeFormatter.ofPattern("yyyy/MM/d")));
            return issuanceDate;
        } catch (Exception e) {

        }

        return "";
    }

    public byte[] getSvg() {
        return svg;
    }

    public void setSvg(byte[] svg) {
        this.svg = svg;
    }

    public IdentFieldsConstraint getIdfc() {
        return idfc;
    }

    public void setIdfc(IdentFieldsConstraint idfc) {
        this.idfc = idfc;
    }

    public void setIssuanceDate(String issuanceDate) {
        this.issuanceDate = issuanceDate;
    }

    public String getFacePhotob64() {
        return facePhotob64;
    }

    public void setFacePhotob64(String facePhotob64) {
        this.facePhotob64 = facePhotob64;
    }

    public byte[] getQrCodeBytes() {
        return qrCodeBytes;
    }

    public void setQrCodeBytes(byte[] qrCodeBytes) {
        this.qrCodeBytes = qrCodeBytes;
    }
}
