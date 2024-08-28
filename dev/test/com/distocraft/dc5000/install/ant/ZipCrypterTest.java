/**
 *
 */
package com.distocraft.dc5000.install.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author ecarbjo
 */
public class ZipCrypterTest {

  private static ZipCrypter crypt;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    crypt = new ZipCrypter();
    final File realZipFile = createRealZipFile("DC_E_IMS_IPW_R2B_b6.tpi");
    realZipFile.deleteOnExit();
    crypt.setFile(realZipFile.getPath());
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
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for {@link com.distocraft.dc5000.install.ant.RSAZipCrypter#execute()}.
   */
  @Test
  public void testExecuteEncrypt() {

    crypt.setCryptType("encrypt");
    crypt.setIsPublicKey("false");
    crypt.setKeyModulate("91904075215482429200974130997378134318659730089278694701294663814671976905189836397175101804787466211425807685632407184853265021082292037775446539705083915756665031257078346103497763827097305749433890688361251048827747830535575010868393704647286975226826020988701838915072852700756010735727623592451574735047");
    crypt.setKeyExponent("15494272350822670556198226549735737157435820342042514880977230274323064418960930502653275246671295820224357982796778415024241653080523135410240151627347482620253514319811320458512506170342554844837745624349328607163139404640677933771711452598878317984827861831877002530137005411348042548281392213546793225993");

    try {
      crypt.execute();
    } catch (Exception e) {
      e.printStackTrace();
      fail("Did not complete");
    }
  }

  private static File createRealZipFile(final String tpiName) throws IOException {
    final String tmpDir = System.getProperty("java.io.tmpdir");
    final File aFile = new File(tmpDir, "testfile.txt");
    aFile.deleteOnExit();
    //noinspection ResultOfMethodCallIgnored
    aFile.createNewFile();
    final byte[] buf = new byte[1024];
    final File realFile = new File(tmpDir, tpiName);
    final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(realFile));

    final FileInputStream in = new FileInputStream(aFile);
    out.putNextEntry(new ZipEntry(aFile.getName()));
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }

    out.closeEntry();
    in.close();
    out.close();
    return realFile;
  }

  @Test
  public void testExecuteDecrypt() {
    crypt.setCryptType("decrypt");
    crypt.setIsPublicKey("true");
    crypt.setKeyModulate("91904075215482429200974130997378134318659730089278694701294663814671976905189836397175101804787466211425807685632407184853265021082292037775446539705083915756665031257078346103497763827097305749433890688361251048827747830535575010868393704647286975226826020988701838915072852700756010735727623592451574735047");
    crypt.setKeyExponent("65537");

    try {
      crypt.execute();
    } catch (Exception e) {
      e.printStackTrace();
      fail("Did not complete");
    }
  }

  @Test
  public void testInvalidZipFile() throws FileNotFoundException {
    final File tmp = new File(System.getProperty("java.io.tmpdir"));
    File fileName = new File(tmp, "test.txt");
    fileName.deleteOnExit();
    String sampleStr = "This is just a small sample text that I have written";

    PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
    pw.print(sampleStr);
    pw.close();

    crypt.setFile(fileName.getPath());
    crypt.setCryptType("encrypt");
    crypt.setIsPublicKey("false");
    crypt.setKeyModulate("91904075215482429200974130997378134318659730089278694701294663814671976905189836397175101804787466211425807685632407184853265021082292037775446539705083915756665031257078346103497763827097305749433890688361251048827747830535575010868393704647286975226826020988701838915072852700756010735727623592451574735047");
    crypt.setKeyExponent("15494272350822670556198226549735737157435820342042514880977230274323064418960930502653275246671295820224357982796778415024241653080523135410240151627347482620253514319811320458512506170342554844837745624349328607163139404640677933771711452598878317984827861831877002530137005411348042548281392213546793225993");

    System.out.println("Begin ZipCrypter.execute()");
    boolean exceptionRaised = false;
    try {
      crypt.execute();
    } catch (Exception e) {
      exceptionRaised = true;
    }
    System.out.println("Ended ZipCrypter.execute()");
    assertTrue("No exception was thrown although a non-valid .zip was tried.", exceptionRaised);
  }
}
