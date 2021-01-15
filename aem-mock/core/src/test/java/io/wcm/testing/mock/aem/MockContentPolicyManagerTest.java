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

import org.apache.sling.api.resource.Resource;
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
import com.google.common.collect.ImmutableMap;

import io.wcm.testing.mock.aem.context.ResourceTypeForcingResourceWrapper;
import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockContentPolicyManagerTest {

  private static final String RT_TEST = "app1/components/test";
  private static final String RT_TEST_2_ABSOLUTE = "/apps/app1/components/test2";
  private static final String RT_TEST_3 = "app1/components/test3";

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
        PROPERTY_RESOURCE_TYPE, RT_TEST_2_ABSOLUTE));

    // create policy
    context.contentPolicyMapping(RT_TEST_2_ABSOLUTE,
        "prop1", "value1");

    ComponentContext componentContext = WCMUtils.getComponentContext(context.request());
    ContentPolicy policy = underTest.getPolicy(componentContext);

    assertNotNull(policy);
    assertEquals("value1", policy.getProperties().get("prop1", String.class));

    Style style = getStyle();
    assertEquals("value1", style.get("prop1", String.class));
  }

  @Test
  @SuppressWarnings("null")
  public void testWithNestedResources() {
    context.currentResource(context.create().resource(page, "resource1",
        PROPERTY_RESOURCE_TYPE, RT_TEST));

    // create policy with nested resources
    context.contentPolicyMapping(RT_TEST,
        ImmutableMap.<String, Object>of(
            "prop1", "value1",
            "child1", ImmutableMap.<String,Object>of(
                "prop2","value2",
                "child2", ImmutableMap.<String,Object>of(
                    "prop3", "value3")
                )
            ));

    ComponentContext componentContext = WCMUtils.getComponentContext(context.request());
    ContentPolicy policy = underTest.getPolicy(componentContext);

    assertNotNull(policy);
    assertEquals("value1", policy.getProperties().get("prop1", String.class));

    Resource resource = policy.adaptTo(Resource.class);
    assertEquals("value1", resource.getValueMap().get("prop1", String.class));

    Resource child1 = resource.getChild("child1");
    assertEquals("value2", child1.getValueMap().get("prop2", String.class));

    Resource child2 = child1.getChild("child2");
    assertEquals("value3", child2.getValueMap().get("prop3", String.class));
  }

  @Test
  public void testWithWrappedResource() {
    context.create().resource("/apps/" + RT_TEST);
    context.create().resource("/apps/" + RT_TEST_3,
        "sling:resourceSuperType", RT_TEST);

    Resource resource = context.create().resource(page, "resource1",
        PROPERTY_RESOURCE_TYPE, RT_TEST_3);

    // set current resource to wrapped resource with resource type of super component
    context.currentResource(new ResourceTypeForcingResourceWrapper(resource, RT_TEST));

    // create policy
    context.contentPolicyMapping(RT_TEST_3,
        "prop1", "value1");

    ComponentContext componentContext = WCMUtils.getComponentContext(context.request());
    ContentPolicy policy = underTest.getPolicy(componentContext);

    assertNotNull(policy);
    assertEquals("value1", policy.getProperties().get("prop1", String.class));

    Style style = getStyle();
    assertEquals("value1", style.get("prop1", String.class));
  }

  @SuppressWarnings("deprecation")
  private Style getStyle() {
    SlingBindings bindings = (SlingBindings)context.request().getAttribute(SlingBindings.class.getName());
    if (bindings != null) {
      return (Style)bindings.get(WCMBindings.CURRENT_STYLE);
    }
    return null;
  }

}
