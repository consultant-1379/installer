/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.distocraft.dc5000.install.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.tools.ant.BuildException;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.common.Properties;
import com.distocraft.dc5000.common.StaticProperties;
import com.distocraft.dc5000.dwhm.StorageTimeAction;
import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.rock.Meta_collection_sets;
import com.distocraft.dc5000.etl.rock.Meta_collection_setsFactory;
import com.distocraft.dc5000.etl.rock.Meta_collections;
import com.distocraft.dc5000.etl.rock.Meta_collectionsFactory;
import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.distocraft.dc5000.etl.rock.Meta_databasesFactory;
import com.distocraft.dc5000.repository.cache.ActivationCache;
import com.distocraft.dc5000.repository.dwhrep.Techpackdependency;
import com.distocraft.dc5000.repository.dwhrep.TechpackdependencyFactory;
import com.distocraft.dc5000.repository.dwhrep.Tpactivation;
import com.distocraft.dc5000.repository.dwhrep.TpactivationFactory;

/**
 * This is custom made ANT task that restores the DWHDB database using the
 * information from repdb database
 * 
 * @author epaujor
 * 
 */
public class RestoreDwhDatabase extends CommonTask {

  private static final String AND_COLLECTION_NAME_LIKE_DWHM_INSTALL = "AND COLLECTION_NAME like 'DWHM_Install_%'";

  private static final String AND_COLLECTION_NAME_LIKE_DIRECTORY_CHECKER = "AND COLLECTION_NAME like 'Directory_Checker_%'";
 
  private static final String ENABLED_FLAG = "Y";

  private static final String USER = "USER";
  
  private static final String DBAUSER = "DBA";

  private static final String DWHREP = "dwhrep";

  private static final String DWH = "dwh";
  
  private static final int NUMBEROFDAYS = 5;
  
  private static final String LOGDATE = "dd.MM HH:mm:ss";

  private RockFactory etlrepRockFactory = null;

  private RockFactory dwhrepRockFactory = null;

  private RockFactory dwhdbRockFactory = null;
  
  private RockFactory dwhdbDBARockFactory = null;

  private String binDirectory = new String();

  private String installDirectory = new String();

  private List<String> activeTechpackNames;
  
  private static final DateFormat logDateFormat = new SimpleDateFormat(LOGDATE);
  
  private static final String ACTIVERESTOREFILE = "/eniq/sw/installer/nodata_active_restore";
  
  private static final String RESTORELISTREGEX = "([A-Z]|[a-z])\\w+=([executed]|[pending]|[EXECUTED]|[PENDING])+";
  
  private static PrintStream print = new PrintStream(System.out){
	  
	  @Override
	  public void println(String log){
		  super.println(logDateFormat.format(Calendar.getInstance().getTime())+" "+log);
	  }
	  
  };
  
  private static Logger LOGGER = null;
  
  static {
      Logger mainLogger = Logger.getLogger("com.active.Restore");
      mainLogger.setUseParentHandlers(false);
      ConsoleHandler handler = new ConsoleHandler();
      handler.setFormatter(new SimpleFormatter() {
          private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

          @Override
          public synchronized String format(LogRecord lr) {
              return String.format(format,
                      new Date(lr.getMillis()),
                      lr.getLevel().getLocalizedName(),
                      lr.getMessage()
              );
          }
      });
      mainLogger.addHandler(handler);
      LOGGER =Logger.getLogger(RestoreDwhDatabase.class.getName());
}

