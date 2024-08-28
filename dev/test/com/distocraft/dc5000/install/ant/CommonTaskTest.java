package com.distocraft.dc5000.install.ant;

import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.engine.system.SetListener;
import com.distocraft.dc5000.etl.scheduler.ISchedulerRMI;
import com.ericsson.eniq.common.Constants;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author etogust
 */
public class CommonTaskTest {
  private final JUnit4Mockery context = new JUnit4Mockery();
  private ITransferEngineRMI mockedEngine = null;
  private ISchedulerRMI mockedScheduler = null;
  private final DirectoryCheckerAndDWHMInstall taskMockedEngine = new DirectoryCheckerAndDWHMInstall() {
    @Override
    public ITransferEngineRMI connectEngine() {
      return mockedEngine;
    }

    @Override
    public ISchedulerRMI connectScheduler() {
      return mockedScheduler;
    }
  };

  private final String testTechPack = "DC_E_TEST";
  private final String testSetName = "DC_E_TEST_SET";

  @Before
  public void before() {
    mockedEngine = context.mock(ITransferEngineRMI.class);
    mockedScheduler = context.mock(ISchedulerRMI.class);
  }

  @After
  public void after() {
    mockedEngine = null;
    mockedScheduler = null;
  }

  @Test
  public void testgetDatabaseConnectionDetails() throws IOException {
    File tmpPropFile = new File("ETLCServer.properties");
    tmpPropFile.createNewFile();
    tmpPropFile.deleteOnExit();

    PrintWriter pw = new PrintWriter(tmpPropFile);
    pw.println("ENGINE_DB_URL = " + "url");
    pw.println("ENGINE_DB_USERNAME = " + "user");
    pw.println("ENGINE_DB_PASSWORD = " + "pass");
    pw.println("ENGINE_DB_DRIVERNAME = " + "driver");

    pw.close();

    String path = tmpPropFile.getAbsolutePath();
    path = path.substring(0, path.lastIndexOf(File.separator));
    System.setProperty(Constants.DC_CONFIG_DIR_PROPERTY_NAME, path);

    CommonTask ct = new CommonTask();
    Project pr = new Project();
    ct.setProject(pr);

    Map<String, String> hm = ct.getDatabaseConnectionDetails();

    Set<String> hmKeys = hm.keySet();
    if (hmKeys == null || hmKeys.size() == 0) {
      fail("Nothing was got from properties");
    }

    if (!"url".equals(pr.getProperty("etlrepDatabaseUrl"))) {
      fail("Projectproperties not set correctly");
    }
    if (!"user".equals(pr.getProperty("etlrepDatabaseUsername"))) {
      fail("Projectproperties not set correctly");
    }
    if (!"pass".equals(pr.getProperty("etlrepDatabasePassword"))) {
      fail("Projectproperties not set correctly");
    }
    if (!"driver".equals(pr.getProperty("etlrepDatabaseDriver"))) {
      fail("Projectproperties not set correctly");
    }

  }

