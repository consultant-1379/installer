package com.ericsson.eniq.install.tools;

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServletWebXmlNodeTest {
  private ServletWebXmlNode testNode1 = null;
  private ServletWebXmlNode testNode2 = null;

  @Before
  public void before() {
    testNode1 = mockNode("1");
    testNode2 = mockNode("2");
  }

  @After
  public void after() {
    testNode1 = null;
    testNode2 = null;
  }

  @Test
  public void test_isSameAs_TRUE() {
    final boolean equals = testNode2.isSameAs(testNode2);
    Assert.assertTrue("sSameAs should be TRUE", equals);
  }

  @Test
  public void test_isSameAs_FALSE() {
    final boolean equals = testNode1.isSameAs(testNode2);
    Assert.assertFalse("sSameAs should be FALSE", equals);
  }

  @Test
  public void test_getDescription() {
    final String desc = testNode1.getDescription();
    Assert.assertEquals("description Not Set Correctly", "description-1", desc);
  }

  @Test
  public void test_getDisplayname() {
    final String desc = testNode1.getDisplayname();
    Assert.assertEquals("display-name Not Set Correctly", "display-name-1", desc);
  }

  @Test
  public void test_getServletname() {
    final String desc = testNode1.getServletname();
    Assert.assertEquals("servlet-name Not Set Correctly", "servlet-name-1", desc);
  }

  @Test
  public void test_getServletclass() {
    final String desc = testNode1.getServletclass();
    Assert.assertEquals("servlet-class Not Set Correctly", "servlet-class-1", desc);
  }


  private ServletWebXmlNode mockNode(final String ID) {
    final Map<String, String> data = new HashMap<String, String>();
    data.put("description", "description-" + ID);
    data.put("display-name", "display-name-" + ID);
    data.put("servlet-name", "servlet-name-" + ID);
    data.put("servlet-class", "servlet-class-" + ID);
    final DummyElement de = new DummyElement(data);
    return new ServletWebXmlNode(de);
  }
}
