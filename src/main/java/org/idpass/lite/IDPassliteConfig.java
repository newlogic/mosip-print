package org.idpass.lite;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * The DetailField enum provides a type safe configuration values
 * in application.properties for the "idpass.lite.qrcode.detailsvisible" key.
 *
 * The configured comma-separated list of fields shall be visible by default
 * in the generated QR code. The rest of the fields shall only be visible after a
 * successful authentication.
 */

enum DetailField {
    DETAIL_SURNAME(IDPassReader.DETAIL_SURNAME),
    DETAIL_GIVENNAME(IDPassReader.DETAIL_GIVENNAME),
    DETAIL_DATEOFBIRTH(IDPassReader.DETAIL_DATEOFBIRTH),
    DETAIL_PLACEOFBIRTH(IDPassReader.DETAIL_PLACEOFBIRTH),
    DETAIL_CREATEDAT(IDPassReader.DETAIL_CREATEDAT),
    DETAIL_UIN(IDPassReader.DETAIL_UIN),
    DETAIL_FULLNAME(IDPassReader.DETAIL_FULLNAME),
    DETAIL_GENDER(IDPassReader.DETAIL_GENDER),
    DETAIL_POSTALADDRESS(IDPassReader.DETAIL_POSTALADDRESS);

    private int bitFlag;

    public int getBitFlag() {
        return bitFlag;
    }

    private DetailField(int value) {
        bitFlag = value;
    }
}

/**
 * A standard spring boot class that reads configuration values in
 * application.properties under a specified key prefix.
 */

@Configuration
@ConfigurationProperties(prefix = "idpass.lite")
public class IDPassliteConfig {

    public static class QrCode {
        // comma-separated list of fields configured in application.properties
        private List<DetailField> detailsVisible = new ArrayList<>();

        public List<DetailField> getDetailsVisible() {
            return detailsVisible;
        }

        public void setDetailsVisible(List<DetailField> detailsVisible) {
            this.detailsVisible = detailsVisible;
        }
    }

    private QrCode qrcode = new QrCode();

    /** Getter/Setter helper methods */

    public QrCode getQrcode() {
        return qrcode;
    }

    public void setQrcode(QrCode qrcode) {
        this.qrcode = qrcode;
    }

    /**
     * Convert the comma-separated list of fields into an integer bit flags
     * @return The bit flags describing which field(s) are publicly visible when
     * generating the QR code.
     */

    public int getVisibleFields() {
        List<DetailField> visibleFields = qrcode.getDetailsVisible();
        int bitFlags = 0;

        for (DetailField f : visibleFields) {
            bitFlags = bitFlags | f.getBitFlag();
        }

        return bitFlags;
    }
}
