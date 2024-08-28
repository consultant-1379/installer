package com.distocraft.dc5000.install.ant;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Vector;

import org.apache.tools.ant.BuildException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.rock.Meta_schedulings;
import com.distocraft.dc5000.etl.rock.Meta_schedulingsFactory;


public class IntervalDSTModifier extends CommonTask {
  private RockFactory etlrepRockFactory = null;

  /**
   * This function starts the daylight savings
   */
  public void execute() throws BuildException {
    try {
      fixDSTScheduling(); 
    } catch (Exception e) {
      throw new BuildException("Interval schedule fixing failed.", e);
    }
  }
  
  /**
   * This method fixes over 1 hour schedulings when DST has changed in
   * schedulings.
   * 
   * @throws Exception
   */
  private void fixDSTScheduling() throws Exception {
    
    try {
      Map <String, String> databaseConnectionDetails = getDatabaseConnectionDetails();
      etlrepRockFactory = createEtlrepRockFactory(databaseConnectionDetails);
      final GregorianCalendar currentCalendar = new GregorianCalendar();
      Meta_schedulings whereMetaSchedulings = new Meta_schedulings(etlrepRockFactory);
      whereMetaSchedulings.setExecution_type("interval");
      Meta_schedulingsFactory metaSchedulingsFact = new Meta_schedulingsFactory(etlrepRockFactory,
          whereMetaSchedulings);

      Vector metaSchedulings = metaSchedulingsFact.get();
      if (metaSchedulings != null) {
        for(int i = 0; i < metaSchedulings.size();i++) {
          try {
            Meta_schedulings targetMetaScheduling = (Meta_schedulings) metaSchedulings.get(i);
            if (targetMetaScheduling.getInterval_hour() != null 
                && targetMetaScheduling.getInterval_hour().intValue() >= 1) {
              int year = 0;
              int month = 0;
              int day = 0;
              int hour = 0;
              int minute = 0;
              if (targetMetaScheduling.getScheduling_year() != null) {
                year = targetMetaScheduling.getScheduling_year().intValue();
              }
              if (targetMetaScheduling.getScheduling_month() != null) {
                month = targetMetaScheduling.getScheduling_month().intValue();
              }
              if (targetMetaScheduling.getScheduling_day() != null) {
                day = targetMetaScheduling.getScheduling_day().intValue();
              }
              if (targetMetaScheduling.getScheduling_hour() != null) {
                hour = targetMetaScheduling.getScheduling_hour().intValue();
              }
              if (targetMetaScheduling.getScheduling_min() != null) {
                minute = targetMetaScheduling.getScheduling_min().intValue();
              }
              final GregorianCalendar initialCalendar = new GregorianCalendar();
              initialCalendar.set(year, month, day, hour, minute);
              int offSetHour = (currentCalendar.get(Calendar.ZONE_OFFSET) + currentCalendar.get(Calendar.DST_OFFSET) - initialCalendar.get(Calendar.ZONE_OFFSET) - initialCalendar.get(Calendar.DST_OFFSET)) / (1000 * 60 * 60);
              if (offSetHour != 0) {
                final GregorianCalendar lastExecuted = new GregorianCalendar();
                if (targetMetaScheduling.getLast_execution_time() != null) {
                  lastExecuted.setTimeInMillis(targetMetaScheduling.getLast_execution_time().getTime());
                  lastExecuted.add(Calendar.HOUR,-offSetHour);
                  targetMetaScheduling.setLast_execution_time(new Timestamp(lastExecuted.getTimeInMillis()));
                  targetMetaScheduling.updateDB();
                }
              }
            }
          } catch (Exception e) {
            System.out.println("Scheduling updation failed.");
          }
        }
      }
    } finally {
      if (etlrepRockFactory != null) {
        try {
          etlrepRockFactory.getConnection().close();
        } catch (Exception e) {
        }
      }
    }
  }
  
  /**
   * This function creates the rockfactory object to etlrep from the database
   * connection details read from ETLCServer.properties file.
   * 
   * @param databaseConnectionDetails
   * @return Returns the created RockFactory.
   */
  private RockFactory createEtlrepRockFactory(Map <String, String>databaseConnectionDetails) throws Exception {
    RockFactory rockFactory = null;
    String databaseUsername = databaseConnectionDetails.get("etlrepDatabaseUsername").toString();
    String databasePassword = databaseConnectionDetails.get("etlrepDatabasePassword").toString();
    String databaseUrl = databaseConnectionDetails.get("etlrepDatabaseUrl").toString();
    String databaseDriver = databaseConnectionDetails.get("etlrepDatabaseDriver").toString();

    try {
      rockFactory = new RockFactory(databaseUrl, databaseUsername, databasePassword, databaseDriver, "PreinstallCheck",
          true);

    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("Unable to initialize database connection.", e);
    }

    if (rockFactory == null){
      throw new Exception(
      "Unable to initialize database connection. Please check the settings in the ETLCServer.properties file.");
    }
    return rockFactory;
  }
}
