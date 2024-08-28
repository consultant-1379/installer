package com.distocraft.dc5000.install.ant;

import com.distocraft.dc5000.common.StaticProperties;
import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ssc.rockfactory.RockFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author ejarsok
 */
public class DirectoryCheckerAndDWHMInstallTest {

  private final DirectoryCheckerAndDWHMInstall dcdi = new DirectoryCheckerAndDWHMInstall();

  private static RockFactory rockFact;

  private static Statement stm;

  @BeforeClass
  public static void init() throws Exception {
    StaticProperties.giveProperties(new Properties());
    try {
      Class.forName("org.hsqldb.jdbcDriver");
    } catch (ClassNotFoundException e2) {
      e2.printStackTrace();
      fail("init() failed, ClassNotFoundException");
    }

    Connection c;
    try {

      c = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "");
      stm = c.createStatement();
      stm.execute("CREATE TABLE Meta_collections (COLLECTION_ID BIGINT, "
        + "COLLECTION_NAME VARCHAR(32), COLLECTION VARCHAR(20), MAIL_ERROR_ADDR VARCHAR(20), "
        + "MAIL_FAIL_ADDR VARCHAR(20), MAIL_BUG_ADDR VARCHAR(20), MAX_ERRORS BIGINT, "
        + "MAX_FK_ERRORS BIGINT, MAX_COL_LIMIT_ERRORS BIGINT, CHECK_FK_ERROR_FLAG VARCHAR(20), "
        + "CHECK_COL_LIMITS_FLAG VARCHAR(20), LAST_TRANSFER_DATE TIMESTAMP,"
        + "VERSION_NUMBER VARCHAR(20), COLLECTION_SET_ID BIGINT, USE_BATCH_ID VARCHAR(20), PRIORITY BIGINT,"
        + "QUEUE_TIME_LIMIT BIGINT, ENABLED_FLAG VARCHAR(20), SETTYPE VARCHAR(20), FOLDABLE_FLAG VARCHAR(20),"
        + "MEASTYPE VARCHAR(20), HOLD_FLAG VARCHAR(20), SCHEDULING_INFO VARCHAR(20))");
      stm.executeUpdate("INSERT INTO Meta_collections VALUES(1, 'dirname', 'collection', 'me', 'mf', 'mb' ,"
        + "5, 5, 5, 'y', 'y', '2010-10-10 10:00:00.0', '1.0', 1, '1', 1, 100, 'Y', 'type', 'n', 'mtype', 'y', 'info')");

      stm.executeUpdate("INSERT INTO Meta_collections (COLLECTION_NAME, ENABLED_FLAG, VERSION_NUMBER) " +
        "VALUES('Directory_Checker_DC_E_TEST', 'Y', '((42))')");
      stm.executeUpdate("INSERT INTO Meta_collections (COLLECTION_NAME, ENABLED_FLAG, VERSION_NUMBER) " +
        "VALUES('DWHM_Install_DC_E_TEST', 'Y', '((42))')");

    } catch (SQLException e1) {
      e1.printStackTrace();
      fail("init() failed, SQLException");
    }

    try {
      rockFact = new RockFactory("jdbc:hsqldb:mem:testdb", "SA", "", "org.hsqldb.jdbcDriver", "con", true, -1);
    } catch (Exception e) {
      e.printStackTrace();
      fail("init() failed, xception");
    }
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {

    stm.execute("DROP TABLE Meta_collections");
  }

  /**
   * Test the order the calls to engine are made in.
   */
  @Test
  public void testExecute() {

    final List<String> commandsInOrder = new ArrayList<String>();
    final DirectoryCheckerAndDWHMInstall task = new DirectoryCheckerAndDWHMInstall() {

      @Override
      void startAndWaitSet(final ITransferEngineRMI engineRMI, final String techPack, final String setName) {
        commandsInOrder.add("startAndWaitSet");
      }

      @Override
      void updateTransformation(final ITransferEngineRMI engine, final String techPack) {
        commandsInOrder.add("updateTransformation");
      }

      @Override
      public void reloadProperties(final ITransferEngineRMI engineRMI) {
        commandsInOrder.add("reloadProperties");
      }

      @Override
      public ITransferEngineRMI connectEngine() {
        return null;
      }
    };
    final Project antProject = new Project();
    antProject.setProperty("etlrepDatabaseUrl", "jdbc:hsqldb:mem:testdb");
    antProject.setProperty("etlrepDatabaseUsername", "SA");
    antProject.setProperty("etlrepDatabasePassword", "");
    antProject.setProperty("etlrepDatabaseDriver", "org.hsqldb.jdbcDriver");

    task.setProject(antProject);
    task.setBinDirectory("/eniq/sw/bin/");
    task.setTechPackName("DC_E_TEST");
    task.setTechPackMetadataVersion("3");
    task.setBuildNumber("42");

    task.execute();
    assertEquals("Wrong number of command called!", 4, commandsInOrder.size());
    assertTrue(" not called first!", commandsInOrder.get(0).equals("reloadProperties"));
    assertTrue("Directory_Checker_DC_E_TEST not called in correct order!", commandsInOrder.get(1).equals("startAndWaitSet"));
    assertTrue("DWHM_Install_DC_E_TEST not called in correct order!", commandsInOrder.get(2).equals("startAndWaitSet"));
    assertTrue("updateTransformation not called last!", commandsInOrder.get(3).equals("updateTransformation"));
  }

