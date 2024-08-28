package com.distocraft.dc5000.install.ant;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ssc.rockfactory.RockFactory;
import ssc.rockfactory.RockException;

import com.distocraft.dc5000.etl.importexport.ETLCImport;
import com.distocraft.dc5000.etl.rock.Meta_collection_sets;
import com.distocraft.dc5000.etl.rock.Meta_collection_setsFactory;
import com.distocraft.dc5000.etl.rock.Meta_collections;
import com.distocraft.dc5000.etl.rock.Meta_collectionsFactory;
import com.distocraft.dc5000.etl.rock.Meta_schedulings;
import com.distocraft.dc5000.etl.rock.Meta_schedulingsFactory;
import com.distocraft.dc5000.etl.rock.Meta_transfer_actions;
import com.distocraft.dc5000.etl.rock.Meta_transfer_actionsFactory;
import com.ericsson.eniq.common.INIGet;

/**
 * This is a custom made ANT task that creates the sets used by the teck pack.
 * The sets to be created are in the unzipped_tp/set directory where all xml
 * files are parsed. In case of tech pack interface sets the directory is
 * unzipped_tp/TECH_PACK_NAME/interface.
 * 
 * @author berggren
 */
public class ETLSetImport extends Task {

	private String setDirectoryPath = new String();

	private String activatedInterface = new String();

	private String importingInterfaces = new String("false");

	private String oldPassword = null;

	private String updatePassword = null;

	private static final String DWH_INI_FILENAME = "dwh.ini";

	private static final String NIQ_INI_FILENAME = "niq.ini";

	private static String Server_Type = null;

	/**
	 * This is the filter to filter out only the files with xml extension.
	 */
	protected FileFilter xmlFileFilter = new FileFilter() {

		public boolean accept(File file) {
			if (file.isFile()) {
				if (file.canRead()) {
					String fileExtension = file.getName().substring(
							file.getName().lastIndexOf(".") + 1,
							file.getName().length());
					System.out.println("Found file " + file.getName()
							+ ". Extension is " + fileExtension);
					if (fileExtension.equalsIgnoreCase("xml")) {
						return true;
					} else{
						return false;
					}
				} else {
					System.out.println("File " + file.getName()
							+ " was not readable.");
					return false;
				}
			} else {
				return false;
			}
		}
	};

	public class metaCollectionSetEntryDetails {

		String collectionSetId = new String();

		String collectionSetName = new String();

		String description = new String();

		String versionNumber = new String();

		String enabledFlag = new String();

		String type = new String();

	}

	public class MyXmlHandler extends DefaultHandler {

		Vector entryDetailsMap = new Vector();

		String currentTagValue = new String();

		String currentFile = new String();

		String currentRevision = new String();

		public MyXmlHandler(Vector entryDetailsMap) {
			this.entryDetailsMap = entryDetailsMap;
		}

		public Vector getEntryDetailsMap() {
			return this.entryDetailsMap;
		}

		public void startDocument() {
		}

		public void endDocument() throws SAXException {
		}

		public void startElement(String uri, String name, String qName,
				Attributes atts) throws SAXException {
			currentTagValue = new String();
			if (qName.equals("META_COLLECTION_SETS")) {
				metaCollectionSetEntryDetails currentMetaCollectionSetEntry = new metaCollectionSetEntryDetails();
				currentMetaCollectionSetEntry.collectionSetId = atts
						.getValue("COLLECTION_SET_ID");
				currentMetaCollectionSetEntry.collectionSetName = atts
						.getValue("COLLECTION_SET_NAME");
				currentMetaCollectionSetEntry.description = atts
						.getValue("DESCRIPTION");
				currentMetaCollectionSetEntry.versionNumber = atts
						.getValue("VERSION_NUMBER");
				currentMetaCollectionSetEntry.enabledFlag = atts
						.getValue("ENABLED_FLAG");
				currentMetaCollectionSetEntry.type = atts.getValue("TYPE");
				this.entryDetailsMap.add(currentMetaCollectionSetEntry);
			}
		}

		public void endElement(String uri, String localName, String qName) {

		}

		/**
		 * This function reads the characters between the xml-tags.
		 */
		public void characters(char ch[], int start, int length) {
			StringBuffer charBuffer = new StringBuffer(length);
			for (int i = start; i < start + length; i++) {
				// If no control char
				if (ch[i] != '\\' && ch[i] != '\n' && ch[i] != '\r'
						&& ch[i] != '\t') {
					charBuffer.append(ch[i]);
				}
			}
			currentTagValue += charBuffer;
		}
	}

