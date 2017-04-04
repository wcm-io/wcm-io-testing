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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
public class MockPageManagerTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private PageManager pageManager;

  private ResourceResolver resourceResolver;

  @Before
  public void setUp() throws Exception {
    // allow to verify calls to resource resolver
    this.resourceResolver = spy(this.context.resourceResolver());

    context.load().json("/json-import-samples/content.json", "/content/sample/en");

    this.pageManager = this.resourceResolver.adaptTo(PageManager.class);
  }

  @Test
  public void testGetPage() {
    Page page = this.pageManager.getPage("/content/sample/en");
    assertNotNull(page);
  }

  @Test
  public void testCreatePage() throws WCMException, PersistenceException {
    testCreatePageInternal(false);
    verify(this.resourceResolver, never()).commit();
  }

  @Test
  public void testCreatePageWithAutoSave() throws WCMException, PersistenceException {
    testCreatePageInternal(true);
    verify(this.resourceResolver, times(1)).commit();
  }

  @Test
  public void testCreatePageWithDefaultContent() throws WCMException {
    // prepare some default content for template
    context.build().resource("/apps/sample/templates/homepage/jcr:content",
        "sling:resourceType", "/apps/sample/components/page/homepage");
    context.create().resource("/apps/sample/templates/homepage/jcr:content/node1",
        "prop1", "abc",
        "prop2", "def");
    context.create().resource("/apps/sample/templates/homepage/jcr:content/node1/node11",
        "prop3", 55);
    context.create().resource("/apps/sample/templates/homepage/jcr:content/node2",
        "prop4", true);

    testCreatePageInternal(false);

    Resource pageResource = this.resourceResolver.getResource("/content/sample/en/test1/jcr:content");
    ValueMap props = pageResource.getValueMap();
    assertEquals("/apps/sample/components/page/homepage", props.get("sling:resourceType", String.class));

    Resource node1 = pageResource.getChild("node1");
    props = node1.getValueMap();
    assertEquals("abc", props.get("prop1", String.class));
    assertEquals("def", props.get("prop2", String.class));

    Resource node11 = pageResource.getChild("node1/node11");
    props = node11.getValueMap();
    assertEquals(55, (int)props.get("prop3", Integer.class));

    Resource node2 = pageResource.getChild("node2");
    props = node2.getValueMap();
    assertEquals(true, props.get("prop4", Boolean.class));
  }

  private void testCreatePageInternal(final boolean autoSave) throws WCMException {
    Page page = this.pageManager.create("/content/sample/en", "test1", "/apps/sample/templates/homepage", "title1", autoSave);
    assertNotNull(page);

    Resource pageResource = this.resourceResolver.getResource("/content/sample/en/test1/jcr:content");
    assertNotNull(pageResource);
    ValueMap props = pageResource.getValueMap();
    assertEquals("title1", props.get(JcrConstants.JCR_TITLE, String.class));
    assertEquals("/apps/sample/templates/homepage", props.get(NameConstants.PN_TEMPLATE, String.class));
  }

  @Test
  public void testCreatePageWithDefaultContent_WTES23() throws Exception {
    context.load().json("/WTEST-23/sample_templates.json", "/content/templates");
    context.pageManager().create("/content", "", "/content/templates/sample-template", "Some title", true);
  }

  @Test
  public void testDeletePage() throws WCMException, PersistenceException {
    this.pageManager.delete(this.pageManager.getPage("/content/sample/en"), false);
    verify(this.resourceResolver, never()).commit();

    assertNull(this.resourceResolver.getResource("/content/sample/en"));
    assertNull(this.resourceResolver.getResource("/content/sample/en/jcr:content"));
    assertNull(this.resourceResolver.getResource("/content/sample/en/toolbar"));
    assertNull(this.resourceResolver.getResource("/content/sample/en/toolbar/jcr:content"));
  }

  @Test
  public void testDeletePageWithAutoSave() throws WCMException, PersistenceException {
    this.pageManager.delete(this.pageManager.getPage("/content/sample/en"), false, true);
    verify(this.resourceResolver, times(1)).commit();

    assertNull(this.resourceResolver.getResource("/content/sample/en"));
    assertNull(this.resourceResolver.getResource("/content/sample/en/jcr:content"));
    assertNull(this.resourceResolver.getResource("/content/sample/en/toolbar"));
    assertNull(this.resourceResolver.getResource("/content/sample/en/toolbar/jcr:content"));
  }

  @Test
  public void testDeletePageShallow() throws WCMException, PersistenceException {
    this.pageManager.delete(this.pageManager.getPage("/content/sample/en"), true, false);
    verify(this.resourceResolver, never()).commit();

    assertNotNull(this.resourceResolver.getResource("/content/sample/en"));
    assertNull(this.resourceResolver.getResource("/content/sample/en/jcr:content"));
    assertNotNull(this.resourceResolver.getResource("/content/sample/en/toolbar"));
    assertNotNull(this.resourceResolver.getResource("/content/sample/en/toolbar/jcr:content"));
  }

  @Test
  public void testDeletePageShallowWithAutoSave() throws WCMException, PersistenceException {
    this.pageManager.delete(this.pageManager.getPage("/content/sample/en"), true, true);
    verify(this.resourceResolver, times(1)).commit();

    assertNotNull(this.resourceResolver.getResource("/content/sample/en"));
    assertNull(this.resourceResolver.getResource("/content/sample/en/jcr:content"));
    assertNotNull(this.resourceResolver.getResource("/content/sample/en/toolbar"));
    assertNotNull(this.resourceResolver.getResource("/content/sample/en/toolbar/jcr:content"));
  }

  @Test
  public void testGetContainingPage() {
    Page containingPage;

    containingPage = this.pageManager.getContainingPage("/content/sample/en");
    assertNotNull(containingPage);
    assertEquals("/content/sample/en", containingPage.getPath());

    containingPage = this.pageManager.getContainingPage("/content/sample/en/jcr:content");
    assertNotNull(containingPage);
    assertEquals("/content/sample/en", containingPage.getPath());

    containingPage = this.pageManager.getContainingPage("/content/sample/en/jcr:content/par/title_1");
    assertNotNull(containingPage);
    assertEquals("/content/sample/en", containingPage.getPath());

    containingPage = this.pageManager.getContainingPage("/content/sample/en/toolbar/jcr:content/par");
    assertNotNull(containingPage);
    assertEquals("/content/sample/en/toolbar", containingPage.getPath());

    containingPage = this.pageManager.getContainingPage("/content/sample");
    assertNull(containingPage);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetTemplate() {
    this.context.load().json("/json-import-samples/application.json", "/apps/sample");
    assertNotNull(this.pageManager.getTemplate("/apps/sample/templates/homepage"));
    assertNull(this.pageManager.getTemplate("/apps/sample/templates/nonExisting"));
  }

  @Test
  public void testCreatePageWithoutName() throws Exception {
    Page page = this.pageManager.create("/content/sample/en", null, "/apps/sample/templates/homepage", "Title 1");
    assertEquals("title-1", page.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreatePageWithInvalidName() throws Exception {
    this.pageManager.create("/content/sample/en", "title.1", "/apps/sample/templates/homepage", "Title 1");
  }

  @Test
  public void testCreatePageWithDuplicateName() throws Exception {
    Page page1 = this.pageManager.create("/content/sample/en", null, "/apps/sample/templates/homepage", "Title 1");
    Page page2 = this.pageManager.create("/content/sample/en", null, "/apps/sample/templates/homepage", "Title 1");
    assertEquals("title-1", page1.getName());
    assertTrue(StringUtils.startsWith(page2.getName(), "title-1"));
    assertNotEquals(page1.getPath(), page2.getPath());
  }

}
