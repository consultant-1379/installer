package com.distocraft.dc5000.install.ant;

import static com.ericsson.eniq.common.Constants.ENGINE_DB_DRIVERNAME;
import static com.ericsson.eniq.common.Constants.ENGINE_DB_PASSWORD;
import static com.ericsson.eniq.common.Constants.ENGINE_DB_URL;
import static com.ericsson.eniq.common.Constants.ENGINE_DB_USERNAME;

import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.rock.Meta_collections;
import com.distocraft.dc5000.etl.rock.Meta_collectionsFactory;
import com.ericsson.eniq.repository.ETLCServerProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;


/**
 * This is custom made ANT task that calls directory checker and DHWM install
 * sets of the techpack installed by tp_installer script.
 *
 * @author Berggren
 */
public class DirectoryCheckerAndDWHMInstall extends CommonTask {

  private String techPackName = "";

  private int techPackMetadataVersion = 1;

  private String techPackVersion = "";

  private String buildNumber = "";

  private String binDirectory = "";

  private String directoryCheckerSetName = "";

  private String dwhmInstallSetName = "";

  private String installingInterface = "";

  protected Integer exitValue;

  final private Logger log = Logger.getLogger("ant.DirectoryCheckerANDDWHInstall");
  
  private static final String VECTORSFOLDER = "/eniq/sw/installer/vectors/";
  private String loadFile = new String();
  
  public static void main(final String[] args) {
    
    final DirectoryCheckerAndDWHMInstall check = new DirectoryCheckerAndDWHMInstall();
    check.setTechPackName("DWH_MONITOR");
    check.setBuildNumber("99");
    check.setTechPackMetadataVersion("3");
    check.execute();
  }
  	/**
	 * Setter method for ANT call
	 * 
	 * @param loadFile the loadFile to set
	 */
	public void setLoadFile(String loadFile) {
		this.loadFile = loadFile;
	}
  
  /**
   * This function starts the calls to directory checker and DWHM install sets.
   */
  public void execute() throws BuildException {
	  if(!(this.loadFile.equals("NULL"))) {
		  moveVectorFile();
	  }

    final ITransferEngineRMI engineRMI = connectEngine();
    reloadProperties(engineRMI);
  // refreshCache(engineRMI);
    ETLCServerProperties props =null;
	try {
		props = new ETLCServerProperties();
	} catch (IOException e) {
		
		log.warning("Properties is not initialised");
	}
    final RockFactory etlrep = createRockFactory(
    		props.getProperty(ENGINE_DB_URL),
            props.getProperty(ENGINE_DB_USERNAME),
            props.getProperty(ENGINE_DB_PASSWORD),
            props.getProperty(ENGINE_DB_DRIVERNAME)
    );
    this.directoryCheckerSetName = "Directory_Checker_" + this.techPackName;
    this.dwhmInstallSetName = "DWHM_Install_" + this.techPackName;
    try {
      if (Boolean.valueOf(installingInterface)) {
        System.out.printf("Directory checker is not started during installation of an interface.%n");
      } else {
        System.out.printf("Searching sets. Metadata version is %s%n", techPackMetadataVersion);
        if (directoryCheckerSetExists(etlrep)) {
          startAndWaitSet(engineRMI, this.techPackName, this.directoryCheckerSetName);
        } else {
          System.out.printf("Directory checker set not found %s. Set not started.%n", this.directoryCheckerSetName);
        }
      }
      if (dwhmInstallSetExists(etlrep)) {
        startAndWaitSet(engineRMI, this.techPackName, this.dwhmInstallSetName);
      } else {
        System.out.printf("DWHM Install set not found %s. Set not started.%n", this.dwhmInstallSetName);
      }
      updateTransformation(engineRMI, this.techPackName);
    } finally {
      try {
        etlrep.getConnection().close();
      } catch (SQLException e) {
    	  log.warning("Errors closing etlrep connecion!");
      }
    }
  }

  /**
   * Move vector file for loading to reference 
   * table to '/eniq/sw/installer/vectors/'
   * 
   */
  private void moveVectorFile() {
	  File loadDir = new File(VECTORSFOLDER);
	  File sourceLoadFile = new File(this.loadFile);
	  File destLoadFile = new File(VECTORSFOLDER + "Tech_Pack_" + this.techPackName + ".txt");
	  try {
		  if(!loadDir.exists()) {
			  loadDir.mkdir();
		  }
		  if(destLoadFile.exists())
			  Files.delete(destLoadFile.toPath());
		  Files.copy(sourceLoadFile.toPath(),destLoadFile.toPath());
	  } 
	  catch (IOException e) {
		  log.warning("Errors copying file to " + VECTORSFOLDER);
	  }
  }

  public String getTechPackName() {
    return techPackName;
  }

  public void setTechPackName(final String techPackName) {
    this.techPackName = techPackName;
  }

  public String getTechPackMetadataVersion() {
    return Integer.toString(techPackMetadataVersion);
  }

  public void setTechPackMetadataVersion(final String techPackMetadataVersion) {
    if (techPackMetadataVersion != null && techPackMetadataVersion.length() > 0) {
      try {
        this.techPackMetadataVersion = Integer.parseInt(techPackMetadataVersion);
      } catch (NumberFormatException nfe) {
      }
    }
  }

