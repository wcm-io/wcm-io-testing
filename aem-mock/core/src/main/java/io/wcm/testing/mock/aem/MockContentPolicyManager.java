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

import java.util.List;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyManager;
import com.day.cq.wcm.api.policies.ContentPolicyMapping;

/**
 * Mock implementation of {@link ContentPolicy}.
 */
class MockContentPolicyManager implements ContentPolicyManager {

  private final ResourceResolver resourceResolver;

  MockContentPolicyManager(@NotNull ResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
  }

  @Override
  public ContentPolicy getPolicy(ComponentContext componentContext) {
    return getPolicy(componentContext.getResource());
  }

  @Override
  public ContentPolicyMapping getPolicyMapping(Resource contentResource) {
    String resourceType = contentResource.getResourceType();
    if (StringUtils.isNotBlank(resourceType)) {
      return MockContentPolicyStorage.getContentPolicyMapping(resourceType, resourceResolver);
    }
    return null;
  }

  @Override
  public ContentPolicy getPolicy(Resource contentResource) {
    ContentPolicyMapping mapping = getPolicyMapping(contentResource);
    if (mapping != null) {
      return mapping.getPolicy();
    }
    return null;
  }

  // AEM 6.4
  @SuppressWarnings("unused")
  public @Nullable ContentPolicy getPolicy(@NotNull Resource contentResource, @Nullable SlingHttpServletRequest request) {
    return getPolicy(contentResource);
  }


  // --- unsupported operations ---

  @Override
  public List<ContentPolicy> getPolicies(String path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ContentPolicy> getPolicies(String path, String policyResourceType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ContentPolicy copyPolicy(ContentPolicy originalPolicy, String newTitle, String newDescription) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ContentPolicyMapping> getPolicyMappings(ContentPolicy contentPolicy) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPolicyLocation(Resource resource) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Template> getTemplates(String policyAbsolutePath) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Template> getTemplates(String policyAbsolutePath, Predicate filter) {
    throw new UnsupportedOperationException();
  }

}
