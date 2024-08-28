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
public class ServletMappingWebXmlNode extends XmlNode implements WebXmlNode {

  private final String servletname;

  private final String urlpattern;

  public ServletMappingWebXmlNode(final Node node) {
    final Element ele = (Element) node;
    this.servletname = getTextValue(ele, "servlet-name");
    this.urlpattern = getTextValue(ele, "url-pattern");
  }

  public String getServletname() {
    return servletname;
  }

  public String getUrlpattern() {
    return urlpattern;
  }

  @Override
  public boolean isSameAs(final WebXmlNode anotherNode) {
    if (anotherNode instanceof ServletMappingWebXmlNode) {
      final ServletMappingWebXmlNode tmp = (ServletMappingWebXmlNode) anotherNode;
      if ((this.urlpattern.equals(tmp.getUrlpattern())) && (this.servletname.equals(tmp.getServletname()))) {
        return true;
      }
    }
    return false;
  }
}