  @Test
  public void testExecuteErrorMessage_SetFailed() throws RemoteException {
    final JUnit4Mockery context = new JUnit4Mockery();
    final ITransferEngineRMI mockedEngine = context.mock(ITransferEngineRMI.class);

    final DirectoryCheckerAndDWHMInstall task = new DirectoryCheckerAndDWHMInstall() {
      @Override
      public ITransferEngineRMI connectEngine() {
        return mockedEngine;
      }
    };
    context.checking(new Expectations() {{
      oneOf(mockedEngine).reloadProperties();
      oneOf(mockedEngine).executeAndWait("DC_E_TEST", "Directory_Checker_DC_E_TEST", "");
      will(returnValue("failed"));
    }});

    final Project antProject = new Project();
    antProject.setProperty("etlrepDatabaseUrl", "jdbc:hsqldb:mem:testdb");
    antProject.setProperty("etlrepDatabaseUsername", "SA");
    antProject.setProperty("etlrepDatabasePassword", "");
    antProject.setProperty("etlrepDatabaseDriver", "org.hsqldb.jdbcDriver");

    task.setProject(antProject);
    task.setBinDirectory("/eniq/sw/bin/");
    task.setTechPackName("DC_E_TEST");
    task.setTechPackMetadataVersion("3");
    task.setBuildNumber("42");

    try {
      task.execute();
      fail("BuildException should have been thrown");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains("Aborting tech pack installation"));
    } catch (Throwable t) {
      fail("Unexpected exception " + t);
    }
  }

  @Test
  public void testExecuteErrorMessage_TransformerException() throws RemoteException {
    final JUnit4Mockery context = new JUnit4Mockery();
    final ITransferEngineRMI mockedEngine = context.mock(ITransferEngineRMI.class);

    final DirectoryCheckerAndDWHMInstall task = new DirectoryCheckerAndDWHMInstall() {
      @Override
      public ITransferEngineRMI connectEngine() {
        return mockedEngine;
      }
    };
    context.checking(new Expectations() {{
      oneOf(mockedEngine).reloadProperties();
      oneOf(mockedEngine).executeAndWait("DC_E_TEST", "Directory_Checker_DC_E_TEST", "");
      will(returnValue("succeeded"));
      oneOf(mockedEngine).executeAndWait("DC_E_TEST", "DWHM_Install_DC_E_TEST", "");
      will(returnValue("succeeded"));
      oneOf(mockedEngine).updateTransformation("DC_E_TEST");
      will(throwException(new RemoteException("Error while updating transformer cache")));
    }});

    final Project antProject = new Project();
    antProject.setProperty("etlrepDatabaseUrl", "jdbc:hsqldb:mem:testdb");
    antProject.setProperty("etlrepDatabaseUsername", "SA");
    antProject.setProperty("etlrepDatabasePassword", "");
    antProject.setProperty("etlrepDatabaseDriver", "org.hsqldb.jdbcDriver");

    task.setProject(antProject);
    task.setBinDirectory("/eniq/sw/bin/");
    task.setTechPackName("DC_E_TEST");
    task.setTechPackMetadataVersion("3");
    task.setBuildNumber("42");

    try {
      task.execute();
      fail("BuildException should have been thrown");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains("Aborting tech pack installation"));
    } catch (Throwable t) {
      fail("Unexpected exception " + t);
    }
  }


  @Test
  public void testSetAndGetTechPackName() {

    dcdi.setTechPackName("TECH_PACK");
    assertEquals("TECH_PACK", dcdi.getTechPackName());
  }

  @Test
  public void testSetAndGetBinDirectory() {

    dcdi.setBinDirectory("BIN_DIR");
    assertEquals("BIN_DIR", dcdi.getBinDirectory());
  }

  @Test
  public void testSetAndGetTechPackVersion() {

    dcdi.setTechPackVersion("v 1.0");
    assertEquals("v 1.0", dcdi.getTechPackVersion());
  }

  @Test
  public void testSetAndGetInstallingInterface() {

    dcdi.setInstallingInterface("INS_INTER");
    assertEquals("INS_INTER", dcdi.getInstallingInterface());
  }

  @Test
  public void testSetAndGetExitValue() {

    dcdi.setExitValue(10);
    assertEquals((Integer) 10, dcdi.getExitValue());
  }

  @Test
  public void testDirectoryCheckerSetExists() {

    Class secretClass = dcdi.getClass();
    try {

      Field field2 = secretClass.getDeclaredField("directoryCheckerSetName");
      field2.setAccessible(true);
      field2.set(dcdi, "dirname");
      dcdi.setTechPackVersion("1.0");
      assertEquals(true, dcdi.directoryCheckerSetExists(rockFact));
      stm.executeUpdate("UPDATE Meta_collections SET COLLECTION_NAME = 'dwhm'" + "WHERE COLLECTION_ID = 1");
      assertEquals(false, dcdi.directoryCheckerSetExists(rockFact));

    } catch (Exception e) {
      e.printStackTrace();
      fail("testDirectoryCheckerSetExists() failed, Exception");
    }
  }

  @Test
  public void testDwhmInstallSetExists() {

    Class secretClass = dcdi.getClass();
    try {

      Field field2 = secretClass.getDeclaredField("dwhmInstallSetName");
      field2.setAccessible(true);
      field2.set(dcdi, "dwhm");
      dcdi.setTechPackVersion("1.0");
      assertEquals(true, dcdi.dwhmInstallSetExists(rockFact));
      stm.executeUpdate("UPDATE Meta_collections SET COLLECTION_NAME = 'foobar'" + "WHERE COLLECTION_ID = 1");
      assertEquals(false, dcdi.dwhmInstallSetExists(rockFact));

    } catch (Exception e) {
      e.printStackTrace();
      fail("testDirectoryCheckerSetExists() failed, Exception");
    }
  }


}
