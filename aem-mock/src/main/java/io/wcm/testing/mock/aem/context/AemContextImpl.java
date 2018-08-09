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

import java.util.Set;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.context.SlingContextImpl;
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
import io.wcm.testing.mock.aem.builder.ContentBuilder;

/**
 * Defines AEM context objects with lazy initialization.
 * Should not be used directly but via the {@link io.wcm.testing.mock.aem.junit.AemContext} JUnit rule.
 */
@ConsumerType
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
   * @return Asset manager
   */
  public AssetManager assetManager() {
    return resourceResolver().adaptTo(AssetManager.class);
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
   * @return Current page from {@link ComponentContext}. If none is set the page containing the current resource.
   *         Null if no containing page exists.
   */
  public Page currentPage() {
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
  public Page currentPage(String pagePath) {
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
  public Page currentPage(Page page) {
    if (page != null) {
      ComponentContext wcmComponentContext = new MockComponentContext(page, request());
      request.setAttribute(ComponentContext.CONTEXT_ATTR_NAME, wcmComponentContext);
      addToSlingBindings( "currentPage", currentPage());
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
   * Add a key, value pair to request (via {@link SlingBindings})
   * so @ScriptVariable annotation will be injected in @Model
   * @param key String
   * @param value Object
   */
  private void addToSlingBindings(String key, Object value) {
    SlingBindings slingBindings = (SlingBindings) request.getAttribute(SlingBindings.class.getName());
    if(slingBindings == null){
      slingBindings = new SlingBindings();
      request.setAttribute(SlingBindings.class.getName(), slingBindings);
    }
    slingBindings.put(key, value);
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
