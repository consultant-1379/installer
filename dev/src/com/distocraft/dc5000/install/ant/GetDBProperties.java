package com.distocraft.dc5000.install.ant;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.distocraft.dc5000.etl.rock.Meta_databasesFactory;
import com.ericsson.eniq.repository.ETLCServerProperties;

/**
 * This is a custom made ANT task that gets the database properties like
 * database url, database username, database password and database driver. This
 * task gets one parameter called <i>name</i> and the ANT properties created by
 * this task are:<br/>
 * <ul>
 * <li><i>name</i>DatabaseUrl</li>
 * <li><i>name</i>DatabaseUsername</li>
 * <li><i>name</i>DatabasePassword</li>
 * <li><i>name</i>DatabaseDriver</li>
 * <li><i>name</i>DatabaseDBAUsername</li>
 * <li><i>name</i>DatabaseDBAPassword</li>
 * </ul>
 * 
 * @author berggren
 */
public class GetDBProperties extends CommonTask {
	
	private ETLCServerProperties etlcserverprops;

	private String name = new String("");

	private String type = new String("USER");

	private String propertiesFilepath = new String("");

	/**
	 * This function starts the checking of the installation file.
	 */
	public void execute() throws BuildException {

		Connection con = null;

		try {

			Map<String, String> etlrepDatabaseConnectionDetails = getDatabaseConnectionDetails();

			if (this.name.equalsIgnoreCase("etlrep")
					&& this.type.equalsIgnoreCase("user")) {
				// Set the connection details directly from
				// ETLCServer.properties file.
				getProject().setNewProperty(
						this.name + "DatabaseUrl",
						etlrepDatabaseConnectionDetails
								.get("etlrepDatabaseUrl").toString());
				getProject().setNewProperty(
						this.name + "DatabaseUsername",
						etlrepDatabaseConnectionDetails.get(
								"etlrepDatabaseUsername").toString());
				getProject().setNewProperty(
						this.name + "DatabasePassword",
						etlrepDatabaseConnectionDetails.get(
								"etlrepDatabasePassword").toString());
				getProject().setNewProperty(
						this.name + "DatabaseDriver",
						etlrepDatabaseConnectionDetails.get(
								"etlrepDatabaseDriver").toString());

				return;
			}

			// Connect actually to database

			// Create the connection to the etlrep.
			con = createETLRepConnection(etlrepDatabaseConnectionDetails);

			// Set the database connection properties to ANT task properties.
			this.setDBProperties(con);

		} catch (BuildException be) {
			throw be;
		} catch (Exception e) {
			throw new BuildException("Exceptional failure", e);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception ex) {
				}
			}
		}

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private Connection createETLRepConnection(
			Map<String, String> etlrepDatabaseConnectionDetails)
			throws BuildException {
		try {
			Driver driver = (Driver) Class.forName(
					etlrepDatabaseConnectionDetails.get("etlrepDatabaseDriver")
							.toString()).newInstance();

			Properties p = new Properties();
			p.put("user",
					etlrepDatabaseConnectionDetails.get(
							"etlrepDatabaseUsername").toString());
			p.put("password",
					etlrepDatabaseConnectionDetails.get(
							"etlrepDatabasePassword").toString());
			p.put("REMOTEPWD", ",,CON=PLAT_INST");

			Connection con = driver.connect(etlrepDatabaseConnectionDetails
					.get("etlrepDatabaseUrl").toString(), p);

			// This should never happen...
			if (con == null) {
				throw new Exception(
						"DB driver initialized null connection object");
			}
			return con;

		} catch (Exception e) {
			throw new BuildException("Database connecting to etlrep failed", e);
		}
	}

	/**
	 * This function sets the database connection details to ANT task
	 * properties. If etlrep database is requested, database properties are
	 * retrieved from ETLCServer.properties.
	 */
	private void setDBProperties(Connection con) throws Exception {
		
		RockFactory etlrep = null;
		Meta_databases db_prop;
		Meta_databases where_obj;
		Meta_databasesFactory md_fact;
		List<Meta_databases> dbs;

		etlcserverprops =  new ETLCServerProperties(System.getProperty(ETLCServerProperties.CONFIG_DIR_PROPERTY_NAME)+"/ETLCServer.properties");
		
		try {
			
			etlrep = new RockFactory(etlcserverprops.getProperty(ETLCServerProperties.DBURL), etlcserverprops.getProperty(ETLCServerProperties.DBUSERNAME),
					etlcserverprops.getProperty(ETLCServerProperties.DBPASSWORD), etlcserverprops.getProperty(ETLCServerProperties.DBDRIVERNAME),
					"GetDBProperties",false);
			where_obj = new Meta_databases(etlrep);
						
			where_obj.setConnection_name(this.name);
			where_obj.setType_name(this.type);
			
			md_fact = new Meta_databasesFactory(etlrep, where_obj);
			dbs = md_fact.get();
			if (dbs.size() <= 0) {
				throw new BuildException("No such database \"" + this.name
						+ "\" of type \"" + type + "\"");
			}	
			
			db_prop = dbs.get(0);			
			getProject().setNewProperty(this.name + "DatabaseUrl",
					db_prop.getConnection_string());
			getProject().setNewProperty(this.name + "DatabaseUsername",
					db_prop.getUsername());
			getProject().setNewProperty(this.name + "DatabasePassword",
					db_prop.getPassword());
			getProject().setNewProperty(this.name + "DatabaseDriver",
					db_prop.getDriver_name());
			
			// Separate DBA properties, as ant property override is not
			// supported.
			if (getType().equalsIgnoreCase("DBA")) {
				getProject().setNewProperty(
						this.name + "DatabaseDBAUsername",
						db_prop.getUsername());
				getProject().setNewProperty(
						this.name + "DatabaseDBAPassword",
						db_prop.getPassword());
			}
		} catch (BuildException be) {
			throw be;
		} catch (Exception e) {
			throw new BuildException(
					"Failed to load DB properties from etlrep.", e);
		} finally {
			try {
				if (etlrep != null)
					etlrep.getConnection().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}	
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
