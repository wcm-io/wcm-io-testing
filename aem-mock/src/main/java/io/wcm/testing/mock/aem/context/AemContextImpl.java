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
package io.wcm.testing.mock.aem.context;

import io.wcm.sling.models.injectors.impl.AemObjectInjector;
import io.wcm.testing.mock.aem.MockAemAdapterFactory;
import io.wcm.testing.mock.aem.MockLayerAdapterFactory;
import io.wcm.testing.mock.osgi.MockOsgi;
import io.wcm.testing.mock.sling.MockSling;
import io.wcm.testing.mock.sling.ResourceResolverType;
import io.wcm.testing.mock.sling.loader.ContentLoader;
import io.wcm.testing.mock.sling.services.MockMimeTypeService;
import io.wcm.testing.mock.sling.services.MockModelAdapterFactory;
import io.wcm.testing.mock.sling.services.MockSlingSettingService;
import io.wcm.testing.mock.sling.servlet.MockRequestPathInfo;
import io.wcm.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import io.wcm.testing.mock.sling.servlet.MockSlingHttpServletResponse;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.models.impl.FirstImplementationPicker;
import org.apache.sling.models.impl.injectors.BindingsInjector;
import org.apache.sling.models.impl.injectors.ChildResourceInjector;
import org.apache.sling.models.impl.injectors.OSGiServiceInjector;
import org.apache.sling.models.impl.injectors.RequestAttributeInjector;
import org.apache.sling.models.impl.injectors.ResourcePathInjector;
import org.apache.sling.models.impl.injectors.ResourceResolverInjector;
import org.apache.sling.models.impl.injectors.SelfInjector;
import org.apache.sling.models.impl.injectors.SlingObjectInjector;
import org.apache.sling.models.impl.injectors.ValueMapInjector;
import org.apache.sling.models.spi.ImplementationPicker;
import org.apache.sling.models.spi.Injector;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Defines AEM context objects with lazy initialization.
 * Should not be used directly but via the {@link io.wcm.testing.mock.aem.junit.AemContext} JUnit rule.
 */
public class AemContextImpl<WrapperType> {

  // default to publish instance run mode
  static final Set<String> DEFAULT_RUN_MODES = ImmutableSet.<String>builder().add("publish").build();

