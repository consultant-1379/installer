package com.distocraft.dc5000.install.ant;

import com.distocraft.dc5000.common.StaticProperties;
import com.ericsson.eniq.common.testutilities.DatabaseTestUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ssc.rockfactory.RockFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
import com.distocraft.dc5000.common.StaticProperties;
 * @author ejarsok
 */

public class UpdateAlarmInterfaceTest {

  private static final Map<String, String> connProps = new HashMap<String, String>();
  private UpdateAlarmInterface testInstance = null;

  private static RockFactory rockFact;

  @BeforeClass
  public static void init() throws Exception {
    StaticProperties.giveProperties(new Properties());
    rockFact = DatabaseTestUtils.getTestDbConnection();
    DatabaseTestUtils.loadSetup(rockFact, "UpdateAlarmInterface");
  }

  @Before
  public void before() {
    connProps.put("etlrepDatabaseUrl", DatabaseTestUtils.getTestDbUrl());
    connProps.put("etlrepDatabaseUsername", DatabaseTestUtils.getTestDbUser());
    connProps.put("etlrepDatabasePassword", DatabaseTestUtils.getTestDbPassword());
    connProps.put("etlrepDatabaseDriver", DatabaseTestUtils.getTestDbDriver());
    testInstance = new UpdateAlarmInterface(){
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
    testInstance.setProject(p);
  }

  @After
  public void after() {
    testInstance = null;
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    DatabaseTestUtils.close(rockFact);
  }

  @Test
  public void testCreateEtlrepRockFactory() throws Exception {
    final RockFactory rf = testInstance.createEtlrepRockFactory(connProps, getClass().getSimpleName());
    final String expected = "SA,,jdbc:hsqldb:mem:testdb,org.hsqldb.jdbcDriver";
    final String actual = rf.getUserName() + "," + rf.getPassword() + "," + rf.getDbURL() + "," + rf.getDriverName();
    assertEquals(expected, actual);
  }

  /**
   * Test method updates row values in Alarminterface table where interfaceId =
   * 1
   */

  @Test
  public void testUpdateInterface() {
    UpdateAlarmInterface instance = new UpdateAlarmInterface();
    Class secretClass = instance.getClass();

    try {
      Method method = secretClass.getDeclaredMethod("updateInterface", null);
      method.setAccessible(true);

      instance.dwhrepRockFactory = rockFact;
      instance.etlrepRockFactory = rockFact;
      instance.setInterfaceId("1");
      instance.setQueueNumber("1");
      instance.setDescription("descript");
      instance.setStatus("status_is_set");

      assertEquals(true, method.invoke(instance, null));

      ITable actualTable = new DatabaseConnection(rockFact.getConnection()).createQueryTable("RESULT_NAME",
          "SELECT * FROM Alarminterface WHERE INTERFACEID = '1'");

      final URL url = ClassLoader.getSystemResource("XMLFiles");
      if (url == null) {
        throw new FileNotFoundException("XMLFiles");
      }
      final File xmlBase = new File(url.toURI());
      final String xmlFile = xmlBase.getAbsolutePath() + "/com.distocraft.dc5000.install.ant_UpdateAlarmInterface_testUpdateInterface/Expected.xml";

      IDataSet expectedDataSet = new FlatXmlDataSet(new File(xmlFile));
      ITable expectedTable = expectedDataSet.getTable("Alarminterface");

      Assertion.assertEquals(expectedTable, actualTable);

    } catch (Exception e) {
      e.printStackTrace();
      fail("testCreateEtlrepRockFactory() failed, Exception");
    }
  }

  @Test
  public void testSetAndGetDescriptionString() {
    testInstance.setDescription("Descript");
    assertEquals("Descript", testInstance.getDescription());
  }

  @Test
  public void testSetAndGetInterfaceId() {
    testInstance.setInterfaceId("Interface_id");
    assertEquals("Interface_id", testInstance.getInterfaceId());
  }

  @Test
  public void testSetAndGetQueueNumber() {
    testInstance.setQueueNumber("QNumber");
    assertEquals("QNumber", testInstance.getQueueNumber());
  }

  @Test
  public void testSetAndGetStatus() {
    testInstance.setStatus("Status");
    assertEquals("Status", testInstance.getStatus());
  }

  @Test
  public void testSetAndGetConfigurationDirectory() {
    testInstance.setConfigurationDirectory("conf_dir" + File.separator);
    assertEquals("conf_dir" + File.separator, testInstance.getConfigurationDirectory());
  }

  @Test
  public void testExecute() throws Exception {

    testInstance.setInterfaceId("2");
    testInstance.setQueueNumber("1");
    testInstance.setDescription("descript2");
    testInstance.setStatus("status_is_set2");
    testInstance.execute();

    final ITable actualTable = new DatabaseConnection(rockFact.getConnection()).createQueryTable("RESULT_NAME",
        "SELECT * FROM Alarminterface WHERE INTERFACEID = '2'");

    final URL url = ClassLoader.getSystemResource("XMLFiles");
    if (url == null) {
      throw new FileNotFoundException("XMLFiles");
    }
    final File xmlBase = new File(url.toURI());
    final String xmlFile = xmlBase.getAbsolutePath() + "/com.distocraft.dc5000.install.ant_UpdateAlarmInterface_testExecute/Expected.xml";


    final IDataSet expectedDataSet = new FlatXmlDataSet(new File(xmlFile));
    final ITable expectedTable = expectedDataSet.getTable("Alarminterface");
    Assertion.assertEquals(expectedTable, actualTable);
  }

}
