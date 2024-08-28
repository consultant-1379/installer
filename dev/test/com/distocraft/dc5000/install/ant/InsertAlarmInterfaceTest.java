package com.distocraft.dc5000.install.ant;

import com.ericsson.eniq.common.testutilities.DatabaseTestUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.dbunit.Assertion;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ssc.rockfactory.RockFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import com.distocraft.dc5000.common.StaticProperties;

/**
 * @author ejarsok
 */

public class InsertAlarmInterfaceTest {

  private static RockFactory rockFact;

  private static Method insertInterface;

  @BeforeClass
  public static void init() throws Exception {
    StaticProperties.giveProperties(new Properties());
    rockFact = DatabaseTestUtils.getTestDbConnection();
    final Statement stm = rockFact.getConnection().createStatement();

    stm.execute("CREATE TABLE Meta_databases (USERNAME VARCHAR(31), VERSION_NUMBER VARCHAR(31), "
        + "TYPE_NAME VARCHAR(31), CONNECTION_ID VARCHAR(31), CONNECTION_NAME VARCHAR(31), "
        + "CONNECTION_STRING VARCHAR(31), PASSWORD VARCHAR(31), DESCRIPTION VARCHAR(31), DRIVER_NAME VARCHAR(31), "
        + "DB_LINK_NAME VARCHAR(31))");

    stm.executeUpdate("INSERT INTO Meta_databases VALUES('SA', '1', 'USER', '1', 'dwhrep', "
        + "'jdbc:hsqldb:mem:testdb', '', 'description', 'org.hsqldb.jdbcDriver', 'dblinkname')");

    stm.execute("CREATE TABLE Alarminterface (INTERFACEID VARCHAR(20), DESCRIPTION VARCHAR(20),"
        + "STATUS VARCHAR(20), COLLECTION_SET_ID BIGINT, COLLECTION_ID BIGINT, QUEUE_NUMBER BIGINT)");

    stm.execute("CREATE TABLE Meta_collection_sets (COLLECTION_SET_ID VARCHAR(20), COLLECTION_SET_NAME VARCHAR(20),"
        + "DESCRIPTION VARCHAR(20),VERSION_NUMBER VARCHAR(20),ENABLED_FLAG VARCHAR(20),TYPE VARCHAR(20))");

    stm
        .executeUpdate("INSERT INTO Meta_collection_sets VALUES('1', 'AlarmInterfaces', 'description', '1', 'Y', 'type')");

    stm.execute("CREATE TABLE Meta_collections (COLLECTION_ID BIGINT, COLLECTION_NAME VARCHAR(20),"
        + "COLLECTION VARCHAR(20), MAIL_ERROR_ADDR VARCHAR(20), MAIL_FAIL_ADDR VARCHAR(20), MAIL_BUG_ADDR VARCHAR(20),"
        + "MAX_ERRORS BIGINT, MAX_FK_ERRORS BIGINT, MAX_COL_LIMIT_ERRORS BIGINT,"
        + "CHECK_FK_ERROR_FLAG VARCHAR(20), CHECK_COL_LIMITS_FLAG VARCHAR(20), LAST_TRANSFER_DATE TIMESTAMP,"
        + "VERSION_NUMBER VARCHAR(20), COLLECTION_SET_ID BIGINT, USE_BATCH_ID VARCHAR(20), PRIORITY BIGINT,"
        + "QUEUE_TIME_LIMIT BIGINT, ENABLED_FLAG VARCHAR(20), SETTYPE VARCHAR(20), FOLDABLE_FLAG VARCHAR(20),"
        + "MEASTYPE VARCHAR(20), HOLD_FLAG VARCHAR(20), SCHEDULING_INFO VARCHAR(20))");

    stm.executeUpdate("INSERT INTO Meta_collections VALUES('1', 'Adapter_ID', 'collection', 'me', 'mf', 'mb' ,"
        + "5, 5, 5, 'y', 'y', '2010-10-10 10:00:00.0', '10', 1, '1', 1, 100, 'Y', 'type', 'n', 'mtype', 'y', 'info')");
    stm.executeUpdate("INSERT INTO Meta_collections VALUES('1', 'Adapter_id', 'collection', 'me', 'mf', 'mb' ,"
        + "5, 5, 5, 'y', 'y', '2010-10-10 10:00:00.0', '10', 1, '1', 1, 100, 'Y', 'type', 'n', 'mtype', 'y', 'info')");

    final InsertAlarmInterface tmp = new InsertAlarmInterface();
    final Class secretClass = tmp.getClass();

    insertInterface = secretClass.getDeclaredMethod("insertInterface");
    insertInterface.setAccessible(true);

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    DatabaseTestUtils.close(rockFact);
  }

