/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
package io.wcm.testing.mock.aem.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.resourceresolver.impl.ResourceResolverImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test with {@link AemContext} which uses by default {@link ResourceResolverMockAemContext}.
 */
@ExtendWith(AemContextExtension.class)
class AemContextMemberInstantiatedTest {

  AemContext context = new JcrMockAemContext();

  @BeforeEach
  void setUp() {
    assertTrue(context instanceof JcrMockAemContext);
    assertTrue(context.resourceResolver() instanceof ResourceResolverImpl);

    context.create().resource("/content/test",
        "prop1", "value1");
  }

  @Test
  void testResource() {
    Resource resource = context.resourceResolver().getResource("/content/test");
    assertEquals("value1", resource.getValueMap().get("prop1"));
  }

}
