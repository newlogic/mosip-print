package org.idpass.lite;

import com.google.protobuf.ByteString;
import io.mosip.kernel.core.util.CryptoUtil;
import org.api.proto.Dat;
import org.api.proto.Ident;
import org.idpass.lite.exceptions.IDPassException;
import org.idpass.lite.proto.PostalAddress;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReader;

/**
 * Spring boot singleton component execution wrapper of
 * an IDPassReader instance
 */

@Component
public class IDPassReaderComponent
{
    public IDPassReader reader;

    /**
     * Instantiates IDPassReader reader with a particular configuration
     *
     * @throws IDPassException Standard exception
     * @throws IOException Standard exception
     */
    public IDPassReaderComponent(IDPassliteConfig config)
            throws IDPassException, IOException
    {
        reader = new IDPassReader();
        reader.setDetailsVisible(config.getVisibleFields());
    }

    /**
     * Returns a PNG image QR code representation as a byte[] array,
     * from the given inputs:
     *
     * @param cs The credential subject input json
     * @param pincode The IDPASS LITE pin code
     * @param photob64 A facial photo image
     * @return Returns PNG QR code of the generated IDPASS LITE card
     */
    public byte[] generateQrCode(String cs, String pincode, String photob64)
            throws IOException
    {
        IdentFieldsConstraint idfc = null;
        try {
            idfc = (IdentFieldsConstraint) IdentFields.parse(cs, IdentFieldsConstraint.class);

            if (idfc == null || !idfc.isValid()) { // in terms of identfieldsconstraint.json
                return null;
            }

        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            return null;
        }

        Ident.Builder identBuilder = Ident.newBuilder()
                .setPin(pincode);

        String imageType = photob64.split(",")[0];
        byte[] photo = CryptoUtil.decodeBase64(photob64.split(",")[1]);
        photo = convertJ2KToJPG(photo);

        if (photo != null) {
            identBuilder.setPhoto(ByteString.copyFrom(photo));
        }

        /* Populate Ident fields from idf object */

        LocalDate dob = idfc.getDateOfBirth();
        Dat dobProto = Dat.newBuilder()
                .setYear(dob.getYear())
                .setMonth(dob.getMonthValue())
                .setDay(dob.getDayOfMonth())
                .build();

        List<String> addrLines = new ArrayList<>();
        if (idfc.getAddressLine1() != null) {
            addrLines.add(idfc.getAddressLine1());
        }
        if (idfc.getAddressLine2() != null) {
            addrLines.add(idfc.getAddressLine2());
        }
        if (idfc.getAddressLine3() != null) {
            addrLines.add(idfc.getAddressLine3());
        }

        PostalAddress.Builder postalAddressBuilder = PostalAddress.newBuilder()
                .setLanguageCode("en") /// TODO
                .addAllAddressLines(addrLines);

        if (idfc.getRegion() != null) {
            postalAddressBuilder.setRegionCode(idfc.getRegion());
        }

        if (idfc.getProvince() != null) {
           postalAddressBuilder.setAdministrativeArea(idfc.getProvince());
        }

        if (idfc.getPostalCode() != null) {
            postalAddressBuilder.setPostalCode(idfc.getPostalCode());
        }

        PostalAddress postalAddress = postalAddressBuilder.build();

        if (idfc.getUIN() != null) {
            identBuilder.setUIN(idfc.getUIN());
        }
        if (idfc.getFullName() != null) {
            identBuilder.setFullName(idfc.getFullName());
        }

        identBuilder.setPostalAddress(postalAddress);
        identBuilder.setGender(idfc.getGender());

        if (idfc.getGivenName() != null) {
            identBuilder.setGivenName(idfc.getGivenName());
        }
        if (idfc.getSurName() != null) {
            identBuilder.setSurName(idfc.getSurName());
        }
        if (idfc.getPlaceOfBirth() != null) {
            identBuilder.setPlaceOfBirth(idfc.getPlaceOfBirth());
        }

        identBuilder.setDateOfBirth(dobProto);

        Ident ident = identBuilder.build();
        byte[] qrcodeId = null;

        try {
            Card card = reader.newCard(ident, null);
            BufferedImage bi = card.asQRCode();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", bos);
            qrcodeId = bos.toByteArray();

        } catch (IOException | IDPassException e) {
            return null;
        }

        return  qrcodeId;
    }

    // Notes: copied from 'mosip-functional-tests' repo
    private static byte[] convertJ2KToJPG(byte[] jp2Data) {
        byte[] jpgImg = null;
        J2KImageReader j2kImageReader = new J2KImageReader(null);
        try {
            j2kImageReader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(jp2Data)));
            ImageReadParam imageReadParam = j2kImageReader.getDefaultReadParam();
            BufferedImage image = j2kImageReader.read(0, imageReadParam);
            ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
            ImageIO.write(image, "JPG", imgBytes);
            jpgImg = imgBytes.toByteArray();
        } catch (IOException e) {

        }

        return jpgImg;
    }
}