  /**
   * This function restores the DWHDB database.
   */
  /* (non-Javadoc)
 * @see org.apache.tools.ant.Task#execute()
 */
@Override
  public void execute() throws BuildException {
	final long pgmStartTime = System.nanoTime();
	System.setProperty("dc5000.config.directory", "/eniq/sw/conf/");
	System.setProperty("CONF_DIR", "/eniq/sw/conf/");
	
	List<String> restrictedTableLevels = new ArrayList<String>();
	restrictedTableLevels.add("DAY");
	restrictedTableLevels.add("DAYBH");
	restrictedTableLevels.add("RANKBH");
	restrictedTableLevels.add("COUNT");

    final ITransferEngineRMI engineRMI = connectEngine();
    if (!binDirectory.endsWith(File.separator)) {
      // Add the missing separator char "/" from the end of the directory
      // string.
      binDirectory = this.binDirectory + File.separator;
    }

    if (!installDirectory.endsWith(File.separator)) {
      // Add the missing separator char "/" from the end of the directory
      // string.
      installDirectory = this.installDirectory + File.separator;
    }
    

    createDatabaseConnections();
    ActivationCache.initialize(etlrepRockFactory);
    try {
		StaticProperties.reload();
		Properties.reload();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		throw new BuildException(e);
	}    
    
    activeTechpackNames = getActiveTechpacks();

    /*print.println("Checking if tables already exist in dwhdb database for user 'dc'...");
    checkIfTablesAlreadyExist();
    print.println("No tables exist in dwhdb database for user 'dc'.\n");*/

    
	
	BufferedReader br = null;
	InputStream propFile;
	
	
	try {
			br = new BufferedReader(new FileReader(ACTIVERESTOREFILE));

			String sCurrentLine;
			
			if(checkIfTablesAlreadyExist()){
				print.println("Clearing DWHPartition, ExternalStatementStatus and CountingManagement tables in dwhrep...");
			    truncateDwhrepTable("truncate table DWHPartition");
			    truncateDwhrepTable("truncate table ExternalStatementStatus");
			    truncateDwhrepTable("truncate table CountingManagement");
			    print.println("Finished clearing DWHPartition, ExternalStatementStatus and CountingManagement tables in dwhrep.\n");
			} else {
				print.println("More than 5 dc tables are available in dwhdb!! So not clearing the dwhpartition, ExternalStatementStatus, CountingManagement tables.");
			}

			while ((sCurrentLine = br.readLine()) != null && sCurrentLine.matches(RESTORELISTREGEX)) {
				final String teckpackName = sCurrentLine.split("=")[0];	
				if(sCurrentLine.contains("=executed")){
					print.println(teckpackName+" has been already executed. Skipping the TECHPACK for active restore...");
					continue;
				}
				/*print.println("Running the Directory_Checker set for "+teckpackName+"...");
				runDirCheckerSetsForTechpacks(teckpackName, engineRMI);*/
			    
				if(teckpackName.equals("DWH_BASE")){
					print.println("Running the DWHM_Install set for DWH_BASE...");
				    runDWHMInstallSets(engineRMI,"DWH_BASE");
				    modifyProperty(ACTIVERESTOREFILE,sCurrentLine,teckpackName+"=executed");
				    continue;
				}
				
				if(teckpackName.equals("DWH_MONITOR")){
					print.println("Running the DWHM_Install set for DWH_MONITOR...");
				    runDWHMInstallSets(engineRMI,"DWH_MONITOR");
				    modifyProperty(ACTIVERESTOREFILE,sCurrentLine,teckpackName+"=executed");
				    continue;
				}			    
				
				print.println("Running the Custom Installation for "+teckpackName+" sets...");
    	final long starttime = System.nanoTime();
    	try {
			new StorageTimeAction(restrictedTableLevels, NUMBEROFDAYS, dwhrepRockFactory, etlrepRockFactory, dwhdbRockFactory, dwhdbDBARockFactory, teckpackName, LOGGER, false);
			modifyProperty(ACTIVERESTOREFILE,sCurrentLine,teckpackName+"=executed");	
			DBConnectionReseter();		
		} catch (Exception e1) {
			print.println(e1);
			throw new BuildException(e1);
		}   	
    	final long endtime = System.nanoTime();
    	print.println("Finished the Custom Installation for "+teckpackName+" set"+" with "+(double)(endtime - starttime)/1000000000.0+" seconds");
			}

		} catch (Exception e) {
			print.println(e);
			throw new BuildException(e);
		} finally {

			try {
				if (br != null)
					br.close();
			} catch(IOException ex) {
				print.println(ex);
				throw new BuildException(ex);
			}

		}
	
	/*print.println("Running Directory checker sets for all the active INTERFACES...");
    final long interfaceStartTime = System.nanoTime();
    List<String> listOfSetNames = listOfDirCheckerEnabledTPs();
    for(String setName : listOfSetNames){
    	if(setName.equalsIgnoreCase("AlarmInterfaces") || !activeTechpackNames.contains(setName)){
    		print.println("Triggering Directory checker sets for "+setName+"...");
    		runDirCheckerSetsForTechpacks(setName,engineRMI);
    	}
    }
    final long interfaceEndtime = System.nanoTime();
    print.println("Finished Directory_Checker set for all the Interface set by "+(double)(interfaceEndtime - interfaceStartTime)/1000000000.0+" seconds");*/
    
    final long pgmEndTime = System.nanoTime();
    print.println("Total time taken is "+(double)(pgmEndTime - pgmStartTime)/1000000000.0+" seconds.");
    
    /*for(String teckpackName : activeTechpackNames){
	
    }*/

    /*print.println("Running the DWHM_Install sets...");
    runDWHMInstallSets(engineRMI);
    print.println("Finished running the DWHM_Install sets.\n");
*/
    //print.println(" Activating interfaces...");
    //reactivateInterfaces();

    /*print.println("Running the restore sets...");
    runRestoreSets(engineRMI);
    print.println("Restore sets are currently running. Please check AdminUI to see if they ran successfully.");*/
  }

