package com.distocraft.dc5000.install.ant;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.repository.dwhrep.Mztechpacks;
import com.distocraft.dc5000.repository.dwhrep.MztechpacksFactory;

/**
 * The Class UpdateMZVersionInfo is an ant task which is triggered by the
 * installer module. The below are the following functionalities 1.Creates the
 * database connection. 2.Read and populates the TechPack information from
 * version.properties. 3.Insert/Update the TechPack information to MZTechPacks
 * table based on the existing techPack.
 * 
 * @author Venkateswaran Ponnusamy (qponven)
 * 
 */
public class UpdateMzTpVersionInfo extends Task {

	// TP's Content path, Example:
	// /eniq/sw/installer/tp_installer_temp/unzipped_tp
	private String techPackContentPath = "";

	// TP Name, Example: M_E_SGEH
	private String techPackName = "";

	// TP Version, Example: R1C
	private String techPackVersion = "";

	private String techPackVersionID;

	private String techpackProductNumber = "";

	private String techPackType = "";

	private final String techPackStatus = "ACTIVE";

	private final String INACTIVE_STATUS = "INACTIVE";

	private int techPackMetadataVersion = 0;

	private int buildNumber = 0;

	// Wrapper class for the database table MZTechPacks
	private Mztechpacks targetMZInfo;

	// Wrapper class for the database table MZTechPacks
	private Mztechpacks predecessorMZInfo = null;

	// Wrapper class for database connection
	private RockFactory dwhrepRockFactory = null;

	// Holds the version.properties's parameters and their values.
	private Properties verProps;

	/**
	 * This method checks whether the tech pack is mediation zone's tech pack,
	 * if so the tech pack version information is extracted and inserted/updated
	 * in the dwhrep.MZTechPacks table
	 * 
	 */
	@Override
	public void execute() throws BuildException {
		if (this.techPackName.startsWith("M_")) // Checks whether the tech pack
		// name starts with M_ in this
		// case we consider this tp as
		// MZ's TP
		{
			System.out.println("TP is identifed as MZ TP");

			setRockFactory();
			readTechPackVersionFile();
			updateMZVersionInfoDB();

			System.out
					.println("MZ tech pack version information is inserted/updated.");

		} else {
			System.out.println("TP is not an MZ TP...returning");
			return;
		}
	}

	/**
	 * The method creates the rock factory object(Database connection Obj) and
	 * instantiate MZTechPack Wrapper class. Note : The dwhrepdatabase
	 * properties already set by GetDBProperties.java (ant task)
	 */
	protected void setRockFactory() {
		this.dwhrepRockFactory = this.createRockFactory(getProject()
				.getProperty("dwhrepDatabaseUrl"),
				getProject().getProperty("dwhrepDatabaseUsername"),
				getProject().getProperty("dwhrepDatabasePassword"),
				getProject().getProperty("dwhrepDatabaseDriver"));
	}

	/**
	 * This function reads up the TECH_PACK_NAME/install/version.properties file
	 * of the tech pack and parses the values to class variables and ANT project
	 * properties.
	 * 
	 * @return Returns a HashMap of required installed tech packs as keys and
	 *         required versions as values.
	 */
	protected void readTechPackVersionFile() throws BuildException {

		final String targetFilePath = this.getTechPackContentPath()
				+ "/install/version.properties";
		final File targetFile = new File(targetFilePath);

		if (targetFile.isFile() == false || targetFile.canRead() == false) {
			throw new BuildException("Could not read file " + targetFilePath
					+ ". Please check that the file " + targetFilePath
					+ " exists and it can be read.");
		}
		try {
			verProps = new Properties();
			verProps.load(new FileInputStream(targetFile));
		} catch (final BuildException be) {
			throw be;
		} catch (final Exception e) {
			e.printStackTrace();
			throw new BuildException("Reading of file " + targetFilePath
					+ " failed.", e);
		}
	}

