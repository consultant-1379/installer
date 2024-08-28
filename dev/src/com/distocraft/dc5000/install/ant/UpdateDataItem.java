package com.distocraft.dc5000.install.ant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.distocraft.dc5000.etl.rock.Meta_databasesFactory;

/**
 * A simple Ant task implementation to update the dataitem table in the dwhrep 
 * with values for datatype, datasize and datascale columns from measurementcolumn 
 * and referencecolumn tables.
 *  
 * @author esunbal
 * @see org.apache.tools.ant.Task
 * 
 */
public class UpdateDataItem extends Task{
	
	private String currentWorkingDirectory = "";		

	private final String updateSqlFile = "update.sql";

	private final double batchSize = 1000;	

	private transient RockFactory etlrepRockFactory = null;

	private transient RockFactory dwhrepRockFactory = null;

	private final String selectQuery = "select distinct di.dataformatid, di.dataname, mc.datatype, mc.datasize, mc.datascale " +
	"from measurementcolumn mc, dataitem di " +
	"where di.dataname = mc.dataname " +
	"and substring(di.dataformatid,1,len(di.dataformatid)-charindex(':',reverse(di.dataformatid))) = substring(mc.mtableid,1,len(mc.mtableid)-charindex(':',reverse(mc.mtableid))) " +
	"and di.datatype = null " +
	"union " +
	"select distinct di.dataformatid, di.dataname, rc.datatype, rc.datasize, rc.datascale " +
	"from referencecolumn rc, dataitem di " +
	"where di.dataname = rc.dataname " +
	"and substring(di.dataformatid,1,len(di.dataformatid)-charindex(':',reverse(di.dataformatid))) = rc.typeid " +
	"and di.datatype = null";

	/* 
	 * The entry method for this class.
	 * @see org.apache.tools.ant.Task#execute()	 * 
	 */
	@Override
	public void execute() throws BuildException {
		/*
		 * Tasks:
		 * 0.) Sanity checks.
		 * 1.) Get the database connection
		 * 2.) Run the select query to create the file with list of update statements
		 * 3.) Read the file, create batches of 1000 and execute the update
		 * 4.) Remove the temporary file update.sql
		 */
	
		
		// Task 0: Sanity Checks
		

		System.out.println("Inside execute method of DataItem");

		if (!this.currentWorkingDirectory.endsWith(File.separator)) {
			this.currentWorkingDirectory = this.currentWorkingDirectory
			+ File.separator;
		}
		
		 //Task 1: Get the database connection.
		 	
		this.createDatabaseConnections();

		
		// Task 2: Get the information from MeasurementColumn and ReferenceColumn tables.
		
		this.getDataForDataItemTable();

		
		// Task 3: Run the batch updates on DataItem table.		

		this.updateDataItem();
		
		// Task 4: Remove the temporary file update.sql
		
		this.removeUpdateSqlFile();
	}


	/**
	 * Removes the temporary update sql file. 
	 */
	private void removeUpdateSqlFile(){
		//TODO : Will be implemented when the code becomes stable.
	}

	/**
	 * Utility method to get the number of lines in a file.
	 * @param fileName
	 * @return double
	 */
	public double getNumberOfLines(String fileName) {
		double numberOfLines = 0;		
		LineNumberReader lineCounter = null;
		try {
			lineCounter = new LineNumberReader(new FileReader(new File(fileName)));
			while ((lineCounter.readLine()) != null) { 
				continue;
			}
			numberOfLines = lineCounter.getLineNumber();

		} catch (IOException e) {
			System.out.println("Unable to read the file "+fileName);
			e.printStackTrace();
		}

		return numberOfLines;
	}


