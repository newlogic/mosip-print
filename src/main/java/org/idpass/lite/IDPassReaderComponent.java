package org.idpass.lite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.ByteString;
import io.mosip.kernel.core.pdfgenerator.exception.PDFGeneratorException;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.pdfgenerator.itext.constant.PDFGeneratorExceptionCodeConstant;
import io.mosip.print.constant.ApiName;
import io.mosip.print.constant.UinCardType;
import io.mosip.print.dto.ErrorDTO;
import io.mosip.print.dto.PDFSignatureRequestDto;
import io.mosip.print.dto.SignatureResponseDto;
import io.mosip.print.exception.ApisResourceAccessException;
import io.mosip.print.exception.PDFSignatureException;
import io.mosip.print.service.PrintRestClientService;
import io.mosip.registration.print.core.http.RequestWrapper;
import io.mosip.registration.print.core.http.ResponseWrapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.api.proto.Dat;
import org.api.proto.Ident;
import org.idpass.lite.exceptions.IDPassException;
import org.idpass.lite.proto.PostalAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReader;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static io.mosip.print.service.impl.PrintServiceImpl.DATETIME_PATTERN;

/**
 * Spring boot singleton component execution wrapper of
 * an IDPassReader instance
 */

@Component
public class IDPassReaderComponent
{
    public IDPassReader reader;
    byte[] m_svg;
    IdentFieldsConstraint m_idfc = null;

    @Autowired
    private PDFGenerator pdfGenerator;

    @Autowired
    IDPassliteConfig m_config;

    @Autowired
    private Environment env;

    @Autowired
    private PrintRestClientService<Object> restClientService;

    @Value("${mosip.registration.processor.print.service.uincard.signature.reason}")
    private String reason;

    private String issuanceDate;

    ObjectMapper mapper = new ObjectMapper();

    public LocalDate getIssuanceDateAsLocalDate() {
        ZonedDateTime zdt = ZonedDateTime.parse(issuanceDate);
        LocalDate ld = zdt.toLocalDate();
        return ld;
    }

    public String getIssuanceDate() {
        ZonedDateTime zdt = ZonedDateTime.parse(issuanceDate);
        LocalDate ld = zdt.toLocalDate();
        System.out.println(ld.format(DateTimeFormatter.ofPattern("yyyy/MM/d")));
        return issuanceDate;
    }

    public void setIssuanceDate(String issuanceDate) {
        this.issuanceDate = issuanceDate;
    }