	/**
	 * This function checks and inserts or updates the techpack data to
	 * MZTechPack table.
	 * 
	 * Note: The Method is protected only because it needs to be tested.
	 * 
	 */
	protected void updateMZVersionInfoDB() throws BuildException {
		try {
			// Get the previous TP information, if no TP is installed then
			// predecessorMZInfo is null
			predecessorMZInfo = getPredecessorMZInfo();
			targetMZInfo = new Mztechpacks(this.dwhrepRockFactory);
			setMZTechPackObj();
			// If no MZ TP is present then insert the new row to the MZTechPack
			// Table
			if (predecessorMZInfo != null) {
				System.out.println("Updating the Existing MZTechPack Table");
				/*
				 * Update the previous installation of the techpack. The new
				 * techpack is the same as the old one, except the new
				 * versionid.
				 */
				if (predecessorMZInfo.getVersionid().equalsIgnoreCase(
						targetMZInfo.getVersionid())) {
					this.predecessorMZInfo.setCreationdate(new Timestamp(System
							.currentTimeMillis()));

					if (!predecessorMZInfo.getTechpack_version()
							.equalsIgnoreCase(
									targetMZInfo.getTechpack_version())) {
						predecessorMZInfo.setStatus(INACTIVE_STATUS);
						predecessorMZInfo.updateDB();
						targetMZInfo.insertDB();
					} else {
						predecessorMZInfo.updateDB();
					}
				} else {
					predecessorMZInfo.setStatus(INACTIVE_STATUS);
					predecessorMZInfo.updateDB();
					targetMZInfo.insertDB();
				}
			} else {
				targetMZInfo.insertDB();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			throw new BuildException("Creating TPActivation failed.", e);
		}
	}

	/**
	 * This function returns a previous version of MZ techpack activation if it
	 * exists in table Mztechpacks. If it doesn't exist, null is returned.
	 * 
	 * @param techPackName
	 *            is the name of the techpack to search for.
	 * @return Returns Mztechpacks instace if a previous version of Mztechpacks
	 *         exists, otherwise returns null.
	 * 
	 * 
	 *         Note: The Method is protected only because it needs to be tested.
	 */
	private Mztechpacks getPredecessorMZInfo() throws BuildException {

		Mztechpacks lclPredecessorMZInfo = null;
		try {
			final Mztechpacks whereMZInfo = new Mztechpacks(
					this.dwhrepRockFactory);
			whereMZInfo.setTechpack_name(this.techPackName);

			final MztechpacksFactory tpActivationFactory = new MztechpacksFactory(
					this.dwhrepRockFactory, whereMZInfo);

			String installedTPName = null;
			String installedTPStatus = null;

			for (final Mztechpacks mzTpForPrint : tpActivationFactory.get()) {
				installedTPName = mzTpForPrint.getTechpack_name();
				installedTPStatus = mzTpForPrint.getStatus();
				if (installedTPName.equalsIgnoreCase(this.techPackName)
						&& installedTPStatus.equalsIgnoreCase("ACTIVE")) {
					lclPredecessorMZInfo = mzTpForPrint;
					break;
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			throw new BuildException("Getting predecessor Mztechpacks failed.",
					e);
		}
		return lclPredecessorMZInfo;
	}

	/**
	 * This method sets the MZTechPack contents.
	 */
	private void setMZTechPackObj() {
		setTechPackVersionID();
		setTechPackProductNumber();
		setTechPackType();
		this.targetMZInfo.setVersionid(this.techPackVersionID);
		this.targetMZInfo.setTechpack_name(this.techPackName);
		this.targetMZInfo.setStatus(this.techPackStatus);
		this.targetMZInfo.setType(this.techPackType);
		this.targetMZInfo.setCreationdate(new Timestamp(System
				.currentTimeMillis()));
		this.targetMZInfo.setTechpack_version(this.techPackVersion);
		this.targetMZInfo.setProduct_number(this.techpackProductNumber);
	}

	/**
	 * The method returns TP's Content path, which looks like following
	 * /eniq/sw/installer/tp_installer_temp/unzipped_tp
	 * 
	 * @return
	 */
	public String getTechPackContentPath() {
		return techPackContentPath;
	}

	/**
	 * The method sets the tp content path ; this method is called by ANT
	 * internally
	 * 
	 * @param techPackContentPath
	 */
	public void setTechPackContentPath(final String techPackContentPath) {
		this.techPackContentPath = techPackContentPath;
	}

	/**
	 * The method returns TP's Name, which looks like following M_E_SGEH
	 * 
	 * @return
	 */
	public String getTechPackName() {
		return techPackName;
	}

	/**
	 * The method sets the tp Name ; this method is called by ANT internally
	 * 
	 * @param techPackName
	 */
	public void setTechPackName(final String techPackName) {
		this.techPackName = techPackName;
	}

	/**
	 * The method returns TP's Version, which looks like following R1C
	 * 
	 * @return
	 */
	public String getTechPackVersion() {
		return this.techPackVersion;
	}

	/**
	 * The method sets the tp version ; this method is called by ANT internally
	 * 
	 * @param techPackVersion
	 */
	public void setTechPackVersion(final String techPackVersion) {
		this.techPackVersion = techPackVersion;
	}

	public String getTechPackMetadataVersion() {
		return String.valueOf(techPackMetadataVersion);
	}

	/**
	 * The method sets the TechPackMetadataVersion version ; this method is
	 * called by ANT internally
	 * 
	 * @param techPackMetadataVersion
	 */
	public void setTechPackMetadataVersion(final String techPackMetadataVersion) {
		try {
			this.techPackMetadataVersion = Integer
					.parseInt(techPackMetadataVersion);
		} catch (final Exception e) {
		}
	}

	/**
	 * The method sets the techpack's product number
	 */
	private void setTechPackProductNumber() {
		this.techpackProductNumber = verProps.getProperty("product.number",
				"MZ-1");
	}

	private void setTechPackType() {
		this.techPackType = verProps.getProperty("tech_pack.type");
		if (this.techPackType == null || this.techPackType.length() <= 0) {
			this.techPackType = "MZ";
		}
	}

	public String getBuildNumber() {
		return String.valueOf(buildNumber);
	}

	/**
	 * The method sets the buildNumber ; this method is called by ANT internally
	 * 
	 * @param buildNumber
	 */
	public void setBuildNumber(final String buildNumber) {
		try {
			this.buildNumber = Integer.parseInt(buildNumber);
		} catch (final Exception e) {
		}
	}

	/**
	 * This function creates the rockfactory object from the database connection
	 * details.
	 * 
	 * @return Returns the created RockFactory.
	 */
	@SuppressWarnings("unused")
	private RockFactory createRockFactory(final String url, final String user,
			final String pwd, final String driver) throws BuildException {
		RockFactory rockFactory = null;
		try {
			rockFactory = new RockFactory(url, user, pwd, driver,
					"Mztechpacks", true);

		} catch (final Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Unable to initialize database connection.", e);
		}

		if (rockFactory == null) {
			throw new BuildException(
					"Unable to initialize database connection. Please check the settings in the ETLCServer.properties file.");
		}
		return rockFactory;
	}

	/**
	 * This method is used for Unit Testing, To populate properties Objects
	 * 
	 * @param testTeckPackVersionID
	 */
	protected void setTechPackVersionID(final String testTeckPackVersionID) {
		this.techPackVersionID = testTeckPackVersionID;
	}

	/**
	 * This method is used for Unit Testing, To populate properties Objects
	 * 
	 * @param testTechPackProductNumber
	 */
	protected void setTechPackProductNumber(
			final String testTechPackProductNumber) {
		this.techpackProductNumber = testTechPackProductNumber;
	}

	private void setTechPackVersionID() {
		if (this.techPackMetadataVersion >= 3) {
			techPackVersionID = this.techPackName + ":((" + this.buildNumber
					+ "))";
		} else if (techPackMetadataVersion == 2) {
			techPackVersionID = this.techPackName + ":b" + this.buildNumber;
		} else {
			techPackVersionID = this.techPackName + ":" + this.techPackVersion
					+ "_b" + this.buildNumber;
		}
	}

	public void printMZInfoObj() {
		System.out.println("----------------------------------------");
		System.out.println("Content of MZTechPack Table");
		System.out.println("----------------------------------------");
		System.out
				.println("techPackVersionID : " + targetMZInfo.getVersionid());
		System.out.println("TechPackName : " + targetMZInfo.getTechpack_name());
		System.out.println("TP Status : " + targetMZInfo.getStatus());
		System.out.println("TP Type  : " + targetMZInfo.getType());
		System.out.println("TP Creation Time :"
				+ targetMZInfo.getCreationdate());
		System.out.println("TP Version :" + targetMZInfo.getTechpack_version());
		System.out.println("Prd Number :" + targetMZInfo.getProduct_number());
		System.out.println("----------------------------------------");
	}
}