  @Test
  public void test_startAndWaitSet_NullStatus() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).executeAndWait(testTechPack, testSetName, "");
      will(returnValue(null));
    }});

    try {
      taskMockedEngine.startAndWaitSet(mockedEngine, testTechPack, testSetName);
      fail("BuildException should have been thrown!");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains("Aborting tech pack installation"));
    }
  }

  @Test
  public void test_startAndWaitSet_DROPPED() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).executeAndWait(testTechPack, testSetName, "");
      will(returnValue(SetListener.DROPPED));
    }});

    try {
      taskMockedEngine.startAndWaitSet(mockedEngine, testTechPack, testSetName);
      fail("BuildException should have been thrown!");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains(testSetName + " has been dropped from priorityqueue"));
    }
  }

  @Test
  public void test_startAndWaitSet_UNKNOWN() throws RemoteException {
    final String status = "Yar!";
    context.checking(new Expectations() {{
      oneOf(mockedEngine).executeAndWait(testTechPack, testSetName, "");
      will(returnValue(status));
    }});

    try {
      taskMockedEngine.startAndWaitSet(mockedEngine, testTechPack, testSetName);
      fail("BuildException should have been thrown!");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains("Status{" + status + "}"));
    }
  }

  @Test
  public void test_startAndWaitSet_NOSET() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).executeAndWait(testTechPack, testSetName, "");
      will(returnValue(SetListener.NOSET));
    }});

    try {
      taskMockedEngine.startAndWaitSet(mockedEngine, testTechPack, testSetName);
    } catch (BuildException e) {
      fail("No BuildException should have been thrown : " + e);
    }
  }

  @Test
  public void test_startAndWaitSet_SUCCEEDED() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).executeAndWait(testTechPack, testSetName, "");
      will(returnValue(SetListener.SUCCEEDED));
    }});

    try {
      taskMockedEngine.startAndWaitSet(mockedEngine, testTechPack, testSetName);
    } catch (BuildException e) {
      fail("No BuildException should have been thrown : " + e);
    }
  }

  @Test
  public void test_startAndWaitSet_RemoteException() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).executeAndWait(testTechPack, testSetName, "");
      will(throwException(new RemoteException("Oops!")));
    }});
    try {
      taskMockedEngine.startAndWaitSet(mockedEngine, testTechPack, testSetName);
      fail("BuildException should have been thrown!");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains("Aborting tech pack installation"));
    }
  }

  @Test
  public void test_startAndWaitSet_ConnectException() throws RemoteException {
    final RemoteException error = new RemoteException("RE-Oops!", new ConnectException("CE-Oops!"));
    context.checking(new Expectations() {{
      oneOf(mockedEngine).executeAndWait(testTechPack, testSetName, "");
      will(throwException(error));
    }});
    try {
      taskMockedEngine.startAndWaitSet(mockedEngine, testTechPack, testSetName);
      fail("BuildException should have been thrown!");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains("Aborting tech pack installation"));
    }
  }

  @Test
  public void test_startAndWaitSet_Exception() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).executeAndWait(testTechPack, testSetName, "");
      will(throwException(new NullPointerException("Oops!")));
    }});
    try {
      taskMockedEngine.startAndWaitSet(mockedEngine, testTechPack, testSetName);
      fail("BuildException should have been thrown!");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains("Aborting tech pack installation"));
    }
  }

  @Test
  public void test_reloadProperties() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).reloadProperties();
    }});
    taskMockedEngine.reloadProperties(mockedEngine);
  }

  @Test
  public void test_reloadProperties_Error() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).reloadProperties();
      will(throwException(new RemoteException("Oops!")));
    }});
    try {
      taskMockedEngine.reloadProperties(mockedEngine);
      fail("BuildException should have been thrown!");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains("Aborting tech pack installation"));
    }
  }

  @Test
  public void test_updateTransformation() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).updateTransformation(testTechPack);
    }});
    try {
      taskMockedEngine.updateTransformation(mockedEngine, testTechPack);
    } catch (BuildException e) {
      fail("No BuildException should have been thrown : " + e);
    }
  }

  @Test
  public void test_errorMessage_updateTransformationFails() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).updateTransformation(testTechPack);
      will(throwException(new RemoteException("Error while updating transformer cache")));
    }});
    try {
      taskMockedEngine.updateTransformation(mockedEngine, testTechPack);
      fail("BuildException should have been thrown!");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains("Aborting tech pack installation"));
    }
  }

  @Test
  public void test_reloadLogging() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).reloadLogging();
    }});
    try {
      taskMockedEngine.reloadLogging(mockedEngine);
      context.assertIsSatisfied();
    } catch (BuildException e) {
      fail("No BuildException should have been thrown : " + e);
    }
  }

  @Test
  public void test_errorMesage_reloadLoggingFails() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).reloadLogging();
      will(throwException(new RemoteException("Oops!")));
    }});
    try {
      taskMockedEngine.reloadLogging(mockedEngine);
      fail("BuildException should have been thrown!");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains("Aborting tech pack installation"));
    }
  }

  @Test
  public void test_restore() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).isTechPackEnabled("DC_E_TEST", "ENIQ_EVENT");
      will(returnValue(true));
      oneOf(mockedEngine).getMeasurementTypesForRestore("DC_E_TEST", "DC_E_TEST_TEST");
      will(returnValue(Arrays.asList("DC_E_TEST_TEST")));
      oneOf(mockedEngine).restore("DC_E_TEST", Arrays.asList("DC_E_TEST_TEST"), "2012-02-01", "abc");
    }});
    try {
      taskMockedEngine.restore(mockedEngine, "DC_E_TEST", "DC_E_TEST_TEST", "2012-02-01", "abc");
      context.assertIsSatisfied();
    } catch (BuildException e) {
      fail("No BuildException should have been thrown : " + e);
    }
  }

  @Test
  public void test_restore_TechpackDisabled() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).isTechPackEnabled("DC_E_TEST", "ENIQ_EVENT");
      will(returnValue(false));
    }});
    try {
      taskMockedEngine.restore(mockedEngine, "DC_E_TEST", "DC_E_TEST_TEST", "2012-02-01", "abc");
      context.assertIsSatisfied();
    } catch (BuildException e) {
      fail("No BuildException should have been thrown : " + e);
    }
  }

  @Test
  public void test_errorMesage_restoreFails() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedEngine).isTechPackEnabled("DC_E_TEST", "ENIQ_EVENT");
      will(returnValue(true));
      oneOf(mockedEngine).getMeasurementTypesForRestore("DC_E_TEST", "DC_E_TEST_TEST");
      will(throwException(new RemoteException("Oops!")));
    }});
    try {
      taskMockedEngine.restore(mockedEngine, "DC_E_TEST", "DC_E_TEST_TEST", "2012-02-01", "abc");
      fail("BuildException should have been thrown!");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains("Aborting tech pack installation"));
    }
  }

  @Test
  public void test_activateScheduler() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedScheduler).reload();
    }});
    try {
      taskMockedEngine.activateScheduler(mockedScheduler);
      context.assertIsSatisfied();
    } catch (BuildException e) {
      fail("No BuildException should have been thrown : " + e);
    }
  }

  @Test
  public void test_errorMesage_activateSchedulerFails() throws RemoteException {
    context.checking(new Expectations() {{
      oneOf(mockedScheduler).reload();
      will(throwException(new RemoteException("Oops!")));
    }});
    try {
      taskMockedEngine.activateScheduler(mockedScheduler);
      fail("BuildException should have been thrown!");
    } catch (BuildException e) {
      assertTrue(e.getMessage().contains("Aborting tech pack installation"));
    }
  }
}