  public String getTechPackVersion() {
    return techPackVersion;
  }

  public void setTechPackVersion(final String techPackVersion) {
    this.techPackVersion = techPackVersion;
  }

  public void setBuildNumber(final String buildNumber) {
    this.buildNumber = buildNumber;
  }

  public String getBuildNumber() {
    return this.buildNumber;
  }

  public String getBinDirectory() {
    return binDirectory;
  }

  public void setBinDirectory(final String binDirectory) {
    this.binDirectory = binDirectory;
  }

  /**
   * This function returns true if the directory checker set exists for the tech
   * pack to be installed/updated.
   *
   * @return Returns true if the directory checker exists, otherwise returns
   *         false.
   */
  public boolean directoryCheckerSetExists(final RockFactory etlrep) throws BuildException {
    try {
      final Meta_collections targetMetaCollection = new Meta_collections(etlrep);
      targetMetaCollection.setCollection_name(this.directoryCheckerSetName);
      targetMetaCollection.setEnabled_flag("Y");
      final Meta_collectionsFactory metaCollectionsFactory = new Meta_collectionsFactory(etlrep,
        targetMetaCollection);

      final Vector<Meta_collections> targetMetaCollectionsVector = metaCollectionsFactory.get();
      final Iterator<Meta_collections> targetMetaCollectionsIterator = targetMetaCollectionsVector.iterator();

      System.out.println(targetMetaCollectionsVector.size() + " directory checker sets found with "
        + this.directoryCheckerSetName);

      // Check if the DirectoryChecker set versionnumber starts with tech pack's
      // version.
      while (targetMetaCollectionsIterator.hasNext()) {
        final Meta_collections currentMetaCollection = targetMetaCollectionsIterator.next();

        if (techPackMetadataVersion >= 3) {

          if (currentMetaCollection.getVersion_number().equals("((" + buildNumber + "))")) {
            return true;
          }

        } else {

          if (currentMetaCollection.getVersion_number().startsWith(this.techPackVersion)) {
            // Directory checker found.
            return true;
          }

        }

      }

      // No directory checker set found.
      System.out.println("No directory checker set found for " + this.directoryCheckerSetName + " where version "
        + this.techPackVersion);
      return false;

    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Checking of directory checker set failed.", e);
    }
  }

  /**
   * This function returns true if the DWHM Install set exists for the tech pack
   * to be installed/updated.
   *
   * @return Returns true if the directory checker exists, otherwise returns
   *         false.
   */
  public boolean dwhmInstallSetExists(final RockFactory etlrep) throws BuildException {
    try {
      final Meta_collections targetMetaCollection = new Meta_collections(etlrep);
      targetMetaCollection.setCollection_name(this.dwhmInstallSetName);
      final Meta_collectionsFactory metaCollectionsFactory = new Meta_collectionsFactory(etlrep,
        targetMetaCollection);

      final Vector<Meta_collections> targetMetaCollectionsVector = metaCollectionsFactory.get();
      final Iterator<Meta_collections> targetMetaCollectionsIterator = targetMetaCollectionsVector.iterator();

      System.out
        .println(targetMetaCollectionsVector.size() + " dwhminstall sets found with " + this.dwhmInstallSetName);

      // Check if the DWHM_Instal set versionnumber starts with tech pack's
      // version.
      while (targetMetaCollectionsIterator.hasNext()) {
        final Meta_collections currentMetaCollection = targetMetaCollectionsIterator.next();

        if (techPackMetadataVersion >= 3) {

          if (currentMetaCollection.getVersion_number().equals("((" + buildNumber + "))")) {
            // Directory checker found.
            return true;
          }

        } else {

          if (currentMetaCollection.getVersion_number().startsWith(this.techPackVersion)) {
            // Directory checker found.
            return true;
          }

        }

      }

      // No DWHM Install set found.
      System.out.println("No DWHM_Install set found for " + this.dwhmInstallSetName + " where version "
        + this.techPackVersion);
      return false;

    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Checking of DWHM Install set failed.", e);
    }

  }

  /**
   * This function creates the rockfactory object from the database connection
   * details.
   *
   * @return Returns the created RockFactory.
   */
  private RockFactory createRockFactory(final String databaseUrl, final String databaseUsername,
                                        final String databasePassword, final String databaseDriver) throws BuildException {

    try {
      return new RockFactory(databaseUrl, databaseUsername, databasePassword, databaseDriver, "PreinstallCheck",
        true);
    } catch (SQLException e) {
      log.warning("Errors getting etlrep connection!");
        
      throw new BuildException("Unable to initialize database connection.", e);
    } catch (RockException e) {
      log.warning("Errors getting etlrep connection!");
      throw new BuildException("Unable to initialize database connection.", e);
    }
  }

  public String getInstallingInterface() {
    return installingInterface;
  }

  public void setInstallingInterface(String installingInterface) {
    this.installingInterface = installingInterface;
  }

  public Integer getExitValue() {
    return exitValue;
  }

  public void setExitValue(Integer exitValue) {
    this.exitValue = exitValue;
  }
}
