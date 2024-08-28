/**
 * 
 */
package com.ericsson.eniq.install.tools;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author eheijun
 * 
 */
public class XmlNode {

  /**
   * Gets text value from xml element
   * @param ele source element
   * @param tagName name of the query tag
   * @return first text value by tagName but if tag does not exists returns empty string
   */
  protected String getTextValue(final Element ele, final String tagName) {
    String textVal = "";
    final NodeList nl = ele.getElementsByTagName(tagName);
    if (nl != null && nl.getLength() > 0) {
      final Element el = (Element) nl.item(0);
      textVal = el.getFirstChild().getNodeValue();
    }
    return textVal;
  }
}
