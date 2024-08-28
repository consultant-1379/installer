package com.distocraft.dc5000.install.ant;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.tools.ant.BuildException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.rock.Meta_collection_sets;
import com.distocraft.dc5000.etl.rock.Meta_collection_setsFactory;
import com.distocraft.dc5000.etl.rock.Meta_collections;
import com.distocraft.dc5000.etl.rock.Meta_collectionsFactory;
import com.distocraft.dc5000.etl.rock.Meta_schedulings;
import com.distocraft.dc5000.etl.rock.Meta_schedulingsFactory;
import com.distocraft.dc5000.etl.rock.Meta_transfer_actions;
import com.distocraft.dc5000.etl.rock.Meta_transfer_actionsFactory;

/**
 * This is custom made ANT task that deactivates an interface and copies
 * interface's set to the deactivated interface.
 * 
 * @author Berggren
 * 
 */
public class DeactivateInterface extends CommonTask {

	private String deactivatedInterfaceName = "";

	private String ossName = "";

	private RockFactory etlrepRockFactory = null;

	private String binDirectory = "";

	private String onlyDeactivateInterface = "";

	/**
	 * This function starts the interface deactivation.
	 */
	public void execute() throws BuildException {
		try {

			final Map<String, String> databaseConnectionDetails = getDatabaseConnectionDetails();

			// Run the reloadConfig before executing DWHM_Install set of the
			// tech
			// pack.

			// Create the connection to the etlrep.
			this.etlrepRockFactory = createEtlrepRockFactory(
					databaseConnectionDetails, deactivatedInterfaceName);
			// Create also the connection to dwhrep.
			this.createDwhrepRockFactory(this.etlrepRockFactory,
					deactivatedInterfaceName);

			// Check if the interface is already deactivated.
			if (this.interfaceAlreadyDeactivated()) {

				System.out.println("Interface " + this.deactivatedInterfaceName
						+ " with OSS " + this.ossName
						+ " is already deactivated. ");
			} else {

				// Remove the existing sets
				// System.out.println("Removing Interface Sets...");
				removeIntfSets();

				// Activate the scheduler again so that the removed sche.....
				System.out.println("Running scheduler activation");
				activateScheduler(connectScheduler());
			}
		} catch (Exception e) {
			throw new BuildException("InterfaceDeactivation failed.", e);
		}

	}

	public String getDeactivatedInterfaceName() {
		return deactivatedInterfaceName;
	}

	public void setDeactivatedInterfaceName(
			final String deactivatedInterfaceName) {
		this.deactivatedInterfaceName = deactivatedInterfaceName;
	}

	public String getOssName() {
		return ossName;
	}

	public void setOssName(final String ossName) {
		this.ossName = ossName;
	}

