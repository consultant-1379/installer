/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.distocraft.dc5000.install.ant;

import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.repository.dwhrep.*;

public class AddAggFlagToOldTechpack {

    private static final String EVENT_E_RAN_CFA = "EVENT_E_RAN_CFA";
    private static final String ACTIVE = "ACTIVE";
    private static final String STATIC_PROPS = "static.properties";
    private static final String FIFTEEN_MIN = "15MIN";
    private static final String EVENT_E_SGEH = "EVENT_E_SGEH";
    private static final String EVENT_E_LTE = "EVENT_E_LTE";
    private static final String AGG_FLAG = "AGG_FLAG";
    private static final String RAW = "RAW";
    private static final String VERSIONID_COLUMN = "VERSIONID";
    private static final String MEDIATION_TP = "MediationTechpack";
    private static final String ENIQ_EVENT = "ENIQ_EVENT";
    private final static String DIM_TECHPACK = "DIM";
    private static final String DEFAULT_TECHPACKS = "EVENT_E_DVTP,EVENT_E_RAN_HFA,EVENT_E_SGEH,EVENT_E_LTE,EVENT_E_RAN_CFA,"
            + "EVENT_E_LTE_CFA,EVENT_E_LTE_HFA,EVENT_E_MSS,EVENT_E_TERM";

    private transient final String techpackName;

    private transient final RockFactory dwhrepRockFactory;

    private transient final RockFactory dwhRockFactory;

    private transient final String confDir;

    private transient String versionId;

    public AddAggFlagToOldTechpack(final String techpackName, final RockFactory dwhrepRockFactory, final RockFactory dwhRockFactory,
                                   final String confDir) {
        this.techpackName = techpackName;
        this.dwhrepRockFactory = dwhrepRockFactory;
        this.dwhRockFactory = dwhRockFactory;
        this.confDir = confDir;
    }

    public void execute() throws BuildException {
        // checking to see if techpack is an ENIQ EVENTS techpack and it is NOT a topology or mediation techpack
        if (!isTopologyTechpack() && !isMediationTechpack() && isEventTechpack()) {
            if (versionId != null && getListOfTpsWithAggFlag(confDir).contains(techpackName)) {
                runAggFlagUpdates();
            }
        }
    }

    /**
     * @throws SQLException
     * @throws RockException
     */
    private void runAggFlagUpdates() throws BuildException {
        Statement statement = null;

        try {
            statement = dwhRockFactory.getConnection().createStatement();

            checkAndAddAggFlagIfNeeded(RAW, statement);
            if (techpackName.equalsIgnoreCase(EVENT_E_LTE) || techpackName.equalsIgnoreCase(EVENT_E_SGEH)
                    || techpackName.equalsIgnoreCase(EVENT_E_RAN_CFA)) {
                //Add AGG_FLAG for 15MIN tables in EVENT_E_LTE, EVENT_E_SGEH and EVENT_E_RAN_CFA
                checkAndAddAggFlagIfNeeded(FIFTEEN_MIN, statement);
            }
        } catch (SQLException e) {
            throw new BuildException("SQL Exception: " + e.getMessage());
        } catch (RockException e) {
            throw new BuildException("RockException: " + e.getMessage());
        } finally {
            try {
                if (statement != null) {
                    while (statement.getMoreResults()) {
                        statement.getResultSet().close();
                    }
                    statement.close();
                }
            } catch (final SQLException e) {
                System.out.println("Statement cleanup error");
            }
        }
    }

    public static String getListOfTpsWithAggFlag(final String confDir) {
        return getStaticProperties(confDir).getProperty("LIST_OF_TPS_WITH_AGG_FLAG", DEFAULT_TECHPACKS);
    }

    /**
     * @throws SQLException
     * @throws RockException
     */
    private void checkAndAddAggFlagIfNeeded(final String tableLevel, final Statement statement) throws SQLException, RockException {
        for (Dwhtype type : getTableTypes(tableLevel)) {
            final String storageId = type.getStorageid();
            //Only continue if AGG_FLAG is not present
            if (isAggFlagNotPresent(storageId)) {
                addAggFlag(storageId, statement);
            }
        }
    }

    /**
     * @param storageId
     * @throws SQLException
     * @throws RockException
     */
    private void addAggFlag(final String storageId, final Statement statement) throws SQLException, RockException {
        long result = 0;
        final long timeInMillisBeforeUpdate = System.currentTimeMillis();
        for (Dwhpartition part : getPartitions(storageId)) {
            result += addAggFlagToPartition(statement, part.getTablename());
        }
        final long timeInMillisToRunUpdate = System.currentTimeMillis() - timeInMillisBeforeUpdate;
        if (result > 0) {
            System.out.println("Added and updated AGG_FLAG for '" + result + "' rows for storageId: " + storageId + ". Time Taken in seconds: "
                    + (timeInMillisToRunUpdate) / (1000.0));
        } else {
            System.out.println("Added AGG_FLAG for storageId: " + storageId + ". Time Taken in seconds: " + (timeInMillisToRunUpdate) / (1000.0));
        }
    }

