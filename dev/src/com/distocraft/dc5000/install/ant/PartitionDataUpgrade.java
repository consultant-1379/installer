/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.distocraft.dc5000.install.ant;

import java.io.File;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;

import org.apache.tools.ant.BuildException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import ssc.rockfactory.RockFactory;

/**
 * This is custom made ANT task that executes per-partition upgrade SQL using
 * velocity templates. It also allows for common initialisation and cleanup
 * SQL,and for per-partition roll-back. The per-partition SQL is executed using
 * parallel DWH connections.
 * 
 * @author efinian
 * 
 */
public class PartitionDataUpgrade extends CommonDBTasks {

  private transient RockFactory etlrepRockFactory = null;

  private String partitionStorageIdList = null;
  private String initTemplateFilepath = null;
  private String partitionTemplateFilepath = null;
  private String partitionRollbackTemplateFilepath = null;
  private String cleanupTemplateFilepath = null;
  private String debug = null;
  private boolean debugFlag = false;
  private Integer maxParallel = 10; // SQL is executed in batches of up to 10 parallel threads.

  private transient boolean rollbackFlag = false;

  /**
   * This ant task controls the per-partition custom data upgrade in DWHDB,
   * including execution of init, dataUpgrade, and cleanup sql velocity
   * templates, as well as an optional rollback template.
   * 
   * @param partitionStorageIdList
   *          (injected) The list of storageIds used to determine the target
   *          partitions
   * @param initTemplateFilepath
   *          (injected) The location of the optional init template that is run
   *          once before partition data is upgraded.
   * @param partitionTemplateFilepath
   *          (injected) The location of the per-partition upgrade template, run
   *          once per partition. The partitions is identified by the
   *          $partitionName velocity template variable.
   * @param partitionRollbackTemplateFilepath
   *          (injected) The location of the optional rollback per-partition
   *          template, used to reverse changes if any partition update fails.
   * @param cleanupTemplateFilepath
   *          (injected) The location of the optional cleanup template that is
   *          run once after partitions are upgraded.
   */
  @Override
  public void execute() throws BuildException {

    initETLConnection();
    final RockFactory dwhdbRockFactory = createDbRockFactory(etlrepRockFactory, DWH);

    try {

      // Construct all SQL strings before execution phase

      final String initSQL = generateSQLForTemplate(initTemplateFilepath, "");

      final List<String> partitionNames = getPartitionNames();
      final List<String> partitionSQLs = new ArrayList<String>();

      String partitionSQL = null;
      for (String partitionName : partitionNames) {
        partitionSQL = generateSQLForTemplate(partitionTemplateFilepath, partitionName);
        if (partitionSQL == null) {
          throw new BuildException("Invalid/Non-existant template found at: " + partitionTemplateFilepath);
        }
        partitionSQLs.add(partitionSQL);
      }

      final List<String> rollbackSQLs = new ArrayList<String>();
      String rollbackSQL = null;
      for (String partitionName : partitionNames) {
        rollbackSQL = generateSQLForTemplate(partitionRollbackTemplateFilepath, partitionName);
        if (rollbackSQL != null) {
          rollbackSQLs.add(rollbackSQL);
        }
      }

      final String cleanupSQL = generateSQLForTemplate(cleanupTemplateFilepath, "");

      // SQL Execution Phase

      // INIT
      if (initSQL != null) {
        System.out.println("Executing SQL from: " + initTemplateFilepath);
        executeSQL(dwhdbRockFactory, initSQL);
        dwhdbRockFactory.getConnection().commit();
      }

      // PARTITION x N
      System.out.println("Executing SQL from: " + partitionTemplateFilepath);
      executeParallelSQL(partitionSQLs);

      // ROLLBACK (optional)
      if (isRollbackFlag()) {
        System.out.println("Executing SQL from: " + partitionRollbackTemplateFilepath);
        executeParallelSQL(rollbackSQLs);
      }

      // CLEAN-UP
      if (cleanupSQL != null) {
        System.out.println("Executing SQL from: " + cleanupTemplateFilepath);
        executeSQL(dwhdbRockFactory, cleanupSQL);
        dwhdbRockFactory.getConnection().commit();
      }

      System.out.println("Task: " + this.getClass().getSimpleName() + " completed OK.");
    } catch (SQLException e) {
      throw new BuildException(e);
    } finally {
      if (dwhdbRockFactory.getConnection() != null) {
        try {
          dwhdbRockFactory.getConnection().close();
        } catch (SQLException e) {
        }
      }
    }
  }

  /**
   * Create database connections to ETLREP
   */
  public void initETLConnection() {
    etlrepRockFactory = createEtlrepRockFactory();
  }

