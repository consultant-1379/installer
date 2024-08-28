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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;


import org.apache.tools.ant.BuildException;

import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.distocraft.dc5000.etl.rock.Meta_databasesFactory;
import com.distocraft.dc5000.repository.dwhrep.Datainterface;
import com.distocraft.dc5000.repository.dwhrep.DatainterfaceFactory;

import ssc.rockfactory.RockFactory;

/**
 * This class is a custom made ANT-task that prints out information about active
 * interfaces.
 * 
 * @author epaujor
 */
public class GetActiveInterfaces extends CommonTask {

  private static final String EMPTY_STRING = "";

  private static final String COLLECTION_SET_NAME_LIKE = "COLLECTION_SET_NAME like '";

  private static final String CLOSE_BRACKET = ")";
 
  private static final String OPEN_BRACKET = "(";

  private static final String AND = "AND ";

  private static final String OR = " or ";

  private static final String WILD_CARD = "-%'";

  RockFactory etlrepRockFactory = null;
  
  RockFactory  dwhrepRockFactory = null;

  private String showNames = EMPTY_STRING;

  private String techpackName = EMPTY_STRING;
  
  private String showDetails = EMPTY_STRING;

  /**
   * This function starts the execution of task.
   */
  @Override
  public void execute() throws BuildException {
    Map<String, String> databaseConnectionDetails;
    try {
      databaseConnectionDetails = getDatabaseConnectionDetails();
    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException(e.getMessage());
    }

    // Create the connection to the etlrep.
    this.etlrepRockFactory = createEtlrepRockFactory(databaseConnectionDetails, getClass().getSimpleName());
    
 // Create also the connection to dwhrep.
    this.createDwhrepRockFactory();
    
    if(this.showDetails.equalsIgnoreCase("true")){
    	this.printDetailsOfInstalledInterface();
  }
  else{

    // Print out the information about active interfaces.
    this.printActiveInterfaces();
  }
  }
  
