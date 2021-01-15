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

import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.PageContext;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.designer.Cell;
import com.day.cq.wcm.api.designer.ComponentStyle;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyManager;

/**
 * Mock implementation of {@link Design}.
 */
class MockDesign implements Design {

  private final Style emptyStyle = new MockStyle(ValueMap.EMPTY, this);
  private final ResourceResolver resourceResolver;

  MockDesign(ResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
  }

  @Override
  public Style getStyle(String path) {
    Resource resource = resourceResolver.getResource(path);
    if (resource != null) {
      return getStyle(resource);
    }
    return emptyStyle;
  }

  @Override
  public Style getStyle(Cell cell) {
    if (cell instanceof MockCell) {
      Resource resource = ((MockCell)cell).getComponentContext().getResource();
      return getStyle(resource);
    }
    return emptyStyle;
  }

  @Override
  public Style getStyle(Resource resource) {
    ContentPolicyManager contentPolicyManager = resource.getResourceResolver().adaptTo(ContentPolicyManager.class);
    if (contentPolicyManager != null && (contentPolicyManager instanceof MockContentPolicyManager)) {
      // unwrap resource to make sure the correct resource type is used when using resource-type forcing wrappers
      Resource unwrappedResource = ResourceUtil.unwrap(resource);
      ContentPolicy policy = ((MockContentPolicyManager)contentPolicyManager).getPolicy(unwrappedResource);
      if (policy != null) {
        return new MockStyle(policy.getProperties(), this);
      }
    }
    return emptyStyle;
  }

  @Override
  public Style getStyle(Resource resource, boolean ignoreExcludedComponents) {
    return getStyle(resource);
  }

  @Override
  @SuppressWarnings("deprecation")
  public String getPath() {
    return Designer.DEFAULT_DESIGN_PATH;
  }


  // --- unsupported operations ---

  @Override
  public Map<String, ComponentStyle> getComponentStyles(Cell cell) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Resource getContentResource() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getCssPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("deprecation")
  public com.day.cq.commons.Doctype getDoctype(Style style) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getJSON() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Calendar getLastModified() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getStaticCssPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasContent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeCSS(Writer writer, boolean includeCustom) throws IOException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeCssIncludes(Writer writer) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeCssIncludes(PageContext pageContext) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("deprecation")
  public void writeCssIncludes(Writer writer, com.day.cq.commons.Doctype doctype) throws IOException {
    throw new UnsupportedOperationException();
  }

}
