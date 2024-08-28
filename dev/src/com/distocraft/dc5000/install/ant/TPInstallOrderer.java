package com.distocraft.dc5000.install.ant;

import java.io.*;
import java.security.Key;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.distocraft.dc5000.etl.rock.Meta_databasesFactory;
import com.distocraft.dc5000.repository.dwhrep.*;
import com.ericsson.eniq.repository.ETLCServerProperties;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

public class TPInstallOrderer extends Task {

	private static String techpackDirectory = null;

	private String installListFile = null;

	private String newTpInstallFile = null;

	private String newDependencyTpFile = null;

	private String debug = null;

	private File tpDir = null;

	private boolean checkForRequiredTechPacks = true;

	private boolean checkTPInstalled = false;

	List<String> combinedList = new ArrayList<String>();

	List<String> tps_list = new ArrayList<String>();

	List<String> new_tps_list = new ArrayList<String>();

	List<String> new_tps_disable_list = new ArrayList<String>();

	private transient RockFactory etlrepRockFactory = null;

	private transient RockFactory dwhrepRockFactory = null;

	public void execute() throws BuildException {
		try {

			if (techpackDirectory == null) {
				throw new BuildException("parameter techpackDirectory has to be defined");
			}

			if (installListFile == null) {
				throw new BuildException("parameter installListFile has to be defined");
			}

			tpDir = new File(techpackDirectory);

			if (!tpDir.exists() || !tpDir.canRead()) {
				throw new BuildException("Unable to read techpackDirectory");
			}

			final File listFile = new File(installListFile);
			final File newTpListFile = new File(newTpInstallFile);
			final File newDepTPFile = new File(newDependencyTpFile);

			if (!listFile.exists() || !listFile.canRead()) {
				throw new BuildException("Unable to read installListFile");
			}

			System.out.println("Checking connection to database...............");

			final Map<String, String> databaseConnectionDetails = getDatabaseConnectionDetails();

			// Create the connection to the etlrep.
			etlrepRockFactory = createEtlrepRockFactory(databaseConnectionDetails);

			// Create also the connection to dwhrep.
			createDwhrepRockFactory();

			System.out.println("Connections to database created.");
			final List<TPEntry> thelist = new ArrayList<TPEntry>();

			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(listFile));

				String line = null;
				while ((line = in.readLine()) != null) {
					line = line.trim();
					final TPEntry tpe = loadTPE(line);

					if (tpe != null) {
						if (!this.checkForRequiredTechPacks) {
							System.out.println("Not checking required tech packs of tech pack " + tpe.name);
							tpe.deps = new String[0];
						}
						thelist.add(tpe);
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
				throw new BuildException("Error reading installListFile", e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
					}
				}
			}

			if (debug != null) {
				System.out.println("First read done. " + thelist.size());
				printList(thelist);
			}

			fillRequirements(thelist);

			if (debug != null) {
				System.out.println("Fill requirements done. " + thelist.size());
				printList(thelist);
			}

			findOrder(thelist);

			System.out.println("Installation of Dependent techpack order list - " + tps_list);

			System.out.println(
					"TechPack Installation order list, after matching the Rstate and versions - " + new_tps_list);

			findDisableOrder(new_tps_list);

			getAllDisableList(tps_list);
			System.out.println("List of Disbaling Techpacks - " + new_tps_disable_list);

			if (debug != null) {
				System.out.println("Ordering done. Final list. " + thelist.size());
				printList(thelist);
			}

			PrintWriter out = null;
			PrintWriter tpOut = null;
			PrintWriter dpTPOut = null;
			try {
				out = new PrintWriter(new FileWriter(listFile));
				tpOut = new PrintWriter(new FileWriter(newTpListFile));
				dpTPOut = new PrintWriter(new FileWriter(newDepTPFile));
				Iterator<String> i = tps_list.iterator();
				Iterator<String> newList = new_tps_list.iterator();
				Iterator<String> newDpTPList = new_tps_disable_list.iterator();

				while (i.hasNext()) {
					out.println(i.next());
				}
				while (newList.hasNext()) {
					tpOut.println(newList.next());
				}
				while (newDpTPList.hasNext()) {
					dpTPOut.println(newDpTPList.next());
				}

				out.flush();
				tpOut.flush();
				dpTPOut.flush();
			} catch (Exception e) {
				throw new BuildException("Error writing installListFile", e);
			} finally {
				if (out != null) {
					try {
						out.close();
						if (tpOut != null) {
							tpOut.close();
						}
						if (dpTPOut != null) {
							dpTPOut.close();
						}
					} catch (Exception e) {
					}
				}
			}

		} catch (BuildException be) {
			throw be;
		} catch (Exception e) {
			throw new BuildException("Unexpected failure", e);
		} finally {
			try {
				if (etlrepRockFactory != null) {
					etlrepRockFactory.getConnection().close();
				}
				if (dwhrepRockFactory != null) {
					dwhrepRockFactory.getConnection().close();
				}
			} catch (final SQLException sqle) {
				System.out.print("Connection cleanup error - " + sqle.toString());
			}
			dwhrepRockFactory = null;
			etlrepRockFactory = null;
		}

	}

	public void removeDuplicates(List<String> myList) {
		HashSet<String> set = new HashSet<String>(myList);
		myList.clear();
		myList.addAll(set);
	}

	private void fillRequirements(final List<TPEntry> list) throws Exception {
		for (int i = 0; i < list.size(); i++) {
			final TPEntry tpe = list.get(i);

			for (int j = 0; j < tpe.deps.length; j++) {
				if (!isInList(tpe.deps[j], list)) {
					final TPEntry dtpe = loadTPE(tpe.deps[j]);
					if (dtpe != null) {
						list.add(dtpe);
					}
					// else {
					// throw new BuildException("Dependency of " + tpe.name +
					// " failed. Techpack " + tpe.deps[j] + " not found");
					// }
				}
			}
		}
	}

	private void findOrder(final List<TPEntry> list) throws Exception {

		List<String> initial_list = new ArrayList<String>();

		Iterator<TPEntry> it1 = list.iterator();
		while (it1.hasNext()) {
			TPEntry tp1 = it1.next();
			combinedList.add(tp1.name);
			initial_list.add(tp1.name);
			for (int jj = 0; jj < tp1.deps.length; jj++) {
				combinedList.add(tp1.deps[jj]);
			}
		}

		removeDuplicates(combinedList);

		int position_dep;
		int position_tp;
		int loop_count = 0;

		while (loop_count < 100) {

			for (int i = 0; i < list.size(); i++) {
				TPEntry tp2 = list.get(i);

				for (int x = 0; x < tp2.deps.length; x++) {
					position_dep = combinedList.indexOf(tp2.deps[x]);
					position_tp = combinedList.indexOf(tp2.name);
					if (position_dep > position_tp) {
						combinedList.remove(position_tp);
						combinedList.add(position_dep, tp2.name);
					}
				}
			}
			++loop_count;
		}

		combinedList.retainAll(initial_list);

		for (String tpi_name : combinedList) {
			for (int y = 0; y < list.size(); y++) {
				TPEntry tp0 = list.get(y);
				if (tp0.name.equals(tpi_name)) {
					tps_list.add(tp0.filename);
					checkAlreadyInstalled(tpi_name, tp0);
					if (!checkTPInstalled) {
						new_tps_list.add(tp0.filename);
					}
				}
			}
		}
	}

	/**
	 * @param dep_tp_list
	 */
	private void findDisableOrder(List<String> dep_tp_list) {
		// TODO Auto-generated method stub
		try {
			for (String list : dep_tp_list) {
				final String[] nameParts = list.split("_");
				String techPackName = "";
				for (int i = 0; i < (nameParts.length - 2); i++) {
					techPackName += nameParts[i] + "_";
				}
				techPackName = techPackName.substring(0, (techPackName.length() - 1));
				final Techpackdependency whereTPName = new Techpackdependency(dwhrepRockFactory);
				whereTPName.setTechpackname(techPackName);
				TechpackdependencyFactory tpDependencyFact;
				tpDependencyFact = new TechpackdependencyFactory(dwhrepRockFactory, whereTPName);
				final Vector<Techpackdependency> tpDependencyVect = tpDependencyFact.get();
				if (tpDependencyVect.size() > 0) {
					final ListIterator<Techpackdependency> targetTPDependency = tpDependencyVect.listIterator();
					while (targetTPDependency.hasNext()) {
						Techpackdependency resultList = targetTPDependency.next();
						final String dependencyTPVersionID = resultList.getVersionid();
						String dependencyTP = dependencyTPVersionID.split(":")[0];
						new_tps_disable_list.add(dependencyTP);

					}

				}
				new_tps_disable_list.add(techPackName);
				removeDuplicates(new_tps_disable_list);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException("Error reading installListFile", e);
		}

	}

	/**
	 * @param tps_list
	 * 
	 */
	private void getAllDisableList(List<String> tps_list) {
		// TODO Auto-generated method stub
		try {
			if (new_tps_list.size() > 0) {
				for (String list : tps_list) {
					final String[] nameParts = list.split("_");
					String techPackName = "";
					for (int i = 0; i < (nameParts.length - 2); i++) {
						techPackName += nameParts[i] + "_";
					}
					techPackName = techPackName.substring(0, (techPackName.length() - 1));
					new_tps_disable_list.add(techPackName);
				}
				removeDuplicates(new_tps_disable_list);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException("Error reading installListFile", e);
		}
	}

	/**
	 * @param tp0
	 * @return
	 * @throws RockException
	 * @throws SQLException
	 */
	private void checkAlreadyInstalled(String techPackName, TPEntry tp0) {
		// TODO Auto-generated method stub
		try {
			final Tpactivation whereTPActivation = new Tpactivation(dwhrepRockFactory);
			whereTPActivation.setTechpack_name(techPackName);
			TpactivationFactory tpActivationFact;
			tpActivationFact = new TpactivationFactory(dwhrepRockFactory, whereTPActivation);
			final Vector<Tpactivation> tpActivationVect = tpActivationFact.get();
			//
			final Datainterface whereDataInterface = new Datainterface(dwhrepRockFactory);
			whereDataInterface.setInterfacename(techPackName);
			final DatainterfaceFactory dataInterfaceFact = new DatainterfaceFactory(dwhrepRockFactory,
					whereDataInterface);
			final Vector<Datainterface> datainterfaceSet = dataInterfaceFact.get();

			//
			if (!techPackName.startsWith("INTF")) {
				checkTechPackInstalled(techPackName, tp0, tpActivationVect);
			} else {
				checkIntfAlreadyInstalled(techPackName, tp0, datainterfaceSet);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException("Error reading installListFile", e);
		}
	}

	/**
	 * @param techPackName
	 * @param tp0
	 * @param tpActivationVect
	 * @throws SQLException
	 * @throws RockException
	 */
	private void checkTechPackInstalled(String techPackName, TPEntry tp0, final Vector<Tpactivation> tpActivationVect)
			throws SQLException, RockException {
		if (tpActivationVect.size() > 0) {
			// Found activated version of this techpack
			final Tpactivation targetTPActivation = tpActivationVect.get(0);
			final String activatedVersionID = targetTPActivation.getVersionid();
			final Versioning whereVersioning = new Versioning(dwhrepRockFactory);
			whereVersioning.setTechpack_name(techPackName);
			whereVersioning.setVersionid(activatedVersionID);
			VersioningFactory versioningFactory;
			versioningFactory = new VersioningFactory(dwhrepRockFactory, whereVersioning);
			final Vector<Versioning> installedVersioningVector = versioningFactory.get();
			final Versioning installedTP = installedVersioningVector.get(0);
			String installedTPVersion = installedTP.getTechpack_version();

			String installedBuild = activatedVersionID.substring(activatedVersionID.indexOf(":") + 1);

			installedBuild = getBuildnumber(installedBuild);
			final Integer rstateCompResultforTP = compareRstates(tp0.rstate, installedTPVersion);

			if (installedVersioningVector.size() > 0) {
				if (rstateCompResultforTP == 0) {
					final Integer buildNumberInteger = Integer.valueOf(tp0.buildNumber);
					final Integer installedBuildNumberInteger = Integer.valueOf(installedBuild);

					if (buildNumberInteger > installedBuildNumberInteger) {
						System.out.println("Older tech pack build b" + installedBuild
								+ " is installed. Tech pack will be updated to b" + tp0.buildNumber);
						checkTPInstalled = false;
					} else {
						System.out.println("Newer or the same version b" + installedBuild
								+ " of this tech pack already installed. This tech pack will not be installed. Skipping rest of the installation phases.");
						checkTPInstalled = true;
					}
				} else if (rstateCompResultforTP == 1) {
					System.out.println("Older tech pack version " + installedTPVersion
							+ " is installed. Tech pack will be updated to version " + tp0.rstate + "_b"
							+ tp0.buildNumber);
					checkTPInstalled = false;
				} else if (rstateCompResultforTP == 2) {
					// This tech pack is older than than the version in
					// database.
					System.out.println("Newer version " + installedTPVersion
							+ " of this tech pack exist in database. This tech pack will not be installed.");
					checkTPInstalled = true;
				} else {
					throw new BuildException("Could not compare the versions of techpacks. Installation has failed.");
				}
			}
		} else {
			System.out.println("Previous version of techpack - " + techPackName
					+ " not found  in the system. Hence adding to the installation list");
			checkTPInstalled = false;
		}
	}

	/**
	 * @param techPackName
	 * @param tp0
	 * @param datainterfaceSet
	 */
	private void checkIntfAlreadyInstalled(String techPackName, TPEntry tp0,
			final Vector<Datainterface> datainterfaceSet) {
		if (datainterfaceSet.size() > 0) {
			// Found activated version of this Interface
			String installedIntfRstate = datainterfaceSet.get(0).getRstate();
			String installedIntfBuild = datainterfaceSet.get(0).getInterfaceversion();
			installedIntfBuild = getBuildnumber(installedIntfBuild);
			final Integer rstateCompResultforIntf = compareRstates(tp0.rstate, installedIntfRstate);

			if (rstateCompResultforIntf == 0) {
				final Integer buildNumberInteger = Integer.valueOf(tp0.buildNumber);
				final Integer installedBuildNumberInteger = Integer.valueOf(installedIntfBuild);

				if (buildNumberInteger > installedBuildNumberInteger) {
					System.out.println("Older tech pack "+techPackName +" build b" + installedIntfBuild
							+ " is installed. Tech pack will be updated to b" + tp0.buildNumber);
					checkTPInstalled = false;
				} else {
					System.out.println("Newer or the same version b" + installedIntfBuild
							+ " of this tech pack "+techPackName +" already installed. Tech pack will not be installed. Skipping rest of the installation phases.");
					checkTPInstalled = true;
				}
			} else if (rstateCompResultforIntf == 1) {
				System.out.println("Older tech pack version " + installedIntfRstate
						+ " is installed. Tech pack "+techPackName +" will be updated to version " + tp0.rstate + "_b" + tp0.buildNumber);
				checkTPInstalled = false;
			} else if (rstateCompResultforIntf == 2) {
				// This tech pack is older than than the version in
				// database.
				System.out.println("Newer version " + installedIntfRstate
						+ " of  tech pack "+techPackName +" exist in database. Tech pack will not be installed.");
				checkTPInstalled = true;
			} else {
				throw new BuildException("Could not compare the versions of techpacks. Installation has failed.");
			}

		} else {
			System.out.println("Previous version of Interface - " + techPackName
					+ " not found in the system. Hence adding to the installation list");
			checkTPInstalled = false;
		}
	}

	/**
	 * @param rstate
	 * @param installedTPVersion
	 * @return
	 */
	private Integer compareRstates(String firstRstate, String secondRstate) {
		// TODO Auto-generated method stub
		// Use regexp to get the number value of RState.
		final Pattern pattern = Pattern.compile("\\d+");
		final Matcher matcher = pattern.matcher(firstRstate);

		if (!matcher.find()) {
			System.out.println("Rstate " + firstRstate + " has invalid format.");
			return -1;
		}

		final String firstRstateNum = matcher.group(0);
		final Matcher matcher2 = pattern.matcher(secondRstate);

		final Pattern pattern2 = Pattern.compile(".$");
		final Matcher matcher3 = pattern2.matcher(firstRstate);

		if (!matcher3.find()) {
			System.out.println("Rstate " + firstRstate + " has invalid format.");
			return -1;
		}

		if (!matcher2.find()) {
			System.out.println("Rstate " + secondRstate + " has invalid format.");
			return -1;
		}
		final String firstRstateLastChar = matcher3.group(0);
		final Matcher matcher4 = pattern2.matcher(secondRstate);

		if (!matcher4.find()) {
			System.out.println("Rstate " + secondRstate + " has invalid format.");
			return -1;
		}
		final String secondRstateLastChar = matcher4.group(0);
		final String secondRstateNum = matcher2.group(0);

		if (Integer.parseInt(firstRstateNum) == Integer.parseInt(secondRstateNum)) {
			// The RState numbers are equal.
			// Check the string after RState number which is bigger.
			if (firstRstateLastChar.compareTo(secondRstateLastChar) == 0) {
				return 0;
			} else if (firstRstateLastChar.compareTo(secondRstateLastChar) > 0) {
				return 1;
			} else {
				return 2;
			}
		} else {
			// Let the Rstate number decide which is bigger.
			if (Integer.parseInt(firstRstateNum) > Integer.parseInt(secondRstateNum)) {
				return 1;
			} else {
				return 2;
			}
		}
	}

	/**
	 * @param installedBuild
	 * @return
	 */
	private String getBuildnumber(String installedBuild) {
		if (installedBuild.startsWith("((")) {
			installedBuild = installedBuild.substring(2);
		}

		if (installedBuild.endsWith("))")) {
			installedBuild = installedBuild.substring(0, installedBuild.length() - 2);
		}
		return installedBuild;
	}

	/**
	 * This function creates the RockFactory to dwhrep. The created RockFactory
	 * is inserted in class variable dwhrepRockFactory.
	 */
	private void createDwhrepRockFactory() {
		try {
			final Meta_databases whereMetaDatabases = new Meta_databases(this.etlrepRockFactory);
			whereMetaDatabases.setConnection_name("dwhrep");
			whereMetaDatabases.setType_name("USER");
			final Meta_databasesFactory metaDatabasesFactory = new Meta_databasesFactory(this.etlrepRockFactory,
					whereMetaDatabases);
			final Vector<Meta_databases> metaDatabases = metaDatabasesFactory.get();

			if ((metaDatabases != null) && (metaDatabases.size() == 1)) {
				final Meta_databases targetMetaDatabase = metaDatabases.get(0);
				this.dwhrepRockFactory = new RockFactory(targetMetaDatabase.getConnection_string(),
						targetMetaDatabase.getUsername(), targetMetaDatabase.getPassword(),
						etlrepRockFactory.getDriverName(), "TPInstallOrderCheck", true);
			} else {
				throw new BuildException(
						"Unable to connect metadata (No dwhrep or multiple dwhreps defined in Meta_databases)");
			}
		} catch (final Exception e) {
			e.printStackTrace();
			throw new BuildException("Creating database connection to dwhrep failed.", e);
		}
	}

	/**
	 * @return
	 */
	protected Map<String, String> getDatabaseConnectionDetails() throws BuildException {

		ETLCServerProperties props;
		try {
			props = new ETLCServerProperties();
		} catch (IOException e) {
			// getProject().log("Could not read ETLCServer.properties", e,
			// Project.MSG_ERR);
			throw new BuildException("Could not read ETLCServer.properties", e);
		}

		final Map<String, String> dbConnDetails = props.getDatabaseConnectionDetails();

		// Set the database connection properties as ANT properties.
		for (String property : dbConnDetails.keySet()) {
			final String value = dbConnDetails.get(property);
			getProject().setNewProperty(property, value);
		}

		return dbConnDetails;
	}

	private RockFactory createEtlrepRockFactory(final Map<String, String> databaseConnectionDetails)
			throws BuildException {
		final String databaseUsername = databaseConnectionDetails.get("etlrepDatabaseUsername");
		final String databasePassword = databaseConnectionDetails.get("etlrepDatabasePassword");
		final String databaseUrl = databaseConnectionDetails.get("etlrepDatabaseUrl");
		final String databaseDriver = databaseConnectionDetails.get("etlrepDatabaseDriver");

		try {
			return new RockFactory(databaseUrl, databaseUsername, databasePassword, databaseDriver,
					"TPInstallOrderCheck", true);
		} catch (final Exception e) {
			e.printStackTrace();
			throw new BuildException("Unable to initialize database connection.", e);
		}
	}

	private void printList(final List<TPEntry> list) {
		int ix = 0;
		final Iterator<TPEntry> i = list.iterator();

		while (i.hasNext()) {
			final TPEntry tpe = i.next();
			String ou = "  " + (ix++) + " " + tpe.name + " -< [ ";
			for (int j = 0; j < tpe.deps.length; j++) {
				ou += tpe.deps[j] + " ";
			}
			ou += "]";
			System.out.println(ou);
		}
	}

	private boolean isInList(final String name, final List<TPEntry> list) {
		Iterator<TPEntry> i = list.iterator();
		while (i.hasNext()) {
			final TPEntry tpe = i.next();

			if (tpe.name.equals(name)) {
				return true;
			}
		}

		return false;
	}

	private TPEntry loadTPE(final String name) throws Exception {

		final File[] files = tpDir.listFiles(new FileFilter() {

			public boolean accept(final File cand) {
				if (cand.getName().endsWith(".tpi") && cand.isFile() && cand.canRead()) {

					final String[] nameParts = cand.getName().split("_");

					// Name of the tpi file is for example
					// DC_Z_ALARM_R1A_b55.tpi

					if (nameParts.length < 3) {
						// Not a tpi with a valid name.
						return false;
					}

					String techPackName = "";

					// Iterate through the parts to drop the last two
					// "_"-characters.
					for (int i = 0; i < (nameParts.length - 2); i++) {
						techPackName += nameParts[i] + "_";
					}

					// Now the name should contain DC_Z_ALARM_
					// Drop the last "_" from the name to get the name of the
					// tech pack.
					// For example DC_Z_ALARM

					techPackName = techPackName.substring(0, (techPackName.length() - 1));

					if (techPackName.equalsIgnoreCase(name)) {
						return true;
					} else {
						return false;
					}

				} else {
					return false;
				}

			}
		});

		if (files.length < 1) {
			return null; // No such TP
		} else if (files.length > 1) {
			throw new BuildException("Multiple techpacks with name " + name + " in techpack directory");
		}

		final ZipFile zf = new ZipFile(files[0]);

		final Enumeration entries = zf.entries();

		while (entries.hasMoreElements()) {
			final ZipEntry ze = (ZipEntry) entries.nextElement();

			if (ze.getName().endsWith("version.properties")) {
				final TPEntry tpe = new TPEntry();
				tpe.filename = files[0].getName();

				final Properties p = new Properties();
				p.load(zf.getInputStream(ze));

				// Check if the properties file is encrypted or not!
				if (p.containsKey("tech_pack.name") == false) {
					// Could not found tech_pack.name property. This techpack
					// must be
					// encrypted.
					// TODO: Check for the license...
					// System.out.println(
					// files[0].getName() + " is encrypted. Trying to decrypt
					// version.properties file...");

					// create a temporary byte array stream
					// ByteArrayOutputStream bos = new ByteArrayOutputStream();

					// TODO: Maybe do some CRC32 checking later?
					// CRC32 crc = new CRC32();

					// read and buffer the input.
					// while ((in = zis.read()) != -1) {
					// bos.write(in);
					// }

					final InputStream cryptIn = zf.getInputStream(ze);

					final ZipCrypter crypter = new ZipCrypter();
					final Key rsaKey = ZipCrypter.getPublicKey(ZipCrypter.DEFAULT_KEY_MOD, ZipCrypter.DEFAULT_KEY_EXP);

					final byte[] extra = ze.getExtra();

					// System.out.println("ze.getExtra() = " + new
					// String(extra));
					// System.out.println("rsaKey = " + rsaKey.toString());

					// get the aesKey from the RSA encrypted metadata.

					final Key aesKey = crypter.decryptAESKey(ze.getExtra(), rsaKey);

					// System.out.println("aesKey = " + aesKey.toString());

					final ByteArrayOutputStream bos = new ByteArrayOutputStream();
					final AESCrypter aesCrypt = new AESCrypter();
					aesCrypt.decrypt(cryptIn, bos, aesKey);
					final byte[] aesOutput = bos.toByteArray();

					// System.out.println("DEBUG: ze.getSize(): " +
					// ze.getSize());

					final InputStream versionPropInStream = new ByteArrayInputStream(aesOutput);

					// System.out.println("versionPropInStream.toString() = " +
					// versionPropInStream.toString());
					p.load(versionPropInStream);

					// System.out.println("p = " + p.toString());

					if (p.containsKey("tech_pack.name") == false) {
						// Even the decrypted techpack is a mess. Bail out...
						throw new BuildException("Techpack " + files[0].getName()
								+ " is errorneous or does not have required name of the techpack. Installation is aborted.");
					}

				} else {
					// System.out.println(files[0].getName() + " is not
					// encrypted.");
				}

				// System.out.println("name = " + name);
				// System.out.println("p.getProperty(tech_pack.name) = " +
				// p.getProperty("tech_pack.name"));

				if (!name.equals(p.getProperty("tech_pack.name"))) {
					throw new BuildException("Techpack " + files[0].getName() + " is errorneous (name)");
				}

				tpe.name = p.getProperty("tech_pack.name");
				tpe.buildNumber = p.getProperty("build.number");
				tpe.rstate = p.getProperty("tech_pack.version");

				final List<String> reqs = new ArrayList<String>();

				final Enumeration keys = p.keys();
				while (keys.hasMoreElements()) {
					final String key = (String) keys.nextElement();

					if (key.startsWith("required_tech_packs.")) {
						reqs.add(key.substring(key.indexOf(".") + 1));
					}
				}

				tpe.deps = (String[]) reqs.toArray(new String[reqs.size()]);

				return tpe;
			}

		}

		return null;
	}

	public void setTechpackDirectory(final String dir) {
		techpackDirectory = dir;
	}

	public static String getTechpackDirectory() {
		return techpackDirectory;
	}

	public void setInstallListFile(final String file) {
		installListFile = file;
	}

	public String getInstallListFile() {
		return installListFile;
	}

	public void setNewTpInstallFile(String newFile) {
		newTpInstallFile = newFile;
	}

	public String getNewTpInstallFile() {
		return newTpInstallFile;
	}

	public void setNewTpDependencyFile(String newFile) {
		newDependencyTpFile = newFile;
	}

	public String getNewTpDependencyFile() {
		return newDependencyTpFile;
	}

	public void setDebug(final String deb) {
		debug = deb;
	}

	public String getDebug() {
		return debug;
	}

	public class TPEntry {

		public String name;

		public String[] deps;

		public String filename;

		public String rstate;

		public String buildNumber;

	};

	public class NonException extends Exception {
	};

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final TPInstallOrderer tpio = new TPInstallOrderer();
		tpio.setTechpackDirectory(args[0]);
		tpio.setInstallListFile(args[1]);
		tpio.setNewTpInstallFile(args[2]);
		tpio.setNewTpDependencyFile(args[3]);
		tpio.execute();

	}

	public String getCheckForRequiredTechPacks() {
		return String.valueOf(checkForRequiredTechPacks);
	}

	public void setCheckForRequiredTechPacks(final String checkForRequiredTechPacks) {
		this.checkForRequiredTechPacks = Boolean.parseBoolean(checkForRequiredTechPacks);
	}

}