  private MockModelAdapterFactory modelAdapterFactory;
  private ResourceResolverType resourceResolverType;
  private ComponentContext componentContext;
  private ResourceResolver resourceResolver;
  private MockSlingHttpServletRequest request;
  private MockSlingHttpServletResponse response;
  private SlingScriptHelper slingScriptHelper;
  private ContentLoader contentLoader;
  private ContentBuilder contentBuilder;

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
    MockSling.setAdapterManagerBundleContext(bundleContext());
    registerDefaultServices();
  }

  /**
   * Default services that should be available for every unit test
   */
  private void registerDefaultServices() {

    // adapter factories
    registerService(AdapterFactory.class, new MockAemAdapterFactory());
    registerService(AdapterFactory.class, new MockLayerAdapterFactory());
    modelAdapterFactory = new MockModelAdapterFactory(componentContext());
    registerService(AdapterFactory.class, modelAdapterFactory);

    // sling models injectors
    registerService(Injector.class, new BindingsInjector());
    registerService(Injector.class, new ChildResourceInjector());
    OSGiServiceInjector osgiServiceInjector = new OSGiServiceInjector();
    osgiServiceInjector.activate(componentContext());
    registerService(Injector.class, osgiServiceInjector);
    registerService(Injector.class, new RequestAttributeInjector());
    registerService(Injector.class, new ResourcePathInjector());
    registerService(Injector.class, new ResourceResolverInjector());
    registerService(Injector.class, new SelfInjector());
    registerService(Injector.class, new SlingObjectInjector());
    registerService(Injector.class, new ValueMapInjector());
    registerService(Injector.class, new AemObjectInjector());

    // sling models implementation pickers
    registerService(ImplementationPicker.class, new FirstImplementationPicker());

    // other services
    registerService(SlingSettingsService.class, new MockSlingSettingService(DEFAULT_RUN_MODES));
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

    this.modelAdapterFactory = null;
    this.componentContext = null;
    this.resourceResolver = null;
    this.request = null;
    this.response = null;
    this.slingScriptHelper = null;
    this.contentLoader = null;
    this.contentBuilder = null;

    MockSling.clearAdapterManagerBundleContext();
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
      this.componentContext = MockOsgi.newComponentContext();
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
  public MockSlingHttpServletRequest request() {
    if (this.request == null) {
      this.request = new MockSlingHttpServletRequest(this.resourceResolver());

      // initialize sling bindings
      SlingBindings bindings = new SlingBindings();
      bindings.put(SlingBindings.REQUEST, this.request);
      bindings.put(SlingBindings.RESPONSE, response());
      bindings.put(SlingBindings.SLING, slingScriptHelper());
      this.request.setAttribute(SlingBindings.class.getName(), bindings);
    }
    return this.request;
  }

  /**
   * @return Request path info
   */
  public MockRequestPathInfo requestPathInfo() {
    return (MockRequestPathInfo)request().getRequestPathInfo();
  }

  /**
   * @return Sling response
   */
  public MockSlingHttpServletResponse response() {
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
      this.slingScriptHelper = MockSling.newSlingScriptHelper(this.request(), this.response(), this.bundleContext());
    }
    return this.slingScriptHelper;
  }

  /**
   * @return Page manager
   */
  public PageManager pageManager() {
    return resourceResolver().adaptTo(PageManager.class);
  }

  /**
   * @return Content loader
   */
  public ContentLoader contentLoader() {
    if (this.contentLoader == null) {
      this.contentLoader = new ContentLoader(resourceResolver());
    }
    return this.contentLoader;
  }

  /**
   * @return Content builder for building test content
   */
  public ContentBuilder create() {
    if (this.contentBuilder == null) {
      this.contentBuilder = new ContentBuilder(this);
    }
    return this.contentBuilder;
  }

  /**
   * Registers a service in the mocked OSGi environment.
   * @param service Service instance
   * @return this
   */
  @SuppressWarnings("unchecked")
  public <T> WrapperType registerService(final T service) {
    registerService(null, service, null);
    return (WrapperType)this;
  }

  /**
   * Registers a service in the mocked OSGi environment.
   * @param serviceClass Service class
   * @param service Service instance
   * @return this
   */
  @SuppressWarnings("unchecked")
  public <T> WrapperType registerService(final Class<T> serviceClass, final T service) {
    registerService(serviceClass, service, null);
    return (WrapperType)this;
  }

  /**
   * Registers a service in the mocked OSGi environment.
   * @param serviceClass Service class
   * @param service Service instance
   * @param properties Service properties (optional)
   * @return this
   */
  @SuppressWarnings("unchecked")
  public <T> WrapperType registerService(final Class<T> serviceClass, final T service, final Map<String, Object> properties) {
    Dictionary<String, Object> serviceProperties = null;
    if (properties != null) {
      serviceProperties = new Hashtable<>(properties);
    }
    bundleContext().registerService(serviceClass != null ? serviceClass.getName() : null, service, serviceProperties);
    return (WrapperType)this;
  }

  /**
   * Injects dependencies, activates and registers a service in the mocked OSGi environment.
   * @param service Service instance
   * @return this
   */
  public <T> WrapperType registerInjectActivateService(final T service) {
    return registerInjectActivateService(service, ImmutableMap.<String, Object>of());
  }

  /**
   * Injects dependencies, activates and registers a service in the mocked OSGi environment.
   * @param service Service instance
   * @param properties Service properties (optional)
   * @return this
   */
  @SuppressWarnings("unchecked")
  public <T> WrapperType registerInjectActivateService(final T service, final Map<String, Object> properties) {
    MockOsgi.injectServices(service, bundleContext());
    MockOsgi.activate(service, bundleContext(), properties);
    registerService(null, service, null);
    return (WrapperType)this;
  }


  /**
   * Set current resource in request.
   * @param resourcePath Resource path
   * @return this
   */
  public WrapperType currentResource(String resourcePath) {
    if (resourcePath != null) {
      Resource resource = resourceResolver().getResource(resourcePath);
      if (resource == null) {
        throw new IllegalArgumentException("Resource does not exist: " + resourcePath);
      }
      return currentResource(resource);
    }
    else {
      return currentResource((Resource)null);
    }
  }

  /**
   * Set current resource in request.
   * @param resource Resource
   * @return this
   */
  @SuppressWarnings("unchecked")
  public WrapperType currentResource(Resource resource) {
    request().setResource(resource);
    return (WrapperType)this;
  }

  /**
   * Set current Page in request (set to content resource of page).
   * @param pagePath Page path
   * @return this
   */
  public WrapperType currentPage(String pagePath) {
    if (pagePath != null) {
      Page page = pageManager().getPage(pagePath);
      if (page == null) {
        throw new IllegalArgumentException("Page does not exist: " + pagePath);
      }
      return currentPage(page);
    }
    else {
      return currentResource((Resource)null);
    }
  }

  /**
   * Set current Page in request (set to content resource of page).
   * @param page Page
   * @return this
   */
  public WrapperType currentPage(Page page) {
    return currentResource(page != null ? page.getContentResource() : null);
  }

  /**
   * Scan classpaths for given package name (and sub packages) to scan for and register all classes
   * with @Model annotation.
   * @param packageName Java package name
   * @return this
   */
  @SuppressWarnings("unchecked")
  public WrapperType addModelsForPackage(String packageName) {
    this.modelAdapterFactory.addModelsForPackage(packageName);
    return (WrapperType)this;
  }

  /**
   * Set current run mode(s).
   * @param runModes Run mode(s).
   * @return this
   */
  @SuppressWarnings("unchecked")
  public WrapperType runMode(String... runModes) {
    Set<String> newRunModes = ImmutableSet.<String>builder().add(runModes).build();
    ServiceReference ref = bundleContext().getServiceReference(SlingSettingsService.class.getName());
    if (ref != null) {
      MockSlingSettingService slingSettings = (MockSlingSettingService)bundleContext().getService(ref);
      slingSettings.setRunModes(newRunModes);
    }
    return (WrapperType)this;
  }

}
