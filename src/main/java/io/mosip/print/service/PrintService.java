package io.mosip.print.service;

import java.util.Map;

import org.idpass.lite.IDPassReader;
import org.idpass.lite.exceptions.IDPassException;
import org.api.proto.*;
import com.google.protobuf.ByteString;

import org.springframework.stereotype.Component;

@Component
public class PrintService {

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

    public String print(Map map){
        //TODO: implement the logic to print the card here

        System.out.println("*** IDPASS LITE QR CODE CARD ***");
        return "*** IDPASS LITE QR CODE CARD ***";
    }
}
