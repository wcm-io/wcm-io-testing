/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;

/**
 * Copy of org.apache.sling.models.impl.via.AbstractResourceTypeViaProvider$ResourceTypeForcingResourceWrapper
 * for testing purposes.
 */
public final class ResourceTypeForcingResourceWrapper extends ResourceWrapper {

  private final String resourceType;

  /**
   * @param resource Resource
   * @param resourceType Resource type
   */
  public ResourceTypeForcingResourceWrapper(Resource resource, String resourceType) {
    super(resource);
    this.resourceType = resourceType;
  }

  @Override
  public String getResourceType() {
    return resourceType;
  }

}
