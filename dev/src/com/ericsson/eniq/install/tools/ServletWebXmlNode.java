/**
 * 
 */
package com.ericsson.eniq.install.tools;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author eheijun
 * 
 */
public class ServletWebXmlNode extends XmlNode implements WebXmlNode {

  private final String description;

  private final String displayname;

  private final String servletname;

  private final String servletclass;

  public ServletWebXmlNode(final Node node) {
    final Element ele = (Element) node;
    this.description = getTextValue(ele, "description");
    this.displayname = getTextValue(ele, "display-name");
    this.servletname = getTextValue(ele, "servlet-name");
    this.servletclass = getTextValue(ele, "servlet-class");
  }

  public String getDescription() {
    return description;
  }

  public String getDisplayname() {
    return displayname;
  }

  public String getServletname() {
    return servletname;
  }

  public String getServletclass() {
    return servletclass;
  }

  @Override
  public boolean isSameAs(final WebXmlNode anotherNode) {
    if (anotherNode instanceof ServletWebXmlNode) {
      final ServletWebXmlNode tmp = (ServletWebXmlNode) anotherNode;
      if ((this.description.equals(tmp.getDescription())) && (this.displayname.equals(tmp.getDisplayname()))
          && (this.servletname.equals(tmp.getServletname())) && (this.servletclass.equals(tmp.getServletclass()))) {
        return true;
      }
    }
    return false;
  }
}
