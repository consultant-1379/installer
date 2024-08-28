package com.distocraft.dc5000.install.ant;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import junit.framework.JUnit4TestAdapter;
import org.apache.tools.ant.BuildException;
import org.apache.xerces.parsers.DOMParser;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * 
 * @author eheijun
 * 
 * 
 */
public class MergeWebXMLTest {

  private static final String INVALID_FILE = "oFile";

  private static final String TARGET_FILE = "targetFile";

  private static final String SOURCE_FILE = "sourceFile";

  private static MergeWebXML mergeWebXMLTask;

  private static File workDir;

  private static File targetFile;

  private static DOMParser parser = new DOMParser();

  @BeforeClass
  public static void init() {
    String userDir = System.getProperty("user.dir");
    mergeWebXMLTask = new MergeWebXML();
    workDir = new File(userDir);
    File sourceFile = new File(workDir, SOURCE_FILE);
    targetFile = new File(workDir, TARGET_FILE);
    try {
      parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

      PrintWriter pw = new PrintWriter(new FileWriter(sourceFile));

      pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      pw.write("<!--");
      pw.write("    This web.xml is used for MergeWebXML test case source file");
      pw.write("  -->");
      pw.write("<web-app id=\"WebApp_ID\" version=\"2.4\"");
      pw.write("    xmlns=\"http://java.sun.com/xml/ns/j2ee\"");
      pw
          .write("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd\">");
      pw.write("    <display-name>Ericsson Network IQ Management</display-name>");
      pw.write("    <servlet>");
      pw.write("        <servlet-name>SourceServlet</servlet-name>");
      pw.write("        <servlet-class>com.ericsson.eniq.source.Servlet</servlet-class>");
      pw.write("    </servlet>");
      pw.write("    <listener>");
      pw.write("        <description>Source listener.</description>");
      pw.write("        <display-name>BusyhourConfigurationSessionListener</display-name>");
      pw.write("        <listener-class>com.ericsson.eniq.source.Listener</listener-class>");
      pw.write("    </listener>");
      pw.write("    <servlet-mapping>");
      pw.write("        <servlet-name>SourceServlet</servlet-name>");
      pw.write("        <url-pattern>/show/source</url-pattern>");
      pw.write("    </servlet-mapping>");
      pw.write("    <welcome-file-list>");
      pw.write("        <welcome-file>SourceServlet</welcome-file>");
      pw.write("    </welcome-file-list>");
      pw.write("    <env-entry>");
      pw.write("        <env-entry-name>sources</env-entry-name>");
      pw.write("        <env-entry-value>source</env-entry-value>");
      pw.write("        <env-entry-type>source</env-entry-type>");
      pw.write("    </env-entry>");
      pw.write("</web-app>");
      pw.close();

      pw = new PrintWriter(new FileWriter(targetFile));

      pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      pw.write("<!--");
      pw.write("    This web.xml is used for MergeWebXML test case target file");
      pw.write("  -->");
      pw.write("<web-app id=\"WebApp_ID\" version=\"2.4\"");
      pw.write("    xmlns=\"http://java.sun.com/xml/ns/j2ee\"");
      pw
          .write("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd\">");
      pw.write("    <display-name>Ericsson Network IQ Management</display-name>");
      pw.write("    <servlet>");
      pw.write("        <servlet-name>TargetServlet</servlet-name>");
      pw.write("        <servlet-class>com.ericsson.eniq.target.Servlet</servlet-class>");
      pw.write("    </servlet>");
      pw.write("    <listener>");
      pw.write("        <description>Target listener.</description>");
      pw.write("        <display-name>BusyhourConfigurationSessionListener</display-name>");
      pw.write("        <listener-class>com.ericsson.eniq.target.Listener</listener-class>");
      pw.write("    </listener>");
      pw.write("    <servlet-mapping>");
      pw.write("        <servlet-name>TargetServlet</servlet-name>");
      pw.write("        <url-pattern>/show/target</url-pattern>");
      pw.write("    </servlet-mapping>");
// Test withouit target welcome-file-list element        
//      pw.write("    <welcome-file-list>");
//      pw.write("        <welcome-file>TargetServlet</welcome-file>");
//      pw.write("    </welcome-file-list>");
      pw.write("    <env-entry>");
      pw.write("        <env-entry-name>targets</env-entry-name>");
      pw.write("        <env-entry-value>target</env-entry-value>");
      pw.write("        <env-entry-type>target</env-entry-type>");
      pw.write("    </env-entry>");
      pw.write("</web-app>");
      pw.close();
    } catch (SAXException e1) {
      e1.printStackTrace();
      fail("can't init test");
    } catch (IOException e1) {
      e1.printStackTrace();
      fail("can't write test files");
    }
  }

