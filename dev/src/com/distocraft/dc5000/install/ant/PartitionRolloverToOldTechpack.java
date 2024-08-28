package com.distocraft.dc5000.install.ant;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import com.distocraft.dc5000.dwhm.StorageTimeAction;
import com.distocraft.dc5000.repository.dwhrep.*;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

public class PartitionRolloverToOldTechpack {

    private static final String EVENT_E_RAN_CFA = "EVENT_E_RAN_CFA";
    private static final String STATIC_PROPS = "static.properties";
    private static final String FIFTEEN_MIN = "15MIN";
    private static final String EVENT_E_SGEH = "EVENT_E_SGEH";
    private static final String EVENT_E_LTE = "EVENT_E_LTE";
    private static final String RAW = "RAW";
    private static final String VERSIONID_COLUMN = "VERSIONID";
    private static final String MEDIATION_TP = "MediationTechpack";
    private static final String ENIQ_EVENT = "ENIQ_EVENT";
    private final static String DIM_TECHPACK = "DIM";
    private static final String DEFAULT_TECHPACKS = "EVENT_E_DVTP,EVENT_E_RAN_HFA,EVENT_E_SGEH,EVENT_E_LTE,EVENT_E_RAN_CFA,"
            + "EVENT_E_LTE_CFA,EVENT_E_LTE_HFA,EVENT_E_MSS,EVENT_E_TERM,EVENT_E_RAN_SESSION,EVENT_E_CORE_SESSION,EVENT_E_USER_PLANE";
  
    private transient final String techpackName;

    private transient final RockFactory dwhrepRockFactory;

    private transient final RockFactory dwhRockFactory;

    private transient final String confDir;

    private transient String versionId;
    
    final long now = System.currentTimeMillis();
    
    // A list of tables to truncate and matching new partition number before
    // loading begins
    protected Map<String, Integer> tablesToTruncate = new HashMap<String, Integer>();

    public PartitionRolloverToOldTechpack(final String techpackName, final RockFactory dwhrepRockFactory, final RockFactory dwhRockFactory,
                                   final String confDir) {
        this.techpackName = techpackName;
        this.dwhrepRockFactory = dwhrepRockFactory;
        this.dwhRockFactory = dwhRockFactory;
        this.confDir = confDir;
    }

    public void execute() throws Exception {
        // checking to see if techpack is an ENIQ EVENTS techpack and it is NOT a topology or mediation techpack
        if (!isTopologyTechpack() && !isMediationTechpack() && isEventTechpack()) {
            if (versionId != null && getListOfTps(confDir).contains(techpackName)) {
                runPartitionUpdates();
            }
        }
    }

