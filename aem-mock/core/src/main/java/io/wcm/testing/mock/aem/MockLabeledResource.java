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
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;
import org.jetbrains.annotations.NotNull;

import com.day.cq.commons.LabeledResource;

/**
 * Mock implementation of {@link LabeledResource}.
 */
@SuppressWarnings("null")
class MockLabeledResource extends ResourceWrapper implements LabeledResource {

  MockLabeledResource(@NotNull Resource resource) {
    super(resource);
  }

  @Override
  public String getTitle() {
    return getValueMap().get(JCR_TITLE, String.class);
  }

  @Override
  public String getDescription() {
    return getValueMap().get(JCR_DESCRIPTION, String.class);
  }

}
