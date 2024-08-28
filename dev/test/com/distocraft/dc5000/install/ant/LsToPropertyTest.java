/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.distocraft.dc5000.install.ant;

import static org.junit.Assert.*;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Hashtable;

import junit.framework.JUnit4TestAdapter;

import org.apache.tools.ant.Project;
import org.junit.BeforeClass;
import org.junit.Test;

public class LsToPropertyTest {

    private static final String FILE_3 = "foobar_LsToPropertyTest";
    private static final String FILE_2 = "File_LsToPropertyTest2";
    private static final String FILE_1 = "fFile_LsToPropertyTest1";

    private static final File TMP = new File(System.getProperty("java.io.tmpdir"));

    @BeforeClass
    public static void init() {

        final File f1 = new File(TMP, FILE_1);
        final File f2 = new File(TMP, FILE_2);
        final File f3 = new File(TMP, FILE_3);

        f1.deleteOnExit();
        f2.deleteOnExit();
        f3.deleteOnExit();

        try {
            PrintWriter pw = new PrintWriter(new FileWriter(f1));
            pw.print("foobar1");
            pw.close();
            pw = new PrintWriter(new FileWriter(f2));
            pw.print("foobar2");
            pw.close();
            pw = new PrintWriter(new FileWriter(f3));
            pw.print("foobar3");
            pw.close();
        } catch (final Exception e) {
            e.printStackTrace();
            fail("Can´t write in file!");
        }
    }

    /**
     * Test method create property value from file name which ends with 'File1'
     * 
     */

    @Test
    public void testExecute() {
        final LsToProperty ltp = new LsToProperty();
        ltp.setDir(TMP.getPath());
        ltp.setPattern("*File_LsToPropertyTest1");
        ltp.setProperty("PROPERTY");

        Project proj = new Project();
        ltp.setProject(proj);

        ltp.execute();

        proj = ltp.getProject();
        final Hashtable p = proj.getProperties();
        assertEquals(FILE_1, p.get("PROPERTY"));
    }

    /**
     * Test method create property value from file name which starts with 'File'
     * 
     */

    @Test
    public void testExecute2() {
        final LsToProperty ltp = new LsToProperty();
        ltp.setDir(TMP.getPath());
        ltp.setPattern("File_LsToPropertyTest*");
        ltp.setProperty("PROPERTY");

        Project proj = new Project();
        ltp.setProject(proj);

        ltp.execute();

        proj = ltp.getProject();
        final Hashtable p = proj.getProperties();
        assertEquals(FILE_2, p.get("PROPERTY"));
    }

    /**
     * Test method create property value from file name which name equals pattern 'foobar'
     * 
     */

    @Test
    public void testExecute3() {
        final LsToProperty ltp = new LsToProperty();
        ltp.setDir(TMP.getPath());
        ltp.setPattern(FILE_3);
        ltp.setProperty("PROPERTY");

        Project proj = new Project();
        ltp.setProject(proj);

        ltp.execute();

        proj = ltp.getProject();
        final Hashtable p = proj.getProperties();
        assertEquals(FILE_3, p.get("PROPERTY"));
    }

    @Test
    public void testSetDir() {
        final LsToProperty ltp = new LsToProperty();
        final Class secretClass = ltp.getClass();

        try {
            final Field dir = secretClass.getDeclaredField("dir");

            dir.setAccessible(true);

            ltp.setDir("DIR");

            assertEquals("DIR", dir.get(ltp));

        } catch (final Exception e) {
            e.printStackTrace();
            fail("testSetters() failed, Exception");
        }
    }

    @Test
    public void testSetProperty() {
        final LsToProperty ltp = new LsToProperty();
        final Class secretClass = ltp.getClass();

        try {
            final Field property = secretClass.getDeclaredField("property");

            property.setAccessible(true);

            ltp.setProperty("PROPERTY");

            assertEquals("PROPERTY", property.get(ltp));

        } catch (final Exception e) {
            e.printStackTrace();
            fail("testSetters() failed, Exception");
        }
    }

    @Test
    public void testSetPattern() {
        final LsToProperty ltp = new LsToProperty();
        final Class secretClass = ltp.getClass();

        try {
            final Field pattern = secretClass.getDeclaredField("pattern");

            pattern.setAccessible(true);

            ltp.setPattern("PATTERN");

            assertEquals("PATTERN", pattern.get(ltp));

        } catch (final Exception e) {
            e.printStackTrace();
            fail("testSetters() failed, Exception");
        }
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(LsToPropertyTest.class);
    }
}
