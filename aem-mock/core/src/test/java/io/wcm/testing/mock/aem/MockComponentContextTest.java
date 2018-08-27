/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;
import com.google.common.collect.ImmutableMap;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockComponentContextTest {

  private static final String COMPONENT_RESOURCE_TYPE = "/apps/test/components/component1";
  private static final String PAGE_PATH = "/content/page1";

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private Page page;
  private Resource resource;
  private ComponentContext underTest;

  @Before
  public void setUp() {
    context.create().resource(COMPONENT_RESOURCE_TYPE, ImmutableMap.<String, Object>builder()
        .put(JcrConstants.JCR_PRIMARYTYPE, "cq:Component")
        .build());

    page = context.create().page(PAGE_PATH);
    resource = context.create().resource(page.getContentResource().getPath() + "/comp1", ImmutableMap.<String, Object>builder()
        .put("sling:resourceType", COMPONENT_RESOURCE_TYPE)
        .build());

    context.currentPage(page);
    context.currentResource(resource);

    underTest = WCMUtils.getComponentContext(context.request());
  }

  @Test
  public void testGetPage() {
    assertEquals(page.getPath(), underTest.getPage().getPath());
  }

  @Test
  public void testGetResource() {
    assertEquals(resource.getPath(), underTest.getResource().getPath());
  }

  @Test
  public void testGetComponent() {
    Component component = underTest.getComponent();
    assertEquals(COMPONENT_RESOURCE_TYPE, component.getPath());
  }

  @Test
  public void testAttributes() {
    underTest.setAttribute("attr1", "value1");
    assertEquals("value1", underTest.getAttribute("attr1"));
  }

  @Test
  public void testHasDecoration() {
    assertTrue(underTest.hasDecoration());
    underTest.setDecorate(false);
    assertFalse(underTest.hasDecoration());
  }

  @Test
  public void testGetDecorationTagName() {
    underTest.setDecorationTagName("xyz");
    assertEquals("xyz", underTest.getDecorationTagName());
  }

  @Test
  public void testGetDefaultDecorationTagName() {
    underTest.setDefaultDecorationTagName("xyz");
    assertEquals("xyz", underTest.getDefaultDecorationTagName());
  }

  @Test
  public void testGetEditContext() {
    // no edit context in wcmmode=disabled
    assertNull(underTest.getEditContext());
  }

}
