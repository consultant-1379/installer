package com.distocraft.dc5000.install.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.tools.ant.BuildException;

import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.distocraft.dc5000.etl.rock.Meta_databasesFactory;
import com.distocraft.dc5000.repository.dwhrep.Dwhtechpacks;
import com.distocraft.dc5000.repository.dwhrep.DwhtechpacksFactory;
import com.distocraft.dc5000.repository.dwhrep.Mztechpacks;
import com.distocraft.dc5000.repository.dwhrep.MztechpacksFactory;
import com.distocraft.dc5000.repository.dwhrep.Tpactivation;
import com.distocraft.dc5000.repository.dwhrep.TpactivationFactory;
import com.distocraft.dc5000.repository.dwhrep.Versioning;
import com.distocraft.dc5000.repository.dwhrep.VersioningFactory;
import com.ericsson.eniq.common.RemoteExecutor;
import com.jcraft.jsch.JSchException;

import ssc.rockfactory.RockFactory;

/**
 * This class is a custom made ANT-task that prints out information about
 * installed tech packs. Copyright (c) 1999 - 2007 AB LM Ericsson Oy All rights
 * reserved.
 *
 * @author ejannbe
 */
public class GetInstalledTechPacks extends CommonTask {

	private Map<String, String> installedFeaturesMap = new HashMap<String, String>();
	private Map<String, String> installFeaturesMap;

	RockFactory etlrepRockFactory = null;

	RockFactory dwhrepRockFactory = null;

	private String showNames = new String("");

	private String showProductNumbers = new String("");

	private String showVersionNumbers = new String("");

	private String showDetails = new String("");

	private String showFeatureList = new String("");

	private final static String APPLICATION_USER = "dcuser";
	
	private final String confFeatureFilePath= "/eniq/sw/conf/install_features";
	private final String installerFeatureFilePath= "/eniq/sw/installer/installed_features";

	/**
	 * This function starts the execution of task.
	 */
	public void execute() throws BuildException {
		Map<String, String> databaseConnectionDetails;
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

		if (this.showDetails.equalsIgnoreCase("true")) {
			this.printDetailsOfInstalledTechPack();
		}
		if (this.showFeatureList.equalsIgnoreCase("true")) {
			this.printFeatureList();
		} else {
			// Print out the information about installed techpacks.
			this.printInstalledTechPack();

			// Print out the information about Mediation Zone installed techpacks.
			this.printMzInstalledTechPack();
		}

	}

	/**
	 * This function creates the rockfactory object to etlrep from the database
	 * connection details read from ETLCServer.properties file.
	 *
	 * @param databaseConnectionDetails
	 * @return Returns the created RockFactory.
	 */
	private RockFactory createEtlrepRockFactory(Map<String, String> databaseConnectionDetails) throws BuildException {
		RockFactory rockFactory = null;
		String databaseUsername = databaseConnectionDetails.get("etlrepDatabaseUsername");
		String databasePassword = databaseConnectionDetails.get("etlrepDatabasePassword");
		String databaseUrl = databaseConnectionDetails.get("etlrepDatabaseUrl");
		String databaseDriver = databaseConnectionDetails.get("etlrepDatabaseDriver");

		try {
			rockFactory = new RockFactory(databaseUrl, databaseUsername, databasePassword, databaseDriver,
					"PreinstallCheck", true);

		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException("Unable to initialize database connection.", e);
		}

		if (rockFactory == null) {
			throw new BuildException(
					"Unable to initialize database connection. Please check the settings in the ETLCServer.properties file.");
		}
		return rockFactory;
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
			Meta_databasesFactory metaDatabasesFactory = new Meta_databasesFactory(this.etlrepRockFactory,
					whereMetaDatabases);
			Vector metaDatabases = metaDatabasesFactory.get();

			if (metaDatabases != null || metaDatabases.size() == 1) {
				Meta_databases targetMetaDatabase = (Meta_databases) metaDatabases.get(0);

				this.dwhrepRockFactory = new RockFactory(targetMetaDatabase.getConnection_string(),
						targetMetaDatabase.getUsername(), targetMetaDatabase.getPassword(),
						etlrepRockFactory.getDriverName(), "PreinstallCheck", true);

			} else {
				throw new BuildException(
						"Unable to connect metadata (No dwhrep or multiple dwhreps defined in Meta_databases)");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException("Creating database connection to dwhrep failed.", e);
		}
	}

