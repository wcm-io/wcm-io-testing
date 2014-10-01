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

import io.wcm.testing.mock.sling.MockSling;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.request.RequestProgressTracker;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;

/**
 * Mock {@link SlingHttpServletRequest} implementation.
 */
public final class MockSlingHttpServletRequest extends SlingAdaptable implements SlingHttpServletRequest {

  private final ResourceResolver resourceResolver;
  private final RequestPathInfo requestPathInfo = new MockRequestPathInfo();
  private final Map<String, Object> attributeMap = new HashMap<>();
  private final Map<String, String[]> parameterMap = new LinkedHashMap<>();
  private HttpSession session;
  private Resource resource;
  private String contextPath;
  private String queryString;
  private String scheme = "http";
  private String serverName = "localhost";
  private int serverPort = 80;
  private String method = HttpConstants.METHOD_GET;

  /**
   * Instantiate with default resource resolver
   */
  public MockSlingHttpServletRequest() {
    this.resourceResolver = MockSling.newResourceResolver();
  }

  /**
   * @param resourceResolver Resource resolver
   */
  public MockSlingHttpServletRequest(final ResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
  }

  @Override
  public ResourceResolver getResourceResolver() {
    return this.resourceResolver;
  }

  @Override
  public HttpSession getSession() {
    return getSession(true);
  }

  @Override
  public HttpSession getSession(final boolean create) {
    if (this.session == null && create) {
      this.session = new MockHttpSession();
    }
    return this.session;
  }

  @Override
  public RequestPathInfo getRequestPathInfo() {
    return this.requestPathInfo;
  }

  @Override
  public Object getAttribute(final String name) {
    return this.attributeMap.get(name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Enumeration<String> getAttributeNames() {
    return IteratorUtils.asEnumeration(this.attributeMap.keySet().iterator());
  }

  @Override
  public void removeAttribute(final String name) {
    this.attributeMap.remove(name);
  }

  @Override
  public void setAttribute(final String name, final Object object) {
    this.attributeMap.put(name, object);
  }

  @Override
  public Resource getResource() {
    return this.resource;
  }

  public void setResource(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public String getParameter(final String name) {
    Object object = this.parameterMap.get(name);
    if (object instanceof String) {
      return (String)object;
    }
    else if (object instanceof String[]) {
      String[] values = (String[])object;
      if (values.length > 0) {
        return values[0];
      }
    }
    return null;
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return this.parameterMap;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Enumeration<String> getParameterNames() {
    return IteratorUtils.asEnumeration(this.parameterMap.keySet().iterator());
  }

  @Override
  public String[] getParameterValues(final String name) { //NOPMD
    Object object = this.parameterMap.get(name);
    if (object instanceof String) {
      return new String[] {
          (String)object
      };
    }
    else if (object instanceof String[]) {
      return (String[])object;
    }
    return null; //NOPMD
  }

  /**
   * @param parameterMap Map of parameters
   */
  public void setParameterMap(final Map<String, Object> parameterMap) {
    this.parameterMap.clear();
    for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof String[]) {
        this.parameterMap.put(key, (String[])value);
      }
      else if (value != null) {
        this.parameterMap.put(key, new String[] {
            value.toString()
        });
      }
      else {
        this.parameterMap.put(key, null);
      }
    }
    try {
      this.queryString = formatQueryString(this.parameterMap);
    }
    catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex);
    }
  }

  private String formatQueryString(final Map<String, String[]> map) throws UnsupportedEncodingException {
    StringBuilder querystring = new StringBuilder();
    for (Map.Entry<String, String[]> entry : this.parameterMap.entrySet()) {
      if (entry.getValue() != null) {
        for (String value : entry.getValue()) {
          if (querystring.length() != 0) {
            querystring.append('&');
          }
          querystring.append(URLEncoder.encode(entry.getKey(), CharEncoding.UTF_8));
          querystring.append('=');
          if (value != null) {
            querystring.append(URLEncoder.encode(value, CharEncoding.UTF_8));
          }
        }
      }
    }
    if (querystring.length() > 0) {
      return querystring.toString();
    }
    else {
      return null;
    }
  }


