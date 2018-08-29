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

/**
 * Provides AEM-specific sling script bindings.
 */
final class MockAemSlingBindings {

  private static final String NAME_COMPONENT_CONTEXT = "componentContext";
  private static final String NAME_EDIT_CONTEXT = "editContext";
  private static final String NAME_PROPERTIES = "properties";
  private static final String NAME_PAGE_MANAGER = "pageManager";
  private static final String NAME_CURRENT_PAGE = "currentPage";
  private static final String NAME_RESOURCE_PAGE = "resourcePage";
  private static final String NAME_PAGE_PROPERTIES = "pageProperties";
  private static final String NAME_COMPONENT = "component";
  private static final String NAME_DESIGNER = "designer";
  private static final String NAME_CURRENT_DESIGN = "currentDesign";
  private static final String NAME_RESOURCE_DESIGN = "resourceDesign";
  private static final String NAME_CURRENT_STYLE = "currentStyle";

  private static final String RA_DESIGN_CACHE_PREFIX = MockAemSlingBindings.class.getName() + "_design_";

  private MockAemSlingBindings() {
    // static methods only
  }

  static @Nullable Object resolveSlingBindingProperty(@NotNull AemContextImpl context, @NotNull String property) {

    if (StringUtils.equals(property, NAME_COMPONENT_CONTEXT)) {
      return getWcmComponentContext(context);
    }
    if (StringUtils.equals(property, NAME_EDIT_CONTEXT)) {
      return getEditContext(context);
    }
    if (StringUtils.equals(property, NAME_PROPERTIES)) {
      return getProperties(context);
    }
    if (StringUtils.equals(property, NAME_PAGE_MANAGER)) {
      return context.pageManager();
    }
    if (StringUtils.equals(property, NAME_CURRENT_PAGE)) {
      return context.currentPage();
    }
    if (StringUtils.equals(property, NAME_RESOURCE_PAGE)) {
      return getResourcePage(context);
    }
    if (StringUtils.equals(property, NAME_PAGE_PROPERTIES)) {
      return getPageProperties(context);
    }
    if (StringUtils.equals(property, NAME_COMPONENT)) {
      return getComponent(context);
    }
    if (StringUtils.equals(property, NAME_DESIGNER)) {
      return getDesigner(context);
    }
    if (StringUtils.equals(property, NAME_CURRENT_DESIGN)) {
      return getCurrentDesign(context);
    }
    if (StringUtils.equals(property, NAME_RESOURCE_DESIGN)) {
      return getResourceDesign(context);
    }
    if (StringUtils.equals(property, NAME_CURRENT_STYLE)) {
      return getStyle(context);
    }

    return null;
  }

  private static ComponentContext getWcmComponentContext(AemContextImpl context) {
    return WCMUtils.getComponentContext(context.request());
  }

  private static EditContext getEditContext(AemContextImpl context) {
    ComponentContext wcmComponentContext = getWcmComponentContext(context);
    if (wcmComponentContext != null) {
      return wcmComponentContext.getEditContext();
    }
    return null;
  }

  @SuppressWarnings("null")
  private static ValueMap getProperties(AemContextImpl context) {
    return wrap(ResourceUtil.getValueMap(context.currentResource()));
  }

  private static Page getResourcePage(AemContextImpl context) {
    Resource resource = context.currentResource();
    if (resource != null) {
      return context.pageManager().getContainingPage(resource);
    }
    return null;
  }

  private static ValueMap getPageProperties(AemContextImpl context) {
    Page page = context.currentPage();
    if (page != null) {
      return wrap(page.getProperties());
    }
    return null;
  }

  private static Component getComponent(AemContextImpl context) {
    Resource resource = context.currentResource();
    if (resource != null) {
      return WCMUtils.getComponent(resource);
    }
    return null;
  }

  private static Designer getDesigner(AemContextImpl context) {
    return context.resourceResolver().adaptTo(Designer.class);
  }

  private static Design getCurrentDesign(AemContextImpl context) {
    return getAndCacheDesign(context.currentPage(), context.request(), getDesigner(context));
  }

  private static Design getResourceDesign(AemContextImpl context) {
    return getAndCacheDesign(getResourcePage(context), context.request(), getDesigner(context));
  }

  private static Style getStyle(AemContextImpl context) {
    ComponentContext wcmComponentContext = getWcmComponentContext(context);
    Design currentDesign = getCurrentDesign(context);
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
