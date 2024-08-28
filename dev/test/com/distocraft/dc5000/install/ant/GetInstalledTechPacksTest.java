package com.distocraft.dc5000.install.ant;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ssc.rockfactory.RockFactory;

import com.ericsson.eniq.common.Constants;
import com.distocraft.dc5000.common.StaticProperties;

/**
 *
 * @author ejarsok
 *
 */

public class GetInstalledTechPacksTest {

  private final GetInstalledTechPacks gitp = new GetInstalledTechPacks();

  private static RockFactory rockFact;

  private static Statement stm;

  private static final File TMP = new File(System.getProperty("java.io.tmpdir"));

  @BeforeClass
  public static void init() throws Exception {
    StaticProperties.giveProperties(new Properties());

    setUpPropertiesFileAndProperty();

    Class.forName("org.hsqldb.jdbcDriver");

    Connection c;

    c = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "");
    stm = c.createStatement();

    stm.execute("CREATE TABLE Meta_databases (USERNAME VARCHAR(31), VERSION_NUMBER VARCHAR(31), "
        + "TYPE_NAME VARCHAR(31), CONNECTION_ID VARCHAR(31), CONNECTION_NAME VARCHAR(31), "
        + "CONNECTION_STRING VARCHAR(31), PASSWORD VARCHAR(31), DESCRIPTION VARCHAR(31), DRIVER_NAME VARCHAR(31), "
        + "DB_LINK_NAME VARCHAR(31))");

    stm.executeUpdate("INSERT INTO Meta_databases VALUES('SA', '1', 'USER', '1', 'dwhrep', "
        + "'jdbc:hsqldb:mem:testdb', '', 'description', 'org.hsqldb.jdbcDriver', 'dblinkname')");

    rockFact = new RockFactory("jdbc:hsqldb:mem:testdb", "SA", "", "org.hsqldb.jdbcDriver", "con", true, -1);

  }


  private static void setUpPropertiesFileAndProperty() throws IOException {
    System.setProperty(Constants.DC_CONFIG_DIR_PROPERTY_NAME, TMP.getPath());
    File prop = new File(TMP, "ETLCServer.properties");

    PrintWriter pw = new PrintWriter(new FileWriter(prop));
    pw.write("ENGINE_DB_URL=jdbc:hsqldb:mem:testdb\n");
    pw.write("ENGINE_DB_USERNAME=SA\n");
    pw.write("ENGINE_DB_PASSWORD= \n");
    pw.write("ENGINE_DB_DRIVERNAME=org.hsqldb.jdbcDriver\n");
    pw.close();
  }

  @Test
  public void testSetAndGetConfigurationDirectory() {
    gitp.setConfigurationDirectory("CONF_DIR");
    assertEquals("CONF_DIR" + File.separator, gitp.getConfigurationDirectory());
  }

  @Test
  public void testSetAndGetShowNames() {
    gitp.setShowNames("SHOW_NAMES");
    assertEquals("SHOW_NAMES", gitp.getShowNames());
  }

  @Test
  public void testSetAndGetShowProductNumbers() {
    gitp.setShowProductNumbers("SHOW_PNUMB");
    assertEquals("SHOW_PNUMB", gitp.getShowProductNumbers());
  }

  @Test
  public void testSetAndGetShowVersionNumbers() {
    gitp.setShowVersionNumbers("SHOW_VNUMB");
    assertEquals("SHOW_VNUMB", gitp.getShowVersionNumbers());
  }

  @Test
  public void testGetDatabaseConnectionDetails() {

    Map<String, String> hm;

    GetInstalledTechPacks instance = new GetInstalledTechPacks();
    try {
      instance.setConfigurationDirectory(TMP.getPath());
      Project proj = new Project();
      instance.setProject(proj);
      hm = instance.getDatabaseConnectionDetails();

      String expected = "jdbc:hsqldb:mem:testdb,SA,,org.hsqldb.jdbcDriver";
      String actual = hm.get("etlrepDatabaseUrl") + "," + hm.get("etlrepDatabaseUsername") + ","
          + hm.get("etlrepDatabasePassword") + "," + hm.get("etlrepDatabaseDriver");

      assertEquals(expected, actual);

      proj = instance.getProject();
      Hashtable ht = proj.getProperties();
      assertEquals("jdbc:hsqldb:mem:testdb", ht.get("etlrepDatabaseUrl"));
      assertEquals("SA", ht.get("etlrepDatabaseUsername"));
      assertEquals("", ht.get("etlrepDatabasePassword"));
      assertEquals("org.hsqldb.jdbcDriver", ht.get("etlrepDatabaseDriver"));

    } catch (Exception e) {
      e.printStackTrace();
      fail("testGetDatabaseConnectionDetails() failed, Exception");
    }
  }

  @Test
  public void testCreateEtlrepRockFactory() {
    HashMap hm = new HashMap();
    hm.put("etlrepDatabaseUsername", "SA");
    hm.put("etlrepDatabasePassword", "");
    hm.put("etlrepDatabaseUrl", "jdbc:hsqldb:mem:testdb");
    hm.put("etlrepDatabaseDriver", "org.hsqldb.jdbcDriver");

    GetInstalledTechPacks instance = new GetInstalledTechPacks();
    Class secretClass = instance.getClass();

    try {
      Method method = secretClass.getDeclaredMethod("createEtlrepRockFactory", new Class[] { Map.class });
      method.setAccessible(true);
      RockFactory rf = (RockFactory) method.invoke(instance, new Object[] { hm });

      String expected = "SA,,jdbc:hsqldb:mem:testdb,org.hsqldb.jdbcDriver";
      String actual = rf.getUserName() + "," + rf.getPassword() + "," + rf.getDbURL() + "," + rf.getDriverName();

      assertEquals(expected, actual);

    } catch (Exception e) {
      e.printStackTrace();
      fail("testCreateEtlrepRockFactory() failed, Exception");
    }
  }

  @Test
  public void testCreateDwhrepRockFactory() {
    GetInstalledTechPacks instance = new GetInstalledTechPacks();
    Class secretClass = instance.getClass();

    try {
      Method method = secretClass.getDeclaredMethod("createDwhrepRockFactory", null);
      method.setAccessible(true);

      instance.etlrepRockFactory = rockFact;
      method.invoke(instance, null);

      String actual = "jdbc:hsqldb:mem:testdb,SA,,org.hsqldb.jdbcDriver";
      String expected = instance.dwhrepRockFactory.getDbURL() + "," + instance.dwhrepRockFactory.getUserName() + ","
          + instance.dwhrepRockFactory.getPassword() + "," + instance.dwhrepRockFactory.getDriverName();

      assertEquals(expected, actual);

    } catch (Exception e) {
      e.printStackTrace();
      fail("CreateDwhrepRockFactory() failed, Exception");
    }
  }

  @Ignore
  public void testExecute() {
    // TODO execute only print data
    fail("Not yet implemented");
  }

  @AfterClass
  public static void clean() throws Exception {
    // TODO System.gc remove
    System.gc();
    File prop = new File(TMP, "ETLCServer.properties");
    prop.delete();
    stm.execute("DROP TABLE Meta_databases");
  }

  /*
   * public static junit.framework.Test suite() { return new
   * JUnit4TestAdapter(GetInstalledTechPacksTest.class); }
   */
}
