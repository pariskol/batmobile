package gr.kgdev.batmobile.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;

public class MultiUserEncryptionUtils extends EncryptionUtils{

	private static Map<String, KeysetHandle> keysMap = new HashMap<>();
	
	public static void loadKeys(String rootPath) {
		try {
			Files.walk(Paths.get(rootPath))
	        .filter(Files::isRegularFile)
	        .filter(path -> path.toString().endsWith(".json"))
	        .forEach(path -> {
	        	try {
					keysMap.put(path.getFileName().toString().replace(".json", ""), readFileKey(path.toString()));
		        } catch (Exception e) {
		        		e.printStackTrace();
				}
	        });
		} catch (IOException e) {
        		e.printStackTrace();
		}
	}
	
	public static void init(String rootPath) {
		try {
			AeadConfig.register();
			loadKeys(rootPath);
		} catch (Exception e) {
			System.err.println(EncryptionUtils.class.getName() + " could not be initialized!");
			e.printStackTrace();
		}
	}
	
	public static String encrypt(String plaintext, String user) {
		try {
			Aead aead = keysMap.get(user).getPrimitive(Aead.class);
			byte[] ciphertext = aead.encrypt(plaintext.getBytes(), new byte[0]);
			return Base64.encodeBase64String(ciphertext);
		} catch (Exception e) {
			return plaintext;
		}
	}
	
	public static String decrypt(String base64EncodedAndEncryptedText, String user) {
		if (!Base64.isBase64(base64EncodedAndEncryptedText))
			return base64EncodedAndEncryptedText;
		
		try {
			byte[] ciphertext = Base64.decodeBase64(base64EncodedAndEncryptedText);
			Aead aead = keysMap.get(user).getPrimitive(Aead.class);
			byte[] decrypted = aead.decrypt(ciphertext, new byte[0]);
			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (Exception e) {
			return base64EncodedAndEncryptedText;
		}
	}
}
