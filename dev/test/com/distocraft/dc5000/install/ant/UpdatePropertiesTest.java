package com.distocraft.dc5000.install.ant;

import static org.junit.Assert.*;

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

public class UpdatePropertiesTest {

  private static final File TMP = new File(System.getProperty("java.io.tmpdir"));

  private static final File prop = new File(TMP, "propertyFile");

  private static UpdateProperties uP = new UpdateProperties();


  @BeforeClass
  public static void init() {
    uP.setPropertiesFile(prop.getPath());
    prop.deleteOnExit();

    PrintWriter pw;
    try {
      pw = new PrintWriter(new FileWriter(prop));
      pw.write("property=value\n");
      pw.write("property2=value2\n");
      pw.write("deleteThis=notDeleted\n");
      pw.write("property_copy=overwrite\n");
      pw.close();
    } catch (IOException e) {
      e.printStackTrace();
      fail("Failed, Can't write in file");
    }
  }

  @Test
  public void testSetAndGetAction() {
    uP.setAction("set_action");
    assertEquals("set_action", uP.getAction());
  }

  @Test
  public void testSetAndGetKey() {
    uP.setKey("set_key");
    assertEquals("set_key", uP.getKey());
  }

  @Test
  public void testSetAndGetValue() {
    uP.setValue("set_value");
    assertEquals("set_value", uP.getValue());
  }

  @Test
  public void testSetAndGetForceCopy() {
    uP.setForceCopy("set_forceCopy");
    assertEquals("set_forceCopy", uP.getForceCopy());
  }

  @Test
  public void testSetAndGetTargetKey() {
    uP.setTargetKey("set_targetKey");
    assertEquals("set_targetKey", uP.getTargetKey());
  }

  @Test
  public void testSetAndGetPropertieFile() {
    assertEquals(prop.getPath(), uP.getPropertiesFile());
  }

  /**
   * Test method add one property value to properties file
   *
   */

  @Test
  public void testExecuteAdd() {
    uP.setAction("add");
    uP.setKey("property1");
    uP.setValue("value1");
    uP.execute();

    Properties props = new Properties();
    try {
      props.load(new FileInputStream(prop));
      assertEquals("value1", props.getProperty("property1"));
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed, testExecuteAdd() Exception");
    }
  }

  /**
   * Test method remove one property value from properties file
   *
   */

  @Test
  public void testExecuteRemove() {
    uP.setAction("remove");
    uP.setKey("deleteThis");
    uP.execute();

    Properties props = new Properties();
    try {
      props.load(new FileInputStream(prop));
      assertEquals(null, props.getProperty("deleteThis"));
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed, testExecuteAdd() Exception");
    }
  }

  /**
   * Test method update one property value in properties file
   *
   */

  @Test
  public void testExecuteUpdate() {
    uP.setAction("update");
    uP.setKey("property");
    uP.setValue("updated_value");
    uP.execute();

    Properties props = new Properties();
    try {
      props.load(new FileInputStream(prop));
      assertEquals("updated_value", props.getProperty("property"));
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed, testExecuteAdd() Exception");
    }
  }

  /**
   * Test method create copy from one property value
   *
   */

  @Test
  public void testExecuteCopy() {
    uP.setAction("copy");
    uP.setKey("property2");
    uP.setTargetKey("property_copy");
    uP.setForceCopy("true");
    uP.execute();
    uP.setKey("property2");
    uP.setTargetKey("property_copy2");
    uP.execute();

    Properties props = new Properties();
    try {
      props.load(new FileInputStream(prop));
      assertEquals("value2", props.getProperty("property_copy"));
      assertEquals("value2", props.getProperty("property_copy2"));
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed, testExecuteAdd() Exception");
    }
  }

  @Test
  public void testExeptions1() {
    UpdateProperties uP = new UpdateProperties();

    try {
      uP.execute();
      fail("1. Should not execute this line, Expected: Both inputFile and action must not be defined");
    } catch (BuildException be) {
      // System.out.println(be.getMessage());
    }
  }

  @Test
  public void testExeptions2() {
    UpdateProperties uP = new UpdateProperties();

    uP.setPropertiesFile("foobar");
    uP.setAction("action");

    try {
      uP.execute();
      fail("2. Should not execute this line, Expected: InputFile cannot be read or not file");
    } catch (BuildException be) {
      // System.out.println(be.getMessage());
    }
  }

  @Test
  public void testExeptions3() {
    UpdateProperties uP = new UpdateProperties();

    uP.setAction("action");
    uP.setPropertiesFile(prop.getPath());

    try {
      uP.execute();
      fail("3. Should not execute this line, Expected: parameter key must not be defined");
    } catch (BuildException be) {
      // System.out.println(be.getMessage());
    }
  }

  @Test
  public void testExeptions4() {
    UpdateProperties uP = new UpdateProperties();

    uP.setPropertiesFile(prop.getPath());
    uP.setKey("key");
    uP.setAction("add");

    try {
      uP.execute();
      fail("4. Should not execute this line, Expected: parameter value must not be defined");
    } catch (BuildException be) {
      // System.out.println(be.getMessage());
    }
  }

  @Test
  public void testExeptions5() {
    UpdateProperties uP = new UpdateProperties();

    uP.setPropertiesFile(prop.getPath());
    uP.setAction("update");
    uP.setKey("key");

    try {
      uP.execute();
      fail("5. Should not execute this line, Expected: parameter value must not be defined");
    } catch (BuildException be) {
      // System.out.println(be.getMessage());
    }
  }

  @Test
  public void testExeptions6() {
    UpdateProperties uP = new UpdateProperties();

    uP.setPropertiesFile(prop.getPath());
    uP.setAction("copy");
    uP.setKey("key");

    try {
      uP.execute();
      fail("6. Should not execute this line, Expected: parameter targetKey must not be defined");
    } catch (BuildException be) {
      // System.out.println(be.getMessage());
    }
  }

  @AfterClass
  public static void clean() {
    System.gc();
    prop.delete();
  }

  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(UpdatePropertiesTest.class);
  }
}
