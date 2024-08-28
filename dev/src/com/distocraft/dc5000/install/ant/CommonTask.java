package com.distocraft.dc5000.install.ant;

import com.distocraft.dc5000.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.engine.system.SetListener;
import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.distocraft.dc5000.etl.rock.Meta_databasesFactory;
import com.distocraft.dc5000.etl.scheduler.ISchedulerRMI;
import com.ericsson.eniq.repository.ETLCServerProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;


/**
 * This is ANT task base class, which can be used for methods which are needed
 * in various custom ant task implemented in the project interface's set to the
 * activated interface.
 *
 * @author etogust
 */
public class CommonTask extends Task {

  private String configurationDirectory;

  /**
   * This function reads the database connection details from the file
   * ${configurationDirectory}/ETLCServer.properties
   *
   * @return Returns a HashMap with the database connection details.
   * @throws BuildException On Errors
   */
  protected Map<String, String> getDatabaseConnectionDetails() throws BuildException {

    ETLCServerProperties props;
    try {
      props = new ETLCServerProperties();
    } catch (IOException e) {
      //getProject().log("Could not read ETLCServer.properties",  e, Project.MSG_ERR);
      throw new BuildException("Could not read ETLCServer.properties", e);
    }

    final Map<String, String> dbConnDetails = props.getDatabaseConnectionDetails();

    // Set the database connection properties as ANT properties.
    for (String property : dbConnDetails.keySet()) {
      final String value = dbConnDetails.get(property);
      getProject().setNewProperty(property, value);
    }

    return dbConnDetails;
  }

  /**
   * Sets the Ant parameter and also the CONF_DIR system property.
   *
   * @param confDir the CONF_DIR value
   */
  public void setConfigurationDirectory(final String confDir) {
    this.configurationDirectory = confDir;

    if (!this.configurationDirectory.endsWith(File.separator)) {
      this.configurationDirectory += File.separator;
    }
    System.setProperty(ETLCServerProperties.CONFIG_DIR_PROPERTY_NAME, this.configurationDirectory);
    System.setProperty(ETLCServerProperties.DC_CONFIG_DIR_PROPERTY_NAME, this.configurationDirectory);
  }

  /**
   * Get the CONF_DIR value
   *
   * @return The value for CONF_DIR
   */
  public String getConfigurationDirectory() {
    return this.configurationDirectory;
  }

