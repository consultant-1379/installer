package com.distocraft.dc5000.install.ant;

import junit.framework.JUnit4TestAdapter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.lang.reflect.Method;
import java.util.Hashtable;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 * 
 * @author ejarsok
 * 
 */

public class DependencyCheckTest {

  private static DependencyCheck instance;

  private static Method parseVersionInfo;

  private static Method validateRequirement;

  @BeforeClass
  public static void init() {
    instance = new DependencyCheck();
    Class secretClass = instance.getClass();

    try {
      parseVersionInfo = secretClass.getDeclaredMethod("parseVersionInfo", new Class[] { String.class });
      validateRequirement = secretClass.getDeclaredMethod("validateRequirement", new Class[] { String.class,
          Hashtable.class });

      parseVersionInfo.setAccessible(true);
      validateRequirement.setAccessible(true);
    } catch (Exception e) {

    }
  }

  @Test
  public void testSetAndGetForceflag() {
    DependencyCheck dC = new DependencyCheck();
    dC.setForceflag("Force_Flag");
    assertEquals("Force_Flag", dC.getForceflag());
  }

  @Test
  public void testExecute() {

    Project proj = new Project();

    proj.setProperty("module.name", "Dependency_moduuli");
    proj.setProperty("module.Dependency_moduuli", "Dependency_moduulib3");
    proj.setProperty("dcinstall.require.Dependency_moduuli", "= b3");

    DependencyCheck dC = new DependencyCheck();
    dC.setProject(proj);
    try {
      dC.execute(); // everything is fine b3 = b3
    } catch (BuildException e) {
      fail("testExecute() failed");
    }
  }

  @Test
  public void testExecute2() {

    Project proj = new Project();

    proj.setProperty("module.name", "Dependency_moduuli");
    proj.setProperty("module.Dependency_moduuli", "Dependency_moduulib4");
    proj.setProperty("dcinstall.require.Dependency_moduuli", "= b3");

    DependencyCheck dC = new DependencyCheck();
    dC.setProject(proj);
    try {
      dC.execute();
      fail("should't execute this line");
    } catch (BuildException e) {
      // test passed version b4 != b3
    }
  }

  @Test
  public void testValidateRequirement() {

    // module.dependencyCheckModule not set yet. should return null
    Hashtable table = new Hashtable();
    table.put("key.dependencyCheckModule", "= b3");
    try {
      assertNull((String) validateRequirement.invoke(instance, new Object[] { "key.dependencyCheckModule", table }));
    } catch (Exception e) {
      e.printStackTrace();
      fail("testValidateRequirement() failed");
    }
  }

  @Test
  public void testValidateRequirement2() {

    // operator is '=' and b4 != b3
    Hashtable table = new Hashtable();
    table.put("key.dependencyCheckModule", "= b3");
    table.put("module.dependencyCheckModule", "b4");
    try {
      assertEquals("Module dependencyCheckModule version = 3 required.", (String) validateRequirement.invoke(instance,
          new Object[] { "key.dependencyCheckModule", table }));
    } catch (Exception e) {
      e.printStackTrace();
      fail("testValidateRequirement2() failed");
    }
  }

  @Test
  public void testValidateRequirement3() {

    // operator is '>' and b2 < b3
    Hashtable table = new Hashtable();
    table.put("key.dependencyCheckModule", "> b3");
    table.put("module.dependencyCheckModule", "b2");
    try {
      assertEquals("Module dependencyCheckModule version > 3 required.", (String) validateRequirement.invoke(instance,
          new Object[] { "key.dependencyCheckModule", table }));
    } catch (Exception e) {
      e.printStackTrace();
      fail("testValidateRequirement3() failed");
    }
  }

  @Test
  public void testValidateRequirement4() {

    // everything is fine
    Hashtable table = new Hashtable();
    table.put("key.dependencyCheckModule", "= b3");
    table.put("module.dependencyCheckModule", "b3");
    try {
      assertNull((String) validateRequirement.invoke(instance, new Object[] { "key.dependencyCheckModule", table }));
    } catch (Exception e) {
      e.printStackTrace();
      fail("testValidateRequirement4() failed");
    }
  }

  @Test
  public void testParseVersionInfo() {
    try {
      assertEquals(2, parseVersionInfo.invoke(instance, new Object[] { "b2" }));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testParseVersionInfo2() {
    try {
      assertEquals(-1, parseVersionInfo.invoke(instance, new Object[] { "bv" })); // Print
      // NumberFormatException
      // exeption
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(DependencyCheckTest.class);
  }
}
