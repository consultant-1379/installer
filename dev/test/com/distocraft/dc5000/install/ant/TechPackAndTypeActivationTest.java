package com.distocraft.dc5000.install.ant;

import com.distocraft.dc5000.repository.dwhrep.Tpactivation;
import com.distocraft.dc5000.repository.dwhrep.TpactivationFactory;
import com.distocraft.dc5000.repository.dwhrep.Typeactivation;
import com.distocraft.dc5000.repository.dwhrep.TypeactivationFactory;
import com.ericsson.eniq.common.testutilities.DatabaseTestUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TechPackAndTypeActivationTest {
  private TechPackAndTypeActivation testInstance = null;
  private RockFactory testDb = null;
  private static final String TPNAME = "DC_E_TEST";
  private static final String TPVERSIONID = TPNAME + ":((1))";

  @Before
  public void before() throws Exception {
    testDb = DatabaseTestUtils.getTestDbConnection();
    testInstance = new TechPackAndTypeActivation();
    final Properties props = new Properties();
    props.put("dwhrepDatabaseUrl", DatabaseTestUtils.getTestDbUrl());
    props.put("dwhrepDatabaseUsername", DatabaseTestUtils.getTestDbUser());
    props.put("dwhrepDatabasePassword", DatabaseTestUtils.getTestDbPassword());
    props.put("dwhrepDatabaseDriver", DatabaseTestUtils.getTestDbDriver());
    final Project p = new Project(){
      @Override
      public String getProperty(final String propertyName) {
        return props.getProperty(propertyName);
      }
    };
    testInstance.setProject(p);
    final Field dwhrepRockFactory = testInstance.getClass().getDeclaredField("dwhrepRockFactory");
    dwhrepRockFactory.setAccessible(true);
    dwhrepRockFactory.set(testInstance, testDb);

    setVersionId(testInstance, TPVERSIONID);

    DatabaseTestUtils.loadSetup(testDb, "TechPackAndTypeActivation");
  }

  private void setVersionId(final TechPackAndTypeActivation instance, final String versionId) throws NoSuchFieldException, IllegalAccessException {
    final Field techPackVersionID = instance.getClass().getDeclaredField("techPackVersionID");
    techPackVersionID.setAccessible(true);
    techPackVersionID.set(instance, versionId);
  }

  @After
  public void after(){
    testInstance = null;
    DatabaseTestUtils.close(testDb);
  }

  @Test
  public void test_execute() throws Exception {
    testInstance.setTechPackName(TPNAME);
    testInstance.setBuildNumber("1");
    testInstance.setTechPackMetadataVersion("3");
    testInstance.setBinDirectory("/tmp/bin");
    testInstance.execute();
    assertActivations();
  }

  @Test
  public void test_techPackExists() throws Exception {
    boolean b = testInstance.techPackExists();
    assertTrue(b);
    setVersionId(testInstance, "bb£");
    b = testInstance.techPackExists();
    assertFalse(b);
  }

  @Test
  public void test_createTPActivation_Existing() throws Exception {
    testInstance.setTechPackName(TPNAME);
    testInstance.setTechPackVersion(TPVERSIONID);


    final Statement stmt = testDb.getConnection().createStatement();
    stmt.execute("insert into TPActivation (TECHPACK_NAME, STATUS, VERSIONID, TYPE, MODIFIED) " +
      "values ('DC_E_TEST', 'ACTIVE', 'DC_E_TEST:((1))', 'TEST_TYPE', 0);");
    stmt.close();


    final Method createTPActivation = testInstance.getClass().getDeclaredMethod("createTPActivation");
    createTPActivation.setAccessible(true);
    createTPActivation.invoke(testInstance);
    assertActivations();

  }

  private void assertActivations() throws RockException, SQLException {
	// eeoidiv,20110926:Automatically create _CALC table for update policy=4=HistoryDynamic (like _CURRENT_DC).
    final List<String> expectedTypes = Arrays.asList(
      "DC_E_TEST_TT_PREV",
      "DIM_E_TEST_TT_pmBwUtilizationRx",
      "DIM_E_TEST_ELEMBH_BHTYPE",
      "DIM_E_TEST_TT_BHTYPE",
      "DC_E_TEST_TT",
      "DC_E_TEST",
      "DC_E_TEST_CURRENT_DC",
      "DC_E_TEST_CALC",
      "DC_E_TEST_HIST_RAW"
    );
    final Typeactivation where = new Typeactivation(testDb);
    where.setTechpack_name(TPNAME);
    final TypeactivationFactory fac = new TypeactivationFactory(testDb, where);
    final List<Typeactivation> types = fac.get();
    assertEquals("Wrong number of types activated", expectedTypes.size(), types.size());
    for(Typeactivation activated : types){
      assertTrue("Type not Activated", expectedTypes.contains(activated.getTypename()));
    }
    final Tpactivation where1 = new Tpactivation(testDb);
    where1.setVersionid(TPVERSIONID);
    final TpactivationFactory fac1 = new TpactivationFactory(testDb, where1);
    final List<Tpactivation> tpa = fac1.get();
    assertEquals("Tpactivation Object not created", 1, tpa.size());
    assertEquals("Tech Pack Type not Set Correctly", "PM", tpa.get(0).getType());
  }

  @Test
  public void test_createTPActivation_New() throws Exception {
    testInstance.setTechPackName(TPNAME);
    testInstance.setTechPackVersion(TPVERSIONID);
    final Method createTPActivation = testInstance.getClass().getDeclaredMethod("createTPActivation");
    createTPActivation.setAccessible(true);
    createTPActivation.invoke(testInstance);
    assertActivations();
  }


  @Test
  public void test_get_setTechPackContentPath(){
    final String path = "/tmp/123/";
    testInstance.setTechPackContentPath(path);
    assertEquals("TechPackContentPath Not Set Correctly", path, testInstance.getTechPackContentPath());
    testInstance.setTechPackContentPath(null);
    assertNull("TechPackContentPath Not Set Correctly", testInstance.getTechPackContentPath());
  }

  @Test
  public void test_get_setTechPackName(){
    final String expected = "techpack_name";
    testInstance.setTechPackName(expected);
    assertEquals("TechPackName Not Set Correctly", expected, testInstance.getTechPackName());
    testInstance.setTechPackName(null);
    assertNull("TechPackName Not Set Correctly", testInstance.getTechPackName());
  }

  @Test
  public void test_get_setTechPackVersion(){
    final String expected = "techpack_version";
    testInstance.setTechPackVersion(expected);
    assertEquals("TechPackVersion Not Set Correctly", expected, testInstance.getTechPackVersion());
    testInstance.setTechPackVersion(null);
    assertNull("TechPackVersion Not Set Correctly", testInstance.getTechPackVersion());
  }

  @Test
  public void test_get_setBuildNumber(){
    final String expected = "25";
    testInstance.setBuildNumber(expected);
    assertEquals("BuildNumber Not Set Correctly", expected, testInstance.getBuildNumber());
    testInstance.setBuildNumber(null);
    assertEquals("BuildNumber Not Set Correctly", expected, testInstance.getBuildNumber());
  }

  @Test
  public void test_get_setTechPackMetadataVersion(){
    final String expected = "25";
    testInstance.setTechPackMetadataVersion(expected);
    assertEquals("TechPackMetadataVersion Not Set Correctly", expected, testInstance.getTechPackMetadataVersion());
    testInstance.setTechPackMetadataVersion(null);
    assertEquals("TechPackMetadataVersion Not Set Correctly", expected, testInstance.getTechPackMetadataVersion());
  }

  @Test
  public void test_get_setBinDirectory(){
    final String expected = "/tmp./bin";
    testInstance.setBinDirectory(expected);
    assertEquals("BinDirectory Not Set Correctly", expected, testInstance.getBinDirectory());
    testInstance.setBinDirectory(null);
    assertNull("BinDirectory Not Set Correctly", testInstance.getBinDirectory());
  }
}
