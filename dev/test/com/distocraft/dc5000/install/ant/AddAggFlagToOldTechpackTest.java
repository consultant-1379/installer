/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.distocraft.dc5000.install.ant;

import static com.ericsson.eniq.common.testutilities.RockDatabaseHelper.*;
import static org.junit.Assert.*;

import java.lang.reflect.*;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.junit.*;

import com.distocraft.dc5000.common.StaticProperties;
import com.distocraft.dc5000.repository.dwhrep.Dwhpartition;
import com.distocraft.dc5000.repository.dwhrep.Dwhtype;
import com.ericsson.eniq.common.testutilities.BaseUnitTestX;
import com.ericsson.eniq.common.testutilities.RockDatabaseHelper;

public class AddAggFlagToOldTechpackTest extends BaseUnitTestX {

    private static final String RAW = "RAW";
    private static final int COLNUMBER = 1;
    private static final int UNIQUEKEY = 0;
    private static final int UNIQUEVALUE = 1;
    private static final int DATASCALE = 0;
    private static final int INCLUDESQL = 0;
    private static final int DATASIZE = 0;
    private static final String BIT = "bit";
    private static final String AGG_FLAG = "AGG_FLAG";
    private static final String STORAGEID_VALUE = "EVENT_E_SGEH_ERR:RAW";
    private static final String VERSION_ID = "EVENT_E_SGEH:((166))";
    private static final String TABLE_NAME = "EVENT_E_SGEH_ERR_RAW_01";

    private static Statement dwhrepStmt;
    private final String confDir = "/eniq/sw/conf";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        StaticProperties.giveProperties(new Properties());
        RockDatabaseHelper.setUpBeforeClass();

        dwhrepStmt = dwhRepConnection.createStatement();

        dwhrepStmt.execute("create table tpactivation (techpack_name varchar(255), VersionID varchar(255),  Type varchar(255))");
        dwhrepStmt.executeUpdate("insert into tpactivation (techpack_name, VersionID, Type) values ('EVENT_E_SGEH', '" + VERSION_ID
                + "', 'ENIQ_EVENT')");