	/**
	 * This function gets the CollectionSetId for the new MetaCollectionSet
	 * entry.
	 * 
	 * @return Returns the new CollectionSetId.
	 * @throws BuildException
	 */
	private Long getNewCollectionSetId() throws BuildException {
		ResultSet resultSet = null;
		Statement statement = null;
		Connection connection = null;
		try {
			Long newCollectionSetId = new Long(0);
			connection = this.etlrepRockFactory.getConnection();
			statement = connection.createStatement();
			final String sqlQuery = "SELECT collection_set_id FROM meta_collection_sets ORDER BY collection_set_id DESC;";

			resultSet = statement.executeQuery(sqlQuery);

			if (resultSet.next()) {
				newCollectionSetId = new Long(
						resultSet.getLong("collection_set_id") + 1);
			}

			return newCollectionSetId;
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Failed to generate new collection set id.", e);
		} finally {

			try {
				connection.close();
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
			}

			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
			}

		}
	}

	/**
	 * This function gets the CollectionId for the new MetaCollections entry.
	 * 
	 * @return Returns the new CollectionId.
	 * @throws BuildException
	 */
	private Long getNewCollectionId() throws BuildException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			Long newCollectionId = new Long(0);
			connection = this.etlrepRockFactory.getConnection();
			statement = connection.createStatement();
			final String sqlQuery = "SELECT collection_id FROM meta_collections ORDER BY collection_id DESC;";

			resultSet = statement.executeQuery(sqlQuery);

			if (resultSet.next()) {
				newCollectionId = new Long(
						resultSet.getLong("collection_id") + 1);
			}

			return newCollectionId;
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException("Failed to generate new collection id.", e);
		} finally {

			try {
				resultSet.close();
				statement.close();
				connection.close();
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
			}

			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
			}

		}
	}

	/**
	 * This function gets the TransferActionId for the new MetaTransferActions
	 * entry.
	 * 
	 * @return Returns the new TransferActionId.
	 * @throws BuildException
	 */
	private Long getNewTransferActionId() throws BuildException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			Long newTransferActionId = new Long(0);
			connection = this.etlrepRockFactory.getConnection();
			statement = connection.createStatement();
			final String sqlQuery = "SELECT transfer_action_id FROM meta_transfer_actions ORDER BY transfer_action_id DESC;";

			resultSet = statement.executeQuery(sqlQuery);

			if (resultSet.next()) {
				newTransferActionId = new Long(
						resultSet.getLong("transfer_action_id") + 1);
			}

			return newTransferActionId;
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Failed to generate new transfer action id.", e);

		} finally {

			try {

				resultSet.close();
				statement.close();
				connection.close();
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
			}

			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
			}

		}
	}

	/**
	 * This function returns true if the directory checker set exists for the
	 * interface to be deactivated.
	 * 
	 * @return Returns true if the directory checker exists, otherwise returns
	 *         false.
	 * @throws BuildException
	 */
	public boolean directoryCheckerSetExists() throws BuildException {
		try {

			// Get the interface's metaCollectionSetId.
			final Meta_collection_sets whereMetaCollectionSets = new Meta_collection_sets(
					this.etlrepRockFactory);
			whereMetaCollectionSets
					.setCollection_set_name(this.deactivatedInterfaceName + "-"
							+ this.ossName);
			final Meta_collection_setsFactory metaCollectionSetsFactory = new Meta_collection_setsFactory(
					this.etlrepRockFactory, whereMetaCollectionSets);
			final Vector metaCollectionSetsVector = metaCollectionSetsFactory
					.get();
			Long metaCollectionSetId = new Long(0);

			if (metaCollectionSetsVector.size() > 0) {
				final Meta_collection_sets targetMetaCollectionSet = (Meta_collection_sets) metaCollectionSetsVector
						.get(0);
				metaCollectionSetId = targetMetaCollectionSet
						.getCollection_set_id();
			} else {
				System.out.println("No set found for "
						+ this.deactivatedInterfaceName
						+ ". Cannot start Directory_Checker set.");
				return false;
			}

			final Meta_collections targetMetaCollection = new Meta_collections(
					this.etlrepRockFactory);
			targetMetaCollection.setCollection_name("Directory_Checker_"
					+ this.deactivatedInterfaceName);
			targetMetaCollection.setCollection_set_id(metaCollectionSetId);
			final Meta_collectionsFactory metaCollectionsFactory = new Meta_collectionsFactory(
					this.etlrepRockFactory, targetMetaCollection);
			final Vector targetMetaCollectionsVector = metaCollectionsFactory
					.get();

			if (targetMetaCollectionsVector.size() > 0) {
				// Directory checker set exists.
				System.out.println("Directory checker set found for "
						+ this.deactivatedInterfaceName + "-" + this.ossName);
				return true;
			} else {
				// Directory checker not found.
				System.out.println("Directory checker set not found for "
						+ this.deactivatedInterfaceName + "-" + this.ossName);
				return false;
			}

		} catch (Exception e) {
			throw new BuildException(
					"Checking of directory checker set failed.", e);
		}
	}

	public String getBinDirectory() {
		return binDirectory;
	}

	public void setBinDirectory(final String binDirectory) {
		this.binDirectory = binDirectory;
	}

	public String getOnlyDeactivateInterface() {
		return onlyDeactivateInterface;
	}

	public void setOnlyDeactivateInterface(final String onlyDeactivateInterface) {
		this.onlyDeactivateInterface = onlyDeactivateInterface;
	}

	/**
	 * This function gets the Id for the new MetaSchedulings entry.
	 * 
	 * @return Returns the new CollectionId.
	 * @throws BuildException
	 */
	private Long getNewMetaSchedulingsId() throws BuildException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			Long newMetaSchedulingsId = new Long(0);
			connection = this.etlrepRockFactory.getConnection();
			statement = connection.createStatement();
			final String sqlQuery = "SELECT id FROM meta_schedulings ORDER BY id DESC;";

			resultSet = statement.executeQuery(sqlQuery);

			if (resultSet.next()) {
				newMetaSchedulingsId = new Long(resultSet.getLong("id") + 1);
			}

			return newMetaSchedulingsId;
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Failed to generate new MetaScheduling id.", e);
		} finally {
			try {
				connection.close();
				try {
					if (resultSet != null) {
						resultSet.close();
					}
				} catch (Exception e) {
				}

				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
			}

		}
	}

	/**
	 * This function checks out if the interface is already deactivated. The
	 * checking is done from deactivated interface META_COLLECTION_SET_NAME's.
	 * 
	 * @return Returns true if the interface is already deactivated. Otherwise
	 *         returns false.
	 */
	private boolean interfaceAlreadyDeactivated() throws BuildException {

		try {
			final Meta_collection_sets whereMetaCollSet = new Meta_collection_sets(
					this.etlrepRockFactory);
			whereMetaCollSet
					.setCollection_set_name(this.deactivatedInterfaceName + "-"
							+ this.ossName);

			final Meta_collection_setsFactory metaCollSetsFactory = new Meta_collection_setsFactory(
					this.etlrepRockFactory, whereMetaCollSet);

			final List<Meta_collection_sets> metaCollSets = metaCollSetsFactory
					.get();
			return metaCollSets.isEmpty();
		} catch (Exception e) {
			throw new BuildException(
					"Failed to check if interface is already deactivated.", e);
		}

	}

	/**
	 * This function removes the sets of this interface and all deactivated
	 * OSS's interface sets.
	 */
	private void removeIntfSets() {

		try {
			final Meta_collection_sets whereCollSets = new Meta_collection_sets(
					this.etlrepRockFactory);
			final Meta_collection_setsFactory collSetsFactory = new Meta_collection_setsFactory(
					this.etlrepRockFactory, whereCollSets);
			final Vector collSets = collSetsFactory.get();
			final Iterator collSetsIterator = collSets.iterator();

			while (collSetsIterator.hasNext()) {
				final Meta_collection_sets currentCollSet = (Meta_collection_sets) collSetsIterator
						.next();

				// Remove the previously deactivated interface's sets.
				// Deactivated interface's sets are in format
				// INTF_DC_E_XYZ-OSS_NAME.
				if (currentCollSet.getCollection_set_name().equalsIgnoreCase(
						this.deactivatedInterfaceName + "-" + this.ossName)) {
					System.out.println("Deleting interface set "
							+ currentCollSet.getCollection_set_name()
							+ " and it's contents.");

					// This set is the currently installed interface's or some
					// deactivated
					// OSS's sets.
					// Delete the whole set including everything related to it.
					final Meta_collections whereColls = new Meta_collections(
							this.etlrepRockFactory);
					whereColls.setCollection_set_id(currentCollSet
							.getCollection_set_id());
					// System.out.println(" collection_set_id "+currentCollSet.getCollection_set_id());
					final Meta_collectionsFactory collFactory = new Meta_collectionsFactory(
							this.etlrepRockFactory, whereColls);
					final Vector collections = collFactory.get();
					final Iterator collectionsIter = collections.iterator();

					while (collectionsIter.hasNext()) {
						final Meta_collections currentCollection = (Meta_collections) collectionsIter
								.next();

						final Meta_transfer_actions whereTrActions = new Meta_transfer_actions(
								this.etlrepRockFactory);
						whereTrActions.setCollection_id(currentCollection
								.getCollection_id());
						// System.out.println(" collection_id "+currentCollection.getCollection_id());
						whereTrActions.setCollection_set_id(currentCollSet
								.getCollection_set_id());
						// System.out.println(" collection_set_id "+currentCollSet.getCollection_set_id());
						final Meta_transfer_actionsFactory trActionsFactory = new Meta_transfer_actionsFactory(
								this.etlrepRockFactory, whereTrActions);
						final Vector trActions = trActionsFactory.get();
						final Iterator trActionsIter = trActions.iterator();

						while (trActionsIter.hasNext()) {
							final Meta_transfer_actions currTrAction = (Meta_transfer_actions) trActionsIter
									.next();
							// System.out.println(" deleting data from META_TRANSFER_ACTION");
							currTrAction.deleteDB();
						}

						// Remove also the schedulings from the interface.
						final Meta_schedulings whereMetaSchedulings = new Meta_schedulings(
								this.etlrepRockFactory);
						whereMetaSchedulings.setCollection_id(currentCollection
								.getCollection_id());
						whereMetaSchedulings
								.setCollection_set_id(currentCollSet
										.getCollection_set_id());
						final Meta_schedulingsFactory metaSchedulingsFactory = new Meta_schedulingsFactory(
								this.etlrepRockFactory, whereMetaSchedulings);
						final Vector metaSchedulings = metaSchedulingsFactory
								.get();
						final Iterator metaSchedulingsIterator = metaSchedulings
								.iterator();

						while (metaSchedulingsIterator.hasNext()) {
							final Meta_schedulings currentMetaScheduling = (Meta_schedulings) metaSchedulingsIterator
									.next();
							// System.out.println(" deleting data from META_TRANSFER_ACTION");
							currentMetaScheduling.deleteDB();
						}

						// Do not delete the meta_transfer_batches. Old
						// meta_transfer_batches are cleaned up by housekeeping
						// set.
						currentCollection.deleteDB();
					}

					currentCollSet.deleteDB();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(
					"Failed removing previous installations interface sets.", e);
		}

	}

}
