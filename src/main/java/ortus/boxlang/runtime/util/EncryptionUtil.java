/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ortus.boxlang.runtime.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.IntStream;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ortus.boxlang.runtime.dynamic.casters.StringCaster;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.exceptions.BoxIOException;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

/**
 * A utility class for encryption and encoding
 */
public final class EncryptionUtil {

	/**
	 * The default algorithm to use
	 */
	public static final String	DEFAULT_HASH_ALGORITHM			= "MD5";

	/**
	 * Default encryptiion algorithm
	 */
	public static final String	DEFAULT_ENCRYPTION_ALGORITHM	= "AES";

	/**
	 * Default key size
	 */
	public static final int		DEFAULT_ENCRYPTION_KEY_SIZE		= 256;

	/**
	 * The default encoding to use
	 */
	public static final String	DEFAULT_ENCODING				= "UTF-8";

	/**
	 * Supported key algorithms
	 * <a href="https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#keyfactory-algorithms">key factory algorithms</a>
	 */
	public static final IStruct	KEY_ALGORITHMS					= Struct.of(
	    Key.of( "AES" ), "AES",
	    Key.of( "ARCFOUR" ), "ARCFOUR",
	    Key.of( "Blowfish" ), "Blowfish",
	    Key.of( "ChaCha20" ), "ChaCha20",
	    Key.of( "DES" ), "DES",
	    Key.of( "DESede" ), "DESede",
	    Key.of( "HmacMD5" ), "HmacMD5",
	    Key.of( "HmacSHA1" ), "HmacSHA1",
	    Key.of( "HmacSHA224" ), "HmacSHA224",
	    Key.of( "HmacSHA256" ), "HmacSHA256",
	    Key.of( "HmacSHA384" ), "HmacSHA384",
	    Key.of( "HmacSHA512" ), "HmacSHA512",
	    Key.of( "HmacSHA3-224" ), "HmacSHA3-224",
	    Key.of( "HmacSHA3-256" ), "HmacSHA3-256",
	    Key.of( "HmacSHA3-384" ), "HmacSHA3-384",
	    Key.of( "HmacSHA3-512" ), "HmacSHA3-512"
	);

	/**
	 * Performs a hash of a an object using the default algorithm
	 *
	 * @param object The object to be hashed
	 *
	 * @return returns the hashed string
	 */
	public static String hash( Object object ) {
		return hash( object, DEFAULT_HASH_ALGORITHM );
	}

	/**
	 * Performs a hash of an object using a supported algorithm
	 *
	 * @param object    The object to be hashed
	 * @param algorithm The supported {@link java.security.MessageDigest } algorithm (case-insensitive)
	 *
	 * @return returns the hashed string
	 */
	public static String hash( Object object, String algorithm ) {
		if ( object instanceof byte[] ) {
			return hash( ( byte[] ) object, algorithm, 1 );
		} else {
			return hash( object.toString().getBytes(), algorithm, 1 );
		}
	}

	/**
	 * Iterative hash of a byte array
	 *
	 * @param byteArray
	 * @param algorithm
	 * @param iterations
	 *
	 * @return
	 */
	public static String hash( byte[] byteArray, String algorithm, int iterations ) {
		String result = null;
		try {
			MessageDigest md = MessageDigest.getInstance( algorithm.toUpperCase() );
			for ( int i = 0; i < iterations; i++ ) {
				try {
					md.reset();
					MessageDigest mdc = ( MessageDigest ) md.clone();
					mdc.update( byteArray );
					byte[] digest = mdc.digest();
					byteArray	= digest;
					result		= digestToString( digest );
				} catch ( CloneNotSupportedException e ) {
					throw new BoxRuntimeException(
					    String.format(
					        "The clone operation is not supported using the algorithm [%s].",
					        algorithm.toUpperCase()
					    )
					);
				}
			}
		} catch ( NoSuchAlgorithmException e ) {
			throw new BoxRuntimeException(
			    String.format(
			        "The algorithm [%s] provided is not a valid digest algorithm.",
			        algorithm.toUpperCase()
			    )
			);
		}
		return result;
	}

