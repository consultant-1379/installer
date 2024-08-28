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
public class EnvEntryWebXmlNode extends XmlNode implements WebXmlNode {

  private final String name;

  private final String type;

  private final String value;

  public EnvEntryWebXmlNode(final Node node) {
    final Element ele = (Element) node;
    this.name = getTextValue(ele, "env-entry-name");
    this.type = getTextValue(ele, "env-entry-type");
    this.value = getTextValue(ele, "env-entry-value");
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean isSameAs(final WebXmlNode anotherNode) {
    if (anotherNode instanceof EnvEntryWebXmlNode) {
      final EnvEntryWebXmlNode tmp = (EnvEntryWebXmlNode) anotherNode;
      if ((this.name.equals(tmp.getName())) && (this.type.equals(tmp.getType())) && (this.value.equals(tmp.getValue()))) {
        return true;
      }
    }
    return false;
  }

}
