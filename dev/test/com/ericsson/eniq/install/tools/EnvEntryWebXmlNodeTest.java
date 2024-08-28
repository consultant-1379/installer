package com.ericsson.eniq.install.tools;

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EnvEntryWebXmlNodeTest {
  private EnvEntryWebXmlNode testNode1 = null;
  private EnvEntryWebXmlNode testNode2 = null;

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
  public void test_getName() {
    final String desc = testNode1.getName();
    Assert.assertEquals("env-entry-name Not Set Correctly", "env-entry-name-1", desc);
  }

  @Test
  public void test_getType() {
    final String desc = testNode1.getType();
    Assert.assertEquals("env-entry-type Not Set Correctly", "env-entry-type-1", desc);
  }

  @Test
  public void test_getValue() {
    final String desc = testNode1.getValue();
    Assert.assertEquals("env-entry-value Not Set Correctly", "env-entry-value-1", desc);
  }


  private EnvEntryWebXmlNode mockNode(final String ID) {
    final Map<String, String> data = new HashMap<String, String>();
    data.put("env-entry-name", "env-entry-name-" + ID);
    data.put("env-entry-type", "env-entry-type-" + ID);
    data.put("env-entry-value", "env-entry-value-" + ID);
    final DummyElement de = new DummyElement(data);
    return new EnvEntryWebXmlNode(de);
  }
}
