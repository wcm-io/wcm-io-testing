/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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

import java.util.Collection;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;

import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;

/**
 * Mock implementation of {@link ComponentManager}.
 */
class MockComponentManager implements ComponentManager {

  private final ResourceResolver resourceResolver;

  public MockComponentManager(ResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
  }

  @Override
  public Component getComponent(String path) {
    Resource resource = resourceResolver.getResource(path);
    if (resource != null) {
      return new MockComponent(resource);
    }
    return null;
  }

  @Override
  public Component getComponentOfResource(Resource resource) {
    String resourceType = resource.getResourceType();
    if (resourceType != null) {
      resourceType = ResourceUtil.resourceTypeToPath(resource.getResourceType());
      return getComponent(resourceType);
    }
    else {
      return null;
    }
  }


  // --- unsupported operations ---

  @Override
  public Collection<Component> getComponents() {
    throw new UnsupportedOperationException();
  }

}