	public String getshowFeatureList() {
		return showFeatureList;
	}

	public void setshowFeatureList(String showFeatureList) {
		this.showFeatureList = showFeatureList;
	}

	public String getShowNames() {
		return showNames;
	}

	public void setShowNames(String showNames) {
		this.showNames = showNames;
	}

	public String getShowProductNumbers() {
		return showProductNumbers;
	}

	public void setShowProductNumbers(String showProductNumbers) {
		this.showProductNumbers = showProductNumbers;
	}

	public String getShowVersionNumbers() {
		return showVersionNumbers;
	}

	public void setShowVersionNumbers(String showVersionNumbers) {
		this.showVersionNumbers = showVersionNumbers;
	}

	public String getShowDetails() {
		return showDetails;
	}

	public void setShowDetails(String showDetails) {
		this.showDetails = showDetails;
	}

	/**
	 * This function prints out the details about installed tech packs.
	 *
	 * @throws BuildException
	 */
	private void printInstalledTechPack() throws BuildException {

		try {
			if (this.showNames.equalsIgnoreCase("true") == false
					&& this.showProductNumbers.equalsIgnoreCase("true") == false
					&& this.showVersionNumbers.equalsIgnoreCase("true") == false) {
				this.showNames = "true";
				this.showProductNumbers = "true";
				this.showVersionNumbers = "true";
			}

			// First get the active tech packs from table TPActivation.
			Tpactivation whereTPActivation = new Tpactivation(this.dwhrepRockFactory);
			whereTPActivation.setStatus("ACTIVE");

			TpactivationFactory tpActivationFact = new TpactivationFactory(this.dwhrepRockFactory, whereTPActivation,
					" ORDER BY techpack_name;");
			Iterator tpActivationsIter = tpActivationFact.get().iterator();

			while (tpActivationsIter.hasNext()) {
				Tpactivation currTPActivation = (Tpactivation) tpActivationsIter.next();

				// Get the data related to this active tech pack from the table
				// Versioning.
				Versioning whereVersioning = new Versioning(this.dwhrepRockFactory);
				whereVersioning.setVersionid(currTPActivation.getVersionid());

				VersioningFactory versioningFactory = new VersioningFactory(this.dwhrepRockFactory, whereVersioning);
				Vector versions = versioningFactory.get();

				if (versions.size() == 1) {
					Versioning currentVersioning = (Versioning) versions.get(0);

					if (showNames.equalsIgnoreCase("true")) {
						System.out.print(currentVersioning.getTechpack_name());

						if (showVersionNumbers.equalsIgnoreCase("true")
								|| showProductNumbers.equalsIgnoreCase("true")) {
							System.out.print(";");
						}
					}

					if (showProductNumbers.equalsIgnoreCase("true")) {

						if (currentVersioning.getProduct_number() == null
								|| currentVersioning.getProduct_number().equalsIgnoreCase("null")) {
							System.out.print("n/a");
						} else {
							System.out.print(currentVersioning.getProduct_number());
						}

						if (showVersionNumbers.equalsIgnoreCase("true")) {
							System.out.print(";");
						}
					}

					if (showVersionNumbers.equalsIgnoreCase("true")) {
						System.out.print(currentVersioning.getTechpack_version());
					}

					System.out.print("\n");

				}
			}
		} catch (Exception e) {
			throw new BuildException("Getting installed tech packs failed.");
		}
	}