	RockFactory etlrepRockFactory = null;

	RockFactory dwhrepRockFactory = null;

	/**
	 * This function starts the installations of the tech pack sets.
	 */
	public void execute() throws BuildException {

		try{
			HashMap etlrepDatabaseConnectionDetails = new HashMap();
			etlrepDatabaseConnectionDetails.put("etlrepDatabaseUrl", getProject()
					.getProperty("etlrepDatabaseUrl"));
			etlrepDatabaseConnectionDetails.put("etlrepDatabaseUsername",
					getProject().getProperty("etlrepDatabaseUsername"));
			etlrepDatabaseConnectionDetails.put("etlrepDatabasePassword",
					getProject().getProperty("etlrepDatabasePassword"));
			etlrepDatabaseConnectionDetails.put("etlrepDatabaseDriver",
					getProject().getProperty("etlrepDatabaseDriver"));
			this.etlrepRockFactory = this
					.createRockFactory(etlrepDatabaseConnectionDetails);

			HashMap dwhrepDatabaseConnectionDetails = new HashMap();
			dwhrepDatabaseConnectionDetails.put("dwhrepDatabaseUrl", getProject()
					.getProperty("dwhrepDatabaseUrl"));
			dwhrepDatabaseConnectionDetails.put("dwhrepDatabaseUsername",
					getProject().getProperty("dwhrepDatabaseUsername"));
			dwhrepDatabaseConnectionDetails.put("dwhrepDatabasePassword",
					getProject().getProperty("dwhrepDatabasePassword"));
			dwhrepDatabaseConnectionDetails.put("dwhrepDatabaseDriver",
					getProject().getProperty("dwhrepDatabaseDriver"));
			this.dwhrepRockFactory = this
					.createRockFactory(dwhrepDatabaseConnectionDetails);

			this.importSets();
		}finally{
			if(this.dwhrepRockFactory != null){
		    	try {
					this.dwhrepRockFactory.getConnection().close();
				} catch (SQLException e) {
				}
	    	}
	    	if(this.etlrepRockFactory != null){
		    	try {
					this.etlrepRockFactory.getConnection().close();
				} catch (SQLException e) {
				}
	    	}
		}
	}

	public String getSetDirectoryPath() {
		return setDirectoryPath;
	}

	public void setSetDirectoryPath(String setDirectoryPath) {
		this.setDirectoryPath = setDirectoryPath;
	}

	/**
	 * This function creates the rockfactory object from the database connection
	 * details.
	 * 
	 * @param databaseConnectionDetails
	 *            is a HashMap containing the etlrep connection details.
	 * @return Returns the created RockFactory.
	 */
	private RockFactory createRockFactory(HashMap databaseConnectionDetails)
			throws BuildException {
		RockFactory rockFactory = null;
		String databaseUsername = new String();
		String databasePassword = new String();
		String databaseUrl = new String();
		String databaseDriver = new String();

		Set databaseConnectionKeys = databaseConnectionDetails.keySet();
		Iterator databaseConnectionKeysIterator = databaseConnectionKeys
				.iterator();
		while (databaseConnectionKeysIterator.hasNext()) {
			String databaseConnectionKey = (String) databaseConnectionKeysIterator
					.next();
			String databaseConnectionValue = (String) databaseConnectionDetails
					.get(databaseConnectionKey);

			if (databaseConnectionKey.indexOf("DatabaseUsername") != -1) {
				databaseUsername = databaseConnectionValue;
			} else if (databaseConnectionKey.indexOf("DatabasePassword") != -1) {
				databasePassword = databaseConnectionValue;
			} else if (databaseConnectionKey.indexOf("DatabaseUrl") != -1) {
				databaseUrl = databaseConnectionValue;
			} else if (databaseConnectionKey.indexOf("DatabaseDriver") != -1) {
				databaseDriver = databaseConnectionValue;
			}
		}

		try {
			rockFactory = new RockFactory(databaseUrl, databaseUsername,
					databasePassword, databaseDriver, "PreinstallCheck", true);

		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Unable to initialize database connection.", e);
		}

		if (rockFactory == null){throw new BuildException(
					"Unable to initialize database connection. Please check the settings in the ETLCServer.properties file.");
		}
		return rockFactory;
	}

