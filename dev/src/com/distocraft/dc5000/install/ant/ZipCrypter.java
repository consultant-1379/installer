/**
 * 
 */
package com.distocraft.dc5000.install.ant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author ecarbjo
 * 
 */
public class ZipCrypter extends Task {

  // used by the isValidKeyPair() method.
  public static String testString = "This is a small test string to check that the encryption works!";

  // The methods to encrypt the symmetrical key.
  public final static String DEFAULT_CIPHER = "RSA/ECB/PKCS1Padding";

  public final static String DEFAULT_CIPHER_NAME = "RSA";

  // This is the default decryption public key. Used if no key is added.
  public final static BigInteger DEFAULT_KEY_MOD = new BigInteger(
      "123355219375882378192369770441285939191353866566017497282747046709534536708757928527167390021388683110840288891057176815668475724440731714035455547579744783774075008195670576737607241438665521837871490309744873315551646300131908174140715653425601662203921855253249615512397376967139410627761058910648132466577");

  public final static BigInteger DEFAULT_KEY_EXP = new BigInteger("65537");

  // this is the number of bytes that a block of plain text will contain. Can
  // be at most (key.length - 11) bytes.
  public final static int CIPHER_BLOCK_PLAIN = 104;

  // this is the number of bytes that the crypted block will contain. Note
  // that this is completely tied to the length of the key that is used.
  public final static int CIPHER_BLOCK_CRYPT = 128;

  protected int cryptMode = Cipher.DECRYPT_MODE;

  protected File fileTarget = null;

  protected BigInteger keyMod = null;

  protected BigInteger keyExp = null;

  protected boolean isPublic = true;

  protected Key rsaKey = null;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException {
    initKey();

    // check that the file string has been specified
    if (fileTarget == null) {
      throw new BuildException("The target file has not been specified.");
    } else {
      // buffer stream for the output of the crypting (since we cannot
      // write
      // back to the same file while reading it.
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final ZipOutputStream zos = new ZipOutputStream(bos);
      zos.setMethod(ZipOutputStream.DEFLATED);

      try {
        // open the .zip file.
        final ZipFile zf = new ZipFile(fileTarget);
        final Enumeration<? extends ZipEntry> entries = zf.entries();

        // read all entries and encrypt/decrypt them.
        while (entries.hasMoreElements()) {
          final ZipEntry ze = entries.nextElement();

          if (ze.isDirectory()) {
            continue;
          } else {
            // log a little.
            final long startTime = System.currentTimeMillis();
            System.out.println("Processing .zip entry: " + ze.getName());

            // create a new entry for the output
            final ZipEntry newEntry = new ZipEntry(ze.getName());

            // get the crypted data from the ZipInputStream
            final ZipCrypterDataEntry output = cryptInputStream(zf.getInputStream(ze), cryptMode, ze.getExtra(), this.rsaKey);

            // update the metadata.
            newEntry.setSize(output.getSize());
            newEntry.setExtra(output.getExtra());
            newEntry.setTime(ze.getTime());

            // create the CRC. Not strictly needed for deflated
            // data, but can't hurt.
            final CRC32 crc = new CRC32();
            crc.update(output.getData());
            newEntry.setCrc(crc.getValue());

            // write the data to the zip stream.
            zos.putNextEntry(newEntry);
            zos.write(output.getData());
            zos.closeEntry();

            // for benchmarking.
            final double totalTime = (System.currentTimeMillis() - startTime) / 1000.0;
            System.out.println("Processed " + ze.getSize() + " bytes in " + totalTime + " seconds.");

          }
        }

        // close the .zip.
        zf.close();

        // and the stream.
        zos.flush();
        zos.close();

        // open the file again for write.
        if (!fileTarget.exists() || fileTarget.canWrite()) {
          try {
            // and write the zipped data back to the file.
            final OutputStream outStream = new FileOutputStream(fileTarget);
            bos.flush();

            outStream.write(bos.toByteArray());

            // flush and close the streams
            outStream.flush();
            outStream.close();
            bos.close();
          } catch (FileNotFoundException e) {
            throw new BuildException("Could not find the file.");
          }
        }
      } catch (Exception e) {
        throw new BuildException(e.getMessage(), e);
      }
    }
  }

