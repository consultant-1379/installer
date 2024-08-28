/**
 * 
 */
package com.distocraft.dc5000.install.tools;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.common.StaticProperties;
import com.ericsson.eniq.install.tools.DBUsers;

/**
 * @author etogust
 * 
 */
public class DBUsersTaskTest extends Task {

	static DBUsers dbu;
	static File tmpPropFile;
	private static RockFactory etlrep;
	private static Connection etlrepConnection;
	private final static String TESTDB_DRIVER = "org.hsqldb.jdbcDriver";

	private final static String ETLREP_URL = "jdbc:hsqldb:mem:etlrep";
	private static final String TEST = "retValue";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
    StaticProperties.giveProperties(new Properties());
		setupEtlRep();
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testTaskWithCryptedPassword() throws IOException {
		tmpPropFile = new File("ETLCServer.properties");
		tmpPropFile.createNewFile();
		tmpPropFile.deleteOnExit();

		PrintWriter pw = new PrintWriter(tmpPropFile);
		pw.println("ENGINE_DB_URL = " + ETLREP_URL);
		pw.println("ENGINE_DB_USERNAME = SA");
		pw.println("ENGINE_DB_PASSWORD =");
		pw.println("ENGINE_DB_DRIVERNAME = " + TESTDB_DRIVER);
		pw.close();
		
		String path = tmpPropFile.getAbsolutePath();
		path = path.substring(0, path.lastIndexOf(File.separator));
		System.setProperty("CONF_DIR", path);
		dbu = new DBUsers();
		dbu.setConnection("dwhrep");
		dbu.setUsername("dwhrep");
		dbu.setPropname("tgu");
		
		Project pr = new Project();
		dbu.setProject(pr);
		setProject(pr);
		dbu.execute();
		String pwd = "";
		String pwdFromProp = ""; 
		
		try {
			pwd = getProject().getProperty( "dwhrep.dwhrep.password");	
			pwdFromProp = getProject().getProperty( "tgu");	
		} catch (Exception e) {

		}
		
		if (pwd == null){
			fail("DBUsers task did not set up property correctly while password was crypted at propertyFile");
		}
		if (!pwd.equals(TEST)){
			fail("DBUsers task did not set up property correctly while password was crypted at propertyFile");
		}
		if (!pwd.equals(pwdFromProp)){
			fail("DBUsers task did not set up propName property correctly.");
		}
		
	}
	
	
	/****************************************************************************************************/
	/************************************* PRIVATE METHODS **********************************************/
	/****************************************************************************************************/
	private static void setupEtlRep() throws SQLException, RockException {
		etlrep = new RockFactory(ETLREP_URL, "SA", "", TESTDB_DRIVER,
				"etlrepConnection", true);
		etlrepConnection = etlrep.getConnection();

		Statement statementForEtlRep = etlrepConnection.createStatement();
		statementForEtlRep.execute("CREATE SCHEMA etlrep AUTHORIZATION DBA");
		createMeta_Databases(etlrepConnection);
		statementForEtlRep.close();
	}

	private static void createMeta_Databases(Connection connection) throws SQLException {
		Statement stmt = connection.createStatement();

		stmt.execute("CREATE TABLE META_DATABASES(USERNAME VARCHAR(30), VERSION_NUMBER VARCHAR(32), TYPE_NAME VARCHAR(15), CONNECTION_ID NUMERIC(38), CONNECTION_NAME VARCHAR(30), CONNECTION_STRING VARCHAR(200), PASSWORD VARCHAR(30), DESCRIPTION VARCHAR(32000), DRIVER_NAME VARCHAR(100), DB_LINK_NAME VARCHAR(128));");
		
		stmt.executeUpdate("INSERT INTO META_DATABASES VALUES ('etlrep','0','USER','0','etlrep','jdbc:hsqldb:mem:etlrep:2641','etlrep','ETL Repository Database','org.hsqldb.jdbcDriver',null);");
		stmt.executeUpdate("INSERT INTO META_DATABASES VALUES ('dwhrep','0','USER','1','dwhrep','jdbc:hsqldb:mem:repdb:2641','"+ TEST + "','DWH Repository Database','org.hsqldb.jdbcDriver',null);");

		stmt.close();

	}

}