	private void printMzInstalledTechPack() throws BuildException {
		try {
			final Mztechpacks mzTechPacks = new Mztechpacks(this.dwhrepRockFactory);
			mzTechPacks.setStatus("ACTIVE");

			final MztechpacksFactory mzTechPackFact = new MztechpacksFactory(this.dwhrepRockFactory, mzTechPacks,
					" ORDER BY techpack_name;");

			for (final Mztechpacks mzTpForPrint : mzTechPackFact.get()) {
				if (showNames.equalsIgnoreCase("true")) {
					System.out.print(mzTpForPrint.getTechpack_name());

					if (showVersionNumbers.equalsIgnoreCase("true") || showProductNumbers.equalsIgnoreCase("true")) {
						System.out.print(";");
					}
				}
				if (showProductNumbers.equalsIgnoreCase("true")) {

					if (mzTpForPrint.getProduct_number() == null
							|| mzTpForPrint.getProduct_number().equalsIgnoreCase("null")) {
						System.out.print("n/a");
					} else {
						System.out.print(mzTpForPrint.getProduct_number());
					}
					if (showVersionNumbers.equalsIgnoreCase("true")) {
						System.out.print(";");
					}
				}
				if (showVersionNumbers.equalsIgnoreCase("true")) {
					System.out.print(mzTpForPrint.getTechpack_version());
				}
				System.out.print("\n");
			}
		} catch (final Exception e) {
			throw new BuildException("Getting installed tech packs failed.");
		}
	}

	/**
	 * This function prints out the rstate , build number and creation date about
	 * installed tech packs.
	 *
	 * @throws BuildException
	 */
	private void printDetailsOfInstalledTechPack() throws BuildException {

		try {
			Tpactivation whereTPActivation = new Tpactivation(this.dwhrepRockFactory);
			whereTPActivation.setStatus("ACTIVE");

			TpactivationFactory tpActivationFact = new TpactivationFactory(this.dwhrepRockFactory, whereTPActivation,
					" ORDER BY techpack_name;");
			Iterator tpActivationsIter = tpActivationFact.get().iterator();
			System.out.printf("%-15s %-7s %-12s %-17s %-25s ", "TechPack Names", "RState", "Build Number",
					"Product Number", "Installed Date");
			System.out.println("");
			System.out.println("-----------------------------------------------------------------------------------");
			while (tpActivationsIter.hasNext()) {
				Tpactivation currTPActivation = (Tpactivation) tpActivationsIter.next();

				// Get the rstate and product number related to this active tech pack from the
				// table Versioning.
				Versioning whereVersioning = new Versioning(this.dwhrepRockFactory);
				whereVersioning.setVersionid(currTPActivation.getVersionid());

				// Get the Build Number and Creation Date related to this active tech pack from
				// the table DWHtechpacks.
				Dwhtechpacks dwhtechpacks = new Dwhtechpacks(this.dwhrepRockFactory);
				dwhtechpacks.setVersionid(currTPActivation.getVersionid());

				VersioningFactory versioningFactory = new VersioningFactory(this.dwhrepRockFactory, whereVersioning);
				Vector versions = versioningFactory.get();

				DwhtechpacksFactory dwhtechpacksfactory = new DwhtechpacksFactory(this.dwhrepRockFactory, dwhtechpacks);
				Vector versions1 = dwhtechpacksfactory.get();

				if ((versions.size() == 1) && (versions1.size() == 1)) {
					Versioning currentVersioning = (Versioning) versions.get(0);
					Dwhtechpacks currentVersioning1 = (Dwhtechpacks) versions1.get(0);
					String buildNumber = currentVersioning1.getVersionid();
					String productNumber = currentVersioning.getProduct_number();
					String build = "b"
							+ buildNumber.substring(buildNumber.indexOf("(") + 2, buildNumber.indexOf(")")).trim();
					System.out.printf("%-15s %-7s %-12s %-17s %-25s ", currentVersioning.getTechpack_name(),
							currentVersioning.getTechpack_version(), build, productNumber.replaceAll("\\s+", ""),
							currentVersioning1.getCreationdate());
				}
				System.out.print("\n");
			}

			// Prints the Rstate, BuildNumber, creationDate productNumber of the installed
			// MZTechpacks.
			final Mztechpacks mzTechPacks = new Mztechpacks(this.dwhrepRockFactory);
			mzTechPacks.setStatus("ACTIVE");

			final MztechpacksFactory mzTechPackFact = new MztechpacksFactory(this.dwhrepRockFactory, mzTechPacks,
					" ORDER BY techpack_name;");

			for (final Mztechpacks mzTpForPrint : mzTechPackFact.get()) {
				String buildNumber = mzTpForPrint.getVersionid();
				String productNumber = mzTpForPrint.getProduct_number();
				String build = "b"
						+ buildNumber.substring(buildNumber.indexOf("(") + 2, buildNumber.indexOf(")")).trim();
				System.out.printf("%-15s %-7s %-12s %-17s %-25s ", mzTpForPrint.getTechpack_name(),
						mzTpForPrint.getTechpack_version(), build, productNumber.replaceAll("\\s+", ""),
						mzTpForPrint.getCreationdate());
				System.out.print("\n");
			}
		} catch (Exception e) {
			throw new BuildException("Getting installed tech packs failed. " + e);
		}

	}

