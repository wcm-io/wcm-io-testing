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
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.designer.Cell;
import com.day.cq.wcm.api.designer.ComponentStyle;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Style;

/**
 * Mock implementation of {@link Design}.
 */
class MockDesign implements Design {

  // all getStyle methods just return an empty but non-null Style object
  private final Style defaultStyle = new MockStyle(this, ValueMap.EMPTY);

  @Override
  public Style getStyle(String path) {
    return defaultStyle;
  }

  @Override
  public Style getStyle(Cell cell) {
    return defaultStyle;
  }

  @Override
  public Style getStyle(Resource resource) {
    return defaultStyle;
  }

  @Override
  public Style getStyle(Resource resource, boolean ignoreExcludedComponents) {
    return defaultStyle;
  }


  // --- unsupported operations ---

  @Override
  public Map<String, ComponentStyle> getComponentStyles(Cell arg0) {
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
  public com.day.cq.commons.Doctype getDoctype(Style arg0) {
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
  public String getPath() {
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
  public void writeCSS(Writer arg0, boolean arg1) throws IOException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeCssIncludes(Writer arg0) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeCssIncludes(PageContext arg0) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("deprecation")
  public void writeCssIncludes(Writer arg0, com.day.cq.commons.Doctype arg1) throws IOException {
    throw new UnsupportedOperationException();
  }

}