	private void DBConnectionReseter(){
		try {
			etlrepRockFactory.getConnection().close();
			dwhrepRockFactory.getConnection().close();
			dwhdbRockFactory.getConnection().close();
			dwhdbDBARockFactory.getConnection().close();
			createDatabaseConnections();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			print.println(e);
			try {
				etlrepRockFactory.getConnection().close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				print.println(e1);
			}
			try {
				dwhrepRockFactory.getConnection().close();
			} catch (SQLException e2) {
				// TODO Auto-generated catch block
				print.println(e2);
			}
			try {
				dwhdbRockFactory.getConnection().close();
			} catch (SQLException e3) {
				// TODO Auto-generated catch block
				print.println(e3);
			}
			try {
				dwhdbDBARockFactory.getConnection().close();
			} catch (SQLException e4) {
				// TODO Auto-generated catch block
				print.println(e4);
			}
			
			throw new BuildException(e);
		}
	}
	
	public List<String> listOfDirCheckerEnabledTPs(){
		List<String> setList = new ArrayList<String>();
		ResultSet resultSet = null;
	    Statement statement = null;
	    try {
	    	statement = etlrepRockFactory.getConnection().createStatement();
	        final String sqlQuery = "SELECT distinct mcs.COLLECTION_SET_NAME as set_name FROM META_COLLECTION_SETS mcs, META_COLLECTIONS mc "
	        		+ "WHERE mcs.ENABLED_FLAG = 'Y' AND mcs.COLLECTION_SET_ID = mc.COLLECTION_SET_ID AND mc.COLLECTION_NAME LIKE 'Directory_Checker_%'";

	      resultSet = statement.executeQuery(sqlQuery);
	      while(resultSet.next()){
	    	  setList.add(resultSet.getString("set_name"));
	      }

	      
	    } catch (SQLException e) {
	      throw new BuildException("Failed to fetch INTERFACE list from ETLREP.", e);
	    } finally {
	      try {
	        if (resultSet != null) {
	          resultSet.close();
	        }
	        if (statement != null) {
	          statement.close();
	        }
	      } catch (SQLException e) {
	        print.println("Issue closing statement/resultSet. error: "+e);
	      }
	    }
		return setList; 
	}
	
	public void modifyProperty(String filePath, String oldString, String newString)
	{
		File fileToBeModified = new File(filePath);
		
		String oldContent = "";
		
		BufferedReader reader = null;
		
		FileWriter writer = null;
		
		try 
		{
			reader = new BufferedReader(new FileReader(fileToBeModified));
			
			//Reading all the lines of input text file into oldContent
			
			String line = reader.readLine();
			
			while (line != null) 
			{
				oldContent = oldContent + line + System.lineSeparator();
				
				line = reader.readLine();
			}
			
			//Replacing oldString with newString in the oldContent
			
			String newContent = oldContent.replaceAll(oldString, newString);
			
			//Rewriting the input text file with newContent
			
			writer = new FileWriter(fileToBeModified);
			
			writer.write(newContent);
		}
		catch (IOException e)
		{
			throw new BuildException(e);
		}
		finally
		{
			try 
			{
				//Closing the resources
				
				reader.close();
				
				writer.close();
			} 
			catch (IOException e) 
			{
				throw new BuildException(e);
			}
		}
	}
  