	/**
	 * Updates the dataitem table by running the update sqls from the update.sql file
	 * in batches of 1000 update statements.
	 */
	private void updateDataItem(){		

		Connection connection = this.dwhrepRockFactory.getConnection();	
		String sqlQuery = null;

		int batchNumber = 0;
		double rowCountPerBatch = 0;
		double rowCountInUpdateSql = 0;
		double numberOfRowsInUpdateSQLFile = getNumberOfLines(this.currentWorkingDirectory+this.updateSqlFile);
		System.out.println("Number of rows in "+this.updateSqlFile+" is:"+numberOfRowsInUpdateSQLFile);
		double numberOfBatches = Math.ceil(numberOfRowsInUpdateSQLFile / batchSize);
		System.out.println("Executing "+numberOfRowsInUpdateSQLFile+" sql updates in "+numberOfBatches+" batches");		

		File file = new File(this.currentWorkingDirectory+this.updateSqlFile);		
		BufferedReader br = null;
    Statement statement = null;
		try {
			if(connVaildation(connection)) {
      statement = connection.createStatement();
			int[] updateCounts = null;	

			// Disable auto-commit	
			connection.setAutoCommit(false);
			br = new BufferedReader(new FileReader(file));
			while ((sqlQuery = br.readLine()) != null) {
				// Read the update.sql file in batches of this.batchSize - currently set to 1000 and add it in the batch for execution.				
				statement.addBatch(sqlQuery);				
				rowCountPerBatch++;	
				rowCountInUpdateSql++;

				if((rowCountPerBatch == batchSize) || (rowCountInUpdateSql == numberOfRowsInUpdateSQLFile)){
					// submit a batch of update commands for execution
					batchNumber++;
					updateCounts = statement.executeBatch();
					statement.clearBatch();
					rowCountPerBatch = 0;					
				}
			}	

			// Since there were no errors, commit
			connection.commit();
			}

		}
		catch (BatchUpdateException be) {
			System.out.println("Not all the statements in batch:"+batchNumber+" were successfully executed");
			
			// Get the updates for the batch and iterate the batch to see which row failed.
			int[] updateCounts = be.getUpdateCounts();
			processUpdateCounts(updateCounts, batchNumber);

		} 
		catch(SQLException se){
			System.out.println("Exception in running a SQL statement.");
			se.printStackTrace();
		}
		catch(IOException ie){
			System.out.println("Exception in file handling of update.sql");
			ie.printStackTrace();
		}
		finally{			
			try {
		        if (br != null) {
		          br.close();
		        }
		        if (statement != null) {
		          statement.close();
		        }
					} catch (IOException e) {
						e.printStackTrace();
		      } catch (SQLException e) {
		        System.out.println("error closing statement");
		      }
		}	
	}
	

	public boolean connVaildation(Connection connection) throws SQLException {

		return (connection != null && !connection.isClosed());
	}


	/**
	 * Creates the database connection for etlrep and dwhrep databases.
	 */

	private void createDatabaseConnections(){
		System.out.println("Creating database connection - etlrep");		
		this.etlrepRockFactory = createEtlrepRockFactory();
		System.out.println("Creating database connection - dwhrep");
		createDwhrepRockFactory();
	}


	/**
	 * Queries the measurementcolumn and referencecolumn to populate the update.sql file 
	 * with update sqls on dataitem table.
	 */
	ResultSet resultSet = null;
	private void getDataForDataItemTable(){
		BufferedWriter out = null;
		Connection connection = null;
		Statement statement = null;
		try {

			String dataFormatId = null;
			String dataName = null;			
			String datatype = null;
			Integer datasize = null;
			Integer datascale = null;
			
			connection = this.dwhrepRockFactory.getConnection();
			statement = connection.createStatement();
			System.out.println("sqlQuery:"+this.selectQuery);
			resultSet = statement.executeQuery(this.selectQuery);
			out = new BufferedWriter(new FileWriter(this.currentWorkingDirectory+this.updateSqlFile));
			
			String updateString = null;			
			int rowCounter = 0;
			
			while(resultSet.next()){
				rowCounter++;
				dataFormatId = resultSet.getString(1);
				dataName = resultSet.getString(2);
				datatype  = resultSet.getString(3);
				datasize  = resultSet.getInt(4);
				datascale  = resultSet.getInt(5);
				updateString = "update DataItem set datatype='"+datatype+"', "+"datasize="+datasize+", "+"datascale="+datascale+" where dataformatid='"+dataFormatId+"' and dataname='"+dataName+"'";				
				out.write(updateString);
				out.write("\n");
			}
		}
		catch (IOException e) {
			// TODO Need to change this into more robust error checking mechanism.
			System.out.println("Exception in File handling of update.sql.");			
			e.printStackTrace();
		}
		catch (SQLException e) {
			// TODO Need to change this into more robust error checking mechanism.
			System.out.println("Exception in querying for values from the database.");
			e.printStackTrace();
		} 
		finally{
			Logger log = Logger.getAnonymousLogger();
			closeConnection(log,resultSet,statement,connection);
			
			try {
				if(out!=null)
					out.close();
			} catch (Exception e) {				
				log.log(Level.WARNING,"Error while closing connection {0}",e.getMessage());
			}
			
		}
	}

	
	private void closeConnection(Logger log, ResultSet resultSet, Statement statement, Connection connection) {
		try {
		if(resultSet!=null)
			resultSet.close();
		if(statement != null)
			statement.close();
		if(connection!=null)
			connection.close();
		log.finest("Connection closed successfully");
		} catch (Exception e) {				
			log.log(Level.WARNING,"Error while closing connection {0}",e.getMessage());
		}		
	}

