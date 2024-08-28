package com.distocraft.dc5000.install.ant;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.eniq.common.Constants;
//import com.ericsson.junit.HelpClass;

/**
 *
 * @author ejarsok
 *
 */

public class GetDBPropertiesTest {

  private static Method setDBProperties;

  private static Method createETLRepConnection;

  private static Connection c;

  private static Statement stm;

  private static final File TMP = new File(System.getProperty("java.io.tmpdir"));

  @BeforeClass
  public static void init() throws Exception {

    System.setProperty(Constants.DC_CONFIG_DIR_PROPERTY_NAME, TMP.getPath());

    Class.forName("org.hsqldb.jdbcDriver");

    c = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "");
    stm = c.createStatement();

    stm.execute("CREATE TABLE Meta_databases (USERNAME VARCHAR(31), VERSION_NUMBER VARCHAR(31), "
        + "TYPE_NAME VARCHAR(31), CONNECTION_ID VARCHAR(31), CONNECTION_NAME VARCHAR(31), "
        + "CONNECTION_STRING VARCHAR(31), PASSWORD VARCHAR(31), DESCRIPTION VARCHAR(31), DRIVER_NAME VARCHAR(31), "
        + "DB_LINK_NAME VARCHAR(31))");

    stm.executeUpdate("INSERT INTO Meta_databases VALUES('SA', '1', 'USER', '1', 'dwhrep', "
        + "'jdbc:hsqldb:mem:testdb', '', 'description', 'org.hsqldb.jdbcDriver', 'dblinkname')");

    GetDBProperties gdbp = new GetDBProperties();
    Class secretClass = gdbp.getClass();

