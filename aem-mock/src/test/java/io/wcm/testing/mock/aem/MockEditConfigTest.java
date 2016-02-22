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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import javax.jcr.Node;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.JSONItem;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.DialogMode;
import com.day.cq.wcm.api.components.DropTarget;
import com.day.cq.wcm.api.components.EditConfig;
import com.day.cq.wcm.api.components.EditLayout;
import com.day.cq.wcm.api.components.InplaceEditingConfig;
import com.day.cq.wcm.commons.WCMUtils;
import com.google.common.collect.ImmutableMap;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockEditConfigTest {

  private static final String COMPONENT_RESOURCE_TYPE = "/apps/test/components/component1";
  private static final String PAGE_PATH = "/content/page1";

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private Page page;
  private Resource resource;
  private ComponentContext componentContext;
  private EditConfig underTest;

  @Before
  public void setUp() {
    WCMMode.EDIT.toRequest(context.request());

    context.create().resource(COMPONENT_RESOURCE_TYPE, ImmutableMap.<String, Object>builder()
        .put(JcrConstants.JCR_PRIMARYTYPE, "cq:Component")
        .build());

    page = context.create().page(PAGE_PATH);
    resource = context.create().resource(page.getContentResource().getPath() + "/comp1", ImmutableMap.<String, Object>builder()
        .put("sling:resourceType", COMPONENT_RESOURCE_TYPE)
        .build());

    context.currentPage(page);
    context.currentResource(resource);

    componentContext = WCMUtils.getComponentContext(context.request());
    underTest = componentContext.getEditContext().getEditConfig();

    assertNotNull(underTest);
  }

  @Test
  public void testGetLayout() {
    assertEquals(EditLayout.AUTO, underTest.getLayout());
    underTest.setLayout(EditLayout.EDITBAR);
    assertEquals(EditLayout.EDITBAR, underTest.getLayout());
  }

  @Test
  public void testGetDialogMode() {
    assertEquals(DialogMode.AUTO, underTest.getDialogMode());
    underTest.setDialogMode(DialogMode.FLOATING);
    assertEquals(DialogMode.FLOATING, underTest.getDialogMode());
  }

  @Test
  public void testGetInplaceEditingConfig() throws Exception {
    assertNull(underTest.getInplaceEditingConfig());

    Node node = mock(Node.class);
    InplaceEditingConfig config = new InplaceEditingConfig(node);

    underTest.setInplaceEditingConfig(config);
    assertSame(config, underTest.getInplaceEditingConfig());
  }

  @Test
  public void testGetInsertBehavior() {
    assertNull(underTest.getInsertBehavior());
    underTest.setInsertBehavior("test");
    assertEquals("test", underTest.getInsertBehavior());
  }

  @Test
  public void testIsEmpty() {
    assertFalse(underTest.isEmpty());
    underTest.setEmpty(true);
    assertTrue(underTest.isEmpty());
  }

  @Test
  public void testGetEmptyText() {
    assertNull(underTest.getEmptyText());
    underTest.setEmptyText("test");
    assertEquals("test", underTest.getEmptyText());
  }

  @Test
  public void testIsOrderable() {
    assertNull(underTest.isOrderable());
    underTest.setOrderable(true);
    assertTrue(underTest.isOrderable());
  }

  @Test
  public void testIsDeepCancel() {
    assertNull(underTest.isDeepCancel());
    underTest.setDeepCancel(true);
    assertTrue(underTest.isDeepCancel());
  }

  @Test
  public void testGetLiveRelationship() {
    assertNull(underTest.getLiveRelationship());

    JSONItem jsonItem = mock(JSONItem.class);

    underTest.setLiveRelationship(jsonItem);
    assertSame(jsonItem, underTest.getLiveRelationship());
  }

  @Test
  public void testGetDropTargets() {
    assertTrue(underTest.getDropTargets().isEmpty());

    DropTarget dropTarget = mock(DropTarget.class);

    underTest.getDropTargets().put("target1", dropTarget);
    assertSame(dropTarget, underTest.getDropTargets().get("target1"));
  }

}