	/**
	 * This function iterates through all xml files in the directory specified
	 * by class variable setDirectoryPath and parses the xml content. In the xml
	 * content exists the set's to insert to the database.
	 */
	private void importSets() throws BuildException {
		try {

			File setDirectory = new File(this.setDirectoryPath);

			File[] xmlFiles = setDirectory.listFiles(this.xmlFileFilter);

			if (xmlFiles == null || xmlFiles.length == 0) {
				System.out.println("No xml files found in "
						+ this.setDirectoryPath + " path");
			} else {
				// code fix for HP35957.
				for (int i = 0; i < xmlFiles.length; i++) {
					if (xmlFiles[i].getName().equalsIgnoreCase(
							"Tech_Pack_DC_Z_ALARM.xml")) {
						try {
							setoldPassword(oldPasswordOfAlarmTechpack());
							System.out.println("password extract from DB");
						} catch (RockException e) {
							e.printStackTrace();

						} catch (Exception e) {

							e.printStackTrace();
						}
					}else if(xmlFiles[i].getName().equalsIgnoreCase("Tech_Pack_AlarmInterfaces.xml")){
						try {
							setoldPassword(oldPasswordOfAlarmTechpack());
							System.out.println("password extracted from DB");
						} catch (RockException e) {
							e.printStackTrace();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				System.out.println("Starting to create sets.");
			}

			HashMap<String, String> newSets = new HashMap<String, String>();

			if (xmlFiles != null) {

				for (int i = 0; i < xmlFiles.length; i++) {

					File currentXmlFile = xmlFiles[i];

					Vector metaCollectionSetVector = new Vector();

					SAXParserFactory saxParserFactory = SAXParserFactory
							.newInstance();
					MyXmlHandler xmlHandler = new MyXmlHandler(
							metaCollectionSetVector);
					try {
						SAXParser saxParser = saxParserFactory.newSAXParser();
						// Parse the file
						saxParser.parse(currentXmlFile, xmlHandler);
					} catch (SAXException se) {
						se.printStackTrace();
					} catch (ParserConfigurationException pce) {
						pce.printStackTrace();
					} catch (IOException ie) {
						ie.printStackTrace();
					}

					System.out.println("Finished parsing "
							+ currentXmlFile.getAbsoluteFile() + "...");

					// Get the loaded entry details map from the xmlHandler.
					metaCollectionSetVector = xmlHandler.getEntryDetailsMap();

					Iterator metaCollectionSetIterator = metaCollectionSetVector
							.iterator();
					while (metaCollectionSetIterator.hasNext()) {
						metaCollectionSetEntryDetails currentMetaCollectionSetEntryDetails = (metaCollectionSetEntryDetails) metaCollectionSetIterator
								.next();

						newSets.put(
								currentMetaCollectionSetEntryDetails.collectionSetName,
								currentMetaCollectionSetEntryDetails.versionNumber);

						// Disable the previous sets of this interface.
						this.disablePreviousMetaCollectionSets(currentMetaCollectionSetEntryDetails.collectionSetName);
						// Disable also the schedulings of this interface.
						this.disablePreviousSchedules(currentMetaCollectionSetEntryDetails.collectionSetName);
						// Disable also the meta collections of this interface.
						this.disablePreviousMetaCollections(currentMetaCollectionSetEntryDetails.collectionSetName);
						// Disable also the meta transfer actions of this
						// interface.
						this.disablePreviousMetaTransferActions(currentMetaCollectionSetEntryDetails.collectionSetName);
					}

					System.out.println("Creating sets from file "
							+ currentXmlFile.getName());

					String propertiesDirectory = new String("");

					if (currentXmlFile.isFile() && currentXmlFile.canRead()) {
						ETLCImport etlcImport = new ETLCImport(
								propertiesDirectory,
								this.etlrepRockFactory.getConnection());
						// Create the sets and schedules from the xml file.
						etlcImport.doImport(currentXmlFile.getAbsolutePath(),
								true, true, false);
						System.out
								.println("Sets created succesfully from file "
										+ currentXmlFile.getName());
					}

					/*
					 * Get the server type from niq.ini. If the Server_Type is
					 * stats then disable the actions of
					 * SessionLogLoader_Collected_Data as it is EVENTS specific.
					 */

					Server_Type = (String) getServerType();

					System.out.println("Server_Type is :" + Server_Type);
					String ServerType = "stats";
					if ((Server_Type.equals(ServerType)) && (currentXmlFile.getName().equalsIgnoreCase("Tech_Pack_DWH_MONITOR.xml"))) {
						disableNonStatsMetaCollections("SessionLogLoader_Collected_Data");
						disableNonStatsMetaTransferActions1("CreateCollectedDataFiles");
						disableNonStatsMetaTransferActions2("SessionCollectedDataStarter");
					}

					Set<String> newSetNames = newSets.keySet();
					// Iterate all new sets and update scheduling_min
					for (String meta_coll_setname : newSetNames) {

						Meta_collection_sets whereMetaCollectionSets = new Meta_collection_sets(
								this.etlrepRockFactory);
						whereMetaCollectionSets
								.setCollection_set_name(meta_coll_setname);
						whereMetaCollectionSets.setVersion_number(newSets
								.get(meta_coll_setname));
						Meta_collection_setsFactory metaCollectionSetsFactory = new Meta_collection_setsFactory(
								this.etlrepRockFactory, whereMetaCollectionSets);
						Vector metaCollections = metaCollectionSetsFactory
								.get();
						String curruntSetName = null;
						Long curruntSetId = null;
						if (metaCollections.size() == 1) {
							Meta_collection_sets targetMetaCollectionSet = (Meta_collection_sets) metaCollections
									.get(0);
							curruntSetName = targetMetaCollectionSet
									.getCollection_set_name();
							curruntSetId = targetMetaCollectionSet
									.getCollection_set_id();
							Meta_schedulings whereMetaSchedulings = new Meta_schedulings(
									this.etlrepRockFactory);
							whereMetaSchedulings
									.setCollection_set_id(targetMetaCollectionSet
											.getCollection_set_id());
							Meta_schedulingsFactory metaSchedulingsFactory = new Meta_schedulingsFactory(
									this.etlrepRockFactory,
									whereMetaSchedulings);
							Vector metaSchedulings = metaSchedulingsFactory
									.get();

							Iterator metaSchedulingsIterator = metaSchedulings
									.iterator();
							while (metaSchedulingsIterator.hasNext()) {
								Meta_schedulings currentMetaScheduling = (Meta_schedulings) metaSchedulingsIterator
										.next();
								if (currentMetaScheduling.getName().startsWith(
										"TriggerAdapter_")) {
									System.out
											.println("Updating the SCHEDULING_MIN field :  "
													+ currentMetaScheduling
															.getName()
													+ " with Set id: "
													+ currentMetaScheduling
															.getCollection_set_id()
													+ " with value: "
													+ PreinstallCheck
															.getoldPollDelay());
									currentMetaScheduling
											.setScheduling_min(PreinstallCheck
													.getoldPollDelay());
									currentMetaScheduling.updateDB();
								} else if (currentMetaScheduling.getExecution_type().equals("weekly")
				                                        || currentMetaScheduling.getExecution_type().equals("interval")) {
				                                    // Reset LAST_EXECUTION_TIME and LAST_EXEC_TIME_MS to null for "weekly" and "interval"
				                                    // If this is not done, previous execution time (prior to upgrade) will be taken as a base for executing
				                                    // This will happen even if the execution time has changed
				                                    currentMetaScheduling.setLast_exec_time_ms(null);
				                                    currentMetaScheduling.setLast_execution_time(null);
				                                    currentMetaScheduling.updateDB();
				                                }
							}
						} else {
							System.out
									.println("There should be only one unique META_COLLECTION_SET. Can not update SCHEDULING_MIN for the COLLECTION_SET_NAME: "
											+ curruntSetName
											+ " with Set ID: "
											+ curruntSetId);
						}
					}// for

					// Iterate through all new sets and deactivate them.

					Iterator newSetNamesIterator = newSetNames.iterator();
					while (newSetNamesIterator.hasNext()) {

						String currentCollectionSetName = (String) newSetNamesIterator
								.next();
						String currentCollectionVersionNumber = (String) newSets
								.get(currentCollectionSetName);
						if (this.importingInterfaces.equalsIgnoreCase("true")
								&& this.activatedInterface.equalsIgnoreCase("") == false
								&& currentCollectionSetName
										.equalsIgnoreCase(this.activatedInterface) == false) {
							Meta_collection_sets whereMetaCollectionSets = new Meta_collection_sets(
									this.etlrepRockFactory);
							whereMetaCollectionSets
									.setCollection_set_name(currentCollectionSetName);
							whereMetaCollectionSets
									.setVersion_number(currentCollectionVersionNumber);
							Meta_collection_setsFactory metaCollectionSetsFactory = new Meta_collection_setsFactory(
									this.etlrepRockFactory,
									whereMetaCollectionSets);
							Vector metaCollections = metaCollectionSetsFactory
									.get();
							if (metaCollections.size() == 1) {
								Meta_collection_sets targetMetaCollectionSet = (Meta_collection_sets) metaCollections
										.get(0);
								targetMetaCollectionSet.setEnabled_flag("N");
								targetMetaCollectionSet.updateDB();
								disablePreviousMetaCollections(targetMetaCollectionSet
										.getCollection_set_name());
								disablePreviousMetaTransferActions(targetMetaCollectionSet
										.getCollection_set_name());
								System.out.println("Disabled tech pack set "
										+ targetMetaCollectionSet
												.getCollection_set_name());
							} else {
								System.out.println("");
							}
						}
					}
					// Code fix for TR HP35957 alarmcfg

					if (currentXmlFile.getName().contains(
							"Tech_Pack_DC_Z_ALARM")) {
						System.out
								.println(" Server Found different password ,restore the old one .");
						updateOldPassword();
					}else if(currentXmlFile.getName().equalsIgnoreCase("Tech_Pack_AlarmInterfaces.xml")){
						System.out.println("Server Found different password ,restoring the old one.");
						updateOldPassword();
					}
				}
				if (xmlFiles.length != 0) {
					System.out.println("All sets created succesfully.");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException("Installation of sets failed.", e);
		}

	}

	/**
	 * This function sets all the previous versions of this interface's sets to
	 * disabled mode.
	 * 
	 * @param metaCollectionSetName
	 *            Name of the new interface.
	 */
	private void disablePreviousMetaCollectionSets(String metaCollectionSetName)
			throws BuildException {
		try {
			Meta_collection_sets whereMetaCollectionSet = new Meta_collection_sets(
					this.etlrepRockFactory);
			whereMetaCollectionSet
					.setCollection_set_name(metaCollectionSetName);
			whereMetaCollectionSet.setEnabled_flag("Y");
			Meta_collection_setsFactory metaCollectionSetFactory = new Meta_collection_setsFactory(
					this.etlrepRockFactory, whereMetaCollectionSet);
			Vector metaCollectionSets = metaCollectionSetFactory.get();
			Iterator metaCollectionSetsIterator = metaCollectionSets.iterator();
			while (metaCollectionSetsIterator.hasNext()) {
				Meta_collection_sets currentMetaCollectionSet = (Meta_collection_sets) metaCollectionSetsIterator
						.next();
				currentMetaCollectionSet.setEnabled_flag("N");
				currentMetaCollectionSet.updateDB();
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException("Disabling tech pack's sets failed.", e);
		}

	}

	/**
	 * This function disables all scheduling of the previous tech pack.
	 * 
	 * @param metaCollectionSetName
	 *            is the name of the meta collection set.
	 */
	private void disablePreviousSchedules(String metaCollectionSetName)
			throws BuildException {
		try {
			Meta_collection_sets whereMetaCollectionSet = new Meta_collection_sets(
					this.etlrepRockFactory);
			whereMetaCollectionSet
					.setCollection_set_name(metaCollectionSetName);
			Meta_collection_setsFactory metaCollectionSetFactory = new Meta_collection_setsFactory(
					this.etlrepRockFactory, whereMetaCollectionSet);
			Vector metaCollectionSets = metaCollectionSetFactory.get();
			Iterator metaCollectionSetsIterator = metaCollectionSets.iterator();
			while (metaCollectionSetsIterator.hasNext()) {
				Meta_collection_sets currentMetaCollectionSet = (Meta_collection_sets) metaCollectionSetsIterator
						.next();
				Long metaCollectionSetId = currentMetaCollectionSet
						.getCollection_set_id();
				Meta_schedulings whereMetaSchedulings = new Meta_schedulings(
						this.etlrepRockFactory);
				whereMetaSchedulings.setCollection_set_id(metaCollectionSetId);
				whereMetaSchedulings.setHold_flag("N");
				Meta_schedulingsFactory metaSchedulingsFactory = new Meta_schedulingsFactory(
						this.etlrepRockFactory, whereMetaSchedulings);
				Vector metaSchedulings = metaSchedulingsFactory.get();
				Iterator metaSchedulingsIterator = metaSchedulings.iterator();
				while (metaSchedulingsIterator.hasNext()) {
					Meta_schedulings currentMetaScheduling = (Meta_schedulings) metaSchedulingsIterator
							.next();
					currentMetaScheduling.setHold_flag("Y");
					currentMetaScheduling.updateDB();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Disabling tech pack's schedulings failed.", e);
		}
	}

	/**
	 * This function disables the previous tech pack's meta collections.
	 * 
	 * @param metaCollectionSetName
	 *            is the name of the meta collection set.
	 */
	private void disablePreviousMetaCollections(String metaCollectionSetName)
			throws BuildException {
		try {
			Meta_collection_sets whereMetaCollectionSet = new Meta_collection_sets(
					this.etlrepRockFactory);
			whereMetaCollectionSet
					.setCollection_set_name(metaCollectionSetName);
			Meta_collection_setsFactory metaCollectionSetFactory = new Meta_collection_setsFactory(
					this.etlrepRockFactory, whereMetaCollectionSet);
			Vector metaCollectionSets = metaCollectionSetFactory.get();
			Iterator metaCollectionSetsIterator = metaCollectionSets.iterator();
			while (metaCollectionSetsIterator.hasNext()) {
				Meta_collection_sets currentMetaCollectionSet = (Meta_collection_sets) metaCollectionSetsIterator
						.next();
				Long metaCollectionSetId = currentMetaCollectionSet
						.getCollection_set_id();
				Meta_collections whereMetaCollections = new Meta_collections(
						this.etlrepRockFactory);
				whereMetaCollections.setCollection_set_id(metaCollectionSetId);
				whereMetaCollections.setEnabled_flag("Y");
				Meta_collectionsFactory metaCollectionsFactory = new Meta_collectionsFactory(
						this.etlrepRockFactory, whereMetaCollections);
				Vector metaCollections = metaCollectionsFactory.get();
				Iterator metaCollectionsIterator = metaCollections.iterator();
				while (metaCollectionsIterator.hasNext()) {
					Meta_collections currentMetaCollection = (Meta_collections) metaCollectionsIterator
							.next();
					currentMetaCollection.setEnabled_flag("N");
					currentMetaCollection.updateDB();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Disabling tech pack's collection failed.", e);
		}
	}

	/**
	 * This function disables the previous tech pack's actions.
	 * 
	 * @param metaCollectionSetName
	 *            is the name of the meta collection set.
	 */
	private void disablePreviousMetaTransferActions(String metaCollectionSetName)
			throws BuildException {
		try {
			Meta_collection_sets whereMetaCollectionSet = new Meta_collection_sets(
					this.etlrepRockFactory);
			whereMetaCollectionSet
					.setCollection_set_name(metaCollectionSetName);
			Meta_collection_setsFactory metaCollectionSetFactory = new Meta_collection_setsFactory(
					this.etlrepRockFactory, whereMetaCollectionSet);
			Vector metaCollectionSets = metaCollectionSetFactory.get();
			Iterator metaCollectionSetsIterator = metaCollectionSets.iterator();
			while (metaCollectionSetsIterator.hasNext()) {
				Meta_collection_sets currentMetaCollectionSet = (Meta_collection_sets) metaCollectionSetsIterator
						.next();
				Long metaCollectionSetId = currentMetaCollectionSet
						.getCollection_set_id();
				Meta_transfer_actions whereMetaTransferActions = new Meta_transfer_actions(
						this.etlrepRockFactory);
				whereMetaTransferActions
						.setCollection_set_id(metaCollectionSetId);
				whereMetaTransferActions.setEnabled_flag("Y");
				Meta_transfer_actionsFactory metaTransferActionsFactory = new Meta_transfer_actionsFactory(
						this.etlrepRockFactory, whereMetaTransferActions);
				Vector metaTransferActions = metaTransferActionsFactory.get();
				Iterator metaTransferActionsIterator = metaTransferActions
						.iterator();
				while (metaTransferActionsIterator.hasNext()) {
					Meta_transfer_actions currentMetaTransferAction = (Meta_transfer_actions) metaTransferActionsIterator
							.next();
					currentMetaTransferAction.setEnabled_flag("N");
					currentMetaTransferAction.updateDB();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Disabling tech pack's transfer action failed.", e);
		}
	}

	private void disableNonStatsMetaTransferActions2(String transferActionName)
			throws BuildException {
		try {
			Meta_transfer_actions whereMetaTransferActions = new Meta_transfer_actions(
					this.etlrepRockFactory);
			whereMetaTransferActions
					.setTransfer_action_name(transferActionName);
			whereMetaTransferActions.setEnabled_flag("Y");
			Meta_transfer_actionsFactory metaTransferActionsFactory = new Meta_transfer_actionsFactory(
					this.etlrepRockFactory, whereMetaTransferActions);
			Vector<Meta_transfer_actions> metaTransferActions = metaTransferActionsFactory
					.get();
			Iterator<Meta_transfer_actions> metaTransferActionsIterator = metaTransferActions
					.iterator();
			while (metaTransferActionsIterator.hasNext()) {
				Meta_transfer_actions currentMetaTransferActionDisable = (Meta_transfer_actions) metaTransferActionsIterator
						.next();
				currentMetaTransferActionDisable.setEnabled_flag("N");
				currentMetaTransferActionDisable.updateDB();
			}
			System.out
					.println("Disabled SessionCollectedDataStarter action in META_TRANSFER_ACTION");
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Disabling action in META_TRANSFER_ACTION failed.", e);
		}
	}

	private void disableNonStatsMetaTransferActions1(String transferActionName)
			throws BuildException {
		Long metaCollectionId = null;
		try {
			Meta_transfer_actions whereMetaTransferActions = new Meta_transfer_actions(
					this.etlrepRockFactory);
			whereMetaTransferActions
					.setTransfer_action_name(transferActionName);
			whereMetaTransferActions.setEnabled_flag("Y");
			Meta_transfer_actionsFactory metaTransferActionsFactory = new Meta_transfer_actionsFactory(
					this.etlrepRockFactory, whereMetaTransferActions);
			Vector<Meta_transfer_actions> metaTransferActions = metaTransferActionsFactory
					.get();
			Iterator<Meta_transfer_actions> metaTransferActionsIterator = metaTransferActions
					.iterator();
			while (metaTransferActionsIterator.hasNext()) {
				Meta_transfer_actions currentMetaTransferAction = (Meta_transfer_actions) metaTransferActionsIterator
						.next();
				metaCollectionId = currentMetaTransferAction.getCollection_id();

				Meta_transfer_actions whereMetaTransferActionsDisable = new Meta_transfer_actions(
						this.etlrepRockFactory);
				whereMetaTransferActionsDisable
						.setCollection_id(metaCollectionId);
				Meta_transfer_actionsFactory metaTransferActionsFactoryDisable = new Meta_transfer_actionsFactory(
						this.etlrepRockFactory, whereMetaTransferActionsDisable);
				Vector<Meta_transfer_actions> metaTransferActionsDisable = metaTransferActionsFactoryDisable
						.get();
				Iterator<Meta_transfer_actions> metaTransferActionsIteratorDisable = metaTransferActionsDisable
						.iterator();
				while (metaTransferActionsIteratorDisable.hasNext()) {
					Meta_transfer_actions currentMetaTransferActionDisable = (Meta_transfer_actions) metaTransferActionsIteratorDisable
							.next();
					currentMetaTransferActionDisable.setEnabled_flag("N");
					currentMetaTransferActionDisable.updateDB();
				}
			}
			System.out
					.println("Disabled actions in META_TRANSFER_ACTION where COLLECTION ID is :"
							+ metaCollectionId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Disabling actions in META_TRANSFER_ACTION failed.", e);
		}
	}

	private void disableNonStatsMetaCollections(String collectionName)
			throws BuildException {
		try {
			Meta_collections whereMetaCollections = new Meta_collections(
					this.etlrepRockFactory);
			whereMetaCollections.setCollection_name(collectionName);
			Meta_collectionsFactory metaCollectionsFactory = new Meta_collectionsFactory(
					this.etlrepRockFactory, whereMetaCollections);
			Vector<Meta_collections> metaCollections = metaCollectionsFactory
					.get();
			Iterator<Meta_collections> metaCollectionsIterator = metaCollections
					.iterator();
			while (metaCollectionsIterator.hasNext()) {
				Meta_collections currentMetaCollection = (Meta_collections) metaCollectionsIterator
						.next();
				currentMetaCollection.setEnabled_flag("N");
				currentMetaCollection.updateDB();
			}
			System.out
					.println("Set SessionLogLoader_Collected_Data disabled successfully in META_COLLECTIONS");
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Disabling set SessionLogLoader_Collected_Data in META_COLLECTIONS failed.",
					e);
		}
	}

	public String getActivatedInterface() {
		return activatedInterface;
	}

	public void setActivatedInterface(String activatedInterface) {
		this.activatedInterface = activatedInterface;
	}

	public String getImportingInterfaces() {
		return importingInterfaces;
	}

	public void setImportingInterfaces(String importingInterfaces) {
		this.importingInterfaces = importingInterfaces;
	}
	
	// Code fix for TR HP35957 alarmcfg
	/**
	 * This function retrieve the previous of previous active techpack.
	 * 
	 */
	public String oldPasswordOfAlarmTechpack() throws Exception, RockException {
		String oldPwd = "eniq_alarm";

		final Meta_transfer_actions whereAction = new Meta_transfer_actions(
				this.etlrepRockFactory);
		whereAction.setEnabled_flag("Y");
		whereAction.setAction_type("AlarmHandler");
		System.out
				.println(" Get the password from Meta_transfer_actions Table ");
		final Meta_transfer_actionsFactory actionsFactory = new Meta_transfer_actionsFactory(
				this.etlrepRockFactory, whereAction);
		Vector<Meta_transfer_actions> alarmHandlerActions = actionsFactory
				.get();

		if (alarmHandlerActions.size() == 0) {
			System.out
					.println("Active AlarmHandler actions not found . So Keep the default password .");

			return oldPwd;
		}

		Iterator<Meta_transfer_actions> alarmHandlerActionsIter = alarmHandlerActions
				.iterator();

		while (alarmHandlerActionsIter.hasNext()) {

			Meta_transfer_actions currAction = (Meta_transfer_actions) alarmHandlerActionsIter
					.next();

			Properties currActionProperties = new Properties();

			String actionContents = currAction.getAction_contents();

			if (actionContents != null && actionContents.length() > 0) {

				try {
					ByteArrayInputStream bais = new ByteArrayInputStream(
							actionContents.getBytes());
					currActionProperties.load(bais);
					bais.close();
				} catch (Exception e) {
					return oldPwd;

				}
			}

			if (currActionProperties.containsKey("password")) {

				oldPwd = currActionProperties.getProperty("password");

			}

		}

		return oldPwd;
	}

	public String getoldPassword() {
		if (this.oldPassword == null) {
			this.oldPassword = "eniq_alarm";
		}
		return this.oldPassword;
	}

	public void setoldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	/**
	 * This function update the previous active techpack into newly installed
	 * techpack.
	 * 
	 */
	public void updateOldPassword() throws Exception, RockException {
		final Meta_transfer_actions whereAction = new Meta_transfer_actions(
				this.etlrepRockFactory);
		whereAction.setEnabled_flag("Y");
		whereAction.setAction_type("AlarmHandler");

		final Meta_transfer_actionsFactory actionsFactory = new Meta_transfer_actionsFactory(
				this.etlrepRockFactory, whereAction);
		Vector<Meta_transfer_actions> alarmHandlerActions = actionsFactory
				.get();

		if (alarmHandlerActions.size() == 0) {
			System.out
					.println("Active AlarmHandler actions not found . So keep the default password");

			return;
		}

		Iterator<Meta_transfer_actions> alarmHandlerActionsIter = alarmHandlerActions
				.iterator();

		while (alarmHandlerActionsIter.hasNext()) {

			Meta_transfer_actions currAction = (Meta_transfer_actions) alarmHandlerActionsIter
					.next();

			Properties currActionProperties = new Properties();

			String actionContents = currAction.getAction_contents();

			if (actionContents != null && actionContents.length() > 0) {

				try {
					ByteArrayInputStream bais = new ByteArrayInputStream(
							actionContents.getBytes());
					currActionProperties.load(bais);
					bais.close();
				} catch (Exception e) {
					return;
				}
			}
			if(!currActionProperties.containsKey("EncryptionFlag")){
				currActionProperties.put("EncryptionFlag", "N");
			}

			if (currActionProperties.containsKey("password")) {

				currActionProperties.setProperty("password",
						this.getoldPassword());

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				currActionProperties.store(baos, "");
				currAction.setAction_contents(baos.toString());
				currAction.setIsDecryptionRequired(false);
				currAction.updateDB();
				System.out.println("Retaining the same (old) password for "
						+ currAction.getTransfer_action_name());
			}

		}

	}

	public String getUpdatePassword() {
		if (this.updatePassword == null) {
			this.updatePassword = "eniq_alarm";
		}
		return this.updatePassword;
	}

	public void setUpdatePassword(String oldPassword) {
		this.updatePassword = oldPassword;
	}

	public static String getServerType() {
		File iniFile;

		// First look for dwh.ini file. If it isn't found, fall back to niq.ini

		iniFile = new File("/eniq/sw/conf", DWH_INI_FILENAME);

		if (!iniFile.exists()) {
			iniFile = new File("/eniq/sw/conf", NIQ_INI_FILENAME);
		}
		System.out.println("Reading ini file :" + iniFile);

		final INIGet iniGet = new INIGet();

		iniGet.setFile(iniFile.getPath());
		iniGet.setSection("ETLC");
		iniGet.setParameter("Server_Type");
		iniGet.execute(null);

		Server_Type = iniGet.getParameterValue();

		return Server_Type;
	}

}
