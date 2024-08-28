package com.distocraft.dc5000.install.ant;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.dbunit.Assertion;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.common.StaticProperties;

/**
 * Tests for ETLSetInstallation class in com.distocraft.dc5000.install.ant.<br>
 * <br>
 * Testing if sets from xml file are installed (inserted into database).
 * 
 * @author EJAAVAH
 */
public class ETLSetInstallationTest {

  private static ETLSetInstallation objUnderTest;

  private static Project proj;

  private static Statement stmt;

  private static Connection con = null;

  private static Method installSets;

  private static Method createRockFactory;

  private static Method disablePreviousMetaCollectionSets;

  private static Method disablePreviousSchedules;

  private static Method disablePreviousMetaCollections;

  private static Method disablePreviousMetaTransferActions;


  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    StaticProperties.giveProperties(new Properties());
    try {
      Class.forName("org.hsqldb.jdbcDriver").newInstance();
      con = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "sa", "");
    } catch (Exception e) {
      e.printStackTrace();
    }
    stmt = con.createStatement();
    stmt.execute("CREATE TABLE META_COLLECTION_SETS (COLLECTION_SET_ID VARCHAR(31),COLLECTION_SET_NAME VARCHAR(31), "
        + "DESCRIPTION VARCHAR(31), VERSION_NUMBER VARCHAR(31), ENABLED_FLAG VARCHAR(31), TYPE VARCHAR(31))");
    stmt.execute("CREATE TABLE META_SCHEDULINGS (VERSION_NUMBER VARCHAR(31), ID BIGINT, EXECUTION_TYPE VARCHAR(31), "
        + "OS_COMMAND VARCHAR(31), SCHEDULING_MONTH BIGINT, SCHEDULING_DAY BIGINT, SCHEDULING_HOUR BIGINT, "
        + "SCHEDULING_MIN BIGINT, COLLECTION_SET_ID BIGINT, COLLECTION_ID BIGINT, MON_FLAG VARCHAR(31), "
        + " TUE_FLAG VARCHAR(31), WED_FLAG VARCHAR(31), THU_FLAG VARCHAR(31), FRI_FLAG VARCHAR(31), "
        + "SAT_FLAG VARCHAR(31), SUN_FLAG VARCHAR(31), STATUS VARCHAR(31), LAST_EXECUTION_TIME TIMESTAMP, "
        + "INTERVAL_HOUR BIGINT, INTERVAL_MIN BIGINT, NAME VARCHAR(31), HOLD_FLAG VARCHAR(31), PRIORITY BIGINT, "
        + "SCHEDULING_YEAR BIGINT, TRIGGER_COMMAND VARCHAR(31), LAST_EXEC_TIME_MS BIGINT)");
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

    /* Creating apache ant Project object for setting connection properties */
    proj = new Project();
    proj.setProperty("etlrepDatabaseUrl", "jdbc:hsqldb:mem:testdb");
    proj.setProperty("etlrepDatabaseUsername", "sa");
    proj.setProperty("etlrepDatabasePassword", "");
    proj.setProperty("etlrepDatabaseDriver", "org.hsqldb.jdbcDriver");
    proj.setProperty("dwhrepDatabaseUrl", "jdbc:hsqldb:mem:testdb");
    proj.setProperty("dwhrepDatabaseUsername", "sa");
    proj.setProperty("dwhrepDatabasePassword", "");
    proj.setProperty("dwhrepDatabaseDriver", "org.hsqldb.jdbcDriver");

    try {

      objUnderTest = new ETLSetInstallation();
      Class ETLSI = objUnderTest.getClass();
      installSets = ETLSI.getDeclaredMethod("installSets", new Class[] {});
      installSets.setAccessible(true);
      createRockFactory = ETLSI.getDeclaredMethod("createRockFactory", new Class[] { HashMap.class });
      createRockFactory.setAccessible(true);
      disablePreviousMetaCollectionSets = ETLSI.getDeclaredMethod("disablePreviousMetaCollectionSets",
          new Class[] { String.class });
      disablePreviousMetaCollectionSets.setAccessible(true);
      disablePreviousSchedules = ETLSI.getDeclaredMethod("disablePreviousSchedules", new Class[] { String.class });
      disablePreviousSchedules.setAccessible(true);
      disablePreviousMetaCollections = ETLSI.getDeclaredMethod("disablePreviousMetaCollections",
          new Class[] { String.class });
      disablePreviousMetaCollections.setAccessible(true);
      disablePreviousMetaTransferActions = ETLSI.getDeclaredMethod("disablePreviousMetaTransferActions",
          new Class[] { String.class });
      disablePreviousMetaTransferActions.setAccessible(true);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {

    stmt.execute("DROP TABLE META_SCHEDULINGS");
    stmt.execute("DROP TABLE META_COLLECTIONS");
    stmt.execute("DROP TABLE META_TRANSFER_ACTIONS");
    stmt.execute("DROP TABLE META_COLLECTION_SETS");
    con = null;
    objUnderTest = null;
  }

  @Before
  public void setupBeforeTest() throws Exception {

    /*
     * Initializing often used objects/methods before every test to avoid mix
     * ups
     */
    objUnderTest = new ETLSetInstallation();
    objUnderTest.setProject(proj);
    objUnderTest.etlrepRockFactory = new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "PreinstallCheck", true);
    objUnderTest.dwhrepRockFactory = new RockFactory("jdbc:hsqldb:mem:testdb", "sa", "", "org.hsqldb.jdbcDriver",
        "PreinstallCheck", true);
  }

  @After
  public void clearUpAfterTest() throws Exception {

    /* Clearing all previous rows from tables used in latest test */
    stmt.execute("DELETE FROM META_SCHEDULINGS");
    stmt.execute("DELETE FROM META_COLLECTIONS");
    stmt.execute("DELETE FROM META_TRANSFER_ACTIONS");
    stmt.execute("DELETE FROM META_COLLECTION_SETS");
    objUnderTest.etlrepRockFactory = null;
    objUnderTest.dwhrepRockFactory = null;
    objUnderTest = null;
  }

  /**
   * Test if the execution() method sets all the necessary dbconnection
   * properties - installSets() method will be called but it will not execute
   * any changes to Meta_collection_sets because no the xml file to be installed
   * is empty.
   */
  @Test
  public void testExecution() throws Exception {

    /*
     * Initializing etl & dwh Rockfactories with null in order to see if
     * execute() method works
     */
    objUnderTest.etlrepRockFactory = null;
    objUnderTest.dwhrepRockFactory = null;
    File setXmlDir = getFile("Unzipped_tptest" + File.separator + "tp3");
    objUnderTest.setSetDirectoryPath(setXmlDir.getPath());
    objUnderTest.execute();
    String expected = objUnderTest.etlrepRockFactory.getDbURL() + ", " + objUnderTest.etlrepRockFactory.getDriverName()
        + ", " + objUnderTest.dwhrepRockFactory.getDbURL() + ", " + objUnderTest.dwhrepRockFactory.getDriverName();
    String actual = "jdbc:hsqldb:mem:testdb, org.hsqldb.jdbcDriver, jdbc:hsqldb:mem:testdb, org.hsqldb.jdbcDriver";
    assertEquals(expected, actual);
  }

  /**
   * Testing if exception is thrown when trying to import data to a table that
   * doesn't exist.
   */
  @Test
  public void testInstallSetsToNotExistingTable() throws Exception {

    File setXmlDir = getFile("Unzipped_tptest" + File.separator + "tp2");
    objUnderTest.setSetDirectoryPath(setXmlDir.getPath());

    try {

      installSets.invoke(objUnderTest, new Object[] {});
      fail("Test Failed - Exception expected as no such table exists as stated in the xml file which is being imported");
    } catch (Exception e) {
      /*
       * Test passed - SQLException thrown because no such table exists as
       * stated in the xml file which is being imported
       */
    }
  }

  /**
   * Testing if exception is thrown when trying to import data from not existing
   * filepath.
   */
  @Test
  public void testInstallSetsToNotExistingInstallPath() throws Exception {

    objUnderTest.setSetDirectoryPath("Notexistingpath");

    try {

      installSets.invoke(objUnderTest, new Object[] {});
      fail("Test Failed - Exception expected as no such path exists");
    } catch (Exception e) {
      /* Test passed - NullpointerException thrown as no such path exists */
    }
  }

  /**
   * Testing generic input with 3 different xml files which include empty
   * columns, doublicate rows and differently enabled flags - Every row should
   * be imported, COLLECTION_SET_ID is auto incremented and will start from 0 in
   * empty table no matter what the actual input in the xml file the set is
   * imported from is.
   */
  @Test
  public void testInstallSets() throws Exception {

    File setXmlDir = getFile("Unzipped_tptest" + File.separator + "tp1");
    objUnderTest.setSetDirectoryPath(setXmlDir.getPath());

    try {

      installSets.invoke(objUnderTest, new Object[] {});
      IDataSet actualDataSet = new DatabaseConnection(con).createDataSet();
      ITable actualTable = actualDataSet.getTable("Meta_collection_sets");
      IDataSet expectedDataSet = new FlatXmlDataSet(
	  getFile("/com.distocraft.dc5000.install.ant_ETLSetInstallationTest_testInstallSets/Expected.xml"));
      ITable expectedTable = expectedDataSet.getTable("Meta_collection_sets");
      Assertion.assertEquals(expectedTable, actualTable);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
	private File getFile(final String name) throws Exception {
		final URL url = ClassLoader.getSystemResource("XMLFiles");
		if(url == null){
			throw new FileNotFoundException("XMLFiles");
		}
		final File xmlBase = new File(url.toURI());
		final String xmlFile = xmlBase.getAbsolutePath() + "/"+name;
		return new File(xmlFile);
	}

  /**
   * Test for creating rockFactory connection.
   */
  @Test
  public void testCreateRockFactory() throws Exception {

    /* Test creating RockFactory with null object - should throw an exception */
    HashMap nullcondetails = new HashMap();

    try {

      createRockFactory.invoke(objUnderTest, new Object[] { nullcondetails });
      fail("Test failed - Exception expected because of null object as parameter");
    } catch (Exception e) {
      /* Test passed - exception thrown because no values initialized */
    }

    /*
     * Test creating RockFactory with generic input and asserting that
     * connection details are the correct
     */
    HashMap condetails = new HashMap();
    condetails.put("DatabaseUsername", "sa");
    condetails.put("DatabasePassword", "");
    condetails.put("DatabaseUrl", "jdbc:hsqldb:mem:testcon");
    condetails.put("DatabaseDriver", "org.hsqldb.jdbcDriver");
    RockFactory rc = (RockFactory) createRockFactory.invoke(objUnderTest, new Object[] { condetails });
    String expected = rc.getUserName() + ", " + rc.getPassword() + ", " + rc.getDbURL() + ", " + rc.getDriverName();
    String actual = "sa, , jdbc:hsqldb:mem:testcon, org.hsqldb.jdbcDriver";
    assertEquals(expected, actual);
  }

  /**
   * Testing disabling given collection set.<br>
   * <br>
   * Testing that the enabled flag is deactivated (from 'Y' to 'N') in 'testset'
   * named set in META_COLLECTION_SETS.
   */
  @Test
  public void testDisableMetacollectionsets() throws Exception {

    /* Creating new data to tables for testing purposes */
    stmt.executeUpdate("INSERT INTO META_COLLECTION_SETS VALUES"
        + "('0', 'testset', 'testing disabling sets', '1.0', 'Y', 'A')");
    disablePreviousMetaCollectionSets.invoke(objUnderTest, new Object[] { "testset" });
    IDataSet actualDataSet = new DatabaseConnection(con).createDataSet();
    ITable actualTable = actualDataSet.getTable("META_COLLECTION_SETS");
    IDataSet expectedDataSet = new FlatXmlDataSet(
	getFile("com.distocraft.dc5000.install.ant_ETLSetInstallationTest_testDisableMethods/Expected.xml"));
    ITable expectedTable = expectedDataSet.getTable("META_COLLECTION_SETS");
    Assertion.assertEquals(expectedTable, actualTable);
  }

  /**
   * Testing disabling given schedule.<br>
   * <br>
   * Testing if the HOLD_FLAG is changed from 'N' to 'Y' when
   * DisableMetaSchedules() is run - schedules are fetched from META_SCHEDULINGS
   * if the COLLECTION_SET_ID field matches with the id in META_COLLECTION_SETS.
   */
  @Test
  public void testDisableMetaschedules() throws Exception {

    /* Creating new data to tables for testing purposes */
    stmt.executeUpdate("INSERT INTO META_COLLECTION_SETS VALUES"
        + "('0', 'testset', 'testing disabling sets', '1.0', 'Y', 'A')");
    stmt.executeUpdate("INSERT INTO META_SCHEDULINGS VALUES"
        + "('1.0', 0, 'weekly', 'oscommand', 1, 1, 1, 1, 0, 1, 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'status',"
        + " '2008-01-01 00:00:00.0', 1, 1, 'testschedule', 'N', 1, 2008, 'triggercommand', 1)");
    disablePreviousSchedules.invoke(objUnderTest, new Object[] { "testset" });
    IDataSet actualDataSet = new DatabaseConnection(con).createDataSet();
    ITable actualTable = actualDataSet.getTable("META_SCHEDULINGS");
    IDataSet expectedDataSet = new FlatXmlDataSet(getFile(
"com.distocraft.dc5000.install.ant_ETLSetImportTest_testDisableMethods/Expected.xml"));
    ITable expectedTable = expectedDataSet.getTable("META_SCHEDULINGS");
    Assertion.assertEquals(expectedTable, actualTable);
  }

  /**
   * Testing disabling previous meta collections.<br>
   * <br>
   * Testing if the ENABLED_FLAG is changed from 'Y' to 'N' when
   * DisableMetaSchedules() is run - collections are fetched from
   * META_COLLECTIONS if the COLLECTION_SET_ID field matches with the id in
   * META_COLLECTION_SETS.
   */
  @Test
  public void testDisablePreviousMetaCollections() throws Exception {

    /* Creating new data to tables for testing purposes */
    stmt.executeUpdate("INSERT INTO META_COLLECTION_SETS VALUES"
        + "('0', 'testset', 'testing disabling sets', '1.0', 'N', 'A')");
    stmt.executeUpdate("INSERT INTO META_COLLECTIONS VALUES"
        + "(0, 'testcollection', 'collection', 'mailerroraddrs', 'mailfailaddrs', 'mailbugaddrs', 1, 1, 1, 'Y',"
        + " 'Y', '2008-01-01 00:00:00.0', '1.0', 0, '0', 1, 1, 'Y', 'A', 'Y', 'meastype', 'Y', 'schedulinginfo')");
    disablePreviousMetaCollections.invoke(objUnderTest, new Object[] { "testset" });
    IDataSet actualDataSet = new DatabaseConnection(con).createDataSet();
    ITable actualTable = actualDataSet.getTable("META_COLLECTIONS");
    IDataSet expectedDataSet = new FlatXmlDataSet(getFile(
"com.distocraft.dc5000.install.ant_ETLSetImportTest_testDisableMethods/Expected.xml"));
    ITable expectedTable = expectedDataSet.getTable("META_COLLECTIONS");
    Assertion.assertEquals(expectedTable, actualTable);
  }

  /**
   * Test for disabling previous actions.<br>
   * <br>
   * Testing if the ENABLED_FLAG is changed from 'Y' to 'N' when
   * DisableMetaSchedules() is run - collections are fetched from
   * META_COLLECTIONS if the COLLECTION_SET_ID field matches with the id in
   * META_COLLECTION_SETS.
   */
  @Test
  public void testDisablePreviousMetaTransferActions() throws Exception {

    /* Creating new data to tables for testing purposes */
    stmt.executeUpdate("INSERT INTO META_COLLECTION_SETS VALUES"
        + "('0', 'testset', 'testing disabling sets', '1.0', 'N', 'A')");
    stmt.executeUpdate("INSERT INTO META_TRANSFER_ACTIONS VALUES"
        + "('1.0', 1, 1, 0, 'actiontype', 'transferactionname', 1, 'description', 'Y', 1, 'whereclause2',"
        + " 'whereclause3', 'actioncontents3', 'actioncontents2', 'actioncontents1', 'whereclause1')");
    disablePreviousMetaTransferActions.invoke(objUnderTest, new Object[] { "testset" });
    IDataSet actualDataSet = new DatabaseConnection(con).createDataSet();
    ITable actualTable = actualDataSet.getTable("META_TRANSFER_ACTIONS");
    IDataSet expectedDataSet = new FlatXmlDataSet(getFile(
        "com.distocraft.dc5000.install.ant_ETLSetInstallationTest_testDisableMethods/Expected.xml"));

    ITable expectedTable = expectedDataSet.getTable("META_TRANSFER_ACTIONS");
    Assertion.assertEquals(expectedTable, actualTable);
  }

  /**
   * Testing set and get methods for directorypaths with generic input and null
   * value.
   */
  @Test
  public void testSetAndGetDirectoryPath() throws Exception {

    /*
     * Test setDirectoryPath() method with null value by checking if the get
     * method returns null
     */
    objUnderTest.setSetDirectoryPath(null);
    assertEquals(null, objUnderTest.getSetDirectoryPath());

    /*
     * Test setter with generic input and see if the return string matches the
     * actual one
     */
    objUnderTest.setSetDirectoryPath("/root/directory/");
    assertEquals("/root/directory/", objUnderTest.getSetDirectoryPath());
  }
}