  @Test
  public void testSetAndGetDescriptionString() {
    InsertAlarmInterface ia = new InsertAlarmInterface();
    ia.setDescription("Descript");
    assertEquals("Descript", ia.getDescription());
  }

  @Test
  public void testSetAndGetInterfaceId() {
    InsertAlarmInterface ia = new InsertAlarmInterface();
    ia.setInterfaceId("iID");
    assertEquals("iID", ia.getInterfaceId());
  }

  @Test
  public void testSetAndGetQueueNumber() {
    InsertAlarmInterface ia = new InsertAlarmInterface();
    ia.setQueueNumber("qnumber");
    assertEquals("qnumber", ia.getQueueNumber());
  }

  @Test
  public void testSetAndGetStatus() {
    InsertAlarmInterface ia = new InsertAlarmInterface();
    ia.setStatus("status");
    assertEquals("status", ia.getStatus());
  }

  @Test
  public void testSetAndGetConfigurationDirectory() {
    InsertAlarmInterface ia = new InsertAlarmInterface();
    ia.setConfigurationDirectory("confDir");
    assertEquals("confDir" + File.separator, ia.getConfigurationDirectory());
  }

  private Field getField(final String name, final InsertAlarmInterface instance) throws Exception {
    final Field f = instance.getClass().getDeclaredField(name);
    f.setAccessible(true);
    return f;
  }

  @Test
  public void testInsertInterface() throws Exception {
    final InsertAlarmInterface ia = new InsertAlarmInterface();
    getField("dwhrepRockFactory", ia).set(ia, rockFact);
    getField("etlrepRockFactory", ia).set(ia, rockFact);
    try {
      ia.setInterfaceId("ID");
      ia.setDescription("DESCRIPTION");
      ia.setStatus("STATUS");
      ia.setQueueNumber("100");

      insertInterface.invoke(ia);

      ITable actualTable = new DatabaseConnection(rockFact.getConnection()).createQueryTable("RESULT_NAME",
          "SELECT * FROM Alarminterface WHERE INTERFACEID = 'ID'");

      IDataSet expectedDataSet = new FlatXmlDataSet(
          getFile("com.distocraft.dc5000.install.ant_InsertAlarmInterface_testInsertInterface/Expected.xml"));
      ITable expectedTable = expectedDataSet.getTable("Alarminterface");

      Assertion.assertEquals(expectedTable, actualTable);

    } catch (Exception e) {
      e.printStackTrace();
      fail("testInsertInterface() failed");
    }
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

  @Test
  public void testExecute() {
    final Map<String, String> connProps = new HashMap<String, String>();
    connProps.put("etlrepDatabaseUrl", DatabaseTestUtils.getTestDbUrl());
    connProps.put("etlrepDatabaseUsername", DatabaseTestUtils.getTestDbUser());
    connProps.put("etlrepDatabasePassword", DatabaseTestUtils.getTestDbPassword());
    connProps.put("etlrepDatabaseDriver", DatabaseTestUtils.getTestDbDriver());
    final InsertAlarmInterface ia = new InsertAlarmInterface() {
      @Override
      protected Map<String, String> getDatabaseConnectionDetails() throws BuildException {
        return connProps;
      }
    };
    final Project p = new Project() {
      @Override
      public String getProperty(final String propertyName) {
        return connProps.get(propertyName);
      }
    };
    ia.setProject(p);
    ia.setInterfaceId("id");
    ia.setDescription("descript");
    ia.setStatus("stat");
    ia.setQueueNumber("10");
    ia.execute();

    try {
      ITable actualTable = new DatabaseConnection(rockFact.getConnection()).createQueryTable("RESULT_NAME",
          "SELECT * FROM Alarminterface WHERE INTERFACEID = 'id'");

      IDataSet expectedDataSet = new FlatXmlDataSet(
          getFile("com.distocraft.dc5000.install.ant_InsertAlarmInterface_testExecute/Expected.xml"));
      ITable expectedTable = expectedDataSet.getTable("Alarminterface");

      Assertion.assertEquals(expectedTable, actualTable);

    } catch (Exception e) {
      e.printStackTrace();
      fail("testExecute() failed");
    }
  }
}