  /**
   * This function creates the RockFactory to dwhrep. The created RockFactory is
   * inserted in class variable dwhrepRockFactory.
   *
   * @param etlrep   The ETLREP Connectio to use to look up the DWHREP connection details
   * @param connName The connection name
   * @return DWHREP Connection
   * @throws org.apache.tools.ant.BuildException
   *          On Errors
   */
  protected RockFactory createDwhrepRockFactory(final RockFactory etlrep, final String connName) throws BuildException {
    try {
      final Meta_databases whereMetaDatabases = new Meta_databases(etlrep);
      whereMetaDatabases.setConnection_name("dwhrep");
      whereMetaDatabases.setType_name("USER");
      final Meta_databasesFactory metaDatabasesFactory = new Meta_databasesFactory(etlrep, whereMetaDatabases);
      final Vector<Meta_databases> metaDatabases = metaDatabasesFactory.get();
      if (metaDatabases == null || metaDatabases.isEmpty()) {
        throw new BuildException("Unable to connect metadata : No dwhrep defined in Meta_databases)");
      } else if (metaDatabases.size() > 1) {
        throw new BuildException("Unable to connect metadata : Multiple dwhreps defined in Meta_databases)");
      } else {
        final Meta_databases targetMetaDatabase = metaDatabases.get(0);
        return new RockFactory(targetMetaDatabase.getConnection_string(), targetMetaDatabase
          .getUsername(), targetMetaDatabase.getPassword(), etlrep.getDriverName(), connName, true);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Creating database connection to dwhrep failed.", e);
    }
  }

  /**
   * Get an instance of RockFactory that's connected to DWH as DBA
   *
   * @param etlrep   ETLREP Connection used to lookup DWH connection details
   * @param connName Connection name
   * @return RockFactory connected to DWH as user DBA
   */
  protected RockFactory createDwhRockFactory(final RockFactory etlrep, final String connName) {
    try {
      final Meta_databases whereMetaDatabases = new Meta_databases(etlrep);
      whereMetaDatabases.setConnection_name("dwh");
      whereMetaDatabases.setType_name("DBA");
      final Meta_databasesFactory metaDatabasesFactory = new Meta_databasesFactory(etlrep, whereMetaDatabases);
      final Vector<Meta_databases> metaDatabases = metaDatabasesFactory.get();

      if (metaDatabases != null && metaDatabases.size() == 1) {
        final Meta_databases targetMetaDatabase = metaDatabases.get(0);
        return new RockFactory(targetMetaDatabase.getConnection_string(), targetMetaDatabase
          .getUsername(), targetMetaDatabase.getPassword(), targetMetaDatabase.getDriver_name(),
          connName, true);
      } else {
        throw new BuildException("Unable to connect metadata (No dwh or multiple dwhs defined in Meta_databases)");
      }
    } catch (final RockException re) {
      re.printStackTrace();
      final Throwable t = re.getNestedException();
      if (t != null) {
        System.err.println("Caused by");
        t.printStackTrace();
      }
      throw new BuildException("Creating database connection to dwh failed as dba.", re);
    } catch (final Exception e) {
      e.printStackTrace();
      throw new BuildException("Creating database connection to dwh failed as dba.", e);
    }
  }

  /**
   * This function creates the rockfactory object from the database connection
   * details.
   *
   * @param url      The JDBC Url
   * @param user     The connection username
   * @param pwd      The connection password
   * @param driver   The JDBC driver to use
   * @param connName The connection name
   * @return Returns the created RockFactory.
   * @throws org.apache.tools.ant.BuildException
   *          On Errors
   */
  protected RockFactory createRockFactory(final String url, final String user,
                                          final String pwd, final String driver, final String connName)
    throws BuildException {
    try {
      return new RockFactory(url, user, pwd, driver, connName, true);
    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Unable to initialize database connection.", e);
    }
  }


  /**
   * This function creates the rockfactory object to etlrep from the database
   * connection details read from ETLCServer.properties file.
   *
   * @param databaseConnectionDetails DC connection info
   * @param connName                  the Connection name
   * @return Returns the created RockFactory.
   * @throws org.apache.tools.ant.BuildException
   *          On Errors
   */
  protected RockFactory createEtlrepRockFactory(final Map<String, String> databaseConnectionDetails,
                                                final String connName) throws BuildException {
    final String databaseUsername = databaseConnectionDetails.get("etlrepDatabaseUsername");
    final String databasePassword = databaseConnectionDetails.get("etlrepDatabasePassword");
    final String databaseUrl = databaseConnectionDetails.get("etlrepDatabaseUrl");
    final String databaseDriver = databaseConnectionDetails.get("etlrepDatabaseDriver");
    try {
      return new RockFactory(databaseUrl, databaseUsername, databasePassword, databaseDriver, connName, true);
    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Unable to initialize database connection.", e);
    }
  }

  /**
   * Get a RockFactory connected to REPDB.ETLREP using the default
   * connection details as read from getDatabaseConnectionDetails()
   *
   * @param connName the connection name
   * @return ETLREP Connection
   */
  protected RockFactory createDefaultEtlrepRockFactory(final String connName) {
    return createEtlrepRockFactory(getDatabaseConnectionDetails(), connName);
  }

   ITransferEngineRMI connectEngine() {
    final String engineRmiUrl = RmiUrlFactory.getInstance().getEngineRmiUrl();
    try {
      return (ITransferEngineRMI) Naming.lookup(engineRmiUrl);
    } catch (MalformedURLException e) {
      throw new BuildException(e);
    } catch (NotBoundException e) {
      throw new BuildException("Engine is not started. Aborting tech pack installation", e);
    } catch (RemoteException e) {
      throw handleRemoteException("Engine", "Could not contact Engine. Aborting tech pack installation.", e);
    }
  }

   ISchedulerRMI connectScheduler() {
    final String engineRmiUrl = RmiUrlFactory.getInstance().getSchedulerRmiUrl();
    try {
      return (ISchedulerRMI) Naming.lookup(engineRmiUrl);
    } catch (MalformedURLException e) {
      throw new BuildException(e);
    } catch (NotBoundException e) {
      throw new BuildException("Scheduler is not started. Aborting tech pack installation", e);
    } catch (RemoteException e) {
      throw handleRemoteException("Scheduler", "Could not contact Scheduler. Aborting tech pack installation.", e);
    }
  }

  private BuildException handleRemoteException(final String serverName, final String msg, final RemoteException e) {
    if (e.getCause() instanceof ConnectException) {
      return new BuildException(serverName + " is not started. Aborting tech pack installation", e);
    }
    return new BuildException(msg, e);
  }

  void refreshCache(final ITransferEngineRMI engineRMI) {
	    System.out.println("Getting engine to refresh its cache");
	    try {
	      engineRMI.refreshCache();
	      System.out.println("Cache refreshed");
	    } catch (RemoteException e) {
	      throw handleRemoteException("Engine", "Could not run refreshCache for the engine. " +
	        "Aborting Interface Activation", e);
	    }
	  }
  
  void reloadProperties(final ITransferEngineRMI engineRMI) {
    System.out.println("Getting engine to reload its properties");
    try {
      engineRMI.reloadProperties();
      System.out.println("Properties reloaded");
    } catch (RemoteException e) {
      throw handleRemoteException("Engine", "Could not run reloadProperties for the engine. " +
        "Aborting tech pack installation.", e);
    }
  }

  void reloadLogging(final ITransferEngineRMI engineRMI) {
    System.out.println("Getting engine to reload its logger properties");
    try {
      engineRMI.reloadLogging();
      System.out.println("Properties reloaded");
    } catch (RemoteException e) {
      throw handleRemoteException("Engine", "Could not run reloadLogging for the engine. " +
        "Aborting tech pack installation", e);
    }
  }

  void restore(final ITransferEngineRMI engineRMI, final String techpackName, final String measurementType,
               final String fromDate, final String toDate) {
    System.out.format("Checking if Techpack(%s) is enabled for Techpack Type: %s.\n", techpackName, "ENIQ_EVENT");
    try {
      if (engineRMI.isTechPackEnabled(techpackName, "ENIQ_EVENT")) {
        System.out.format("Techpack(%s) enabled.\n", techpackName);
        System.out.println("Retrieving MeasurementTypes.");
        final List<String> measurementTypes = engineRMI.getMeasurementTypesForRestore(techpackName, measurementType);
        System.out.format("Found the following MeasuementTypes(%d).\n", measurementTypes.size());
        for (String type : measurementTypes) {
          System.out.println("MeasuementType:" + type);
        }
        if (!measurementTypes.isEmpty()) {
          engineRMI.restore(techpackName, measurementTypes, fromDate, toDate);
        }
      } else {
        System.out.format("Techpack(%s) is disabled.\n", techpackName);
      }
    } catch (RemoteException e) {
      throw handleRemoteException("Engine", "Could not run restore for " + techpackName +
        ". Aborting tech pack installation", e);
    }
  }

  void activateScheduler(final ISchedulerRMI schedulerRMI) {
    System.out.println("Activating Scheduler");
    try {
    	System.setProperty("activatescheduler","false");
      schedulerRMI.reload();
      System.setProperty("activatescheduler","true");
      System.out.println("Scheduler actived.");
    } catch (RemoteException e) {
      throw handleRemoteException("Scheduler", "Could not run reload for scheduler. " +
        "Aborting tech pack installation", e);
    }
  }

  void startAndWaitSet(final ITransferEngineRMI engineRMI, final String techPack, final String setName) {
    final String status;
    try {
      System.out.printf("Starting set %s::%s%n", techPack, setName);
      status = engineRMI.executeAndWait(techPack, setName, "");
    } catch (RemoteException e) {
      throw handleRemoteException("Engine", "Coult not run set " + techPack + ":" + setName +
        "Aborting tech pack installation", e);
    } catch (Throwable t) {
      throw new BuildException("Execution of " + techPack + "::" + setName + " failed. " +
        "Aborting tech pack installation", t);
    }
    if (SetListener.SUCCEEDED.equals(status)) {
      System.out.printf("Set %s::%s succeeded.%n", techPack, setName);
    } else if (status == null) {
      throw new BuildException(setName + " has undetermined status!. Aborting tech pack installation.");
    } else if (status.equals(SetListener.NOSET)) {
      System.out.printf("Set %s not found. Cannot run %s %s%n", setName, techPack, setName);
    } else if (status.equals(SetListener.DROPPED)) {
      throw new BuildException(setName + " has been dropped from priorityqueue. Aborting tech pack installation.");
    } else {
      throw new BuildException(setName + " has failed Status{" + status + "}. Aborting tech pack installation.");
    }
  }

  void updateTransformation(final ITransferEngineRMI engine, final String techPack) {
    try {
      engine.updateTransformation(techPack);
    } catch (RemoteException e) {
      throw handleRemoteException("Engine",
        "Cannot update TransformerCache for tech pack " + techPack + ". Aborting tech pack installation.", e);
    }
  }

  /**
   * @deprecated Use RMI to call an ITransferEngineRMI/ISchedulerRMI method and not spawn a new process
   *             to call engine -e or scheduler to do it, it ends up calling the RMI method anyway.
   *             Stats Multiblade can have an engine JVM of up to 64G in size, this can cause 'error=12:not enough space' errors
   */
  @Deprecated
  String runCommand(final String command) throws BuildException {
    final StringBuilder result = new StringBuilder();
    try {
      final Runtime runtime = Runtime.getRuntime();
      final Process process = runtime.exec(command);
      // read what process wrote to the STDIN (immediate)
      final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        result.append(line).append("\n");
      }
      // wait for process to end
      try {
        process.waitFor();
      } catch (InterruptedException e) {
        try {
          process.waitFor();
        } catch (InterruptedException e2) {
          // do we have a problem here?
        }
      }
      // and read whatever was left to STDIN
      while ((line = bufferedReader.readLine()) != null) {
        result.append(line).append("\n");
      }
      // close streams
      bufferedReader.close();
      process.getErrorStream().close();
      process.getOutputStream().close();
      result.append("Command executed with exitvalue ").append(process.exitValue());
    } catch (IOException e) {
      throw new BuildException("IOException occurred while trying to run command", e);
    }
    return result.toString();
  }
}