    /**
     * @param statement
     * @param part
     * @return
     * @throws SQLException
     */
    private long addAggFlagToPartition(final Statement statement, final String tableName) {
        long result = 0;
        ResultSet resultSet = null;
        try {
            final String checkForAggFlag = "select count(*) from sys.syscolumn col, sys.systab tab where col.column_name = "
                    + "'AGG_FLAG' and col.table_id = tab.table_id and tab.table_name = " + "'" + tableName + "'";
            resultSet = statement.executeQuery(checkForAggFlag);
            if (resultSet.next()) {
                if (resultSet.getInt(1) == 0) {
                    //AGG_FLAG not present, add it
                    final String addAggFlag = "alter table " + tableName + " add AGG_FLAG bit null";
                    statement.executeUpdate(addAggFlag);
                    final String updateAggFlag = "update " + tableName + " set AGG_FLAG=1 where AGG_FLAG is null";
                    result = statement.executeUpdate(updateAggFlag);
                }
            }
        } catch (SQLException e) {
            throw new BuildException("SQL Exception: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (final SQLException e) {
                System.out.println("ResultSet cleanup error");
            }
        }
        return result;
    }

    /**
     * @return
     * @throws SQLException
     * @throws RockException
     */
    private List<Dwhtype> getTableTypes(final String tableLevel) throws SQLException, RockException {
        final Dwhtype dt_cond = new Dwhtype(dwhrepRockFactory);
        dt_cond.setTechpack_name(techpackName);
        dt_cond.setTablelevel(tableLevel);
        final DwhtypeFactory dt_fact = new DwhtypeFactory(dwhrepRockFactory, dt_cond);
        return dt_fact.get();
    }

    /**
     * @param measTable
     * @return
     * @throws SQLException
     * @throws RockException
     */
    private boolean isAggFlagNotPresent(final String storageId) throws SQLException, RockException {
        final Dwhcolumn whereForAggFlag = new Dwhcolumn(dwhrepRockFactory);
        whereForAggFlag.setStorageid(storageId);
        whereForAggFlag.setDataname(AGG_FLAG);
        final DwhcolumnFactory factoryForAggFlag = new DwhcolumnFactory(dwhrepRockFactory, whereForAggFlag);
        return factoryForAggFlag.get().size() == 0;
    }

    /**
     * @param storageId
     * @throws SQLException
     * @throws RockException
     */
    private List<Dwhpartition> getPartitions(final String storageId) throws SQLException, RockException {
        final Dwhpartition wherePartitions = new Dwhpartition(dwhrepRockFactory);
        wherePartitions.setStorageid(storageId);
        wherePartitions.setStatus(ACTIVE);
        final DwhpartitionFactory dwhPartitionFactory = new DwhpartitionFactory(dwhrepRockFactory, wherePartitions);
        return dwhPartitionFactory.get();
    }

    /**
     * Checks if this is a Topology Techpack
     */
    private boolean isTopologyTechpack() {
        boolean isTopologyTp = false;
        if (techpackName.startsWith(DIM_TECHPACK)) {
            isTopologyTp = true;
        }
        return isTopologyTp;
    }

    private boolean isMediationTechpack() {
        return techpackName.equalsIgnoreCase(MEDIATION_TP);
    }

    private boolean isEventTechpack() throws BuildException {
        final String sqlEventTechpack = " Select " + VERSIONID_COLUMN + " From TPActivation Where Techpack_Name = '" + techpackName
                + "' And Type = '" + ENIQ_EVENT + "'";

        boolean isEventTechpack = false;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = dwhrepRockFactory.getConnection().createStatement();
            resultSet = statement.executeQuery(sqlEventTechpack);

            if (resultSet.next()) {
                isEventTechpack = true;
                this.versionId = resultSet.getString(VERSIONID_COLUMN);
            }
        } catch (SQLException se) {
            throw new BuildException("Unable to run SQL statement: " + sqlEventTechpack, se);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (final SQLException e) {
                System.out.println("ResultSet cleanup error ");
            }

            try {
                if (statement != null) {
                    while (statement.getMoreResults()) {
                        statement.getResultSet().close();
                    }
                    statement.close();
                }
            } catch (final SQLException e) {
                System.out.println("Statement cleanup error ");
            }
        }

        return isEventTechpack;
    }

    private static Properties getStaticProperties(final String confDirectory) {
        String dir;
        if (confDirectory.endsWith(File.separator)) {
            dir = confDirectory;
        } else {
            dir = confDirectory + File.separator;
        }
        try {
            final FileInputStream streamProperties = new FileInputStream(dir + STATIC_PROPS);

            final Properties etlcProps = new Properties();
            etlcProps.load(streamProperties);

            streamProperties.close();
            return etlcProps;
        } catch (IOException e) {
            throw new BuildException("Exception reading properties file: " + STATIC_PROPS, e);
        }
    }
}