  /**
   *
   */
  private void reactivateInterfaces() {

    int indexHypen;
    String interfaceAbsName = null;
    String ossName = null;
    final StringBuilder activateInterfaceCommand = new StringBuilder();

    print.println("Reactivating each interfaces.");

    try {
      final Vector<Meta_collection_sets> activeInterfaceNames = getActiveInterfaces();

      if (activeInterfaceNames.size() > 0) {
        print.println("ActiveInterfaceNames are as below\n");

        for (Meta_collection_sets interfaceName : activeInterfaceNames) {
          print.println(interfaceName.getCollection_set_name());
        }

        for (Meta_collection_sets interfaceName : activeInterfaceNames) {

          final String interfaceNameWithOssName = interfaceName.getCollection_set_name();
          if(interfaceNameWithOssName.indexOf("-")>=0 && interfaceNameWithOssName.toString().contains("INTF_DC_E_IMS_PGM_AP-WUIGM"))
        	{
        	  interfaceAbsName="INTF_DC_E_IMS_PGM_AP-WUIGM";
        	  ossName = interfaceNameWithOssName.substring(interfaceAbsName.length()+1);
        	}else{
        		indexHypen = interfaceNameWithOssName.indexOf("-");
        		if (indexHypen > 0) {
        			interfaceAbsName = interfaceNameWithOssName.substring(0, indexHypen);
        			ossName = interfaceNameWithOssName.substring(indexHypen + 1);
        		}
        	}
            print.println("Attempting to activate interface " + interfaceAbsName + " for OSS " + ossName);

            activateInterfaceCommand.append(installDirectory);
            activateInterfaceCommand.append("activate_interface -o ");
            activateInterfaceCommand.append(ossName);
            activateInterfaceCommand.append(" -i ");
            activateInterfaceCommand.append(interfaceAbsName);

            print.println(runCommand(activateInterfaceCommand.toString()));
            print.println(activateInterfaceCommand.toString() + "ran successfully.");

            activateInterfaceCommand.delete(0, activateInterfaceCommand.length());

          }
        }else {
            print.println("No ActiveInterfaceNames found. Exiting");
        }

      } catch (SQLException e) {
      e.printStackTrace();
    } catch (RockException e) {
      e.printStackTrace();
    }

  }

  /**
   * @return
   * @throws RockException
   * @throws SQLException
   */
  private Vector<Meta_collection_sets> getActiveInterfaces() throws SQLException, RockException {

    final Meta_collection_sets whereMetaCollSet = new Meta_collection_sets(this.etlrepRockFactory);
    whereMetaCollSet.setType("Interface");
    whereMetaCollSet.setEnabled_flag(ENABLED_FLAG);

    final Meta_collection_setsFactory metaCollSetsFactory = new Meta_collection_setsFactory(this.etlrepRockFactory,
        whereMetaCollSet);

    return metaCollSetsFactory.get();
  }

  /**
   * Runs the Directory_Checker sets for all the active techpacks
   * 
   * @throws BuildException
   */
  private void runDirCheckerSetsForTechpacks(final ITransferEngineRMI engineRMI) {
    for (String techpackName : activeTechpackNames) {
      if (techpackName.contains("DWH_BASE")) {
        continue; // DWH_BASE does not have a Directory Checker set.
      }

      try {
        final List<Meta_collection_sets> metaCollSetForTps = getActiveMetaCollectionSetsForTp(techpackName);

        if (metaCollSetForTps.size() > 0) {
          final List<Meta_collections> dirCheckerCollections = getActiveCollectionForWhereClause(metaCollSetForTps,
              AND_COLLECTION_NAME_LIKE_DIRECTORY_CHECKER);
          if (dirCheckerCollections.size() > 0) {
            for (Meta_collections dirCheckerCol : dirCheckerCollections) {
              final String directoryCheckerName = dirCheckerCol.getCollection_name();
              print.println("Attempting to run " + directoryCheckerName + " ...");
              startAndWaitSet(engineRMI, techpackName, directoryCheckerName);
              print.println(directoryCheckerName + " ran successfully.");
            }
          }
        }
      } catch (Exception e) {
        print.println("Exception seen while running directory checker set(s) for techpack: '" + techpackName
            + "'. error:"+e);
      }
    }
  }
  
