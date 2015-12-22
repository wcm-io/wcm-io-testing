/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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

import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.EditConfig;
import com.day.cq.wcm.api.components.EditContext;

/**
 * Mock implementation of {@link EditContext}.
 */
final class MockEditContext implements EditContext {

  private final ComponentContext componentContext;
  private final EditConfig editConfig = new MockEditConfig();

  /**
   * @param componentContext Component context
   */
  MockEditContext(ComponentContext componentContext) {
    this.componentContext = componentContext;
  }

  @Override
  public ComponentContext getComponentContext() {
    return componentContext;
  }

  @Override
  public Component getComponent() {
    return componentContext.getComponent();
  }

  @Override
  public Object getAttribute(String name) {
    return componentContext.getAttribute(name);
  }

  @Override
  public Object setAttribute(String name, Object value) {
    return componentContext.setAttribute(name, value);
  }

  @Override
  public EditConfig getEditConfig() {
    return editConfig;
  }


  // --- unsupported operations ---

  @Override
  public boolean isRoot() {
    throw new UnsupportedOperationException();
  }

  @Override
  public EditContext getParent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public EditContext getRoot() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setContentPath(String path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void includeProlog(SlingHttpServletRequest req, SlingHttpServletResponse resp, WCMMode mode) throws IOException, ServletException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void includeEpilog(SlingHttpServletRequest req, SlingHttpServletResponse resp, WCMMode mode) throws IOException, ServletException {
    throw new UnsupportedOperationException();
  }

}