        dwhrepStmt.execute("create table DWHType (TECHPACK_NAME varchar(255), TYPENAME varchar(255), TABLELEVEL varchar(255), "
                + "STORAGEID varchar(255), PARTITIONSIZE int, PARTITIONCOUNT int, STATUS varchar(50),TYPE varchar(50), "
                + "OWNER varchar(50), VIEWTEMPLATE varchar(255), CREATETEMPLATE varchar(255), NEXTPARTITIONTIME timestamp,"
                + "BASETABLENAME varchar(125), DATADATECOLUMN varchar(128),PUBLICVIEWTEMPLATE varchar(255), PARTITIONPLAN varchar(128))");
        dwhrepStmt.execute("create table DwhColumn (STORAGEID varchar(255), DATANAME varchar(128), COLNUMBER int,"
                + "DATATYPE varchar(50), DATASIZE int, DATASCALE int, UNIQUEVALUE int,"
                + "NULLABLE int, INDEXES varchar(20), UNIQUEKEY int, STATUS varchar(10), INCLUDESQL int)");
        dwhrepStmt.execute("create table DWHPartition (STORAGEID varchar(255), TABLENAME varchar(128), STARTTIME date,"
                + "ENDTIME date, STATUS varchar(20), LOADORDER int)");
    }

    @AfterClass
    public static void tearDownAfterClass() throws SQLException {
        dwhrepStmt.execute("DROP TABLE DWHType");
        dwhrepStmt.execute("DROP TABLE DwhColumn");
        dwhrepStmt.execute("DROP TABLE tpactivation");
    }

    @Test
    public void isEventTechpackTestFalse() throws Exception {
        final String techpackName = "DIM_E_SGEH";
        final Field versionId = AddAggFlagToOldTechpack.class.getDeclaredField("versionId");
        versionId.setAccessible(true);
        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        final boolean eventTechpack = runIsEventTechpackMethod(addAggFlagToOldTp, addAggFlagToOldTpClass);
        assertEquals(false, eventTechpack);
        assertEquals(null, versionId.get(addAggFlagToOldTp));
    }

    @Test
    public void isEventTechpackTestTrue() throws Exception {
        final String techpackName = "EVENT_E_SGEH";
        final Field versionId = AddAggFlagToOldTechpack.class.getDeclaredField("versionId");
        versionId.setAccessible(true);
        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        final boolean eventTechpack = runIsEventTechpackMethod(addAggFlagToOldTp, addAggFlagToOldTpClass);
        assertEquals(true, eventTechpack);
        assertEquals(VERSION_ID, versionId.get(addAggFlagToOldTp));
    }

    @Test
    public void isTopologyTechpackTestTrue() throws Exception {
        final String techpackName = "DIM_E_SGEH";
        final Field versionId = AddAggFlagToOldTechpack.class.getDeclaredField("versionId");
        versionId.setAccessible(true);
        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        final String methodIsTopologyTechpack = "isTopologyTechpack";

        final Method method = addAggFlagToOldTpClass.getDeclaredMethod(methodIsTopologyTechpack, new Class[] {});
        method.setAccessible(true);
        final Boolean eventTechpack = (Boolean) method.invoke(addAggFlagToOldTp, new Object[] {});

        assertEquals(true, eventTechpack);
        assertEquals(null, versionId.get(addAggFlagToOldTp));
    }

    @Test
    public void isTopologyTechpackTestFalse() throws Exception {
        final String techpackName = "EVENT_E_SGEH";
        final Field versionId = AddAggFlagToOldTechpack.class.getDeclaredField("versionId");
        versionId.setAccessible(true);
        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        final String methodIsTopologyTechpack = "isTopologyTechpack";

        final Method method = addAggFlagToOldTpClass.getDeclaredMethod(methodIsTopologyTechpack, new Class[] {});
        method.setAccessible(true);
        final Boolean eventTechpack = (Boolean) method.invoke(addAggFlagToOldTp, new Object[] {});

        assertEquals(false, eventTechpack);
        assertEquals(null, versionId.get(addAggFlagToOldTp));
    }

    @Test
    public void isMediationTechpackTestTrue() throws Exception {
        final String techpackName = "MediationTechpack";
        final Field versionId = AddAggFlagToOldTechpack.class.getDeclaredField("versionId");
        versionId.setAccessible(true);
        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        final String methodIsMediationTechpack = "isMediationTechpack";

        final Method method = addAggFlagToOldTpClass.getDeclaredMethod(methodIsMediationTechpack, new Class[] {});
        method.setAccessible(true);
        final Boolean eventTechpack = (Boolean) method.invoke(addAggFlagToOldTp, new Object[] {});

        assertEquals(true, eventTechpack);
        assertEquals(null, versionId.get(addAggFlagToOldTp));
    }

    @Test
    public void isMediationTechpackTestFalse() throws Exception {
        final String techpackName = "EVENT_E_SGEH";
        final Field versionId = AddAggFlagToOldTechpack.class.getDeclaredField("versionId");
        versionId.setAccessible(true);
        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        final String methodIsMediationTechpack = "isMediationTechpack";

        Method method = addAggFlagToOldTpClass.getDeclaredMethod(methodIsMediationTechpack, new Class[] {});
        method.setAccessible(true);
        final Boolean eventTechpack = (Boolean) method.invoke(addAggFlagToOldTp, new Object[] {});

        assertEquals(false, eventTechpack);
        assertEquals(null, versionId.get(addAggFlagToOldTp));
    }

    @Test
    public void checkFalseIsReturnedWhenEntryInDwhColumnForRawAggFlag() throws Exception {
        final String techpackName = "EVENT_E_SGEH";
        dwhrepStmt.executeUpdate("insert into DWHType (TECHPACK_NAME, TABLELEVEL, STORAGEID) values ('EVENT_E_SGEH', 'RAW', '" + STORAGEID_VALUE
                + "' )");
        dwhrepStmt.executeUpdate("insert into DwhColumn (STORAGEID, DATANAME, COLNUMBER, DATASIZE, DATASCALE, "
                + "UNIQUEVALUE, UNIQUEKEY, INCLUDESQL, DATATYPE) values ('" + STORAGEID_VALUE + "', '" + AGG_FLAG + "', " + COLNUMBER + ", "
                + DATASIZE + ", " + DATASCALE + ", " + UNIQUEVALUE + ", " + UNIQUEKEY + ", '" + INCLUDESQL + "', '" + BIT + "')");
        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        runIsEventTechpackMethod(addAggFlagToOldTp, addAggFlagToOldTpClass);
        //This should return false as AGG_FLAG already exists
        final boolean isThereNoAggflag = runIsAggFlagNotPresent(addAggFlagToOldTp, addAggFlagToOldTpClass, STORAGEID_VALUE);

        assertEquals(false, isThereNoAggflag);
        dwhrepStmt.execute("truncate table DWHType");
        dwhrepStmt.execute("truncate table DwhColumn");
    }

    @Test
    public void checkTrueIsReturnedWhenEntryInDwhColumnForRawAggFlag() throws Exception {
        final String techpackName = "EVENT_E_SGEH";
        dwhrepStmt.executeUpdate("insert into DWHType (TECHPACK_NAME, TABLELEVEL, STORAGEID) values ('EVENT_E_SGEH', 'RAW', '" + STORAGEID_VALUE
                + "' )");
        dwhrepStmt.executeUpdate("insert into DwhColumn (STORAGEID, DATANAME, COLNUMBER, DATASIZE, DATASCALE, "
                + "UNIQUEVALUE, UNIQUEKEY, INCLUDESQL, DATATYPE) values ('" + STORAGEID_VALUE + "', 'DATE_ID', " + COLNUMBER + ", " + DATASIZE + ", "
                + DATASCALE + ", " + UNIQUEVALUE + ", " + UNIQUEKEY + ", '" + INCLUDESQL + "', 'date')");

        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        runIsEventTechpackMethod(addAggFlagToOldTp, addAggFlagToOldTpClass);
        //This should return true as AGG_FLAG did NOT exist
        final boolean isThereNoAggflag = runIsAggFlagNotPresent(addAggFlagToOldTp, addAggFlagToOldTpClass, STORAGEID_VALUE);

        assertEquals(true, isThereNoAggflag);
        dwhrepStmt.execute("truncate table DWHType");
        dwhrepStmt.execute("truncate table DwhColumn");
    }

    @Test
    public void checkNothingIsReturnedWhenNoEntryInDwhPartition() throws Exception {
        final String techpackName = "EVENT_E_SGEH";
        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        runIsEventTechpackMethod(addAggFlagToOldTp, addAggFlagToOldTpClass);
        final List<Dwhpartition> isEntryInDwhPartition = runGetPartitions(addAggFlagToOldTp, addAggFlagToOldTpClass, STORAGEID_VALUE);
        assertEquals(0, isEntryInDwhPartition.size());
    }

    @Test
    public void checkNothingIsReturnedWhenNoEntryInDwhPartitionThatIsActive() throws Exception {
        final String techpackName = "EVENT_E_SGEH";
        dwhrepStmt.executeUpdate("insert into DwhPartition (STORAGEID, TABLENAME, STATUS) values ('" + STORAGEID_VALUE + "', '" + TABLE_NAME
                + "', 'DEACTIVE')");
        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        runIsEventTechpackMethod(addAggFlagToOldTp, addAggFlagToOldTpClass);
        final List<Dwhpartition> isEntryInDwhPartition = runGetPartitions(addAggFlagToOldTp, addAggFlagToOldTpClass, STORAGEID_VALUE);
        assertEquals(0, isEntryInDwhPartition.size());
        dwhrepStmt.execute("truncate table DWHType");
        dwhrepStmt.execute("truncate table DwhColumn");
    }

    @Test
    public void checkEntryIsReturnedWhenAnActiveEntryInDwhPartition() throws Exception {
        final String techpackName = "EVENT_E_SGEH";
        dwhrepStmt.executeUpdate("insert into DwhPartition (STORAGEID, TABLENAME, STATUS) values ('" + STORAGEID_VALUE + "', '" + TABLE_NAME
                + "', 'ACTIVE')");
        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        runIsEventTechpackMethod(addAggFlagToOldTp, addAggFlagToOldTpClass);
        final List<Dwhpartition> isEntryInDwhPartition = runGetPartitions(addAggFlagToOldTp, addAggFlagToOldTpClass, STORAGEID_VALUE);
        assertEquals(1, isEntryInDwhPartition.size());
        assertEquals(STORAGEID_VALUE, isEntryInDwhPartition.get(0).getStorageid());
        assertEquals(TABLE_NAME, isEntryInDwhPartition.get(0).getTablename());
        dwhrepStmt.execute("truncate table DWHType");
        dwhrepStmt.execute("truncate table DwhColumn");
    }

    @Test
    public void checkNothingIsReturnedWhenNoEntryInDwhType() throws Exception {
        final String techpackName = "EVENT_E_SGEH";
        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        runIsEventTechpackMethod(addAggFlagToOldTp, addAggFlagToOldTpClass);
        final List<Dwhtype> isEntryInDwhType = runGetTableTypes(addAggFlagToOldTp, addAggFlagToOldTpClass, RAW);
        assertEquals(0, isEntryInDwhType.size());
    }

    @Test
    public void checkNothingIsReturnedWhenNoEntryInDwhTypeForRaw() throws Exception {
        final String techpackName = "EVENT_E_SGEH";
        dwhrepStmt.executeUpdate("insert into DWHType (TECHPACK_NAME, TABLELEVEL, STORAGEID) values ('EVENT_E_SGEH', '15MIN', '" + STORAGEID_VALUE
                + "' )");
        dwhrepStmt.executeUpdate("insert into DwhColumn (STORAGEID, DATANAME, COLNUMBER, DATASIZE, DATASCALE, "
                + "UNIQUEVALUE, UNIQUEKEY, INCLUDESQL, DATATYPE) values ('" + STORAGEID_VALUE + "', '" + AGG_FLAG + "', " + COLNUMBER + ", "
                + DATASIZE + ", " + DATASCALE + ", " + UNIQUEVALUE + ", " + UNIQUEKEY + ", '" + INCLUDESQL + "', '" + BIT + "')");
        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        runIsEventTechpackMethod(addAggFlagToOldTp, addAggFlagToOldTpClass);
        final List<Dwhtype> isEntryInDwhType = runGetTableTypes(addAggFlagToOldTp, addAggFlagToOldTpClass, RAW);
        assertEquals(0, isEntryInDwhType.size());
        dwhrepStmt.execute("truncate table DWHType");
        dwhrepStmt.execute("truncate table DwhColumn");
    }

    @Test
    public void checkEntryIsReturnedWhenAEntryInDwhTypeForRaw() throws Exception {
        final String techpackName = "EVENT_E_SGEH";
        dwhrepStmt.executeUpdate("insert into DWHType (TECHPACK_NAME, TABLELEVEL, STORAGEID) values ('EVENT_E_SGEH', '" + RAW + "', '"
                + STORAGEID_VALUE + "' )");
        dwhrepStmt.executeUpdate("insert into DwhColumn (STORAGEID, DATANAME, COLNUMBER, DATASIZE, DATASCALE, "
                + "UNIQUEVALUE, UNIQUEKEY, INCLUDESQL, DATATYPE) values ('" + STORAGEID_VALUE + "', 'DATE_ID', " + COLNUMBER + ", " + DATASIZE + ", "
                + DATASCALE + ", " + UNIQUEVALUE + ", " + UNIQUEKEY + ", '" + INCLUDESQL + "', 'date')");

        final AddAggFlagToOldTechpack addAggFlagToOldTp = new AddAggFlagToOldTechpack(techpackName, dwhrepRock, dwhRock, confDir);
        final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass = addAggFlagToOldTp.getClass();
        runIsEventTechpackMethod(addAggFlagToOldTp, addAggFlagToOldTpClass);
        final List<Dwhtype> isEntryInDwhType = runGetTableTypes(addAggFlagToOldTp, addAggFlagToOldTpClass, RAW);
        assertEquals(1, isEntryInDwhType.size());
        assertEquals(STORAGEID_VALUE, isEntryInDwhType.get(0).getStorageid());
        assertEquals(RAW, isEntryInDwhType.get(0).getTablelevel());
        dwhrepStmt.execute("truncate table DWHType");
        dwhrepStmt.execute("truncate table DwhColumn");
    }

    private Boolean runIsAggFlagNotPresent(final AddAggFlagToOldTechpack addAggFlagToOldTp,
                                           final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass, final String storageId)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final String methodIsAggFlagNotPresent = "isAggFlagNotPresent";

        final Method method = addAggFlagToOldTpClass.getDeclaredMethod(methodIsAggFlagNotPresent, new Class[] { String.class });
        method.setAccessible(true);
        return (Boolean) method.invoke(addAggFlagToOldTp, new Object[] { storageId });
    }

    private List<Dwhtype> runGetTableTypes(final AddAggFlagToOldTechpack addAggFlagToOldTp,
                                           final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass, final String tableLevel)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final String methodGetTableTypes = "getTableTypes";

        final Method method = addAggFlagToOldTpClass.getDeclaredMethod(methodGetTableTypes, new Class[] { String.class });
        method.setAccessible(true);
        return (List<Dwhtype>) method.invoke(addAggFlagToOldTp, new Object[] { tableLevel });
    }

    private List<Dwhpartition> runGetPartitions(final AddAggFlagToOldTechpack addAggFlagToOldTp,
                                                final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass, final String tableLevel)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final String methodGetPartitions = "getPartitions";

        final Method method = addAggFlagToOldTpClass.getDeclaredMethod(methodGetPartitions, new Class[] { String.class });
        method.setAccessible(true);
        return (List<Dwhpartition>) method.invoke(addAggFlagToOldTp, new Object[] { tableLevel });
    }

    private boolean runIsEventTechpackMethod(final AddAggFlagToOldTechpack addAggFlagToOldTp,
                                             final Class<? extends AddAggFlagToOldTechpack> addAggFlagToOldTpClass) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        final String methodIsEventTechpack = "isEventTechpack";

        final Method method = addAggFlagToOldTpClass.getDeclaredMethod(methodIsEventTechpack, new Class[] {});
        method.setAccessible(true);
        return (Boolean) method.invoke(addAggFlagToOldTp, new Object[] {});
    }
}