  /**
   * Runs the Directory_Checker sets for all the active techpacks
   * 
   * @throws BuildException
   */
  private void runDirCheckerSetsForTechpacks(final String techpackName,final ITransferEngineRMI engineRMI) {
      if (techpackName.contains("DWH_BASE")) {
    	print.println("Skipping Directory_Checker for DWH_BASE techpack as it does not have the set..");
        return; // DWH_BASE does not have a Directory Checker set.
      }

      try {
        final List<Meta_collection_sets> metaCollSetForTps = getActiveMetaCollectionSetsForTp(techpackName);

        if (metaCollSetForTps.size() > 0) {
          final List<Meta_collections> dirCheckerCollections = getActiveCollectionForWhereClause(metaCollSetForTps,
              AND_COLLECTION_NAME_LIKE_DIRECTORY_CHECKER);
          if (dirCheckerCollections.size() > 0) {
            for (Meta_collections dirCheckerCol : dirCheckerCollections) {
              final String directoryCheckerName = dirCheckerCol.getCollection_name();
              print.println("Attempting to run " + directoryCheckerName + " ...");
              startAndWaitSet(engineRMI, techpackName, directoryCheckerName);
              print.println(directoryCheckerName + " ran successfully.");
            }
          }
        }
      } catch (Exception e) {
        print.println("Exception seen while running directory checker set(s) for techpack: '" + techpackName
            + "'. error:"+e);
      }
  }
  
  /**
   * Runs the DWHM_Install sets for all the active techpacks
   * 
   * @throws BuildException
   */
  private void runDWHMInstallSets(final ITransferEngineRMI engineRMI) throws BuildException {
    for (String techpackName : activeTechpackNames) {
      try {
        final List<Meta_collection_sets> metaCollSetForTps = getActiveMetaCollectionSetsForTp(techpackName);

        if (metaCollSetForTps.size() > 0) {
          final List<Meta_collections> dwhmInstallCollections = getActiveCollectionForWhereClause(metaCollSetForTps,
              AND_COLLECTION_NAME_LIKE_DWHM_INSTALL);
          if (dwhmInstallCollections.size() > 0) {
            for (Meta_collections dwhmInstallCol : dwhmInstallCollections) {
              final String dwhmInstallName = dwhmInstallCol.getCollection_name();
              print.println("Attempting to run " + dwhmInstallName + " ...");
              startAndWaitSet(engineRMI, techpackName, dwhmInstallName);
              print.println(dwhmInstallName + " ran successfully.");
            }
          }
        }
      } catch (Exception e) {
        print.println("Exception seen while running DWHM_Install set(s) for techpack: '" + techpackName + "'. error"+e);
      }
    }
  }
  
  /**
   * Runs the DWHM_Install sets for specified TECHPACK
   * 
   * @throws BuildException
   */
  private void runDWHMInstallSets(final ITransferEngineRMI engineRMI,String techpackName) throws BuildException {
      try {
        final List<Meta_collection_sets> metaCollSetForTps = getActiveMetaCollectionSetsForTp(techpackName);
        if (metaCollSetForTps.size() > 0) {
          final List<Meta_collections> dwhmInstallCollections = getActiveCollectionForWhereClause(metaCollSetForTps,
              AND_COLLECTION_NAME_LIKE_DWHM_INSTALL);
          if (dwhmInstallCollections.size() > 0) {
            for (Meta_collections dwhmInstallCol : dwhmInstallCollections) {
              final String dwhmInstallName = dwhmInstallCol.getCollection_name();
              print.println("Attempting to run " + dwhmInstallName + " ...");
              startAndWaitSet(engineRMI, techpackName, dwhmInstallName);
              print.println(dwhmInstallName + " ran successfully.");
            }
          }
        }
      } catch (Exception e) {
        print.println("Exception seen while running DWHM_Install set(s) for techpack: '" + techpackName + "'. error"+e);
      }
  }


