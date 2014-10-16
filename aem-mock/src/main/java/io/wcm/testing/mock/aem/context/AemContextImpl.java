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
import io.wcm.testing.mock.aem.builder.ContentBuilder;

import java.util.Set;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.spi.Injector;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.context.SlingContextImpl;
import org.osgi.annotation.versioning.ConsumerType;

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

  @Override
  protected void registerDefaultServices() {
    super.registerDefaultServices();

    // adapter factories
    registerService(AdapterFactory.class, new MockAemAdapterFactory());
    registerService(AdapterFactory.class, new MockLayerAdapterFactory());

    // sling models injectors
    registerService(Injector.class, new AemObjectInjector());
  }

  @Override
  protected void setResourceResolverType(ResourceResolverType resourceResolverType) {
    super.setResourceResolverType(resourceResolverType);
  }

  @Override
  protected ResourceResolverFactory newResourceResolverFactory() {
    return ContextResourceResolverFactory.get(this.resourceResolverType);
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

}
