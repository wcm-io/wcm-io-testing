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
package io.wcm.testing.mock.aem.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.modelsautoreg.ClasspathRegisteredModel;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("null")
public class AemContextTest {

  private final AemContextCallback contextBeforeSetup = mock(AemContextCallback.class);
  private final AemContextCallback contextAfterSetup = mock(AemContextCallback.class);
  private final AemContextCallback contextBeforeTeardown = mock(AemContextCallback.class);
  private final AemContextCallback contextAfterTeardown = mock(AemContextCallback.class);

  // Run all unit tests for each resource resolver types listed here
  @Rule
  public AemContext context = new AemContextBuilder(TestAemContext.ALL_TYPES)
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

  @Before
  public void setUp() throws Exception {
    verify(contextBeforeSetup).execute(context);
    verify(contextAfterSetup).execute(context);
  }

  @Test
  public void testRequest() {
    assertNotNull(context.request());
  }

  @Test
  public void testResourceResolverFactoryActivatorProps() {

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
  public void testSlingModelClasspathRegistered() {
    context.request().setAttribute("prop1", "myValue");
    ClasspathRegisteredModel model = context.request().adaptTo(ClasspathRegisteredModel.class);
    assertEquals("myValue", model.getProp1());
  }

  @After
  public void tearDown() {
    // reset required because mockito gets puzzled with the parameterized JUnit rule
    // TODO: better solution?
    reset(contextBeforeSetup);
    reset(contextAfterSetup);
  }

}