  /**
   * @param metaCollSetForTps
   * @return
   * @throws SQLException
   * @throws RockException
   */
  protected List<Meta_collections> getActiveCollectionForWhereClause(final List<Meta_collection_sets> metaCollSetForTps,
      final String whereClause) throws SQLException, RockException {
    final Meta_collection_sets metaCollSetForTp = metaCollSetForTps.get(0);

    final Meta_collections whereColl = new Meta_collections(etlrepRockFactory);
    whereColl.setCollection_set_id(metaCollSetForTp.getCollection_set_id());
    whereColl.setVersion_number(metaCollSetForTp.getVersion_number());
    whereColl.setEnabled_flag(ENABLED_FLAG);

    final Meta_collectionsFactory metCollFactory = new Meta_collectionsFactory(etlrepRockFactory, whereColl,
        whereClause);
    return metCollFactory.get();
  }

  /**
   * @param techpackName
   * @return
   * @throws SQLException
   * @throws RockException
   */
  private List<Meta_collection_sets> getActiveMetaCollectionSetsForTp(final String techpackName) throws SQLException,
      RockException {
    final Meta_collection_sets whereMetaCollSet = new Meta_collection_sets(etlrepRockFactory);
    whereMetaCollSet.setCollection_set_name(techpackName);
    whereMetaCollSet.setEnabled_flag(ENABLED_FLAG);
    final Meta_collection_setsFactory metaCollSetFactory = new Meta_collection_setsFactory(etlrepRockFactory,
        whereMetaCollSet);
    return metaCollSetFactory.get();
  }

  /**
   * Create database connections to ETLREP, DWHREP and DWHDB
   */
  private void createDatabaseConnections() {
    print.println("Checking USER connection to etlrep database...");
    final Map<String, String> databaseConnectionDetails = getDatabaseConnectionDetails();
    // Create the connection to the etlrep.
    etlrepRockFactory = createEtlrepRockFactory(databaseConnectionDetails);
    print.println("Connection to etlrep database as USER created.\n");

    print.println("Checking USER connection to dwhrep database...");
    // Create also the connection to dwhrep.
    dwhrepRockFactory = createDwhrepRockFactory(DWHREP);
    print.println("Connection to dwhrep database as USER created.\n");

    print.println("Checking USER connection to dwhdb database...");
    // Create also the connection to dwhrep.
    dwhdbRockFactory = createDwhrepRockFactory(DWH);
    print.println("Connection to dwhdb database as USER created.");
    
    print.println("Checking DBA connection to dwhdb database...");
    // Create also the connection to dwhrep.
    dwhdbDBARockFactory = createDBARockFactory(DWH);
    print.println("Connection to dwhdb database as DBA created.");
  }

  /**
   * This function creates the rockfactory object to etlrep from the database
   * connection details read from ETLCServer.properties file.
   * 
   * @param databaseConnectionDetails
   * @return Returns the created RockFactory.
   */
  private RockFactory createEtlrepRockFactory(final Map<String, String> databaseConnectionDetails)
      throws BuildException {

    RockFactory rockFactory = null;
    final String databaseUsername = databaseConnectionDetails.get("etlrepDatabaseUsername").toString();
    final String databasePassword = databaseConnectionDetails.get("etlrepDatabasePassword").toString();
    final String databaseUrl = databaseConnectionDetails.get("etlrepDatabaseUrl").toString();
    final String databaseDriver = databaseConnectionDetails.get("etlrepDatabaseDriver").toString();

    try {
      rockFactory = new RockFactory(databaseUrl, databaseUsername, databasePassword, databaseDriver, "PreinstallCheck",
          true);
    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Unable to initialize database connection.", e);
    } finally {
      if (rockFactory == null) {
        throw new BuildException(
            "Unable to initialize database connection. Please check the settings in the ETLCServer.properties file.");
      }
    }
    return rockFactory;
  }

