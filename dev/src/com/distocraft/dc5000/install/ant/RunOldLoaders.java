/*------------------------------------------------------------------------
 *
 *
 *      COPYRIGHT (C)                   ERICSSON RADIO SYSTEMS AB, Sweden
 *
 *      The  copyright  to  the document(s) herein  is  the property of
 *      Ericsson Radio Systems AB, Sweden.
 *
 *      The document(s) may be used  and/or copied only with the written
 *      permission from Ericsson Radio Systems AB  or in accordance with
 *      the terms  and conditions  stipulated in the  agreement/contract
 *      under which the document(s) have been supplied.
 *
 *------------------------------------------------------------------------
 */

package com.distocraft.dc5000.install.ant;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.common.ServicenamesHelper;
import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;

public class RunOldLoaders {

  private static final String MEDIATION_TECHPACK = "MediationTechpack";

  private final static String LOADER_TYPE = "Loader";

  protected String propFilepath = "";

  private final String techpackName;

  private final String ENIQ_EVENT = "ENIQ_EVENT";

  private final static String DIM_TECHPACK = "DIM";

  private final RockFactory dwhrepRockFactory;

  private final RockFactory etlrepRockFactory;

  private boolean topologyTechpack;

  private String serverHostName;

  private int serverPort;

  private String serverRefName;
  
  private String sqlGetLoaderSets;
  
  public RunOldLoaders(final String techpackName, final RockFactory etlrepRockFactory,
      final RockFactory dwhrepRockFactory) {
    this.techpackName = techpackName;
    this.etlrepRockFactory = etlrepRockFactory;
    this.dwhrepRockFactory = dwhrepRockFactory;
  }

  /**
   * Runs the Loader and Counter sets for the techpack if there has been an
   * upgrade.
   * 
   * @throws BuildException
   */
  public void execute() throws BuildException {
    if (techpackName.equalsIgnoreCase(MEDIATION_TECHPACK)) {
      runAllLoaderSetsForMediationTp();
    } else {
      runLoaderSetsForSpecificTp();
    }

  }

  private void runLoaderSetsForSpecificTp() {

    isTopologyTechpack();

    if (!topologyTechpack) {
    	sqlGetLoaderSets = "select distinct case"
    			+ " when foldername like '%DC_%' then 'Loader_' || foldername"
    			+ " when foldername like '%DIM_%' then 'TopologyLoader_' || foldername"
    			+" END as Collection_Name"
    			+ " from dataformat where versionid in "
    			+ "(select distinct versionid from tpactivation where status = 'active' and techpack_name = '?')";  
    	runLoaderSets(techpackName, sqlGetLoaderSets, dwhrepRockFactory);
    }
  }


  private void runAllLoaderSetsForMediationTp() {
    System.out.println("Techpack is a mediation techpack");

    //Get all techpacks starting with EVENT_E
    final String sqlGetTechpackNames = "select distinct(COLLECTION_SET_NAME) From Meta_Collection_Sets "
        + "where ENABLED_FLAG = 'Y' and TYPE = 'Techpack' and COLLECTION_SET_NAME like 'EVENT_E_%'";
    final List<String> tpNames = getTechpackNames(sqlGetTechpackNames);
    sqlGetLoaderSets = " Select Collection_Name From Meta_Collections "
	        + " Where Collection_name like '%Loader_%' And Enabled_Flag = 'Y' And "
	        + "Collection_Set_ID  =  (    Select Collection_Set_ID  From Meta_Collection_Sets "
	        + " Where Collection_Set_Name  = '?' " + " And Enabled_Flag = 'Y')";
    
    //Run loader set for each techpack
    for (String tpName : tpNames) {
    	
      runLoaderSets(tpName, sqlGetLoaderSets, etlrepRockFactory);
    }
  }

  private void runLoaderSets(final String tpName, String sqlGetLoaderSets, RockFactory rf) {
    final List<String> loaderSets = getLoaderSets(sqlGetLoaderSets, rf, tpName);

    if (loaderSets != null && !loaderSets.isEmpty()) {
      System.out.println("Loader Sets will be run for " + tpName + " before continuing with the Installation.");
      startSets(loaderSets, tpName);
    }
  }

  /**
   * Starts the Loader and Counter sets for this techpack
   * 
   * @param loaderSets
   * @throws BuildException
   */
  private void startSets(final List<String> loaderSets, final String tpName) throws BuildException {
    setEngineConnectionProperties();

    final Iterator<String> loaderIterator = loaderSets.iterator();
    try {
      final ITransferEngineRMI engine = connect();
      while (loaderIterator.hasNext()) {
        engine.execute(tpName, loaderIterator.next().toString(), "force=true\nsetListener=true");
      }

      checkLoaderSetsInExecSlots(engine, tpName);
    } catch (Exception e) {
      throw new BuildException("Unable to connect to the Transfer Engine");
    }
  }

