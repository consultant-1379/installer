/**
 *
 */
package com.distocraft.dc5000.install.ant;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.distocraft.dc5000.common.StaticProperties;

/**
 * @author qponven
 *
 */
public class UpdateMzTpVersionInfoTest {

    private static Connection con = null;

    private static Statement stmt;

    private final static String DWHREP_URL = "jdbc:hsqldb:mem:dwhrep";

    private static UpdateMzTpVersionInfo objUnderTest;

    private static File TPVersionProperties;

    private static String testResult_techPackName;

    private static String testResult_versionID;

    private static String testResult_techPackVersion;

    private static Project proj;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    StaticProperties.giveProperties(new Properties());
        try {
            Class.forName("org.hsqldb.jdbcDriver").newInstance();
            con = DriverManager.getConnection(DWHREP_URL, "sa", "");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        stmt = con.createStatement();
        stmt.execute("create table MZTechPacks (VERSIONID varchar(128) not null,TECHPACK_NAME varchar(30) "
                + "not null,STATUS varchar(10) null,CREATIONDATE datetime null,PRODUCT_NUMBER varchar(255) null,TYPE varchar(10) not null,TECHPACK_VERSION varchar(32) null)");

        stmt.execute("INSERT INTO MZTechPacks (VERSIONID, TECHPACK_NAME, STATUS, CREATIONDATE, PRODUCT_NUMBER, TYPE, TECHPACK_VERSION) VALUES('M1','M_E_SGEH','Active','2010-10-10 10:00:00.0','PRO123 ','MZTP ','R1C')");
        stmt.execute("INSERT INTO MZTechPacks (VERSIONID, TECHPACK_NAME, STATUS, CREATIONDATE, PRODUCT_NUMBER, TYPE, TECHPACK_VERSION) VALUES('M_E_SGEH:((0))','M_E_SGEH','Active','2010-10-10 10:00:00.0','CXC1730751 ','MZ ','R1C')");
               
        
        populateTpDetails();

        proj = new Project();

    }

