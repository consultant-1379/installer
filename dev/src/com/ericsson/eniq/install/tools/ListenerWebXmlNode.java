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
public class ListenerWebXmlNode extends XmlNode implements WebXmlNode {

  private final String description;

  private final String displayname;

  private final String listenerclass;

  public ListenerWebXmlNode(final Node node) {
    final Element ele = (Element) node;
    this.description = getTextValue(ele, "description");
    this.displayname = getTextValue(ele, "display-name");
    this.listenerclass = getTextValue(ele, "listener-class");
  }

  public String getDescription() {
    return description;
  }

  public String getDisplayname() {
    return displayname;
  }

  public String getListenerclass() {
    return listenerclass;
  }

  @Override
  public boolean isSameAs(final WebXmlNode anotherNode) {
    if (anotherNode instanceof ListenerWebXmlNode) {
      final ListenerWebXmlNode tmp = (ListenerWebXmlNode) anotherNode;
      if ((this.description.equals(tmp.getDescription())) && (this.listenerclass.equals(tmp.getListenerclass()))
          && (this.displayname.equals(tmp.getDisplayname()))) {
        return true;
      }
    }
    return false;
  }

}
