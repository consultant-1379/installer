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
public class WelcomeFileListWebXmlNode extends XmlNode implements WebXmlNode {

  private final String welcomefile;

  public WelcomeFileListWebXmlNode(final Node node) {
    final Element ele = (Element) node;
    this.welcomefile = getTextValue(ele, "welcome-file");
  }

  private Object getWelcomefile() {
    return welcomefile;
  }

  @Override
  public boolean isSameAs(final WebXmlNode anotherNode) {
    if (anotherNode instanceof WelcomeFileListWebXmlNode) {
      final WelcomeFileListWebXmlNode tmp = (WelcomeFileListWebXmlNode) anotherNode;
      if (this.welcomefile.equals(tmp.getWelcomefile())) {
        return true;
      }
    }
    return false;
  }

}
