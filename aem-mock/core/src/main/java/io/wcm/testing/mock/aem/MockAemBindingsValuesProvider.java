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
package io.wcm.testing.mock.aem;

import javax.script.Bindings;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.scripting.api.BindingsValuesProvider;
import org.osgi.service.component.annotations.Component;

import com.day.cq.commons.ValueMapWrapper;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.EditContext;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.commons.WCMUtils;

/**
 * Provides AEM-specific sling script bindings.
 */
@Component(service = BindingsValuesProvider.class)
public class MockAemBindingsValuesProvider implements BindingsValuesProvider {

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

  private static final String RA_DESIGN_CACHE_PREFIX = MockAemBindingsValuesProvider.class.getName() + "_design_";

  @Override
  @SuppressWarnings("null")
  public void addBindings(Bindings bindings) {
    SlingHttpServletRequest request = (SlingHttpServletRequest)bindings.get(SlingBindings.REQUEST);
    Resource resource = request.getResource();
    ResourceResolver resolver = request.getResourceResolver();

    ComponentContext componentContext = WCMUtils.getComponentContext(request);
    EditContext editContext = componentContext != null ? componentContext.getEditContext() : null;
    ValueMap properties = ResourceUtil.getValueMap(resource);
    PageManager pageManager = resolver.adaptTo(PageManager.class);

    Page resourcePage = pageManager.getContainingPage(resource);
    Page currentPage = componentContext == null ? null : componentContext.getPage();
    if (currentPage == null) {
      currentPage = resourcePage;
    }
    ValueMap pageProperties = null;
    if (currentPage != null) {
      pageProperties = new HierarchyNodeInheritanceValueMap(currentPage.getContentResource());
    }

    com.day.cq.wcm.api.components.Component component = WCMUtils.getComponent(resource);

    if (componentContext != null) {
      bindings.put(NAME_COMPONENT_CONTEXT, componentContext);
    }
    if (editContext != null) {
      bindings.put(NAME_EDIT_CONTEXT, editContext);
    }
    if (properties != null) {
      bindings.put(NAME_PROPERTIES, new ValueMapWrapper(properties) {
        // wrap value map to make sure it is not modified
      });
    }
    if (pageManager != null) {
      bindings.put(NAME_PAGE_MANAGER, pageManager);
    }
    if (currentPage != null) {
      bindings.put(NAME_CURRENT_PAGE, currentPage);
    }
    if (resourcePage != null) {
      bindings.put(NAME_RESOURCE_PAGE, resourcePage);
    }
    if (pageProperties != null) {
      bindings.put(NAME_PAGE_PROPERTIES, pageProperties);
    }
    if (component != null) {
      bindings.put(NAME_COMPONENT, component);
    }


    // -- design and style --
    Designer designer = request.getResourceResolver().adaptTo(Designer.class);
    Design currentDesign = null;
    Design resourceDesign = null;
    Style currentStyle = null;
    if (currentPage != null) {
      currentDesign = getAndCacheDesign(currentPage, request, designer);
    }
    if (resourcePage != null) {
      resourceDesign = getAndCacheDesign(resourcePage, request, designer);
    }
    if (currentDesign != null && componentContext != null) {
      currentStyle = currentDesign.getStyle(componentContext.getCell());
    }

    if (designer != null) {
      bindings.put(NAME_DESIGNER, designer);
    }
    if (currentDesign != null) {
      bindings.put(NAME_CURRENT_DESIGN, currentDesign);
    }
    if (resourceDesign != null) {
      bindings.put(NAME_RESOURCE_DESIGN, resourceDesign);
    }
    if (currentStyle != null) {
      bindings.put(NAME_CURRENT_STYLE, currentStyle);
    }

  }

  private Design getAndCacheDesign(Page page, SlingHttpServletRequest request, Designer designer) {
    if (designer == null) {
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
