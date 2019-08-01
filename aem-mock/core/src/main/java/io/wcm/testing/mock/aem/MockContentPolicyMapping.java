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

import static io.wcm.testing.mock.aem.MockContentPolicyStorage.MOCK_POLICIES_PATH;
import static io.wcm.testing.mock.aem.MockContentPolicyStorage.PN_POLICY;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;

import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyMapping;

/**
 * Mock implementation of {@link ContentPolicyMapping}.
 */
class MockContentPolicyMapping implements ContentPolicyMapping {

  private final Resource resource;
  private final ResourceResolver resourceResolver;

  MockContentPolicyMapping(@NotNull Resource resource) {
    this.resource = resource;
    this.resourceResolver = resource.getResourceResolver();
  }

  @Override
  public String getPath() {
    return resource.getPath();
  }

  @Override
  public ContentPolicy getPolicy() {
    String policyRelativePath = resource.getValueMap().get(PN_POLICY, String.class);
    if (StringUtils.isNotBlank(policyRelativePath)) {
      String policyPath = MOCK_POLICIES_PATH + "/" + policyRelativePath;
      Resource policyResource = resourceResolver.getResource(policyPath);
      if (policyResource != null) {
        return new MockContentPolicy(policyResource);
      }
    }
    return null;
  }


  // --- unsupported operations ---

  @Override
  public Template getTemplate() {
    throw new UnsupportedOperationException();
  }

}
