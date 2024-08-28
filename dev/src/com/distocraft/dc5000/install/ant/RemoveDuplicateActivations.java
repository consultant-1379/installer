package com.distocraft.dc5000.install.ant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class RemoveDuplicateActivations extends Task {

  private String filepath = "";

  /**
   * This custom ANT-task reads a file, removes duplicate rows from the file and
   * overwrites it.
   */
  public void execute() throws BuildException {
    BufferedReader reader = null;
    BufferedWriter writer = null;

    try {

      File file = new File(filepath);
      ArrayList<String> filerows = new ArrayList<String>();

      if (file.isFile()) {
        if (file.canRead()) {
          System.out.println("Reading file " + file.getName() + " for duplicate activations.");

          reader = new BufferedReader(new FileReader(file));

          String line = new String();
          
          while ((line = reader.readLine()) != null) {
            if (filerows.contains(line.toString())) {
              System.out.println("Interface activation \"" + line.trim() + "\" dropped as a duplicate entry.");
            } else {
              filerows.add(line);
            }
          }

          reader.close();

          System.out.println("Trying to overwrite file " + file.getName() + ".");

          if (file.canWrite()) {
            writer = new BufferedWriter(new FileWriter(file));
            Iterator<String> filerowsIter = filerows.iterator();

            while (filerowsIter.hasNext()) {
              String currentRow = filerowsIter.next();
              writer.write(currentRow);
              writer.newLine();
            }

            writer.close();
            System.out.println("File " + file.getName() + " overwritten successfully.");

          } else {
            System.out.println("Can't overwrite file " + file.getName() + ". Removal of duplicate activations failed.");
            if (reader != null) {
              reader.close();
            }
            if (writer != null) {
              writer.close();
            }
            System.exit(1);
          }

        } else {
          System.out.println("File " + file.getName() + " was not readable.");
          if (reader != null) {
            reader.close();
          }
          if (writer != null) {
            writer.close();
          }
          System.exit(2);
        }
      } else {
        System.out.println("File " + file.getName() + " is not a file. Please check the file parameter.");
        if (reader != null) {
          reader.close();
        }
        if (writer != null) {
          writer.close();
        }
        System.exit(3);
      }
    } catch (Exception e) {
      System.out.println("Removing duplicate activations failed.");
      System.out.println(e.getMessage());
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
        if (writer != null) {
          writer.close();
        }
      } catch (Exception e) {

      }
    }
  }

  public String getFilepath() {
    return filepath;
  }

  public void setFilepath(String filepath) {
    this.filepath = filepath;
  }

}
