/**
 * ----------------------------------------------------------------------- *
 * Copyright (C) 2010 LM Ericsson Limited. All rights reserved. *
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.install.tools;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.ericsson.eniq.repository.DBUsersGet;

/**
 * Task which sets ant project property named
 * &lt;username&gt;&lt;connection&gt;".password" to a value from database.
 * Username and connection are given as parameters
 * 
 * <br>
 * <table border="1">
 * <tr>
 * <th>username</th>
 * <th>connection</th>
 * </tr>
 * <tr>
 * <td>Username for the dbuser</td>
 * <td>Connection name, which' password is to be fetched</td>
 * </tr>
 * </table>
 * 
 * @author etogust
 * 
 */
public class DBUsers extends Task {

	private transient String username = null;
	private transient String connection = null;
	private transient String propname = null;

	public void setUsername(final String uName) {
		this.username = uName;
	}

	public void setConnection(final String conn) {
		this.connection = conn;
	}

	public void setPropname(final String name) {
		this.propname = name;
	}

	/**
	 * Gets the needed value.
	 */
	@Override
	public void execute() throws BuildException { 

		System.out.println("Getting " + username + "." + connection + ".password");

		if (username == null || username.length() <= 0) {
			throw new BuildException("Empty username... exiting task");
		}

		if (connection == null || connection.length() <= 0) {
			throw new BuildException("Empty connection... exiting task");
		}

		final List<Meta_databases> mdbs = DBUsersGet.getMetaDatabases(username, connection);
		
		for (Meta_databases mdb : mdbs){
			if (mdb.getUsername().equals(username) && mdb.getConnection_name().equals(connection)){
				final Project proj = getProject();
				if (proj != null){
					
					proj.setNewProperty(this.username + "." + this.connection + ".password", mdb.getPassword());
					
					if (this.propname != null){
						proj.setNewProperty(this.propname, mdb.getPassword());
					}
				}
			}
		}
	}

}