  /**
   * Retrieves the partitionNames for input storageId(s), using the
   * repdb.dwhparitition table.
   * 
   * @return partitionNames
   * @throws SQLException
   */
  public List<String> getPartitionNames() throws SQLException {
    // parse target storage ids
    final List<String> storageIds = new ArrayList<String>(50);
    final StringTokenizer partition_tokens = new StringTokenizer(partitionStorageIdList, ",");
    while (partition_tokens.hasMoreElements()) {
      storageIds.add(partition_tokens.nextToken().trim());
    }

    // select tablename from dwhpartition where storageid = 'EVENT_E_SGEH_SUC:RAW'
    final RockFactory dwhrepRockFactory = createDbRockFactory(etlrepRockFactory, DWHREP);
    final Connection connection = dwhrepRockFactory.getConnection();
    Statement statement = null;
    final List<String> partitions = new ArrayList<String>(50);
    ResultSet rs = null;

    try {
      statement = connection.createStatement();

      for (String storageId : storageIds) {
        rs = statement.executeQuery("select tablename from dwhpartition where storageid = '" + storageId + "';");

        int count = 0;
        while (rs.next()) {
          partitions.add(rs.getString(1));
          count++;
        }

        System.out.println("Resolved " + count + " partitions from StorageID: " + storageId);
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (connection != null) {
        connection.close();
      }
    }

    return partitions;
  }

  /**
   * Execute a sqlStatement, leaving connection open.
   * 
   * @param rockFactory
   *          - the target DB connection (left open after execution).
   * @param sqlString
   *          - the sqlStatement to execute
   * @throws SQLException
   *           , IllegalArgumentException
   */
  public void executeSQL(final RockFactory rockFactory, final String sqlString) throws SQLException {

    if (sqlString == null || "".equals(sqlString)) {
      throw new IllegalArgumentException("sqlString must have a value");
    }

    if (debugFlag) {
      System.out.println("Executing SQL [[\n" + sqlString + "\n]]\n");
    }

    final Connection connection = rockFactory.getConnection(); // NOPMD - connection left open
    Statement statement = null;

    try {
      statement = connection.createStatement();
      statement.execute(sqlString);
    } finally {
      if (statement != null) {
        statement.close();
      }
    }
  }

  /**
   * Execute a list of per-partitions SQL statements in parallel.
   * 
   * @param partitionSQLs
   */
  public void executeParallelSQL(final List<String> partitionSQLs) {

    int currentPartitionIndex = 0;
    final String[] partitionArray = partitionSQLs.toArray(new String[0]);

    while (currentPartitionIndex < partitionArray.length) {

      final CountDownLatch doneSignal = new CountDownLatch(Math.min(maxParallel.intValue(),
          (partitionArray.length - currentPartitionIndex)));

      for (int parallelCount = 0; (parallelCount < maxParallel) && (currentPartitionIndex < partitionArray.length); parallelCount++, currentPartitionIndex++) {

        // Spawn a thread to execute the request
        final String partitionSQL = partitionArray[currentPartitionIndex];
        final RockFactory dwhdbRockFactory = createDbRockFactory(etlrepRockFactory, CommonDBTasks.DWH);
        new Thread(new PartitionSQLWorker(dwhdbRockFactory, partitionSQL, this, doneSignal)).start(); // NOPMD
      }

      // Wait for completion of all partition threads
      try {
        doneSignal.await();
      } catch (InterruptedException e) {
        System.out.println("Warning: Interrupted while waiting for parition updates to complete.");
        setRollbackFlag(true);
        break;
      }
    }
  }

  /**
   * Evaluate a SQL velocity template, using an optional partitionName variable.
   * 
   * @param templateFilePath
   *          The velocity template filePath (must be on same root path as
   *          execution folder).
   * @param partitionName
   *          Optional partitionName.
   * @return
   */
  public String generateSQLForTemplate(final String templateFilePath, final String partitionName) {

    if (templateFilePath == null || "".equals(templateFilePath)) {
      return null;
    }

    String relativeFilePath = templateFilePath;
    try {
      // Find the root path, e.g. '/' or 'C:\\'
      final String anypath = new File("").getAbsolutePath();
      final String fsRoot = anypath.substring(0, anypath.indexOf(File.separator));

      // Set the base path to the root patht.
      final Properties p = new Properties();
      p.setProperty("file.resource.loader.path", fsRoot);
      Velocity.init(p);

      // Remove the root path to create a relative filePath for Velocity.
      relativeFilePath = templateFilePath.substring(fsRoot.length());

      final VelocityContext context = new VelocityContext();
      context.put("partitionName", partitionName);

      Template template;
      template = Velocity.getTemplate(relativeFilePath);
      final StringWriter sw = new StringWriter();
      template.merge(context, sw);
      return sw.toString();
    } catch (final ResourceNotFoundException e) {
      throw new IllegalStateException("Can not find velocity template " + relativeFilePath, e);
    } catch (final ParseErrorException e) {
      throw new IllegalStateException("Error parsing velocity template " + relativeFilePath, e);
    } catch (final Exception e) {
      throw new IllegalStateException("Error during template initialization " + relativeFilePath, e);
    }
  }

  /**
   * @return the partitionStorageId
   */
  public String getPartitionStorageIdList() {
    return partitionStorageIdList;
  }

  /**
   * @param partitionStorageId
   *          the partitionStorageId to set
   */
  public void setPartitionStorageIdList(final String partitionStorageId) {
    this.partitionStorageIdList = partitionStorageId;
  }


  /**
   * @return the initTemplateFilepath
   */
  public String getInitTemplateFilepath() {
    return initTemplateFilepath;
  }


  /**
   * @param initTemplateFilepath
   *          the initTemplateFilepath to set
   */
  public void setInitTemplateFilepath(final String initTemplateFilepath) {
    this.initTemplateFilepath = initTemplateFilepath;
  }

  /**
   * @return the partitionTemplateFilepath
   */
  public String getPartitionTemplateFilepath() {
    return partitionTemplateFilepath;
  }


  /**
   * @param partitionTemplateFilepath
   *          the partitionTemplateFilepath to set
   */
  public void setPartitionTemplateFilepath(final String partitionTemplateFilepath) {
    this.partitionTemplateFilepath = partitionTemplateFilepath;
  }


  /**
   * @return the cleanupTemplateFilepath
   */
  public String getCleanupTemplateFilepath() {
    return cleanupTemplateFilepath;
  }

  /**
   * @param cleanupTemplateFilepath
   *          the cleanupTemplateFilepath to set
   */
  public void setCleanupTemplateFilepath(final String cleanupTemplateFilepath) {
    this.cleanupTemplateFilepath = cleanupTemplateFilepath;
  }

  /**
   * @return the partitionRollbackTemplateFilepath
   */
  public String getPartitionRollbackTemplateFilepath() {
    return partitionRollbackTemplateFilepath;
  }

  /**
   * @param partitionRollbackTemplateFilepath
   *          the partitionRollbackTemplateFilepath to set
   */
  public void setPartitionRollbackTemplateFilepath(final String partitionRollbackTemplateFilepath) {
    this.partitionRollbackTemplateFilepath = partitionRollbackTemplateFilepath;
  }

  /**
   * @return the rollbackFlag
   */
  public synchronized boolean isRollbackFlag() {
    return rollbackFlag;
  }

  /**
   * @param rollbackFlag
   *          the rollbackFlag to set
   */
  protected synchronized void setRollbackFlag(final boolean rollbackFlag) {
    this.rollbackFlag = rollbackFlag;
  }

  /**
   * @return the debug
   */
  public String getDebug() {
    return debug;
  }

  /**
   * @param debug
   *          the debug to set
   */
  public void setDebug(final String debug) {
    this.debug = debug;
    if (debug != null && ("ON".equals(debug.toUpperCase()) || "TRUE".equals(debug.toUpperCase()))) {
      this.debugFlag = true;
    } else {
      this.debugFlag = false;
    }
  }

  /**
   * @return the maxParallel
   */
  public Integer getMaxParallel() {
    return maxParallel;
  }

  /**
   * @param maxParallel
   *          the maxParallel to set
   */
  public void setMaxParallel(final Integer maxParallel) {
    if (maxParallel.intValue() > 0) {
      this.maxParallel = maxParallel;
    } else {
      throw new IllegalArgumentException("maxParallel value must be a positive integer > 0");
    }
  }

}

/**
 * Inner class to manage parallel per-partition thread execution
 * 
 * @author efinian
 * 
 */
class PartitionSQLWorker implements Runnable {

