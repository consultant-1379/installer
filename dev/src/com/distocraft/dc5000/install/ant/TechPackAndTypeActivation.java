package com.distocraft.dc5000.install.ant;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.repository.dwhrep.Measurementobjbhsupport;
import com.distocraft.dc5000.repository.dwhrep.MeasurementobjbhsupportFactory;
import com.distocraft.dc5000.repository.dwhrep.Measurementtable;
import com.distocraft.dc5000.repository.dwhrep.MeasurementtableFactory;
import com.distocraft.dc5000.repository.dwhrep.Measurementtype;
import com.distocraft.dc5000.repository.dwhrep.MeasurementtypeFactory;
import com.distocraft.dc5000.repository.dwhrep.Measurementvector;
import com.distocraft.dc5000.repository.dwhrep.MeasurementvectorFactory;
import com.distocraft.dc5000.repository.dwhrep.Referencetable;
import com.distocraft.dc5000.repository.dwhrep.ReferencetableFactory;
import com.distocraft.dc5000.repository.dwhrep.Tpactivation;
import com.distocraft.dc5000.repository.dwhrep.TpactivationFactory;
import com.distocraft.dc5000.repository.dwhrep.Typeactivation;
import com.distocraft.dc5000.repository.dwhrep.TypeactivationFactory;
import com.distocraft.dc5000.repository.dwhrep.Versioning;
import com.distocraft.dc5000.repository.dwhrep.VersioningFactory;

/**
 * This is a custom made ANT task that creates the data for techpack and type
 * activation.
 *
 * @author berggren
 */
public class TechPackAndTypeActivation extends CommonTask {

  private String techPackContentPath = "";

  private String techPackName = "";

  private String techPackVersion = "";

  private int buildNumber = 0;

  private int techPackMetadataVersion = 0;

  private String techPackVersionID;

  private RockFactory dwhrepRockFactory = null;

  private String binDirectory = null;
  
  private String newVectorFlag="";
  
  
  /**
   * Setter method for ANT call
   * Indicates if new vector handling 
   * is implemented for the techpack
   * 
   * @param newVectorFlag
   */
  public void setNewVectorFlag(final String newVectorFlag) {
	    this.newVectorFlag = newVectorFlag;
  }

  /**
   * This function starts the installation or update of techpack and type
   * activations.
   */
  public void execute() throws BuildException {

    if (techPackMetadataVersion >= 3) {
      techPackVersionID = this.techPackName + ":((" + this.buildNumber + "))";
    } else if (techPackMetadataVersion == 2) {
      techPackVersionID = this.techPackName + ":b" + this.buildNumber;
    } else {
      techPackVersionID = this.techPackName + ":" + this.techPackVersion + "_b" + this.buildNumber;
    }

    this.dwhrepRockFactory = createRockFactory(getProject().getProperty("dwhrepDatabaseUrl"), getProject()
        .getProperty("dwhrepDatabaseUsername"), getProject().getProperty("dwhrepDatabasePassword"), getProject()
        .getProperty("dwhrepDatabaseDriver"), getClass().getSimpleName());

    if (!this.binDirectory.endsWith(File.separator)) {
      // Add the missing separator char "/" from the end of the directory
      // string.
      this.binDirectory = this.binDirectory + File.separator;
    }

    if (techPackExists()) {
      System.out.println("Starting tech pack activation.");

      createTPActivation();

      System.out.println("Tech pack activation succesfully finished.");
    } else {
      System.out.println("Metadata of this techpack has not been installed. Tech pack will not activated.");
    }

  }

  public String getTechPackContentPath() {
    return techPackContentPath;
  }

  public void setTechPackContentPath(final String techPackContentPath) {
    this.techPackContentPath = techPackContentPath;
  }

  public String getTechPackVersion() {
    return techPackVersion;
  }

  public void setTechPackVersion(final String techPackVersion) {
    this.techPackVersion = techPackVersion;
  }

  public String getBuildNumber() {
    return String.valueOf(buildNumber);
  }

  public void setBuildNumber(final String buildNumber) {
    try {
      this.buildNumber = Integer.parseInt(buildNumber);
    } catch (Exception e) {
      getProject().log("Build Number was not a number '" + buildNumber + "'", Project.MSG_DEBUG);
    }
  }