  /**
   * @param engine
   */
  private void checkLoaderSetsInExecSlots(final ITransferEngineRMI engine, final String tpName) {
    System.out.println("Checking if Loader sets exist in the execution slots for: " + this.techpackName);
    boolean loaderSetsExecuting = areSetsExecuting(LOADER_TYPE, engine, tpName);
    // check if the loader sets are still executing in the execution slots
    while (loaderSetsExecuting) {
      System.out.println("Loader sets found in execution, wait for 3 seconds");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        throw new BuildException("Thread Interrupted while waiting for Loader Sets to finnish executing");
      }
      loaderSetsExecuting = areSetsExecuting(LOADER_TYPE, engine, tpName);
    }
  }

  /**
   * Checks if the sets (Loader or Counter) are still in the execution slots
   * 
   * @param sets
   * @param engine
   * @return setsExecuting
   */
  private boolean areSetsExecuting(final String setType, final ITransferEngineRMI engine, final String tpName) {
    boolean setsExecuting = false;
    try {
      final List<Map<String, String>> runningSets = engine.getRunningSets();
      final Iterator<Map<String, String>> iterator = runningSets.iterator();

      while (iterator.hasNext()) {
        final Map<String, String> setMap = iterator.next();

        final String techpack = setMap.get("techpackName");
        final String setTypeInSlot = setMap.get("setType");

        if (tpName.equals(techpack) && setTypeInSlot.startsWith(setType)) {
          setsExecuting = true;
          break;
        }
      }
    } catch (RemoteException e) {
      throw new BuildException("Could not connect to Transfer Engine.");
    }

    return setsExecuting;
  }

  /**
   * Looks up the transfer engine
   * 
   * @throws NotBoundException
   * @throws RemoteException
   * @throws MalformedURLException
   */
  private ITransferEngineRMI connect() throws MalformedURLException, RemoteException, NotBoundException {

    final String rmiURL = "//" + serverHostName + ":" + serverPort + "/" + serverRefName;

    System.out.println("Connecting engine @ " + rmiURL);

    final ITransferEngineRMI termi = (ITransferEngineRMI) Naming.lookup(rmiURL);

    return termi;
  }

  /**
   * sets the engine connection settings
   * 
   * @throws BuildException
   */
  private void setEngineConnectionProperties() throws BuildException {
    try {

      String sysPropDC5000 = System.getProperty("dc5000.config.directory");
      if (sysPropDC5000 == null) {
        sysPropDC5000 = "/eniq/sw/conf";
      }

      if (!sysPropDC5000.endsWith(File.separator)) {
        sysPropDC5000 += File.separator;
      }

      final FileInputStream streamProperties = new FileInputStream(sysPropDC5000 + "ETLCServer.properties");
      final Properties appProps = new Properties();
      appProps.load(streamProperties);

     // Get the host name by service name and default to localhost
      String hostNameByServiceName = null ;
      try{
    	  hostNameByServiceName = ServicenamesHelper.getServiceHost("engine", "localhost");
      }catch(final Exception e){
    	  hostNameByServiceName = "localhost" ;
      }
	  this.serverHostName = appProps.getProperty("ENGINE_HOSTNAME", hostNameByServiceName);
    
      final String sporttmp = appProps.getProperty("ENGINE_PORT", "1200");
      try {
        this.serverPort = Integer.parseInt(sporttmp);
      } catch (final NumberFormatException nfe) {
        throw new BuildException("Engine Port in a numerical format.");
      }

      this.serverRefName = appProps.getProperty("ENGINE_REFNAME", "TransferEngine");

      streamProperties.close();

    } catch (final Exception e) {
      System.err.println("Cannot read configuration: " + e.getMessage());
    }
  }

  /**
   * Gets the loader sets that need to run.
 * @param tpName 
   * 
   */
  private List<String> getLoaderSets(final String sqlGetLoaderSets, RockFactory rf, String tpName) {

    final List<String> setsToLoad = new ArrayList<String>();

    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      statement = rf.getConnection().prepareStatement(sqlGetLoaderSets);
      statement.setString(1, tpName);
      resultSet = statement.executeQuery();

      while (resultSet.next()) {
        final String loaderSets = resultSet.getString("Collection_Name");
        setsToLoad.add(loaderSets);
      }
    } catch (SQLException se) {
      throw new BuildException("Unable to run SQL statement: " + sqlGetLoaderSets);
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
      resultSet = null;
      statement = null;
    }

    return setsToLoad;
  }

  /**
   * Gets the loader sets that need to run.
   * 
   */
  private List<String> getTechpackNames(final String sqlGetTechpackNames) {

    final List<String> techpackNames = new ArrayList<String>();

    Statement statement = null;
    ResultSet resultSet = null;

    try {
      statement = etlrepRockFactory.getConnection().createStatement();
      resultSet = statement.executeQuery(sqlGetTechpackNames);

      while (resultSet.next()) {
        final String tpName = resultSet.getString("COLLECTION_SET_NAME");
        techpackNames.add(tpName);
      }
    } catch (SQLException se) {
      throw new BuildException("Unable to run SQL statement: " + sqlGetTechpackNames);
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
      resultSet = null;
      statement = null;
    }

    return techpackNames;
  }

  /**
   * Checks if this is a Topology Techpack
   */
  private void isTopologyTechpack() {
    if (techpackName.startsWith(DIM_TECHPACK)) {
      topologyTechpack = true;
    } else {
      topologyTechpack = false;
    }
  }
}