	/**
	 * Exception handling utility method to determine which update statement failed.
	 * @param updateCounts
	 * @param batchNumber
	 */
	public void processUpdateCounts(int[] updateCounts, int batchNumber) {		
		double recordNumber = 0;
		for (int i=0; i<updateCounts.length; i++) {
			recordNumber = batchNumber * batchSize + i;
			if (updateCounts[i] == Statement.SUCCESS_NO_INFO) {
				// Successfully executed - number of affected rows not available
				System.out.println("Updation of Record "+recordNumber+" succeeded but with no info on affected rows.");
			} else if (updateCounts[i] == Statement.EXECUTE_FAILED) {
				// Failed to execute				
				System.out.println("Updation of Record "+recordNumber+" failed");
			}
		}
	}


	/**
	 * Creates the dwhrep rockfactory.
	 * @param databaseConnectionDetails
	 */
	
	private void createDwhrepRockFactory() {
		try {
			Meta_databases whereMetaDatabases = new Meta_databases(
					this.etlrepRockFactory);
			whereMetaDatabases.setConnection_name("dwhrep");
			whereMetaDatabases.setType_name("USER");
			Meta_databasesFactory metaDatabasesFactory = new Meta_databasesFactory(
					this.etlrepRockFactory, whereMetaDatabases);
			Vector<Meta_databases> metaDatabases = metaDatabasesFactory.get();

			if (metaDatabases != null && metaDatabases.size() == 1) {
				Meta_databases targetMetaDatabase = metaDatabases
				.get(0);
				this.dwhrepRockFactory = new RockFactory(targetMetaDatabase
						.getConnection_string(), targetMetaDatabase
						.getUsername(), targetMetaDatabase.getPassword(),
						etlrepRockFactory.getDriverName(), "UpdateDataItem",
						true);
			} else {
				throw new BuildException(
				"Unable to connect metadata (No dwhrep or multiple dwhreps defined in Meta_databases)");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Creating database connection to dwhrep failed.", e);
		}
	}

	/**
	 * Creates etlrep rockfactory.
	 * @param databaseConnectionDetails
	 * @return
	 * @throws BuildException
	 */
	private RockFactory createEtlrepRockFactory() throws BuildException {

		RockFactory rockFactory = null;
		final String databaseUsername = getProject().getProperty("etlrepDatabaseUsername");
		final String databasePassword = getProject().getProperty("etlrepDatabasePassword");
		final String databaseUrl = getProject().getProperty("etlrepDatabaseUrl");
		final String databaseDriver = getProject().getProperty("etlrepDatabaseDriver");

		try {
			rockFactory = new RockFactory(databaseUrl, databaseUsername,
					databasePassword, databaseDriver, "UpdateDataItem", true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Unable to initialize database connection.", e);
		}

		if (rockFactory == null)
			throw new BuildException(
			"Unable to initialize database connection. Please check the settings in the ETLCServer.properties file.");
		return rockFactory;
	}	

	/**
	 * @return
	 */
	public String getCurrentWorkingDirectory() {
		return currentWorkingDirectory;
	}

	/**
	 * @param currentWorkingDirectory
	 */
	public void setCurrentWorkingDirectory(String currentWorkingDirectory) {
		this.currentWorkingDirectory = currentWorkingDirectory;
	}
}
