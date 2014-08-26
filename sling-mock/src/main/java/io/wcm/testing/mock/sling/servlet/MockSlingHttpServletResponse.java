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

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;

import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.adapter.SlingAdaptable;

/**
 * Mock {@link SlingHttpServletResponse} implementation.
 */
public class MockSlingHttpServletResponse extends SlingAdaptable implements SlingHttpServletResponse {

  private static final String CHARSET_SEPARATOR = ";charset=";

  private static final String RFC_1123_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
  private static final DateFormat RFC1123_DATE_FORMAT = new SimpleDateFormat(RFC_1123_DATE_PATTERN, Locale.US);
  static {
    RFC1123_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  private String contentType = "text/html";
  private String characterEncoding;
  private int contentLength;
  private int status = 200;
  private final List<KeyValue<String, String>> headers = new ArrayList<>();

  @Override
  public void flushBuffer() throws IOException {
    // ignore
  }

  @Override
  public String getContentType() {
    return this.contentType + (StringUtils.isNotBlank(characterEncoding) ? CHARSET_SEPARATOR + characterEncoding : "");
  }

  @Override
  public void setContentType(final String type) {
    this.contentType = type;
    if (StringUtils.contains(this.contentType, CHARSET_SEPARATOR)) {
      this.characterEncoding = StringUtils.substringAfter(this.contentType, CHARSET_SEPARATOR);
      this.contentType = StringUtils.substringBefore(this.contentType, CHARSET_SEPARATOR);
    }
  }

  @Override
  public void setCharacterEncoding(final String charset) {
    this.characterEncoding = charset;
  }

  @Override
  public String getCharacterEncoding() {
    return this.characterEncoding;
  }

  @Override
  public void setContentLength(final int len) {
    this.contentLength = len;
  }

  public int getContentLength() {
    return this.contentLength;
  }

  @Override
  public void setStatus(final int sc, final String sm) {
    setStatus(sc);
  }

  @Override
  public void setStatus(final int sc) {
    this.status = sc;
  }

  @Override
  public int getStatus() {
    return this.status;
  }

  @Override
  public void sendError(final int sc, final String msg) {
    setStatus(sc);
  }

  @Override
  public void sendError(final int sc) {
    setStatus(sc);
  }

  @Override
  public void sendRedirect(final String location) {
    setStatus(302);
    setHeader("Location", location);
  }

  @Override
  public void addHeader(final String name, final String value) {
    headers.add(new DefaultKeyValue<String, String>(name, value));
  }

  @Override
  public void addIntHeader(final String name, final int value) {
    headers.add(new DefaultKeyValue<String, String>(name, Integer.toString(value)));
  }

  @Override
  public void addDateHeader(final String name, final long date) {
    headers.add(new DefaultKeyValue<String, String>(name, formatDate(new Date(date))));
  }

  @Override
  public void setHeader(final String name, final String value) {
    removeHeaders(name);
    addHeader(name, value);
  }

  @Override
  public void setIntHeader(final String name, final int value) {
    removeHeaders(name);
    addIntHeader(name, value);
  }

  @Override
  public void setDateHeader(final String name, final long date) {
    removeHeaders(name);
    addDateHeader(name, date);
  }

  private void removeHeaders(final String name) {
    for (int i = this.headers.size() - 1; i >= 0; i--) {
      if (StringUtils.equals(this.headers.get(i).getKey(), name)) {
        headers.remove(i);
      }
    }
  }

  @Override
  public boolean containsHeader(final String name) {
    return !getHeaders(name).isEmpty();
  }

  @Override
  public String getHeader(final String name) {
    Collection<String> values = getHeaders(name);
    if (!values.isEmpty()) {
      return values.iterator().next();
    }
    else {
      return null;
    }
  }

  @Override
  public Collection<String> getHeaders(final String name) {
    List<String> values = new ArrayList<>();
    for (KeyValue<String, String> entry : headers) {
      if (StringUtils.equals(entry.getKey(), name)) {
        values.add(entry.getValue());
      }
    }
    return values;
  }

  @Override
  public Collection<String> getHeaderNames() {
    Set<String> values = new HashSet<>();
    for (KeyValue<String, String> entry : headers) {
      values.add(entry.getKey());
    }
    return values;
  }

  static synchronized String formatDate(Date pDate) {
    return RFC1123_DATE_FORMAT.format(pDate);
  }

  static synchronized Date parseDate(String pDateString) throws ParseException {
    return RFC1123_DATE_FORMAT.parse(pDateString);
  }


  // --- unsupported operations ---
  @Override
  public int getBufferSize() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Locale getLocale() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ServletOutputStream getOutputStream() {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrintWriter getWriter() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCommitted() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void reset() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void resetBuffer() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBufferSize(final int size) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLocale(final Locale loc) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addCookie(final Cookie cookie) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String encodeRedirectUrl(final String url) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String encodeRedirectURL(final String url) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String encodeUrl(final String url) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String encodeURL(final String url) {
    throw new UnsupportedOperationException();
  }

}
