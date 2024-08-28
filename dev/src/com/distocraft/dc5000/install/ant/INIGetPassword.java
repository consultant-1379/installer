package com.distocraft.dc5000.install.ant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This custom made ANT task gets a variable from niq.ini file. Parameters
 * are:<br />
 * <i>file</i> - Path to the niq.ini file.<br />
 * <i>section</i> - Name of the section.<br />
 * <i>parameter</i> - Parameter within that section.<br />
 * <br />
 * The parameter and it's value are set as a ANT project's property with the
 * following style:<br />
 * Property name is <i>section.parameter</i> and value is the value of this
 * section parameter.
 * 
 * @author
 *
 */
public class INIGetPassword extends Task {

	private String section = new String();

	private String parameter = new String();

	private String parameterValue = new String();

	private String file = new String();

	/**
	 * This function will start the execution of this ANT task.
	 */
	public void execute() throws BuildException {

		String iniGetCommand = ". /eniq/admin/lib/common_functions.lib ; " + "inigetpassword " + this.section + " -f "
				+ this.file + " -v " + this.parameter;

		FileWriter fw = null;
		BufferedWriter bw = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		Process commandProcess;

		try {
			Files.deleteIfExists(Paths.get("/eniq/sw/installer/iniGet_temp"));
			
			fw = new FileWriter("/eniq/sw/installer/iniGet_temp");
			bw = new BufferedWriter(fw);
			bw.write("#!/bin/bash\n");
			bw.write(iniGetCommand);
		} catch (Exception e) {
			System.out.println("Exception occured while creating the iniGetPassword temp script: " + e);
			e.printStackTrace();
		} finally {
			try {
				bw.flush();
				bw.close();
				fw.close();
			} catch (Exception e) {
				System.out.println("Exception occured in closing the writer streams while creating the iniGetPassword temp script: " + e);
				e.printStackTrace();
			}
		}

		try {
			commandProcess = Runtime.getRuntime().exec("sh /eniq/sw/installer/iniGet_temp");
			isr = new InputStreamReader(commandProcess.getInputStream());
			br = new BufferedReader(isr);
			while ((parameterValue = br.readLine()) != null) {
				getProject().setNewProperty(this.section + "." + this.parameter, parameterValue.trim());
			}
			
			commandProcess.waitFor();
			System.out.println("Exit code for iniGetPassword: " + commandProcess.exitValue());
			commandProcess.destroy();
		} catch (Exception e) {
			System.out.println("Exception occured while executing the iniGetPassword temp script: " + e);
			e.printStackTrace();
		} finally {
			try {
				Files.deleteIfExists(Paths.get("/eniq/sw/installer/iniGet_temp"));
				isr.close();
				br.close();
			} catch (Exception e) {
				System.out.println("Exception occured in closing the reader streams while executing the iniGetPassword temp script: " + e);
				e.printStackTrace();
			}

		}
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

}
