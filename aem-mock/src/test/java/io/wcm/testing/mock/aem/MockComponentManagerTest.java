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
package io.wcm.testing.mock.aem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextTest;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;
import com.google.common.collect.ImmutableMap;

public class MockComponentManagerTest {

  @Rule
  public AemContext context = new AemContext(AemContextTest.ALL_TYPES);

  private ComponentManager underTest;

  @Before
  public void setUp() {
    // create some component paths
    context.create().resource("/apps/app1/components/c1", ImmutableMap.<String, Object>builder()
        .put(JcrConstants.JCR_TITLE, "myTitle")
        .put(JcrConstants.JCR_DESCRIPTION, "myDescription")
        .build());

    context.create().resource("/libs/app1/components/c2");
    context.create().resource("/apps/app1/components/c2");

    context.create().resource("/libs/app1/components/c3");

    context.create().resource("/content/myresource", ImmutableMap.<String, Object>builder()
        .put("sling:resourceType", "/apps/app1/components/c1")
        .build());

    underTest = context.resourceResolver().adaptTo(ComponentManager.class);
  }

  @Test
  public void testGetComponent() {
    Component component = underTest.getComponent("/apps/app1/components/c1");

    assertNotNull(component);
    assertEquals("/apps/app1/components/c1", component.getPath());
    assertEquals("c1", component.getName());
    assertEquals("myTitle", component.getTitle());
    assertEquals("myDescription", component.getDescription());
    assertEquals("myTitle", component.getProperties().get(JcrConstants.JCR_TITLE, String.class));
    assertTrue(StringUtils.isEmpty(component.getResourceType())
        || StringUtils.equals(JcrConstants.NT_UNSTRUCTURED, component.getResourceType()));
    assertTrue(component.isAccessible());
    assertNotNull(component.adaptTo(Resource.class));
  }

  @Test
  public void testInvalidComponent() {
    Component component = underTest.getComponent("/invalidPath");
    assertNull(component);
  }

  @Test
  public void testGetComponentWithSearchPath() {
    assertEquals("/apps/app1/components/c1", underTest.getComponent("app1/components/c1").getPath());
    assertEquals("/apps/app1/components/c2", underTest.getComponent("app1/components/c2").getPath());
    assertEquals("/libs/app1/components/c3", underTest.getComponent("app1/components/c3").getPath());
  }

  @Test
  public void testGetComponentOfResource() {
    Resource resource = context.resourceResolver().getResource("/content/myresource");
    Component component = underTest.getComponentOfResource(resource);
    assertNotNull(component);
    assertEquals("/apps/app1/components/c1", component.getPath());
  }

  @Test
  public void testGetComponentOfResourceWithoutResourceType() {
    context.create().resource("/content/myresourceWithoutResourceType", ImmutableMap.<String, Object>builder()
        .build());

    Resource resource = context.resourceResolver().getResource("/content/myresourceWithoutResourceType");
    Component component = underTest.getComponentOfResource(resource);
    assertNull(component);
  }

}
