/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.distocraft.dc5000.install.ant;

import java.util.Map;
import java.util.Vector;

import org.apache.tools.ant.BuildException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.distocraft.dc5000.etl.rock.Meta_databasesFactory;

/**
 * 
 * Note: This class needs to be used during upgrade scenario. Caution: Here we
 * refer repository's rockfactory so during initial installation we cannot get
 * the repository's jar.So avoid using this class during II.
 * 
 * 
 * @author qponven
 * @since 2011
 * 
 */
public class CommonDBTasks extends CommonTask {

  protected static final String DWHREP = "dwhrep";

  protected static final String DWH = "dwh";

  private static final String USER = "USER";

  /**
   * This function creates the rockfactory object to etlrep from the database
   * connection details read from ETLCServer.properties file.
   * 
   * @return Returns the created RockFactory.
   */
  public RockFactory createEtlrepRockFactory() throws BuildException {
    final Map<String, String> databaseConnectionDetails = getDatabaseConnectionDetails();
    return createEtlrepRockFactory(databaseConnectionDetails);
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
   * This function creates the RockFactory to dwhrep/dwh.
   */
  public static RockFactory createDbRockFactory(final RockFactory etlrepRockFactory, final String connectionName)
      throws BuildException {
    RockFactory rockFactory = null;
    try {
      final Meta_databases whereMetaDatabases = new Meta_databases(etlrepRockFactory);
      whereMetaDatabases.setConnection_name(connectionName);
      whereMetaDatabases.setType_name(USER);
      final Meta_databasesFactory metaDatabasesFactory = new Meta_databasesFactory(etlrepRockFactory,
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

}