    /**
     * Instantiates IDPassReader reader with a particular configuration
     *
     * @throws IDPassException Standard exception
     * @throws IOException Standard exception
     */
    public IDPassReaderComponent(IDPassliteConfig config)
            throws IDPassException, IOException
    {
        InputStream is = IDPassReaderComponent.class.getClassLoader().getResourceAsStream(config.getP12File());

        // Initialize reader
        reader = new IDPassReader(
                config.getStorePrefix(), is,
                config.getStorePassword(), config.getKeyPassword());

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
        m_idfc = null;
        try {
            m_idfc = (IdentFieldsConstraint) IdentFields.parse(cs, IdentFieldsConstraint.class);

            if (m_idfc == null || !m_idfc.isValid()) { // in terms of identfieldsconstraint.json
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

        LocalDate dob = m_idfc.getDateOfBirth();
        Dat dobProto = Dat.newBuilder()
                .setYear(dob.getYear())
                .setMonth(dob.getMonthValue())
                .setDay(dob.getDayOfMonth())
                .build();

        List<String> addrLines = new ArrayList<>();
        if (m_idfc.getAddressLine1() != null) {
            addrLines.add(m_idfc.getAddressLine1());
        }
        if (m_idfc.getAddressLine2() != null) {
            addrLines.add(m_idfc.getAddressLine2());
        }
        if (m_idfc.getAddressLine3() != null) {
            addrLines.add(m_idfc.getAddressLine3());
        }

        PostalAddress.Builder postalAddressBuilder = PostalAddress.newBuilder()
                .setLanguageCode("en") /// TODO
                .addAllAddressLines(addrLines);

        if (m_idfc.getRegion() != null) {
            postalAddressBuilder.setRegionCode(m_idfc.getRegion());
        }

        if (m_idfc.getProvince() != null) {
           postalAddressBuilder.setAdministrativeArea(m_idfc.getProvince());
        }

        if (m_idfc.getPostalCode() != null) {
            postalAddressBuilder.setPostalCode(m_idfc.getPostalCode());
        }

        PostalAddress postalAddress = postalAddressBuilder.build();

        if (m_idfc.getUIN() != null) {
            identBuilder.setUIN(m_idfc.getUIN());
        }
        if (m_idfc.getFullName() != null) {
            identBuilder.setFullName(m_idfc.getFullName());
        }

        identBuilder.setPostalAddress(postalAddress);
        identBuilder.setGender(m_idfc.getGender());

        if (m_idfc.getGivenName() != null) {
            identBuilder.setGivenName(m_idfc.getGivenName());
        }
        if (m_idfc.getSurName() != null) {
            identBuilder.setSurName(m_idfc.getSurName());
        }
        if (m_idfc.getPlaceOfBirth() != null) {
            identBuilder.setPlaceOfBirth(m_idfc.getPlaceOfBirth());
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
            m_svg = card.asQRCodeSVG().getBytes(StandardCharsets.UTF_8);
        } catch (IOException | IDPassException e) {
            return null;
        }

        return  qrcodeId;
    }

    /**
     * Call editor.idpass.org to generate ID PASS Lite PDF card
     * @return Returns pdf bytes array
     * @throws IOException Standard exception
     */

    public byte[] editorGenerate()
            throws IOException
    {
        byte[] pdfbytes = null;

        ObjectNode front = mapper.createObjectNode();
        front.put("identification_number",m_idfc.getUIN());
        front.put("given_names",m_idfc.getGivenName() == null ? m_idfc.getFullName() : m_idfc.getGivenName());
        front.put("surname",m_idfc.getSurName() == null ? m_idfc.getFullName() : m_idfc.getSurName());
        front.put("sex",m_idfc.getGender() == 1 ? "Female" : "Male");
        front.put("nationality","African");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/d"); /// TODO: move to config? or list of possible combinations
        front.put("birth_date",m_idfc.getDateOfBirth().format(formatter));
        String issue_date = getIssuanceDateAsLocalDate().format(formatter);
        front.put("issue_date",issue_date);
        LocalDate exp = getIssuanceDateAsLocalDate().plusYears(10);
        front.put("expiry_date", exp.format(formatter));

        String svgqrcode = CryptoUtil.encodeBase64String(m_svg);

        ObjectNode back = mapper.createObjectNode();
        back.put("qrcode", "data:image/svg+xml;base64," + svgqrcode);

        ObjectNode fields = mapper.createObjectNode();
        fields.set("front", front);
        fields.set("back", back);

        ObjectNode payload = mapper.createObjectNode();
        payload.put("create_qr_code", false);
        payload.set("fields", fields);

        String jsonPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);

        //////////////
        RestTemplate restTemplate = new RestTemplate();
        String uri = m_config.getEditorUrl();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", MediaType.APPLICATION_JSON.toString());

        HttpEntity<String> request = new HttpEntity<String>(jsonPayload, headers);

        String response = restTemplate.postForObject(uri, request, String.class);
        JsonNode node = mapper.readTree(response);
        String blob = node.get("files").get("pdf").asText();
        String b64 = blob.split(",")[1];
        pdfbytes = CryptoUtil.decodeBase64(b64);
        /////////////

        return pdfbytes;
    }

    /**
     * This method is a modified from UinCardGeneratorImpl::generateUinCard
     * as this invokes a REST call to editor.idpass.org to generate the pdf
     * that is about to be send to MOSIP backend for signature
     *
     * @param in Template. Not used here
     * @param type Card type
     * @param password password
     * @return Returns pdf bytes of signed pdf
     * @throws ApisResourceAccessException standard exception
     */

    public byte[] generateUinCard(InputStream in, UinCardType type, String password)
            throws ApisResourceAccessException
    {
        byte[] pdfSignatured=null;
        try {
            // Calls editor.idpass.org REST API to generate initial PDF
            byte[] pdfbuf = editorGenerate();
            PDFSignatureRequestDto request = new PDFSignatureRequestDto(0, 0, 0, 0, reason, 1, password);

            request.setApplicationId("KERNEL");
            request.setReferenceId("SIGN");
            request.setData(CryptoUtil.encodeBase64String(pdfbuf));
            DateTimeFormatter format = DateTimeFormatter.ofPattern(env.getProperty(DATETIME_PATTERN));
            LocalDateTime localdatetime = LocalDateTime
                    .parse(DateUtils.getUTCCurrentDateTimeString(env.getProperty(DATETIME_PATTERN)), format);

            request.setTimeStamp(DateUtils.getUTCCurrentDateTimeString());
            RequestWrapper<PDFSignatureRequestDto> requestWrapper = new RequestWrapper<>();

            requestWrapper.setRequest(request);
            requestWrapper.setRequesttime(localdatetime);
            ResponseWrapper<?> responseWrapper;
            SignatureResponseDto signatureResponseDto;

            responseWrapper= (ResponseWrapper<?>)restClientService.postApi(ApiName.PDFSIGN, null, null,
                    requestWrapper, ResponseWrapper.class,MediaType.APPLICATION_JSON);


            if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
                ErrorDTO error = responseWrapper.getErrors().get(0);
                throw new PDFSignatureException(error.getMessage());
            }
            signatureResponseDto = mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()),
                    SignatureResponseDto.class);

            pdfSignatured = CryptoUtil.decodeBase64(signatureResponseDto.getData());

        } catch (IOException | PDFGeneratorException e) {
            throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
                    e.getMessage() + ExceptionUtils.getStackTrace(e));
        }
        catch (ApisResourceAccessException e) {
            e.printStackTrace();
        }

        return pdfSignatured;
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
            return null;
        }

        return jpgImg;
    }
}