  /**
   * This function creates the RockFactory to dwhrep. The created RockFactory is
   * inserted in class variable dwhrepRockFactory.
   */
  private RockFactory createDwhrepRockFactory(final String connectionName) throws BuildException {
    RockFactory rockFactory = null;
    try {
      final Meta_databases whereMetaDatabases = new Meta_databases(this.etlrepRockFactory);
      whereMetaDatabases.setConnection_name(connectionName);
      whereMetaDatabases.setType_name(USER);
      final Meta_databasesFactory metaDatabasesFactory = new Meta_databasesFactory(this.etlrepRockFactory,
          whereMetaDatabases);
      final Vector<Meta_databases> metaDatabases = metaDatabasesFactory.get();

      if (metaDatabases != null && metaDatabases.size() == 1) {
        final Meta_databases targetMetaDatabase = metaDatabases.get(0);
        rockFactory = new RockFactory(targetMetaDatabase.getConnection_string(), targetMetaDatabase.getUsername(),
            targetMetaDatabase.getPassword(), etlrepRockFactory.getDriverName(), "PreinstallCheck", true);
      } else {
        throw new BuildException("Unable to connect metadata (No dwhrep or multiple dwhreps defined in Meta_databases)");
      }
    } catch (Exception e) {
      throw new BuildException("Creating database connection to dwhrep failed.", e);
    } finally {
      if (rockFactory == null) {
        throw new BuildException("Unable to initialize database connection.");
      }
    }

    return rockFactory;
  }
  
  /**
   * This function creates the RockFactory to dwhrep. The created RockFactory is
   * inserted in class variable dwhrepRockFactory.
   */
  private RockFactory createDBARockFactory(final String connectionName) throws BuildException {
    RockFactory rockFactory = null;
    try {
      final Meta_databases whereMetaDatabases = new Meta_databases(this.etlrepRockFactory);
      whereMetaDatabases.setConnection_name(connectionName);
      whereMetaDatabases.setType_name(DBAUSER);
      final Meta_databasesFactory metaDatabasesFactory = new Meta_databasesFactory(this.etlrepRockFactory,
          whereMetaDatabases);
      final Vector<Meta_databases> metaDatabases = metaDatabasesFactory.get();

      if (metaDatabases != null && metaDatabases.size() == 1) {
        final Meta_databases targetMetaDatabase = metaDatabases.get(0);
        rockFactory = new RockFactory(targetMetaDatabase.getConnection_string(), targetMetaDatabase.getUsername(),
            targetMetaDatabase.getPassword(), etlrepRockFactory.getDriverName(), "PreinstallCheck", true);
      } else {
        throw new BuildException("Unable to connect metadata (No dwhrep or multiple dwhreps defined in Meta_databases)");
      }
    } catch (Exception e) {
      throw new BuildException("Creating database connection to dwhrep failed.", e);
    } finally {
      if (rockFactory == null) {
        throw new BuildException("Unable to initialize database connection.");
      }
    }

    return rockFactory;
  }

