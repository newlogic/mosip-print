package io.mosip.print.controller;

import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.print.model.MOSIPMessage;
import io.mosip.print.service.PrintService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.kernel.websub.api.constants.WebSubClientConstants;
import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;


@RestController
@RequestMapping(value = "/print")
public class Print {
	private static final Logger LOGGER = LoggerFactory.getLogger(Print.class);

	//@Value("${mosip.event.secret}")
	  //private String secret;
	
	@Autowired
	PrintService printService;

    @PostMapping(value = "/enqueue",consumes = "application/json")
	//@PostMapping(value = "/enqueue")
	//@PreAuthenticateContentAndVerifyIntent(secret = "helloworld", callback = "/print/enqueue",topic = "http://192.168.2.234:9090/")
    @PreAuthenticateContentAndVerifyIntent(secret = "Kslk30SNF2AChs2", callback = "/print/enqueue", topic = "http://mosip.io/print/pdf")
	public Object printPost(MOSIPMessage message) throws IOException {
		LOGGER.info(message.getTopic());
		//TODO: Validate the MOSIPmessage
		//TODO:Call the print service with the map that we received from MOSIPMessage
		printService.print(null);

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode returnData = objectMapper.readTree("{\"message\":\"Halo\"}");

		return new ResponseEntity<Object>(returnData, new HttpHeaders(), HttpStatus.OK);
	}

    @GetMapping(value = "/")
    public Object cbHandler() throws IOException {
		LOGGER.info("*** CB HANDLER ***");

        ObjectMapper objectMapper = new ObjectMapper();
		JsonNode returnData = objectMapper.readTree("{\"message\":\"NOT IMPLEMENTED\"}");

		return new ResponseEntity<Object>(returnData, new HttpHeaders(), HttpStatus.OK);
    }
}
