package com.distocraft.dc5000.install.ant;

import static org.junit.Assert.*;
import com.ericsson.eniq.common.testutilities.DatabaseTestUtils;
import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.common.StaticProperties;

/**
 * @author ejarsok
 */
public class IntervalDSTModifierTest {
  private static RockFactory rockFac = null;
  private static final File TMP = new File(System.getProperty("java.io.tmpdir"));

  @BeforeClass
  public static void init() throws Exception {
    StaticProperties.giveProperties(new Properties());

    rockFac = DatabaseTestUtils.getTestDbConnection();
    try {

      final Statement stm = rockFac.getConnection().createStatement();
      stm.execute("CREATE TABLE Meta_schedulings (VERSION_NUMBER VARCHAR(20), ID BIGINT, "
          + "EXECUTION_TYPE VARCHAR(20), OS_COMMAND VARCHAR(20), SCHEDULING_MONTH BIGINT, "
          + "SCHEDULING_DAY BIGINT, SCHEDULING_HOUR BIGINT, SCHEDULING_MIN BIGINT, "
          + "COLLECTION_SET_ID BIGINT, COLLECTION_ID BIGINT, MON_FLAG VARCHAR(20), "
          + "TUE_FLAG VARCHAR(20), WED_FLAG VARCHAR(20), THU_FLAG VARCHAR(20), "
          + "FRI_FLAG VARCHAR(20), SAT_FLAG VARCHAR(20), SUN_FLAG VARCHAR(20), "
          + "STATUS VARCHAR(20), LAST_EXECUTION_TIME TIMESTAMP, INTERVAL_HOUR BIGINT, "
          + "INTERVAL_MIN BIGINT, NAME VARCHAR(20),HOLD_FLAG VARCHAR(20), PRIORITY BIGINT, "
          + "SCHEDULING_YEAR BIGINT, TRIGGER_COMMAND VARCHAR(20), LAST_EXEC_TIME_MS BIGINT)");
      stm.executeUpdate("INSERT INTO Meta_schedulings VALUES('1', 1, 'interval', 'os_c', 1, 1, "
          + "1, 1, 1, 1, 'y', 'y', 'y','y', 'y', 'y', 'y', 'ok', '2008-09-10 01:01:01.0', 10, 10, "
          + "'Meta_schedulings', 'y', 1, 2008, 't_co', 1)");
      stm.close();

    } catch (SQLException e1) {
      e1.printStackTrace();
      fail("init() failed, SQLException");
    }
  }

  @Test
  public void testExecute() {
    final Map<String, String> connProps = new HashMap<String, String>();
    connProps.put("etlrepDatabaseUrl", DatabaseTestUtils.getTestDbUrl());
    connProps.put("etlrepDatabaseUsername", DatabaseTestUtils.getTestDbUser());
    connProps.put("etlrepDatabasePassword", DatabaseTestUtils.getTestDbPassword());
    connProps.put("etlrepDatabaseDriver", DatabaseTestUtils.getTestDbDriver());
    final IntervalDSTModifier ia = new IntervalDSTModifier() {
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
    System.setProperty("CONF_DIR", TMP.getPath());
    ia.execute();
  }

  @AfterClass
  public static void clean() throws Exception {
    DatabaseTestUtils.close(rockFac);
  }
}
