package com.distocraft.dc5000.install.ant;

import static org.junit.Assert.*;

import com.ericsson.eniq.common.testutilities.DatabaseTestUtils;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.common.StaticProperties;

/**
 * 
 * @author ejarsok
 *
 */

public class ChangeDBUsersPermissionsTest {

  private static RockFactory rockFact;
  
  private static Method changeDBPermissions;
  
  private static Field etlrepRockFactory;
  
  private static Field dwhRockFactory;
  
  private static Connection c;
  
  private static Statement stm;

  @BeforeClass
  public static void init() throws Exception {
    StaticProperties.giveProperties(new Properties());

      Class.forName("org.hsqldb.jdbcDriver");

      rockFact = DatabaseTestUtils.getTestDbConnection();
      stm = rockFact.getConnection().createStatement();

      stm.execute("CREATE TABLE Meta_databases (USERNAME VARCHAR(31), VERSION_NUMBER VARCHAR(31), "
          + "TYPE_NAME VARCHAR(31), CONNECTION_ID VARCHAR(31), CONNECTION_NAME VARCHAR(31), "
          + "CONNECTION_STRING VARCHAR(31), PASSWORD VARCHAR(31), DESCRIPTION VARCHAR(31), DRIVER_NAME VARCHAR(31), "
          + "DB_LINK_NAME VARCHAR(31))");
      
      stm.executeUpdate("INSERT INTO Meta_databases VALUES('SA', '1', 'DBA', '1', 'dwh', "
          + "'jdbc:hsqldb:mem:testdb', '', 'description', 'org.hsqldb.jdbcDriver', 'dblinkname')");
    stm.close();
      
    ChangeDBUsersPermissions cup = new ChangeDBUsersPermissions();
    Class secretClass = cup.getClass();



      changeDBPermissions = secretClass.getDeclaredMethod("changeDBPermissions");
      etlrepRockFactory = secretClass.getDeclaredField("etlrepRockFactory");
      dwhRockFactory = secretClass.getDeclaredField("dwhRockFactory");
      changeDBPermissions.setAccessible(true);
      etlrepRockFactory.setAccessible(true);
      dwhRockFactory.setAccessible(true);

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    DatabaseTestUtils.close(rockFact);
  }
  
  @Test
  public void testSetAndGetConfigurationDirectory() {
    ChangeDBUsersPermissions cup = new ChangeDBUsersPermissions();
    cup.setConfigurationDirectory("confDir");
    assertEquals("confDir" + File.separator, cup.getConfigurationDirectory());
  }

  @Test
  public void testSetAndGetAction() {
    ChangeDBUsersPermissions cup = new ChangeDBUsersPermissions();
    cup.setAction("action");
    assertEquals("action", cup.getAction());
  }

  @Test
  public void testSetAndGetDbUser() {
    ChangeDBUsersPermissions cup = new ChangeDBUsersPermissions();
    cup.setDbUser("DBuser");
    assertEquals("DBuser", cup.getDbUser());
  }

  public void testChangeDBPermissions() {
    //  TODO CALL lock_user procedure problem
    ChangeDBUsersPermissions cup = new ChangeDBUsersPermissions();
    cup.setAction("lock");
    cup.setDbUser("all");
    try {
      dwhRockFactory.set(cup, rockFact);
      Integer i = (Integer) changeDBPermissions.invoke(cup, null);
      assertEquals((Integer) 0, i);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testChangeDBPermissions() failed");
    }
  }
  
  /**
   * If Action = null or empty string, method returns integer 1
   *
   */
  public void testChangeDBPermissions2() {
    //  TODO CALL lock_user procedure problem
    ChangeDBUsersPermissions cup = new ChangeDBUsersPermissions();
    try {
      Integer i = (Integer) changeDBPermissions.invoke(cup, null);
      assertEquals((Integer) 1, i);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testChangeDBPermissions() failed");
    }
  }
  
  /**
   * if dbUser = null or empty string, method returns integer 2
   *
   */
  public void testChangeDBPermissions3() {
    //  TODO CALL lock_user procedure problem
    ChangeDBUsersPermissions cup = new ChangeDBUsersPermissions();
    cup.setAction("action");
    try {
      Integer i = (Integer) changeDBPermissions.invoke(cup, null);
      assertEquals((Integer) 2, i);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testChangeDBPermissions() failed");
    }
  }
}