  public String getTechPackMetadataVersion() {
    return String.valueOf(techPackMetadataVersion);
  }

  public void setTechPackMetadataVersion(final String techPackMetadataVersion) {
    try {
      this.techPackMetadataVersion = Integer.parseInt(techPackMetadataVersion);
    } catch (Exception e) {
      getProject().log("TechPackMetadataVersion was not a number '" + buildNumber + "'", Project.MSG_DEBUG);
    }
  }
  
  public String getTechPackName() {
	    return techPackName;
	  }

  public void setTechPackName(final String techPackName) {
	    this.techPackName = techPackName;
  }

  /**
   * This function checks and inserts or updates the techpack data to
   * TPActivation table.
   * @throws org.apache.tools.ant.BuildException On Errors
   */
  private void createTPActivation() throws BuildException {
    try {

      boolean newActivation = false;

      Tpactivation targetTPActivation = new Tpactivation(this.dwhrepRockFactory);
      final Tpactivation predecessorTPActivation = getPredecessorTPActivation(this.techPackName);

      if (predecessorTPActivation == null) {

        // Insert the techpack activation data.

        final String techPackType = getTechPackType(techPackVersionID);
        targetTPActivation.setTechpack_name(this.techPackName);
        targetTPActivation.setStatus("ACTIVE");
        targetTPActivation.setVersionid(techPackVersionID);
        targetTPActivation.setType(techPackType);
        targetTPActivation.setModified(0);
        targetTPActivation.insertDB();

        newActivation = true;
      } else {
        // Update the previous installation of the techpack.
        // The new techpack is the same as the old one, except the new
        // versionid.
        targetTPActivation = predecessorTPActivation;
        final String techPackType = getTechPackType(techPackVersionID);
        targetTPActivation.setType(techPackType);
        targetTPActivation.setVersionid(techPackVersionID);
        targetTPActivation.updateDB();
      }

      // Insert or update the values in table TypeActivation.
      saveTypeActivationData(newActivation, targetTPActivation);

    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Creating TPActivation failed.", e);
    }

  }

