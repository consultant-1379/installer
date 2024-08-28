package com.distocraft.dc5000.install.ant;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.Properties;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This custom made ANT task can manipulate a Java Properties file. It can add,
 * remove, update or copy one property in a Properties file.
 * 
 * @author ejannbe
 * 
 */
public class UpdateProperties extends Task {

	public String propertiesFile = null;

	public String action = null;

	public String key = null;

	public String value = null;

	public String targetKey = null;

	public String forceCopy = null;

	private String verbose = "true";
	
	
	
	

	public void execute() throws BuildException {

		if (propertiesFile == null || action == null) {
			throw new BuildException(
					"Both inputFile and action must be defined");
		}
		File in = new File(propertiesFile);

		try {
			if (!in.canRead() || !in.isFile()) {
				throw new BuildException("InputFile cannot be read or not file");
			}
			Properties props = new Properties();
			props.load(new FileInputStream(in));

			boolean changed = false;

			if (key == null || key.equalsIgnoreCase("")) {
				throw new BuildException("When action is " + action
						+ " parameter key must be defined.");
			}

			if (action.equalsIgnoreCase("add")) {

				if (value == null) {
					throw new BuildException("When action is " + action
							+ " parameter value must be defined.");
				}

				if (props.containsKey(key)) {
					System.out.println("Properties file " + propertiesFile
							+ " already contain value with key " + key
							+ ". Use action \"update\" instead.");
				} else {
					props.put(key, value);
					changed = true;
				}

			} else if (action.equalsIgnoreCase("remove")) {

				if (props.containsKey(key)) {
					props.remove(key);
					changed = true;
				} else if (Boolean.valueOf(verbose)) {
					System.out.println("Properties file " + propertiesFile
							+ " does not contain key " + key);
				}

			} 
			
			else if (action.equalsIgnoreCase("update")) {
				
				if (value == null) {
					throw new BuildException("When action is " + action
							+ " parameter value must be defined.");
				}
                  
				if (props.containsKey(key)) {
					props.remove(key);
					
				}

				props.put(key, value);
				System.out.println("Added new entry of " +key+"="+ value +" successfully");
				changed = true;

			
			}
			
			
			else if (action.equalsIgnoreCase("check"))
			{
				if (value == null)
				{
					throw new BuildException("When action is"+action+"parameter value must be defined");
				}
				if (props.containsKey(key)) 
				{
					System.out.println("Versiondb.properties is already updated");
				}
				else
				{
					props.put(key, value);
					changed = true;
					System.out.println("Versiondb.properties is updated in check method");
				}
				}
					

			
		        
			
			
			else if (action.equalsIgnoreCase("copy")) {

				if (targetKey == null || targetKey.equalsIgnoreCase("")) {
					throw new BuildException("When action is " + action
							+ " parameter targetKey must be defined.");
				}

				if (props.containsKey(key) == true) {

					if (props.containsKey(targetKey) == true) {

						if (forceCopy != null
								&& forceCopy.equalsIgnoreCase("TRUE")) {
							System.out.println("Properties file "
									+ propertiesFile
									+ " already contains value for key "
									+ targetKey
									+ ". Will overwrite the existing value.");
							props.remove(targetKey);
							String copyValue = props.getProperty(key);
							props.put(targetKey, copyValue);
							changed = true;

						} else {
							System.out
									.println("Properties file "
											+ propertiesFile
											+ " already contains value for key "
											+ targetKey
											+ ". Copying will not be done.");
						}

					} else {
						String copyValue = props.getProperty(key);
						props.put(targetKey, copyValue);
						changed = true;
					}

				} else {
					System.out.println("Properties file " + propertiesFile
							+ " does not contain key " + key
							+ " Copying value from the key cannot be done.");
				}

			}

			if (changed) {
				
				props.store(new FileOutputStream(in), "");
			}
		} catch (BuildException be) {
			throw be;
		} catch (Exception e) {
			throw new BuildException("Runtime error", e);
		}
	}

	
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	
	

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getPropertiesFile() {
		return propertiesFile;
	}

	public void setPropertiesFile(String propertiesFile) {
		this.propertiesFile = propertiesFile;
	}

	public String getForceCopy() {
		return forceCopy;
	}

	public void setForceCopy(String forceCopy) {
		this.forceCopy = forceCopy;
	}

	public String getTargetKey() {
		return targetKey;
	}

	public void setTargetKey(String targetKey) {
		this.targetKey = targetKey;
	}

	public void setVerbose(final String verbose) {
		this.verbose = verbose;
	}

	public String getVerbose() {
		return this.verbose;
	}

}
