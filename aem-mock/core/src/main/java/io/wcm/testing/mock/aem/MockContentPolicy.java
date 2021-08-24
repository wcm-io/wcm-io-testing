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

import static com.day.cq.commons.jcr.JcrConstants.JCR_DESCRIPTION;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LASTMODIFIED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LAST_MODIFIED_BY;
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;

import java.util.Calendar;

import com.day.cq.commons.LabeledResource;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.policies.ContentPolicy;

/**
 * Mock implementation of {@link ContentPolicy}.
 */
@SuppressWarnings("null")
class MockContentPolicy extends SlingAdaptable implements ContentPolicy, LabeledResource {

  private final Resource resource;

  MockContentPolicy(Resource resource) {
    this.resource = resource;
  }

  @Override
  public ValueMap getProperties() {
    return resource.getValueMap();
  }

  @Override
  public Calendar getLastModified() {
    return getProperties().get(JCR_LASTMODIFIED, Calendar.class);
  }

  @Override
  public String getLastModifiedBy() {
    return getProperties().get(JCR_LAST_MODIFIED_BY, String.class);
  }

  @Override
  public String getPath() {
    return resource.getPath();
  }

  @Override
  public String getName() {
    return resource.getName();
  }

  @Override
  public String getTitle() {
    return getProperties().get(JCR_TITLE, String.class);
  }

  @Override
  public String getDescription() {
    return getProperties().get(JCR_DESCRIPTION, String.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == Resource.class) {
      return (AdapterType)resource;
    }
    if (type == LabeledResource.class) {
      return (AdapterType)this;
    }
    return super.adaptTo(type);
  }
}
