package com.distocraft.dc5000.install.ant;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.common.StaticProperties;
import com.ericsson.eniq.common.Constants;
import com.ericsson.eniq.common.INIGet;

/**
 * Tests for PreinstallCheck class in com.distocraft.dc5000.install.ant.<br>
 * <br>
 * Testing preinstall validating process. Checking things like correct files
 * being in correct directory and required versions of needed techpacks being
 * installed.
 * 
 * @author EJAAVAH
 */
@RunWith(JMock.class)
public class PreinstallCheckTest {

  final Mockery mockingContext = new JUnit4Mockery();

  private static PreinstallCheck objUnderTest;

  private static Project proj;

  private static Statement stmt;

  private static Connection con = null;

  private static File etlServerProperties;

  private static File TPVersionProperties;

  private static File engprop;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    StaticProperties.giveProperties(new Properties());

    /* Setting up connection object and database to be used in this test */
    try {
      Class.forName("org.hsqldb.jdbcDriver").newInstance();
      con = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "sa", "");
    } catch (final Exception e) {
      e.printStackTrace();
    }
    stmt = con.createStatement();
    stmt.execute("CREATE TABLE VERSIONING (VERSIONID VARCHAR(31), DESCRIPTION VARCHAR(31), STATUS BIGINT, "
        + "TECHPACK_NAME VARCHAR(31), TECHPACK_VERSION VARCHAR(31), TECHPACK_TYPE VARCHAR(31), "
        + "PRODUCT_NUMBER VARCHAR(31), LOCKEDBY VARCHAR(31), LOCKDATE TIMESTAMP, BASEDEFINITION VARCHAR(31), "
        + "BASEVERSION VARCHAR(31), INSTALLDESCRIPTION VARCHAR(31), UNIVERSENAME VARCHAR(31), "
        + "UNIVERSEEXTENSION VARCHAR(31), ENIQ_LEVEL VARCHAR(31), LICENSENAME VARCHAR(31))");
    stmt.execute("CREATE TABLE META_DATABASES (USERNAME VARCHAR(31), VERSION_NUMBER VARCHAR(31), "
        + "TYPE_NAME VARCHAR(31), CONNECTION_ID BIGINT, CONNECTION_NAME VARCHAR(31), "
        + "CONNECTION_STRING VARCHAR(31), PASSWORD VARCHAR(31), DESCRIPTION VARCHAR(31), "
        + "DRIVER_NAME VARCHAR(31), DB_LINK_NAME VARCHAR(31))");
    stmt.execute("CREATE TABLE TPACTIVATION (TECHPACK_NAME VARCHAR(31), STATUS VARCHAR(31), "
        + "VERSIONID VARCHAR(31), TYPE VARCHAR(31), MODIFIED BIGINT)");
    stmt.execute("CREATE TABLE META_COLLECTION_SETS (COLLECTION_SET_ID VARCHAR(31),COLLECTION_SET_NAME VARCHAR(31), "
        + "DESCRIPTION VARCHAR(31), VERSION_NUMBER VARCHAR(31), ENABLED_FLAG VARCHAR(31), TYPE VARCHAR(31))");
    stmt.execute("CREATE TABLE INTERFACEMEASUREMENT (TAGID VARCHAR(31), DATAFORMATID VARCHAR(31), "
        + "INTERFACENAME VARCHAR(31), TRANSFORMERID VARCHAR(31), STATUS BIGINT, MODIFTIME TIMESTAMP, "
        + " DESCRIPTION VARCHAR(31), TECHPACKVERSION VARCHAR(31), INTERFACEVERSION VARCHAR(31))");
    stmt.execute("CREATE TABLE INTERFACETECHPACKS (INTERFACENAME VARCHAR(31), TECHPACKNAME VARCHAR(31), "
        + "TECHPACKVERSION VARCHAR(31), INTERFACEVERSION VARCHAR(31))");
    stmt.execute("CREATE TABLE DATAINTERFACE (INTERFACENAME VARCHAR(31), STATUS BIGINT, "
        + "INTERFACETYPE VARCHAR(31), DESCRIPTION VARCHAR(31), DATAFORMATTYPE VARCHAR(31), "
        + "INTERFACEVERSION VARCHAR(31), LOCKEDBY VARCHAR(31), LOCKDATE TIMESTAMP, PRODUCTNUMBER VARCHAR(31), "
        + "ENIQ_LEVEL VARCHAR(31), RSTATE VARCHAR(31), INSTALLDESCRIPTION varchar(32000))");
    stmt.execute("CREATE TABLE META_COLLECTIONS (COLLECTION_ID BIGINT, COLLECTION_NAME VARCHAR(31), "
        + "COLLECTION VARCHAR(31), MAIL_ERROR_ADDR VARCHAR(31), MAIL_FAIL_ADDR VARCHAR(31), "
        + "MAIL_BUG_ADDR VARCHAR(31), MAX_ERRORS BIGINT, MAX_FK_ERRORS BIGINT, MAX_COL_LIMIT_ERRORS BIGINT, "
        + "CHECK_FK_ERROR_FLAG VARCHAR(31), CHECK_COL_LIMITS_FLAG VARCHAR(31), LAST_TRANSFER_DATE TIMESTAMP, "
        + "VERSION_NUMBER VARCHAR(31), COLLECTION_SET_ID BIGINT, USE_BATCH_ID VARCHAR(31), PRIORITY BIGINT, "
        + "QUEUE_TIME_LIMIT BIGINT, ENABLED_FLAG VARCHAR(31), SETTYPE VARCHAR(31), FOLDABLE_FLAG VARCHAR(31), "
        + "MEASTYPE VARCHAR(31), HOLD_FLAG VARCHAR(31), SCHEDULING_INFO VARCHAR(31))");
    stmt.execute("CREATE TABLE META_TRANSFER_ACTIONS (VERSION_NUMBER VARCHAR(31), TRANSFER_ACTION_ID BIGINT, "
        + "COLLECTION_ID BIGINT, COLLECTION_SET_ID BIGINT, ACTION_TYPE VARCHAR(31), TRANSFER_ACTION_NAME VARCHAR(31), "
        + "ORDER_BY_NO BIGINT, DESCRIPTION VARCHAR(31), ENABLED_FLAG VARCHAR(31), CONNECTION_ID BIGINT, "
        + "WHERE_CLAUSE_02 VARCHAR(31), WHERE_CLAUSE_03 VARCHAR(31), ACTION_CONTENTS_03 VARCHAR(31), "
        + "ACTION_CONTENTS_02 VARCHAR(31), ACTION_CONTENTS_01 VARCHAR(31), WHERE_CLAUSE_01 VARCHAR(31))");
    stmt.execute("CREATE TABLE INTERFACEDEPENDENCY (INTERFACEVERSION VARCHAR(31), INTERFACENAME VARCHAR(31), "
        + "TECHPACKNAME VARCHAR(31), TECHPACKVERSION VARCHAR(31))");
    stmt.execute("CREATE TABLE META_SCHEDULINGS (VERSION_NUMBER VARCHAR(31), ID BIGINT, EXECUTION_TYPE VARCHAR(31), "
                + "OS_COMMAND VARCHAR(31), SCHEDULING_MONTH BIGINT, SCHEDULING_DAY BIGINT, SCHEDULING_HOUR BIGINT, "
                + "SCHEDULING_MIN BIGINT, COLLECTION_SET_ID BIGINT, COLLECTION_ID BIGINT, MON_FLAG VARCHAR(31), "
                + " TUE_FLAG VARCHAR(31), WED_FLAG VARCHAR(31), THU_FLAG VARCHAR(31), FRI_FLAG VARCHAR(31), "
                + "SAT_FLAG VARCHAR(31), SUN_FLAG VARCHAR(31), STATUS VARCHAR(31), LAST_EXECUTION_TIME TIMESTAMP, "
                + "INTERVAL_HOUR BIGINT, INTERVAL_MIN BIGINT, NAME VARCHAR(31), HOLD_FLAG VARCHAR(31), PRIORITY BIGINT, "
                + "SCHEDULING_YEAR BIGINT, TRIGGER_COMMAND VARCHAR(31), LAST_EXEC_TIME_MS BIGINT)");


    stmt.execute("create table MZTechPacks (VERSIONID varchar(128) not null,TECHPACK_NAME varchar(30) "
        + "not null,STATUS varchar(10) null,CREATIONDATE datetime null,PRODUCT_NUMBER varchar(255) null,TYPE varchar(10) not null,TECHPACK_VERSION varchar(32) null)");

    /* Creating property file for database connection details */
    etlServerProperties = new File(System.getProperty("user.dir"), "ETLCServer.properties");
    etlServerProperties.deleteOnExit();
    try {
      final PrintWriter pw = new PrintWriter(new FileWriter(etlServerProperties));
      pw.write("ENGINE_DB_URL = url\n");
      pw.write("ENGINE_DB_USERNAME = user\n");
      pw.write("ENGINE_DB_PASSWORD = pass\n");
      pw.write("ENGINE_DB_DRIVERNAME = driver\n");
      pw.close();
    } catch (final Exception e) {
      e.printStackTrace();
    }

    /* Creating property file for tech packs version properties */
    final File TPVersionPropertiesDir = new File(System.getProperty("user.dir"), "install");
    TPVersionPropertiesDir.mkdir();
    TPVersionPropertiesDir.deleteOnExit();
    TPVersionProperties = new File(System.getProperty("user.dir"), "/install/version.properties");
    TPVersionProperties.deleteOnExit();
    try {
      final PrintWriter pw = new PrintWriter(new FileWriter(TPVersionProperties));
      pw.write("required_tech_packs = tp1\n");
      pw.write("tech_pack.name = tp1\n");
      pw.write("tech_pack.metadata_version = v1.2\n");
      pw.write("tech_pack.version = v2.01\n");
      pw.write("build.tag = btag\n");
      pw.write("build.number = 3\n");
      pw.write("tech_pack.version = v2.01\n");
      pw.close();
    } catch (final Exception e) {
      e.printStackTrace();
    }

    /* Creating property file for engine properties */
    engprop = new File(System.getProperty("user.dir"), "engineLogging.properties");
    engprop.deleteOnExit();
    try {
      final PrintWriter pw = new PrintWriter(new FileWriter(engprop));
      pw.write(".level = newvalue");
      pw.close();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {

    /* Cleaning up after tests */
    stmt.execute("DROP TABLE VERSIONING");
    stmt.execute("DROP TABLE META_DATABASES");
    stmt.execute("DROP TABLE TPACTIVATION");
    stmt.execute("DROP TABLE META_COLLECTION_SETS");
    stmt.execute("DROP TABLE INTERFACEMEASUREMENT");
    stmt.execute("DROP TABLE INTERFACETECHPACKS");
    stmt.execute("DROP TABLE DATAINTERFACE");
    stmt.execute("DROP TABLE META_COLLECTIONS");
    stmt.execute("DROP TABLE META_TRANSFER_ACTIONS");
    stmt.execute("DROP TABLE INTERFACEDEPENDENCY");
    stmt.execute("DROP TABLE META_SCHEDULINGS");
    stmt.execute("DROP TABLE MZTechPacks");

    con = null;
  }

  @Before
  public void setUpBeforeTest() throws Exception {

    /*
     * Creating new PreinstallCheck instance before every test and setting up a
     * ant project object for it
     */
    objUnderTest = new PreinstallCheck();
    proj = new Project();
    objUnderTest.setProject(proj);
    mockingContext.setImposteriser(ClassImposteriser.INSTANCE);
  }

  @After
  public void CleanUpAfterTest() throws Exception {

    /* Clearing all previous rows from tables used in latest test */
    stmt.execute("DELETE FROM VERSIONING");
    stmt.execute("DELETE FROM META_DATABASES");
    stmt.execute("DELETE FROM TPACTIVATION");
    stmt.execute("DELETE FROM META_COLLECTION_SETS");
    stmt.execute("DELETE FROM INTERFACEMEASUREMENT");
    stmt.execute("DELETE FROM INTERFACETECHPACKS");
    stmt.execute("DELETE FROM DATAINTERFACE");
    stmt.execute("DELETE FROM META_COLLECTIONS");
    stmt.execute("DELETE FROM META_TRANSFER_ACTIONS");
    stmt.execute("DELETE FROM INTERFACEDEPENDENCY");
    stmt.execute("DELETE FROM META_SCHEDULINGS");
    stmt.execute("DELETE FROM MZTechPacks");
    proj = null;
    objUnderTest = null;
  }

  /**
   * Testing reading database connection details from a properties file. The
   * information is returned in a hashmap and saved to a project object.
   */
  @Test
  public void testGetDatabaseConnectionDetails() throws Exception {

    /* Setting filepath from where the connection details are fetched */
    objUnderTest.setConfigurationDirectory(etlServerProperties.getParent());

    System.setProperty(Constants.DC_CONFIG_DIR_PROPERTY_NAME, etlServerProperties.getParent());

    /*
     * Invoking getDatabaseConnectionDetails() method and saving returned
     * hashmap and project objects
     */
    final Map<String, String> condetails = objUnderTest.getDatabaseConnectionDetails();

    /*
     * Asserting that connection details are the same in returned HashMap as
     * well as in properties file
     */
    String actual = condetails.get("etlrepDatabaseUrl") + ", " + condetails.get("etlrepDatabaseUsername") + ", "
        + condetails.get("etlrepDatabasePassword") + ", " + condetails.get("etlrepDatabaseDriver");
    final String expected = "url, user, pass, driver";
    assertEquals(expected, actual);

    /* Asserting that connection details are saved to the Project object also */
    actual = proj.getProperty("etlrepDatabaseUrl") + ", " + proj.getProperty("etlrepDatabaseUsername") + ", "
        + proj.getProperty("etlrepDatabasePassword") + ", " + proj.getProperty("etlrepDatabaseDriver");
    assertEquals(expected, actual);
  }

  /**
   * Testing getting database connection details with invalid filepath.
   */
  @Test
  public void testGetDatabaseConnectionDetailsWithNotExistingPath() {
    /*
     * Testing if exception is thrown when trying to fetch properties from
     * non-existing path
     */

    File f = new File("NotExistingPath");
    int cnt = 1;
    while (f.exists()) {
      f = new File("NotExistingPath" + cnt);
      cnt++;
    }
    objUnderTest.setConfigurationDirectory(f.getAbsolutePath());

    try {
      objUnderTest.getDatabaseConnectionDetails();
      fail("Test failed - Exception expected as no such file should exist as stated in propertiesfilepath");
    } catch (final Exception e) {
      /* Test passed - Exception catched */
    }
  }

  /**
   * Testing RockFactory object creation using Hashmapped connection details.
   * Returns RockFactory object.
   */
  @Test
  public void testCreateEtlrepRockFactory() throws Exception {

    /* Initializing a HashMap with all the connection details */
    final HashMap condetails = new HashMap();
    condetails.put("etlrepDatabaseUrl", "jdbc:hsqldb:mem:testdb");
    condetails.put("etlrepDatabaseUsername", "sa");
    condetails.put("etlrepDatabasePassword", "");
    condetails.put("etlrepDatabaseDriver", "org.hsqldb.jdbcDriver");

    /* Reflecting the tested method */
    final Class pcClass = objUnderTest.getClass();
    final Method createEtlrepRockFactory = pcClass.getDeclaredMethod("createEtlrepRockFactory",
        new Class[] { Map.class });
    createEtlrepRockFactory.setAccessible(true);
    /* Asserting that the object is created and it has correct properties */
    final RockFactory rockFactory = (RockFactory) createEtlrepRockFactory.invoke(objUnderTest,
        new Object[] { condetails });
    final String actual = rockFactory.getDbURL() + ", " + rockFactory.getUserName() + ", " + rockFactory.getPassword()
        + ", " + rockFactory.getDriverName();
    final String expected = "jdbc:hsqldb:mem:testdb, sa, , org.hsqldb.jdbcDriver";
    assertEquals(expected, actual);
  }

  /**
   * Testing ETLrepRockFactory creating with map including empty values.
   */
  @Test
  public void testCreateEtlrepRockFactoryWithEmptyMap() throws Exception {

    /* Reflecting the tested method */
    final Class pcClass = objUnderTest.getClass();
    final Method createEtlrepRockFactory = pcClass.getDeclaredMethod("createEtlrepRockFactory",
        new Class[] { Map.class });
    createEtlrepRockFactory.setAccessible(true);

    /*
     * Testing if exception is thrown when trying to create RockFactory with
     * empty property values in HashMap
     */
    final HashMap emptymap = new HashMap();
    emptymap.put("etlrepDatabaseUrl", "");
    emptymap.put("etlrepDatabaseUsername", "");
    emptymap.put("etlrepDatabasePassword", "");
    emptymap.put("etlrepDatabaseDriver", "");
    try {
      createEtlrepRockFactory.invoke(objUnderTest, new Object[] { emptymap });
      fail("Test failed - Exception expected as empty HashMap given as parameter");
    } catch (final Exception e) {
      /* Test passed - Exception catched */
    }
  }

  /**
   * Testing property reading from version properties and setting them to a ANT
   * project object.
   */
  @Test
  public void testReadTechPackVersionFile() throws Exception {

    /* Reflecting the tested method */
    final Class pcClass = objUnderTest.getClass();
    final Method readTechPackVersionFile = pcClass.getDeclaredMethod("readTechPackVersionFile", new Class[] {});
    readTechPackVersionFile.setAccessible(true);

    /*
     * Reflecting version properties filepath variable to be able to set value
     * for it
     */
    final Field techPackContentPath = pcClass.getDeclaredField("tpContentPath");
    techPackContentPath.setAccessible(true);
    techPackContentPath.set(objUnderTest, System.getProperty("user.dir"));

    /* Asserting that version properties are saved to the Project object */
    readTechPackVersionFile.invoke(objUnderTest, new Object[] {});
    final String actual = proj.getProperty("techPackName") + ", " + proj.getProperty("techPackVersion") + ", "
        + proj.getProperty("buildNumber") + ", " + proj.getProperty("techPackMetadataVersion");
    final String expected = "tp1, v2.01, 3, 1";
    assertEquals(expected, actual);
  }

  /**
   * Testing required techpack checking with generic input. Certain techpacks
   * might need other techpacks to be installed before they can be installed and
   * checkRequiredTechPackInstallations() method does just that. Exception is
   * thrown if one or more of required techpacks are missing or they are of
   * lesser version.
   */
  @Test
  public void testCheckRequiredTechPackInstallations() throws Exception {

    /* Inserting required techpack to VERSIONING table */
    stmt.executeUpdate("INSERT INTO VERSIONING VALUES"
        + "('1', 'description', '1', 'tp1', '7', 'techpaktype', 'productnumber', "
        + "'lockedby', '2010-10-10 10:00:00.0', 'basedefinition', 'baseversion', 'installdescription', 'universename', "
        + "'universeextension', 'eniq_level', 'licensename')");

    /* Initializing a HashMap with required techpacks and their version as value */
    final HashMap reqtps = new HashMap();
    reqtps.put("tp1", "7");

    /* Reflecting the tested method */
    final Class pcClass = objUnderTest.getClass();
    final Method checkRequiredTechPackInstallations = pcClass.getDeclaredMethod("checkRequiredTechPackInstallations",
        new Class[] { HashMap.class });
    checkRequiredTechPackInstallations.setAccessible(true);

    /* Initializing dwhRockFactory */
    final Field dwhrepRockFactory = pcClass.getDeclaredField("dwhrepRockFactory");
    dwhrepRockFactory.setAccessible(true);
    dwhrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "con", true));

    /* Testing that techpack installation check passes */
    try {
      checkRequiredTechPackInstallations.invoke(objUnderTest, new Object[] { reqtps });
      /* Test passed */
    } catch (final Exception e) {
      fail("Test Failed - Unexpected exception!\n" + e);
    }
  }

  /**
   * Testing required techpack checking with missing techpack installation.
   * Certain techpacks might need other techpacks to be installed before they
   * can be installed and checkRequiredTechPackInstallations() method does just
   * that. Exception is thrown if one or more of required techpacks are missing
   * or they are of lesser version.
   */
  @Test
  public void testCheckRequiredTechPackInstallationsTPNotFoundException() throws Exception {

    /*
     * Inserting data to VERSION table - checking if these techpacks match to
     * those in required HashMap
     */
    stmt.executeUpdate("INSERT INTO VERSIONING VALUES"
        + "('0', 'description', 1, 'tp1', '10_b', 'techpaktype', 'productnumber', "
        + "'lockedby', '2010-10-10 10:00:00.0', 'basedefinition', 'baseversion', 'installdescription', 'universename', "
        + "'universeextension', 'eniq_level', 'licensename')");

    /* Initializing a HashMap with required techpacks and their version as value */
    final HashMap reqtps = new HashMap();
    reqtps.put("tp1", "10");
    reqtps.put("tp2", "8");

    /* Reflecting the tested method */
    final Class pcClass = objUnderTest.getClass();
    final Method checkRequiredTechPackInstallations = pcClass.getDeclaredMethod("checkRequiredTechPackInstallations",
        new Class[] { HashMap.class });
    checkRequiredTechPackInstallations.setAccessible(true);

    /* Initializing dwhRockFactory */
    final Field dwhrepRockFactory = pcClass.getDeclaredField("dwhrepRockFactory");
    dwhrepRockFactory.setAccessible(true);
    dwhrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "con", true));

    /*
     * Testing if exception is thrown when required techpack HashMap has more
     * elements than there are in the VERSIONING table
     */
    try {
      checkRequiredTechPackInstallations.invoke(objUnderTest, new Object[] { reqtps });
      fail("Test Failed - Exception expected as only one techpack defined in VERSIONING table when required list has two");
    } catch (final Exception e) {
      /* Test passed - Exception caught */
    }
  }

  /**
   * Testing required techpack checking with older version installed than
   * required. Certain techpacks might need other techpacks to be installed
   * before they can be installed and checkRequiredTechPackInstallations()
   * method does just that. Exception is thrown if one or more of required
   * techpacks are missing or they are of lesser version.
   */
  @Test
  public void testCheckRequiredTechPackInstallationsLesserVersion() throws Exception {

    /* Inserting older version than required into VERSIONING table */
    stmt.executeUpdate("INSERT INTO VERSIONING VALUES"
        + "('1', 'description', 1, 'tp1', '7', 'techpaktype', 'productnumber', "
        + "'lockedby', '2000-01-01 10:00:00.0', 'basedefinition', 'baseversion', 'installdescription', 'universename', "
        + "'universeextension', 'eniq_level', 'licensename')");

    /* Initializing a HashMap with required techpacks and their version as value */
    final HashMap reqtps = new HashMap();
    reqtps.put("tp1", "9");

    /* Reflecting the tested method */
    final Class pcClass = objUnderTest.getClass();
    final Method checkRequiredTechPackInstallations = pcClass.getDeclaredMethod("checkRequiredTechPackInstallations",
        new Class[] { HashMap.class });
    checkRequiredTechPackInstallations.setAccessible(true);

    /* Initializing dwhRockFactory */
    final Field dwhrepRockFactory = pcClass.getDeclaredField("dwhrepRockFactory");
    dwhrepRockFactory.setAccessible(true);
    dwhrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "con", true));

    /*
     * Testing if exception is thrown when one of the already installed
     * techpacks has older version than required
     */
    try {
      checkRequiredTechPackInstallations.invoke(objUnderTest, new Object[] { reqtps });
      fail("Test Failed - Exception expected one of the techpacks is of older version than required");
    } catch (final Exception e) {
      /* Test passed - Exception caught */
    }
  }

  /**
   * Testing parsing out techpacks content path and saving it to ANT project
   * object.
   */
  @Test
  public void testParseTechPackContentPath() throws Exception {

    /* Reflecting the tested method */
    final Class pcClass = objUnderTest.getClass();
    final Method parseTechPackContentPath = pcClass.getDeclaredMethod("parsetpContentPath", new Class[] {});
    parseTechPackContentPath.setAccessible(true);

    /* Setting workingdirectory from which the contentpath will be parsed */
    final File contpath = new File(System.getProperty("user.dir"));
    objUnderTest.setCurrentWorkingDirectory(contpath.getPath());

    /*
     * Invoking parseTechPackContentPath() method, asserting that working
     * directory is correct and parsed contentpath given to ANT project object
     */
    parseTechPackContentPath.invoke(objUnderTest, new Object[] {});
    assertEquals(contpath.getPath(), objUnderTest.getCurrentWorkingDirectory());
    assertEquals(contpath + "/tp_installer_temp/unzipped_tp", proj.getProperty("techPackContentPath"));
  }

  /**
   * Testing dwhrepRockFactory creation. EtlrepRockFactory connection details
   * and META_DATABASE values are used to create the dwhrepFactory.
   */
  @Test
  public void testCreateDwhrepRockFactory() throws Exception {

    /* Inserting META_DATABASE data for dwhRockFactory creating */
    stmt.executeUpdate("INSERT INTO META_DATABASES VALUES"
        + "('sa', 'v1.2', 'USER', 1, 'dwhrep', 'jdbc:hsqldb:mem:testdb', '', 'desc', "
        + "'org.hsqldb.jdbcDriver', 'db')");

    /* Reflecting the tested method */
    final Class pcClass = objUnderTest.getClass();
    final Method createDwhrepRockFactory = pcClass.getDeclaredMethod("createDwhrepRockFactory", new Class[] {});
    createDwhrepRockFactory.setAccessible(true);

    /* Initializing etl & dwh RockFactory */
    final Field etlrepRockFactory = pcClass.getDeclaredField("etlrepRockFactory");
    final Field dwhrepRockFactory = pcClass.getDeclaredField("dwhrepRockFactory");
    etlrepRockFactory.setAccessible(true);
    dwhrepRockFactory.setAccessible(true);
    etlrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "con", true));
    dwhrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "con", true));

    /*
     * Asserting that the dwhRockFactory is created by checking that it is
     * initialized with correct connection values
     */
    createDwhrepRockFactory.invoke(objUnderTest, new Object[] {});
    final RockFactory dwhResult = (RockFactory) dwhrepRockFactory.get(objUnderTest);

    /* Asserting that the object is created and it has correct properties */
    final String actual = dwhResult.getDbURL() + ", " + dwhResult.getUserName() + ", " + dwhResult.getPassword() + ", "
        + dwhResult.getDriverName();
    final String expected = "jdbc:hsqldb:mem:testdb, sa, , org.hsqldb.jdbcDriver";
    assertEquals(expected, actual);
  }

  /**
   * Testing if previous installation of the same techpack of current or newer
   * version already exists. If so, techpack will not be installed (ANT property
   * "skipInstallationPhases" is set to <i>true</i>)
   */
  @Test
  public void testCheckForPrevTPInstallation() throws Exception {

    /* Inserting data into tables used in testing */
    stmt.executeUpdate("INSERT INTO TPACTIVATION VALUES ('previoustp', '0', '5', 'techpacktype', '0')");
    stmt.executeUpdate("INSERT INTO VERSIONING VALUES"
                + "('5', 'description', 0, 'previoustp', 'R2A_b12', 'techpacktype', 'productnumber', 'lockedby', "
        + " '2000-01-01 10:00:00.0', 'basedefinition', 'baseversion', 'installdescription', 'universename', "
        + "'universeextension', 'eniq_level', 'licensename')");

    /* Reflecting techpack name, version and buildnumber values */
    final Class pcClass = objUnderTest.getClass();
    final Field techPackName = pcClass.getDeclaredField("techPackName");
    final Field techPackVersion = pcClass.getDeclaredField("techPackVersion");
    final Field buildNumber = pcClass.getDeclaredField("buildNumber");
    techPackName.setAccessible(true);
    techPackVersion.setAccessible(true);
    buildNumber.setAccessible(true);
    techPackName.set(objUnderTest, "previoustp");
        techPackVersion.set(objUnderTest, "R2A");
    buildNumber.set(objUnderTest, "12");

    // Initializing dwhRockFactory
    final Field dwhrepRockFactory = pcClass.getDeclaredField("dwhrepRockFactory");
    dwhrepRockFactory.setAccessible(true);
    dwhrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "con", true));

    /*
     * Testing if ANT property "skipInstallationPhases" is set when same
     * techpack with the same or newer version already exists
     */
    objUnderTest.checkForPrevTPInstallation();
    assertEquals("true", proj.getProperty("skipInstallationPhases"));
  }

  enum eVersion {
    ONE, TWO
  }

  private String checkIfVersionsAllowsInstall_Eniq1VersionFormat(final String tpName, final int installedBuildNumber,
      final String installedRState, final int newTpBuildNumber, final String newTpRState,
      final eVersion fromEniqFormatVersion) throws Exception {

    final Class pcClass = objUnderTest.getClass();
    final Field techPackContentPath = pcClass.getDeclaredField("tpContentPath");
    techPackContentPath.setAccessible(true);

    /* Setup the from rstates and build versions.... */
    final String tpActivaction_Status = "1";
    final String tpActivaction_versionID;
    if (fromEniqFormatVersion == eVersion.ONE) {
      tpActivaction_versionID = tpName + ":b" + installedBuildNumber;
    } else {
      tpActivaction_versionID = tpName + ":((" + installedBuildNumber + "))";
    }
    final String tpActivaction_type = "Topology";

    /* Inserting data into tables used in testing */
    stmt.executeUpdate("INSERT INTO TPACTIVATION VALUES (" + "'" + tpName + "', " + "'" + tpActivaction_Status + "', "
        + "'" + tpActivaction_versionID + "', " + "'" + tpActivaction_type + "', '0')");

    final String versioning_versionID;
    if (fromEniqFormatVersion == eVersion.ONE) {
      versioning_versionID = tpName + ":b" + installedBuildNumber;
    } else {
      versioning_versionID = tpName + ":((" + installedBuildNumber + "))";
    }
    final String versioning_description = "desc";
    final String versioning_Status = "1";
    final String versioning_techPackVersion;
    if (fromEniqFormatVersion == eVersion.ONE) {
      versioning_techPackVersion = installedRState + "_b" + installedBuildNumber;
    } else {
      versioning_techPackVersion = installedRState;
    }
    final String versioning_type = "Topology";
    final String versioning_ProdNumber = "COA 252 177/1";
    final String versioning_EniqLevel = "2.0";
    stmt.executeUpdate("INSERT INTO VERSIONING VALUES" + "('" + versioning_versionID + "', '" + versioning_description
        + "', '" + versioning_Status + "', " + "'" + tpName + "', " + "'" + versioning_techPackVersion + "', '"
        + versioning_type + "', " + "'" + versioning_ProdNumber + "', 'test', "
        + "'2000-01-01 10:00:00.0', 'basedefinition', 'baseversion', " + "'installdescription', 'universename', "
        + "'universeextension', '" + versioning_EniqLevel + "', " + "'licensename')");

    /* Reflecting techpack name, version and buildnumber values */

    techPackContentPath.setAccessible(true);
        techPackContentPath.set(objUnderTest, getTestSqlDir().getParentFile().getPath());

    /* Setup the to rstate and build numbers.. */
    final Field techPackName = pcClass.getDeclaredField("techPackName");
    final Field techPackVersion = pcClass.getDeclaredField("techPackVersion");
    final Field buildNumber = pcClass.getDeclaredField("buildNumber");
    techPackName.setAccessible(true); // tech_pack.name
    techPackVersion.setAccessible(true); // tech_packversion
    buildNumber.setAccessible(true); // build.number
    techPackName.set(objUnderTest, tpName);
    techPackVersion.set(objUnderTest, newTpRState);
    buildNumber.set(objUnderTest, "" + newTpBuildNumber);

    // Initializing dwhRockFactory
    final Field dwhrepRockFactory = pcClass.getDeclaredField("dwhrepRockFactory");
    dwhrepRockFactory.setAccessible(true);
    dwhrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "con", true));

    /*
     * Testing if ANT property "skipInstallationPhases" is set when same
     * techpack with the same or newer version already exists
     */
    objUnderTest.checkForPrevTPInstallation();
    return proj.getProperty("skipInstallationPhases");
  }

  @Test
  public void testShouldUpgrade_1() throws Exception {
    final String tpName = "DC_E_ABC";
    final String fromRState = "R1B";
    final int fromBuildNumber = 1;
    final String toRState = "R1C";
    final int toBuildNumber = 1;

    final String shouldSkipInstall = checkIfVersionsAllowsInstall_Eniq1VersionFormat(tpName, fromBuildNumber,
        fromRState, toBuildNumber, toRState, eVersion.ONE);
    assertNull("skipInstallationPhases should not be set... ", shouldSkipInstall);
  }

  @Test
  public void testShouldUpgrade_2() throws Exception {
    final String tpName = "DC_E_ABC";
    final String fromRState = "R1B";
    final int fromBuildNumber = 1;
    final String toRState = "R1B";
    final int toBuildNumber = 2;

    final String shouldSkipInstall = checkIfVersionsAllowsInstall_Eniq1VersionFormat(tpName, fromBuildNumber,
        fromRState, toBuildNumber, toRState, eVersion.ONE);
    assertNull("skipInstallationPhases should not be set... ", shouldSkipInstall);
  }

  @Test
  public void testShouldUpgrade_3() throws Exception {
    final String tpName = "DC_E_ABC";
    final String fromRState = "R1B";
    final int fromBuildNumber = 1;
    final String toRState = fromRState;
    final int toBuildNumber = fromBuildNumber;

    final String shouldSkipInstall = checkIfVersionsAllowsInstall_Eniq1VersionFormat(tpName, fromBuildNumber,
        fromRState, toBuildNumber, toRState, eVersion.ONE);
    assertEquals("All RStates and Versions are the same, install should be skipped.. ", "true", shouldSkipInstall);
  }

    @Test // Failed due to RSTATE checking
  public void testShouldUpgrade_4() throws Exception {
    // This one will fail, detects R2F as less than R20A
    final String tpName = "DC_E_ABC";
    final String fromRState = "R2F";
    final int fromBuildNumber = 34;
    final String toRState = "R20A";
    final int toBuildNumber = 34;

    final String shouldSkipInstall = checkIfVersionsAllowsInstall_Eniq1VersionFormat(tpName, fromBuildNumber,
        fromRState, toBuildNumber, toRState, eVersion.ONE);
        assertNull("To State is less than from state, install step should not be skipped.. ", shouldSkipInstall);
  }

  @Test
  public void testShouldUpgrade_5() throws Exception {
    final String tpName = "DC_E_ABC";
    final String fromRState = "R2F";
    final int fromBuildNumber = 34;
    final String toRState = "R2F";
    final int toBuildNumber = 12;

    final String shouldSkipInstall = checkIfVersionsAllowsInstall_Eniq1VersionFormat(tpName, fromBuildNumber,
        fromRState, toBuildNumber, toRState, eVersion.ONE);
    assertEquals("To State is less than from State, install step should be skipped.. ", "true", shouldSkipInstall);
  }

  @Test
  public void testShouldUpgrade_6() throws Exception {
    final String tpName = "DC_E_ABC";
    final String fromRState = "R2F";
    final int fromBuildNumber = 2;
    final String toRState = "R2F";
    final int toBuildNumber = 1;

    final String shouldSkipInstall = checkIfVersionsAllowsInstall_Eniq1VersionFormat(tpName, fromBuildNumber,
        fromRState, toBuildNumber, toRState, eVersion.TWO);
    assertEquals("To State is less than from State, install step should be skipped.. ", "true", shouldSkipInstall);
  }

  @Test
  public void testShouldUpgrade_7() throws Exception {
    final String tpName = "DC_E_ABC";
    final String fromRState = "R2F";
    final int fromBuildNumber = 2;
    final String toRState = "R2F";
    final int toBuildNumber = 3;

    final String shouldSkipInstall = checkIfVersionsAllowsInstall_Eniq1VersionFormat(tpName, fromBuildNumber,
        fromRState, toBuildNumber, toRState, eVersion.TWO);
    assertNull("Build Number is greater, should install", shouldSkipInstall);
  }

  @Test
  public void testShouldUpgrade_8() throws Exception {
    final String tpName = "DC_E_CMN_STS";
    final String fromRState = "R2A";
    final int fromBuildNumber = 4;
    final String toRState = "R21B";
    final int toBuildNumber = 4;

    final String shouldSkipInstall = checkIfVersionsAllowsInstall_Eniq1VersionFormat(tpName, fromBuildNumber,
        fromRState, toBuildNumber, toRState, eVersion.ONE);
    assertNull("RSate Number is greater, should install", shouldSkipInstall);
  }

  @Test
  public void testShouldUpgrade_9() throws Exception {
    final String tpName = "DWH_MONITOR";
    final String fromRState = "R6F";
    final int fromBuildNumber = 81;

    final String toRState = "R7B";
    final int toBuildNumber = 85;

    final String shouldSkipInstall = checkIfVersionsAllowsInstall_Eniq1VersionFormat(tpName, fromBuildNumber,
      fromRState, toBuildNumber, toRState, eVersion.TWO);
    assertNull("RSate Number is greater, should install", shouldSkipInstall);
  }

    @Test
  public void testCompareRstates() {
        String firstState = "R20A";
        String secondState = "R20B";

        int res = objUnderTest.compareRstates(firstState, secondState);
        assertEquals("Second State is greater", 2, res);

        firstState = "R20B";
        secondState = "R20A";
        res = objUnderTest.compareRstates(firstState, secondState);
        assertEquals("First State is greater", 1, res);

        firstState = "R2B";
        secondState = "R2B";
        res = objUnderTest.compareRstates(firstState, secondState);
        assertEquals("State are equal", 0, res);

        firstState = "R2";
        secondState = "R2B";
        res = objUnderTest.compareRstates(firstState, secondState);
        assertEquals("Second State is greater", 2, res);

      firstState = "R2F";
      secondState =   "R20A";
      res = objUnderTest.compareRstates(firstState, secondState);
      assertEquals("Second State is greater", 2, res);

      firstState = "R6F";
      secondState =   "R7B";
      res = objUnderTest.compareRstates(firstState, secondState);
    assertEquals("TO State is greater", 2, res);
      //Do it again but reverse the states...
      res = objUnderTest.compareRstates(secondState, firstState);
    assertEquals("FROM State is greater", 1, res);

  }

  /**
   * Testing checking for tech pack installation files from invalid filepath.
   */
  @Test
  public void testCheckForTechPackInstallationWithNotExistingFiles() throws Exception {

    /* Reflecting method to be tested */
    final Class pcClass = objUnderTest.getClass();
    final Method checkForTechPackInstallation = pcClass.getDeclaredMethod("checkForTechPackInstallation",
        new Class[] {});
    checkForTechPackInstallation.setAccessible(true);

    /*
     * Reflecting version properties filepath variable to be able to set
     * properties
     */
    final Field techPackContentPath = pcClass.getDeclaredField("tpContentPath");
    techPackContentPath.setAccessible(true);
    techPackContentPath.set(objUnderTest, System.getProperty("user.dir"));

    /*
     * Testing if false is returned and "installingTechPack" ANT property is set
     * when installation files cannot be found
     */
    assertEquals(false, checkForTechPackInstallation.invoke(objUnderTest, new Object[] {}));
    assertEquals("false", proj.getProperty("installingTechPack"));
  }

  private File getFile(final String name) throws Exception {
    final URL url = ClassLoader.getSystemResource("XMLFiles");
    if (url == null) {
      throw new FileNotFoundException("XMLFiles");
    }
    final File xmlBase = new File(url.toURI());
    final String xmlFile = xmlBase.getAbsolutePath() + "/" + name;
    return new File(xmlFile);
  }
  /**
   * Testing checking for tech pack installation files from a given directory.
   * If correct files are found true is returned and "installingTechPack" ANT
   * project property value is set to the same, otherwise false.
   */
  @Test
  public void testCheckForTechPackInstallation() throws Exception {

    /* Reflecting method to be tested */
    final Class pcClass = objUnderTest.getClass();
    final Method checkForTechPackInstallation = pcClass.getDeclaredMethod("checkForTechPackInstallation",
        new Class[] {});
    checkForTechPackInstallation.setAccessible(true);

    /*
     * Reflecting version properties filepath variable to be able to set
     * properties
     */
    final File testDir = getTestSqlDir();
    final Field techPackContentPath = pcClass.getDeclaredField("tpContentPath");
    techPackContentPath.setAccessible(true);
    techPackContentPath.set(objUnderTest, testDir.getPath());

    /*
     * Creating setDirectory and few installation files to the same directory
     * for testing purposes
     */
        final File setDir = new File(testDir, "set");
    setDir.mkdirs();
    setDir.deleteOnExit();
    for (int i = 0; i < 3; i++) {
      final File tpifile = new File(setDir, "tpi" + i);
      final PrintWriter pw = new PrintWriter(new FileWriter(tpifile));
      pw.write("This is tpi" + i + " file");
      pw.close();
      tpifile.deleteOnExit();
    }

    /*
     * Testing if true is returned and "installingTechPack" ANT property is set
     * when installation files are found
     */
    assertEquals(true, checkForTechPackInstallation.invoke(objUnderTest));
    assertEquals("true", proj.getProperty("installingTechPack"));
  }

  /**
   * Testing checking for interface installation files from a given directory.
   * If correct files are found true is returned and "installingInterface" ANT
   * project property value is set to the same, otherwise false.
   */
  @Test
  public void testcheckForInterfaceInstallationWithNotExistingFiles() throws Exception {

    /* Reflecting the method to be tested */
    final Class pcClass = objUnderTest.getClass();
    final Method checkForInterfaceInstallation = pcClass.getDeclaredMethod("checkForInterfaceInstallation",
        new Class[] {});
    checkForInterfaceInstallation.setAccessible(true);

    /*
     * Reflecting version properties filepath variable to be able to read
     * properties
     */
    final Field techPackContentPath = pcClass.getDeclaredField("tpContentPath");
    techPackContentPath.setAccessible(true);
    techPackContentPath.set(objUnderTest, System.getProperty("user.dir"));

    /*
     * Testing if false is returned and "installingInterface" ANT property is
     * set when installation files cannot be found
     */
    assertEquals(false, checkForInterfaceInstallation.invoke(objUnderTest, new Object[] {}));
    assertEquals("false", proj.getProperty("installingInterface"));
  }

  /**
   * Testing checking for interface installation files from a given directory.
   * If correct files are found true is returned and "installingInterface" ANT
   * project property value is set to the same, otherwise false.
   */
  @Test
  public void testcheckForInterfaceInstallation() throws Exception {

    /* Reflecting the method to be tested */
    final Class pcClass = objUnderTest.getClass();
    final Method checkForInterfaceInstallation = pcClass.getDeclaredMethod("checkForInterfaceInstallation",
        new Class[] {});
    checkForInterfaceInstallation.setAccessible(true);

    /*
     * Reflecting version properties filepath variable to be able to read
     * properties
     */
    final File testBase = getTestSqlDir();
    final Field techPackContentPath = pcClass.getDeclaredField("tpContentPath");
    techPackContentPath.setAccessible(true);
    techPackContentPath.set(objUnderTest, testBase.getPath());

    /*
     * Creating setDirectory and few installation files to the same directory
     * for testing purposes
     */
        final File interfaceDir = new File(testBase, "interface");
    interfaceDir.mkdirs();
    interfaceDir.deleteOnExit();
    for (int i = 0; i < 3; i++) {
      final File tpiifile = new File(interfaceDir, "tpi" + i);
      final PrintWriter pw = new PrintWriter(new FileWriter(tpiifile));
      pw.write("This is tpi" + i + " file");
      pw.close();
      tpiifile.deleteOnExit();
    }

    /*
     * Testing if true is returned and "installingInterface" ANT property is set
     * when installation files are found
     */
    assertEquals(true, checkForInterfaceInstallation.invoke(objUnderTest, new Object[] {}));
    assertEquals("true", proj.getProperty("installingInterface"));
  }

  private String checkPreviousInterface(final String tpName, final String currentTpVersionNumber,
      final String newTpRState, final String newBuildNumber, final String currentTpRState, final eVersion ver)
      throws Exception {
    final String enabled = "Y";
    final String type = "Interface";

    stmt.executeUpdate("INSERT INTO META_COLLECTION_SETS VALUES" + "('0', '" + tpName + "', 'desc', '"
        + currentTpVersionNumber + "', '" + enabled + "', '" + type + "')");

    final Class pcClass = objUnderTest.getClass();

    if (ver == eVersion.TWO) {
      stmt.executeUpdate("INSERT INTO DataInterface VALUES" + "('" + tpName + "', '1', '" + type + "', 'desc', 'mdc', "
          + "'" + currentTpVersionNumber + "', 'test', '2010-10-10 10:00:00.0', 'prodNumb', '2.0', '" + currentTpRState
          + "', 'installInfo')");
      /* Initializing dwhrepRockFactory */
      final Field dwhrepRockFactory = pcClass.getDeclaredField("dwhrepRockFactory");
      dwhrepRockFactory.setAccessible(true);
      dwhrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
          "con", true));
    }

    final Method checkForPrevIntfInstallation = pcClass.getDeclaredMethod("checkForPrevIntfInstallation",
        new Class[] {});
    checkForPrevIntfInstallation.setAccessible(true);

    /* Reflecting techpack name, version and buildnumber values */
    final Field techPackName = pcClass.getDeclaredField("techPackName");
    final Field techPackVersion = pcClass.getDeclaredField("techPackVersion");
    final Field buildNumber = pcClass.getDeclaredField("buildNumber");
    techPackName.setAccessible(true);
    techPackVersion.setAccessible(true);
    buildNumber.setAccessible(true);
    techPackName.set(objUnderTest, tpName);
    techPackVersion.set(objUnderTest, newTpRState); // "R22A"
    buildNumber.set(objUnderTest, newBuildNumber); // "101"

    /* Initializing etlRockFactory */
    final Field etlrepRockFactory = pcClass.getDeclaredField("etlrepRockFactory");
    etlrepRockFactory.setAccessible(true);
    etlrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "con", true));

    /*
     * Testing if ANT property "skipInstallationPhases" is set when same
     * interface with the same or newer version already exists
     */
    checkForPrevIntfInstallation.invoke(objUnderTest, new Object[] {});
    return proj.getProperty("skipInstallationPhases");
  }

  @Test
  public void testCheckForPrevIntfInstallation_Eniq2_RStateSame_BuildSame() throws Exception {
    final String tpName = "INTF_DC_E_MGW";
    final String installedBuild = "((100))";
    final String installedRState = "R21A";
    final String newRState = "R21A";
    final String newBuildVersion = "100";
    final String skipInstallationPhases = checkPreviousInterface(tpName, installedBuild, newRState, newBuildVersion,
        installedRState, eVersion.TWO);
    assertEquals("skipInstallationPhases should set ", "true", skipInstallationPhases);
  }

  @Test
  public void testCheckForPrevIntfInstallation_Eniq2_RStateSame_BuildSmaller() throws Exception {
    final String tpName = "INTF_DC_E_MGW";
    final String installedBuild = "((100))";
    final String installedRState = "R21A";
    final String newRState = "R21A";
    final String newBuildVersion = "99";
    final String skipInstallationPhases = checkPreviousInterface(tpName, installedBuild, newRState, newBuildVersion,
        installedRState, eVersion.TWO);
    assertEquals("skipInstallationPhases should set ", "true", skipInstallationPhases);
  }

  @Test
  public void testCheckForPrevIntfInstallation_Eniq2_RStateSame_BuildGreater() throws Exception {
    final String tpName = "INTF_DC_E_MGW";
    final String installedBuild = "((100))";
    final String installedRState = "R21A";
    final String newRState = "R21A";
    final String newBuildVersion = "101";
    final String skipInstallationPhases = checkPreviousInterface(tpName, installedBuild, newRState, newBuildVersion,
        installedRState, eVersion.TWO);
    assertNull("skipInstallationPhases should not be set! ", skipInstallationPhases);
  }

  @Test
  public void testCheckForPrevIntfInstallation_Eniq2_RState_Smaller() throws Exception {
    final String tpName = "INTF_DC_E_MGW";
    final String installedBuild = "((100))";
    final String installedRState = "R21A";
    final String newRState = "R20A";
    final String newBuildVersion = "101";
    final String skipInstallationPhases = checkPreviousInterface(tpName, installedBuild, newRState, newBuildVersion,
        installedRState, eVersion.TWO);
    assertEquals("skipInstallationPhases should set ", "true", skipInstallationPhases);
  }

  @Test
  public void testCheckForPrevIntfInstallation_Eniq2_RStateLarger() throws Exception {
    final String tpName = "INTF_DC_E_MGW";
    final String installedBuild = "((100))";
    final String installedRState = "R21A";
    final String newRState = "R22A";
    final String newBuildVersion = "101";
    final String skipInstallationPhases = checkPreviousInterface(tpName, installedBuild, newRState, newBuildVersion,
        installedRState, eVersion.TWO);
    assertNull("skipInstallationPhases should not be set! ", skipInstallationPhases);
  }

  @Test
  public void testCheckForPrevIntfInstallation_Eniq1_RStateLarger() throws Exception {
    final String tpName = "INTF_DC_E_MGW";
    final String installedBuild = "R21B_b111";
    final String installedRState = "R21B";
    final String newRState = "R25A";
    final String newBuildVersion = "101";
    final String skipInstallationPhases = checkPreviousInterface(tpName, installedBuild, newRState, newBuildVersion,
        installedRState, eVersion.ONE);
      assertNull("skipInstallationPhases should not be set, new RSTATE is larger", skipInstallationPhases);
  }

  @Test
  public void testCheckForPrevIntfInstallation_Eniq1_RStateSmaller() throws Exception {
    final String tpName = "INTF_DC_E_MGW";
    final String installedBuild = "R21B_b111";
    final String installedRState = "R21B";
    final String newRState = "R20B";
    final String newBuildVersion = "101";
    final String skipInstallationPhases = checkPreviousInterface(tpName, installedBuild, newRState, newBuildVersion,
        installedRState, eVersion.ONE);
    assertEquals("skipInstallationPhases should set ", "true", skipInstallationPhases);
  }

  @Test
  public void testCheckForPrevIntfInstallation_Eniq1_RStateSame_BuildLarger() throws Exception {
    final String tpName = "INTF_DC_E_MGW";
    final String installedBuild = "R21B_b111";
    final String installedRState = "R21B";
    final String newRState = "R21B";
    final String newBuildVersion = "121";
    final String skipInstallationPhases = checkPreviousInterface(tpName, installedBuild, newRState, newBuildVersion,
        installedRState, eVersion.ONE);
    assertNull("skipInstallationPhases should not be set! ", skipInstallationPhases);
  }

  @Test
  public void testCheckForPrevIntfInstallation_Eniq1_RStateSame_BuildSmaller() throws Exception {
    final String tpName = "INTF_DC_E_MGW";
    final String installedBuild = "R21B_b111";
    final String installedRState = "R21B";
    final String newRState = "R21B";
    final String newBuildVersion = "101";
    final String skipInstallationPhases = checkPreviousInterface(tpName, installedBuild, newRState, newBuildVersion,
        installedRState, eVersion.ONE);
    assertEquals("skipInstallationPhases should set ", "true", skipInstallationPhases);
  }

  @Test
  public void testCheckForPrevIntfInstallation_Eniq1_RStateSame_BuildSame() throws Exception {
    final String tpName = "INTF_DC_E_MGW";
    final String installedBuild = "R21B_b123";
    final String installedRState = "R21B";
    final String newRState = "R21B";
    final String newBuildVersion = "123";
    final String skipInstallationPhases = checkPreviousInterface(tpName, installedBuild, newRState, newBuildVersion,
        installedRState, eVersion.ONE);
    assertEquals("skipInstallationPhases should set ", "true", skipInstallationPhases);
  }

  /**
   * Testing if previous installation of the same interface of current or newer
   * * version already exists. If so, interface will not be installed (ANT
   * property "skipInstallationPhases" is set to <i>true</i>)
   * 
   * @throws Exception
   *           e
   */
  @Test
  public void testCheckForPrevIntfInstallation() throws Exception {

    /* Inserting data into tables used in testing */
    stmt.executeUpdate("INSERT INTO META_COLLECTION_SETS VALUES"
        + "('0', 'previousinterface', 'desc', 'v5_b12', 'Y', 'A')");

    /* Reflecting method to be tested */
    final Class pcClass = objUnderTest.getClass();
    final Method checkForPrevIntfInstallation = pcClass.getDeclaredMethod("checkForPrevIntfInstallation",
        new Class[] {});
    checkForPrevIntfInstallation.setAccessible(true);

    /* Reflecting techpack name, version and buildnumber values */
    final Field techPackName = pcClass.getDeclaredField("techPackName");
    final Field techPackVersion = pcClass.getDeclaredField("techPackVersion");
    final Field buildNumber = pcClass.getDeclaredField("buildNumber");
    techPackName.setAccessible(true);
    techPackVersion.setAccessible(true);
    buildNumber.setAccessible(true);
    techPackName.set(objUnderTest, "previousinterface");
    techPackVersion.set(objUnderTest, "v5");
    buildNumber.set(objUnderTest, "12");

    /* Initializing etlRockFactory */
    final Field etlrepRockFactory = pcClass.getDeclaredField("etlrepRockFactory");
    etlrepRockFactory.setAccessible(true);
    etlrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "con", true));

    /*
     * Testing if ANT property "skipInstallationPhases" is set when same
     * interface with the same or newer version already exists
     */
    checkForPrevIntfInstallation.invoke(objUnderTest, new Object[] {});
    assertEquals("true", proj.getProperty("skipInstallationPhases"));
  }

  /**
   * Testing removal of the metadatabases. Asserting that all rows from
   * INTERFACEMEASUREMENT, INTERFACETECHPACKS and DATAINTERFACE are removed.
   */
  @Test
  public void testRemoveIntfMetadata() throws Exception {

    /* Inserting data to tables to be removed */
    stmt.executeUpdate("INSERT INTO INTERFACEMEASUREMENT VALUES"
        + "('tagid', 'dataformatid', 'tptoberemoved', 'transformerid', 1, '2001-01-02 10:00:00.0', 'description', "
        + " 'techpackversion', 'interfaceversion')");
    stmt.executeUpdate("INSERT INTO INTERFACETECHPACKS VALUES"
        + "('tptoberemoved', 'techpackname', 'techpackversion', 'interfaceversion')");
    stmt.executeUpdate("INSERT INTO DATAINTERFACE VALUES"
        + "('tptoberemoved', 1, 'interfacetype', 'description', 'dataformattype', 'interfaceversion', "
        + "'lockedby', '2004-10-10 10:00:00.0', 'productnumber', 'eniq_level', 'rstate', 'install info')");

    /* Reflecting method to be tested */
    final Class pcClass = objUnderTest.getClass();
    final Method removeIntfMetadata = pcClass.getDeclaredMethod("removeIntfMetadata", new Class[] {});
    removeIntfMetadata.setAccessible(true);

    /* Initializing dwhRockFactory */
    final Field dwhrepRockFactory = pcClass.getDeclaredField("dwhrepRockFactory");
    dwhrepRockFactory.setAccessible(true);
    dwhrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "con", true));

    /* Reflecting techpack name */
    final Field techPackName = pcClass.getDeclaredField("techPackName");
    techPackName.setAccessible(true);
    techPackName.set(objUnderTest, "tptoberemoved");
    IDataSet actualDataSet = new DatabaseConnection(con).createDataSet();
    ITable actualINTERFACEMEASUREMENT = actualDataSet.getTable("INTERFACEMEASUREMENT");
    ITable actualINTERFACETECHPACKS = actualDataSet.getTable("INTERFACETECHPACKS");
    ITable actualDATAINTERFACE = actualDataSet.getTable("DATAINTERFACE");

    /* Making sure the testdata is in place */
    if (actualINTERFACEMEASUREMENT.getRowCount() == 1 && actualINTERFACETECHPACKS.getRowCount() == 1
        && actualDATAINTERFACE.getRowCount() == 1) {

      /* Invoking removeIntfMetadata() method */
      removeIntfMetadata.invoke(objUnderTest, new Object[] {});

      /* Asserting that there are no rows in any of the tables */
      actualDataSet = new DatabaseConnection(con).createDataSet();
      actualINTERFACEMEASUREMENT = actualDataSet.getTable("INTERFACEMEASUREMENT");
      actualINTERFACETECHPACKS = actualDataSet.getTable("INTERFACETECHPACKS");
      actualDATAINTERFACE = actualDataSet.getTable("DATAINTERFACE");
      assertEquals(0, actualINTERFACEMEASUREMENT.getRowCount());
      assertEquals(0, actualINTERFACETECHPACKS.getRowCount());
      assertEquals(0, actualDATAINTERFACE.getRowCount());
    } else {
      fail("Test Error - Something has gone wrong in initializing test tables. \n"
          + "There was either too many or no rows at all in one or more of the test tables.");
    }
  }

  /**
   * Testing removal of interface sets. RemoveIntfSets() method goes through
   * META_COLLECTION_SETS searching for given set by name. When set is found
   * rows from META_COLLECTIONS and META_TRANSFER_ACTIONS are removed which
   * belong to the to be removed set.
   */
  @Test
  public void testRemoveIntfSets() throws Exception {

    /* Inserting data to tables to be removed */
    stmt.executeUpdate("INSERT INTO META_COLLECTION_SETS VALUES"
        + "('0', 'interfacetoberemoved-testOSS', 'desc', 'v5_b12', 'Y', 'A')");
    stmt.executeUpdate("INSERT INTO META_COLLECTIONS VALUES"
        + "(0, 'testcollection', 'collection', 'mailerroraddrs', 'mailfailaddrs', 'mailbugaddrs', 1, 1, 1, 'Y',"
        + " 'Y', '2008-01-01 00:00:00.0', '1.0', 0, '0', 0, 1, 'Y', 'A', 'Y', 'meastype', 'Y', 'schedulinginfo')");
    stmt.executeUpdate("INSERT INTO META_TRANSFER_ACTIONS VALUES"
        + "('1.1', 0, 0, 0, 'actiontype', 'transferactionname', 1, 'description', 'Y', 1, 'whereclause2',"
        + " 'whereclause3', 'actioncontents3', 'actioncontents2', 'actioncontents1', 'whereclause1')");
    //adding here into META_SCHEDULINGS
    stmt.executeUpdate("INSERT INTO META_SCHEDULINGS VALUES"
            + "('1.0', 0, 'weekly', 'oscommand', 1, 1, 1, 1, 0, 1, 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'status',"
            + " '2008-01-01 00:00:00.0', 1, 1, 'testschedule', 'N', 1, 2008, 'triggercommand', 1)");

    /* Reflecting the tested method */
    final Class pcClass = objUnderTest.getClass();
    final Method removeIntfSets = pcClass.getDeclaredMethod("removeIntfSets", new Class[] {});
    removeIntfSets.setAccessible(true);

    /* Initializing etlRockFactory */
    final Field etlrepRockFactory = pcClass.getDeclaredField("etlrepRockFactory");
    etlrepRockFactory.setAccessible(true);
    etlrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "con", true));

    /* Reflecting techpack name */
    final Field techPackName = pcClass.getDeclaredField("techPackName");
    techPackName.setAccessible(true);
    techPackName.set(objUnderTest, "interfacetoberemoved-testOSS");
    IDataSet actualDataSet = new DatabaseConnection(con).createDataSet();
    ITable actualMETA_COLLECTION_SETS = actualDataSet.getTable("META_COLLECTION_SETS");
    ITable actualMETA_COLLECTIONS = actualDataSet.getTable("META_COLLECTIONS");
    ITable actualMETA_TRANSFER_ACTIONS = actualDataSet.getTable("META_TRANSFER_ACTIONS");

    /* Making sure the testdata is in place */
    if (actualMETA_COLLECTION_SETS.getRowCount() == 1 && actualMETA_COLLECTIONS.getRowCount() == 1
        && actualMETA_TRANSFER_ACTIONS.getRowCount() == 1) {

      /* Invoking removeIntfMetadata() method */
      removeIntfSets.invoke(objUnderTest, new Object[] {});

      /* Asserting that there are no rows in any of the tables */
      actualDataSet = new DatabaseConnection(con).createDataSet();
      actualMETA_COLLECTION_SETS = actualDataSet.getTable("META_COLLECTION_SETS");
      actualMETA_COLLECTIONS = actualDataSet.getTable("META_COLLECTIONS");
      actualMETA_TRANSFER_ACTIONS = actualDataSet.getTable("META_TRANSFER_ACTIONS");
      assertEquals(0, actualMETA_COLLECTION_SETS.getRowCount());
      assertEquals(0, actualMETA_COLLECTIONS.getRowCount());
      assertEquals(0, actualMETA_TRANSFER_ACTIONS.getRowCount());
    } else {
      fail("Test Error - Something has gone wrong in initializing test tables. \n"
          + "There was either too many or no rows at all in one or more of the test tables.");
    }
  }

  /**
   * Testing logfile updating.
   */
  @Test
  public void testUpdateEngineLoggingFile() throws Exception {

    /*
     * Initializing new PreinstallCheck instance and reflecting the tested
     * method
     */
    final Class pcClass = objUnderTest.getClass();
    final Method updateEngineLoggingFile = pcClass.getDeclaredMethod("updateEngineLoggingFile",
        new Class[] { String.class });
    updateEngineLoggingFile.setAccessible(true);

    /* Setting config files pathname */
    objUnderTest.setCurrentWorkingDirectory(engprop.getParent());
    objUnderTest.setConfigurationDirectory(engprop.getParent());
    /* Asserting that the log property is changed */
    updateEngineLoggingFile.invoke(objUnderTest, new Object[] { "new" });
    final Properties props = new Properties();
    props.load(new FileInputStream(engprop));
    final String actual = props.getProperty("etl.new.level");
    assertEquals("newvalue", actual);
  }

  /**
   * Testing setting and getting <i>CheckForRequiredTechPacks</i> variable used
   * in PreinstallCheck class.
   */
  @Test
  public void testSetAndGetCheckForRequiredTechPacks() throws Exception {
    objUnderTest.setCheckForRequiredTechPacks("testCheckForRequiredTechPacks");
    assertEquals("testCheckForRequiredTechPacks", objUnderTest.getCheckForRequiredTechPacks());
  }

  /**
   * Testing setting and getting <i>CurrentWorkingDirectory</i> variable used in
   * PreinstallCheck class.
   */
  @Test
  public void testSetAndGetCurrentWorkingDirectory() throws Exception {
    objUnderTest.setCurrentWorkingDirectory("testCurrentWorkingDirectory");
    assertEquals("testCurrentWorkingDirectory", objUnderTest.getCurrentWorkingDirectory());
  }

  /**
   * Testing setting and getting <i>ConfigurationDirectory</i> variable used in
   * PreinstallCheck class.
   */
  @Test
  public void testSetAndGetConfigurationDirectory() throws Exception {
    objUnderTest.setConfigurationDirectory("testConfigurationDirectory" + File.separator);
    assertEquals("testConfigurationDirectory" + File.separator, objUnderTest.getConfigurationDirectory());

    objUnderTest.setConfigurationDirectory("testConfigurationDirectory");
    assertEquals("testConfigurationDirectory" + File.separator, objUnderTest.getConfigurationDirectory());
  }

  /**
   * Testing setting and getting <i>BinDirectory</i> variable used in
   * PreinstallCheck class.
   */
  @Test
  public void testSetAndGetBinDirectory() throws Exception {
    objUnderTest.setBinDirectory("testBinDirectory");
    assertEquals("testBinDirectory", objUnderTest.getBinDirectory());
  }

  /**
   * Testing required techpack checking with generic input. Certain techpacks
   * might need other techpacks to be installed before they can be installed and
   * checkRequiredTechPackInstallations() method does just that. Exception is
   * thrown if one or more of required techpacks are missing or they are of
   * lesser version.
   */
  @Test
  public void testCompareProductNumbers() throws Exception {

    /* Reflecting the tested method */
    final Class pcClass = objUnderTest.getClass();
    final Method compareProductNumbers = pcClass.getDeclaredMethod("compareProductNumbers", new Class[] { String.class,
        String.class });
    compareProductNumbers.setAccessible(true);

    /* Testing that techpack installation check passes */
    try {
      final Integer result = (Integer) compareProductNumbers.invoke(objUnderTest, new Object[] { "COA 123 456",
          "COA 123 456" });
      assertEquals(new Integer(0), result);

      final Integer result2 = (Integer) compareProductNumbers.invoke(objUnderTest, new Object[] { "COA 123 456/1",
          "COA 123 456" });
      assertEquals(new Integer(1), result2);

      final Integer result3 = (Integer) compareProductNumbers.invoke(objUnderTest, new Object[] { "COA 123 456",
          "COA 123 456/1" });
      assertEquals(new Integer(2), result3);

      final Integer result4 = (Integer) compareProductNumbers.invoke(objUnderTest, new Object[] { "COA 123 456/1",
          "COA 123 456/1" });
      assertEquals(new Integer(0), result4);

      final Integer result5 = (Integer) compareProductNumbers.invoke(objUnderTest, new Object[] { "COA 123 456/2",
          "COA 123 456/1" });
      assertEquals(new Integer(1), result5);

      final Integer result6 = (Integer) compareProductNumbers.invoke(objUnderTest, new Object[] { "COA 123 456/1",
          "COA 123 456/2" });
      assertEquals(new Integer(2), result6);

      final Integer result7 = (Integer) compareProductNumbers.invoke(objUnderTest, new Object[] { "COA 123 456/1",
          "COA 123 456/10" });
      assertEquals(new Integer(2), result7);

      final Integer result8 = (Integer) compareProductNumbers.invoke(objUnderTest, new Object[] { "COA 123 456/10",
          "COA 123 456/1" });
      assertEquals(new Integer(1), result8);

      final Integer result9 = (Integer) compareProductNumbers.invoke(objUnderTest, new Object[] { "one", "two" });
      assertEquals(new Integer(-1), result9);

      /* Test passed */
    } catch (final Exception e) {
      fail("Test Failed - Unexpected exception!\n" + e);
    }
  }

  /**
   * Testing method readMetadataValue in PreinstallCheck.
   */
  @Test
  public void testReadMetadataValue() throws Exception {
    // Reflecting the method under test - readMetadataValue.

    final Class pcClass = objUnderTest.getClass();
    final Method readMetadataValue = pcClass.getDeclaredMethod("readMetadataValue", new Class[] { String.class });
    readMetadataValue.setAccessible(true);

    // Reflecting and setting fields - tpContentPath and techPackName.
    final File testBase = getTestSqlDir();
    final Field techPackContentPath = pcClass.getDeclaredField("tpContentPath");
    techPackContentPath.setAccessible(true);
	    techPackContentPath.set(objUnderTest, testBase.getParentFile().getPath());

    final Field techPackName = pcClass.getDeclaredField("techPackName");
    techPackName.setAccessible(true);
    techPackName.set(objUnderTest, "INTF_DC_E_CUDB");

    // Case 1: TP_Type
        final String actualValue1 = (String) readMetadataValue.invoke(objUnderTest, "TP_TYPE");
    assertEquals("PM", actualValue1);

    // Case 2: PROD_NUMBER
        final String actualValue2 = (String) readMetadataValue.invoke(objUnderTest, "PROD_NUMBER");
    assertEquals("COA 252 177/1", actualValue2);
  }

  public void setForMzTpChecks(String tpName, String tpVersion, String builNumber, String productNumber)
      throws Exception {

    /* Reflecting techpack name, version and buildnumber values */
    final Class pcClass = objUnderTest.getClass();
    final Field techPackName = pcClass.getDeclaredField("techPackName");
    final Field techPackVersion = pcClass.getDeclaredField("techPackVersion");
    final Field buildNumber = pcClass.getDeclaredField("buildNumber");
    final Field props = pcClass.getDeclaredField("props");
    Properties p = new Properties();
    p.put("product.number", productNumber);
    techPackName.setAccessible(true);
    techPackVersion.setAccessible(true);
    buildNumber.setAccessible(true);
    techPackName.set(objUnderTest, tpName);
    techPackVersion.set(objUnderTest, tpVersion);
    buildNumber.set(objUnderTest, builNumber);

    props.setAccessible(true);
    props.set(objUnderTest, p);

    // Initializing dwhRockFactory
    final Field dwhrepRockFactory = pcClass.getDeclaredField("dwhrepRockFactory");
    dwhrepRockFactory.setAccessible(true);
    dwhrepRockFactory.set(objUnderTest, new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "con", true));
  }

  /**
   * Testing if previous installation of the same techpack of current or newer
   * version already exists. If so, techpack will not be installed.
   * 
   * Scenario : No Mz TP is installed
   */
  @Test
  public void test1CheckForPrevMzTPInstallation() throws Exception {

    /* Inserting data into tables used in testing */
    setForMzTpChecks("M_E_SGEH", "R1B", "10", "CXC101010");
    /*
     * Testing if ANT property "skipInstallationPhases" is set when same
     * techpack with the same or newer version already exists
     */
    assertTrue(0==objUnderTest.checkForPrevMzTPInstallation());
  }

  /**
   * Testing if previous installation of the same techpack of current or newer
   * version already exists. If so, techpack will not be installed.
   * 
   * Scenario : Same Mz TP is installed
   */
  @Test
  public void test2CheckForPrevMzTPInstallation() throws Exception {
    setForMzTpChecks("M_E_SGEH", "R1C", "10", "CXC101010");
    stmt.execute("INSERT INTO MZTechPacks VALUES('M_E_SGEH:((10))','M_E_SGEH','Active','2010-10-10 10:00:00.0','PRO123 ','MZTP ','R1C')");
    assertFalse(0==objUnderTest.checkForPrevMzTPInstallation());
  }

  /**
   * Testing if previous installation of the same techpack of current or newer
   * version already exists. If so, techpack will not be installed.
   * 
   * Scenario : Trying to Upgrade Mz TP with higher version
   */
  @Test
  public void test3CheckForPrevMzTPInstallation() throws Exception {
    setForMzTpChecks("M_E_SGEH", "R1D", "10", "CXC101010");
    stmt.execute("INSERT INTO MZTechPacks VALUES('M_E_SGEH:((9))','M_E_SGEH','Active','2010-10-10 10:00:00.0','PRO123 ','MZTP ','R1C')");
    assertTrue(0==objUnderTest.checkForPrevMzTPInstallation());
  }

  /**
   * Testing if previous installation of the same techpack of current or newer
   * version already exists. If so, techpack will not be installed.
   * 
   * Scenario : Trying to Upgrade Mz TP with lower version
   */
  @Test
  public void test4CheckForPrevMzTPInstallation() throws Exception {
    setForMzTpChecks("M_E_SGEH", "R1B", "9", "CXC101010");
    stmt.execute("INSERT INTO MZTechPacks VALUES('M_E_SGEH:((10))','M_E_SGEH','Active','2010-10-10 10:00:00.0','PRO123 ','MZTP ','R1C')");
    assertFalse(0==objUnderTest.checkForPrevMzTPInstallation());
  }

  /**
   * Testing if previous installation of the same techpack of current or newer
   * version already exists. If so, techpack will not be installed.
   * 
   * Scenario : Trying to Upgrade Mz TP with lower version
   */
  @Test
  public void test5CheckForPrevMzTPInstallation() throws Exception {

    setForMzTpChecks("M_E_SGEH", "R1C", "9", "CXC101010");

    stmt.execute("INSERT INTO MZTechPacks VALUES('M_E_SGEH1:((10))','M_E_SGEH1','Active','2010-10-10 10:00:00.0','CXC101010','MZTP ','R1C')");
    stmt.execute("INSERT INTO MZTechPacks VALUES('M_E_SGEH2:((11))','M_E_SGEH2','Active','2010-10-10 10:00:00.0','PRO123 ','MZTP ','R1C')");
    stmt.execute("INSERT INTO MZTechPacks VALUES('M_E_SGEH3:((12))','M_E_SGEH3','Active','2010-10-10 10:00:00.0','PRO123 ','MZTP ','R1C')");
    assertTrue(0==objUnderTest.checkForPrevMzTPInstallation());
  }

  @Test
  public void test6CheckForPrevMzTPInstallation() throws Exception {

    setForMzTpChecks("M_E_SGEH", "R1B", "10", "CXC101010");

    stmt.execute("INSERT INTO MZTechPacks VALUES('M_E_SGEH1:((110))','M_E_SGEH1','Active','2010-10-10 10:00:00.0','CXC101010','MZTP ','R1C')");
    stmt.execute("INSERT INTO MZTechPacks VALUES('M_E_SGEH2:((101))','M_E_SGEH2','Active','2010-10-10 10:00:00.0','PRO123 ','MZTP ','R1C')");
    stmt.execute("INSERT INTO MZTechPacks VALUES('M_E_SGEH:((1110))','M_E_SGEH','Active','2010-10-10 10:00:00.0','PRO123 ','MZTP ','R1C')");

    objUnderTest.checkForPrevMzTPInstallation();
    assertFalse(0==objUnderTest.checkForPrevMzTPInstallation());
  }


  private static File getTestSqlDir() throws FileNotFoundException, URISyntaxException {
    final URL url = ClassLoader.getSystemResource("sql");
		if(url == null){
			throw new FileNotFoundException("sql/");
		}
		return new File(url.toURI());
  }
  
  /**
   * testing deleteOldBOPackage method success.
   * @throws Exception
   */
    @Test
	public void testDeleteOldBOPackageSuccess() throws Exception {
		final String reportPackage = "BO_E_GGSN";
		final Class pcClass = objUnderTest.getClass();
		final Method deleteOldBOPackage = pcClass.getDeclaredMethod("deleteOldBOPackage",
				new Class[] { String.class, String.class });
		deleteOldBOPackage.setAccessible(true);

		//using user.dir for temporary storage of the file.
		File Dir = new File(System.getProperty("user.dir"));
		
		//creating the reportPackage directory inside user.dir
		final String reportPackagePath = Dir + File.separator + reportPackage + "_R15B";
		File dirToDel = new File(reportPackagePath);
		dirToDel.mkdirs();
		File fileToDel = new File(reportPackagePath, "fileToDelete.unv");
		
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(fileToDel));
			pw.write("foobar");
			pw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("can't write inFile");
		}
		//delete on JVM exit both reportPackage directory and file created inside that.
		fileToDel.deleteOnExit();
		dirToDel.deleteOnExit();
		deleteOldBOPackage.invoke(objUnderTest, new Object[] { reportPackage,
				Dir.getAbsolutePath() });

		assertFalse("Directory should be removed after execution of this testcase",
				dirToDel.exists());
		assertFalse("File inside the directory should be removed after execution of this testcase",
				fileToDel.exists());

	}
    
    /**
     * negative testcase for deleteOldBOPackage method with reportPackage null.
     * @throws Exception
     */
	@Test
	public void testDeleteOldBOPackageFailureWithReportPackageNull()
			throws Exception {
		final String reportPackage = "BO_E_BSS";
		final Class pcClass = objUnderTest.getClass();
		final Method deleteOldBOPackage = pcClass.getDeclaredMethod(
				"deleteOldBOPackage",
				new Class[] { String.class, String.class });
		deleteOldBOPackage.setAccessible(true);
		
		File Dir = new File(System.getProperty("user.dir"));

		try {
			deleteOldBOPackage.invoke(objUnderTest, new Object[] {
					reportPackage, Dir.getAbsolutePath() });
		} catch (Exception e) {
			fail("Exception is not supposed to raise during deletion as it may affect the installation process");
		}
	}
	
	/**
	 * negative testcase for deleteOldBOPackage method with boUniversePath null.
	 * @throws Exception
	 */
	@Test
	public void testDeleteOldBOPackageFailureWithBOUniversePathNull()
			throws Exception {
		final String reportPackage = null;
		final Class pcClass = objUnderTest.getClass();
		final Method deleteOldBOPackage = pcClass.getDeclaredMethod(
				"deleteOldBOPackage",
				new Class[] { String.class, String.class });
		deleteOldBOPackage.setAccessible(true);

		try {
			deleteOldBOPackage.invoke(objUnderTest, new Object[] {
					reportPackage, null });
		} catch (Exception e) {
			fail("Exception is not supposed to raise during deletion as it may affect the installation process");
		}

	}
	
	/**
	 * negative testcase for deleteOldBOPackage method with boUniversePath not exists.
	 * @throws Exception
	 */
	@Test
	public void testDeleteOldBOPackageFailureWithBOUniversePathNotExists()
			throws Exception {
		final String reportPackage = "BO_E_BSS";
		final Class pcClass = objUnderTest.getClass();
		final Method deleteOldBOPackage = pcClass.getDeclaredMethod(
				"deleteOldBOPackage",
				new Class[] { String.class, String.class });
		deleteOldBOPackage.setAccessible(true);

		// using directory which doesn't exist.
		File Dir = new File(System.getProperty("user.dir") + File.separator
				+ reportPackage + "_R15B");

		try {
			deleteOldBOPackage.invoke(objUnderTest, new Object[] {
					reportPackage, Dir.getAbsolutePath() });
		} catch (Exception e) {
			fail("Exception is not supposed to raise during deletion as it may affect the installation process");
		}
	}
	
  /**
   * Happy test case for addToInstalledFile(). If we add 4 or 5 techpacks to the
   * file, we should be able to read back out all of these techpacks.
   */
  @Test
	public void testAddToInstalledFile() {

	  // Define test list of interfaces and tech packs that have been upgraded:
	  final ArrayList<String> listOfTechPacks = new ArrayList<String>();
	  listOfTechPacks.add("DC_E_CPP");
	  listOfTechPacks.add("DC_E_SGSN");
	  listOfTechPacks.add("INTF_DC_E_SGSN");
	  listOfTechPacks.add("INTF_DC_E_CPP");
	  listOfTechPacks.add("INTF_DC_E_GGSN");
	  	  
	  try {
	    // Create temporary file to simulate upgraded_artifacts file:	  
      final File dummy_artifacts_file = File.createTempFile("installed_artifacts", ".tmp");
      
      PreinstallCheck testObject = new PreinstallCheck() {
        protected String getInstalledFilename(final INIGet iniGet) {
          return dummy_artifacts_file.getAbsolutePath();
        }
      };
            
      // Call addToUpgradedFile() for each tech pack/interface:
      for (String techpack : listOfTechPacks) {
        testObject.addToInstalledFile(techpack);   
        System.out.println("Added " + techpack);
      }
      
      // Check that file has everything we added:
      final ArrayList<String> techPacksFromFile = new ArrayList<String>();
      BufferedReader br = new BufferedReader(new FileReader(dummy_artifacts_file.getAbsolutePath()));
      String line;
      while ((line = br.readLine()) != null) {
        techPacksFromFile.add(line);
        System.out.println("Read in " + line);
      }
      br.close();
      
      // Check if each tech pack added is in the file:
      for (String techpack : listOfTechPacks) {
        if (!techPacksFromFile.contains(techpack)) {
          fail("Couldn't find " + techpack + " in list of installed tech packs.");
        }
      }
    } catch (IOException e) {
      fail("Failed to create temporary file.");
      e.printStackTrace();
    }
	}
  
  /**
   * addToInstalledFile() method should be able to handle empty or null arguments.
   */
  @Test
  public void testAddToInstalledFile_EmptyTechPackName() {
    // Set up test object and override writing to file:
    PreinstallCheck testObject = new PreinstallCheck() {
      protected void appendLineToFile(final String filename, final boolean append, final String textToAdd)
          throws IOException {
        System.out.println("Wrote " + textToAdd + " to file.");
      }
    };
    // Both of these calls should be handled and the method should return.
    // Build should print a warning and continue.
    testObject.addToInstalledFile(null);
    testObject.addToInstalledFile("");
  }
  
  /**
   * addToInstalledFile() method should throw a build exception if there is a 
   * file error while writing to the installed_artifacts file.
   */
  @Test(expected = BuildException.class)
  public void testAddToInstalledFile_FileError() {
    // Set up test object and set up writeLineToFile() to throw an IOException:
    PreinstallCheck testObject = new PreinstallCheck() {
      protected void appendLineToFile(final String filename, final boolean append, final String textToAdd)
          throws IOException {
        throw new IOException("File error!");
      }
    };

    // Set up dummy properties for StaticProperties:
    final String INSALLED_FILE_PROPERTY = "installedArtifactsFile";   
    final Properties testProperties = new Properties();
    testProperties.setProperty(INSALLED_FILE_PROPERTY, "testFile.txt");
    StaticProperties.giveProperties(testProperties);

    testObject.addToInstalledFile("DC_E_CPP");
  }
  
  /**
   * Test getting the installed techpacks filename from niq.ini.
   */
  @Test
  public void testGetInstalledFilename() {    
    final INIGet inigetMock = mockingContext.mock(INIGet.class);
    final File niq_ini_fileMock = mockingContext.mock(File.class);
    
    // Create test object:
    PreinstallCheck testObject = new PreinstallCheck() {
      protected File getNiqIniFile() {
        // return a mock File instead of a real one.
        return niq_ini_fileMock;
      }
    };
    
    // Set up mocking for INIGet and File:
    mockingContext.checking(new Expectations() {
      {
          // Mock out calls to niq.ini file.
          // niq.ini exists and has the usual path.
          one(niq_ini_fileMock).exists();
          will(returnValue(true));
          
          one(niq_ini_fileMock).getPath();
          will(returnValue("/eniq/sw/conf/niq.ini"));
        
          // Mock out calls to INIGet. Simulate it returning the file name:
          one(inigetMock).setFile("/eniq/sw/conf/niq.ini");
          one(inigetMock).setSection("FEATURE_INFO");
          one(inigetMock).setParameter("Last_Tech_Packs_Installed_File");
          one(inigetMock).execute(null);
          
          // Parameter we get back for the filename value will be the expected one (installed_artifacts):
          one(inigetMock).getParameterValue();
          will(returnValue("installed_artifacts"));
      }
    });

    final String filename = testObject.getInstalledFilename(inigetMock);    
    final String expectedFilename = "/eniq/sw/installer/installed_artifacts";
    Assert.assertEquals("getInstalledFilename should return the expected filename", expectedFilename, filename);
  }
  
  /**
   * Test getting the installed techpacks filename from niq.ini when niq.ini file doesn't exist.
   * Should return the default filename instead. 
   */
  @Test
  public void testGetInstalledFilename_iniFileDoesntExist() {
    final INIGet inigetMock = mockingContext.mock(INIGet.class);
    final File niq_ini_fileMock = mockingContext.mock(File.class);

    // Create test object:
    PreinstallCheck testObject = new PreinstallCheck() {
      protected File getNiqIniFile() {
        // return a mock file instead of a real one.
        return niq_ini_fileMock;
      }
    };

    // Set up mocking for INIGet and File:
    mockingContext.checking(new Expectations() {
      {
        // Test case where niq.ini file doesn't exist:
        one(niq_ini_fileMock).exists();
        will(returnValue(false));
      }
    });

    final String filename = testObject.getInstalledFilename(inigetMock);

    final String expectedFilename = "/eniq/sw/installer/installed_artifacts";
    Assert.assertEquals("getInstalledFilename should return the default filename if niq.ini file can't be found",
        expectedFilename, filename);
  }
  
  /**
   * Test getting the installed techpacks filename from niq.ini when the filename param isn't in the niq.ini file.
   * Should return the default filename instead. 
   */
  @Test
  public void testGetInstalledFilename_paramNotFound() {
    final INIGet inigetMock = mockingContext.mock(INIGet.class);
    final File niq_ini_fileMock = mockingContext.mock(File.class);

    // Create test object:
    PreinstallCheck testObject = new PreinstallCheck() {
      protected File getNiqIniFile() {
        // return a mock niq.ini file instead of a real one.
        return niq_ini_fileMock;
      }
    };

    // Set up mocking for INIGet and File:
    mockingContext.checking(new Expectations() {

      {
        // Mock out calls to niq.ini file:
        one(niq_ini_fileMock).exists();
        will(returnValue(true));
        
        one(niq_ini_fileMock).getPath();
        will(returnValue("/eniq/sw/conf/niq.ini"));
      
        // Mock out calls to INIGet. Simulate it returning the file name:
        one(inigetMock).setFile("/eniq/sw/conf/niq.ini");
        one(inigetMock).setSection("FEATURE_INFO");
        one(inigetMock).setParameter("Last_Tech_Packs_Installed_File");
        one(inigetMock).execute(null);
        
        // Parameter
        one(inigetMock).getParameterValue();
        will(returnValue(""));
      }
    });

    final String filename = testObject.getInstalledFilename(inigetMock);

    final String expectedFilename = "/eniq/sw/installer/installed_artifacts";
    Assert.assertEquals("getInstalledFilename should return the default filename if niq.ini file can't be found",
        expectedFilename, filename);
  }
  
}