  /**
   * Checks if any tables already exist for the user dc. If they do, an
   * exception is thrown.
   * 
   * @throws BuildException
   */
  private boolean checkIfTablesAlreadyExist() throws BuildException {
    ResultSet resultSet = null;
    Statement statement = null;
    try {
      statement = dwhdbRockFactory.getConnection().createStatement();
      final String sqlQuery = "select count(tab.table_name) as count from systable tab, sysuser suser where "
          + "table_type='BASE' and tab.creator = suser.user_id and suser.user_name = 'dc'";

      resultSet = statement.executeQuery(sqlQuery);

      if (resultSet.next()) {
        if (resultSet.getInt("count") < 5) {
          //throw new BuildException("Tables already exist in the dwhdb database for user 'dc'.");
        	return true;
        }
      }
    } catch (SQLException e) {
      throw new BuildException("Failed to check if tables already exist in dwhdb database for user 'dc'.", e);
    } finally {
      try {
        if (resultSet != null) {
          resultSet.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        print.println("Issue closing statement/resultSet. error: "+e);
      }
    }
    return false;
  }

  private void truncateDwhrepTable(final String sql) throws BuildException {
    Statement statement = null;
    try {
      statement = dwhrepRockFactory.getConnection().createStatement();
      statement.executeUpdate(sql);
    } catch (SQLException e) {
      throw new BuildException("Failed to truncate the table", e);
    } finally {
      try {
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        print.println("Issue closing statement. error: "+e);
      }
    }
  }

  /**
   * Get all the active techpacks
   * 
   * @return
   */
  private List<String> getActiveTechpacks() {
    final List<String> orderedActiveTpNames = new ArrayList<String>();
    try {
      final Tpactivation whereTpactivation = new Tpactivation(dwhrepRockFactory);
      whereTpactivation.setStatus("ACTIVE");
      final TpactivationFactory tpactivationFactory = new TpactivationFactory(dwhrepRockFactory, whereTpactivation);
      final Vector<Tpactivation> activeTps = tpactivationFactory.get();

      for (Tpactivation tp : activeTps) {
        final List<Techpackdependency> tpDependencies = getDependentTpsBasedOnVersion(tp.getVersionid());
        addTechpacksInCorrectOrder(tp, orderedActiveTpNames, tpDependencies.listIterator());
      }
    } catch (SQLException e) {
      throw new BuildException("Failed to retreive active techpacks", e);
    } catch (RockException e) {
      throw new BuildException("Failed to retreive active techpacks", e);
    }
    return orderedActiveTpNames;
  }

  private List<Techpackdependency> getDependentTpsBasedOnVersion(final String versionId) throws SQLException,
      RockException {
    final Techpackdependency whereTpDependency = new Techpackdependency(dwhrepRockFactory);
    whereTpDependency.setVersionid(versionId);
    final TechpackdependencyFactory tpDependencyFactory = new TechpackdependencyFactory(this.dwhrepRockFactory,
        whereTpDependency);

    return tpDependencyFactory.get();
  }

  /**
   * Get the list of techpacks in the correct order so that the dependant
   * techpacks are listed first
   * 
   * @param techpack
   * @param orderedActiveTpNames
   * @throws SQLException
   * @throws RockException
   */
  private void addTechpacksInCorrectOrder(final Tpactivation techpack, final List<String> orderedActiveTpNames,
      final Iterator<Techpackdependency> tpDependencies) throws SQLException, RockException {

    if (!orderedActiveTpNames.contains(techpack.getTechpack_name())) {
      while (tpDependencies.hasNext()) {
        final Techpackdependency tp = tpDependencies.next();
        final Tpactivation whereTpactivation = new Tpactivation(dwhrepRockFactory);
        whereTpactivation.setTechpack_name(tp.getTechpackname());

        final TpactivationFactory tpactivationFactory = new TpactivationFactory(dwhrepRockFactory, whereTpactivation);
        final Vector<Tpactivation> activeTps = tpactivationFactory.get();

        for (Tpactivation activeTp : activeTps) {
          tpDependencies.remove();
          addTechpacksInCorrectOrder(activeTp, orderedActiveTpNames, tpDependencies);
        }

      }
      print.println("Adding " + techpack.getTechpack_name() + " to list of techpacks.");
      orderedActiveTpNames.add(techpack.getTechpack_name());
    }
  }

  private void runRestoreSets(final ITransferEngineRMI engineRMI) throws BuildException {
    for (String techpackName : activeTechpackNames) {
      print.println("Attempting to run restore sets for " + techpackName + "...");
      restore(engineRMI, techpackName, "ALL", "2000:01:01", getCurrentDate());
      print.println("Restore sets running for " + techpackName
          + ". Please check AdminUI to see if they ran successfully.");
    }
  }

  private String getCurrentDate() {
    final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy:MM:dd", Locale.getDefault());
    return dateTimeFormat.format(new Date());
  }

  public void setBinDirectory(final String dir) {
    binDirectory = dir;
  }

  public String getBinDirectory() {
    return binDirectory;
  }

  public void setInstallDirectory(final String dir) {
    installDirectory = dir;
  }

  public String getInstallDirectory() {
    return installDirectory;
  }

}
