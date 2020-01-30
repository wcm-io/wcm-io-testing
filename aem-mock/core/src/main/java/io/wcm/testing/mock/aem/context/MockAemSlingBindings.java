/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.commons.ValueMapWrapper;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.EditContext;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.commons.WCMUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Provides AEM-specific sling script bindings.
 */
final class MockAemSlingBindings {

  enum SlingBindingsProperty {

    COMPONENT_CONTEXT("componentContext"),

    EDIT_CONTEXT("editContext"),

    PROPERTIES("properties"),

    PAGE_MANAGER("pageManager"),

    CURRENT_PAGE("currentPage"),

    RESOURCE_PAGE("resourcePage"),

    PAGE_PROPERTIES("pageProperties"),

    COMPONENT("component"),

    DESIGNER("designer"),

    CURRENT_DESIGN("currentDesign"),

    RESOURCE_DESIGN("resourceDesign"),

    CURRENT_STYLE("currentStyle");

    private final String key;

    SlingBindingsProperty(String key) {
      this.key = key;
    }

    public String key() {
      return this.key;
    }

  }

  private static final String RA_DESIGN_CACHE_PREFIX = MockAemSlingBindings.class.getName() + "_design_";

  private MockAemSlingBindings() {
    // static methods only
  }

  static @Nullable Object resolveSlingBindingProperty(@NotNull AemContextImpl context, @NotNull String property,
      @Nullable SlingHttpServletRequest givenRequest) {
    SlingHttpServletRequest request = givenRequest;
    if (givenRequest == null) {
      request = context.request();
    }

    if (StringUtils.equals(property, SlingBindingsProperty.COMPONENT_CONTEXT.key)) {
      return getWcmComponentContext(request);
    }
    if (StringUtils.equals(property, SlingBindingsProperty.EDIT_CONTEXT.key())) {
      return getEditContext(request);
    }
    if (StringUtils.equals(property, SlingBindingsProperty.PROPERTIES.key())) {
      return getProperties(request);
    }
    if (StringUtils.equals(property, SlingBindingsProperty.PAGE_MANAGER.key())) {
      return context.pageManager();
    }
    if (StringUtils.equals(property, SlingBindingsProperty.CURRENT_PAGE.key())) {
      return getCurrentPage(request);
    }
    if (StringUtils.equals(property, SlingBindingsProperty.RESOURCE_PAGE.key())) {
      return getResourcePage(request, context);
    }
    if (StringUtils.equals(property, SlingBindingsProperty.PAGE_PROPERTIES.key())) {
      return getPageProperties(request);
    }
    if (StringUtils.equals(property, SlingBindingsProperty.COMPONENT.key())) {
      return getComponent(request);
    }
    if (StringUtils.equals(property, SlingBindingsProperty.DESIGNER.key())) {
      return getDesigner(request);
    }
    if (StringUtils.equals(property, SlingBindingsProperty.CURRENT_DESIGN.key())) {
      return getCurrentDesign(request);
    }
    if (StringUtils.equals(property, SlingBindingsProperty.RESOURCE_DESIGN.key())) {
      return getResourceDesign(request, context);
    }
    if (StringUtils.equals(property, SlingBindingsProperty.CURRENT_STYLE.key())) {
      return getStyle(request);
    }

    return null;
  }

  private static ComponentContext getWcmComponentContext(SlingHttpServletRequest request) {
    return WCMUtils.getComponentContext(request);
  }

  private static EditContext getEditContext(SlingHttpServletRequest request) {
    ComponentContext wcmComponentContext = getWcmComponentContext(request);
    if (wcmComponentContext != null) {
      return wcmComponentContext.getEditContext();
    }
    return null;
  }

  private static ValueMap getProperties(SlingHttpServletRequest request) {
    return wrap(ResourceUtil.getValueMap(request.getResource()));
  }

  private static Page getCurrentPage(SlingHttpServletRequest request) {
    ComponentContext wcmComponentContext = getWcmComponentContext(request);
    if (wcmComponentContext != null) {
      return wcmComponentContext.getPage();
    }
    return null;
  }

  @SuppressWarnings({ "null", "unused" })
  @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
  private static Page getResourcePage(SlingHttpServletRequest request, AemContextImpl context) {
    Resource resource = request.getResource();
    if (resource != null) {
      return context.pageManager().getContainingPage(resource);
    }
    return null;
  }

  private static ValueMap getPageProperties(SlingHttpServletRequest request) {
    Page currentPage = getCurrentPage(request);
    if (currentPage != null) {
      return wrap(currentPage.getProperties());
    }
    return null;
  }

  @SuppressWarnings({ "null", "unused" })
  @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
  private static Component getComponent(SlingHttpServletRequest request) {
    Resource resource = request.getResource();
    if (resource != null) {
      return WCMUtils.getComponent(resource);
    }
    return null;
  }

  private static Designer getDesigner(SlingHttpServletRequest request) {
    return request.getResourceResolver().adaptTo(Designer.class);
  }

  private static Design getCurrentDesign(SlingHttpServletRequest request) {
    Page currentPage = getCurrentPage(request);
    return getAndCacheDesign(currentPage, request, getDesigner(request));
  }

  private static Design getResourceDesign(SlingHttpServletRequest request, AemContextImpl context) {
    return getAndCacheDesign(getResourcePage(request, context), request, getDesigner(request));
  }

  private static Style getStyle(SlingHttpServletRequest request) {
    ComponentContext wcmComponentContext = getWcmComponentContext(request);
    Design currentDesign = getCurrentDesign(request);
    if (wcmComponentContext != null && currentDesign != null) {
      return currentDesign.getStyle(wcmComponentContext.getCell());
    }
    return null;
  }

  private static ValueMap wrap(ValueMap props) {
    return new ValueMapWrapper(props) {
      // wrap value map to make sure it is not modified
    };
  }

  private static Design getAndCacheDesign(Page page, SlingHttpServletRequest request, Designer designer) {
    if (page == null || designer == null) {
      return null;
    }
    String cacheKey = RA_DESIGN_CACHE_PREFIX + page.getPath();
    Design design = (Design)request.getAttribute(cacheKey);
    if (design == null) {
      design = designer.getDesign(page);
      request.setAttribute(cacheKey, design);
    }
    return design;
  }

}
