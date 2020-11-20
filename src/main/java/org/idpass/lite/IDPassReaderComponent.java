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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        IdentFields idf = new IdentFields();
        Map<String, Object> idFields = idf.parse(cs);

        Ident.Builder identBuilder = Ident.newBuilder()
                .setPin(pincode);

        String imageType = photob64.split(",")[0];
        byte[] photo = CryptoUtil.decodeBase64(photob64.split(",")[1]);
        photo = convertJ2KToJPG(photo);

        if (photo != null) {
            identBuilder.setPhoto(ByteString.copyFrom(photo));
        }

        /* Populate Ident fields from idf object */

        String dobStr = "1920/01/02"; //idf.getDateOfBirth(); /// TODO
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/d");
        LocalDate dob = LocalDate.parse(dobStr, formatter);
        Dat dobProto = Dat.newBuilder()
                .setYear(dob.getYear())
                .setMonth(dob.getMonthValue())
                .setDay(dob.getDayOfMonth())
                .build();

        List<String> addrLines = Arrays.asList("Address line1", "Address line2");

        PostalAddress postalAddress = PostalAddress.newBuilder()
                .setLanguageCode("en") /// TODO
                .addAllAddressLines(/* idf.getAddressLines() */ addrLines)
                .build();

        identBuilder.setUIN(/*idf.getUIN()*/ "314159"); /// TODO
        identBuilder.setFullName(/*idf.getFullName()*/ "John Doe");
        identBuilder.setPostalAddress(postalAddress);
        identBuilder.setGender(/*idf.getGenderAsInt()*/ 2);

        identBuilder.setGivenName(/*idf.getGivenName()*/ "John");
        identBuilder.setSurName(/*idf.getSurName()*/ "Doe");
        identBuilder.setPlaceOfBirth(/*idf.getPlaceOfBirth()*/ "Cebu");
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
            e.printStackTrace();
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
