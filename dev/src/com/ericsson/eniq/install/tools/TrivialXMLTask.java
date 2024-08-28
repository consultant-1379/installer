/**
 * ----------------------------------------------------------------------- *
 * Copyright (C) 2010 LM Ericsson Limited. All rights reserved. *
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.install.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A simple custom ant task for modifying XML-files.
 * Supports two operations with following parameters.<br>
 * <br>
 * <table border="1">
 * <tr><th>Operation</th><th>Parameter</th><th>Description</th></tr>
 * <tr><td rowspan="4">SetAttribute</td><td>fileName</td><td>Name of XML file to modify</td></tr>
 * <tr><td>xPath</td><td>XPath definition of element containing the parameter</td></tr>
 * <tr><td>attribName</td><td>Name of attribute to set or modify</td></tr>
 * <tr><td>attribValue</td><td>Value of attribute to set or modify</td></tr>
 * <tr><td rowspan="6">AddElement</td><td>fileName</td><td>Name of XML file to modify</td></tr>
 * <tr><td>xPath</td><td>XPath definition of parent element of element to be added</td></tr>
 * <tr><td>elemName</td><td>Name of element to be added</td></tr>
 * <tr><td>attribName</td><td>Name of attribute to set or modify (OPTIONAL)</td></tr>
 * <tr><td>attribValue</td><td>Value of attribute to set or modify (OPTIONAL)</td></tr>
 * <tr><td>elemValue</td><td>Textual value of element (OPTIONAL)</td></tr>
 * <tr><td rowspan="3">SetValue</td><td>fileName</td><td>Name of XML file to modify</td></tr>
 * <tr><td>xPath</td><td>XPath definition of element which value is modified</td></tr>
 * <tr><td>elemValue</td><td>Textual value of element</td></tr>
 * </table>
 * 
 * @author etuolem
 */
public class TrivialXMLTask extends Task {

	public static final String SET_ATTRIB = "SetAttribute";
	public static final String ADD_ELEM = "AddElement";
	public static final String SET_VALUE = "SetValue";
  public static final String IS_DEFINED = "IsDefined";

	private String fileName = null;
	private String xPath = null;
	private String operation = null;
	private String elemName = null;
	private String elemValue = null;
	private String attribName = null;
	private String attribValue = null;
	private String isDefinedProperty = null;

	private transient File file;
	private transient XPathExpression expr;

	@Override
	public void execute() throws BuildException {

		try {

			readParameters();

			final DocumentBuilderFactory builderFact = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = builderFact.newDocumentBuilder();
			final Document doc = builder.parse(file);
			final NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			if (nodes == null || nodes.getLength() <= 0) {
        if(isDefinedOperation()){
          getProject().setProperty(this.isDefinedProperty, Boolean.toString(false));
          return;
        } else {
				  throw new BuildException("Node \"" + xPath + "\" not found from " + fileName + ". Check xPath parameter.");
        }
			} else if (nodes.getLength() > 1) {
				throw new BuildException("Multiple nodes \"" + xPath +"\" found from " + fileName + ". Check xPath parameter.");
			}
			
			final Element elem = (Element) nodes.item(0);
			if(IS_DEFINED.equalsIgnoreCase(this.operation)){
        getProject().setProperty(this.isDefinedProperty, Boolean.toString(true));
        return;
      } else if (SET_ATTRIB.equalsIgnoreCase(this.operation)) {
				final Attr attribute = doc.createAttribute(this.attribName);
				attribute.setNodeValue(this.attribValue);				
				elem.setAttributeNode(attribute);
			} else if (ADD_ELEM.equalsIgnoreCase(this.operation)) {
				final Element newElem = doc.createElement(this.elemName);
				elem.appendChild(newElem);
				
				if(this.attribName != null && this.attribName.length() > 0 && this.attribValue != null) {
					final Attr attribute = doc.createAttribute(this.attribName);
					attribute.setNodeValue(this.attribValue);				
					newElem.setAttributeNode(attribute);
				}
				
				if(this.elemValue != null && this.elemValue.length() > 0) {
					newElem.setTextContent(this.elemValue);
				}
			} else if (SET_VALUE.equalsIgnoreCase(this.operation)) {
				elem.setTextContent(this.elemValue);
			}

			final StringBuilder xmlOut = new StringBuilder();
			xmlOut.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
			printElement(xmlOut, doc, 0);
			
			final PrintWriter fileout = new PrintWriter(new FileOutputStream(this.file));
			fileout.print(xmlOut);
			fileout.close();
			
			System.out.println("File " + fileName + " modified at " + xPath);
			
		} catch (BuildException build) { // NOPMD
			throw build;			
		} catch (Exception e) {
			throw new BuildException("Failed exceptionally", e);
		}

	}

