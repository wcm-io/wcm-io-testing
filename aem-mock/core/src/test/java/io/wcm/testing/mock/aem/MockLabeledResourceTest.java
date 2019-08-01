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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.sling.api.resource.Resource;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.LabeledResource;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockLabeledResourceTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  @Test
  public void testProperties() {
    Resource resource = context.create().resource("/content/test",
        JCR_TITLE, "My Title",
        JCR_DESCRIPTION, "My Description");
    LabeledResource underTest = new MockLabeledResource(resource);

    assertEquals("My Title", underTest.getTitle());
    assertEquals("My Description", underTest.getDescription());
  }

  @Test
  public void testEmpty() {
    Resource resource = context.create().resource("/content/test");
    LabeledResource underTest = new MockLabeledResource(resource);

    assertNull(underTest.getTitle());
    assertNull(underTest.getDescription());
  }

}