  protected ZipCrypterDataEntry cryptInputStream(final InputStream is, final int mode, final byte[] extra, final Key key) throws BuildException {
    // this is crypter data entry used for return.
    final ZipCrypterDataEntry returnEntry = new ZipCrypterDataEntry();

    // initialize the AES crypter for this file.
    final AESCrypter aes = new AESCrypter();

    // create a new buffer for the zip entry.
    final ByteArrayOutputStream entryBos = new ByteArrayOutputStream();
    Key aesKey = null;

    // encrypt or decrypt the stream.
    if (mode == Cipher.DECRYPT_MODE) {
      try {
        aesKey = decryptAESKey(extra, key);
        if (aesKey == null) {
          // assume that the given data is not encrypted. Just pass
          // through the data. We can't do anything anyway without a key.
          int in;
          while ((in = is.read()) != -1) {
            entryBos.write(in);
          }
        } else {
          aes.decrypt(is, entryBos, aesKey);
        }
      } catch (Exception e) {
        throw new BuildException(e.getMessage(), e);
      }
    } else {
      // encrypt the data. The encrypt method will return a random AES key
      // that
      // is encrypted and stored in the DataEntry object.
      aesKey = aes.encrypt(is, entryBos);

      // if the key is null, then the generation of the
      // random AES key has failed. Throw an exception.
      if (aesKey == null) {
        throw new BuildException("AES Key generation failed!");
      }

      try {
        returnEntry.setExtra(encryptAESKey(key, aesKey));
      } catch (Exception e) {
        throw new BuildException(e.getMessage(), e);
      }
    }

    // save the buffered data into the dataentry
    returnEntry.setData(entryBos.toByteArray());

    // return the crypted dataentry.
    return returnEntry;
  }

  /**
   * Encrypt the session key with the RSA key.
   * 
   * @param rsaKey2
   * @param aesKey
   * @return
   * @throws NoSuchAlgorithmException
   * @throws NoSuchPaddingException
   * @throws InvalidKeyException
   * @throws IllegalBlockSizeException
   * @throws BadPaddingException
   */
  protected byte[] encryptAESKey(final Key rsaKey2, final Key aesKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    final Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER);
    cipher.init(Cipher.ENCRYPT_MODE, rsaKey2);

    final byte[] key = aesKey.getEncoded();

    // System.out.print("DEBUG: Encrypting AES key: 0x");
    /*
     * for (int i = 0; i < key.length; i++) {
     * System.out.print(Integer.toHexString(key[i])); } System.out.print("\n");
     */

