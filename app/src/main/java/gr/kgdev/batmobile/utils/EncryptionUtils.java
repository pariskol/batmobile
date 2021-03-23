package gr.kgdev.batmobile.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import org.apache.commons.codec.binary.Base64;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AesGcmKeyManager;

public class EncryptionUtils {

	private static KeysetHandle keysetHandle;

	public static void init(String filePathStr) {
		try {
			AeadConfig.register();
			keysetHandle = readFileKey(filePathStr);
		} catch (Exception e) {
			System.err.println(EncryptionUtils.class.getName() + " could not be initialized!");
			e.printStackTrace();
		}
	}

	protected static KeysetHandle readFileKey(String filePathStr) throws GeneralSecurityException, IOException {
		return CleartextKeysetHandle.read(JsonKeysetReader.withFile(new File(filePathStr)));
	}

	public static String encrypt(String plaintext) {
		try {
			Aead aead = keysetHandle.getPrimitive(Aead.class);
			byte[] ciphertext = aead.encrypt(plaintext.getBytes(), new byte[0]);
			return Base64.encodeBase64String(ciphertext);
		} catch (Exception e) {
			return plaintext;
		}
	}

	public static String decrypt(String base64EncodedAndEncryptedText) {
		try {
			byte[] ciphertext = Base64.decodeBase64(base64EncodedAndEncryptedText);
			Aead aead = keysetHandle.getPrimitive(Aead.class);
			byte[] decrypted = aead.decrypt(ciphertext, new byte[0]);
			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (Exception e) {
			return base64EncodedAndEncryptedText;
		}
	}

	public static void generateFileKey(String filePathStr) throws GeneralSecurityException, IOException {
		AeadConfig.register();
		KeysetHandle keysetHandle = KeysetHandle.generateNew(AesGcmKeyManager.aes256GcmTemplate());

		String keysetFilename = filePathStr;
		CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(new File(keysetFilename)));
	}

}
