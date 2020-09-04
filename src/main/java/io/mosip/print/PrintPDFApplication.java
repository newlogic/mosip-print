package io.mosip.print;

import com.google.protobuf.ByteString;
import org.api.proto.KeySet;
import org.api.proto.byteArray;
import org.idpass.lite.IDPassReader;
import org.idpass.lite.exceptions.IDPassException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;

@SpringBootApplication
@ComponentScan(basePackages = {"io.mosip"})
public class PrintPDFApplication {

	public static void idpass_init() {

		byte[] enc = new byte[32];
		byte[] pk = new byte[32];
		byte[] sk = new byte[64];

		IDPassReader.generateEncryptionKey(enc);
		IDPassReader.generateSecretSignatureKeypair(pk, sk);

		KeySet keyset = KeySet.newBuilder()
				.setEncryptionKey(ByteString.copyFrom(enc))
				.setSignatureKey(ByteString.copyFrom(sk))
				.addVerificationKeys(byteArray.newBuilder()
						.setTyp(byteArray.Typ.ED25519PUBKEY)
						.setVal(ByteString.copyFrom(pk)).build())
				.build();

		try {
			IDPassReader reader = new IDPassReader(keyset, null);
			System.out.println("--- idpass init ok ---");
		} catch (IDPassException e) {
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(PrintPDFApplication.class, args);
		idpass_init();
	}

}
