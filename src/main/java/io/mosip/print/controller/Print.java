package io.mosip.print.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.idpass.lite.IDPassliteConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.print.exception.RegPrintAppException;
import io.mosip.print.model.EventModel;
import io.mosip.print.service.PrintService;
import io.mosip.print.util.CryptoCoreUtil;

@RestController
@RequestMapping(value = "/print")
public class Print {

	@Autowired
	IDPassliteConfig m_config;

	/** The printservice. */
	@Autowired
	private PrintService<Map<String, byte[]>> printService;
	
	@Value("${mosip.event.topic}")
	private String topic;

	@Autowired
	CryptoCoreUtil cryptoCoreUtil;

	/**
	 * Gets the file.
	 *
	 * @param printRequest the print request DTO
	 * @param token        the token
	 * @param errors       the errors
	 * @param printRequest the print request DTO
	 * @return the file
	 * @throws Exception
	 * @throws RegPrintAppException the reg print app exception
	 */
	@PostMapping(path = "/callback/notifyPrint", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthenticateContentAndVerifyIntent(secret = "${mosip.event.secret}", callback = "/v1/print/print/callback/notifyPrint", topic = "${mosip.event.topic}")
	public ResponseEntity<String> handleSubscribeEvent(@RequestBody EventModel eventModel) throws Exception {
		String credential = eventModel.getEvent().getData().get("credential").toString();
		String ecryptionPin = eventModel.getEvent().getData().get("protectionKey").toString();
		String decodedCrdential = cryptoCoreUtil.decrypt(credential);
		Map proofMap = new HashMap<String, String>();
		proofMap = (Map) eventModel.getEvent().getData().get("proof");
		String sign = proofMap.get("signature").toString();
		byte[] pdfbytes = printService.getDocuments(decodedCrdential,
				eventModel.getEvent().getData().get("credentialType").toString(), ecryptionPin,
				eventModel.getEvent().getTransactionId(),
				getSignature(sign, credential), "UIN", false)
				.get("uinPdf");
		InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfbytes));
		/*
		 * File pdfFile = new File(
		 * "/media/lenovo/872da60f-3c16-4cfb-b900-0f63cbe7f3a9/opt/projects/mosip/MajorBug/print/uin.pdf"
		 * ); OutputStream os = new FileOutputStream(pdfFile); os.write(pdfbytes);
		 * os.close();
		 */

		LocalDateTime ldt = LocalDateTime.now();
		String cardfile = m_config.getCardDir() + ldt.toString() + ".pdf";
		OutputStream idCard = new FileOutputStream(new File(cardfile));
		idCard.write(pdfbytes);

		return new ResponseEntity<>("successfully printed", HttpStatus.OK);
	}

	private String getSignature(String sign, String crdential) {
		String signHeader = sign.split("\\.")[0];
		String signData = sign.split("\\.")[2];
		String signature = signHeader + "." + crdential + "." + signData;
		return signature;
	}

}