  /**
   * This function creates the RockFactory to dwhrep. The created RockFactory is
   * inserted in class variable dwhrepRockFactory.
   */
  private void createDwhrepRockFactory() {
    try {
      Meta_databases whereMetaDatabases = new Meta_databases(this.etlrepRockFactory);
      whereMetaDatabases.setConnection_name("dwhrep");
      whereMetaDatabases.setType_name("USER");
      Meta_databasesFactory metaDatabasesFactory = new Meta_databasesFactory(this.etlrepRockFactory, whereMetaDatabases);
      Vector metaDatabases = metaDatabasesFactory.get();

      if (metaDatabases != null || metaDatabases.size() == 1) {
        Meta_databases targetMetaDatabase = (Meta_databases) metaDatabases.get(0);

        this.dwhrepRockFactory = new RockFactory(targetMetaDatabase.getConnection_string(), targetMetaDatabase
            .getUsername(), targetMetaDatabase.getPassword(), etlrepRockFactory.getDriverName(), "PreinstallCheck",
            true);

      } else {
        throw new BuildException("Unable to connect metadata (No dwhrep or multiple dwhreps defined in Meta_databases)");
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Creating database connection to dwhrep failed.", e);
    }
  }

  public String getShowNames() {
    return showNames;
  }

  public void setShowNames(final String showNames) {
    this.showNames = showNames;
  }

  public String getTechpackName() {
    return techpackName;
  }

  public void setTechpackName(final String techpackName) {
    this.techpackName = techpackName;
  }
  
  public String getShowDetails() {
	    return showDetails;
  }

  public void setShowDetails(final String showDetails) {
	    this.showDetails = showDetails;
  }

  /**
   * This function prints out the details about active interfaces.
   * 
   * @throws BuildException
   */
  private void printActiveInterfaces() throws BuildException {
    ResultSet rs = null;
    Statement statement = null;
    SortedSet<String> PrintInterfaceSet= new TreeSet<String>();
    try {
        final String whereClause = getWhereClause();
        
        if (null != whereClause){
  	      statement = etlrepRockFactory.getConnection().createStatement();
  	      rs = statement.executeQuery("SELECT DISTINCT COLLECTION_SET_NAME FROM META_COLLECTION_SETS WHERE "
  	          + "ENABLED_FLAG = 'Y' " + whereClause.toString() + " AND type = 'Interface' ORDER BY COLLECTION_SET_NAME");
  	      
  	      while (rs.next()) {
  	        final String interfaceName = rs.getString(1);
  	        if (showNames.equalsIgnoreCase("true")) {
  	        	//Code Change for TR HU30877
  	        	if(interfaceName.indexOf("-")>=0 && interfaceName.toString().compareTo("INTF_DC_E_IMS_PGM_AP-WUIGM")!=0 )
  	        	{
  	        		PrintInterfaceSet.add(interfaceName.substring(0, interfaceName.indexOf("-")));
  	        	}else{
  	        		PrintInterfaceSet.add(interfaceName.toString());
  	        	}	    
  	        } else if (interfaceName.contains("INTF_DC_E_IMS_PGM_AP-WUIGM")) {
  	        	PrintInterfaceSet.add(interfaceName.replaceAll("-WUIGM-", "-WUIGM ").trim());
  	        } else {
  	        	PrintInterfaceSet.add(interfaceName.replaceAll("-", " ").trim());
  	        }
  	      }
  	     
  	      @SuppressWarnings("rawtypes")
		Iterator itr=PrintInterfaceSet.iterator();
  	      while(itr.hasNext()){
  	      System.out.println(" "+ itr.next());
        }
  	    System.out.print("\n");
        }
      }catch (Exception e) {
      throw new BuildException("Getting active interfaces failed."+ e);
      } finally {
      try {
        if (rs != null) {
          rs.close();
        }
      } catch (final SQLException sqle) {
        System.out.print("ResultSet cleanup error - " + sqle.toString());
      }
      try {
        if (statement != null) {
          while (statement.getMoreResults()) {
            statement.getResultSet().close();
          }
          statement.close();
        }
      } catch (final SQLException sqle) {
        System.out.print("Statement cleanup error - " + sqle.toString());
      }
      try {
        if (etlrepRockFactory != null) {
          etlrepRockFactory.getConnection().close();
        }
      } catch (final SQLException sqle) {
        System.out.print("Connection cleanup error - " + sqle.toString());
      }
      rs = null;
      statement = null;
      etlrepRockFactory = null;
    }
  }

  /**
   * Prints the Rstate buildNumber of the all the active interfaces 
   * 
   */
  private void printDetailsOfInstalledInterface() throws BuildException { 
	  
	  ResultSet rs = null;
	    Statement statement = null;
	    try {
	      final String whereClause = getWhereClause();
	      SortedSet<String> PrintInterfaceSet= new TreeSet<String>();    
	      if (null != whereClause){
		      statement = etlrepRockFactory.getConnection().createStatement();
		      rs = statement.executeQuery("SELECT DISTINCT COLLECTION_SET_NAME FROM META_COLLECTION_SETS WHERE "
		          + "ENABLED_FLAG = 'Y' " + whereClause.toString() + " AND type = 'Interface' ORDER BY COLLECTION_SET_NAME");
		
		      while (rs.next()) {
		  	     final String interfaceName = rs.getString(1);
			        	if(interfaceName.indexOf("-")>=0 && interfaceName.toString().compareTo("INTF_DC_E_IMS_PGM_AP-WUIGM")!=0 ){
			        		PrintInterfaceSet.add(interfaceName.substring(0, interfaceName.indexOf("-")));
			        	}else{
			        		PrintInterfaceSet.add(interfaceName.toString());
			        	}
		      }
		  	    @SuppressWarnings("rawtypes")
		  		Iterator itr=PrintInterfaceSet.iterator();
		  	  System.out.printf("%-35s %-10s %-10s ","Active Interfaces" , "RState" , "Build Number");
		  	  System.out.println("");
		  	  System.out.println("--------------------------------------------------------------------");
		  	    while(itr.hasNext()){
			    	final String itrInterfaceName = (String)itr.next();
		  	    	Datainterface dataInterface = new Datainterface(this.dwhrepRockFactory);
			    	if(itrInterfaceName.contains("-eniq")){
			    		dataInterface.setInterfacename(itrInterfaceName.substring(0, itrInterfaceName.indexOf("-")));
			    	}else{
				 		dataInterface.setInterfacename(itrInterfaceName);
			    	}
			 		dataInterface.setInterfacename(itrInterfaceName);
					DatainterfaceFactory dataInterfaceFact = new DatainterfaceFactory(this.dwhrepRockFactory , dataInterface);
					Vector versions = dataInterfaceFact.get();	
					 if ( (versions.size() == 1)) {
						 Datainterface currActiveInterface = (Datainterface) versions.get(0);
				  		 String buildNumber = currActiveInterface.getInterfaceversion();
				  		 String build = "b"+buildNumber.substring(buildNumber.indexOf("(")+2, buildNumber.indexOf(")")).trim();
				  		 System.out.printf("%-35s %-10s %-10s  ", itrInterfaceName, currActiveInterface.getRstate() , build );
			  		}
		  			 System.out.println("");
	  				}
	      }
	    }  catch (Exception e) {
	      throw new BuildException("Getting active interfaces failed.");
	    } finally {
	      try {
	        if (rs != null) {
	          rs.close();
	        }
	      } catch (final SQLException sqle) {
	        System.out.print("ResultSet cleanup error - " + sqle.toString());
	      }
	      try {
	        if (statement != null) {
	          while (statement.getMoreResults()) {
	            statement.getResultSet().close();
	          }
	          statement.close();
	        }
	      } catch (final SQLException sqle) {
	        System.out.print("Statement cleanup error - " + sqle.toString());
	      }
	      try {
	        if (etlrepRockFactory != null) {
	          etlrepRockFactory.getConnection().close();
	        }
	      } catch (final SQLException sqle) {
	        System.out.print("Connection cleanup error - " + sqle.toString());
	      }
	      rs = null;
	      statement = null;
	      etlrepRockFactory = null;
	      dwhrepRockFactory = null;
	    }
	  }
  
  /**
   * If techpack name is not empty, then, this will return specific interface
   * names to lookup. Otherwise, this will return an empty string.
   * 
   * @return
   */
  private String getWhereClause() {
    String whereClause = EMPTY_STRING;
    if (techpackName != null && !techpackName.isEmpty()) {
      whereClause = getInterfaceNamesForTechpack();
    }
    return whereClause;
  }

  /**
   * Get Interface Names based on techpack
   * 
   * @param whereClause
   * @throws SQLException
   */
  private String getInterfaceNamesForTechpack() {
    final StringBuilder whereClause = new StringBuilder();
    RockFactory dwhrepRockFactory = null;
    ResultSet rs = null;
    Statement statement = null;
    try {
      dwhrepRockFactory = createDwhrepRockFactory(etlrepRockFactory, getClass().getSimpleName());
      statement = dwhrepRockFactory.getConnection().createStatement();
      rs = statement.executeQuery("SELECT distinct INTERFACENAME FROM InterfaceTechpacks where TECHPACKNAME = '"
          + techpackName + "'");

      if (rs.next()) {
        whereClause.append(AND);
        whereClause.append(OPEN_BRACKET);
        whereClause.append(COLLECTION_SET_NAME_LIKE);
        whereClause.append(rs.getString(1)); 
        whereClause.append(WILD_CARD);
        while (rs.next()) {
          whereClause.append(OR);
          whereClause.append(COLLECTION_SET_NAME_LIKE);
          whereClause.append(rs.getString(1));
          whereClause.append(WILD_CARD);
        }
        whereClause.append(CLOSE_BRACKET);
      } else{
        System.out.print("No active interfaces associated with the techpack: '"+techpackName+"'");
        //throw new BuildException("No active interfaces associated with the techpack: '"+techpackName+"'");
      }
    } catch (Exception e) {
      throw new BuildException("Getting active interfaces failed.");
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
      } catch (final SQLException sqle) {
        System.out.print("ResultSet cleanup error - " + sqle.toString());
      }
      try {
        if (statement != null) {
          while (statement.getMoreResults()) {
            statement.getResultSet().close();
          }
          statement.close();
        }
      } catch (final SQLException sqle) {
        System.out.print("Statement cleanup error - " + sqle.toString());
      }
      try {
        if (dwhrepRockFactory != null) {
          dwhrepRockFactory.getConnection().close();
        }
      } catch (final SQLException sqle) {
        System.out.print("Connection cleanup error - " + sqle.toString());
      }
      rs = null;
      statement = null;
      dwhrepRockFactory = null;
    }
    if (!whereClause.toString().toString().isEmpty()){
    	return whereClause.toString();
    } else {
    	return null;
    }
    
  }
}
