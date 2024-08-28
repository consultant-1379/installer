/**
 * 
 */
package com.distocraft.dc5000.install.ant;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author ecarbjo Encryption class for crypting AES
 */
public class AESCrypter {

  /**
   * These are the encryption methods to use.
   */
  public final static String DEFAULT_CIPHER_NAME = "AES";

  public final static String DEFAULT_METHOD = "ECB";

  public final static String DEFAULT_PADDING = "PKCS5Padding";

  // variables that will contain the cipher types.
  private final String cipherName;

  private final String cipherMethod;

  /**
   * Default constructor
   */
  public AESCrypter() {
    cipherName = DEFAULT_CIPHER_NAME;
    cipherMethod = "/" + DEFAULT_METHOD + "/" + DEFAULT_PADDING;
  }

  /**
   * @param is
   *          the InputStream to read the data from.
   * @param os
   *          the OutputStream to
   * @return
   */
  public Key encrypt(final InputStream is, final OutputStream os) {
    CipherOutputStream cos = null;
    final Key key = getRandomKey();

    try {
      // initialize the cipher.
      final Cipher cipher = Cipher.getInstance(this.cipherName + this.cipherMethod);
      cipher.init(Cipher.ENCRYPT_MODE, key);

      // initialize the output stream.
      cos = new CipherOutputStream(os, cipher);

      // read the input and write via the cipher output.
      int in;
      while ((in = is.read()) != -1) {
        cos.write(in);
      }

      // closing the crypt stream
      cos.close();
    } catch (Exception e) {
      System.out.println("Caught an exception while encrypting!");
      e.printStackTrace();
      return null;
    }

    // return the generated key so that it can be used later.
    return key;

  }

  /**
   * Decrypt the contents of the in stream and write it back to the outstream.
   * 
   * @param is
   * @param os
   * @param key
   * @throws Exception
   */
  public void decrypt(final InputStream is, final OutputStream os, final Key key) throws Exception {
    try {
      final Cipher cipher = Cipher.getInstance(this.cipherName + this.cipherMethod);
      cipher.init(Cipher.DECRYPT_MODE, key);

      final CipherInputStream cis = new CipherInputStream(is, cipher);

      int in;
      while ((in = cis.read()) != -1) {
        os.write(in);
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("Decryption failed: " + e.getMessage());
    }
  }

  /**
   * @return a random AES key.
   */
  public static Key getRandomKey() {
    return new SecretKeySpec(getRandomBytes(16), DEFAULT_CIPHER_NAME);
  }

  /**
   * Return <i>num</i> number of bytes of random data.
   * 
   * @param num
   *          the number of bytes to return.
   * @return an array of random bytes.
   */
  private static byte[] getRandomBytes(final int num) {
    // note that this is not a truly (/secure) random data generator. It needs
    // to be updated to use something other than Math.random() to be more than
    // pseudo-random.
    if (num > 0) {
      byte[] bytes = new byte[num];
      for (int i = 0; i < bytes.length; i++) {
        bytes[i] = (byte) Math.round(Math.random() * Byte.MAX_VALUE);
      }

      return bytes;
    } else {
      return null;
    }
  }
}
