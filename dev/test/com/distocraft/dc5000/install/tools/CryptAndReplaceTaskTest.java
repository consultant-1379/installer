/**
 * 
 */
package com.distocraft.dc5000.install.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.ericsson.eniq.repository.AsciiCrypter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.imageio.stream.FileImageInputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.distocraft.dc5000.install.ant.ActivateInterface;
import com.ericsson.eniq.install.tools.CryptAndReplaceTask;
import com.sun.org.apache.bcel.internal.classfile.Field;

/**
 * @author etogust
 * 
 */
public class CryptAndReplaceTaskTest {

	static final String VALUE = "teststring";
	static final String TOKEN = "@@crypted@@";
	static final String TESTPROP1 = "clear1";
	static final String TESTVAL1 = "test1";
	static final String TESTPROP2 = "clear2";
	static final String TESTVAL2 = "test2";
	static final String CRYPTEDPROP = "crypted";

	static CryptAndReplaceTask crt;
	static File tmpOutput;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		crt = new CryptAndReplaceTask();
		
		String tmpFileName = new Long(System.currentTimeMillis()).toString();
		while (new File(tmpFileName).exists()) {
			tmpFileName = new Long(System.currentTimeMillis()).toString();
		}

		tmpOutput = new File(tmpFileName);
		tmpOutput.createNewFile();
		tmpOutput.deleteOnExit();
		
		PrintWriter pw = new PrintWriter(tmpOutput);
		pw.println(TESTPROP1 + "=" + TESTVAL1);
		pw.println(CRYPTEDPROP + "=" + TOKEN);
		pw.println(TESTPROP2 + "=" + TESTVAL2);
		pw.close();

		crt.setFile(tmpOutput.getAbsolutePath());
		crt.setToken(TOKEN);
		crt.setValue(VALUE);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		File tmp = new File(tmpOutput.getAbsolutePath());
		tmp.delete();
	}

	@Test
	public void testCryptAndReplaceTask() {
		crt.setFile(tmpOutput.getAbsolutePath());
		crt.setToken(TOKEN);
		crt.setValue(VALUE);
		crt.execute();

		Properties props = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(tmpOutput);
			props.load(fis);
		} catch (Exception e) {
			fail("Testing of cryptAndReplaceTask itself failed.");
		}

		String testprop1 = (String) props.get(TESTPROP1);
		String testprop2 = (String) props.get(TESTPROP2);
		String crypted = (String) props.get(CRYPTEDPROP);

		if (!testprop1.equalsIgnoreCase(TESTVAL1)) {
			fail("cryptAndReplaceTask crypted broke properties before crypted value, which it should not touch at all");
		}
		if (!testprop2.equalsIgnoreCase(TESTVAL2)) {
			fail("cryptAndReplaceTask crypted broke properties after crypted value, which it should not touch at all");
		}
		if (crypted.equalsIgnoreCase(VALUE)) {
			fail("cryptAndReplaceTask did not crypt the value it should have crypted");
		}
		if (crypted.equalsIgnoreCase(TOKEN)) {
			fail("cryptAndReplaceTask did not do anything");
		}

	}

	@Test
	public void testDecrypting() {
		testCryptAndReplaceTask();
		
		Properties props = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(tmpOutput);
			props.load(fis);
		} catch (Exception e) {
			fail("Testing of cryptAndReplaceTask itself failed.");
		}
		String crypted = (String) props.get(CRYPTEDPROP);
		try {
			crypted = AsciiCrypter.getInstance().decrypt(crypted);
		} catch (Exception e) {
			fail("decryption exited with Exception.");			
		} 
		if (!crypted.equalsIgnoreCase(VALUE)) {
			fail("decrypted String does not match for the original");
		}

	}
	
	
	
	
}