	/**
	 * Stringifies a digest
	 *
	 * @param digest The digest
	 *
	 * @return the strigified result
	 */
	public static String digestToString( byte[] digest ) {
		StringBuilder result = new StringBuilder();
		IntStream
		    .range( 0, digest.length )
		    .forEach( idx -> result.append( String.format( "%02x", digest[ idx ] ) ) );
		return result.toString();
	}

	/**
	 * Peforms a checksum of a file path object using the MD5 algorithm
	 *
	 * @param filePath The {@link java.nio.file.Path} object
	 *
	 * @return returns the checksum string
	 */
	public static String checksum( Path filePath ) {
		return checksum( filePath, DEFAULT_HASH_ALGORITHM );
	}

	/**
	 * Peforms a checksum of a file path object using a supported algorithm
	 *
	 * @param filePath  The {@link java.nio.file.Path} object
	 * @param algorithm The supported {@link java.security.MessageDigest } algorithm (case-insensitive)
	 *
	 * @return returns the checksum string
	 */
	public static String checksum( Path filePath, String algorithm ) {
		try {
			return hash( Files.readAllBytes( filePath ) );
		} catch ( IOException e ) {
			throw new BoxIOException( e );
		}
	}

	/**
	 * HMAC encodes a byte array using the default encoding
	 *
	 * @param encryptItem The byte array to encode
	 * @param key         The key to use
	 * @param algorithm   The algorithm to use
	 * @param encoding    The encoding to use
	 *
	 * @return returns the HMAC encoded string
	 */
	public static String hmac( byte[] encryptItem, String key, String algorithm, String encoding ) {
		Charset charset = Charset.forName( encoding );
		// Attempt to keep the correct casing on the key
		algorithm = ( String ) KEY_ALGORITHMS.getOrDefault( Key.of( algorithm ), algorithm );
		try {
			Mac				mac			= Mac.getInstance( algorithm );
			SecretKeySpec	secretKey	= new SecretKeySpec( key.getBytes( charset ), algorithm );
			mac.init( secretKey );
			mac.reset();
			return digestToString( mac.doFinal( encryptItem ) );
		} catch ( NoSuchAlgorithmException e ) {
			throw new BoxRuntimeException(
			    String.format(
			        "The algorithm [%s] provided is not a valid digest algorithm.",
			        algorithm
			    ),
			    e
			);
		} catch ( InvalidKeyException e ) {
			throw new BoxRuntimeException(
			    String.format(
			        "The the key provided, [%s], is not a valid encryption key.",
			        key
			    ),
			    e
			);
		} catch ( IllegalStateException e ) {
			throw new BoxRuntimeException(
			    "An illegal state exception occurred",
			    e
			);
		}
	}

	/**
	 * HMAC encodes an object using the default encoding
	 *
	 * @param input     The object to encode
	 * @param key       The key to use
	 * @param algorithm The algorithm to use
	 * @param encoding  The encoding to use
	 *
	 * @return returns the HMAC encoded string
	 */
	public static String hmac( Object input, String key, String algorithm, String encoding ) {
		Charset	charset		= Charset.forName( encoding );
		byte[]	encryptItem	= null;

		if ( input instanceof String ) {
			encryptItem = StringCaster.cast( input ).getBytes( charset );
		} else {
			encryptItem = input.toString().getBytes( charset );
		}

		return hmac( encryptItem, key, algorithm, encoding );

	}

	/**
	 * Base64 encodes an object using the default encoding
	 *
	 * @param item    The object to encode
	 * @param charset The charset to use
	 *
	 * @return returns the base64 encoded string
	 */
	public static String base64Encode( Object item, Charset charset ) {
		byte[] encodeItem = null;
		if ( item instanceof byte[] ) {
			encodeItem = ( byte[] ) item;
		} else if ( item instanceof String ) {
			encodeItem = StringCaster.cast( item ).getBytes( charset );
		} else {
			encodeItem = item.toString().getBytes( charset );
		}
		return Arrays.toString( Base64.getEncoder().encode( encodeItem ) );
	}

