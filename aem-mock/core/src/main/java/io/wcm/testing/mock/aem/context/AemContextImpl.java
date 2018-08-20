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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.context.SlingContextImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.dam.api.AssetManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;
import com.google.common.collect.ImmutableSet;

import io.wcm.testing.mock.aem.MockAemAdapterFactory;
import io.wcm.testing.mock.aem.MockComponentContext;
import io.wcm.testing.mock.aem.MockLayerAdapterFactory;
import io.wcm.testing.mock.aem.MockAemBindingsValuesProvider;
import io.wcm.testing.mock.aem.builder.ContentBuilder;

/**
 * Defines AEM context objects with lazy initialization.
 * Should not be used directly but via the JUnit 4 rule or JUnit 5 extension.
 */
@ConsumerType
@SuppressWarnings("null")
public class AemContextImpl extends SlingContextImpl {

  // default to publish instance run mode
  static final Set<String> DEFAULT_RUN_MODES = ImmutableSet.<String>builder().add("publish").build();

  @Override
  protected void registerDefaultServices() {
    // register default services from osgi-mock and sling-mock
    super.registerDefaultServices();

    // adapter factories
    registerInjectActivateService(new MockAemAdapterFactory());
    registerInjectActivateService(new MockLayerAdapterFactory());

    // bindings value providerrs
    registerInjectActivateService(new MockAemBindingsValuesProvider());
  }

  @Override
  protected void setResourceResolverType(@Nullable ResourceResolverType resourceResolverType) {
    super.setResourceResolverType(resourceResolverType);
  }

  @Override
  protected @NotNull ResourceResolverFactory newResourceResolverFactory() {
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
   * Merges the given custom Resource Resolver Factory Activator OSGi configuration with the default configuration
   * applied in AEM 6. The custom configuration has higher precedence.
   * @param customProps Custom config
   * @return Merged config
   */
  protected final Map<String, Object> resourceResolverFactoryActivatorPropsMergeWithAemDefault(@Nullable Map<String, Object> customProps) {
    Map<String, Object> props = new HashMap<>();

    props.put("resource.resolver.searchpath", new String[] {
        "/apps",
        "/libs",
        "/apps/foundation/components/primary",
        "/libs/foundation/components/primary",
    });
    props.put("resource.resolver.manglenamespaces", true);
    props.put("resource.resolver.allowDirect", true);
    props.put("resource.resolver.virtual", new String[] {
        "/:/"
    });
    props.put("resource.resolver.mapping", new String[] {
        "/-/"
    });
    props.put("resource.resolver.map.location", "/etc/map");
    props.put("resource.resolver.default.vanity.redirect.status", "");
    props.put("resource.resolver.virtual", "302");
    props.put("resource.resolver.enable.vanitypath", false);
    props.put("resource.resolver.vanitypath.maxEntries", -1);
    props.put("resource.resolver.vanitypath.bloomfilter.maxBytes", 1024000);
    props.put("resource.resolver.optimize.alias.resolution", true);
    props.put("resource.resolver.vanitypath.whitelist", new String[] {
        "/apps/",
        "/libs/",
        "/content/"
    });
    props.put("resource.resolver.vanitypath.blacklist", new String[] {
        "/content/usergenerated"
    });
    props.put("resource.resolver.vanity.precedence", false);
    props.put("resource.resolver.providerhandling.paranoid", false);

    if (customProps != null) {
      props.putAll(customProps);
    }

    return props;
  }

  /**
   * @return Page manager
   */
  public @NotNull PageManager pageManager() {
    return resourceResolver().adaptTo(PageManager.class);
  }

  /**
   * @return Asset manager
   */
  public @NotNull AssetManager assetManager() {
    return resourceResolver().adaptTo(AssetManager.class);
  }

  /**
   * @return Content builder for building test content
   */
  @Override
  public @NotNull ContentBuilder create() {
    if (this.contentBuilder == null) {
      this.contentBuilder = new ContentBuilder(resourceResolver());
    }
    return (ContentBuilder)this.contentBuilder;
  }

  /**
   * @return Current page from {@link ComponentContext}. If none is set the page containing the current resource.
   *         Null if no containing page exists.
   */
  public @Nullable Page currentPage() {
    ComponentContext context = WCMUtils.getComponentContext(request());
    if (context != null) {
      return context.getPage();
    }
    if (currentResource() != null) {
      return pageManager().getContainingPage(currentResource());
    }
    return null;
  }

  /**
   * Set current Page in request (via {@link ComponentContext}).
   * This also sets the current resource to the content resource of the page.
   * You can set it to a different resources afterwards if required.
   * @param pagePath Page path
   * @return currentPage
   */
  public @Nullable Page currentPage(@Nullable String pagePath) {
    if (pagePath != null) {
      Page page = pageManager().getPage(pagePath);
      if (page == null) {
        throw new IllegalArgumentException("Page does not exist: " + pagePath);
      }
      return currentPage(page);
    }
    else {
      currentPage((Page)null);
      return null;
    }
  }

  /**
   * Set current Page in request (via {@link ComponentContext}).
   * This also sets the current resource to the content resource of the page.
   * You can set it to a different resources afterwards if required.
   * @param page Page
   * @return currentPage
   */
  public @Nullable Page currentPage(@Nullable Page page) {
    if (page != null) {
      ComponentContext wcmComponentContext = new MockComponentContext(page, request());
      request.setAttribute(ComponentContext.CONTEXT_ATTR_NAME, wcmComponentContext);
      currentResource(page.getContentResource());
      return page;
    }
    else {
      request.setAttribute(ComponentContext.CONTEXT_ATTR_NAME, null);
      currentResource((Resource)null);
      return null;
    }
  }

  /**
   * Create unique root paths for unit tests (and clean them up after the test run automatically).
   * @return Unique root path helper
   */
  @Override
  public @NotNull UniqueRoot uniqueRoot() {
    if (uniqueRoot == null) {
      uniqueRoot = new UniqueRoot(this);
    }
    return (UniqueRoot)uniqueRoot;
  }

}
