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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.sling.api.SlingHttpServletResponse;
import org.junit.Before;
import org.junit.Test;

public class MockSlingHttpServletResponseTest {

  private SlingHttpServletResponse response;

  @Before
  public void setUp() throws Exception {
    this.response = new MockSlingHttpServletResponse();
  }

  @Test
  public void testFlushBuffer() throws IOException {
    // does nothing, but can be called
    this.response.flushBuffer();
  }

  @Test
  public void testContentTypeCharset() throws Exception {
    assertEquals("text/html", response.getContentType());
    assertNull(response.getCharacterEncoding());

    response.setContentType("text/plain;charset=UTF-8");
    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals("UTF-8", response.getCharacterEncoding());
  }

  @Test
  public void testContentLength() throws Exception {
    assertEquals(0, ((MockSlingHttpServletResponse)response).getContentLength());

    response.setContentLength(55);
    assertEquals(55, ((MockSlingHttpServletResponse)response).getContentLength());
  }

  @Test
  public void testHeaders() throws Exception {
    assertEquals(0, response.getHeaderNames().size());

    response.addHeader("header1", "value1");
    response.addIntHeader("header2", 5);
    response.addDateHeader("header3", System.currentTimeMillis());

    assertEquals(3, response.getHeaderNames().size());
    assertEquals("value1", response.getHeader("header1"));
    assertEquals("5", response.getHeader("header2"));
    assertNotNull(response.getHeader("header3"));

    response.setHeader("header1", "value2");
    response.addIntHeader("header2", 10);

    assertEquals(3, response.getHeaderNames().size());

    Collection<String> header1Values = response.getHeaders("header1");
    assertEquals(1, header1Values.size());
    assertEquals("value2", header1Values.iterator().next());

    Collection<String> header2Values = response.getHeaders("header2");
    assertEquals(2, header2Values.size());
    Iterator<String> header2Iterator = header2Values.iterator();
    assertEquals("5", header2Iterator.next());
    assertEquals("10", header2Iterator.next());
  }

  @Test
  public void testRedirect() throws Exception {
    response.sendRedirect("/location.html");
    assertEquals(302, response.getStatus());
    assertEquals("/location.html", response.getHeader("Location"));
  }

}
