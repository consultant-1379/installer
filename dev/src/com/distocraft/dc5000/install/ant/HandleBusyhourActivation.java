package com.distocraft.dc5000.install.ant;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.tools.ant.BuildException;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.distocraft.dc5000.etl.rock.Meta_databasesFactory;
import com.distocraft.dc5000.repository.dwhrep.Aggregationrule;
import com.distocraft.dc5000.repository.dwhrep.AggregationruleFactory;
import com.distocraft.dc5000.repository.dwhrep.Busyhour;
import com.distocraft.dc5000.repository.dwhrep.BusyhourFactory;
import com.distocraft.dc5000.repository.dwhrep.Busyhourmapping;
import com.distocraft.dc5000.repository.dwhrep.BusyhourmappingFactory;
import com.distocraft.dc5000.repository.dwhrep.Busyhourplaceholders;
import com.distocraft.dc5000.repository.dwhrep.BusyhourplaceholdersFactory;
import com.distocraft.dc5000.repository.dwhrep.Busyhourrankkeys;
import com.distocraft.dc5000.repository.dwhrep.BusyhourrankkeysFactory;
import com.distocraft.dc5000.repository.dwhrep.Busyhoursource;
import com.distocraft.dc5000.repository.dwhrep.BusyhoursourceFactory;
import com.distocraft.dc5000.repository.dwhrep.Tpactivation;
import com.distocraft.dc5000.repository.dwhrep.TpactivationFactory;

public class HandleBusyhourActivation extends CommonTask {

	private transient RockFactory etlrepRockFactory = null;
	private transient RockFactory dwhrepRockFactory = null;
	private transient String propFilepath = "";
	private String techPackVersionID;

	// parameters from ANT
	private String configurationDirectory = "";
	private String techPackName = "";
	private int techPackMetadataVersion = 0;
	private int buildNumber = 0;
	private String techPackVersion = "";

	public void execute() throws BuildException {

		if (techPackMetadataVersion >= 3) {
			techPackVersionID = this.techPackName + ":((" + this.buildNumber
					+ "))";
		} else if (techPackMetadataVersion == 2) {
			techPackVersionID = this.techPackName + ":b" + this.buildNumber;
		} else {
			techPackVersionID = this.techPackName + ":" + this.techPackVersion
					+ "_b" + this.buildNumber;
		}

    System.out.println("Checking connection to database...");
    final Map<String, String> databaseConnectionDetails;
    try {
      databaseConnectionDetails = getDatabaseConnectionDetails();
    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException(e.getMessage());
    }


    // Create the connection to the etlrep.
    this.etlrepRockFactory = createEtlrepRockFactory(databaseConnectionDetails);

		// Create also the connection to dwhrep.
		this.createDwhrepRockFactory();

		if(checkBusyhourPlaceholdersExist()){
			updateBusyhourEnableFlags();
			updateBusyhourPlaceholders();
			updateAggregationRules();
			copyCustomBusyhours();
			copyCustomBusyhourmapping();
			copyCustomBusyhourRankkeys();
			copyCustomBusyhourSource();
		}
	}

