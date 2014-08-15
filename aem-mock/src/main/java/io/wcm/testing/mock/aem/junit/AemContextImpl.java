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
package io.wcm.testing.mock.aem.junit;

import io.wcm.testing.mock.aem.MockAemAdapterFactory;
import io.wcm.testing.mock.osgi.MockOsgiFactory;
import io.wcm.testing.mock.sling.MockMimeTypeService;
import io.wcm.testing.mock.sling.MockSlingFactory;
import io.wcm.testing.mock.sling.ResourceResolverType;
import io.wcm.testing.mock.sling.contentimport.JsonImporter;
import io.wcm.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import io.wcm.testing.mock.sling.servlet.MockSlingHttpServletResponse;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * Defines AEM context objects with lazy initialization.
 */
class AemContextImpl {

  private ResourceResolverType resourceResolverType;
  private ComponentContext componentContext;
  private ResourceResolver resourceResolver;
  private SlingHttpServletRequest request;
  private SlingHttpServletResponse response;
  private SlingScriptHelper slingScriptHelper;
  private JsonImporter jsonImporter;

  /**
   * @param resourceResolverType Resource resolver type
   */
  protected void setResourceResolverType(final ResourceResolverType resourceResolverType) {
    this.resourceResolverType = resourceResolverType;
  }

  /**
   * Setup actions before test method execution
   */
  protected void setUp() {
    registerDefaultAdapterFactories();
    registerDefaultServices();
  }

  /**
   * Default adapter factories that should be available for every unit test
   */
  private void registerDefaultAdapterFactories() {
    registerAdapterFactory(new MockAemAdapterFactory());
  }

  /**
   * Default services that should be available for every unit test
   */
  private void registerDefaultServices() {
    registerService(MimeTypeService.class, new MockMimeTypeService());
  }

  /**
   * Teardown actions after test method execution
   */
  protected void tearDown() {

    if (this.resourceResolver != null) {
      // revert potential unsaved changes in resource resolver/JCR session
      this.resourceResolver.revert();
      Session session = this.resourceResolver.adaptTo(Session.class);
      if (session != null) {
        try {
          session.refresh(false);
        }
        catch (RepositoryException ex) {
          // ignore
        }
      }
    }

    this.componentContext = null;
    this.resourceResolver = null;
    this.request = null;
    this.response = null;
    this.slingScriptHelper = null;

    MockSlingFactory.clearAdapterRegistrations();
  }

  /**
   * @return Resource resolver type
   */
  public ResourceResolverType resourceResolverType() {
    return this.resourceResolverType;
  }

  /**
   * @return OSGi component context
   */
  public ComponentContext componentContext() {
    if (this.componentContext == null) {
      this.componentContext = MockOsgiFactory.newComponentContext();
    }
    return this.componentContext;
  }

  /**
   * @return OSGi Bundle context
   */
  public BundleContext bundleContext() {
    return componentContext().getBundleContext();
  }

  /**
   * @return Resource resolver
   */
  public ResourceResolver resourceResolver() {
    if (this.resourceResolver == null) {
      this.resourceResolver = AemResourceResolverFactory.initializeResourceResolver(resourceResolverType());
    }
    return this.resourceResolver;
  }

  /**
   * @return Sling request
   */
  public SlingHttpServletRequest request() {
    if (this.request == null) {
      this.request = new MockSlingHttpServletRequest(this.resourceResolver());
    }
    return this.request;
  }

  /**
   * @return Sling response
   */
  public SlingHttpServletResponse response() {
    if (this.response == null) {
      this.response = new MockSlingHttpServletResponse();
    }
    return this.response;
  }

  /**
   * @return Sling script helper
   */
  public SlingScriptHelper slingScriptHelper() {
    if (this.slingScriptHelper == null) {
      this.slingScriptHelper = MockSlingFactory.newSlingScriptHelper(this.request(), this.response(), this.bundleContext());
    }
    return this.slingScriptHelper;
  }

  /**
   * @return JSON importer
   */
  public JsonImporter jsonImporter() {
    if (this.jsonImporter == null) {
      this.jsonImporter = new JsonImporter(resourceResolver());
    }
    return this.jsonImporter;
  }

  /**
   * Register adapter factory
   * @param adapterFactory Adapter factory
   */
  public void registerAdapterFactory(final AdapterFactory adapterFactory) {
    MockSlingFactory.registerAdapterFactory(adapterFactory);
  }

  /**
   * Registers a service in the mocked OSGi environment.
   * @param serviceClass Service class
   * @param service Service instance
   */
  public <T> void registerService(final Class<T> serviceClass, final T service) {
    registerService(serviceClass, service, null);
  }

  /**
   * Registers a service in the mocked OSGi environment.
   * @param serviceClass Service class
   * @param service Service instance
   * @param properties Service properties (optional)
   */
  public <T> void registerService(final Class<T> serviceClass, final T service, final Map<String, Object> properties) {
    Dictionary<String, Object> serviceProperties = null;
    if (properties != null) {
      serviceProperties = new Hashtable<>(properties);
    }
    bundleContext().registerService(serviceClass.getName(), service, serviceProperties);
  }

}