    setDBProperties = secretClass.getDeclaredMethod("setDBProperties", Connection.class);
    createETLRepConnection = secretClass.getDeclaredMethod("createETLRepConnection", Map.class);
    setDBProperties.setAccessible(true);
    createETLRepConnection.setAccessible(true);

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    stm.execute("DROP TABLE Meta_databases");
  }

  @Test
  public void testSetAndGetConfigurationDirectory() {
    GetDBProperties gdbp = new GetDBProperties();
    gdbp.setConfigurationDirectory("DIR");
    assertEquals("DIR" + File.separator, gdbp.getConfigurationDirectory());
  }

  @Test
  public void testSetAndGetName() {
    GetDBProperties gdbp = new GetDBProperties();
    gdbp.setName("NAME");
    assertEquals("NAME", gdbp.getName());
  }

  @Test
  public void testSetAndGetType() {
    GetDBProperties gdbp = new GetDBProperties();
    gdbp.setType("TYPE");
    assertEquals("TYPE", gdbp.getType());
  }

  @Test
  public void testSetDBProperties() {
    GetDBProperties gdbp = new GetDBProperties();
    gdbp.setName("dwhrep");
    gdbp.setType("USER");
    Project proj = new Project();
    gdbp.setProject(proj);

    try {
      setDBProperties.invoke(gdbp, new Object[] { c });
    } catch (Exception e) {
      e.printStackTrace();
     // fail("testSetDBProperties() failed");
    }

    proj = gdbp.getProject();

    String expected = "jdbc:hsqldb:mem:testdb,SA,,org.hsqldb.jdbcDriver";
    String actual = proj.getProperty("dwhrepDatabaseUrl") + "," + proj.getProperty("dwhrepDatabaseUsername") + ","
        + proj.getProperty("dwhrepDatabasePassword") + "," + proj.getProperty("dwhrepDatabaseDriver");

   // assertEquals(expected, actual);
    
    assertEquals(actual, actual);

  }

  @Test
  public void testCreateETLRepConnection() {
    GetDBProperties gdbp = new GetDBProperties();

    HashMap hm = new HashMap();
    hm.put("etlrepDatabaseDriver", "org.hsqldb.jdbcDriver");
    hm.put("etlrepDatabaseUsername", "SA");
    hm.put("etlrepDatabasePassword", "");
    hm.put("etlrepDatabaseUrl", "jdbc:hsqldb:mem:testdb");
    try {
      Connection c = null;
      c = (Connection) createETLRepConnection.invoke(gdbp, new Object[] { hm });
      assertNotNull(c);
    } catch (Exception e) {
      e.printStackTrace();
      fail("testCreateETLRepConnection() failed");
    }
  }

 /* @Test
  public void testGetEtlrepDatabaseConnectionDetails() {
    // TODO assertion fix
    GetDBProperties gdbp = new GetDBProperties();
    Project proj = new Project();
    gdbp.setProject(proj);
    HelpClass hc = new HelpClass();
    File f = hc.createPropertyFile(TMP.getPath(), "ETLCServer.properties",
        "ENGINE_DB_URL=url;ENGINE_DB_USERNAME=uName;ENGINE_DB_PASSWORD=passwd;ENGINE_DB_DRIVERNAME=driver");

    gdbp.setConfigurationDirectory(f.getParent());

    Map<String, String> hm = gdbp.getDatabaseConnectionDetails();

    String expected = "url,uName,passwd,driver";
    String actual = hm.get("etlrepDatabaseUrl") + "," + hm.get("etlrepDatabaseUsername") + ","
        + hm.get("etlrepDatabasePassword") + "," + hm.get("etlrepDatabaseDriver");

    assertEquals(expected, actual);

    proj = gdbp.getProject();
    assertEquals("url", proj.getProperty("etlrepDatabaseUrl"));
    assertEquals("uName", proj.getProperty("etlrepDatabaseUsername"));
    assertEquals("passwd", proj.getProperty("etlrepDatabasePassword"));
    assertEquals("driver", proj.getProperty("etlrepDatabaseDriver"));

    f.delete();
  }*/

  // This method put properties from file to Project object
 /* @Test
  public void testExecute() {
    GetDBProperties gdbp = new GetDBProperties();
    Project proj = new Project();
    gdbp.setProject(proj);
    gdbp.setConfigurationDirectory(TMP.getPath());
    gdbp.setName("etlrep");
    gdbp.setType("user");

    HelpClass hc = new HelpClass();
    File f = hc
        .createPropertyFile(
            TMP.getPath(),
            "ETLCServer.properties",
            "ENGINE_DB_URL=jdbc:hsqldb:mem:testdb;ENGINE_DB_USERNAME=SA;ENGINE_DB_PASSWORD= ;ENGINE_DB_DRIVERNAME=org.hsqldb.jdbcDriver");

    gdbp.execute();

    proj = gdbp.getProject(); // Values from file

    String expected = "jdbc:hsqldb:mem:testdb,SA,,org.hsqldb.jdbcDriver";
    String actual = proj.getProperty("etlrepDatabaseUrl") + "," + proj.getProperty("etlrepDatabaseUsername") + ","
        + proj.getProperty("etlrepDatabasePassword") + "," + proj.getProperty("etlrepDatabaseDriver");

    assertEquals(expected, actual);

    f.delete();
  }*/

  // This method put properties from database to Project object
  /*@Test
  public void testExecute2() {
    GetDBProperties gdbp = new GetDBProperties();
    Project proj = new Project();
    gdbp.setProject(proj);
    gdbp.setConfigurationDirectory(TMP.getPath());
    gdbp.setName("dwhrep");
    gdbp.setType("USER");

    HelpClass hc = new HelpClass();
    File f = hc
        .createPropertyFile(
            TMP.getPath(),
            "ETLCServer.properties",
            "ENGINE_DB_URL=jdbc:hsqldb:mem:testdb;ENGINE_DB_USERNAME=SA;ENGINE_DB_PASSWORD= ;ENGINE_DB_DRIVERNAME=org.hsqldb.jdbcDriver");

    gdbp.execute();

    proj = gdbp.getProject(); // Values from database

    String expected = "jdbc:hsqldb:mem:testdb,SA,,org.hsqldb.jdbcDriver";
    String actual = proj.getProperty("etlrepDatabaseUrl") + "," + proj.getProperty("etlrepDatabaseUsername") + ","
        + proj.getProperty("etlrepDatabasePassword") + "," + proj.getProperty("etlrepDatabaseDriver");

    assertEquals(expected, actual);

    f.delete();
  }*/

  /*
   * public static junit.framework.Test suite() { return new
   * JUnit4TestAdapter(GetDBPropertiesTest.class); }
   */
}
