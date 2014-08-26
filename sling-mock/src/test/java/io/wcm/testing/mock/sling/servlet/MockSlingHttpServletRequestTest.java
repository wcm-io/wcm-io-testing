/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.testing.mock.sling.servlet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.CharEncoding;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MockSlingHttpServletRequestTest {

  @Mock
  private ResourceResolver resourceResolver;
  @Mock
  private Resource resource;

  private MockSlingHttpServletRequest request;

  @Before
  public void setUp() throws Exception {
    this.request = new MockSlingHttpServletRequest(this.resourceResolver);
  }

  @Test
  public void testResourceResolver() {
    assertSame(this.resourceResolver, this.request.getResourceResolver());
  }

  @Test
  public void testDefaultResourceResolver() {
    assertNotNull(new MockSlingHttpServletRequest().getResourceResolver());
  }

  @Test
  public void testSession() {
    HttpSession session = this.request.getSession(false);
    assertNull(session);
    session = this.request.getSession();
    assertNotNull(session);
  }

  @Test
  public void testRequestPathInfo() {
    assertNotNull(this.request.getRequestPathInfo());
  }

  @Test
  public void testAttributes() {
    this.request.setAttribute("attr1", "value1");
    assertTrue(this.request.getAttributeNames().hasMoreElements());
    assertEquals("value1", this.request.getAttribute("attr1"));
    this.request.removeAttribute("attr1");
    assertFalse(this.request.getAttributeNames().hasMoreElements());
  }

  @Test
  public void testResource() {
    assertNull(this.request.getResource());
    this.request.setResource(this.resource);
    assertSame(this.resource, this.request.getResource());
  }

  @Test
  public void testContextPath() {
    assertNull(this.request.getContextPath());
    this.request.setContextPath("/ctx");
    assertEquals("/ctx", this.request.getContextPath());
  }

  @Test
  public void testLocale() {
    assertEquals(Locale.US, this.request.getLocale());
  }

  @Test
  public void testQueryString() throws UnsupportedEncodingException {
    assertNull(this.request.getQueryString());
    assertEquals(0, this.request.getParameterMap().size());
    assertFalse(this.request.getParameterNames().hasMoreElements());

    this.request.setQueryString("param1=123&param2=" + URLEncoder.encode("äöüß€!:!", CharEncoding.UTF_8)
        + "&param3=a&param3=b");

    assertNotNull(this.request.getQueryString());
    assertEquals(3, this.request.getParameterMap().size());
    assertTrue(this.request.getParameterNames().hasMoreElements());
    assertEquals("123", this.request.getParameter("param1"));
    assertEquals("äöüß€!:!", this.request.getParameter("param2"));
    assertArrayEquals(new String[] {
        "a", "b"
    }, this.request.getParameterValues("param3"));

    Map<String, String[]> paramMap = new LinkedHashMap<>();
    paramMap.put("p1", new String[] {
        "a"
    });
    paramMap.put("p2", new String[] {
        "b", "c"
    });
    this.request.setParameterMap(paramMap);

    assertEquals("p1=a&p2=b&p2=c", this.request.getQueryString());
  }

  @Test
  public void testSchemeSecure() {
    assertEquals("http", this.request.getScheme());
    assertFalse(this.request.isSecure());

    this.request.setScheme("https");
    assertEquals("https", this.request.getScheme());
    assertTrue(this.request.isSecure());
  }

  @Test
  public void testServerNamePort() {
    assertEquals("localhost", this.request.getServerName());
    assertEquals(80, this.request.getServerPort());

    this.request.setServerName("myhost");
    this.request.setServerPort(12345);
    assertEquals("myhost", this.request.getServerName());
    assertEquals(12345, this.request.getServerPort());
  }

}
