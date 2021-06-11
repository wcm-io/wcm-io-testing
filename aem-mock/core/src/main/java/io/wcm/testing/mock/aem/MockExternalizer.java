/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.testing.mock.aem;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import com.day.cq.commons.Externalizer;

/**
 * Mock implementation of {@link Externalizer}.
 */
@Component(service = Externalizer.class)
public final class MockExternalizer implements Externalizer {

  private static final String DEFAULT_URI_AUTHOR = "http://localhost:4502";
  private static final String DEFAULT_URI_PUBLISH = "http://localhost:4503";
  private static final String SCHEME_SEPARATOR = "://";

  private final Map<String, URI> domainMappings = new HashMap<>();

  /**
   * Constructor
   */
  public MockExternalizer() {
    try {
      // default mappings
      domainMappings.put(LOCAL, new URI(DEFAULT_URI_AUTHOR));
      domainMappings.put(AUTHOR, new URI(DEFAULT_URI_AUTHOR));
      domainMappings.put(PUBLISH, new URI(DEFAULT_URI_PUBLISH));
    }
    catch (URISyntaxException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  /**
   * Allows to set a custom domain mapping for the unit test.
   * @param domain Domain
   * @param domainURI Domain URI
   */
  public void setMapping(String domain, String domainURI) {
    try {
      domainMappings.put(domain, new URI(domainURI));
    }
    catch (URISyntaxException ex) {
      throw new IllegalArgumentException("Invalid URI: " + domainURI, ex);
    }
  }

  @Override
  public String externalLink(ResourceResolver resourceResolver, String domain, String path) {
    return externalLink(resourceResolver, domain, null, path);
  }

  @Override
  public String externalLink(ResourceResolver resourceResolver, String domain, String scheme, String path) {
    return buildExternalLink(domain, scheme, path);
  }

  @Override
  public String authorLink(ResourceResolver resourceResolver, String path) {
    return externalLink(resourceResolver, AUTHOR, null, path);
  }

  @Override
  public String authorLink(ResourceResolver resourceResolver, String scheme, String path) {
    return externalLink(resourceResolver, AUTHOR, scheme, path);
  }

  @Override
  public String publishLink(ResourceResolver resourceResolver, String path) {
    return externalLink(resourceResolver, PUBLISH, null, path);
  }

  @Override
  public String publishLink(ResourceResolver resourceResolver, String scheme, String path) {
    return externalLink(resourceResolver, PUBLISH, scheme, path);
  }

  @Override
  public String absoluteLink(ResourceResolver resourceResolver, String scheme, String path) {
    return externalLink(resourceResolver, LOCAL, scheme, path);
  }

  @Override
  public String absoluteLink(SlingHttpServletRequest request, String scheme, String path) {
    return buildAbsoluteLink(request, scheme, path);
  }

  @Override
  public String absoluteLink(String scheme, String path) {
    return externalLink(null, LOCAL, scheme, path);
  }

  @Override
  public String relativeLink(SlingHttpServletRequest request, String path) {
    return path;
  }

  private String buildExternalLink(String domain, String scheme, String path) {
    URI domainURI = domainMappings.get(Objects.requireNonNull(domain));
    if (domainURI == null) {
      throw new IllegalArgumentException("No mapping defined for: " + domain);
    }

    StringBuilder url = new StringBuilder();

    // scheme, host name, port
    url.append(StringUtils.defaultString(scheme, StringUtils.defaultString(domainURI.getScheme(), "http")))
        .append(SCHEME_SEPARATOR)
        .append(getHost(scheme, domainURI.getHost(), domainURI.getPort()));

    // context path
    if (domainURI.getRawPath() != null) {
      url.append(domainURI.getRawPath());
    }

    // path
    url.append(path);

    return url.toString();
  }

  private String buildAbsoluteLink(SlingHttpServletRequest request, String scheme, String path) {

    // return path unchanged if it is already absolute (or if no request available)
    if (StringUtils.contains(path, SCHEME_SEPARATOR)) {
      return path;
    }

    StringBuilder url = new StringBuilder();
    url.append(scheme).append(SCHEME_SEPARATOR)
        .append(getHost(scheme, request.getServerName(), request.getServerPort()))
        .append(request.getContextPath())
        .append(path);
    return url.toString();
  }

  private static String getHost(String scheme, String host, int port) {
    if (port < 0
        || (StringUtils.equals(scheme, "http") && port == 80)
        || (StringUtils.equals(scheme, "https") && port == 443)) {
      return host;
    }
    else {
      return host + ":" + port;
    }
  }

}