	/**
	 * URL encodes a string. We use the default encoding
	 *
	 * @param target The string to encode
	 *
	 * @return returns the URL encoded string
	 */
	public static String urlEncode( String target ) {
		return urlEncode( target, DEFAULT_ENCODING );
	}

	/**
	 * URL encodes a string. We use the default encoding
	 *
	 * @param target   The string to encode
	 * @param encoding The encoding to use
	 *
	 * @return returns the URL encoded string
	 */
	public static String urlEncode( String target, String encoding ) {
		try {
			return URLEncoder.encode( target, encoding );
		} catch ( UnsupportedEncodingException e ) {
			throw new BoxRuntimeException( e.getMessage() );
		}
	}

	/**
	 * URL decodes a string
	 * We use the default encoding
	 *
	 * @param target The string to decode
	 *
	 * @return returns the URL decoded string
	 */
	public static String urlDecode( String target ) {
		return urlDecode( target, DEFAULT_ENCODING );
	}

	/**
	 * URL decodes a string
	 *
	 * @param target   The string to decode
	 * @param encoding The encoding to use
	 *
	 * @return returns the URL decoded string
	 */
	public static String urlDecode( String target, String encoding ) {
		try {
			return URLDecoder.decode( target, encoding );
		} catch ( UnsupportedEncodingException e ) {
			throw new BoxRuntimeException( e.getMessage() );
		}
	}

	/**
	 * Generates a secret key
	 *
	 * @param algorithm The algorithm to use: AES, ARCFOUR, Blowfish, ChaCha20, DES, DESede, HmacMD5, HmacSHA1, HmacSHA224, HmacSHA256, HmacSHA384, HmacSHA512, HmacSHA3-224, HmacSHA3-256, HmacSHA3-384, HmacSHA3-512
	 * @param keySize   The key size
	 *
	 * @return returns the secret key
	 */
	public static SecretKey generateKey( String algorithm, int keySize ) {
		algorithm = ( String ) KEY_ALGORITHMS.getOrDefault( Key.of( algorithm ), algorithm );
		try {
			// 1. Obtain an instance of KeyGenerator for the specified algorithm.
			KeyGenerator keyGenerator = KeyGenerator.getInstance( algorithm );
			// 2. Initialize the key generator with a specific key size.
			keyGenerator.init( keySize );
			// 3. Generate a secret key.
			return keyGenerator.generateKey();
		} catch ( NoSuchAlgorithmException e ) {
			throw new BoxRuntimeException(
			    String.format(
			        "The algorithm [%s] provided is not a valid key algorithm or it has not been loaded properly.",
			        algorithm
			    ),
			    e
			);
		}
	}

	/**
	 * Generates a secret key using the default algorithm and key size
	 *
	 * @param algorithm The algorithm to use: AES, ARCFOUR, Blowfish, ChaCha20, DES, DESede, HmacMD5, HmacSHA1, HmacSHA224, HmacSHA256, HmacSHA384, HmacSHA512, HmacSHA3-224, HmacSHA3-256, HmacSHA3-384, HmacSHA3-512
	 * @param keySize   The key size
	 *
	 * @return returns the secret key
	 */
	public static String generateKeyAsString( String algorithm, int keySize ) {
		return convertSecretKeyToString( generateKey( algorithm, keySize ) );
	}

	/**
	 * Generates a secret key using the default algorithm and key size
	 */
	public static SecretKey generateKey() {
		return generateKey( DEFAULT_ENCRYPTION_ALGORITHM, DEFAULT_ENCRYPTION_KEY_SIZE );
	}

	/**
	 * Generates a secret key using the default algorithm and key size
	 */
	public static String generateKeyAsString() {
		return convertSecretKeyToString( generateKey() );
	}

	/**
	 * Converts a secret key to a string
	 *
	 * @param secretKey The secret key
	 *
	 * @return returns the secret key as a string
	 */
	public static String convertSecretKeyToString( SecretKey secretKey ) {
		// Get the secret key bytes
		byte[] rawData = secretKey.getEncoded();
		// Encode the bytes to a Base64 string
		return Base64.getEncoder().encodeToString( rawData );
	}

}