  /**
   * This function returns a previous version of techpack activation if it
   * exists in table TPActivation. If it doesn't exist, null is returned.
   *
   * @param techPackName
   *          is the name of the techpack to search for.
   * @return Returns Tpactivation instace if a previous version of TPActivation
   *         exists, otherwise returns null.
   * @throws org.apache.tools.ant.BuildException On Errors
   */
  private Tpactivation getPredecessorTPActivation(final String techPackName) throws BuildException {

    Tpactivation targetTPActivation = null;

    try {
      final Tpactivation whereTPActivation = new Tpactivation(this.dwhrepRockFactory);
      whereTPActivation.setTechpack_name(techPackName);
      final TpactivationFactory tpActivationFactory = new TpactivationFactory(this.dwhrepRockFactory, whereTPActivation);

      final Vector<Tpactivation> tpActivations = tpActivationFactory.get();
      if (tpActivations.size() > 0) {
        targetTPActivation = tpActivations.get(0);
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Getting predecessor TPActivation failed.", e);
    }

    return targetTPActivation;
  }



  /**
   * This function returns the type of the tech pack. The tech pack type is in
   * Versioning table.
   *
   * @param versionId
   *          Versionid of the tech pack. Used as primary key in table
   *          Versioning.
   * @return Returns the type of the tech pack. Returns empty string if no tech
   *         pack type data is not found.
   * @throws org.apache.tools.ant.BuildException On Errors
   */
  private String getTechPackType(final String versionId) throws BuildException {

    try {

      final Versioning whereVersioning = new Versioning(this.dwhrepRockFactory);
      whereVersioning.setVersionid(versionId);
      final VersioningFactory versioningFactory = new VersioningFactory(this.dwhrepRockFactory, whereVersioning);

      final Vector<Versioning> targetVersioningVector = versioningFactory.get();

      if (targetVersioningVector.size() > 0) {
        final Versioning targetVersioning = targetVersioningVector.get(0);
        return targetVersioning.getTechpack_type();
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Reading tech pack type from Versioning failed.", e);
    }

    return "";
  }

  /**
   * This method saves (inserts or updates) the data in TypeActivation-table
   * related to a TPActivation.
   *
   * @param newActivation
   *          is true if the TPActivation is a completely new TPActivation.
   *          newActivation is false if the TPActivation already exists in the
   *          database.
   * @param tpactivation
   *          Tpactivation of which TypeActivation data is to be updated.
   */

  private void saveTypeActivationData(final boolean newActivation, final Tpactivation tpactivation) {
	  boolean isVector = false;
    try {

      // This vector holds the TypeActivations to be updated.
      final Vector<Typeactivation> typeActivations = new Vector<Typeactivation>();
      final Vector<String> createdTypes = new Vector<String>();

      final String targetVersionId = tpactivation.getVersionid();

      // First get the TypeActivations of type Measurement.
      // Get all MeasurementTypes related to this VersionID.
      final Measurementtype whereMeasurementType = new Measurementtype(tpactivation.getRockFactory());
      whereMeasurementType.setVersionid(targetVersionId);
      final MeasurementtypeFactory measurementtypeFactory = new MeasurementtypeFactory(tpactivation.getRockFactory(),
          whereMeasurementType);

      final Vector<Measurementtype> targetMeasurementTypes = measurementtypeFactory.get();

      for (Measurementtype targetMeasurementType : targetMeasurementTypes) {
        final String targetTypeId = targetMeasurementType.getTypeid();
        final String targetTypename = targetMeasurementType.getTypename(); // Typename

        if (targetMeasurementType.getJoinable() != null && targetMeasurementType.getJoinable().length() != 0) {
          // Adding new PREV_ table.

          final Typeactivation preTypeActivation = new Typeactivation(tpactivation.getRockFactory());
          preTypeActivation.setTypename(targetTypename + "_PREV");
          preTypeActivation.setTablelevel("PLAIN");
          preTypeActivation.setStoragetime((long) -1);
          preTypeActivation.setType("Measurement");
          preTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
          preTypeActivation.setStatus(tpactivation.getStatus());
          preTypeActivation.setPartitionplan(null);
          typeActivations.add(preTypeActivation);
        }
        
        if(newVectorFlag.equals("true")) {
        
      //For PoC - adding new type activation for new vector reference table
        if (!isVector && targetMeasurementType.getVectorsupport() != null && targetMeasurementType.getVectorsupport() == 1) {
            final String vectorTypeName = "DIM" + tpactivation.getTechpack_name().substring(tpactivation.getTechpack_name().indexOf("_"))
                    + "_VECTOR_REFERENCE";
            if (createdTypes.contains(vectorTypeName)) {
            	isVector = true;
            }
            else {
                // Adding new vector counter table.
                final Typeactivation preTypeActivation = new Typeactivation(tpactivation.getRockFactory());
                preTypeActivation.setTypename(vectorTypeName);
                preTypeActivation.setTablelevel("PLAIN");
                preTypeActivation.setStoragetime((long) -1);
                preTypeActivation.setType("Reference");
                preTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
                preTypeActivation.setStatus(tpactivation.getStatus());
                preTypeActivation.setPartitionplan(null);
                typeActivations.add(preTypeActivation);
                createdTypes.add(vectorTypeName);
                isVector = true;
            }
        }
        }
        else {
        
        //For PoC - removing old vector handlings
        if (targetMeasurementType.getVectorsupport() != null && targetMeasurementType.getVectorsupport() == 1) {

          // Adding new Vectorcounter
          final Measurementvector mv_cond = new Measurementvector(tpactivation.getRockFactory());
          mv_cond.setTypeid(targetMeasurementType.getTypeid());
          final MeasurementvectorFactory vc_condF = new MeasurementvectorFactory(tpactivation.getRockFactory(), mv_cond);

          for (Measurementvector vc : vc_condF.get()) {

            // replace DC with DIM in DC_X_YYY_ZZZ
            final String typename = "DIM"
              + targetMeasurementType.getTypename().substring(targetMeasurementType.getTypename().indexOf("_")) + "_"
              + vc.getDataname();

            if (createdTypes.contains(typename)) {
              // all ready exists
            } else {

              // Adding new vector counter table.
              final Typeactivation preTypeActivation = new Typeactivation(tpactivation.getRockFactory());
              preTypeActivation.setTypename(typename);
              preTypeActivation.setTablelevel("PLAIN");
              preTypeActivation.setStoragetime((long) -1);
              preTypeActivation.setType("Reference");
              preTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
              preTypeActivation.setStatus(tpactivation.getStatus());
              preTypeActivation.setPartitionplan(null);
              typeActivations.add(preTypeActivation);
              createdTypes.add(typename);
            }
          }
        }
        }

        final Measurementobjbhsupport mobhs = new Measurementobjbhsupport(tpactivation.getRockFactory());
        mobhs.setTypeid(targetMeasurementType.getTypeid());
        final MeasurementobjbhsupportFactory mobhsF = new MeasurementobjbhsupportFactory(tpactivation.getRockFactory(), mobhs);

        // ELEMBH
        if ((targetMeasurementType.getElementbhsupport() != null && targetMeasurementType.getElementbhsupport() == 1)) {
          // replace DC_E_XXX with DIM_E_XXX_ELEMBH_BHTYPE
          final String typename = "DIM"
            + targetMeasurementType.getVendorid().substring(targetMeasurementType.getVendorid().indexOf("_"))
            + "_ELEMBH_BHTYPE";
          if (createdTypes.contains(typename)) {
            // all ready exists
          } else {
            // Adding new ELEMBH table.
            final Typeactivation preTypeActivation = new Typeactivation(tpactivation.getRockFactory());
            preTypeActivation.setTypename(typename);
            preTypeActivation.setTablelevel("PLAIN");
            preTypeActivation.setStoragetime((long) -1);
            preTypeActivation.setType("Reference");
            preTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
            preTypeActivation.setStatus(tpactivation.getStatus());
            preTypeActivation.setPartitionplan(null);
            typeActivations.add(preTypeActivation);
            createdTypes.add(typename);
          }
        }

        // OBJBH
        if (mobhsF != null && !mobhsF.get().isEmpty()) {
          // replace DC_E_XXX_YYY with DIM_E_XXX_YYY_BHTYPE
          final String typename = "DIM"
            + targetMeasurementType.getTypename().substring(targetMeasurementType.getTypename().indexOf("_"))
            + "_BHTYPE";
          if (createdTypes.contains(typename)) {
            // all ready exists
          } else {
            // Adding new OBJBH table.
            final Typeactivation preTypeActivation = new Typeactivation(tpactivation.getRockFactory());
            preTypeActivation.setTypename(typename);
            preTypeActivation.setTablelevel("PLAIN");
            preTypeActivation.setStoragetime((long) -1);
            preTypeActivation.setType("Reference");
            preTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
            preTypeActivation.setStatus(tpactivation.getStatus());
            preTypeActivation.setPartitionplan(null);
            typeActivations.add(preTypeActivation);
            createdTypes.add(typename);
          }
        }

        if (targetMeasurementType.getLoadfile_dup_check() != null && targetMeasurementType.getLoadfile_dup_check() == 1) {
          final String typename = targetMeasurementType.getTypename() + "_DUBCHECK";

          final Typeactivation preTypeActivation = new Typeactivation(tpactivation.getRockFactory());
          preTypeActivation.setTypename(typename);
          preTypeActivation.setTablelevel("PLAIN");
          preTypeActivation.setStoragetime((long) -1);
          preTypeActivation.setType("Reference");
          preTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
          preTypeActivation.setStatus(tpactivation.getStatus());
          preTypeActivation.setPartitionplan(null);
          typeActivations.add(preTypeActivation);
          createdTypes.add(typename);
        }

        final Measurementtable whereMeasurementTable = new Measurementtable(tpactivation.getRockFactory());
        whereMeasurementTable.setTypeid(targetTypeId);
        final MeasurementtableFactory measurementTableFactory = new MeasurementtableFactory(tpactivation.getRockFactory(),
          whereMeasurementTable);
        final Vector<Measurementtable> targetMeasurementTables = measurementTableFactory.get();

        for (Measurementtable targetMeasurementTable : targetMeasurementTables) {
          final String targetTableLevel = targetMeasurementTable.getTablelevel(); // Tablelevel

          // All the needed data is gathered from tables.
          // Add the Typeactivation of type Measurement to
          // typeActivations-vector to be saved later.
          final Typeactivation targetTypeActivation = new Typeactivation(tpactivation.getRockFactory());
          targetTypeActivation.setTypename(targetTypename);
          targetTypeActivation.setTablelevel(targetTableLevel);
          targetTypeActivation.setStoragetime((long) -1);
          targetTypeActivation.setType("Measurement");
          targetTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
          targetTypeActivation.setStatus(tpactivation.getStatus());
          targetTypeActivation.setPartitionplan(targetMeasurementTable.getPartitionplan());
          typeActivations.add(targetTypeActivation);
        }
      }

      // Next get the TypeActivations of type Reference.
      // Get all ReferenceTables related to this VersionID.
      final Referencetable whereReferenceTable = new Referencetable(tpactivation.getRockFactory());
      whereReferenceTable.setVersionid(targetVersionId);
      final ReferencetableFactory referenceTableFactory = new ReferencetableFactory(tpactivation.getRockFactory(),
          whereReferenceTable);
      final Vector<Referencetable> targetReferenceTables = referenceTableFactory.get();

      for (Referencetable targetReferenceTable : targetReferenceTables) {
        final String typename = targetReferenceTable.getTypename();

        final Typeactivation targetTypeActivation1 = new Typeactivation(tpactivation.getRockFactory());
        targetTypeActivation1.setTypename(typename);
        targetTypeActivation1.setType("Reference");
        targetTypeActivation1.setTablelevel("PLAIN");
        targetTypeActivation1.setStoragetime((long) -1);
        targetTypeActivation1.setTechpack_name(tpactivation.getTechpack_name());
        targetTypeActivation1.setStatus(tpactivation.getStatus());
        typeActivations.add(targetTypeActivation1);
        createdTypes.add(typename);
        // 20110830 EANGUAN :: Adding comparison for policy number 4 for History Dynamic (for SON)
        if ((targetReferenceTable.getUpdate_policy() != null) && ((targetReferenceTable.getUpdate_policy() == 2)
          || (targetReferenceTable.getUpdate_policy() == 3) || (targetReferenceTable.getUpdate_policy() == 4))) {

          if (!createdTypes.contains(typename + "_CURRENT_DC")) {

            // create current_dc tables
            // if reference tables update policy is dynamic (2)
            // and the table does not already contain such a row

            final Typeactivation targetTypeActivation = new Typeactivation(tpactivation.getRockFactory());
            targetTypeActivation.setTypename(typename + "_CURRENT_DC");
            targetTypeActivation.setType("Reference");
            targetTypeActivation.setTablelevel("PLAIN");
            targetTypeActivation.setStoragetime((long) -1);
            targetTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
            targetTypeActivation.setStatus(tpactivation.getStatus());
            typeActivations.add(targetTypeActivation);

            createdTypes.add(typename + "_CURRENT_DC");
          }

          // eromsza: History Dynamic: If data format support is false, don't handle _HIST and _CALC tables
          boolean dataFormatSupport = true;
          if (targetReferenceTable.getDataformatsupport() != null) {
              dataFormatSupport = targetReferenceTable.getDataformatsupport().intValue() == 1;
          }

          // eeoidiv,20110926:Automatically create _CALC table for update policy=4=HistoryDynamic (like _CURRENT_DC).
          if(targetReferenceTable.getUpdate_policy() == 4 && dataFormatSupport) {
	          if (!createdTypes.contains(typename + "_CALC")) {
	              // create _CALC tables for HistoryDynamic(4)
	              // and the table does not already contain such a row
	          	  final Typeactivation targetTypeActivation = new Typeactivation(tpactivation.getRockFactory());
	              targetTypeActivation.setTypename(typename + "_CALC");
	              targetTypeActivation.setType("Reference");
	              targetTypeActivation.setTablelevel("PLAIN");
	              targetTypeActivation.setStoragetime((long) -1);
	              targetTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
	              targetTypeActivation.setStatus(tpactivation.getStatus());
	              typeActivations.add(targetTypeActivation);
	              createdTypes.add(typename + "_CALC");
	          }
	          if (!createdTypes.contains(typename + "_HIST_RAW")) {
	              // create _HIST_RAW tables for HistoryDynamic(4)
	              // and the table does not already contain such a row
	          	  final Typeactivation targetTypeActivation = new Typeactivation(tpactivation.getRockFactory());
	              targetTypeActivation.setTypename(typename + "_HIST_RAW");
	              targetTypeActivation.setType("Reference");
	              targetTypeActivation.setTablelevel("PLAIN");
	              targetTypeActivation.setStoragetime((long) -1);
	              targetTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
	              targetTypeActivation.setStatus(tpactivation.getStatus());
	              typeActivations.add(targetTypeActivation);
	              createdTypes.add(typename + "_HIST_RAW");
	          }
          }//if(targetReferenceTable.getUpdate_policy() == 4)
        }
      }

      final Vector<String> duplicateCheck = new Vector<String>();

      // Now the vector typeActivations holds the Typeactivation-objects ready
      // to be saved. Start saving the values.
      if (newActivation) {
        for (Typeactivation targetTypeActivation : typeActivations) {
          final String uniqueName = targetTypeActivation.getTechpack_name() + "/" + targetTypeActivation.getTypename() + "/"
            + targetTypeActivation.getTablelevel();
          if (!duplicateCheck.contains(uniqueName)) {
            // Tpactivation is new. Just insert the values.
            targetTypeActivation.insertDB();
            duplicateCheck.add(uniqueName);
          }
        }
      } else {
        // Update the values in TypeActivation table.
        updateTypeActivationsTable(tpactivation.getRockFactory(), typeActivations, tpactivation.getTechpack_name());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /*
   * private void saveTypeActivationData(final boolean newActivation, final
   * Tpactivation tpactivation) throws BuildException { try {
   *
   * // This vector holds the TypeActivations to be updated. final
   * Vector<Typeactivation> typeActivations = new Vector<Typeactivation>();
   *
   * final String targetVersionId = tpactivation.getVersionid();
   *
   * // First get the TypeActivations of type Measurement. // Get all
   * MeasurementTypes related to this VersionID.
   *
   * final Measurementtype whereMeasurementType = new
   * Measurementtype(tpactivation.getRockFactory());
   * whereMeasurementType.setVersionid(targetVersionId); final
   * MeasurementtypeFactory measurementtypeFactory = new
   * MeasurementtypeFactory(tpactivation.getRockFactory(),
   * whereMeasurementType);
   *
   * final Vector<Measurementtype> targetMeasurementTypes =
   * measurementtypeFactory.get(); final Iterator<Measurementtype>
   * targetMeasurementTypesIterator = targetMeasurementTypes.iterator();
   *
   * while (targetMeasurementTypesIterator.hasNext()) { final Measurementtype
   * targetMeasurementType = targetMeasurementTypesIterator.next();
   *
   * final String targetTypeId = targetMeasurementType.getTypeid(); final String
   * targetTypename = targetMeasurementType.getTypename(); // Typename
   *
   * if (targetMeasurementType.getJoinable() != null &&
   * targetMeasurementType.getJoinable().length() != 0) { // Adding new PREV_
   * table. final Typeactivation preTypeActivation = new
   * Typeactivation(tpactivation.getRockFactory());
   * preTypeActivation.setTypename(targetTypename + "_PREV");
   * preTypeActivation.setTablelevel("PLAIN");
   * preTypeActivation.setStoragetime(new Long(-1));
   * preTypeActivation.setType("Measurement");
   * preTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
   * preTypeActivation.setStatus(tpactivation.getStatus());
   * preTypeActivation.setPartitionplan(null);
   *
   * typeActivations.add(preTypeActivation); }
   *
   * final Measurementtable whereMeasurementTable = new
   * Measurementtable(tpactivation.getRockFactory());
   * whereMeasurementTable.setTypeid(targetTypeId); final
   * MeasurementtableFactory measurementTableFactory = new
   * MeasurementtableFactory(tpactivation.getRockFactory(),
   * whereMeasurementTable);
   *
   * final Vector<Measurementtable> targetMeasurementTables =
   * measurementTableFactory.get(); final Iterator<Measurementtable>
   * targetMeasurementTablesIterator = targetMeasurementTables.iterator();
   *
   * while (targetMeasurementTablesIterator.hasNext()) { final Measurementtable
   * targetMeasurementTable = targetMeasurementTablesIterator.next(); final
   * String targetTableLevel = targetMeasurementTable.getTablelevel(); //
   * Tablelevel
   *
   * // All the needed data is gathered from tables. // Add the Typeactivation
   * of type Measurement to // typeActivations-vector to be saved later.
   *
   * final Typeactivation targetTypeActivation = new
   * Typeactivation(tpactivation.getRockFactory());
   * targetTypeActivation.setTypename(targetTypename);
   * targetTypeActivation.setTablelevel(targetTableLevel);
   * targetTypeActivation.setStoragetime(new Long(-1));
   * targetTypeActivation.setType("Measurement");
   * targetTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
   * targetTypeActivation.setStatus(tpactivation.getStatus());
   * targetTypeActivation
   * .setPartitionplan(targetMeasurementTable.getPartitionplan());
   *
   * typeActivations.add(targetTypeActivation); } }
   *
   * // Next get the TypeActivations of type Reference. // Get all
   * ReferenceTables related to this VersionID. final Referencetable
   * whereReferenceTable = new Referencetable(tpactivation.getRockFactory());
   * whereReferenceTable.setVersionid(targetVersionId); final
   * ReferencetableFactory referenceTableFactory = new
   * ReferencetableFactory(tpactivation.getRockFactory(), whereReferenceTable);
   *
   * Vector<Referencetable> targetReferenceTables = referenceTableFactory.get();
   * Iterator<Referencetable> targetReferenceTablesIterator =
   * targetReferenceTables.iterator();
   *
   * while (targetReferenceTablesIterator.hasNext()) { final Referencetable
   * targetReferenceTable = targetReferenceTablesIterator.next(); final String
   * typename = targetReferenceTable.getTypename();
   *
   * final Typeactivation targetTypeActivation = new
   * Typeactivation(tpactivation.getRockFactory());
   * targetTypeActivation.setTypename(typename);
   * targetTypeActivation.setType("Reference");
   * targetTypeActivation.setTablelevel("PLAIN");
   * targetTypeActivation.setStoragetime(new Long(-1));
   * targetTypeActivation.setTechpack_name(tpactivation.getTechpack_name());
   * targetTypeActivation.setStatus(tpactivation.getStatus());
   *
   * typeActivations.add(targetTypeActivation); }
   *
   * // Now the vector typeActivations holds the Typeactivation-objects ready //
   * to be saved. Start saving the values. if (newActivation) { final
   * Iterator<Typeactivation> typeActivationsIterator =
   * typeActivations.iterator();
   *
   * while (typeActivationsIterator.hasNext()) { Typeactivation
   * targetTypeActivation = typeActivationsIterator.next(); // Tpactivation is
   * new. Just insert the values. targetTypeActivation.insertDB(); } } else { //
   * Update the values in TypeActivation table.
   * updateTypeActivationsTable(tpactivation.getRockFactory(), typeActivations,
   * tpactivation.getTechpack_name()); }
   *
   * } catch (Exception e) { e.printStackTrace(); throw new
   * BuildException("Saving of type activation data failed.", e); } }
   */

  /**
   * This method updates the TypeActivation-table when a new tech pack is
   * installed or updated. New TypeActivations are created and TypeActivations
   * of predecessor tech pack are kept the same.
   *
   * @param dwhrepRockFactory
   *          RockFactory object to handle database actions.
   * @param newTypeActivations
   *          New TypeActivations to be saved to database.
   * @param techpackName
   *          is the name of the techpack.
   * @throws org.apache.tools.ant.BuildException On Errors
   */
  private void updateTypeActivationsTable(final RockFactory dwhrepRockFactory,
      final Vector<Typeactivation> newTypeActivations, final String techpackName) throws BuildException {
    try {

      // These two hashmaps contain the keys and objects to compare between new
      // and existing TypeActivations.
      final HashMap<String, Typeactivation> existingTypeActivationsMap = new HashMap<String, Typeactivation>();
      final HashMap<String, Typeactivation> newTypeActivationsMap = new HashMap<String, Typeactivation>();

      for (Typeactivation currentNewTypeActivation : newTypeActivations) {

        // Create a string that identifies the TypeActivation.
        // This string is used as a key value when comparing between new and
        // existing TypeActivations.
        // This id string is generated by the primary keys of this object.
        // For example. TECH_PACK_NAME;TYPE_NAME;TABLE_LEVEL

        final String idString = currentNewTypeActivation.getTechpack_name() + ";"
          + currentNewTypeActivation.getTypename() + ";" + currentNewTypeActivation.getTablelevel();

        newTypeActivationsMap.put(idString, currentNewTypeActivation);
      }

      // Get the existing typeactivations.
      final Typeactivation whereTypeActivation = new Typeactivation(dwhrepRockFactory);
      whereTypeActivation.setTechpack_name(techpackName);
      final TypeactivationFactory typeActivationRockFactory = new TypeactivationFactory(dwhrepRockFactory,
          whereTypeActivation);

      final Vector<Typeactivation> existingTypeActivations = typeActivationRockFactory.get();

      for (Typeactivation currentExistingTypeActivation : existingTypeActivations) {

        // Create a string that identifies the TypeActivation and add it to the
        // existing TypeActivations map.

        final String idString = currentExistingTypeActivation.getTechpack_name() + ";"
          + currentExistingTypeActivation.getTypename() + ";" + currentExistingTypeActivation.getTablelevel();

        existingTypeActivationsMap.put(idString, currentExistingTypeActivation);
      }

      final Set<String> existingTypeActivationsIdStringsSet = new HashSet<String>();

      // First iterate through the existing TypeActivations and remove the
      // duplicate TypeActivations from the new TypeActivations.

      final Set<String> existingTypeActivationsIdStrings = existingTypeActivationsMap.keySet();

      for (String currentIdString : existingTypeActivationsIdStrings) {

        if (newTypeActivationsMap.containsKey(currentIdString)) {

          // Update the value of PARTITIONPLAN of the existing TypeActivation.
          final String[] primKeyValues = currentIdString.split(";");

          final Typeactivation wwhereTypeActivation = new Typeactivation(this.dwhrepRockFactory);
          wwhereTypeActivation.setTechpack_name(primKeyValues[0]);
          wwhereTypeActivation.setTypename(primKeyValues[1]);
          wwhereTypeActivation.setTablelevel(primKeyValues[2]);

          final TypeactivationFactory typeActivationFactory = new TypeactivationFactory(this.dwhrepRockFactory,
            wwhereTypeActivation);
          final Typeactivation targetTypeActivation = typeActivationFactory.get().get(0);

          final Typeactivation newTypeActivation = newTypeActivationsMap.get(currentIdString);

          if (targetTypeActivation == null) {
            throw new BuildException("Failed to update partitionplan of existing TypeActivation entry.");
          } else {
            targetTypeActivation.setPartitionplan(newTypeActivation.getPartitionplan());
            targetTypeActivation.updateDB();
          }

          // Remove this from newTypeActivations.
          newTypeActivationsMap.remove(currentIdString);
          existingTypeActivationsIdStringsSet.add(currentIdString);
        }
      }

      for (String targetTypeActivationIdString : existingTypeActivationsIdStringsSet) {
        // Don't do anything to the existing TypeActivation in the database.
        existingTypeActivationsMap.remove(targetTypeActivationIdString);
      }

      // Now the two HashMaps should contain new and obsolete values.
      // New TypeActivations are simply inserted to database.

      final Collection<Typeactivation> newTypeActivationsCollection = newTypeActivationsMap.values();

      for (Typeactivation currentNewTypeActivation : newTypeActivationsCollection) {

        System.out.println("Inserting new TypeActivation " + currentNewTypeActivation.getTechpack_name() + " "
          + currentNewTypeActivation.getTypename() + " during tech pack update.");

        currentNewTypeActivation.insertDB();
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Updating of type activations failed.", e);
    }
  }

  /**
   * This function checks if the techpack even exists in database before it can
   * be activated.
   *
   * @return Returns true if the techpack exists, otherwise returns false.
   * @throws BuildException On Errors
   */
  public boolean techPackExists() throws BuildException {
    try {

      final Versioning whereVersioning = new Versioning(this.dwhrepRockFactory);
      whereVersioning.setVersionid(techPackVersionID);
      final VersioningFactory versioningFactory = new VersioningFactory(this.dwhrepRockFactory, whereVersioning);
      final int length = versioningFactory.get().size();
      return length != 0;

    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Checking tech pack metadata failed.", e);
    }

  }

  public String getBinDirectory() {
    return binDirectory;
  }

  public void setBinDirectory(final String binDirectory) {
    this.binDirectory = binDirectory;
  }

}
