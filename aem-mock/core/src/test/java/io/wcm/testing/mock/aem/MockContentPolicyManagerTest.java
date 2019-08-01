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

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.sling.api.scripting.SlingBindings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.adobe.cq.sightly.WCMBindings;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyManager;
import com.day.cq.wcm.commons.WCMUtils;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockContentPolicyManagerTest {

  private static final String RT_TEST = "app1/components/test";
  private static final String RT_TEST_2 = "/apps/app1/components/test2";

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private ContentPolicyManager underTest;

  private Page page;

  @Before
  public void setUp() {
    page = context.create().page("/content/test");
    underTest = context.resourceResolver().adaptTo(ContentPolicyManager.class);
  }

  @Test
  public void testWithoutPolicy() {
    context.currentResource(context.create().resource(page, "resource1",
        PROPERTY_RESOURCE_TYPE, RT_TEST));

    ComponentContext componentContext = WCMUtils.getComponentContext(context.request());
    ContentPolicy policy = underTest.getPolicy(componentContext);

    assertNull(policy);

    Style style = getStyle();
    assertNull(style.get("prop1", String.class));
  }

  @Test
  public void testWithPolicy() {
    context.currentResource(context.create().resource(page, "resource1",
        PROPERTY_RESOURCE_TYPE, RT_TEST));

    // create policy
    context.contentPolicyMapping(RT_TEST,
        "prop1", "value1");

    ComponentContext componentContext = WCMUtils.getComponentContext(context.request());
    ContentPolicy policy = underTest.getPolicy(componentContext);

    assertNotNull(policy);
    assertEquals("value1", policy.getProperties().get("prop1", String.class));

    Style style = getStyle();
    assertEquals("value1", style.get("prop1", String.class));


    // overwrite policy with new data
    context.contentPolicyMapping(RT_TEST,
        "prop1", "value2");

    policy = underTest.getPolicy(componentContext);

    assertNotNull(policy);
    assertEquals("value2", policy.getProperties().get("prop1", String.class));

    style = getStyle();
    assertEquals("value2", style.get("prop1", String.class));
  }

  @Test
  public void testWithoutResourceType() {
    context.currentResource(context.create().resource(page, "resource1"));

    ComponentContext componentContext = WCMUtils.getComponentContext(context.request());
    ContentPolicy policy = underTest.getPolicy(componentContext);

    assertNull(policy);

    Style style = getStyle();
    assertNull(style.get("prop1", String.class));
  }

  @Test
  public void testWithAbsoluteResourceType() {
    context.currentResource(context.create().resource(page, "resource1",
        PROPERTY_RESOURCE_TYPE, RT_TEST_2));

    // create policy
    context.contentPolicyMapping(RT_TEST_2,
        "prop1", "value1");

    ComponentContext componentContext = WCMUtils.getComponentContext(context.request());
    ContentPolicy policy = underTest.getPolicy(componentContext);

    assertNotNull(policy);
    assertEquals("value1", policy.getProperties().get("prop1", String.class));

    Style style = getStyle();
    assertEquals("value1", style.get("prop1", String.class));
  }

  private Style getStyle() {
    SlingBindings bindings = (SlingBindings)context.request().getAttribute(SlingBindings.class.getName());
    if (bindings != null) {
      return (Style)bindings.get(WCMBindings.CURRENT_STYLE);
    }
    return null;
  }

}
