package com.distocraft.dc5000.install.ant;

import java.util.Hashtable;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Created on Mar 2, 2005
 * 
 * @author lemminkainen
 */
public class DependencyCheck extends Task {

	private String forceflag = "nope";

	public void execute() throws BuildException {

		Project proj = getProject();
		Hashtable props = proj.getProperties();

		String this_name = (String) props.get("module.name");

		String installed_version = (String) props.get("module." + this_name);

		System.out.println("Checking previous installation...");
//Code Commented for EQEV-49365 and made only update call 
		/*if (installed_version != null) { // A version of this module is
											// installed
			System.out.println("Installation type is update");
			proj.setNewProperty("dc.installation.type", "update");
		} else {
			System.out.println("Installation type is install");
			proj.setNewProperty("dc.installation.type", "install");
		}*/
		System.out.println("Installation type is update");
		proj.setNewProperty("dc.installation.type", "update");
		
		if (!"force".equals(forceflag)) {

			boolean passed = true;

			Iterator i = props.keySet().iterator();

			while (i.hasNext()) {
				String key = (String) i.next();

				if (!key.startsWith("dcinstall.require.")) {
					continue;
				}

				String error = validateRequirement(key, props);

				if (error != null) {

					System.out.println(error);

					passed = false;

				}

			} // foreach requirement

			if (!passed) {
				throw new BuildException(
						"Installation aborted: Failed dependencies");
			}
		}

	}

	/**
	 * Validate a requirement
	 */
	private String validateRequirement(String key, Hashtable props) {

		String module = key.substring(key.lastIndexOf(".") + 1, key.length());

		String rq = ((String) props.get(key)).trim();

		int ix = rq.indexOf(" ");

		String operator = rq.substring(0, ix).trim();

		rq = rq.substring(ix).trim();

		String installed_module = (String) props.get("module." + module);

		if (installed_module == null) {
			return null;
		}

		int rq_version = parseVersionInfo(rq);
		int inst_version = parseVersionInfo(installed_module);

		if (operator.equals(">")) {

			if (rq_version >= 0 && inst_version < rq_version) {
				return "Module " + module + " version > " + rq_version
						+ " required.";
			}
		} else if (operator.equals("=")) {

			if (rq_version >= 0 && inst_version != rq_version) {
				return "Module " + module + " version = " + rq_version
						+ " required.";
			}
		}

		return null;

	}

	private int parseVersionInfo(String src) {

		try {

			if (src.lastIndexOf("b") >= 0) {

				return Integer
						.parseInt(src.substring(src.lastIndexOf("b") + 1));

			} else {

				System.out.println("Error parsing version info \"" + src
						+ "\" (build number not found)");

			}

		} catch (Exception vne) {
			System.out.println("Error parsing version info \"" + src + "\" ("
					+ vne.getMessage() + ")");
			vne.printStackTrace();
		}

		return -1;

	}

	public void setForceflag(String force) {
		forceflag = force;
	}

	public String getForceflag() {
		return forceflag;
	}

}