	/**
	 * This function creates the RockFactory to dwhrep. The created RockFactory
	 * is inserted in class variable dwhrepRockFactory.
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
				Meta_databases targetMetaDatabase = (Meta_databases) metaDatabases
						.get(0);
				this.dwhrepRockFactory = new RockFactory(targetMetaDatabase
						.getConnection_string(), targetMetaDatabase
						.getUsername(), targetMetaDatabase.getPassword(),
						etlrepRockFactory.getDriverName(), "PreinstallCheck",
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
	 * This function creates the rockfactory object to etlrep from the database
	 * connection details read from ETLCServer.properties file.
	 * 
	 * @param databaseConnectionDetails
	 * @return Returns the created RockFactory.
	 */
	private RockFactory createEtlrepRockFactory(
			final Map<String, String> databaseConnectionDetails)
			throws BuildException {

		final RockFactory rockFactory;
		final String databaseUsername = databaseConnectionDetails.get(
				"etlrepDatabaseUsername").toString();
		final String databasePassword = databaseConnectionDetails.get(
				"etlrepDatabasePassword").toString();
		final String databaseUrl = databaseConnectionDetails.get(
				"etlrepDatabaseUrl").toString();
		final String databaseDriver = databaseConnectionDetails.get(
				"etlrepDatabaseDriver").toString();

		try {
			rockFactory = new RockFactory(databaseUrl, databaseUsername,
					databasePassword, databaseDriver, "PreinstallCheck", true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Unable to initialize database connection.", e);
		}

		if (rockFactory == null){
			throw new BuildException(
					"Unable to initialize database connection. Please check the settings in the ETLCServer.properties file.");
		}
			return rockFactory;
	}

	/**
	 * This function returns a previous version of techpack activation if it
	 * exists in table TPActivation. If it doesn't exist, null is returned.
	 * 
	 * @param techPackName
	 *            is the name of the techpack to search for.
	 * @return Returns Tpactivation instace if a previous version of
	 *         TPActivation exists, otherwise returns null.
	 */
	private Tpactivation getPredecessorTPActivation(final String techPackName)
			throws BuildException {

		Tpactivation targetTPActivation = null;

		try {
			final Tpactivation whereTPActivation = new Tpactivation(
					this.dwhrepRockFactory);
			whereTPActivation.setTechpack_name(techPackName);
			final TpactivationFactory tpActivationFactory = new TpactivationFactory(
					this.dwhrepRockFactory, whereTPActivation);

			final Vector<Tpactivation> tpActivations = tpActivationFactory
					.get();
			if (tpActivations.size() > 0) {
				targetTPActivation = tpActivations.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Getting predecessor TPActivation failed.", e);
		}

		return targetTPActivation;
	}

	/**
	 * This method performs a check to see if there are Placeholders 
	 * from the "old TP". 
	 * If there is, then the "old TP" contains both 
	 * PPs and CPs and its data needs to be copied to the "new TP".
	 * If there isn't, then the "old TP" doesn't contain any information 
	 * which needs to be copied over to "new TP".
	 * @return TRUE (if Placeholders exist), FALSE otherwise.
	 */
	private boolean checkBusyhourPlaceholdersExist(){
		boolean result = false;
		String prevversionid = "";
		try {
			System.out.println("Checking the presence of BH Placeholders in Active TechPack...");

			Tpactivation preTPActivation = getPredecessorTPActivation(techPackName);
			// if targetTPActivation is null (no previously activated techpacks
			// in system) no need to do anything
			if (preTPActivation != null){
				// fetch the previous (old) TP BusyhourPlaceholders
				prevversionid = preTPActivation.getVersionid();
				final Busyhourplaceholders oldBHPlaceholdersSearch = new Busyhourplaceholders(this.dwhrepRockFactory);
				oldBHPlaceholdersSearch.setVersionid(prevversionid);
				BusyhourplaceholdersFactory oldBHPlaceholdersFactory = new BusyhourplaceholdersFactory(
						this.dwhrepRockFactory, oldBHPlaceholdersSearch);
				if(oldBHPlaceholdersFactory.size() > 0){
					result = true;	
				}
			}
		} catch (SQLException e) {
			System.out.println("Failed to check the presence of BH Placeholders in Active TechPack...");
			e.printStackTrace();
		} catch (RockException e) {
			System.out.println("Failed to check the presence of BH Placeholders in Active TechPack...");
			e.printStackTrace();
		}
		if(result){
			System.out.println("...Active TP: "+prevversionid+" has placeholders. ");
		}else{
			System.out.println("...Active TP: "+prevversionid+" has no placeholders. There are no Product or Custom Placeholders to copy to new TP:"+techPackVersionID);
		}
		return result;
	}
	
	
	/**
	 * Updates the busyhours enable flags to match the previously activated TP
	 * @return
	 */
	private int updateBusyhourEnableFlags() {
		int updatedBusyhourStatuses = 0;

		try {

			
			System.out.println("Checking Busyhour Product Placeholder enabled states...");
			
			Tpactivation preTPActivation = getPredecessorTPActivation(techPackName);

			// if targetTPActivation is null (no previously activated techpacks
			// in system) no need to do anything
			if (preTPActivation != null) {

				

				
				// fetch the previous (old) techpacks product busyhours
				String prevversionid = preTPActivation.getVersionid();
				final Busyhour prevbh = new Busyhour(this.dwhrepRockFactory);
				prevbh.setVersionid(prevversionid);
				prevbh.setPlaceholdertype("PP");
				BusyhourFactory prevbhF = new BusyhourFactory(
						this.dwhrepRockFactory, prevbh);

				// fetch the installed (new) techpacks product busyhours
				final Busyhour installedbh = new Busyhour(
						this.dwhrepRockFactory);
				installedbh.setVersionid(techPackVersionID);
				installedbh.setPlaceholdertype("PP");
				BusyhourFactory installedbhF = new BusyhourFactory(
						this.dwhrepRockFactory, installedbh);


				
				// loop all new TP busyhours match them with old TP busyhours
				for (Busyhour oldBH : prevbhF.get()) {

					for (Busyhour newBH : installedbhF.get()) {

						// if we found a match and the new TP bhcriteria is not empty we change the new TP busyhour
						// status to old TP busyhour status
						if (oldBH.getBhlevel().equals(newBH.getBhlevel())
								&& oldBH.getBhtype().equals(newBH.getBhtype())
								&& !oldBH.getBhcriteria().equals("")) {

							newBH.setEnable(oldBH.getEnable());
							newBH.saveToDB();
							updatedBusyhourStatuses++;
						}
					}
				}
			} else {
				System.out.println("No previous tecpack installed, no need to update busyhour enabled states.");
			}
			
			System.out.println("Maintaining "+updatedBusyhourStatuses+" Busyhour Product Placeholder enabled states.");
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Maintaining Busyhour Product Placeholder enabled states failed.", e);
		}
		return updatedBusyhourStatuses;
	}

	/**
	 * Updates both the Product and Custom AggregationRules.
	 * @return
	 */
	private int updateAggregationRules() {
		int updatedBusyhourStatuses = 0;

		try {
			System.out.println("Updating AggregationRules...");
			
			Tpactivation preTPActivation = getPredecessorTPActivation(techPackName);

			// if targetTPActivation is null (no previously activated techpacks
			// in system) no need to do anything
			if (preTPActivation != null) {
				// fetch the previous (old) techpacks Aggregationrule (PP & CP)
				String prevversionid = preTPActivation.getVersionid();
				//Update the Product Busyhour AggergationRules.
				updateProductBHAggregationRules(prevversionid);
				//Update the Custom Busyhour AggregationRules.
				copyCustomBHAggregationRules(prevversionid);
			} else {
				System.out.println("No previous tecpack installed, no need to update AggregationRules.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Updating AggregationRules failed.", e);
		}
		return updatedBusyhourStatuses;
	}

	/**
	 * This  method is required to change the ENABLED status of the AggregationRules that
	 * existed on the old TP. This method will not change the ENABLED status of new 
	 * Product Busyhours delivered in the new TP. 
	 * This method must first find all old PP's that have a source_type (i.e. it's not an empty String).
	 * This means that these PP's existed in the old TP. The ENABLED status of the old PP's must be 
	 * updated to the new TP as the user could enable/disable a PP in the AdminUI, thus enabling/disabling 
	 * the linked AggregationRule (DAY|WEEK|MONTH).  
	 * @param oldVersionId
	 * @return
	 */
	private int updateProductBHAggregationRules(String oldVersionId){
		int updatedPBHAggregationRules = 0;
		try {
			System.out.println("Updating AggregationRules for Product Busyhours...");

			//Get all AggregationRules from oldTP
			final Aggregationrule oldAggregationRuleSearch = new Aggregationrule(this.dwhrepRockFactory);
			oldAggregationRuleSearch.setVersionid(oldVersionId);
			oldAggregationRuleSearch.setTarget_level("RANKBH");
			oldAggregationRuleSearch.setAggregationscope("DAY");
			AggregationruleFactory oldAggregationRuleFactory = new AggregationruleFactory(
					this.dwhrepRockFactory, oldAggregationRuleSearch);

			for(Aggregationrule oldAggregationRule : oldAggregationRuleFactory.get()){
				String bhtype = oldAggregationRule.getBhtype();
				if(bhtype.startsWith("PP", bhtype.lastIndexOf("_")+1)){
					if(oldAggregationRule.getSource_type().equals("")){
						//Do nothing. This PP doesn't exist on the old TP.
					}else{
						//Copy the enabled state to the DAY, WEEK and MONTH
						//get the DAY, WEEK, MONTH AggregationRule...
						final Aggregationrule newAggregationRuleSearch = new Aggregationrule(this.dwhrepRockFactory);
						newAggregationRuleSearch.setVersionid(techPackVersionID);
						newAggregationRuleSearch.setTarget_level("RANKBH");
						newAggregationRuleSearch.setTarget_type(oldAggregationRule.getTarget_type());
						newAggregationRuleSearch.setBhtype(oldAggregationRule.getBhtype());
						AggregationruleFactory newAggregationRuleFactory = new AggregationruleFactory(
								this.dwhrepRockFactory, newAggregationRuleSearch);
						for(Aggregationrule newAggregationRule:newAggregationRuleFactory.get()){
							newAggregationRule.setEnable(oldAggregationRule.getEnable());
							newAggregationRule.saveToDB();
							updatedPBHAggregationRules++;
							System.out.println("Updated enabled state of: "+newAggregationRule.getAggregation());
						}
					}
				}
				
			}
			System.out.println("Updated "+updatedPBHAggregationRules+" Product Busyhour AggregationRules.");

		}catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Updating AggregationRules (Product Busyhours) enabled flags failed.", e);
		}

		return updatedPBHAggregationRules;
	}
	
	/**
	 * 
	 * @param oldVersionId
	 * @return
	 */
	private int copyCustomBHAggregationRules(String oldVersionId){
		int updatedCBHAggregationRules = 0;
		//Add the name of the banned setters in lowercase!
		ArrayList<String> bannedSetters = new ArrayList<String>();
		bannedSetters.add("setversionid");
		bannedSetters.add("settarget_mtableid");
		bannedSetters.add("setsource_mtableid");

		try {
			System.out.println("Updating AggregationRules for Custom Busyhours...");

			//Get all AggregationRules from oldTP
			final Aggregationrule oldDAYAggregationRuleSearch = new Aggregationrule(this.dwhrepRockFactory);
			oldDAYAggregationRuleSearch.setVersionid(oldVersionId);
			oldDAYAggregationRuleSearch.setTarget_level("RANKBH");
			oldDAYAggregationRuleSearch.setAggregationscope("DAY");
			AggregationruleFactory oldDAYAggregationRuleFactory = new AggregationruleFactory(
					this.dwhrepRockFactory, oldDAYAggregationRuleSearch);

			for(Aggregationrule oldDAYAggregationRule: oldDAYAggregationRuleFactory.get()){
				String bhtype = oldDAYAggregationRule.getBhtype();
				if(bhtype.startsWith("CP", bhtype.lastIndexOf("_")+1)){
					if(oldDAYAggregationRule.getSource_type().equals("")){
						//Do nothing. This CP doesn't exist on the old TP.
					}else{
						//Copy AggregationRules to  DAY, WEEK and MONTH
						//now need to get all the CP's from oldTP and newTP that match this criteria!
						//AggregationRules from old TP.
						final Aggregationrule oldAggregationRuleSearch = new Aggregationrule(this.dwhrepRockFactory);
						oldAggregationRuleSearch.setVersionid(oldVersionId);
						oldAggregationRuleSearch.setTarget_level("RANKBH");
						oldAggregationRuleSearch.setTarget_type(oldDAYAggregationRule.getTarget_type());
						oldAggregationRuleSearch.setBhtype(oldDAYAggregationRule.getBhtype());
						AggregationruleFactory oldAggregationRuleFactory = new AggregationruleFactory(
								this.dwhrepRockFactory, oldAggregationRuleSearch);

						//AggregationRules from new TP.
						final Aggregationrule newAggregationRuleSearch = new Aggregationrule(this.dwhrepRockFactory);
						newAggregationRuleSearch.setVersionid(techPackVersionID);
						newAggregationRuleSearch.setTarget_level("RANKBH");
						newAggregationRuleSearch.setTarget_type(oldDAYAggregationRule.getTarget_type());
						newAggregationRuleSearch.setBhtype(oldDAYAggregationRule.getBhtype());
						AggregationruleFactory newAggregationRuleFactory = new AggregationruleFactory(
								this.dwhrepRockFactory, newAggregationRuleSearch);

						for (Aggregationrule oldAggregationRule : oldAggregationRuleFactory.get()) {
							for (Aggregationrule newAggregationRule : newAggregationRuleFactory.get()) {
								if(oldAggregationRule.getAggregation().equals(newAggregationRule.getAggregation()) 
										&& oldAggregationRule.getRuleid().equals(newAggregationRule.getRuleid())){
									copy(oldAggregationRule, newAggregationRule, bannedSetters);
									String mtableid = oldAggregationRule.getSource_mtableid().replace(oldVersionId, techPackVersionID);
									newAggregationRule.setSource_mtableid(mtableid);
									newAggregationRule.saveToDB();
									updatedCBHAggregationRules++;
									System.out.println("Copied: "+newAggregationRule.getAggregation());
								}
							}
						}
					}					
				}
			}
			System.out.println("Copied "+updatedCBHAggregationRules+" Custom Busyhour AggregationRules.");
		}catch(Exception e){
			e.printStackTrace();
			throw new BuildException(
					"Copying AggregationRules (Custom Busyhours) failed.", e);
		}
		return updatedCBHAggregationRules;
	}
	/**
	 * Updates the number of CP Busyhour Placeholders in new TP to match the old TP.
	 * @return
	 */
	private int updateBusyhourPlaceholders(){
		int updatedBusyhourPlaceholders = 0;

		try {

			
			System.out.println("Updating Busyhour Placeholders...");
			
			Tpactivation preTPActivation = getPredecessorTPActivation(techPackName);

			// if targetTPActivation is null (no previously activated techpacks
			// in system) no need to do anything
			if (preTPActivation != null) {
				
				// fetch the previous (old) TP BusyhourPlaceholders
				String prevversionid = preTPActivation.getVersionid();
				final Busyhourplaceholders oldBHPlaceholdersSearch = new Busyhourplaceholders(this.dwhrepRockFactory);
				oldBHPlaceholdersSearch.setVersionid(prevversionid);
				BusyhourplaceholdersFactory oldBHPlaceholdersFactory = new BusyhourplaceholdersFactory(
						this.dwhrepRockFactory, oldBHPlaceholdersSearch);

				// fetch the installed (new) TP BusyhourPlaceholders
				final Busyhourplaceholders newBHPlaceholdersSearch = new Busyhourplaceholders(
						this.dwhrepRockFactory);
				newBHPlaceholdersSearch.setVersionid(techPackVersionID);
				BusyhourplaceholdersFactory newBHPlaceholdersFactory = new BusyhourplaceholdersFactory(
						this.dwhrepRockFactory, newBHPlaceholdersSearch);


				
				// loop all new TP busyhours match them with old TP busyhours
				for (Busyhourplaceholders oldBHPlaceholders : oldBHPlaceholdersFactory.get()) {

					for (Busyhourplaceholders newBHPlaceholders : newBHPlaceholdersFactory.get()) {
						
						if (oldBHPlaceholders.getBhlevel().equals(newBHPlaceholders.getBhlevel())) {
							newBHPlaceholders.setCustomplaceholders(oldBHPlaceholders.getCustomplaceholders());
							newBHPlaceholders.saveToDB();
							updatedBusyhourPlaceholders++;
						}
					}
				}
			} else {
				System.out.println("No previous tecpack installed, no need to update Busyhour Placeholders.");
			}
			System.out.println("Updated "+updatedBusyhourPlaceholders+" Busyhour Placeholders.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Updating Busyhour Placeholders failed.", e);
		}
		return updatedBusyhourPlaceholders;
		
	}
	
	/**
	 * Copies the Custom Busyhours from the old TP into the new TP.
	 * @return
	 */
	private int copyCustomBusyhours() {
		int updatedCustomBusyhours = 0;

		//Add the name of the banned setters in lowercase!
		ArrayList<String> bannedSetters = new ArrayList<String>();
		bannedSetters.add("setversionid");
		bannedSetters.add("settargetversionid");

		try {
			System.out.println("Copying Custom Busyhours...");
			
			Tpactivation preTPActivation = getPredecessorTPActivation(techPackName);

			// if targetTPActivation is null (no previously activated techpacks
			// in system) no need to do anything
			if (preTPActivation != null) {

				// fetch the previous (old) techpacks product busyhours
				String prevversionid = preTPActivation.getVersionid();
				final Busyhour prevbh = new Busyhour(this.dwhrepRockFactory);
				prevbh.setVersionid(prevversionid);
				prevbh.setPlaceholdertype("CP");
				BusyhourFactory prevbhF = new BusyhourFactory(
						this.dwhrepRockFactory, prevbh);

				// fetch the installed (new) techpacks product busyhours
				final Busyhour installedbh = new Busyhour(
						this.dwhrepRockFactory);
				installedbh.setVersionid(techPackVersionID);
				installedbh.setPlaceholdertype("CP");
				BusyhourFactory installedbhF = new BusyhourFactory(
						this.dwhrepRockFactory, installedbh);
				
				
				//loop all new TP busyhours match them with old TP busyhours
				for (Busyhour oldBH : prevbhF.get()) {
					for (Busyhour newBH : installedbhF.get()) {
						if (oldBH.getBhlevel().equals(newBH.getBhlevel())
							&& oldBH.getBhtype().equals(newBH.getBhtype())
							&& !oldBH.getBhcriteria().equals("")) {
						
							//need to set all the columns
							//get a list of all the setters allowed on Busyhour
							copy(oldBH, newBH, bannedSetters);
							newBH.saveToDB();
							updatedCustomBusyhours++;
							break;
						}
					}
				}
			}
				
			else {
				System.out.println("No previous tecpack installed, no need to copy Custom Busyhour(s).");
			}
			
			System.out.println("Copied "+updatedCustomBusyhours+" Custom Busyhour(s).");
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Copying Custom Busyhours failed.", e);
		}
		return updatedCustomBusyhours;
	}

	/**
	 * Copies the Custom BusyhourRankkeys from old TP to new TP
	 * @return
	 */
	private int copyCustomBusyhourRankkeys() {
		int copiedCustomBHRankkeys = 0;

		//Add the name of the banned setters in lowercase!
		ArrayList<String> bannedSetters = new ArrayList<String>();
		bannedSetters.add("setversionid");
		bannedSetters.add("settargetversionid");

		try {
			System.out.println("Copying Custom BusyhourRankkeys...");
			
			Tpactivation preTPActivation = getPredecessorTPActivation(techPackName);

			// if targetTPActivation is null (no previously activated techpacks
			// in system) no need to do anything
			if (preTPActivation != null) {

				// fetch the previous (old) techpacks custom Busyhourrankkeys
				String prevversionid = preTPActivation.getVersionid();
				final Busyhourrankkeys oldBHRankKeysSearch = new Busyhourrankkeys(this.dwhrepRockFactory);
				oldBHRankKeysSearch.setVersionid(prevversionid);
				BusyhourrankkeysFactory oldBHRankkeysFactory = new BusyhourrankkeysFactory(
						this.dwhrepRockFactory, oldBHRankKeysSearch);
								
				//loop all new TP busyhours match them with old TP busyhours
				for (Busyhourrankkeys oldBHRankkeys : oldBHRankkeysFactory.get()) {
					if(oldBHRankkeys.getBhtype().startsWith("CP")){ //only look at the CP's
						Busyhourrankkeys copiedBHRankkeys = new Busyhourrankkeys(this.dwhrepRockFactory);
						copy(oldBHRankkeys, copiedBHRankkeys, bannedSetters);
						copiedBHRankkeys.setVersionid(techPackVersionID);//TODO: Look at this for custom TP
						
						//need to get the targetVersionID. This can be obtained from the Busyhour table.
						final Busyhour busyhour = new Busyhour(this.dwhrepRockFactory);
						busyhour.setVersionid(techPackVersionID);
						busyhour.setBhlevel(copiedBHRankkeys.getBhlevel());
						busyhour.setBhtype(copiedBHRankkeys.getBhtype());
						final BusyhourFactory busyhourFactory = new BusyhourFactory(this.dwhrepRockFactory, busyhour);
						
						if(busyhourFactory.size() == 0){
							throw new Exception("There is no entry in the Busyhour for: versionID="+techPackVersionID+ ", BHLevel="+copiedBHRankkeys.getBhlevel()+ ", BHType="+copiedBHRankkeys.getBhtype());
						}
						
						String targetVersionID = busyhourFactory.getElementAt(0).getTargetversionid();

						//set the TargetversionID into the copied RankKeys entry.
						copiedBHRankkeys.setTargetversionid(targetVersionID);

						copiedBHRankkeys.saveToDB();
						copiedCustomBHRankkeys++;
					}
				}
			}
				
			else {
				System.out.println("No previous tecpack installed, no need to copy Custom BusyhourRankkeys.");
			}
			
			System.out.println("Copied "+copiedCustomBHRankkeys+" Custom BusyhourRankkeys.");
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Copying Custom BusyhourRankkeys failed.", e);
		}
		return copiedCustomBHRankkeys;
	}

	
	/**
	 * Copies the Custom BusyhourSource from old TP to new TP
	 * @return
	 */
	private int copyCustomBusyhourSource() {
		int copiedCustomBHSource = 0;
		//Add the name of the banned setters in lowercase!
		ArrayList<String> bannedSetters = new ArrayList<String>();
		bannedSetters.add("setversionid");
		bannedSetters.add("settargetversionid");

		try {
			System.out.println("Copying Custom BusyhourSource...");
			
			Tpactivation preTPActivation = getPredecessorTPActivation(techPackName);

			// if targetTPActivation is null (no previously activated techpacks
			// in system) no need to do anything
			if (preTPActivation != null) {

				// fetch the previous (old) techpacks custom Busyhourrankkeys
				String prevversionid = preTPActivation.getVersionid();
				final Busyhoursource oldBHSourceSearch = new Busyhoursource(this.dwhrepRockFactory);
				oldBHSourceSearch.setVersionid(prevversionid);
				BusyhoursourceFactory oldBHSourceFactory = new BusyhoursourceFactory(
						this.dwhrepRockFactory, oldBHSourceSearch);
								
				//loop all new TP busyhours match them with old TP busyhours
				for (Busyhoursource oldBHSource : oldBHSourceFactory.get()) {
					if(oldBHSource.getBhtype().startsWith("CP")){ //only look at the CP's
						Busyhoursource copiedBHSource = new Busyhoursource(this.dwhrepRockFactory);
						copy(oldBHSource, copiedBHSource, bannedSetters);
						copiedBHSource.setVersionid(techPackVersionID);
						
						//need to get the targetVersionID. This can be obtained from the Busyhour table.
						final Busyhour busyhour = new Busyhour(this.dwhrepRockFactory);
						busyhour.setVersionid(techPackVersionID);
						busyhour.setBhlevel(copiedBHSource.getBhlevel());
						busyhour.setBhtype(copiedBHSource.getBhtype());
						final BusyhourFactory busyhourFactory = new BusyhourFactory(this.dwhrepRockFactory, busyhour);
						
						if(busyhourFactory.size() == 0){
							throw new Exception("There is no entry in the Busyhour for: versionID="+techPackVersionID+ ", BHLevel="+copiedBHSource.getBhlevel()+ ", BHType="+copiedBHSource.getBhtype());
						}
						
						String targetVersionID = busyhourFactory.getElementAt(0).getTargetversionid();						
						copiedBHSource.setTargetversionid(targetVersionID);
						copiedBHSource.saveToDB();
						copiedCustomBHSource++;
					}
				}
			}
				
			else {
				System.out.println("No previous tecpack installed, no need to copy Custom BusyhourSource.");
			}
			
			System.out.println("Copied "+copiedCustomBHSource+" Custom BusyhourSource(s).");
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Copying Custom BusyhourSource failed.", e);
		}
		return copiedCustomBHSource;
	}

	

	/**
	 * This method copies the CustomBusyhourMappings from old TP to new TP.
	 * @return
	 */
	private int copyCustomBusyhourmapping() {
		int copiedCustomBusyhourmapping = 0;

		//Add the name of the banned setters in lowercase!
		ArrayList<String> bannedSetters = new ArrayList<String>();
		bannedSetters.add("setversionid");
		bannedSetters.add("settypeid");
		bannedSetters.add("settargetversionid");
		
		try {
			System.out.println("Copying Custom Busyhourmapping...");
			
			Tpactivation preTPActivation = getPredecessorTPActivation(techPackName);

			// if targetTPActivation is null (no previously activated techpacks
			// in system) no need to do anything
			if (preTPActivation != null) {

				// fetch the previous (old) techpacks custom busyhourmapping
				String prevversionid = preTPActivation.getVersionid();
				final Busyhourmapping oldBHMappingSearch = new Busyhourmapping(this.dwhrepRockFactory);
				oldBHMappingSearch.setVersionid(prevversionid);
				BusyhourmappingFactory oldBHMFactory = new BusyhourmappingFactory(
						this.dwhrepRockFactory, oldBHMappingSearch);

				// fetch the installed (new) techpacks custom busyhourmapping
				final Busyhourmapping newBHMappingSearch = new Busyhourmapping(this.dwhrepRockFactory);
				newBHMappingSearch.setVersionid(techPackVersionID);
				BusyhourmappingFactory newBHMFactory = new BusyhourmappingFactory(
						this.dwhrepRockFactory, newBHMappingSearch);
				

				//loop all new TP busyhourMapping match them with old TP busyhourMapping
				for (Busyhourmapping oldBHMapping : oldBHMFactory.get()) {
					if(oldBHMapping.getBhtype().startsWith("CP")){ //only look at CP*
						for (Busyhourmapping newBHMapping : newBHMFactory.get()) {
							//need to massage the TypeId of the oldObject so that it can be used to match 
							//against the newObject.This is needed because someone thought it was a 
							//good idea to concatenate a key value with another value!
							oldBHMapping.setTypeid(oldBHMapping.getTypeid().replace(prevversionid, techPackVersionID));
							if (oldBHMapping.getBhlevel().equals(newBHMapping.getBhlevel())
									&& oldBHMapping.getBhtype().equals(newBHMapping.getBhtype())
									&& oldBHMapping.getBhobject().equals(newBHMapping.getBhobject())
									&& oldBHMapping.getTypeid().equals(newBHMapping.getTypeid())) {
								//need to set all the columns
								//get a list of all the setters allowed on Busyhour
								copy(oldBHMapping, newBHMapping, bannedSetters);
								newBHMapping.saveToDB();
								copiedCustomBusyhourmapping++;
								break;
							}
						}
					}
				}
			}
				
			else {
				System.out.println("No previous tecpack installed, no need to copy Custom Busyhourmapping.");
			}
			
			System.out.println("Copied "+copiedCustomBusyhourmapping+" Custom Busyhourmapping(s).");
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Copying Custom Busyhourmapping failed.", e);
		}
		return copiedCustomBusyhourmapping;
  }
	

	public String getTechPackName() {
		return techPackName;
	}

	public void setTechPackName(String techPackName) {
		this.techPackName = techPackName;
	}

	public String getTechPackMetadataVersion() {
		return String.valueOf(techPackMetadataVersion);
	}

	public void setTechPackMetadataVersion(String techPackMetadataVersion) {
		this.techPackMetadataVersion = Integer.parseInt(techPackMetadataVersion);
	}

	public String getBuildNumber() {
		return  String.valueOf(buildNumber);
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = Integer.parseInt(buildNumber);
	}

	public String getTechPackVersion() {
		return techPackVersion;
	}

	public void setTechPackVersion(String techPackVersion) {
		this.techPackVersion = techPackVersion;
	}

	/**
	 * Find all the setter methods from the given Class. 
	 * The returned Methods do not include setter methods from the "banned list". 
	 * The "banned list" are methods which are not columns in the table 
	 * @param className
	 * @return
	 */
	public ArrayList<Method> findSetterMethods(Class className) {
		ArrayList<String> bannedSetters = new ArrayList<String>();
		bannedSetters.add("setModifiedColumns");
		bannedSetters.add("setcolumnsAndSequences");
		bannedSetters.add("setDefaults");
		bannedSetters.add("setNewItem");
		bannedSetters.add("setValidateData");
		bannedSetters.add("setOriginal");
		
		ArrayList<Method> setterMethods = new ArrayList<Method>();
		
		for(Method m:className.getDeclaredMethods()){
			if(m.getName().startsWith("set")){
				if(!bannedSetters.contains(m.getName())){
					setterMethods.add(m);
				}
			}
		}
		return setterMethods;
	}


	/**
	 * This method takes two Table Objects (old and new) and copies one to the other.
	 * The old versionid is not copied to the new Table Object.
	 * @param oldTableObject
	 * @param newTableObject
	 * @param bannedSetters
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void copy(Object oldTableObject, Object newTableObject, ArrayList<String> bannedSetters)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		ArrayList<Method> setters = findSetterMethods(newTableObject.getClass());
		Iterator<Method> methodIterator = setters.iterator();

		while (methodIterator.hasNext()) {
			Method setter = methodIterator.next();
			if(bannedSetters.contains(setter.getName().toLowerCase())){
				//don't copy, this is a banned setter!
			}else{
				//copy
				String getterMethodName = setter.getName().replaceFirst("set",
						"get");
				Method getter = newTableObject.getClass().getDeclaredMethod(
						getterMethodName, null);

				Object o = getter.invoke(oldTableObject, (Object[]) null);
				
				if (o instanceof Integer) {
					setter.invoke(newTableObject, (Integer) o);
				} else if (o instanceof Long) {
					setter.invoke(newTableObject, (Long) o);
				} else if (o instanceof String) {
					setter.invoke(newTableObject, (String) o);
				}
			}
		}
	}

	/**
	 * This method is used to check if the objects are equal based on the Primary key.
	 * @param oldTable
	 * @param newTable
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public boolean isPrimaryKeyMatch(Object oldTable,
			Object newTable) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		//need to get the PrimaryKey List from one of the tables.
		Method getPrimaryKeys = oldTable.getClass().getDeclaredMethod("getprimaryKeyNames", null);
		String[] list = (String[])getPrimaryKeys.invoke(oldTable, null);

		Method[] allMethods = oldTable.getClass().getDeclaredMethods();
		//Iterate over the list of methods and get the primary Key methods.
		for(Method method: allMethods){
			String tmp = method.getName();
			if(tmp.startsWith("get")){
				tmp = tmp.replaceFirst("get", ""); //remove the "get" part.
				for(String s: list){
					if(s.equalsIgnoreCase(tmp)){
						//we have found the method to allow us to fetch one of the primary keys.
						if(method.invoke(oldTable, null) != method.invoke(newTable, null)){
							return false;
						}
					}
				}
			}
		}
		return true;
	}

}