    private static void populateTpDetails() {
        // System.out.println("populateTpDetails in");
        /* Creating property file for tech packs version properties */
        final File TPVersionPropertiesDir = new File(System.getProperty("user.dir"), "install");
        TPVersionPropertiesDir.mkdir();
        TPVersionPropertiesDir.deleteOnExit();
        TPVersionProperties = new File(System.getProperty("user.dir"), "/install/version.properties");
        TPVersionProperties.deleteOnExit();
        try {
            final PrintWriter pw = new PrintWriter(new FileWriter(TPVersionProperties));
            pw.write("required_tech_packs = tp1\n");
            pw.write("tech_pack.name = M_E_Test123\n");
            pw.write("tech_pack.metadata_version = v1.2\n");
            pw.write("tech_pack.version = R1C\n");
            pw.write("build.tag = btag\n");
            pw.write("build.number = 3\n");
            pw.write("tech_pack.version = v2.01\n");
            pw.write("product.number = CXC123\n");
            pw.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // System.out.println("populateTpDetails out");

    }

    @Before
    public void setUpBeforeTest() throws Exception {
        objUnderTest = new UpdateMzTpVersionInfo();

        objUnderTest.setProject(proj);
        objUnderTest.getProject().setProperty("dwhrepDatabaseUrl", DWHREP_URL);
        objUnderTest.getProject().setProperty("dwhrepDatabaseUsername", "sa");
        objUnderTest.getProject().setProperty("dwhrepDatabasePassword", "");
        objUnderTest.getProject().setProperty("dwhrepDatabaseDriver", "org.hsqldb.jdbcDriver");

        objUnderTest.setRockFactory();

        testResult_techPackVersion = null;
        testResult_versionID = null;
        testResult_techPackName = null;

    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        stmt.execute("DROP TABLE MZTechPacks");
        stmt.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link com.distocraft.dc5000.install.ant.UpdateMzTpVersionInfo#execute()}
     *
     * .
     */
    @Test
    public void testExecute() throws Exception {

        System.out.println("--------------------------");
        System.out.println("Test Case  : testExecute");
        System.out.println("--------------------------");
        objUnderTest.setTechPackMetadataVersion("3");
        objUnderTest.setBuildNumber("45");
        objUnderTest.setTechPackName("M_E_TEMP");
        objUnderTest.setTechPackVersion("EE01");
        objUnderTest.setTechPackContentPath(System.getProperty("user.dir"));
        objUnderTest.execute();

        stmt.execute("SELECT * FROM MZTechPacks WHERE TECHPACK_NAME='M_E_TEMP'");
        final ResultSet rs = stmt.getResultSet();

        while (rs.next()) {
            testResult_techPackVersion = rs.getString("TECHPACK_VERSION");
            testResult_versionID = rs.getString("VERSIONID");
            testResult_techPackName = rs.getString("TECHPACK_NAME");
        }

        assertTrue(testResult_techPackVersion.equals("EE01"));
        assertTrue(testResult_versionID.equals("M_E_TEMP:((45))"));
        assertTrue(testResult_techPackName.equals("M_E_TEMP"));
        System.out.println("--------------------------");
    }

    /**
     * Test method for
     * {@link com.distocraft.dc5000.install.ant.UpdateMzTpVersionInfo#execute()}
     *
     * .
     */
    @Test
    public void testExecute_otherTP() throws Exception {
        System.out.println("----------------------------------");
        System.out.println("Test Case  : testExecute_otherTP");
        System.out.println("----------------------------------");

        objUnderTest.setTechPackVersionID("Execute2");
        objUnderTest.setTechPackName("E_SGEH_TERM");
        objUnderTest.setTechPackVersion("EE02");
        objUnderTest.setTechPackContentPath(System.getProperty("user.dir"));
        objUnderTest.execute();

        stmt.execute("SELECT * FROM MZTechPacks WHERE TECHPACK_NAME='E_SGEH_TERM'");
        final ResultSet rs = stmt.getResultSet();

        while (rs.next()) {
            testResult_techPackVersion = rs.getString("TECHPACK_VERSION");
            testResult_versionID = rs.getString("VERSIONID");
            testResult_techPackName = rs.getString("TECHPACK_NAME");
        }

        assertEquals(null, testResult_techPackVersion);
        assertEquals(null, testResult_versionID);
        assertEquals(null, testResult_techPackName);
        System.out.println("----------------------------------");

    }

    /**
     * Test method for
     * {@link com.distocraft.dc5000.install.ant.UpdateMzTpVersionInfo#updateMZVersionInfoDB()}
     *
     * .
     */
    @Test
    public void testUpdateMZVersionInfoDB_Update() throws Exception {

        System.out.println("---------------------------------------------------");
        System.out.println("Test Case  : testUpdateMZVersionInfoDB_Update");
        System.out.println("---------------------------------------------------");

        System.out.println("testUpdateMZVersionInfoDB() in");

        // Testcase for Updating table
        objUnderTest.setTechPackVersionID("M1");
        objUnderTest.setTechPackName("M_E_SGEH");
        objUnderTest.setTechPackVersion("VVVR1C");
        objUnderTest.setTechPackContentPath(System.getProperty("user.dir"));
        objUnderTest.readTechPackVersionFile();

        objUnderTest.updateMZVersionInfoDB();

        stmt.execute("SELECT * FROM MZTechPacks WHERE TECHPACK_NAME='M_E_SGEH'");
        final ResultSet rs = stmt.getResultSet();

        while (rs.next()) {
            testResult_techPackVersion = rs.getString("TECHPACK_VERSION");
        }

        System.out.println("testResult_techPackName : " + testResult_techPackName);

        assertTrue(testResult_techPackVersion.equals("VVVR1C"));

        // fail("Not yet implemented"); // TODO
        System.out.println("testUpdateMZVersionInfoDB() out");
        System.out.println("---------------------------------------------------");

    }
    
    /**
     * Test method for
     * {@link com.distocraft.dc5000.install.ant.UpdateMzTpVersionInfo#updateMZVersionInfoDB()}
     *
     * .
     */
    @Test
    public void testUpdateMZVersionInfoDB_UpdateSameBuildNumber_NewRState() throws Exception {

        System.out.println("---------------------------------------------------");
        System.out.println("Test Case  : testUpdateMZVersionInfoDB_Update");
        System.out.println("---------------------------------------------------");

               
        System.out.println("testUpdateMZVersionInfoDB() in");
        String versionID = "M_E_SGEH:((0))";
        String techpackName = "M_E_SGEH";
        String techpackVersion = "R1D";
        String techpackMetaDataVersion = "4";
        
        
        String seperator = "-";
        StringBuilder expected = new StringBuilder();
        expected.append(versionID);
        expected.append(seperator);
        expected.append(techpackName);
        expected.append(seperator);
        expected.append(techpackVersion);
        
        
        // Testcase for Updating table
        objUnderTest.setTechPackVersionID(versionID);
        objUnderTest.setTechPackName(techpackName);
        objUnderTest.setTechPackVersion(techpackVersion);
        objUnderTest.setTechPackContentPath(System.getProperty("user.dir"));
        objUnderTest.setTechPackMetadataVersion(techpackMetaDataVersion);
        objUnderTest.readTechPackVersionFile();

        objUnderTest.updateMZVersionInfoDB();

        stmt.execute("SELECT VERSIONID, TECHPACK_NAME, TECHPACK_VERSION FROM MZTechPacks WHERE TECHPACK_NAME='M_E_SGEH'");
        final ResultSet rs = stmt.getResultSet();
                
        boolean versionFound = false;
        System.out.println("Printing out the versions");
        
        while (rs.next()) {
        	StringBuilder actual = new StringBuilder();
        	actual.append(rs.getString("VERSIONID"));
        	actual.append(seperator);
        	actual.append(rs.getString("TECHPACK_NAME"));
        	actual.append(seperator);
        	actual.append(rs.getString("TECHPACK_VERSION"));
        	if (actual.toString().equals(expected.toString())){
        		versionFound = true;
        	}
        	System.out.println("Techpack Details : " + actual.toString());
        }

        

        assertTrue(versionFound);     

    }

    /**
     * Test method for
     * {@link com.distocraft.dc5000.install.ant.UpdateMzTpVersionInfo#updateMZVersionInfoDB()}
     *
     * .
     */
    @Test
    public void testUpdateMZVersionInfoDB_Insert() throws Exception {

        System.out.println("---------------------------------------------------");
        System.out.println("Test Case  : testUpdateMZVersionInfoDB_Insert");
        System.out.println("---------------------------------------------------");

        System.out.println("testUpdateMZVersionInfoDB() in");

        stmt.execute("DELETE FROM MZTechPacks");

        // Testcase for Updating table
        objUnderTest.setTechPackVersionID("MM1");
        objUnderTest.setTechPackName("M_E_TERM");
        objUnderTest.setTechPackVersion("R1C");
        objUnderTest.setTechPackContentPath(System.getProperty("user.dir"));
        objUnderTest.readTechPackVersionFile();
 
        objUnderTest.updateMZVersionInfoDB();

        stmt.execute("SELECT * FROM MZTechPacks WHERE TECHPACK_NAME='M_E_TERM'");
        final ResultSet rs = stmt.getResultSet();

        while (rs.next()) {
            testResult_techPackVersion = rs.getString("TECHPACK_VERSION");
        }

        assertTrue(testResult_techPackVersion.equals("R1C"));

        System.out.println("testUpdateMZVersionInfoDB() out");

        System.out.println("---------------------------------------------------");

    }

   
}

/*
 * final Class classUnderTest = objUnderTest.getClass();
 *
 * final Field dwhrepRockFactory = classUnderTest
 * .getDeclaredField("dwhrepRockFactory");
 * dwhrepRockFactory.setAccessible(true); dwhrepRockFactory.set(objUnderTest,
 * new RockFactory(DWHREP_URL, "sa", "", "org.hsqldb.jdbcDriver", "con",
 * false));
 *
 * final Field targetMZInfo = classUnderTest .getDeclaredField("targetMZInfo");
 * targetMZInfo.setAccessible(true); targetMZInfo.set(objUnderTest, new
 * Mztechpacks( (RockFactory) dwhrepRockFactory.get(objUnderTest)));
 */

