/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2012 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.distocraft.dc5000.install.ant;

import java.sql.SQLException;

import ssc.rockfactory.RockFactory;

/**
 * 
 * Wrapper ant task for RunOldLoaders
 * 
 * @author epaujor
 * @since 2012
 * 
 */
public class RunOldLoadersAntTask extends CommonDBTasks {

  private transient String techpackName;

  private transient RockFactory dwhrepRockFactory = null;

  private transient RockFactory etlrepRockFactory = null;

  @Override
  public void execute() {
    try {
      this.etlrepRockFactory = createEtlrepRockFactory();
      this.dwhrepRockFactory = createDbRockFactory(this.etlrepRockFactory, DWHREP);
      final RunOldLoaders runOldLoadersCounters = new RunOldLoaders(this.techpackName, this.etlrepRockFactory,
          this.dwhrepRockFactory);
      runOldLoadersCounters.execute();
    } finally {
      if (dwhrepRockFactory != null) {
        try {
          dwhrepRockFactory.getConnection().close();
        } catch (SQLException e) {
          System.out.println("dwhrepRockFactory cleanup error ");
        }
      }

      if (etlrepRockFactory != null) {
        try {
          etlrepRockFactory.getConnection().close();
        } catch (SQLException e) {
          System.out.println("etlrepRockFactory cleanup error ");
        }
      }
    }
  }
  
  public String getTechpackName() {
    return techpackName;
  }

  public void setTechpackName(final String techpackName) {
    this.techpackName = techpackName;
  }
}
