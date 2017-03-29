package de.dailab.jiactng.agentcore.util.sec;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

/**
 * This class provides methods for encryption and decryption of short data using public and private RSA keys.
 * @author Jan Keiser
 */
public final class CryptoRSA {

	private static final String ASYMMETRIC_ALGO = "RSA";
	private static final int RSA_KEY_SIZE = 2048;

	/** The maximum length of input data in bytes, that can be encrypted. */
	public static final int ENCRYPTION_INPUT_LENGTH = 245;
	/** The length of every encryption result in bytes. */
	public static final int ENCRYPTION_OUTPUT_LENGTH = 256;

	/**
	 * Generates a private and a public RSA key. Both streams will be closed.
	 * @param privateKeyFile the stream where the private key will be written
	 * @param publicKeyFile the stream where the public key will be written
	 * @throws NoSuchAlgorithmException if no Provider supports a KeyPairGeneratorSpi implementation for RSA
	 * @throws IOException if the keys can not be written to the streams
	 */
	public static void generateKeyPair( OutputStream privateKeyFile, OutputStream publicKeyFile ) throws NoSuchAlgorithmException, IOException {
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance( ASYMMETRIC_ALGO );
			keyPairGen.initialize( RSA_KEY_SIZE );
			KeyPair keyPair = keyPairGen.generateKeyPair();
			ObjectOutputStream out = new ObjectOutputStream( publicKeyFile );
			try { out.writeObject( keyPair.getPublic() ); } finally { out.close(); }
			out = new ObjectOutputStream( privateKeyFile );
			try { out.writeObject( keyPair.getPrivate() ); } finally { out.close(); }
		} finally {
			privateKeyFile.close();
			publicKeyFile.close();
		}
	}

	/**
	 * Gets the key object from a stream. The stream will be closed.
	 * @param key the stream containing the key object
	 * @return the key object
	 * @throws ClassNotFoundException if the stream contains an object of an unknown class
	 * @throws IOException if the stream can not be read
	 */
	public static Key getKey(InputStream key) throws IOException, ClassNotFoundException {
		try {
			Key result;
			ObjectInputStream keyIn = new ObjectInputStream( key );
			try { result = (Key) keyIn.readObject(); } finally { keyIn.close(); }
			return result;
		} 
		finally {
			key.close();
		}
	}

	/**
	 * Encrypts short data using a public or private RSA key. The encrypted data can be 
	 * decrypted by using the corresponding private or public key. The stream will be closed.
	 * @param keyFile the stream of the RSA key
	 * @param input the input buffer
	 * @return the new buffer with the RSA encrypted data
	 * @throws GeneralSecurityException if RSA provider is not available, the key is invalid or the encryption fails
	 * @throws ClassNotFoundException if the stream contains an object of an unknown class
	 * @throws IOException if the stream can not be read
	 */
	public static byte[] encrypt( InputStream keyFile, byte[] input ) throws GeneralSecurityException, ClassNotFoundException, IOException {
		return transform(keyFile, Cipher.ENCRYPT_MODE, input);
	}

	/**
	 * Encrypts short data using a public or private RSA key. The encrypted data can be 
	 * decrypted by using the corresponding private or public key. The stream will be closed.
	 * @param keyFile the stream of the RSA key
	 * @param input the input buffer
	 * @param inputOffset the offset in input where the input starts
	 * @param inputLen the input length
	 * @return the new buffer with the RSA encrypted data
	 * @throws GeneralSecurityException if RSA provider is not available, the key is invalid or the encryption fails
	 * @throws ClassNotFoundException if the stream contains an object of an unknown class
	 * @throws IOException if the stream can not be read
	 */
	public static byte[] encrypt( InputStream keyFile, byte[] input, int inputOffset, int inputLen ) throws GeneralSecurityException, ClassNotFoundException, IOException {
		return transform(keyFile, Cipher.ENCRYPT_MODE, input, inputOffset, inputLen);
	}

	/**
	 * Encrypts short data using a public or private RSA key. The encrypted data can be 
	 * decrypted by using the corresponding private or public key. The stream will be closed.
	 * @param keyFile the stream of the RSA key
	 * @param input the input buffer
	 * @param inputOffset the offset in input where the input starts
	 * @param inputLen the input length
	 * @param output the buffer for the RSA encrypted data
	 * @param outputOffset the offset in output where the RSA encrypted data are stored
	 * @return the number of bytes stored in output
	 * @throws GeneralSecurityException if RSA provider is not available, the key is invalid or the encryption fails
	 * @throws ClassNotFoundException if the stream contains an object of an unknown class
	 * @throws IOException if the stream can not be read
	 */
	public static int encrypt( InputStream keyFile, byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset ) throws GeneralSecurityException, ClassNotFoundException, IOException {
		return transform(keyFile, Cipher.ENCRYPT_MODE, input, inputOffset, inputLen, output, outputOffset);
	}

	/**
	 * Encrypts short data using a public or private RSA key. The encrypted data can be 
	 * decrypted by using the corresponding private or public key.
	 * @param key the key object
	 * @param input the input buffer
	 * @return the new buffer with the RSA encrypted data
	 * @throws GeneralSecurityException if RSA provider is not available, the key is invalid or the encryption fails
	 */
	public static byte[] encrypt( Key key, byte[] input ) throws GeneralSecurityException {
		return transform(key, Cipher.ENCRYPT_MODE, input);
	}

	/**
	 * Encrypts short data using a public or private RSA key. The encrypted data can be 
	 * decrypted by using the corresponding private or public key.
	 * @param key the key object
	 * @param input the input buffer
	 * @param inputOffset the offset in input where the input starts
	 * @param inputLen the input length
	 * @return the new buffer with the RSA encrypted data
	 * @throws GeneralSecurityException if RSA provider is not available, the key is invalid or the encryption fails
	 */
	public static byte[] encrypt( Key key, byte[] input, int inputOffset, int inputLen ) throws GeneralSecurityException {
		return transform(key, Cipher.ENCRYPT_MODE, input, inputOffset, inputLen);
	}

	/**
	 * Encrypts short data using a public or private RSA key. The encrypted data can be 
	 * decrypted by using the corresponding private or public key.
	 * @param key the key object
	 * @param input the input buffer
	 * @param inputOffset the offset in input where the input starts
	 * @param inputLen the input length
	 * @param output the buffer for the RSA encrypted data
	 * @param outputOffset the offset in output where the RSA encrypted data are stored
	 * @return the number of bytes stored in output
	 * @throws GeneralSecurityException if RSA provider is not available, the key is invalid or the encryption fails
	 */
	public static int encrypt( Key key, byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset ) throws GeneralSecurityException {
		return transform(key, Cipher.ENCRYPT_MODE, input, inputOffset, inputLen, output, outputOffset);
	}

	/**
	 * Decrypts short data using a public or private RSA key. The given data must be 
	 * encrypted with the corresponding private or public key. The stream will be closed.
	 * @param keyFile the stream of the RSA key
	 * @param input the buffer with the RSA encrypted data
	 * @return the new buffer with the original data
	 * @throws GeneralSecurityException if RSA provider is not available, the key is invalid or the decryption fails
	 * @throws ClassNotFoundException if the stream contains an object of an unknown class
	 * @throws IOException if the stream can not be read
	 */
	public static byte[] decrypt( InputStream keyFile, byte[] input ) throws GeneralSecurityException, ClassNotFoundException, IOException {
		return transform(keyFile, Cipher.DECRYPT_MODE, input);
	}

	/**
	 * Decrypts short data using a public or private RSA key. The given data must be 
	 * encrypted with the corresponding private or public key. The stream will be closed.
	 * @param keyFile the stream of the RSA key
	 * @param input the buffer with the RSA encrypted data
	 * @param inputOffset the offset in input where the input starts
	 * @param inputLen the input length
	 * @return the new buffer with the original data
	 * @throws GeneralSecurityException if RSA provider is not available, the key is invalid or the decryption fails
	 * @throws ClassNotFoundException if the stream contains an object of an unknown class
	 * @throws IOException if the stream can not be read
	 */
	public static byte[] decrypt( InputStream keyFile, byte[] input, int inputOffset, int inputLen ) throws GeneralSecurityException, ClassNotFoundException, IOException {
		return transform(keyFile, Cipher.DECRYPT_MODE, input, inputOffset, inputLen);
	}

	/**
	 * Decrypts short data using a public or private RSA key. The given data must be 
	 * encrypted with the corresponding private or public key. The stream will be closed.
	 * @param keyFile the stream of the RSA key
	 * @param input the buffer with the RSA encrypted data
	 * @param inputOffset the offset in input where the input starts
	 * @param inputLen the input length
	 * @param output the buffer for the original data
	 * @param outputOffset the offset in output where the original data are stored
	 * @return the number of bytes stored in output
	 * @throws GeneralSecurityException if RSA provider is not available, the key is invalid or the decryption fails
	 * @throws ClassNotFoundException if the stream contains an object of an unknown class
	 * @throws IOException if the stream can not be read
	 */
	public static int decrypt( InputStream keyFile, byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset ) throws GeneralSecurityException, ClassNotFoundException, IOException {
		return transform(keyFile, Cipher.DECRYPT_MODE, input, inputOffset, inputLen, output, outputOffset);
	}

	/**
	 * Decrypts short data using a public or private RSA key. The given data must be 
	 * encrypted with the corresponding private or public key.
	 * @param key the key object
	 * @param input the buffer with the RSA encrypted data
	 * @return the new buffer with the original data
	 * @throws GeneralSecurityException if RSA provider is not available, the key is invalid or the decryption fails
	 */
	public static byte[] decrypt( Key key, byte[] input ) throws GeneralSecurityException {
		return transform(key, Cipher.DECRYPT_MODE, input);
	}

	/**
	 * Decrypts short data using a public or private RSA key. The given data must be 
	 * encrypted with the corresponding private or public key.
	 * @param key the key object
	 * @param input the buffer with the RSA encrypted data
	 * @param inputOffset the offset in input where the input starts
	 * @param inputLen the input length
	 * @return the new buffer with the original data
	 * @throws GeneralSecurityException if RSA provider is not available, the key is invalid or the decryption fails
	 */
	public static byte[] decrypt( Key key, byte[] input, int inputOffset, int inputLen ) throws GeneralSecurityException {
		return transform(key, Cipher.DECRYPT_MODE, input, inputOffset, inputLen);
	}

	/**
	 * Decrypts short data using a public or private RSA key. The given data must be 
	 * encrypted with the corresponding private or public key.
	 * @param key the key object
	 * @param input the buffer with the RSA encrypted data
	 * @param inputOffset the offset in input where the input starts
	 * @param inputLen the input length
	 * @param output the buffer for the original data
	 * @param outputOffset the offset in output where the original data are stored
	 * @return the number of bytes stored in output
	 * @throws GeneralSecurityException if RSA provider is not available, the key is invalid or the decryption fails
	 */
	public static int decrypt( Key key, byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset ) throws GeneralSecurityException {
		return transform(key, Cipher.DECRYPT_MODE, input, inputOffset, inputLen, output, outputOffset);
	}

	private static byte[] transform( InputStream keyFile, int cipherMode, byte[] input ) throws GeneralSecurityException, ClassNotFoundException, IOException {
		return transform( getKey(keyFile), cipherMode, input );
	}

	private static byte[] transform( InputStream keyFile, int cipherMode, byte[] input, int inputOffset, int inputLen ) throws GeneralSecurityException, ClassNotFoundException, IOException {
		return transform( getKey(keyFile), cipherMode, input, inputOffset, inputLen );
	}

	private static int transform( InputStream keyFile, int cipherMode, byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset ) throws GeneralSecurityException, ClassNotFoundException, IOException {
		return transform( getKey(keyFile), cipherMode, input, inputOffset, inputLen, output, outputOffset );
	}

	private static byte[] transform( Key key, int cipherMode, byte[] input ) throws GeneralSecurityException {
		return transform( key, cipherMode, input, 0, input.length );
	}

	private static byte[] transform( Key key, int cipherMode, byte[] input, int inputOffset, int inputLen ) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance( ASYMMETRIC_ALGO );
		cipher.init( cipherMode, key );				
		return cipher.doFinal(input, inputOffset, inputLen);
	}

	private static int transform( Key key, int cipherMode, byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset ) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance( ASYMMETRIC_ALGO );
		cipher.init( cipherMode, key );				
		return cipher.doFinal(input, inputOffset, inputLen, output, outputOffset);
	}

	/**
	 * Converts a byte array (e.g. encrypted data or public key) to a
	 * hexadecimal string to avoid non-printable or special characters, that
	 * may induce problems in some cases (e.g. JSON mapping). The hexadecimal 
	 * string has always the double length of the corresponding byte array.
	 * @param data the input data
	 * @return the String in hexadecimal notation
	 */
	public static String toHexString(byte[] data) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<data.length; i++) {
			final String hex = Integer.toHexString(Byte.toUnsignedInt(data[i]));
			if (hex.length() == 1) {
				sb.append("0");
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	/**
	 * Converts a hexadecimal string back to a byte array. The hexadecimal 
	 * string has always the double length of the corresponding byte array.
	 * @param hex the String in hexadecimal notation
	 * @return the original byte array
	 */
	public static byte[] fromHexString(String hex) {
		byte[] data = new byte[hex.length()/2];
		for (int i=0; i<data.length; i++) {
			data[i] = Integer.valueOf(hex.substring(2*i, 2*i+2), 16).byteValue();
		}
		return data;
	}
}