  final private CountDownLatch doneSignal;
  final private RockFactory dwhdbRockFactory;
  final private String partitionSQL;
  final private PartitionDataUpgrade parent;

  /**
   * Construct a worker thread, for SQL Execution.
   * 
   * @param dwhdbRockFactory
   *          The connection to the target database. Connection is always closed
   *          on return from doWork()
   * @param partitionSQL
   *          The SQL to execute
   * @param parent
   *          The parent class (for roll-back call-back and SQLexecution
   *          method).
   * @param doneSignal
   *          The count-down latch - decremented on completion for
   *          synchronisation purposes.
   */
  PartitionSQLWorker(final RockFactory dwhdbRockFactory, final String partitionSQL, final PartitionDataUpgrade parent,
      final CountDownLatch doneSignal) {
    this.dwhdbRockFactory = dwhdbRockFactory;
    this.partitionSQL = partitionSQL;
    this.doneSignal = doneSignal;
    this.parent = parent;
  }

  @Override
  public void run() {
      doWork();
      doneSignal.countDown();
  }

  void doWork() {
    try {
      parent.executeSQL(dwhdbRockFactory, partitionSQL);
    } catch (SQLException e) {
      System.out.println("Error occured during parallel background SQL execution: " + e.getMessage());
      e.printStackTrace(System.out);
      parent.setRollbackFlag(true);
    } finally {
      if (dwhdbRockFactory.getConnection() != null) {
        try {
          dwhdbRockFactory.getConnection().close();
        } catch (SQLException e) {
        }
      }
    }
  }
}