  @Test
  public void testSetAndGetSourceFile() {
    mergeWebXMLTask.setSourceFile(SOURCE_FILE);
    assertNotNull(mergeWebXMLTask.getSourceWebXML());
  }

  @Test
  public void testSetAndGetTargetFile() {
    mergeWebXMLTask.setTargetFile(TARGET_FILE);
    assertNotNull(mergeWebXMLTask.getTargetWebXML());
  }

  /**
   * 
   * Test method load elements from sourceFile and writes them to targetFile
   * 
   * 
   */

  @Test
  public void testExecute() {
    MergeWebXML task = new MergeWebXML();
    try {
      task.setSourceFile(SOURCE_FILE);
      task.setTargetFile(TARGET_FILE);
      final Document originalWebXML = task.getTargetWebXML();
      task.execute();
      parser.parse(TARGET_FILE);
      final Document modifiedWebXML = parser.getDocument();
      assertTrue(!originalWebXML.isEqualNode(modifiedWebXML));
      NodeList test;
      test = modifiedWebXML.getElementsByTagName("servlet");
      assertTrue(test.getLength() == 2);
      test = modifiedWebXML.getElementsByTagName("listener");
      assertTrue(test.getLength() == 2);
      test = modifiedWebXML.getElementsByTagName("servlet-mapping");
      assertTrue(test.getLength() == 2);
      test = modifiedWebXML.getElementsByTagName("welcome-file-list");
      assertTrue(test.getLength() == 1);
      test = modifiedWebXML.getElementsByTagName("env-entry");
      assertTrue(test.getLength() == 2);
    } catch (Exception e) {
      fail("Failed, Exception");
    }
  }

  /**
   * 
   * Test method load properties from targetFile and writes them to targetFile
   * (so actually there should not be any changes)
   * 
   */

  @Test
  public void testAnotherExecute() {
    MergeWebXML task = new MergeWebXML();
    try {
      task.setSourceFile(TARGET_FILE);
      task.setTargetFile(TARGET_FILE);
      final Document originalWebXML = task.getTargetWebXML();
      task.execute();
      parser.parse(TARGET_FILE);
      final Document modifiedWebXML = parser.getDocument();
      assertTrue(originalWebXML.isEqualNode(modifiedWebXML));
    } catch (Exception e) {
      fail("Failed, Exception");
    }
  }

  /**
   * 
   * Test exception. inputFile or outputFile should be null
   * 
   * 
   */

  @Test
  public void testExeption() {
    try {
      MergeWebXML task = new MergeWebXML();
      task.execute(); // Test exception
      fail("should not execute this, Expected: inputFile == null || outputFile == null");
    } catch (BuildException e) {
      //  
    }
  }

  /**
   * 
   * Test exception. inputFile should be unreadable or not file
   * 
   * 
   */

  @Test
  public void testExeption2() {
    try {
      MergeWebXML task = new MergeWebXML();
      task.setSourceFile(INVALID_FILE); // unreadable or not file
      task.setTargetFile(TARGET_FILE);
      task.execute(); // Test exception
      fail("should not execute this, Expected: SourceFile cannot be read or not file");
    } catch (BuildException e) {
      //  
    }
  }

  /**
   * 
   * Test exception. outputFile should be unreadable or not file
   * 
   * 
   */

  @Test
  public void testExeption3() {
    try {
      MergeWebXML task = new MergeWebXML();
      task.setSourceFile(SOURCE_FILE);
      task.setTargetFile(INVALID_FILE); // unreadable or not file
      task.execute(); // Test exception
      fail("should not execute this, Expected: TargetFile cannot be read or written or not file");
    } catch (BuildException e) {
      //
    }
  }

  @AfterClass
  public static void clean() {
    System.gc();
    File sourceFile = new File(workDir, SOURCE_FILE);
    File targetFile = new File(workDir, TARGET_FILE);
    sourceFile.delete();
    targetFile.delete();
  }

  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(MergeWebXMLTest.class);
  }

}
