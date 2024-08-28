package com.distocraft.dc5000.install.ant;

import com.distocraft.dc5000.etl.scheduler.ISchedulerRMI;
import com.ericsson.eniq.common.testutilities.UnitDatabaseTestCase;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ssc.rockfactory.RockFactory;

public class DeactivateInterfaceTest extends UnitDatabaseTestCase {
  @BeforeClass
  public static void beforeClass() {
    setup(TestType.unit);
  }

  @Test
  public void test_get_set_DeactivatedInterfaceName() throws Exception {
    final DeactivateInterface di = new DeactivateInterface();
    final String iname = "lsdkjflksdjf";
    di.setDeactivatedInterfaceName(iname);
    Assert.assertEquals("DeactivatedInterfaceName not correct ", iname, di.getDeactivatedInterfaceName());
  }

  @Test
  public void test_get_set_OssName() throws Exception {
    final DeactivateInterface di = new DeactivateInterface();
    final String ossname = "dsdsfffffffffff";
    di.setOssName(ossname);
    Assert.assertEquals("OssName not correct ", ossname, di.getOssName());
  }

  @Test
  public void test_get_set_BinDirectory() throws Exception {
    final DeactivateInterface di = new DeactivateInterface();
    final String bindir = "bindir";
    di.setBinDirectory(bindir);
    Assert.assertEquals("BinDirectory not correct ", bindir, di.getBinDirectory());
  }

  @Test
  public void test_get_set_OnlyDeactivateInterface() throws Exception {
    final DeactivateInterface di = new DeactivateInterface();
    final String onlya = "true";
    di.setOnlyDeactivateInterface(onlya);
    Assert.assertEquals("OnlyDeactivateInterface not correct ", onlya, di.getOnlyDeactivateInterface());
  }

  @Test
  public void test_getDatabaseConnectionDetails() throws Exception {
    final DeactivateInterface di = new DeactivateInterface() {
      @Override
      public Map<String, String> getDatabaseConnectionDetails() throws BuildException {
        return super.getDatabaseConnectionDetails();
      }
    };
    final Project project = new Project();
    di.setProject(project);
    final Map<String, String> details = di.getDatabaseConnectionDetails();
    Assert.assertNotNull("null value returned", details);

    final RockFactory etlrep = getRockFactory(Schema.etlrep);
    checkForKey(details, "etlrepDatabaseUrl", etlrep.getDbURL());
    checkForKey(details, "etlrepDatabaseUsername", etlrep.getUserName());
    checkForKey(details, "etlrepDatabasePassword", etlrep.getPassword());
    checkForKey(details, "etlrepDatabaseDriver", etlrep.getDriverName());
  }

  @Test
  public void testDeactivateExecute() throws Exception {

    final JUnit4Mockery context = new JUnit4Mockery();
    final ISchedulerRMI mockedScheduler = context.mock(ISchedulerRMI.class);

    final DeactivateInterface di = new DeactivateInterface(){
      @Override
      public ISchedulerRMI connectScheduler() {
        return mockedScheduler;
      }
    };
    context.checking(new Expectations() {{
      oneOf(mockedScheduler).reload();
    }});

    final Project project = new Project();
    di.setProject(project);

    final String diName = "test_interface";
    final String ossName = "oss_1";
    final String setName = diName + "-" + ossName;
    final long collectionSetId = 12;
    final long collectionId = 5689;

    final RockFactory etlrep = getRockFactory(Schema.etlrep);
    Statement stmt = etlrep.getConnection().createStatement();

    stmt.executeUpdate("insert into META_COLLECTION_SETS (" +
      "COLLECTION_SET_ID, COLLECTION_SET_NAME, DESCRIPTION, VERSION_NUMBER, ENABLED_FLAG, TYPE) values (" +
      collectionSetId + ", '" + setName + "', '', '((102))', 'Y', 'Interface');");

    stmt.executeUpdate("insert into META_COLLECTIONS (" +
      "COLLECTION_ID, COLLECTION_NAME, COLLECTION, MAIL_ERROR_ADDR, MAIL_FAIL_ADDR, MAIL_BUG_ADDR, MAX_ERRORS, MAX_FK_ERRORS, MAX_COL_LIMIT_ERRORS, CHECK_FK_ERROR_FLAG, CHECK_COL_LIMITS_FLAG, LAST_TRANSFER_DATE, VERSION_NUMBER, COLLECTION_SET_ID, USE_BATCH_ID, PRIORITY, QUEUE_TIME_LIMIT, ENABLED_FLAG, SETTYPE, FOLDABLE_FLAG, MEASTYPE, HOLD_FLAG, SCHEDULING_INFO) values (" +
      collectionId + ", 'Adapter_INTF_TEST_mdc', null, null, null, null, 0, 0, 0, 'N', 'N', null, '((102))', " + collectionSetId + ", null, 0, 30, 'Y', 'Adapter', 'Y', null, 'N', null);");

    stmt.executeUpdate("insert into META_TRANSFER_ACTIONS (" +
      "VERSION_NUMBER, TRANSFER_ACTION_ID, COLLECTION_ID, COLLECTION_SET_ID, ACTION_TYPE, TRANSFER_ACTION_NAME, ORDER_BY_NO, DESCRIPTION, ENABLED_FLAG, CONNECTION_ID) values (" +
      "'((102))', 123, " + collectionId + ", " + collectionSetId + ", 'Parse', 'mdc', 1, '', 'Y', 2);");

    stmt.executeUpdate("insert into META_SCHEDULINGS (" +
      "VERSION_NUMBER, ID, EXECUTION_TYPE, COLLECTION_SET_ID, COLLECTION_ID, PRIORITY, NAME) values ('" +
      "((102))', 5366, 'interval', " + collectionSetId + ", " + collectionId + ", null, 'test-name');");

    stmt.close();

    di.setDeactivatedInterfaceName(diName);
    di.setOssName(ossName);
    di.execute();

    context.assertIsSatisfied();
    assertTableEmpty("META_COLLECTION_SETS");
    assertTableEmpty("META_COLLECTIONS");
    assertTableEmpty("META_TRANSFER_ACTIONS");
    assertTableEmpty("META_SCHEDULINGS");
  }

  private void assertTableEmpty(final String tableName) throws SQLException {
    final Statement stmt = getRockFactory(Schema.etlrep).getConnection().createStatement();
    ResultSet rs = null;
    try {
      rs = stmt.executeQuery("SELECT COUNT(*) AS CC FROM " + tableName);
      rs.next();
      final int cc = rs.getInt("CC");
      Assert.assertEquals("Table '" + tableName + "' is not empty", 0, cc);
    } catch (SQLException e) {
      if ("42501".equals(e.getSQLState())) {
        Assert.fail("Table '" + tableName + "' not found.");
      } else {
        throw e;
      }
    } finally {
      if(rs != null){
        try{rs.close();} catch (Throwable t){/**/}
      }
      if(stmt != null){
        try{stmt.close();} catch (Throwable t){/**/}
      }
    }
  }

  private void checkForKey(final Map<String, String> details, final String key, final String value) {
    Assert.assertTrue("Key '" + key + "' not found in connection details", details.containsKey(key));
    Assert.assertEquals("Key '" + key + "' value incorrect ", value, details.get(key));
  }
}
