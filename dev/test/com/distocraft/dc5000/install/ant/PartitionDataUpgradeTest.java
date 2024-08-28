package com.distocraft.dc5000.install.ant;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ssc.rockfactory.RockFactory;

import com.ericsson.eniq.common.Constants;
import com.distocraft.dc5000.common.StaticProperties;

/**
 * Tests for PartitionDataUpgrade class in com.distocraft.dc5000.install.ant.<br>
 * <br>
 *
 * @author efinian
 */
public class PartitionDataUpgradeTest {

  private static PartitionDataUpgrade objUnderTest;

  private static Project proj;

  private static Statement stm;

  private static final File TMP = new File(System.getProperty("java.io.tmpdir"));
  @BeforeClass
  public static void init() throws Exception {
    StaticProperties.giveProperties(new Properties());
    setUpPropertiesFileAndProperty();

    Class.forName("org.hsqldb.jdbcDriver");

    Connection c;

    c = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "");
    stm = c.createStatement();

    stm.execute("CREATE TABLE Meta_databases (USERNAME VARCHAR(31), VERSION_NUMBER VARCHAR(31), "
        + "TYPE_NAME VARCHAR(31), CONNECTION_ID VARCHAR(31), CONNECTION_NAME VARCHAR(31), "
        + "CONNECTION_STRING VARCHAR(31), PASSWORD VARCHAR(31), DESCRIPTION VARCHAR(31), DRIVER_NAME VARCHAR(31), "
        + "DB_LINK_NAME VARCHAR(31))");

    stm.executeUpdate("INSERT INTO Meta_databases VALUES('SA', '1', 'USER', '1', 'dwhrep', "
        + "'jdbc:hsqldb:mem:testdb', '', 'description', 'org.hsqldb.jdbcDriver', 'dblinkname')");

    stm.executeUpdate("INSERT INTO Meta_databases VALUES('SA', '1', 'USER', '1', 'dwh', "
        + "'jdbc:hsqldb:mem:testdb', '', 'description', 'org.hsqldb.jdbcDriver', 'dblinkname')");

