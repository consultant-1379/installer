/**
 * ----------------------------------------------------------------------- *
 * Copyright (C) 2010 LM Ericsson Limited. All rights reserved. *
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.install.tools;

import com.ericsson.eniq.repository.AsciiCrypter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Task which replaces string from file as crypted string
 * 
 * <br>
 * <table border="1">
 * <tr>
 * <th>File</th>
 * <th>Token</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>Name of the file which will be handled</td>
 * <td>String which will be replaced from the file</td>
 * <td>String which will be crypted and added to file</td>
 * </tr>
 * </table>
 * 
 * @author etogust
 * 
 */
public class CryptAndReplaceTask extends Task {
	
	private transient String file = null;
	private transient String token = null;
	private transient String value = null;

	public void setFile(final String file) {
		this.file = file;
	}

	public void setToken(final String token) {
		this.token = token;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	/**
	 * Replaces string from file as crypted string
	 */
	@Override
	public void execute() throws BuildException {

		System.out.println("Handling file: " + file + "for changing values.");

		if (file == null || file.length() <= 0){
			return;
		}

		final File input = new File(file);
		File outputTmp = new File(file + "_tmp");
		int cnt = 1;
		while (outputTmp.exists() && cnt < 10) {
			outputTmp = new File(file + "_tmp" + cnt);
			cnt++;
		}
		outputTmp.deleteOnExit();
		
		if (cnt == 10) {
			throw new BuildException("Could not complete the crypting task.");
		}

		BufferedReader reader = null;
		PrintWriter writer = null;

		try {
			final Reader fReader = new FileReader(input);
			reader = new BufferedReader(fReader);
			writer = new PrintWriter(outputTmp);

			final String cryptedValue = AsciiCrypter.getInstance().encrypt(value);

			String oldLine = reader.readLine();
			String newLine;
			while (oldLine != null) {
				if (oldLine.contains(token)) {
					newLine = oldLine.replace(token, cryptedValue);
				} else {
					newLine = oldLine;
				}
				writer.println(newLine);
				oldLine=reader.readLine();
			}
			writer.close();
			reader.close();
			
			

		} catch (Exception e) {
			clean(writer, reader, outputTmp);

			System.out.println(e.getMessage());
			System.out.println("Replace failed.");
			throw new BuildException(e);
		}
		input.delete();
		outputTmp.renameTo(input);
	}

	/**
	 * Close streams and delete tmp file
	 * 
	 * @param writer
	 * @param reader
	 * @param outputTmp
	 */
	private void clean(final PrintWriter writer, final BufferedReader reader, final File outputTmp) {

		try {
			if (writer != null) {
				writer.close();
			}
			if (reader != null) {
				reader.close();
			}
		} catch (IOException e) {
			System.out.println("Failed to close reader/writer");
		}
		if (outputTmp != null && outputTmp.isFile()){
			outputTmp.delete();
		}
	}
	
}
