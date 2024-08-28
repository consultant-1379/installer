package com.distocraft.dc5000.install.ant;

import com.distocraft.dc5000.common.StaticProperties;
import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.ericsson.eniq.common.testutilities.DatabaseTestUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.dbunit.Assertion;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ssc.rockfactory.RockFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author ejarsok
 */
public class ActivateInterfaceTest {
  private static final Map<String, String> connProps = new HashMap<String, String>();

  private static RockFactory rockFact;

  private ActivateInterface testInstance = null;
  private Connection con = null;

  private Field sqlQueryField;
  private Logger log = Logger.getAnonymousLogger();
  private Statement stmt = null;
  private ResultSet rs = null;


  @BeforeClass
  public static void init() throws Exception {
    StaticProperties.giveProperties(new Properties());
    rockFact = DatabaseTestUtils.getTestDbConnection();
    //DatabaseTestUtils.loadSetup(rockFact, "ActivateInterface");
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    DatabaseTestUtils.close(rockFact);
  }

  @Before
  public void before() throws Exception {
    connProps.put("etlrepDatabaseUrl", DatabaseTestUtils.getTestDbUrl());
    connProps.put("etlrepDatabaseUsername", DatabaseTestUtils.getTestDbUser());
    connProps.put("etlrepDatabasePassword", DatabaseTestUtils.getTestDbPassword());
    connProps.put("etlrepDatabaseDriver", DatabaseTestUtils.getTestDbDriver());
    testInstance = new ActivateInterface() {
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

    testInstance.createEtlrepRockFactory(connProps, testInstance.getClass().getSimpleName());
    final Field f1 = testInstance.getClass().getSuperclass().getDeclaredField("dwhrepRockFactory");
    f1.setAccessible(true);
    f1.set(testInstance, rockFact);
    final Field f2 = testInstance.getClass().getSuperclass().getDeclaredField("etlrepRockFactory");
    f2.setAccessible(true);
    f2.set(testInstance, rockFact);
  }

  @After
  public void after() {
    testInstance = null;
  }

  @Test
  public void testSetAndGetActivatedInterfaceName() {
    testInstance.setActivatedInterfaceName("aiName");
    assertEquals("aiName", testInstance.getActivatedInterfaceName());
  }

  @Test
  public void testSetAndGetOssName() {
    testInstance.setOssName("ossName");
    assertEquals("ossName", testInstance.getOssName());
  }

  @Test
  public void testSetAndGetConfigurationDirectory() {
    testInstance.setConfigurationDirectory("confDir");
    assertEquals("confDir" + File.separator, testInstance.getConfigurationDirectory());
  }

  @Test
  public void testSetAndGetBinDirectory() {
    testInstance.setBinDirectory("binDir");
    assertEquals("binDir", testInstance.getBinDirectory());
  }

  @Test
  public void testSetAndGetOnlyActivateInterface() {
    testInstance.setOnlyActivateInterface("only_ai");
    assertEquals("only_ai", testInstance.getOnlyActivateInterface());
  }

  private Method getMethod(final String method, final ActivateInterface instance, final Class<?>... inputTypes) throws Exception {
    final Method m = instance.getClass().getSuperclass().getDeclaredMethod(method, inputTypes);
    m.setAccessible(true);
    return m;
  }

  @Test
  public void testActivateInterface() throws Exception {
    testInstance.setActivatedInterfaceName("if_name");
    final Method activateInterface = getMethod("activateInterface", testInstance);
    Boolean b = (Boolean) activateInterface.invoke(testInstance);
    assertEquals(true, b);

    IDataSet actualDataSet = new DatabaseConnection(rockFact.getConnection()).createDataSet();
    ITable actualTable = actualDataSet.getTable("Interfacemeasurement");
    final URL url = ClassLoader.getSystemResource("XMLFiles");
    if (url == null) {
      throw new FileNotFoundException("XMLFiles");
    }
    final File xmlBase = new File(url.toURI());
    final String xmlFile = xmlBase.getAbsolutePath() + "/com.distocraft.dc5000.install.ant_ActivateInterface_testActivateInterface/Expected.xml";
    IDataSet expectedDataSet = new FlatXmlDataSet(new File(xmlFile));
    ITable expectedTable = expectedDataSet.getTable("Interfacemeasurement");
    ITable filteredTable = DefaultColumnFilter.includedColumnsTable(actualTable, expectedTable.getTableMetaData()
        .getColumns());
    Assertion.assertEquals(expectedTable, filteredTable);
  }

  @Test
  public void testGetNewCollectionSetId() throws Exception {
    final Method getNewCollectionSetId = getMethod("getNewCollectionSetId", testInstance);
    Long l = (Long) getNewCollectionSetId.invoke(testInstance);
    assertEquals("CollectionSetId", (Long) 3l, l);
  }

  @Test
  public void testGetNewCollectionId() throws Exception {
    final Method getNewCollectionId = getMethod("getNewCollectionId", testInstance);
    Long l = (Long) getNewCollectionId.invoke(testInstance);
    assertEquals("CollectionId", (Long) 3l, l);
  }

  @Test
  public void testGetNewTransferActionId() throws Exception {
    final Method getNewTransferActionId = getMethod("getNewTransferActionId", testInstance);
    Long l = (Long) getNewTransferActionId.invoke(testInstance);
    assertEquals("TransferActionId", (Long) 2l, l);

  }

  @Test
  public void testDirectoryCheckerSetExists() throws Exception {
    testInstance.setActivatedInterfaceName("aiName");
    testInstance.setOssName("ossName");
    Boolean b = testInstance.directoryCheckerSetExists();
    assertTrue("true expected", b);
  }

  @Test
  public void testGetNewMetaSchedulingsId() throws Exception {
    final Method getNewMetaSchedulingsId = getMethod("getNewMetaSchedulingsId", testInstance);
    Long l = (Long) getNewMetaSchedulingsId.invoke(testInstance);
    assertEquals("SchedulingsId", (Long) 2l, l);
  }

  @Test
  public void testInterfaceAlreadyActivated() throws Exception {
    testInstance.setActivatedInterfaceName("aiName");
    testInstance.setOssName("ossName");
    final Method interfaceAlreadyActivated = getMethod("interfaceAlreadyActivated", testInstance);
    Boolean b = (Boolean) interfaceAlreadyActivated.invoke(testInstance);
    assertTrue("true expected", b);
  }

  @Test
  public void testInterfaceAlreadyActivated2() throws Exception {
    testInstance.setActivatedInterfaceName("foo");
    testInstance.setOssName("bar");
    final Method interfaceAlreadyActivated = getMethod("interfaceAlreadyActivated", testInstance);
    Boolean b = (Boolean) interfaceAlreadyActivated.invoke(testInstance);
    assertFalse("false expected", b);
  }
  
  @Test
  public void testCopyInterfaceSet() throws Exception {
    final String iName = "copyaiName";
    final String osName = "ossi";
    final String csname = iName+"-"+osName;
    testInstance.setActivatedInterfaceName(iName);
    testInstance.setOssName(osName);
    final DatabaseConnection dc = new DatabaseConnection(rockFact.getConnection());

    try{
      final Method copyInterfaceSet = getMethod("copyInterfaceSet", testInstance, ITransferEngineRMI.class);
      copyInterfaceSet.invoke(testInstance, new Object[]{null});

      final ITable mcs = dc.createQueryTable("Meta_collection_sets", "select * from Meta_collection_sets where COLLECTION_SET_NAME = '" + csname+"'");
      assertNotNull("No new Meta_collection_sets found", mcs);
      assertEquals("Wrong number of new Meta_collection_sets found", 1, mcs.getRowCount());
      final BigDecimal COLLECTION_SET_ID = new BigDecimal(mcs.getValue(0, "COLLECTION_SET_ID").toString());
      assertEquals("New Meta_collection_sets.COLLECTION_ID is wrong", new BigDecimal(3), COLLECTION_SET_ID);

      final ITable mc = dc.createQueryTable("Meta_collections", "select * from Meta_collections where COLLECTION_SET_ID = " + COLLECTION_SET_ID);
      assertNotNull("No new Meta_collections found", mc);
      assertEquals("Wrong number of new Meta_collections found", 1, mc.getRowCount());
      final BigDecimal COLLECTION_ID = new BigDecimal(mc.getValue(0, "COLLECTION_ID").toString());
      assertEquals("New Meta_collections.COLLECTION_ID is wrong", new BigDecimal(3), COLLECTION_ID);

      final ITable ms = dc.createQueryTable("Meta_schedulings", "select * from Meta_schedulings " +
        "where COLLECTION_SET_ID = "+COLLECTION_SET_ID+" and COLLECTION_ID = " + COLLECTION_ID);
      assertNotNull("No new Meta_schedulings found", ms);
      assertEquals("Wrong number of new Meta_schedulings found", 1, ms.getRowCount());



      final ITable mta = dc.createQueryTable("Meta_transfer_actions", "select * from Meta_transfer_actions " +
        "where COLLECTION_SET_ID = "+COLLECTION_SET_ID+" and COLLECTION_ID = " + COLLECTION_ID);
      assertNotNull("No new Meta_transfer_actions found", mta);
      assertEquals("Wrong number of new Meta_transfer_actions found", 1, mta.getRowCount());
      assertEquals("New Meta_transfer_actions.TRANSFER_ACTION_NAME is wrong", "TA_nameossi", mta.getValue(0, "TRANSFER_ACTION_NAME"));
    } finally {
      final Statement stm = rockFact.getConnection().createStatement();
      stm.execute("DELETE FROM META_COLLECTION_SETS WHERE COLLECTION_SET_ID=3");
      stm.execute("DELETE FROM META_TRANSFER_ACTIONS WHERE TRANSFER_ACTION_NAME='TA_nameossi'");
      stm.execute("DELETE FROM META_COLLECTIONS WHERE COLLECTION_SET_ID=3 and COLLECTION_ID=3");
      stm.execute("DELETE FROM META_SCHEDULINGS WHERE COLLECTION_SET_ID=3 and COLLECTION_ID=3");
      stm.close();
    }
  }
  @Test
  public void testcloseConn() {
		try {
			String query = "SELECT CURRENT_DATE AS today, CURRENT_TIME AS now FROM (VALUES(0))";
			sqlQueryField = ActivateInterface.class.getDeclaredField("sqlQuery");
			sqlQueryField.setAccessible(true);
			sqlQueryField.set(testInstance, query);
			Method getNewCollectionSetId = ActivateInterface.class.getDeclaredMethod("getNewCollectionSetId",new Class[] {});
			getNewCollectionSetId.setAccessible(true);
			getNewCollectionSetId.invoke(testInstance, new Object[] {});			
			Method getNewCollectionId = ActivateInterface.class.getDeclaredMethod("getNewCollectionId", new Class[] {});
			getNewCollectionId.setAccessible(true);
			getNewCollectionId.invoke(testInstance, new Object[] {});
			Method getNewTransferActionId = ActivateInterface.class.getDeclaredMethod("getNewTransferActionId",new Class[] {});
			getNewTransferActionId.setAccessible(true);
			getNewTransferActionId.invoke(testInstance,new Object[] {});
			Method getNewMetaSchedulingsId = ActivateInterface.class.getDeclaredMethod("getNewMetaSchedulingsId",new Class[] {});
			getNewMetaSchedulingsId.setAccessible(true);
			getNewMetaSchedulingsId.invoke(testInstance, new Object[] {});
		} catch (Exception e) {
			e.printStackTrace();
		}
  }

	@Test
	public void testextractedFinallyBlock1() {
		log.log(Level.INFO, "Positive test case for extractedFinallyBlock ");
		try {
			mCall();			
			Method extractedFinallyBlock = ActivateInterface.class.getDeclaredMethod("closeConn",
					Connection.class, Statement.class, ResultSet.class);
			extractedFinallyBlock.setAccessible(true);
			extractedFinallyBlock.invoke(testInstance, con, stmt, rs);

		} catch (Exception e) {
			log.log(Level.INFO, "Error closing connections under positive test extractedFinallyBlock() ");
		}
	}
	
	@Test
	public void testextractedFinallyBlock2() {
		log.log(Level.INFO, "Negative test case for extractedFinallyBlock ");
		try {
			mCall();			
			Method extractedFinallyBlock = ActivateInterface.class.getDeclaredMethod("closeConn",
					Connection.class, Statement.class, ResultSet.class);
			extractedFinallyBlock.setAccessible(true);
			extractedFinallyBlock.invoke(testInstance, null, null, null);

		} catch (Exception e) {
			log.log(Level.INFO, "Error closing connections under positive test extractedFinallyBlock() ");
		}
	}
	
	public void mCall() {
		log.log(Level.INFO, "Creating connection under mCall()");
		final String driver = "org.hsqldb.jdbcDriver";
		final String dburl = "jdbc:hsqldb:mem:testdb";
		final String dbusr = "SA";
		final String dbpwd = "";
		try {
			con = DriverManager.getConnection(dburl, dbusr, dbpwd);
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT CURRENT_DATE AS today, CURRENT_TIME AS now FROM (VALUES(0))");
		} catch (Exception e) {
			log.log(Level.INFO, "Error closing connections under mCall() ",e.getMessage());
		}
	}
	
}