	/**
	 * Creates XML string presentation from DOM tree.
	 * Doing this because transformer does not work with ANT+JRE6.
	 */
	private void printElement(final StringBuilder builder, final Node node, final int intend) {
		
		if(node.getNodeType() == Node.ELEMENT_NODE) {
			for(int i = 0; i < intend ; i++) {
				builder.append(" ");
			}
			
			builder.append("<").append(node.getNodeName());
			
			final NamedNodeMap attrs = node.getAttributes();
			for(int i = 0 ; i < attrs.getLength() ; i++) {
				final Node attr = attrs.item(i);
				builder.append(" ").append(attr.getNodeName()).append("=\"");
				builder.append(attr.getNodeValue()).append("\"");
			}
				
			final NodeList nl = node.getChildNodes();
			if(nl.getLength() > 0) {
				builder.append(">");
				
				for(int i = 0 ; i < nl.getLength() ; i++) {
					if(nl.item(i).getNodeType() == Node.TEXT_NODE) {
						builder.append(nl.item(i).getNodeValue());
					} else {
						printElement(builder, nl.item(i), intend+1);
					}
				}
				builder.append("</").append(node.getNodeName()).append(">");
			} else {
				builder.append("/>");
			}
			
		} else {
			final NodeList nl = node.getChildNodes();
			for(int i = 0 ; i < nl.getLength() ; i++) {
				printElement(builder, nl.item(i), intend+1);
			}
		}
				
	}
	
	/**
	 * Reads parameters and throws BuildException if parameters are not valid.
	 */
	private void readParameters() throws BuildException {

		if (this.fileName == null) {
			throw new BuildException("FileName not defined");
		} else {
			final File file = new File(this.fileName);
			if (!file.canRead()) {
				throw new BuildException("Can't read file " + this.fileName);
			}
			this.file = file;
		}

		if (this.xPath == null) {
			throw new BuildException("xPath is not defined");
		} else {
			try {
				final XPathFactory factory = XPathFactory.newInstance();
				final XPath xpath = factory.newXPath();
				expr = xpath.compile(this.xPath);
			} catch (XPathExpressionException path) {
				throw new BuildException("Invalid xPath", path);
			}
		}

    if(isDefinedOperation()){
      if (this.isDefinedProperty == null || this.xPath == null || this.xPath.length() == 0) {
        throw new BuildException("Parameters isDefinedProperty and xPath must be defined for operation " + IS_DEFINED);
      }
    } else if (SET_ATTRIB.equalsIgnoreCase(this.operation)) {
			if (this.attribName == null || this.attribName.length() <= 0 || this.attribValue == null) {
				throw new BuildException("Parameters attribName and attribValue must be defined for operation " + SET_ATTRIB);
			}
		} else if (ADD_ELEM.equalsIgnoreCase(this.operation)) {
			if (this.elemName == null || this.elemName.length() <= 0) {
				throw new BuildException("Parameter elemName must be defined for operation " + ADD_ELEM);
			}
		} else if (SET_VALUE.equalsIgnoreCase(this.operation)) {
			if (this.elemValue == null || this.elemValue.length() <= 0) {
				throw new BuildException("Parameter elementValue must be defined for operation " + SET_VALUE);
			}
		} else {
			throw new BuildException("Unknown operation \"" + this.operation + "\"");
		}

	}

	public static void main(String[] args) {
    final Project p = new Project();
		final TrivialXMLTask tt = new TrivialXMLTask();
    tt.setProject(p);
    tt.setFileName("C:\\Users\\eeipca\\Desktop\\executioncontext.xml");
    tt.setOperation("isDefined");
    tt.setIsDefinedProperty("is_defined");
    tt.setXPath("//config[@name='EC1']//jdkarg[@value='-XX:+UseParNewGC']");
		tt.execute();
    System.out.println(p.getProperty("is_defined"));
	}
	
	// --- Setters and Getters ---

	public String getFileName() {
		return fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public String getXPath() {
		return xPath;
	}

	public void setXPath(final String xPath) {
		this.xPath = xPath;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(final String operation) {
		this.operation = operation;
	}

	public String getElemName() {
		return elemName;
	}

	public void setElemName(final String elemName) {
		this.elemName = elemName;
	}

	public String getAttribName() {
		return attribName;
	}

	public void setAttribName(final String attribName) {
		this.attribName = attribName;
	}

	public String getAttribValue() {
		return attribValue;
	}

	public void setAttribValue(final String attribValue) {
		this.attribValue = attribValue;
	}

	public String getElemValue() {
		return elemValue;
	}

	public void setElemValue(final String elemValue) {
		this.elemValue = elemValue;
	}

  public void setIsDefinedProperty(final String property){
    this.isDefinedProperty = property;
  }

  private boolean isDefinedOperation(){
    return IS_DEFINED.equalsIgnoreCase(this.operation);
  }

}
