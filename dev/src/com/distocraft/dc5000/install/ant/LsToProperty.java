package com.distocraft.dc5000.install.ant;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class LsToProperty extends Task {

	private String dir = null;

	private String property = null;

	private String pattern = null;

	public void setDir(String dir) {
		this.dir = dir;
	}

	public void setProperty(String prope) {
		this.property = prope;
	}

	public void setPattern(String patte) {
		this.pattern = patte;
	}

	public void execute() throws BuildException {

		File xdir = new File(dir);

		if (!xdir.isDirectory()) {
			throw new BuildException(dir + " is not directory");
		}
		if (!xdir.canRead()) {
			throw new BuildException(dir + " cannot be read");
		}
		String[] arr = xdir.list(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if (pattern.endsWith("*")) {
					return name.startsWith(pattern.substring(0,
							pattern.length() - 1));
				} else if (pattern.startsWith("*")) {
					return name.endsWith(pattern.substring(1, pattern.length()));
				} else {
					return name.equals(pattern);
				}
			}
		});

		if (arr.length >= 1) {
			getProject().setNewProperty(property, arr[arr.length - 1]);
		}
	}

}
