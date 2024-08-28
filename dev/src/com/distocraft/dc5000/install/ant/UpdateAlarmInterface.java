package com.distocraft.dc5000.install.ant;

import java.util.Map;
import java.util.Vector;

import org.apache.tools.ant.BuildException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.rock.Meta_collection_sets;
import com.distocraft.dc5000.etl.rock.Meta_collection_setsFactory;
import com.distocraft.dc5000.etl.rock.Meta_collections;
import com.distocraft.dc5000.etl.rock.Meta_collectionsFactory;
import com.distocraft.dc5000.repository.dwhrep.Alarminterface;
import com.distocraft.dc5000.repository.dwhrep.AlarminterfaceFactory;

/**
 * This class is a custom made ANT-task that updates an entry to etlrep database
 * table AlarmInterface. Copyright (c) 1999 - 2007 AB LM Ericsson Oy All rights
 * reserved.
 * 
 * @author ejannbe
 */
public class UpdateAlarmInterface extends CommonTask {

  RockFactory etlrepRockFactory = null;

  RockFactory dwhrepRockFactory = null;

  private String interfaceId = "";

  private String description = "";

  private String status = "";

  private String queueNumber = "";

  /**
   * This function starts the execution of task.
   */
  public void execute() throws BuildException {
    final Map<String, String> databaseConnectionDetails = getDatabaseConnectionDetails();
    // Create the connection to the etlrep.
    this.etlrepRockFactory = createEtlrepRockFactory(databaseConnectionDetails, getClass().getSimpleName());
    // Create also the connection to dwhrep.
    this.dwhrepRockFactory = createDwhrepRockFactory(this.etlrepRockFactory, getClass().getSimpleName());

    final boolean updateSuccessful = this.updateInterface();

    if (updateSuccessful) {
      System.out.println("Update of AlarmInterface was successful.");
    } else {
      System.out.println("Update of AlarmInterface failed.");
    }

  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public String getInterfaceId() {
    return interfaceId;
  }

  public void setInterfaceId(final String interfaceId) {
    this.interfaceId = interfaceId;
  }

  public String getQueueNumber() {
    return queueNumber;
  }

  public void setQueueNumber(final String queueNumber) {
    this.queueNumber = queueNumber;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }

  /**
   * This function tries to update an entry to table AlarmInterface.
   * 
   * @return Returns true if the update was succesful, otherwise returns
   *         false.
   */
  private boolean updateInterface() {
    try {
      final Alarminterface whereAlarmInterface = new Alarminterface(this.dwhrepRockFactory);
      whereAlarmInterface.setInterfaceid(this.interfaceId);
      final AlarminterfaceFactory alarmInterfaceFactory = new AlarminterfaceFactory(this.dwhrepRockFactory,
          whereAlarmInterface);
      final Vector<Alarminterface> alarmInterfaceVector = alarmInterfaceFactory.get();
      final Long metaCollectionSetId;
      final Long metaCollectionId;

      if (alarmInterfaceVector.size() <= 0) {
        System.out.println("AlarmInterface with interfaceId " + this.interfaceId
            + " does not exist. Skipping update of AlarmInterface.");
        return false;
      } else {
        final String collection_set_name;
        if ("AlarmInterface_RD".equals(this.interfaceId)) {
          collection_set_name = "DC_Z_ALARM";
        } else {
          collection_set_name = "AlarmInterfaces";
        }
        System.out.println("Updating AlarmInterface with interfaceId " + this.interfaceId + " for TP " + collection_set_name);
        final Meta_collection_sets whereMetaCollectionSet = new Meta_collection_sets(this.etlrepRockFactory);
        whereMetaCollectionSet.setCollection_set_name(collection_set_name);
        whereMetaCollectionSet.setEnabled_flag("Y");
        final Meta_collection_setsFactory metaCollectionSetFactory = new Meta_collection_setsFactory(this.etlrepRockFactory,
            whereMetaCollectionSet);
        final Vector<Meta_collection_sets> metaCollectionSetVector = metaCollectionSetFactory.get();

        if (metaCollectionSetVector.size() > 0) {
          final Meta_collection_sets targetMetaCollectionSet = metaCollectionSetVector.get(0);
          metaCollectionSetId = targetMetaCollectionSet.getCollection_set_id();
        } else {
          System.out.println("Could not found Meta_collection_set_id.");
          return false;
        }

        final Meta_collections whereMetaCollection = new Meta_collections(this.dwhrepRockFactory);
        whereMetaCollection.setCollection_set_id(metaCollectionSetId);
        whereMetaCollection.setCollection_name("Adapter_" + this.interfaceId);
        whereMetaCollection.setEnabled_flag("Y");

        final Meta_collectionsFactory metaCollectionFactory = new Meta_collectionsFactory(this.etlrepRockFactory,
            whereMetaCollection);
        
        final Vector<Meta_collections> metaCollectionVector = metaCollectionFactory.get();

        if (metaCollectionVector.size() > 0) {
          final Meta_collections targetMetaCollection = metaCollectionVector.get(0);
          metaCollectionId = targetMetaCollection.getCollection_id();
        } else {
          System.out.println("Could not find Meta_collection where metaCollectionSetId=" + metaCollectionSetId
              + ", collectionName=" + "Adapter_" + this.interfaceId + ", enabled=Y");
          return false;
        }

        // At this point, new metaCollectionSetId and metaCollectionId values are retrieved
        // from database.
        // Update all the new values to AlarmInterface table.
        final Alarminterface targetAlarmIntf = alarmInterfaceVector.get(0);
        targetAlarmIntf.setDescription(this.description);
        targetAlarmIntf.setStatus(this.status);
        targetAlarmIntf.setCollection_set_id(metaCollectionSetId);
        targetAlarmIntf.setCollection_id(metaCollectionId);
        targetAlarmIntf.setQueue_number(new Long(this.queueNumber));
        targetAlarmIntf.updateDB();

        System.out.println("Updated AlarmInterface with interfaceId " + this.interfaceId + " to database.");
        return true;
      }

    } catch (Exception e) {
      System.out.println("Inserting AlarmInterface failed.");
      throw new BuildException("Inserting AlarmInterface failed.", e);
    }
  }

}
