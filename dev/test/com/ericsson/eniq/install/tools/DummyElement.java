package com.ericsson.eniq.install.tools;

import java.util.Map;
import org.apache.xerces.dom.ElementImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

public class DummyElement extends ElementImpl {
  private final Map<String, String> data;
  private String valueGet = null;
  private final NodeList proxyList = new NodeList() {
    @Override
    public Node item(final int index) {
      return DummyElement.this;
    }

    @Override
    public int getLength() {
      return 1;
    }
  };

  public DummyElement(final Map<String, String> data) {
    this.data = data;
  }

  @Override
  public Node getFirstChild() {
    return this;
  }

  @Override
  public String getNodeValue() throws DOMException {
    return data.get(valueGet);
  }

  @Override
  public TypeInfo getSchemaTypeInfo() {
    return null;
  }

  @Override
  public void setIdAttribute(String name, boolean isId) throws DOMException {
  }

  @Override
  public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
  }

  @Override
  public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
  }

  @Override
  public short compareDocumentPosition(Node other) throws DOMException {
    return 0;
  }

  @Override
  public String lookupPrefix(String namespaceURI) {
    return null;
  }

  @Override
  public Object getFeature(String feature, String version) {
    return null;
  }

  @Override
  public NodeList getElementsByTagName(final String s) {
    valueGet = s;
    return proxyList;
  }
}