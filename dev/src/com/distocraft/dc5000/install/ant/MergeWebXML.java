/**
 * 
 */
package com.distocraft.dc5000.install.ant;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ericsson.eniq.install.tools.EnvEntryWebXmlNode;
import com.ericsson.eniq.install.tools.ListenerWebXmlNode;
import com.ericsson.eniq.install.tools.ServletMappingWebXmlNode;
import com.ericsson.eniq.install.tools.ServletWebXmlNode;
import com.ericsson.eniq.install.tools.WebXmlNode;
import com.ericsson.eniq.install.tools.WelcomeFileListWebXmlNode;

/**
 * 
 * This class merges some elements from busyhourcfg web.xml file into adminui web.xml file:
 * servlet, listener, servlet-mapping, welcome-file-list and env-entry.
 * It perform simple check that element does not exist before adding it, but if element has been
 * changed it can not merge it properly. 
 * 
 * @author eheijun
 * 
 */
public class MergeWebXML extends Task {

  public String sourceFile;
  
  public String targetFile;
  
  private Document sourceWebXML;  

  private Document targetWebXML;

  /**
   * Actual ant task to perform merge. Both source and target files should be setted
   * before execution.
   */
  public void execute() {

    if (sourceWebXML == null || targetWebXML == null) {
      throw new BuildException("Both source and target files must be defined");
    }

    try {

      // Get root of target document and investigate what elements it has to
      // find right places to insert new elements
      final Element targetRoot = targetWebXML.getDocumentElement();
      final NodeList targetNodes = targetRoot.getChildNodes();

      Node[] targetPlace = new Node[7];

      for (int i = 0; i < targetNodes.getLength(); i++) {
        final Node current = targetNodes.item(i);
        final int type = current.getNodeType();
        switch (type) {
        case Node.ELEMENT_NODE:
          if ((targetPlace[0] == null) && current.getNodeName().equals("servlet")) {
            targetPlace[0] = current;
          }
          if ((targetPlace[1] == null) && current.getNodeName().equals("listener")) {
            targetPlace[1] = current;
          }
          if ((targetPlace[2] == null) && current.getNodeName().equals("servlet-mapping")) {
            targetPlace[2] = current;
          }
          if ((targetPlace[3] == null) && current.getNodeName().equals("security-constraint")) {
            targetPlace[3] = current;
          }
          if ((targetPlace[4] == null) && current.getNodeName().equals("login-config")) {
            targetPlace[4] = current;
          }
          if ((targetPlace[5] == null) && current.getNodeName().equals("security-role")) {
            targetPlace[5] = current;
          }
          if ((targetPlace[6] == null) && current.getNodeName().equals("env-entry")) {
            targetPlace[6] = current;
          }
          break;
        }
      }

      for (int j = targetPlace.length; j > 0; j--) {
        if (targetPlace[j - 1] == null) {
          if (j < targetPlace.length) {
            targetPlace[j - 1] = targetPlace[j];
          }
        }
      }

      final Element sourceRoot = sourceWebXML.getDocumentElement();
      final NodeList sourceNodes = sourceRoot.getChildNodes();

      // Copy all required elements from source into target
      for (int i = 0; i < sourceNodes.getLength(); i++) {
        final Node current = sourceNodes.item(i);
        switch (current.getNodeType()) {
        case Node.ELEMENT_NODE:
          if (current.getNodeName().equals("servlet")) {
            copyNodeToTarget(targetWebXML, current, targetPlace[1]);
          }
          if (current.getNodeName().equals("listener")) {
            copyNodeToTarget(targetWebXML, current, targetPlace[2]);
          }
          if (current.getNodeName().equals("servlet-mapping")) {
            copyNodeToTarget(targetWebXML, current, targetPlace[3]);
          }
          if (current.getNodeName().equals("welcome-file-list")) {
            copyNodeToTarget(targetWebXML, current, targetPlace[4]);
          }
          if (current.getNodeName().equals("env-entry")) {
            copyNodeToTarget(targetWebXML, current, null);
          }
          break;
        }
      }

      // Initialise the OutputFormat
      final OutputFormat format = new OutputFormat(targetWebXML);
      format.setIndenting(true);
      format.setLineSeparator(System.getProperty("line.separator"));

      // write changes into target file
      final XMLSerializer serializer = new XMLSerializer(format);
      serializer.setOutputCharStream(new java.io.FileWriter(targetFile));
      serializer.serialize(targetWebXML);

    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new BuildException("IO problem: " + ioe.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Fatal problem: " + e.getMessage());
    }

  }

  public String getSourceFile() {
    return sourceFile;
  }

  public void setSourceFile(final String sourceFile) {
    final File in = new File(sourceFile);
    if(!in.canRead() || !in.isFile()) {
      throw new BuildException("SourceFile cannot be read or not file");
    }
    // Create the dom document for source
    final DOMParser parser = new DOMParser();
    try {
      parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      parser.parse(sourceFile);
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    sourceWebXML = parser.getDocument();
    this.sourceFile = sourceFile;
  }

  public String getTargetFile() {
    return targetFile;
  }

  public void setTargetFile(final String targetFile) {
    final File out = new File(targetFile);
    if(!out.canWrite() || !out.canRead() || !out.isFile()) {
      throw new BuildException("TargetFile cannot be read or written or not file");
    }
    // Create the dom document for target
    final DOMParser parser = new DOMParser();
    try {
      parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      parser.parse(targetFile);
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    targetWebXML = parser.getDocument();
    this.targetFile = targetFile;
    
  }

  
  public Document getSourceWebXML() {
    return sourceWebXML;
  }

  
  public void setSourceWebXML(Document sourceWebXML) {
    this.sourceWebXML = sourceWebXML;
  }

  
  public Document getTargetWebXML() {
    return targetWebXML;
  }

  
  public void setTargetWebXML(Document targetWebXML) {
    this.targetWebXML = targetWebXML;
  }

  private boolean compareNodes(final Node targetNode, final Node sourceNode) {

    WebXmlNode sourceWebXmlNode;
    WebXmlNode targetWebXmlNode;
    boolean isSame = false;
    if (targetNode.getNodeType() == sourceNode.getNodeType()) {
      if (targetNode.getNodeName().equals(sourceNode.getNodeName())) {
        if (sourceNode.getNodeName().equals("servlet")) {
          sourceWebXmlNode = new ServletWebXmlNode(sourceNode);
          targetWebXmlNode = new ServletWebXmlNode(targetNode);
          isSame = sourceWebXmlNode.isSameAs(targetWebXmlNode);
        }
        if (sourceNode.getNodeName().equals("servlet-mapping")) {
          sourceWebXmlNode = new ServletMappingWebXmlNode(sourceNode);
          targetWebXmlNode = new ServletMappingWebXmlNode(targetNode);
          isSame = sourceWebXmlNode.isSameAs(targetWebXmlNode);
        }
        if (sourceNode.getNodeName().equals("listener")) {
          sourceWebXmlNode = new ListenerWebXmlNode(sourceNode);
          targetWebXmlNode = new ListenerWebXmlNode(targetNode);
          isSame = sourceWebXmlNode.isSameAs(targetWebXmlNode);
        }
        if (sourceNode.getNodeName().equals("welcome-file-list")) {
          sourceWebXmlNode = new WelcomeFileListWebXmlNode(sourceNode);
          targetWebXmlNode = new WelcomeFileListWebXmlNode(targetNode);
          isSame = sourceWebXmlNode.isSameAs(targetWebXmlNode);
        }
        if (sourceNode.getNodeName().equals("env-entry")) {
          sourceWebXmlNode = new EnvEntryWebXmlNode(sourceNode);
          targetWebXmlNode = new EnvEntryWebXmlNode(targetNode);
          isSame = sourceWebXmlNode.isSameAs(targetWebXmlNode);
        }
      }
    }
    return isSame;
  }

  private void copyNodeToTarget(final Document targetWebXML, final Node sourceNode, final Node placeholderNode) {
    final String originalTextVal = sourceNode.getNodeName();
    boolean nodeExists = false;

    final Element targetRoot = targetWebXML.getDocumentElement();
    final NodeList targetNodes = targetRoot.getChildNodes();

    if (targetNodes != null) {
      for (int i = 0; i < targetNodes.getLength(); i++) {
        final Node targetNode = targetNodes.item(i);
        if (targetNode.getNodeType() == Node.ELEMENT_NODE) {
          nodeExists = compareNodes(targetNode, sourceNode);
        }
        if (nodeExists) {
          break;
        }
      }
    }
    if (nodeExists) {
      System.out.println("no need to copying " + originalTextVal);
    } else {
      System.out.println("copying " + originalTextVal + " from source into target");
      final Node importedNode = targetWebXML.importNode(sourceNode, true);
      if (placeholderNode == null) {
        targetRoot.appendChild(importedNode);
      } else {
        targetRoot.insertBefore(importedNode, placeholderNode);
      }
    }
  }

//  /**
//   * basic test  
//   * @param args
//   */
//  public static void main(final String[] args) {
//    final MergeWebXML mergeWebXML = new MergeWebXML();
//    mergeWebXML.setSourceFile("C:\\work\\eclipse_workspaces\\busyhour_workspace\\busyhourcfg\\web\\WEB-INF\\web.xml");
//    mergeWebXML
//        .setTargetFile("C:\\CCRC\\eheijun_busyhourimprovements_view\\vobs\\eniq\\design\\plat\\admin_ui\\dev\\admin_ui\\web\\WEB-INF\\web.xml");
//    // mergeWebXML.setTargetFile("C:\\Temp\\mergexmltest\\mergeddemos.xml");
//    mergeWebXML.execute();
//  }

}
