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
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;

import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.adapter.SlingAdaptable;

/**
 * Mock {@link SlingHttpServletResponse} implementation.
 */
public class MockSlingHttpServletResponse extends SlingAdaptable implements SlingHttpServletResponse {

  @Override
  public void flushBuffer() throws IOException {
    // ignore
  }

  // --- unsupported operations ---
  @Override
  public int getBufferSize() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getCharacterEncoding() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getContentType() {
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
  public void setCharacterEncoding(final String charset) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setContentLength(final int len) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setContentType(final String type) {
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
  public void addDateHeader(final String name, final long date) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addHeader(final String name, final String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addIntHeader(final String name, final int value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsHeader(final String name) {
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

  @Override
  public void sendError(final int sc, final String msg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendError(final int sc) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendRedirect(final String location) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDateHeader(final String name, final long date) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setHeader(final String name, final String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setIntHeader(final String name, final int value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setStatus(final int sc, final String sm) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setStatus(final int sc) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getStatus() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getHeader(final String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<String> getHeaders(final String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<String> getHeaderNames() {
    throw new UnsupportedOperationException();
  }

}
