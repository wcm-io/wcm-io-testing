/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import org.apache.sling.api.resource.Resource;

import com.adobe.cq.dam.cfm.VariationDef;
import com.adobe.cq.dam.cfm.VariationTemplate;
import com.day.cq.commons.jcr.JcrConstants;

/**
 * Mock implementation of {@link VariationDef}.
 */
class MockContentFragment_VariationDef implements VariationDef, VariationTemplate {

  private final Resource resource;

  MockContentFragment_VariationDef(Resource resource) {
    this.resource = resource;
  }

  @Override
  public String getName() {
    return resource.getValueMap().get("name", resource.getName());
  }

  @Override
  public String getTitle() {
    return resource.getValueMap().get(JcrConstants.JCR_TITLE, String.class);
  }

  @Override
  public String getDescription() {
    return resource.getValueMap().get(JcrConstants.JCR_DESCRIPTION, String.class);
  }

  // AEM 6.4/6.5
  @SuppressWarnings("unused")
  public void setTitle(String title) {
    throw new UnsupportedOperationException();
  }

  // AEM 6.4/6.5
  @SuppressWarnings("unused")
  public void setDescription(String description) {
    throw new UnsupportedOperationException();
  }

}
