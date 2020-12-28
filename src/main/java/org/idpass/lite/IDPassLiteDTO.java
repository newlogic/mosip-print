package org.idpass.lite;

import org.api.proto.Ident;

/**
 * The IDPassLiteDTO member fields are the stateful
 * data values computed, per request. This object
 * instance gets returned and passed across calls to
 * IDPassReaderComponent
 */

public class IDPassLiteDTO {

    private Ident ident;
    private boolean result;

    private byte[] svg; // ID PASS Lite QR code in SVG format
    private IdentFieldsConstraint idfc = null; // The constrained extracted fields

    private String facePhotob64; // identity face photo in base64 format
    private byte[] qrCodeBytes; // ID PASS Lite QR code in bytes[] format

    /* getter/setter functions */

    public Ident getIdent() {
        return ident;
    }

    public void setIdent(Ident ident) {
        this.ident = ident;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
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
