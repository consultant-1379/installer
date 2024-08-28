/**
 * 
 */
package com.distocraft.dc5000.install.ant;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Custom made ANT task which will create a flag
 * to indicate new vector handling
 * @author xarjsin
 *
 */
public class SetNewVectorFlag {
	
	private String techPackName = new String();
	
	private static final String CONFFOLDER = "/eniq/sw/conf/vectorflags/";
	final private Logger log = Logger.getLogger("ant.LoadVectorReference");
	

	/**
	 * Method for ANT call to get Techpack name
	 * @param techPackName
	 */
	public void setTechPackName(String techPackName) {
		this.techPackName = techPackName;
	}
	
	/**
	 * The ANT task will create a flag in the conf folder
	 * 
	 */
	public void execute() {
		File confDir = new File(CONFFOLDER);
		File vectorFlag = new File(CONFFOLDER + "New_Vector_" + this.techPackName);

		try {
			if(!confDir.exists()) {
				confDir.mkdir();
			}
			vectorFlag.createNewFile();
		}
		catch (IOException e) {
			log.warning("Errors creating flag file " + vectorFlag.getPath() + " in " + CONFFOLDER);
		}
	}

}