	private void printFeatureList() {

		try {
			File featurefile = new File(installerFeatureFilePath);
			if (featurefile.exists()) {
				System.out.println(installedFeatureDetail(installerFeatureFilePath));
			} else {
				System.out.println(" File not found in the /eniq/sw/installer/installed_features");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String installedFeatureDetail(String filePath) throws IOException {
		
		try {
			installFeaturesMap = populateInstallFeaturesMap();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line;
		while ((line = br.readLine()) != null) {
			if (!line.trim().equals("")) {
				if (isCorrectFeature(line)) {
					populateInstalledFeaturesMap(line);
				}
			}
		}
		br.close();

		String listOfInstalledFeatures;
		installFeaturesMap = populateInstallFeaturesMap();
		listOfInstalledFeatures = getListOfInstalledFeatures();
		return listOfInstalledFeatures.toString();
	}

	/*
	 * installed_feature file is not having proper readable feature 
	 * name(not having space between words i.e. EricssonWCGPMTechPack for Ericsson WCG PM Tech Pack)
	 *  so we are reading feature details of corresponding feature
	 * from install_features file present in conf dir. 
	 * */
	private boolean isCorrectFeature(String string) {
		return ((string.chars().filter(ch -> ch == ':').count() / 2) == 3);
	}

	private void populateInstalledFeaturesMap(String line) {
		String[] strArary = line.split("::");
		if(installFeaturesMap.containsKey(strArary[0])) {
			installedFeaturesMap.put(strArary[0], strArary[2] + "::" + strArary[3]);
		}else {
			installedFeaturesMap.put(strArary[0], strArary[1] + "::" +strArary[2] + "::" + strArary[3]);			
		}
	}
	
	/*
	 * To get all features from install_featre file of conf dir.
	 * */

	private Map<String, String> populateInstallFeaturesMap() throws IOException {

		Map<String, String> map = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(confFeatureFilePath));

		String line;
		while ((line = br.readLine()) != null) {
			if (!line.trim().equals("")) {
				String[] strArr = line.split("::");
				map.put(strArr[0], strArr[1]);
			}
		}
		br.close();
		return map;

	}

	private String getListOfInstalledFeatures() {

		StringBuilder sb = new StringBuilder();
		Set<String> set = installedFeaturesMap.keySet();

		for (String str : set) {
			if (installFeaturesMap.containsKey(str)) {
				sb.append(str + "::" + installFeaturesMap.get(str) + "::" + installedFeaturesMap.get(str) + "\n");
			}else {
				sb.append(str +"::" + installedFeaturesMap.get(str) + "\n");
			}
		}
		return sb.toString();
	}

}