    setUpTestData(stm);
  }

  private static void setUpPropertiesFileAndProperty() throws IOException {
    System.setProperty(Constants.DC_CONFIG_DIR_PROPERTY_NAME, TMP.getPath());
    File prop = new File(TMP, "ETLCServer.properties");

    PrintWriter pw = new PrintWriter(new FileWriter(prop));
    pw.write("ENGINE_DB_URL=jdbc:hsqldb:mem:testdb\n");
    pw.write("ENGINE_DB_USERNAME=SA\n");
    pw.write("ENGINE_DB_PASSWORD= \n");
    pw.write("ENGINE_DB_DRIVERNAME=org.hsqldb.jdbcDriver\n");
    pw.close();
  }

  private static void setUpTestData(Statement stm) throws SQLException {
    stm.execute("create table DWHPartition ( " + "STORAGEID varchar(255) null, " + "TABLENAME varchar(255) null, "
        + "STARTTIME timestamp null, " + "ENDTIME timestamp null, " + "STATUS varchar(10) null, "
        + "LOADORDER int null " + "); ");

    stm.executeUpdate("insert into DWHPartition" + "(STORAGEID, TABLENAME) values ("
        + "'EVENT_E_SGEH_SUC:RAW', 'EVENT_E_SGEH_SUC_RAW_01'" + "); ");
    stm.executeUpdate("insert into DWHPartition" + "(STORAGEID, TABLENAME) values ("
        + "'EVENT_E_SGEH_SUC:RAW', 'EVENT_E_SGEH_SUC_RAW_02'" + "); ");
    stm.executeUpdate("insert into DWHPartition" + "(STORAGEID, TABLENAME) values ("
        + "'EVENT_E_SGEH_ERR:RAW', 'EVENT_E_SGEH_ERR_RAW_01'" + "); ");

    stm.execute("create table EVENT_E_SGEH_STATE ( state tinyint null);");
    stm.executeUpdate("insert into EVENT_E_SGEH_STATE values (0)");

    stm.execute("create table EVENT_E_SGEH_SUC_RAW_01 ( a tinyint null, b tinyint null );");
    stm.executeUpdate("insert into EVENT_E_SGEH_SUC_RAW_01 values (1, null)");

    stm.execute("create table EVENT_E_SGEH_SUC_RAW_02 ( a tinyint null, b tinyint null );");
    stm.executeUpdate("insert into EVENT_E_SGEH_SUC_RAW_02 values (2, null)");

    stm.execute("create table EVENT_E_SGEH_ERR_RAW_01 ( a tinyint null, b tinyint null );");
    stm.executeUpdate("insert into EVENT_E_SGEH_ERR_RAW_01 values (3, null)");
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {

    /* Cleaning up after tests */
    stm.execute("DROP TABLE META_DATABASES");
  }

  @Before
  public void setUpBeforeTest() throws Exception {

    /*
     * Creating new PreinstallCheck instance before every test and setting up a
     * ant project object for it
     */
    objUnderTest = new PartitionDataUpgrade();
    proj = new Project();
    objUnderTest.setProject(proj);
  }

  @After
  public void CleanUpAfterTest() throws Exception {
    proj = null;
    objUnderTest = null;
  }

  /**
   * Testing the execute() method which does all the preinstall checking.
   */
  @Test
  public void testExecute() throws Exception {

    /*
     * Testing the execution, which upgrades the DB column b=a for N partitions.
     */

    objUnderTest.setInitTemplateFilepath(generateTemplateFile("init.vm",
 "update EVENT_E_SGEH_STATE set state=1;"));

    objUnderTest.setPartitionTemplateFilepath(generateTemplateFile("partitions.vm", "update $partitionName set b=a;"));

    objUnderTest.setCleanupTemplateFilepath("");

    objUnderTest.setPartitionStorageIdList("EVENT_E_SGEH_SUC:RAW,EVENT_E_SGEH_ERR:RAW");

    objUnderTest.execute();

    String result = checkResults("select b from EVENT_E_SGEH_SUC_RAW_01 union all select b from EVENT_E_SGEH_SUC_RAW_02 union all select b from EVENT_E_SGEH_ERR_RAW_01;");
    assertEquals(result, "1 2 3 ");

    result = checkResults("select state from EVENT_E_SGEH_STATE;");
    assertEquals(result, "1 ");
  }

  @Test
  public void testExecuteSQL() throws Exception {
    CommonDBTasks commonTask = new CommonDBTasks();
    commonTask.setProject(proj);

    RockFactory etlrepRockFactory = commonTask.createEtlrepRockFactory();
    objUnderTest.executeSQL(etlrepRockFactory, "update EVENT_E_SGEH_STATE set state=0");
  }

  @Test
  public void testExecuteParallelSQL() throws Exception {
    ArrayList<String> sqls = new ArrayList<String>();
    sqls.add("update EVENT_E_SGEH_STATE set state=1");
    sqls.add("update EVENT_E_SGEH_STATE set state=2");
    sqls.add("update EVENT_E_SGEH_STATE set state=3");
    objUnderTest.initETLConnection();
    objUnderTest.setMaxParallel(new Integer(2));
    objUnderTest.executeParallelSQL(sqls);
  }

  @Test
  public void testGenerateSQLForTemplate() throws IOException {
    objUnderTest.generateSQLForTemplate(generateTemplateFile("temp.vm", "Partition is: "), "partition1");
  }

  @Test
  public void testGetPartitionNames() throws Exception {
    objUnderTest.initETLConnection();
    objUnderTest.setPartitionStorageIdList("EVENT_E_SGEH_SUC:RAW,EVENT_E_SGEH_ERR:RAW");
    List<String> partitionList = objUnderTest.getPartitionNames();
    assertEquals(partitionList.size(), 3);
  }

  @Test
  public void testExecuteDebug() throws Exception {

    /*
     * Testing the execution, which upgrades the DB column b=a for N partitions.
     */

    objUnderTest.setInitTemplateFilepath(generateTemplateFile("init.vm", "update EVENT_E_SGEH_STATE set state=1;"));

    objUnderTest.setPartitionTemplateFilepath(generateTemplateFile("partitions.vm", "update $partitionName set b=a;"));

    objUnderTest.setCleanupTemplateFilepath("");

    objUnderTest.setPartitionStorageIdList("EVENT_E_SGEH_SUC:RAW,EVENT_E_SGEH_ERR:RAW");

    objUnderTest.setDebug("ON");

    objUnderTest.execute();

    String result = checkResults("select b from EVENT_E_SGEH_SUC_RAW_01 union all select b from EVENT_E_SGEH_SUC_RAW_02 union all select b from EVENT_E_SGEH_ERR_RAW_01;");
    assertEquals(result, "1 2 3 ");

    result = checkResults("select state from EVENT_E_SGEH_STATE;");
    assertEquals(result, "1 ");
  }

  private String checkResults(String query) throws SQLException {
    ResultSet rs = stm
.executeQuery(query);

    String result = "";
    while (rs.next()) {
      result = result + rs.getString(1) + " ";
    }

    return result;
  }

  /**
   * Returns absolute path of generated template file.
   *
   * @param relativeFileName
   * @param fileText
   * @return
   * @throws IOException
   */
  public static String generateTemplateFile(String relativeFileName, String fileText) throws IOException {
    File tmpPropFile = new File(relativeFileName);
    tmpPropFile.createNewFile();
    tmpPropFile.deleteOnExit();

    Writer writer = new BufferedWriter(new FileWriter(tmpPropFile));
    writer.write(fileText);
    writer.close();
    return tmpPropFile.getAbsolutePath();
  }

}
