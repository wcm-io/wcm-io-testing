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
package io.wcm.testing.mock.sling;

import io.wcm.testing.mock.osgi.MockOsgiFactory;
import io.wcm.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import io.wcm.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import io.wcm.testing.mock.sling.spi.ResourceResolverTypeAdapter;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.BundleContext;

/**
 * Factory for mock Sling objects.
 */
public final class MockSlingFactory {

  /**
   * Default resource resolver type is {@link ResourceResolverType#JCR_MOCK}.
   */
  public static final ResourceResolverType DEFAULT_RESOURCERESOLVER_TYPE = ResourceResolverType.JCR_MOCK;

  private static final ThreadsafeMockAdapterManagerWrapper ADAPTER_MANAGER = new ThreadsafeMockAdapterManagerWrapper();
  static {
    // register mocked adapter manager
    SlingAdaptable.setAdapterManager(ADAPTER_MANAGER);
  }

  private MockSlingFactory() {
    // static methods only
  }

  /**
   * Creates new sling resource resolver factory instance.
   * @param type Type of underlying repository.
   * @return Resource resolver factory instance
   */
  public static ResourceResolverFactory newResourceResolverFactory(final ResourceResolverType type) {
    ResourceResolverTypeAdapter adapter = getResourceResolverTypeAdapter(type);
    ResourceResolverFactory factory = adapter.newResourceResolverFactory();
    if (factory == null) {
      SlingRepository repository = adapter.newSlingRepository();
      if (repository == null) {
        throw new RuntimeException("Adapter neither provides resource resolver factory nor sling repository.");
      }
      factory = new MockJcrResourceResolverFactory(repository);
    }
    return factory;
  }

  private static ResourceResolverTypeAdapter getResourceResolverTypeAdapter(final ResourceResolverType type) {
    try {
      Class clazz = Class.forName(type.getResourceResolverTypeAdapterClass());
      return (ResourceResolverTypeAdapter)clazz.newInstance();
    }
    catch (ClassNotFoundException|InstantiationException|IllegalAccessException ex) {
      throw new RuntimeException("Unable to instantiate resourcer resolver: "
          + type.getResourceResolverTypeAdapterClass()
          + (type.getArtifactCoordinates() != null ?
              "Make sure this maven dependency is included: " + type.getArtifactCoordinates() : ""));
    }
  }

  /**
   * Creates new sling resource resolver factory instance using {@link #DEFAULT_RESOURCERESOLVER_TYPE}.
   * @return Resource resolver factory instance
   */
  public static ResourceResolverFactory newResourceResolverFactory() {
    return newResourceResolverFactory(DEFAULT_RESOURCERESOLVER_TYPE);
  }

  /**
   * Creates new sling resource resolver instance.
   * @param type Type of underlying repository.
   * @return Resource resolver instance
   */
  public static ResourceResolver newResourceResolver(final ResourceResolverType type) {
    ResourceResolverFactory factory = newResourceResolverFactory(type);
    try {
      return factory.getResourceResolver(null);
    }
    catch (LoginException ex) {
      throw new RuntimeException("Mock resource resolver factory implementation seems to require login.", ex);
    }
  }

  /**
   * Creates new sling resource resolver instance using {@link #DEFAULT_RESOURCERESOLVER_TYPE}.
   * @return Resource resolver instance
   */
  public static ResourceResolver newResourceResolver() {
    return newResourceResolver(DEFAULT_RESOURCERESOLVER_TYPE);
  }

  /**
   * Creates a new sling script helper instance.
   * @param request Request
   * @param response Response
   * @param bundleContext Bundle context
   * @return Sling script helper instance
   */
  public static SlingScriptHelper newSlingScriptHelper(final SlingHttpServletRequest request,
      final SlingHttpServletResponse response, final BundleContext bundleContext) {
    return new MockSlingScriptHelper(request, response, bundleContext);
  }

  /**
   * Creates a new sling script helper instance using {@link #DEFAULT_RESOURCERESOLVER_TYPE} for the resource resolver.
   * @return Sling script helper instance
   */
  public static SlingScriptHelper newSlingScriptHelper() {
    SlingHttpServletRequest request = new MockSlingHttpServletRequest(newResourceResolver());
    SlingHttpServletResponse response = new MockSlingHttpServletResponse();
    BundleContext bundleContext = MockOsgiFactory.newBundleContext();
    return newSlingScriptHelper(request, response, bundleContext);
  }

  /**
   * Register adapter factory
   * @param adapterFactory Adapter factory
   */
  public static void registerAdapterFactory(final AdapterFactory adapterFactory) {
    ADAPTER_MANAGER.register(adapterFactory);
  }

  /**
   * Clear adapter registrations..
   */
  public static void clearAdapterRegistrations() {
    ADAPTER_MANAGER.clearRegistrations();
  }

}
