package com.ericsson.eniq.install.tools;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WelcomeFileListWebXmlNodeTest {
  private WelcomeFileListWebXmlNode testNode1 = null;
  private WelcomeFileListWebXmlNode testNode2 = null;

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

  private WelcomeFileListWebXmlNode mockNode(final String ID) {
    final Map<String, String> data = new HashMap<String, String>();
    data.put("welcome-file", "welcome-file-" + ID);
    final DummyElement de = new DummyElement(data);
    return new WelcomeFileListWebXmlNode(de);
  }

  @Test
  public void test_getWelcomefile() throws Exception {
    final Method getWelcomefile = testNode1.getClass().getDeclaredMethod("getWelcomefile");
    getWelcomefile.setAccessible(true);
    final Object result = getWelcomefile.invoke(testNode1);
    Assert.assertEquals("welcome-file not set correctly", "welcome-file-1", result);
  }

  @Test
  public void test_isSameAs_TRUE() {
    final boolean equals = testNode1.isSameAs(testNode1);
    Assert.assertTrue("isSameAs should be TRUE", equals);
  }


  @Test
  public void test_isSameAs_FALSE() {
    final boolean equals = testNode1.isSameAs(testNode2);
    Assert.assertFalse("isSameAs should be FALSE", equals);
  }
}
