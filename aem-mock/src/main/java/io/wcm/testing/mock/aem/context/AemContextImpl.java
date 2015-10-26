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

import io.wcm.testing.mock.aem.MockAemAdapterFactory;
import io.wcm.testing.mock.aem.MockLayerAdapterFactory;
import io.wcm.testing.mock.aem.builder.ContentBuilder;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.context.SlingContextImpl;
import org.osgi.annotation.versioning.ConsumerType;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.ImmutableSet;

/**
 * Defines AEM context objects with lazy initialization.
 * Should not be used directly but via the {@link io.wcm.testing.mock.aem.junit.AemContext} JUnit rule.
 */
@ConsumerType
public class AemContextImpl extends SlingContextImpl {

  // default to publish instance run mode
  static final Set<String> DEFAULT_RUN_MODES = ImmutableSet.<String>builder().add("publish").build();

  private static final String RESOURCERESOLVERFACTORYACTIVATOR_PID = "org.apache.sling.jcr.resource.internal.JcrResourceResolverFactoryImpl";

  @Override
  protected void registerDefaultServices() {

    // prepare customized configuration for ResourceResolverFactoryActivator with the default values from AEM
    // AEM uses different default properties than the ResourceResolverFactoryActivator used by default
    ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class);
    try {
      Configuration resourceResolverFactoryActivatorConfig = configAdmin.getConfiguration(RESOURCERESOLVERFACTORYACTIVATOR_PID);
      Dictionary<String, Object> defaultProps = new Hashtable<>();
      defaultProps.put("resource.resolver.searchpath", new String[] {
          "/apps",
          "/libs",
          "/apps/foundation/components/primary",
          "/libs/foundation/components/primary",
      });
      defaultProps.put("resource.resolver.manglenamespaces", true);
      defaultProps.put("resource.resolver.allowDirect", true);
      defaultProps.put("resource.resolver.virtual", "/:/");
      defaultProps.put("resource.resolver.mapping", "/-/");
      defaultProps.put("resource.resolver.map.location", "/etc/map");
      defaultProps.put("resource.resolver.default.vanity.redirect.status", "");
      defaultProps.put("resource.resolver.virtual", "302");
      defaultProps.put("resource.resolver.enable.vanitypath", true);
      defaultProps.put("resource.resolver.vanitypath.maxEntries", -1);
      defaultProps.put("resource.resolver.vanitypath.bloomfilter.maxBytes", 1024000);
      defaultProps.put("resource.resolver.optimize.alias.resolution", true);
      defaultProps.put("resource.resolver.vanitypath.whitelist", new String[] {
          "/apps/",
          "/libs/",
          "/content/"
      });
      defaultProps.put("resource.resolver.vanitypath.blacklist", new String[] {
          "/content/usergenerated"
      });
      defaultProps.put("resource.resolver.vanity.precedence", false);
      defaultProps.put("resource.resolver.providerhandling.paranoid", false);
      resourceResolverFactoryActivatorConfig.update(defaultProps);
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    // register default services from osgi-mock and sling-mock
    super.registerDefaultServices();

    // adapter factories
    registerInjectActivateService(new MockAemAdapterFactory());
    registerInjectActivateService(new MockLayerAdapterFactory());
  }

  @Override
  protected void setResourceResolverType(ResourceResolverType resourceResolverType) {
    super.setResourceResolverType(resourceResolverType);
  }

  @Override
  protected ResourceResolverFactory newResourceResolverFactory() {
    return ContextResourceResolverFactory.get(this.resourceResolverType, bundleContext());
  }

  @Override
  protected void setUp() {
    super.setUp();
  }

  @Override
  protected void tearDown() {
    super.tearDown();
  }

  /**
   * @return Page manager
   */
  public PageManager pageManager() {
    return resourceResolver().adaptTo(PageManager.class);
  }

  /**
   * @return Content builder for building test content
   */
  @Override
  public ContentBuilder create() {
    if (this.contentBuilder == null) {
      this.contentBuilder = new ContentBuilder(resourceResolver());
    }
    return (ContentBuilder)this.contentBuilder;
  }

  /**
   * @return Current page
   */
  public Page currentPage() {
    if (currentResource() != null) {
      return pageManager().getContainingPage(currentResource());
    }
    return null;
  }

  /**
   * Set current Page in request (set to content resource of page).
   * @param pagePath Page path
   * @return currentPage
   */
  public Page currentPage(String pagePath) {
    if (pagePath != null) {
      Page page = pageManager().getPage(pagePath);
      if (page == null) {
        throw new IllegalArgumentException("Page does not exist: " + pagePath);
      }
      return currentPage(page);
    }
    else {
      currentResource((Resource)null);
      return null;
    }
  }

  /**
   * Set current Page in request (set to content resource of page).
   * @param page Page
   * @return currentPage
   */
  public Page currentPage(Page page) {
    if (page != null) {
      currentResource(page.getContentResource());
      return page;
    }
    else {
      currentResource((Resource)null);
      return null;
    }
  }

  /**
   * Create unique root paths for unit tests (and clean them up after the test run automatically).
   * @return Unique root path helper
   */
  @Override
  public UniqueRoot uniqueRoot() {
    if (uniqueRoot == null) {
      uniqueRoot = new UniqueRoot(this);
    }
    return (UniqueRoot)uniqueRoot;
  }

}