    return cipher.doFinal(key);

  }

  /**
   * Decrypt the AES session key that is contained in the byte array given.
   * 
   * @param encrypted
   * @param rsaKey2
   * @return
   */
  public Key decryptAESKey(final byte[] encrypted, final Key rsaKey2) throws Exception {
    if (encrypted == null || encrypted.length == 0) {
      // There is no data to decrypt, hence return null.
      return null;
    } else {
      final Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER);
      cipher.init(Cipher.DECRYPT_MODE, rsaKey2);

      // If there is data to decrypt, try to do so, and return the key.
      // return null if decryption fails.
      byte[] key;
      try {
        key = cipher.doFinal(encrypted);
      } catch (Exception e) {
        System.out.println("WARNING: Supplied data could not be decrypted as a key! Assuming plain-text file content.");
        return null;
      }

      // System.out.print("DEBUG: Decrypted AES key: 0x");
      /*
       * for (int i = 0; i < key.length; i++) {
       * System.out.print(Integer.toHexString(key[i])); }
       * System.out.print("\n");
       */

      return new SecretKeySpec(key, AESCrypter.DEFAULT_CIPHER_NAME);
    }
  }

  /**
   * Create the keys from the ANT script inputs.
   */
  protected void initKey() throws BuildException {
    // Check the key attributes and create the key.
    if (keyMod == null || keyExp == null) {
      System.out.println("Using default public key.");
      keyMod = DEFAULT_KEY_MOD;
      keyExp = DEFAULT_KEY_EXP;
    }
    // check if the key is public or not to know which spec to use
    // (which method to use)
    if (isPublic) {
      this.rsaKey = getPublicKey(keyMod, keyExp);
    } else {
      this.rsaKey = getPrivateKey(keyMod, keyExp);
    }
  }

  /**
   * You can get the keys needed from for example ZipCrypter.getPrivateKey() and
   * ZipCrypter.getPublicKey()
   * 
   * @param pubKey
   *          the public key to be checked
   * @param privKey
   *          the private key to be checked
   * @return true if the RSA key pair works, false otherwise.
   */
  public static boolean isValidKeyPair(final PublicKey pubKey, final PrivateKey privKey) {
    try {
      final Cipher encrypter = Cipher.getInstance(DEFAULT_CIPHER);
      encrypter.init(Cipher.ENCRYPT_MODE, privKey);

      final Cipher decrypter = Cipher.getInstance(DEFAULT_CIPHER);
      decrypter.init(Cipher.DECRYPT_MODE, pubKey);

      final byte[] encrypted = encrypter.doFinal(testString.getBytes());
      final byte[] decrypted = decrypter.doFinal(encrypted);

      final String result = new String(decrypted);
      return result.equals(testString);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * @param pubMod
   *          the data for the modulate
   * @param pubExp
   *          the data for the exponent
   * @return the PublicKey object for this key
   * @throws BuildException
   */
  public static PublicKey getPublicKey(final BigInteger pubMod, final BigInteger pubExp) throws BuildException {
    try {
      final RSAPublicKeySpec keySpec = new RSAPublicKeySpec(pubMod, pubExp);
      final KeyFactory keyFactory = KeyFactory.getInstance(DEFAULT_CIPHER_NAME);
      return keyFactory.generatePublic(keySpec);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new BuildException("Could not initialize the KeyFactory");
    } catch (InvalidKeySpecException e) {
      throw new BuildException("Invalid key specification");
    }
  }

  /**
   * @param privMod
   *          the data for the modulate
   * @param privExp
   *          the data for the exponent
   * @return the PrivateKey object for this key
   * @throws BuildException
   */
  public static PrivateKey getPrivateKey(final BigInteger privMod, final BigInteger privExp) throws BuildException {
    try {
      final RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(privMod, privExp);
      final KeyFactory keyFactory = KeyFactory.getInstance(DEFAULT_CIPHER_NAME);
      return keyFactory.generatePrivate(keySpec);
    } catch (NoSuchAlgorithmException e) {
      throw new BuildException("Could not initialize the KeyFactory");
    } catch (InvalidKeySpecException e) {
      throw new BuildException("Invalid key specification");
    }
  }

  /**
   * @param mod
   */
  public void setKeyModulate(final String mod) {
    // System.out.println("DEBUG: Setting the keymodulate to " + mod);
    keyMod = new BigInteger(mod);
  }

  /**
   * @param mod
   */
  public void setKeyExponent(final String exp) {
    // System.out.println("DEBUG: Setting the keyexponent to " + exp);
    keyExp = new BigInteger(exp);
  }

  /**
   * Set the type of crypting (encryption, decryption)
   * 
   * @param type
   *          the string should be "encrypt" for encrypting the input data, and
   *          "decrypt" for decrypting the input data.
   */
  public void setCryptType(final String type) {
    if (type.equalsIgnoreCase("encrypt")) {
      // System.out.println("DEBUG: Setting the crypt type to 'encrypt'");
      this.cryptMode = Cipher.ENCRYPT_MODE;
    } else {
      // System.out.println("DEBUG: Setting the crypt type to 'decrypt'");
      this.cryptMode = Cipher.DECRYPT_MODE;
    }
  }

  /**
   * @param isPublic
   */
  public void setIsPublicKey(final String isPublic) {
    // System.out.println("DEBUG: Setting public key to " + isPublic);
    this.isPublic = Boolean.parseBoolean(isPublic);
  }

  /**
   * Set the filename to use for encryption
   * 
   * @param file
   */
  public void setFile(final String file) {
    // System.out.println("DEBUG: Setting the file to " + file);
    // check that the file string has been specified
    if (file == "") {
      this.fileTarget = null;
    }

    // check that the file exists and is writable
    fileTarget = new File(file);
    if (!fileTarget.exists() || !fileTarget.canRead()) {
      throw new BuildException("The target file cannot be accessed or does not exist.");
    }
  }

  /**
   * @author ecarbjo Inner data structure class for decrypted/encrypted data.
   */
  public class ZipCrypterDataEntry {

    // contains encrypted/decrypted data.
    private byte[] data;

    private byte[] extra;

    /**
     * @return the crypted data
     */
    public byte[] getData() {
      return data;
    }

    /**
     * @param data
     *          the data
     */
    public void setData(final byte[] data) {
      this.data = data;
    }

    /**
     * @return the extra field. Contains a crypted AES key.
     */
    public byte[] getExtra() {
      return extra;
    }

    /**
     * @param extra
     *          sets the crypted AES key.
     */
    public void setExtra(final byte[] extra) {
      this.extra = extra;
    }

    /**
     * @return the length of the data.
     */
    public int getSize() {
      return data.length;
    }

  }
}
