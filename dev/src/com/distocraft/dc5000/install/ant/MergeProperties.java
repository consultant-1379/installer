package com.distocraft.dc5000.install.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class MergeProperties extends Task {

	public String inputFile = null;
	public String outputFile = null;

	public void execute() throws BuildException {
		if (inputFile == null || outputFile == null) {
			throw new BuildException(
					"Both inputFile and outputFile must be defined");
		}
		try {
			File in = new File(inputFile);
			if (!in.canRead() || !in.isFile()) {
				throw new BuildException("InputFile cannot be read or not file");
			}
			Properties inprops = new Properties();
			inprops.load(new FileInputStream(in));

			File out = new File(outputFile);
			if (!out.canWrite() || !out.canRead() || !out.isFile()) {
				throw new BuildException(
						"OutputFile cannot be read or written or not file");
			}
			Properties outprops = new Properties();
			outprops.load(new FileInputStream(out));

			Iterator i = inprops.keySet().iterator();

			boolean changed = false;
			while (i.hasNext()) {
				String key = (String) i.next();

				// For CR 8/10918-18/FCP1038147/12:
				// To check if firstDayOfTheWeek property value in
				// static.proeprties is <> 2 don't change value

				if (key.equals("firstDayOfTheWeek")) {

					int firstDayOfTheWeek = 0;

					if (outprops.get(key) != null) {
						firstDayOfTheWeek = Integer.parseInt(outprops.get(key)
								.toString());
					}

					if (outprops.get(key) == null
							|| outprops.get(key).equals("2")) {
						changed = true;
						outprops.put(key, inprops.get(key));
					} else if (!(outprops.get(key).toString().equals(inprops
							.get(key).toString()))
							&& firstDayOfTheWeek > 2
							&& firstDayOfTheWeek < 2) {
						changed = false;
						continue;
					}

				}

				else if (outprops.get(key) == null) {
					changed = true;
					outprops.put(key, inprops.get(key));
				}
			}

			if (changed) {
				outprops.store(new FileOutputStream(out), "");
			}
		} catch (BuildException be) {
			throw be;
		} catch (Exception e) {
			throw new BuildException("Runtime error", e);
		}
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inp) {
		this.inputFile = inp;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String out) {
		this.outputFile = out;
	}

}
