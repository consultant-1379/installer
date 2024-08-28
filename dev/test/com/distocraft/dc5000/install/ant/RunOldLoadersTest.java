/*------------------------------------------------------------------------
 *
 *
 *      COPYRIGHT (C)                   ERICSSON RADIO SYSTEMS AB, Sweden
 *
 *      The  copyright  to  the document(s) herein  is  the property of
 *      Ericsson Radio Systems AB, Sweden.
 *
 *      The document(s) may be used  and/or copied only with the written
 *      permission from Ericsson Radio Systems AB  or in accordance with
 *      the terms  and conditions  stipulated in the  agreement/contract
 *      under which the document(s) have been supplied.
 *
 *------------------------------------------------------------------------
 */
package com.distocraft.dc5000.install.ant;

import static com.ericsson.eniq.common.testutilities.RockDatabaseHelper.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.distocraft.dc5000.common.StaticProperties;
import com.distocraft.dc5000.etl.engine.common.EngineCom;
import com.distocraft.dc5000.etl.engine.main.EngineThread;
import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.engine.main.TransferEngine;
import com.distocraft.dc5000.etl.engine.priorityqueue.PersistenceHandler;
import com.distocraft.dc5000.etl.engine.priorityqueue.PriorityQueue;
import com.ericsson.eniq.common.testutilities.BaseUnitTestX;
import com.ericsson.eniq.common.testutilities.RockDatabaseHelper;

/**
 * This class is run during the installation of an Events Techpack If theres a
 * schema change then this will find if theres any loader files to load into the
 * database. It will wait until all the counter and counter_day sets have run.
 * 
 * @author edeclyd
 * 
 */
public class RunOldLoadersTest extends BaseUnitTestX {

  private static Map<String, String> env = System.getenv();

  private static final String USER_HOME = env.get("WORKSPACE");

  private static final String events_etldata_directory = USER_HOME + File.separator + "eventsETLData";

  private static final String CONFIG_DIRECTORY_PROPERTY_NAME = "dc5000.config.directory";

  private final String eniqConfDirectory = USER_HOME + File.separator + "eniqConf";

  private final String techpackName = "EVENT_E_SGEH";

  private RunOldLoaders rolc = null;

  private static Statement etlrep_stmt;

  private static Statement dwhrep_stmt;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    StaticProperties.giveProperties(new Properties());
    RockDatabaseHelper.setUpBeforeClass();

    dwhrep_stmt = dwhRepConnection.createStatement();

    dwhrep_stmt
        .execute("create table tpactivation (techpack_name varchar(255), VersionID varchar(255),  Type varchar(255))");
    dwhrep_stmt
        .executeUpdate("insert into tpactivation (techpack_name, VersionID, Type) values ('EVENT_E_SGEH', '5', 'ENIQ_EVENT')");

    dwhrep_stmt.execute("create table ReferenceTable (VersionID varchar(255), TypeName varchar(255))");
    dwhrep_stmt
        .executeUpdate("insert into ReferenceTable (VersionID, TypeName) values ('DIM_E_SGEH((19))', 'DIM_E_SGEH_EVENTRESULT')");
    dwhrep_stmt
        .executeUpdate("insert into ReferenceTable (VersionID, TypeName) values ('DIM_E_SGEH((19))', 'DIM_E_SGEH_SGSN_HIER3')");
    dwhrep_stmt
        .executeUpdate("insert into ReferenceTable (VersionID, TypeName) values ('DIM_E_SGEH((19))', 'DIM_E_SGEH_RAT')");
    dwhrep_stmt
        .executeUpdate("insert into ReferenceTable (VersionID, TypeName) values ('DIM_E_SGEH((19))', 'DIM_E_SGEH_TAC')");
    dwhrep_stmt
        .executeUpdate("insert into ReferenceTable (VersionID, TypeName) values ('DIM_E_SGEH((19))', 'DIM_E_SGEH_GGSN')");

