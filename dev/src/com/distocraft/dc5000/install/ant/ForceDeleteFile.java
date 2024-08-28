/**
 * ----------------------------------------------------------------------- *
 * Copyright (C) 2005-2010 LM Ericsson Limited. All rights reserved. *
 * -----------------------------------------------------------------------
 */
package com.distocraft.dc5000.install.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * Because ANT is somehow unable to destroy links here is a dirty task to perform such action.
 *
 * @author lemminkainen
 */
public class ForceDeleteFile extends Task {

  private String file = null;
  
  public void setFile(final String file) {
    this.file = file;
  }
  
  public void execute() throws BuildException {
    
    System.out.println("Deleting file: "+file);
    
    if (file != null && file.length() > 0) {
      
    	boolean success = false;
    	
    	try {
      
    		final File f = new File(file);
    		success = f.delete();
      
    	} catch(Exception e) {
    		System.out.println("File.delete failed to :" + e.getMessage());
    	}
    		
    	if (!success) {
    		System.out.println("Java delete file failed. Executing native rm.");
      
    		try {
    			final Runtime rt = Runtime.getRuntime();
          final String ar = "/usr/bin/rm -fr " + file;
    			final Process p = rt.exec(ar);
    			p.waitFor();      
    		} catch(Exception e2) {
    			throw new BuildException("Unable to remove file " + file + ": " + e2.getMessage());
    		}
    	}
    
    }
    
  }
  
}

