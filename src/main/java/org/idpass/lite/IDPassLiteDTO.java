package org.idpass.lite;

/**
 * The SessionData member fields are the stateful
 * data values per request. A SessionData object
 * instance get passed across calls to IDPassReaderComponent
 */

public class IDPassLiteDTO {

    private boolean result;

    private byte[] svg;
    private IdentFieldsConstraint idfc = null;

    private String facePhotob64;
    private byte[] qrCodeBytes;

    /* getter/setter functions */

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