  @Override
  public Locale getLocale() {
    return Locale.US;
  }

  @Override
  public String getContextPath() {
    return this.contextPath;
  }

  /**
   * @param contextPath Webapp context path
   */
  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  /**
   * @param queryString Query string (with proper URL encoding)
   */
  public void setQueryString(final String queryString) {
    this.queryString = queryString;
    try {
      parseQueryString(this.parameterMap, this.queryString);
    }
    catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void parseQueryString(final Map<String, String[]> map,
      final String query) throws UnsupportedEncodingException {
    final Map<String, List<String>> queryPairs = new LinkedHashMap<>();
    final String[] pairs = query.split("&");
    for (String pair : pairs) {
      final int idx = pair.indexOf('=');
      final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), CharEncoding.UTF_8) : pair;
      if (!queryPairs.containsKey(key)) {
        queryPairs.put(key, new ArrayList<String>());
      }
      final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), CharEncoding.UTF_8) : null;
      queryPairs.get(key).add(value);
    }
    map.clear();
    for (Map.Entry<String, List<String>> entry : queryPairs.entrySet()) {
      map.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
    }
  }

  @Override
  public String getQueryString() {
    return this.queryString;
  }

  @Override
  public String getScheme() {
    return this.scheme;
  }

  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  @Override
  public String getServerName() {
    return this.serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  @Override
  public int getServerPort() {
    return this.serverPort;
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  @Override
  public boolean isSecure() {
    return StringUtils.equals("https", getScheme());
  }

  @Override
  public String getMethod() {
    return this.method;
  }

  public void setMethod(String method) {
    this.method = method;
  }


  // --- unsupported operations ---
  @Override
  public Cookie getCookie(final String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RequestDispatcher getRequestDispatcher(final Resource dispatcherResource) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RequestDispatcher getRequestDispatcher(final String dispatcherPath, final RequestDispatcherOptions options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RequestDispatcher getRequestDispatcher(final Resource dispatcherResource, final RequestDispatcherOptions options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RequestParameter getRequestParameter(final String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RequestParameterMap getRequestParameterMap() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RequestParameter[] getRequestParameters(final String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RequestProgressTracker getRequestProgressTracker() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResourceBundle getResourceBundle(final Locale pLocale) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResourceBundle getResourceBundle(final String baseName, final Locale locale) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getResponseContentType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Enumeration<String> getResponseContentTypes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getAuthType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Cookie[] getCookies() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getDateHeader(final String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getHeader(final String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Enumeration<String> getHeaders(final String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getIntHeader(final String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPathInfo() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPathTranslated() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getRemoteUser() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getRequestURI() {
    throw new UnsupportedOperationException();
  }

  @Override
  public StringBuffer getRequestURL() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getRequestedSessionId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getServletPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Principal getUserPrincipal() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isRequestedSessionIdFromUrl() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isUserInRole(final String role) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getCharacterEncoding() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getContentLength() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getContentType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ServletInputStream getInputStream() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getLocalAddr() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getLocalName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getLocalPort() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Enumeration<Locale> getLocales() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getProtocol() {
    throw new UnsupportedOperationException();
  }

  @Override
  public BufferedReader getReader() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getRealPath(final String path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getRemoteAddr() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getRemoteHost() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getRemotePort() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RequestDispatcher getRequestDispatcher(final String path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setCharacterEncoding(final String env) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean authenticate(final HttpServletResponse response) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void login(final String pUsername, final String password) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void logout() throws ServletException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Part> getParts() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Part getPart(final String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ServletContext getServletContext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public AsyncContext startAsync() {
    throw new UnsupportedOperationException();
  }

  @Override
  public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAsyncStarted() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAsyncSupported() {
    throw new UnsupportedOperationException();
  }

  @Override
  public AsyncContext getAsyncContext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public DispatcherType getDispatcherType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<RequestParameter> getRequestParameterList() {
    throw new UnsupportedOperationException();
  }

}
