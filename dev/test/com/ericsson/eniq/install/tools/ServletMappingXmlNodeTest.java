package com.ericsson.eniq.install.tools;

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServletMappingXmlNodeTest {
  private ServletMappingWebXmlNode testNode1 = null;
  private ServletMappingWebXmlNode testNode2 = null;

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
    Assert.assertFalse("isSameAs should be FALSE", equals);
  }

  @Test
  public void test_getServletname() {
    final String desc = testNode1.getServletname();
    Assert.assertEquals("iservlet-name Not Set Correctly", "servlet-name-1", desc);
  }

  @Test
  public void test_getUrlpattern() {
    final String desc = testNode1.getUrlpattern();
    Assert.assertEquals("display-name Not Set Correctly", "url-pattern-1", desc);
  }


  private ServletMappingWebXmlNode mockNode(final String ID) {
    final Map<String, String> data = new HashMap<String, String>();
    data.put("servlet-name", "servlet-name-" + ID);
    data.put("url-pattern", "url-pattern-" + ID);
    final DummyElement de = new DummyElement(data);
    return new ServletMappingWebXmlNode(de);
  }


}
