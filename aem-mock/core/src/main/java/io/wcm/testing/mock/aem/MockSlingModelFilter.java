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
package io.wcm.testing.mock.aem;

import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.service.component.annotations.Component;

import com.adobe.cq.export.json.SlingModelFilter;

/**
 * Mock implementation of {@link SlingModelFilter} which does no filtering at all.
 */
@Component(service = SlingModelFilter.class)
@ProviderType
public final class MockSlingModelFilter implements SlingModelFilter {

  @Override
  public Iterable<Resource> filterChildResources(Iterable<Resource> resources) {
    return resources;
  }

  @Override
  public Map<String, Object> filterProperties(Map<String, Object> properties) {
    return properties;
  }

}
