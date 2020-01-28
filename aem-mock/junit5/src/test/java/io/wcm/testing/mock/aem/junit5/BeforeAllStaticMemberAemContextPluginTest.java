/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.ImmutableMap;

/**
 * Test with {@link AemContext} with context plugins.
 */
@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class BeforeAllStaticMemberAemContextPluginTest {

  private static AemContextCallback contextBeforeSetup = mock(AemContextCallback.class);
  private static AemContextCallback contextAfterSetup = mock(AemContextCallback.class);
  // note: we cannot test the execution of the before/afterTearDown methods as this happens after @AfterAll
  private static AemContextCallback contextBeforeTeardown = mock(AemContextCallback.class);
  private static AemContextCallback contextAfterTeardown = mock(AemContextCallback.class);

  private static AemContext context = new AemContextBuilder()
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

  @BeforeAll
  static void beforeAll() throws Exception {
    verify(contextBeforeSetup).execute(context);
    verify(contextAfterSetup).execute(context);
  }

  @Test
  void testRequest() throws Exception {
    assertNotNull(context.request());
  }

}