    /**
     * @throws Exception 
     * @throws SQLException
     * @throws RockException
     */
    private void runPartitionUpdates() throws Exception {
        Statement statement = null;

        try {
            statement = dwhRockFactory.getConnection().createStatement();

            checkAndRolloverIfNeeded(RAW, statement);
            if (techpackName.equalsIgnoreCase(EVENT_E_LTE) || techpackName.equalsIgnoreCase(EVENT_E_SGEH)
                    || techpackName.equalsIgnoreCase(EVENT_E_RAN_CFA)) {
                //Partition Rollover for 15MIN tables in EVENT_E_LTE, EVENT_E_SGEH and EVENT_E_RAN_CFA
                checkAndRolloverIfNeeded(FIFTEEN_MIN, statement);
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

    public static String getListOfTps(final String confDir) {
        return getStaticProperties(confDir).getProperty("LIST_OF_TPS", DEFAULT_TECHPACKS);
    }

    private void checkAndRolloverIfNeeded(final String tableLevel, final Statement statement) throws Exception {
        for (Dwhtype type : getTableTypes(tableLevel)) {
        	if ("partitioned".equalsIgnoreCase(type.getType())) {
        		final List<Partitionplan> partitionPlanList =  getPartitionPlan(type);
        		final Partitionplan partitionPlan = partitionPlanList.get(0);
				ResultSet resultSet = null;
        		String nextTableName = null;
        		
        		final String storageId = type.getStorageid();
        		final List<Dwhpartition> partitions = getCurrentPartitions(storageId);

        		if (partitionPlan.getPartitiontype() == StorageTimeAction.TIME_BASED_PARTITION_TYPE) {
            	    continue;
        		}
        		else{
                    Dwhpartition hasNonTiered = partitions.get(0);
        			nextTableName = hasNonTiered.getTablename();
					final String checkForTiered = "SELECT Value2 as hasNonTiered FROM sp_iqindexmetadata  ((SELECT top 1 index_name FROM sys.sysindex WHERE index_name LIKE '"+nextTableName+"%_HG'), '"+nextTableName+"', 'dc') WHERE Value1 = 'Maintains Exact Distinct'";
        			resultSet = statement.executeQuery(checkForTiered);
        			if (resultSet.next()) {
                        if (resultSet.getString(1).equalsIgnoreCase("No")) {
                        	System.out.println("Already Tiered HG indexs for: " +nextTableName);
                        	continue;
                        }
                        else{
                        	final String physicalTableName = getRolloverPartitionWithLoadOrder(storageId, partitions); 
                            loadTable(physicalTableName, storageId);
                        }  
        			} 
        		}
        	}
        }
    }
    
    /**
     * This function returns the partitionplan to be used for the DWHType
     * 
     * @param type
     *          Dwhtype object containing values of the target Dwhtype.
     * @return Returns the value of PartitionSize to use.
     * @throws Exception
     */
    private List<Partitionplan> getPartitionPlan(final Dwhtype type) throws SQLException, RockException,
        Exception {

      final String partitionPlan = type.getPartitionplan();

      if (partitionPlan == null || partitionPlan.equalsIgnoreCase("")) {
        System.out.println("Partitionplan for type " + type.getTypename() + " is undefined.");
        throw new Exception("Partitionplan for type " + type.getTypename() + " is undefined.");
      }

      final Partitionplan wherePartitionPlan = new Partitionplan(dwhrepRockFactory);
      wherePartitionPlan.setPartitionplan(partitionPlan);
      final PartitionplanFactory partitionPlanFactory = new PartitionplanFactory(dwhrepRockFactory, wherePartitionPlan);
      final List<Partitionplan> partitionPlanVector = partitionPlanFactory.get();

      if (partitionPlanVector.size() == 0) {
        System.out.println("No partitionplan named " + partitionPlan + " found for type " + type.getTypename() + ".");
        throw new Exception("No partitionplan named " + partitionPlan + " found for type " + type.getTypename() + ".");
      }

      return partitionPlanFactory.get();
    }
    
    /**
     * @param storageId
     * @throws RockException
     * @throws SQLException
     */
    private List<Dwhpartition> getCurrentPartitions(final String storageId) throws SQLException, RockException {
        final Dwhpartition wherePartitions = new Dwhpartition(dwhrepRockFactory);
        wherePartitions.setStorageid(storageId);
        final DwhpartitionFactory dwhPartitionFactory = new DwhpartitionFactory(dwhrepRockFactory, wherePartitions, "ORDER BY starttime desc");
        return dwhPartitionFactory.get();
    }
    
  
    /**
     * @param storageId
     * @param partitions
     * @throws RockException
     * @throws SQLException
     */
    private String getRolloverPartitionWithLoadOrder(final String storageId, final List<Dwhpartition> partitions) throws RockException,
            SQLException {
		String currentTableName = null;
		String nextTableName = null;
        Dwhpartition table = partitions.get(0);
        Integer partitionNo = table.getLoadorder();
       
        if (partitionNo == null || partitionNo == 0) {
        	nextTableName = table.getTablename();
        	partitionNo = 1;
        	tablesToTruncate.put(nextTableName, partitionNo);
        	
        }else{
        	currentTableName = table.getTablename();
        	System.out.println("Current Active Partition: " + currentTableName);
            table = partitions.get(partitions.size() - 1);         
            nextTableName = table.getTablename();
            tablesToTruncate.put(nextTableName, ++partitionNo);
         }   
        
        // Create a list of tables that need to be truncated and partition
        return nextTableName;
    }
      
    public void loadTable(final String physicalTableName, final String storageId) throws SQLException,IOException {
    	final Integer newPartitionNo = tablesToTruncate.get(physicalTableName);

    	if (newPartitionNo != null) {
    		// Truncate table and update partition number
    		truncateTable(physicalTableName);
    		try {
    			updateLoadOrder(storageId, physicalTableName, newPartitionNo);
    		} catch (RockException re) {
    			throw new SQLException("Unable to update load order", re);
    		}
    	}
    }
    
    
    public void updateLoadOrder(final String storageID, final String tableName, final int loadOrder) throws RockException, SQLException {
    	
    	final Dwhpartition part = new Dwhpartition(dwhrepRockFactory, tableName);
        part.setLoadorder(loadOrder);
        part.setStarttime(new Timestamp(System.currentTimeMillis()));
        part.updateDB();
        System.out.println("Next Active Partition is: " +tableName+ " with updated Loadorder :" +loadOrder);
    }
    
    /**
     * Truncate a table. The current transaction will be implicitly committed.
     * 
     * @param tableName
     * @return
     * @throws SQLException
     *             - if truncation fails or throws an error.
     */
    private void truncateTable(final String tableName) throws SQLException {

        Statement truncateTable = null;
        try {
            truncateTable = dwhRockFactory.getConnection().createStatement();
            truncateTable.executeUpdate("TRUNCATE TABLE " + tableName);
            truncateTable.executeUpdate("rebuild_idx " + tableName);
            System.out.println("Successfully truncated and retier partition " + tableName);
        } finally {
            try {
                if (truncateTable != null) {
                    truncateTable.close();
                }
            } catch (final SQLException e) {
            	throw new SQLException("Cleanup failed", e);
            }
        }
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