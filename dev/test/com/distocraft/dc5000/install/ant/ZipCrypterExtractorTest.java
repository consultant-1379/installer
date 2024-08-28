/**
 * 
 */
package com.distocraft.dc5000.install.ant;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author ecarbjo
 *
 */
public class ZipCrypterExtractorTest {

	private static ZipCrypterExtractor crypt = null;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		crypt = new ZipCrypterExtractor();
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
    final String tmp = System.getProperty("java.io.tmpdir");


    final File OutputFile = new File(tmp, "bopacks_ext");
    OutputFile.mkdirs();
    OutputFile.deleteOnExit();
		crypt.setOutputFile(OutputFile.getPath());


    final File setFile = new File(tmp, "bopacks");
    setFile.mkdirs();
    setFile.deleteOnExit();
		crypt.setFile(setFile.getPath());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.distocraft.dc5000.install.ant.ZipCrypterExtractor#execute()}.
	 */
	@Test
	public void testExecute() {
		try {
			crypt.execute();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Caught an exception");
		}
	}

}
