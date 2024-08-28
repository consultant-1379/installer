/**
 * 
 */
package com.distocraft.dc5000.install.ant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.Key;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author ecarbjo
 * 
 */
public class AESCrypterTest {

    static private Key key;

    private final String fileName = "Tech_Pack_DC_E_CUDB";

    private File getFile(final String name) throws Exception {
        final URL url = ClassLoader.getSystemResource(name);
        if (url == null) {
            throw new FileNotFoundException(name);
        }
        return new File(url.toURI());
    }

    private File newFile(final String name) {
        //return new File(System.getProperty("java.io.tmpdir") + "/" + name);
    	return new File(System.getProperty("user.dir")+"/" + name);
    }

    @Test
    public void testAESEncryption() throws Exception {
        final long startTime = System.currentTimeMillis();
        final FileInputStream fis = new FileInputStream(getFile("sql" + File.separator + fileName + ".sql"));
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        final AESCrypter aes = new AESCrypter();
        key = AESCrypter.getRandomKey();
        aes.encrypt(fis, bos);

        fis.close();
        final FileOutputStream fos = new FileOutputStream(newFile(fileName + ".enc.sql"));
        bos.flush();
        fos.write(bos.toByteArray());
        bos.close();
        fos.close();
        final double duration = System.currentTimeMillis() - startTime / 1000.0;
        System.out.println("Encryption complete in " + duration + " seconds");
    }

    @Test
    public void testAESDecryption() throws Exception {
        final long startTime = System.currentTimeMillis();
        final FileInputStream fis = new FileInputStream(getFile("sql" + File.separator + fileName + ".sql"));
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        final AESCrypter aes = new AESCrypter();
        aes.decrypt(fis, bos, key);
        System.out.println("Key: " + new BigInteger(key.getEncoded()));

        fis.close();
        final FileOutputStream fos = new FileOutputStream(newFile(fileName + ".dec.sql"));
        bos.flush();
        fos.write(bos.toByteArray());
        bos.close();
        fos.close();
        final double duration = System.currentTimeMillis() - startTime / 1000.0;
        System.out.println("Decryption complete in " + duration + " seconds");
    }

}