    dwhrep_stmt.execute("create table MeasurementType (VersionID varchar(255), FolderName varchar(255))");
    dwhrep_stmt
        .executeUpdate("insert into MeasurementType (VersionID, FolderName) values ('EVENT_E_SGEH:((10))', 'EVENT_E_SGEH_VEND_HIER3_SUC')");
    dwhrep_stmt
        .executeUpdate("insert into MeasurementType (VersionID, FolderName) values ('EVENT_E_SGEH:((10))', 'EVENT_E_SGEH_APN_CC_SCC_SUC')");
    dwhrep_stmt
        .executeUpdate("insert into MeasurementType (VersionID, FolderName) values ('EVENT_E_SGEH:((10))', 'EVENT_E_SGEH_EVNTSRC_CC_ERR')");
    dwhrep_stmt
        .executeUpdate("insert into MeasurementType (VersionID, FolderName) values ('EVENT_E_SGEH:((10))', 'EVENT_E_SGEH_EVNTSRC_CC_SUC')");
    dwhrep_stmt
        .executeUpdate("insert into MeasurementType (VersionID, FolderName) values ('EVENT_E_SGEH:((10))', 'EVENT_E_SGEH_EVNTSRC_ERR')");
    dwhrep_stmt
        .executeUpdate("insert into MeasurementType (VersionID, FolderName) values ('EVENT_E_SGEH:((11))', 'EVENT_E_SGEH_VEND_HIER3_SUC')");
    dwhrep_stmt
        .executeUpdate("insert into MeasurementType (VersionID, FolderName) values ('EVENT_E_SGEH:((11))', 'EVENT_E_SGEH_APN_CC_SCC_SUC')");
    dwhrep_stmt
        .executeUpdate("insert into MeasurementType (VersionID, FolderName) values ('EVENT_E_SGEH:((11))', 'EVENT_E_SGEH_EVNTSRC_CC_ERR')");
    dwhrep_stmt
        .executeUpdate("insert into MeasurementType (VersionID, FolderName) values ('EVENT_E_SGEH:((11))', 'EVENT_E_SGEH_EVNTSRC_CC_SUC')");
    dwhrep_stmt
        .executeUpdate("insert into MeasurementType (VersionID, FolderName) values ('EVENT_E_SGEH:((11))', 'EVENT_E_SGEH_EVNTSRC_ERR')");

    etlrep_stmt = etlRepConnection.createStatement();
    etlrep_stmt
        .execute("create table Meta_Collections (Collection_Set_ID numeric(2), Collection_Name varchar(128), Enabled_Flag varchar(1))");
    etlrep_stmt
        .executeUpdate("insert into Meta_Collections (Collection_Set_ID, Collection_Name, Enabled_Flag) values (10, 'Loader_EVENT_E_SGEH_ERR', 'Y')");
    etlrep_stmt
        .executeUpdate("insert into Meta_Collections (Collection_Set_ID, Collection_Name, Enabled_Flag) values (10, 'Loader_EVENT_E_SGEH_SUC', 'Y')");
    etlrep_stmt
        .executeUpdate("insert into Meta_Collections (Collection_Set_ID, Collection_Name, Enabled_Flag) values (11, 'Loader_EVENT_E_SGEH_ERR', 'N')");
    etlrep_stmt
        .executeUpdate("insert into Meta_Collections (Collection_Set_ID, Collection_Name, Enabled_Flag) values (11, 'Loader_EVENT_E_SGEH_SUC', 'N')");

    etlrep_stmt
        .execute("create table Meta_Collection_Sets (Collection_Set_ID numeric(2), Collection_Set_Name varchar(128), Enabled_Flag varchar(1))");
    etlrep_stmt
        .executeUpdate("insert into Meta_Collection_Sets (Collection_Set_ID, Collection_Set_Name, Enabled_Flag) values (8, 'EVENT_E_SGEH', 'N')");
    etlrep_stmt
        .executeUpdate("insert into Meta_Collection_Sets (Collection_Set_ID, Collection_Set_Name, Enabled_Flag) values (9, 'EVENT_E_SGEH', 'N')");
    etlrep_stmt
        .executeUpdate("insert into Meta_Collection_Sets (Collection_Set_ID, Collection_Set_Name, Enabled_Flag) values (10, 'EVENT_E_SGEH', 'Y')");
    etlrep_stmt
        .executeUpdate("insert into Meta_Collection_Sets (Collection_Set_ID, Collection_Set_Name, Enabled_Flag) values (5, 'DIM_E_GRAN', 'N')");
    etlrep_stmt
        .executeUpdate("insert into Meta_Collection_Sets (Collection_Set_ID, Collection_Set_Name, Enabled_Flag) values (6, 'DIM_E_GRAN', 'Y')");

