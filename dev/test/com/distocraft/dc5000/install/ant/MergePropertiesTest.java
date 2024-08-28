package com.distocraft.dc5000.install.ant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import junit.framework.JUnit4TestAdapter;

import org.apache.tools.ant.BuildException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author ejarsok
 *
 */

public class MergePropertiesTest {

  private static MergeProperties smP;
  
  private static MergeProperties smP2;
  
  private static File Dir;
  
  private static File inFile;
  
  private static File outFile;
  
  @BeforeClass
  public static void init() {
    String userDir = System.getProperty("user.dir");
    smP = new MergeProperties();
    smP2 = new MergeProperties();
    Dir = new File(userDir);
    inFile = new File(Dir, "inFile");
    outFile = new File(Dir, "outFile");
    
    try {
      PrintWriter pw = new PrintWriter(new FileWriter(inFile));   // Put property in inputFile
      pw.write("property1=value1" + "\n");
      pw.close();
      pw = new PrintWriter(new FileWriter(outFile));  // Put property in outputFile
      pw.write("property=value" + "\n");
      pw.write("property1=value2");
      
      pw.close();
    } catch (IOException e1) {
      e1.printStackTrace();
      fail("can't write inFile");
    }
  }
  
  @Test
  public void testSetAndGetInputFile() {
    smP.setInputFile("inFile"); 
    assertEquals("inFile", smP.getInputFile());
  }
  
  @Test
  public void testSetAndGetOutputFile() {
    smP.setOutputFile("outFile");
    assertEquals("outFile", smP.getOutputFile());
  }
  
  /**
   * Test method load properties from inFile and writes them to outFile
   *
   */
  
  @Test
  public void testExecute() {
    
    MergeProperties mP = new MergeProperties();
    mP.setInputFile("inFile");
    mP.setOutputFile("outFile");
    
    mP.execute(); // Write property value from inputFile to outputFile
    
    Properties outprops = new Properties();
    try {
      outprops.load(new FileInputStream(outFile));
      // Test that execute works
      assertEquals("value", outprops.get("property"));  // "property" copied over
      assertEquals("value2", outprops.get("property1"));  // "property1" left unchanged
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed, Exception");
    }
  }
  
  @Test
  public void testExecuteForfirstDayOfTheWeekNonDefault() {
	  
	  try {
	      PrintWriter pw = new PrintWriter(new FileWriter(inFile));   // Put property in inputFile
	      pw.write("property1=value1" + "\n");
	      pw.write("firstDayOfTheWeek=2");
	      pw.close();
	      pw = new PrintWriter(new FileWriter(outFile));  // Put property in outputFile
	      pw.write("property=value" + "\n");
	      pw.write("firstDayOfTheWeek=6" + "\n");
	      pw.close();
	    } catch (IOException e1) {
	      e1.printStackTrace();
	      fail("can't write inFile");
	    }
    
    MergeProperties mP = new MergeProperties();
    mP.setInputFile("inFile");
    mP.setOutputFile("outFile");
    
    mP.execute(); // Write property value from inputFile to outputFile
    
    Properties outprops = new Properties();
    try {
      outprops.load(new FileInputStream(outFile));
      assertEquals("6", outprops.get("firstDayOfTheWeek"));  // Test that execute work
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed, Exception");
    }
  }
  
  @Test
  public void testExecutefirstDayOfTheWeekDefault() {
	  
	  try {
	      PrintWriter pw = new PrintWriter(new FileWriter(inFile));   // Put property in inputFile
	      pw.write("property1=value1" + "\n");
	      pw.write("firstDayOfTheWeek=2");
	      pw.close();
	      pw = new PrintWriter(new FileWriter(outFile));  // Put property in outputFile
	      pw.write("property=value" + "\n");
	      pw.close();
	    } catch (IOException e1) {
	      e1.printStackTrace();
	      fail("can't write inFile");
	    }
    
    MergeProperties mP = new MergeProperties();
    mP.setInputFile("inFile");
    mP.setOutputFile("outFile");
    
    mP.execute(); // Write property value from inputFile to outputFile
    
    Properties outprops = new Properties();
    try {
      outprops.load(new FileInputStream(outFile));
      assertEquals("2", outprops.get("firstDayOfTheWeek"));  // Test that execute work
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed, Exception");
    }
  }
  
  /**
   * Test exception. inputFile or outputFile should be null
   *
   */
  
  @Test
  public void testExeption() {
    try {
      smP2.execute();     // Test exception
      fail("should not execute this, Expected: inputFile == null || outputFile == null");
    } catch(BuildException e) {
      
    }
  }
  
  /**
   * Test exception. inputFile should be unreadable or not file
   *
   */
  
  @Test
  public void testExeption2() {
    smP2.setInputFile("iFile"); // unreadable or not file
    smP2.setOutputFile("outFile");
    try {
      smP2.execute();    // Test exception
      fail("should not execute this, Expected: InputFile cannot be read or not file");
    } catch(BuildException e) {

    }
  }
  
  /**
   * Test exception. outputFile should be unreadable or not file
   *
   */
  
  @Test
  public void testExeption3() {
    smP2.setInputFile("inFile");
    smP2.setOutputFile("oFile"); // unreadable or not file
    try {
      smP2.execute();   // Test exception
      fail("should not execute this, Expected: OutputFile cannot be read or written or not file");
    } catch(BuildException e) {

    }
  }
  
  @AfterClass
  public static void clean() {
    System.gc();
    File inFile = new File(Dir, "inFile");
    File outFile = new File(Dir, "outFile");
    
    inFile.delete();
    outFile.delete();
  }
  
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(MergePropertiesTest.class);
  }
}
