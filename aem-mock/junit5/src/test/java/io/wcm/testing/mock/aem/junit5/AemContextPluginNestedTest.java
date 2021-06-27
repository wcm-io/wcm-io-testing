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
package io.wcm.testing.mock.aem.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.ImmutableMap;

import io.wcm.testing.mock.aem.modelsautoreg.ClasspathRegisteredModel;

/**
 * Test with {@link AemContext} with context plugins and nested tests.
 */
@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class AemContextPluginNestedTest {

  private final AemContextCallback contextBeforeSetup = mock(AemContextCallback.class);
  private final AemContextCallback contextAfterSetup = mock(AemContextCallback.class);
  private final AemContextCallback contextBeforeTeardown = mock(AemContextCallback.class);
  // note: we cannot test the execution of the afterTearDown method as this happens after @AfterEach
  private final AemContextCallback contextAfterTeardown = mock(AemContextCallback.class);

  private final AemContext context = new AemContextBuilder()
      .beforeSetUp(contextBeforeSetup)
      .afterSetUp(contextAfterSetup)
      .beforeTearDown(contextBeforeTeardown)
      .afterTearDown(contextAfterTeardown)
      .resourceResolverFactoryActivatorProps(ImmutableMap.<String, Object>of("resource.resolver.searchpath", new String[] {
          "/apps",
          "/libs",
          "/testpath",
      }))
      .build();

  @Nested
  class NestedTest {

    @BeforeEach
    void setUp() throws Exception {
      verify(contextBeforeSetup).execute(context);
    }

    @Test
    void testRequest() throws Exception {
      verify(contextAfterSetup).execute(context);
      assertNotNull(context.request());
    }

    @Test
    void testResourceResolverFactoryActivatorProps() throws Exception {
      verify(contextAfterSetup).execute(context);

      // skip this test for resource resolver mock, because it does not respect the custom config
      if (context.resourceResolverType() == ResourceResolverType.RESOURCERESOLVER_MOCK) {
        return;
      }

      context.create().resource("/apps/node1");

      context.create().resource("/libs/node1");
      context.create().resource("/libs/node2");

      context.create().resource("/testpath/node1");
      context.create().resource("/testpath/node2");
      context.create().resource("/testpath/node3");

      assertEquals("/apps/node1", context.resourceResolver().getResource("node1").getPath());
      assertEquals("/libs/node2", context.resourceResolver().getResource("node2").getPath());
      assertEquals("/testpath/node3", context.resourceResolver().getResource("node3").getPath());
      assertNull(context.resourceResolver().getResource("node4"));
    }

    @Test
    void testSlingModelClasspathRegistered() {
      context.request().setAttribute("prop1", "myValue");
      ClasspathRegisteredModel model = context.request().adaptTo(ClasspathRegisteredModel.class);
      assertEquals("myValue", model.getProp1());
    }

    @AfterEach
    void tearDown() throws Exception {
      verify(contextBeforeTeardown).execute(context);
    }

  }

}
