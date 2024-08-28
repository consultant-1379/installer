package com.distocraft.dc5000.install.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import junit.framework.JUnit4TestAdapter;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 * @author ejarsok
 *
 */

public class ForceDeleteFileTest {

  @Test
  public void testExecute() {
    ForceDeleteFile fDF = new ForceDeleteFile();
    File Dir = new File(System.getProperty("user.dir"));
    File fileToDel = new File(Dir, "fileToDelete");
    fileToDel.deleteOnExit();
    
    try {
      PrintWriter pw = new PrintWriter(new FileWriter(fileToDel));
      pw.write("foobar");
      pw.close();
    } catch (IOException e1) {
      e1.printStackTrace();
      fail("can't write inFile");
    }
    
    fDF.setFile("fileToDelete");
    fDF.execute();
    assertEquals(false, fileToDel.exists());
    
  }
  
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(ForceDeleteFileTest.class);
  }
}