    etlrep_stmt
        .execute("create table Meta_Transfer_Actions (Collection_ID numeric(4), transfer_action_name varchar(32), where_clause_01 varchar(300), Enabled_Flag varchar(1))");
    etlrep_stmt
        .executeUpdate("insert into Meta_Transfer_Actions (Collection_ID, transfer_action_name, where_clause_01, Enabled_Flag) values (8, 'Loader_EVENT_E_SGEH_ERR', '#"
            + "\n#Fri May 14 15:51:45 BST 2010"
            + "\ndateformat=yyyy-MM-dd"
            + "\ntaildir=raw"
            + "\ntechpack=EVENT_E_SGEH" + "\ntablename=EVENT_E_SGEH_ERR" + "\nversiondir=10" + "\n', 'Y')");
    etlrep_stmt
        .executeUpdate("insert into Meta_Transfer_Actions (Collection_ID, transfer_action_name, where_clause_01, Enabled_Flag) values (9, 'Loader_EVENT_E_SGEH_ERR', '#"
            + "\n#Fri May 14 15:51:45 BST 2010"
            + "\ndateformat=yyyy-MM-dd"
            + "\ntaildir=raw"
            + "\ntechpack=EVENT_E_SGEH" + "\ntablename=EVENT_E_SGEH_ERR" + "\nversiondir=11" + "\n', 'Y')");
    etlrep_stmt
        .executeUpdate("insert into Meta_Transfer_Actions (Collection_ID, transfer_action_name, where_clause_01, Enabled_Flag) values (10, 'Loader_EVENT_E_SGEH_ERR', '#"
            + "\n#Fri May 14 15:51:45 BST 2010"
            + "\ndateformat=yyyy-MM-dd"
            + "\ntaildir=raw"
            + "\ntechpack=EVENT_E_SGEH" + "\ntablename=EVENT_E_SGEH_ERR" + "\nversiondir=12" + "\n', 'Y')");
    etlrep_stmt
        .executeUpdate("insert into Meta_Transfer_Actions (Collection_ID, transfer_action_name, where_clause_01, Enabled_Flag) values (5, 'Loader_EVENT_E_SGEH_ERR', '#"
            + "\n#Fri May 14 15:51:45 BST 2010"
            + "\ndateformat=yyyy-MM-dd"
            + "\ntaildir=raw"
            + "\ntechpack=EVENT_E_SGEH" + "\ntablename=EVENT_E_SGEH_ERR" + "\nversiondir=13" + "\n', 'Y')");
    etlrep_stmt
        .executeUpdate("insert into Meta_Transfer_Actions (Collection_ID, transfer_action_name, where_clause_01, Enabled_Flag) values (6, 'Loader_EVENT_E_SGEH_ERR', '#"
            + "\n#Fri May 14 15:51:45 BST 2010"
            + "\ndateformat=yyyy-MM-dd"
            + "\ntaildir=raw"
            + "\ntechpack=EVENT_E_SGEH" + "\ntablename=EVENT_E_SGEH_ERR" + "\nversiondir=14" + "\n', 'Y')");
  }

  @Before
  public void setupPropertyFiles() throws IOException {
    System.setProperty(CONFIG_DIRECTORY_PROPERTY_NAME, eniqConfDirectory);
    createEmptyFile(eniqConfDirectory, "ETLCServer.properties");
    File propertiesFile = createEmptyFile(eniqConfDirectory, "niq.rc");
    addPropertyToFile("EVENTS_ETLDATA_DIR", events_etldata_directory, propertiesFile);
    new File(events_etldata_directory).mkdirs();

  }

  @AfterClass
  public static void tearDownAfterClass() {
    try {
      etlrep_stmt.execute("DROP TABLE Meta_Collection_Sets");
      etlrep_stmt.execute("DROP TABLE Meta_Collections");
      etlrep_stmt.execute("DROP TABLE Meta_Transfer_Actions");
      dwhrep_stmt.execute("DROP TABLE MeasurementType");
      dwhrep_stmt.execute("DROP TABLE ReferenceTable");
      dwhrep_stmt.execute("DROP TABLE tpactivation");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void areSetsInQueueTest() {
    rolc = new RunOldLoaders(this.techpackName, etlrepRock, dwhrepRock);
    Class<? extends RunOldLoaders> rolcClass = rolc.getClass();
    String methodAreSetsInQueue = "areSetsInQueue";

    String setType = "Loader";

    PriorityQueue pq = null;
    TransferEngine engine = null;

    try {

      // creating the engine for the test
      engine = new TransferEngine(true, true, 5, Logger.getLogger("Test"));

      // setting up the priority queue
      StaticProperties.giveProperties(new Properties());
      pq = new PriorityQueue(1000, 15, new MyPersHandler(), null);

    } catch (RemoteException e2) {
      e2.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // setting up the tranfer engine
    Class<? extends TransferEngine> engClass = engine.getClass();

    // adding the engine threads to the priority queue
    for (int i = 0; i < 6; i++) {
      final EngineThread engineSet = new EngineThread(setType, Long.valueOf(5L), Logger.getLogger("Test"),
          new EngineCom());
      pq.addSet(engineSet);
    }

    Iterator<EngineThread> iterator = pq.getAvailable();

    try {

      final Field engURL = engClass.getDeclaredField("etlrep_url");
      engURL.setAccessible(true);
      engURL.set(engClass, "//localhost:1200/TransferEngine");

      final Field engUserName = engClass.getDeclaredField("userName");
      engUserName.setAccessible(true);
      engUserName.set(engClass, "sa");

      final Field engPassword = engClass.getDeclaredField("password");
      engPassword.setAccessible(true);
      engPassword.set(engClass, "");

      final Field engDbDriverName = engClass.getDeclaredField("dbDriverName");
      engDbDriverName.setAccessible(true);
      engDbDriverName.set(engClass, "jdbc:hsqldb:mem:testdb");

      // setting up variables in the engine
      final Field engPriorityQueue = engClass.getDeclaredField("priorityQueue");
      engPriorityQueue.setAccessible(true);
      engPriorityQueue.set(engClass, pq);

      Method engMethodInit = engClass.getDeclaredMethod("init", new Class[] {});
      engMethodInit.setAccessible(true);
      engMethodInit.invoke(engClass, new Object[] {});

      ITransferEngineRMI engineActual = (ITransferEngineRMI) Naming.lookup("//localhost:1200/TransferEngine");

      Method method = rolcClass.getDeclaredMethod(methodAreSetsInQueue, new Class[] { String.class,
          ITransferEngineRMI.class });
      method.setAccessible(true);
      Boolean setsInQueueActual = (Boolean) method.invoke(rolc, new Object[] { setType, engineActual });

      assertEquals(true, setsInQueueActual);

    } catch (IllegalArgumentException e) {
    } catch (IllegalAccessException e) {
    } catch (InvocationTargetException e) {
    } catch (final SecurityException e1) {
      e1.printStackTrace();
    } catch (final NoSuchFieldException e1) {
      e1.printStackTrace();
    } catch (final Exception e) {
      e.printStackTrace();
      fail("areSetsExecutingTest() failed, Exception");
    }
  }

  @Test
  public void areSetsExecutingTest() {
    rolc = new RunOldLoaders(this.techpackName, etlrepRock, dwhrepRock);
    Class<? extends RunOldLoaders> rolcClass = rolc.getClass();
    String methodAreSetsExecutingTest = "areSetsExecuting";

    String setType = "Loader";

    PriorityQueue pq = null;
    TransferEngine engine = null;

    try {

      // creating the engine for the test
      engine = new TransferEngine(true, true, 5, Logger.getLogger("Test"));

      // setting up the priority queue
      StaticProperties.giveProperties(new Properties());
      pq = new PriorityQueue(1000, 15, new MyPersHandler(), null);

    } catch (RemoteException e2) {
      e2.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // setting up the transfer engine
    Class<? extends TransferEngine> engClass = engine.getClass();

    // adding the engine threads to the priority queue
    for (int i = 0; i < 6; i++) {
      final EngineThread engineSet = new EngineThread(setType, Long.valueOf(5L), Logger.getLogger("Test"),
          new EngineCom());
      pq.addSet(engineSet);
    }

    try {

      final Field engURL = engClass.getDeclaredField("etlrep_url");
      engURL.setAccessible(true);
      engURL.set(engClass, "//localhost:1200/TransferEngine");

      final Field engUserName = engClass.getDeclaredField("userName");
      engUserName.setAccessible(true);
      engUserName.set(engClass, "sa");

      final Field engPassword = engClass.getDeclaredField("password");
      engPassword.setAccessible(true);
      engPassword.set(engClass, "");

      final Field engDbDriverName = engClass.getDeclaredField("dbDriverName");
      engDbDriverName.setAccessible(true);
      engDbDriverName.set(engClass, "jdbc:hsqldb:mem:testdb");

      // setting up variables in the engine
      final Field engPriorityQueue = engClass.getDeclaredField("priorityQueue");
      engPriorityQueue.setAccessible(true);
      engPriorityQueue.set(engClass, pq);

      Method engMethodInit = engClass.getDeclaredMethod("init", new Class[] {});
      engMethodInit.setAccessible(true);
      engMethodInit.invoke(engClass, new Object[] {});

      ITransferEngineRMI engineActual = (ITransferEngineRMI) Naming.lookup("//localhost:1200/TransferEngine");

      Method method = rolcClass.getDeclaredMethod(methodAreSetsExecutingTest, new Class[] { String.class,
          ITransferEngineRMI.class });
      method.setAccessible(true);

      Boolean setsRunningActual = (Boolean) method.invoke(rolc, new Object[] { setType, engineActual });

      assertEquals(true, setsRunningActual);

    } catch (IllegalArgumentException e) {
    } catch (IllegalAccessException e) {
    } catch (InvocationTargetException e) {
    } catch (final SecurityException e1) {
      e1.printStackTrace();
    } catch (final NoSuchFieldException e1) {
      e1.printStackTrace();
    } catch (final Exception e) {
      e.printStackTrace();
      fail("areSetsExecutingTest() failed, Exception");
    }
  }

  @Test
  public void connectTest() {
    rolc = new RunOldLoaders(this.techpackName, etlrepRock, dwhrepRock);
    Class<? extends RunOldLoaders> rolcClass = rolc.getClass();
    String methodConnect = "connect";

    StringBuilder actualConnectionProperties = new StringBuilder();
    String expectedConnectionProperties = "localhost, 1200, TransferEngine";

    try {
      Method method = rolcClass.getDeclaredMethod(methodConnect, new Class[] {});
      method.setAccessible(true);

      final Field serverHostName = rolcClass.getDeclaredField("serverHostName");
      serverHostName.setAccessible(true);
      serverHostName.set(rolc, "localhost");

      final Field serverPort = rolcClass.getDeclaredField("serverPort");
      serverPort.setAccessible(true);
      serverPort.set(rolc, "1200");

      final Field serverRefName = rolcClass.getDeclaredField("serverRefName");
      serverRefName.setAccessible(true);
      serverRefName.set(rolc, "TransferEngine");

      ITransferEngineRMI engine = (ITransferEngineRMI) method.invoke(rolc, new Object[] {});

    } catch (IllegalArgumentException e) {
    } catch (IllegalAccessException e) {
    } catch (InvocationTargetException e) {
    } catch (final SecurityException e1) {
      e1.printStackTrace();
    } catch (final NoSuchFieldException e1) {
      e1.printStackTrace();
    } catch (final Exception e) {
      e.printStackTrace();
      fail("connectTest() failed, Exception");
    }
  }

  @Test
  public void setEngineConnectionPropertiesNoPropertiesSet() throws Exception {
    rolc = new RunOldLoaders(this.techpackName, etlrepRock, dwhrepRock);
    Class<? extends RunOldLoaders> rolcClass = rolc.getClass();
    String methodSetEngineConnectionProperties = "setEngineConnectionProperties";

    StringBuilder actualConnectionProperties = new StringBuilder();
    String expectedConnectionProperties = "";
 
    expectedConnectionProperties = "localhost" + ", 1200, TransferEngine";

    Method method = rolcClass.getDeclaredMethod(methodSetEngineConnectionProperties, new Class[] {});
    method.setAccessible(true);
    method.invoke(rolc, new Object[] {});

    final Field serverHostName = rolcClass.getDeclaredField("serverHostName");
    serverHostName.setAccessible(true);
    actualConnectionProperties.append(serverHostName.get(rolc) + ", ");

    final Field serverPort = rolcClass.getDeclaredField("serverPort");
    serverPort.setAccessible(true);
    actualConnectionProperties.append(serverPort.get(rolc) + ", ");

    final Field serverRefName = rolcClass.getDeclaredField("serverRefName");
    serverRefName.setAccessible(true);
    actualConnectionProperties.append(serverRefName.get(rolc));
    assertEquals(expectedConnectionProperties, actualConnectionProperties.toString());
  }
   
  private File createEmptyFile(String localEniqConfigDirectory, String fileName) throws IOException {
    File directory = new File(localEniqConfigDirectory);
    directory.mkdirs();
    directory.deleteOnExit();
    File etlcServerPropertiesFile = new File(directory, fileName);
    etlcServerPropertiesFile.createNewFile();
    etlcServerPropertiesFile.deleteOnExit();
    return etlcServerPropertiesFile;
  }

  @Test
  public void getLoaderSets() throws Exception {
    rolc = new RunOldLoaders(this.techpackName, etlrepRock, dwhrepRock);
    Class<? extends RunOldLoaders> rolcClass = rolc.getClass();
    String methodGetLoaderSets = "getLoaderSets";

    Method method = rolcClass.getDeclaredMethod(methodGetLoaderSets, new Class[] {String.class});
    method.setAccessible(true);
    List<String> loaderSets = (List<String>) method.invoke(rolc, new Object[] {" Select Collection_Name From Meta_Collections "
        + " Where Collection_name like '%Loader_%' And Enabled_Flag = 'Y' And "
        + "Collection_Set_ID  =  (    Select Collection_Set_ID  From Meta_Collection_Sets "
        + " Where Collection_Set_Name  = '" + techpackName + "' " + " And Enabled_Flag = 'Y')"});

    StringBuilder expectedLoaderSets = new StringBuilder();
    expectedLoaderSets.append("Loader_EVENT_E_SGEH_ERR");
    expectedLoaderSets.append(", ");
    expectedLoaderSets.append("Loader_EVENT_E_SGEH_SUC");
    expectedLoaderSets.append(", ");

    Iterator<String> iterator = loaderSets.iterator();

    StringBuilder actualLoaderSets = new StringBuilder();

    while (iterator.hasNext()) {
      actualLoaderSets.append(iterator.next());
      actualLoaderSets.append(", ");
    }

    assertEquals(expectedLoaderSets.toString(), actualLoaderSets.toString());

  }

  @Test
  public void isTopologyTechpackTestTrue() {
    rolc =new RunOldLoaders(this.techpackName, etlrepRock, dwhrepRock);
    Class<? extends RunOldLoaders> rolcClass = rolc.getClass();
    String methodIsTopologyTechpack = "isTopologyTechpack";

    try {
      final Field techpackName = rolcClass.getDeclaredField("techpackName");
      final Field topologyTechpack = rolcClass.getDeclaredField("topologyTechpack");

      techpackName.setAccessible(true);
      techpackName.set(rolc, "DIM_E_SGEH");

      topologyTechpack.setAccessible(true);

      Method method = rolcClass.getDeclaredMethod(methodIsTopologyTechpack, new Class[] {});
      method.setAccessible(true);
      method.invoke(rolc, new Object[] {});

      topologyTechpack.setAccessible(true);

      assertEquals(true, topologyTechpack.getBoolean(rolc));

    } catch (final SecurityException e1) {
      e1.printStackTrace();
    } catch (final NoSuchFieldException e1) {
      e1.printStackTrace();
    } catch (final Exception e) {
      e.printStackTrace();
      fail("isTopologyTechpackTestTrue() failed, Exception");
    }
  }

  @Test
  public void isTopologyTechpackTestFalse() {
    rolc = new RunOldLoaders(this.techpackName, etlrepRock, dwhrepRock);
    Class<? extends RunOldLoaders> rolcClass = rolc.getClass();
    String methodIsTopologyTechpack = "isTopologyTechpack";

    try {
      final Field topologyTechpack = rolcClass.getDeclaredField("topologyTechpack");
      topologyTechpack.setAccessible(true);

      Method method = rolcClass.getDeclaredMethod(methodIsTopologyTechpack, new Class[] {});
      method.setAccessible(true);
      method.invoke(rolc, new Object[] {});

      assertEquals(false, topologyTechpack.getBoolean(rolc));

    } catch (final SecurityException e1) {
      e1.printStackTrace();
    } catch (final NoSuchFieldException e1) {
      e1.printStackTrace();
    } catch (final Exception e) {
      e.printStackTrace();
      fail("isTopologyTechpackTestFalse() failed, Exception");
    }
  }
  
  private void addPropertyToFile(String propertyName, String propertyValue, File propertiesFile) throws IOException {
    Properties properties = new Properties();
    properties.put(propertyName, propertyValue);
    properties.store(new FileOutputStream(propertiesFile), "properties for testing");
  }

  public class MyPersHandler extends PersistenceHandler {

    public MyPersHandler() {
    }

    @Override
    public void newSet(final EngineThread et) {
    }

    @Override
    public void droppedSet(final EngineThread et) {
    }

    @Override
    public void executedSet(final EngineThread et) {
    }

    public List<EngineThread> getSets() throws Exception {

      final List<EngineThread> sets = new ArrayList<EngineThread>();
      return sets;
    }
  };
}

