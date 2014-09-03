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
package io.wcm.testing.mock.sling.resourceresolvertype.rrmock.resource;

import io.wcm.testing.mock.sling.MockSling;
import io.wcm.testing.mock.sling.ResourceResolverType;

import org.apache.sling.api.resource.ResourceResolver;

public class SlingCrudResourceResolverTest extends io.wcm.testing.mock.sling.resource.SlingCrudResourceResolverTest {

  @Override
  protected ResourceResolverType getResourceResolverType() {
    return ResourceResolverType.RESOURCERESOLVER_MOCK;
  }

  @Override
  protected ResourceResolver newResourceResolver() {
    return MockSling.newResourceResolver(getResourceResolverType());
  }

}
