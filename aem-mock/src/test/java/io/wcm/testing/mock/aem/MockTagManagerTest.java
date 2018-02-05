/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 - 2015 wcm.io
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.RangeIterator;
import com.day.cq.tagging.InvalidTagFormatException;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagConstants;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockTagManagerTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private String tagRoot;
  private ResourceResolver resolver;
  private TagManager tagManager;

  private Page rootPage;

  @Before
  public void setUp() throws Exception {
    tagRoot = MockTagManager.getTagRootPath();
    resolver = context.resourceResolver();
    context.load().json("/json-import-samples/tags.json", tagRoot);
    context.load().json("/json-import-samples/content.json", "/content/sample/en");
    // commit now to be able to utilize hasChanges() accurately in test cases
    resolver.commit();

    tagManager = resolver.adaptTo(TagManager.class);
    rootPage = resolver.getResource("/content/sample/en").adaptTo(Page.class);
  }

  @Test
  public void testGetPageTags() {
    Page page = rootPage;
    Tag[] pageTags = page.getTags();
    assertNotNull(pageTags);
    assertEquals(2, pageTags.length);
    assertTrue(containsPath(pageTags, tagRoot + "/default/tagA"));
    assertTrue(containsPath(pageTags, tagRoot + "/wcmio/aem/api"));

    page = page.listChildren().next();
    pageTags = page.getTags();
    assertNotNull(pageTags);
    assertEquals(2, pageTags.length);
    assertTrue(containsPath(pageTags, tagRoot + "/default/tagB"));
    assertTrue(containsPath(pageTags, tagRoot + "/wcmio/nondescript"));

    page = page.listChildren().next();
    pageTags = page.getTags();
    assertNotNull(pageTags);
    assertEquals(0, pageTags.length);
  }

  @Test
  public void testGetNamespaces() {
    Tag[] namespaces = tagManager.getNamespaces();
    assertNotNull(namespaces);
    assertEquals(2, namespaces.length);
    assertTrue(containsPath(namespaces, tagRoot + "/default"));
    assertTrue(containsPath(namespaces, tagRoot + "/wcmio"));

    Iterator<Tag> namespacesIter = tagManager.getNamespacesIter();
    assertNotNull(namespacesIter);
    assertTrue(namespacesIter.hasNext());
    assertEquals(tagRoot + "/default", namespacesIter.next().getPath());
    assertTrue(namespacesIter.hasNext());
    assertEquals(tagRoot + "/wcmio", namespacesIter.next().getPath());
    assertFalse(namespacesIter.hasNext());
  }

  @Test
  public void testCanCreateTag() throws InvalidTagFormatException {
    assertFalse(tagManager.canCreateTag(TagConstants.DEFAULT_NAMESPACE_ID));
    assertFalse(tagManager.canCreateTag("wcmio:"));
    assertTrue(tagManager.canCreateTag("wcmio"));
  }

  @Test
  public void testCreateTag() throws InvalidTagFormatException, PersistenceException {
    Tag existing = tagManager.createTag("wcmio:", "foo", "bar");
    assertNotNull(existing);
    assertEquals(tagRoot + "/wcmio", existing.getPath());
    // verify that it didn't get overwritten
    assertEquals("WCM IO Tag Namespace", existing.getTitle());
    assertEquals("The WCM IO namespace for testing tag functionality", existing.getDescription());

    Tag newTag = tagManager.createTag("wcmio", "WCM Tag", "This exists in the default namespace", false);
    assertNotNull(newTag);
    assertEquals(tagRoot + "/default/wcmio", newTag.getPath());
    assertEquals("WCM Tag", newTag.getTitle());
    assertEquals("This exists in the default namespace", newTag.getDescription());

    if (context.resourceResolverType() != ResourceResolverType.JCR_MOCK) {
      // verify that it wasn't auto saved
      assertTrue(resolver.hasChanges());
    }

    context.resourceResolver().commit();

    newTag = tagManager.createTag("wcmio:new", null, null);
    assertNotNull(newTag);
    assertEquals(tagRoot + "/wcmio/new", newTag.getPath());
    assertEquals(newTag.getName(), newTag.getTitle());
    assertNull(newTag.getDescription());

    if (context.resourceResolverType() != ResourceResolverType.JCR_MOCK) {
      // verify the auto-save
      assertFalse(resolver.hasChanges());
    }
  }

  @Test
  public void setTags() {
    Page page = rootPage;
    Tag[] startTags = page.getTags();

    tagManager.setTags(page.getContentResource(), null, false);
    Tag[] currentTags = page.getTags();
    // prove that it's now empty
    assertNotNull(currentTags);
    assertEquals(0, currentTags.length);
    // prove that it wasn't empty to begin with
    assertNotEquals(startTags.length, currentTags.length);

    if (context.resourceResolverType() != ResourceResolverType.JCR_MOCK) {
      // prove that there are pending changed
      assertTrue(resolver.hasChanges());
    }

    try {
      resolver.commit();
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }

    tagManager.setTags(page.getContentResource(), startTags);
    currentTags = page.getTags();
    assertNotNull(currentTags);
    assertArrayEquals(startTags, currentTags);

    if (context.resourceResolverType() != ResourceResolverType.JCR_MOCK) {
      assertFalse(resolver.hasChanges());
    }
  }

  @Test
  public void testFind() {
    RangeIterator<Resource> resources = tagManager.find("wcmio:");
    assertNotNull(resources);
    assertTrue(resources.hasNext());
    assertEquals(2, resources.getSize());
    assertEquals(0, resources.getPosition());
    assertEquals("/content/sample/en/jcr:content", resources.next().getPath());
    assertTrue(resources.hasNext());
    assertEquals(1, resources.getPosition());
    assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    assertFalse(resources.hasNext());
    assertEquals(2, resources.getPosition());

    resources = tagManager.find("/content/sample/en/toolbar", new String[]{"wcmio:"});
    assertNotNull(resources);
    assertEquals(0, resources.getPosition());
    assertTrue(resources.hasNext());
    assertEquals(1, resources.getSize());
    assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    assertFalse(resources.hasNext());
    assertEquals(1, resources.getPosition());

    resources = tagManager.find("/content", new String[] { "wcmio:nondescript", tagRoot + "/default" }, false);
    assertNotNull(resources);
    assertEquals(0, resources.getPosition());
    assertTrue(resources.hasNext());
    assertEquals(1, resources.getSize());
    assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    assertFalse(resources.hasNext());
    assertEquals(1, resources.getPosition());

    resources = tagManager.find("/content", new String[] { "wcmio:nondescript", tagRoot + "/default" }, true);
    assertNotNull(resources);
    assertEquals(0, resources.getPosition());
    assertTrue(resources.hasNext());
    assertEquals(2, resources.getSize());
    assertEquals("/content/sample/en/jcr:content", resources.next().getPath());
    assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    assertFalse(resources.hasNext());
    assertEquals(2, resources.getPosition());

    resources = tagManager.find("/content", new String[] { "wcmio:nondescrip", tagRoot + "/default" }, true);
    assertNull(resources);
  }

  @Test
  public void testResolve() {
    Tag tag = tagManager.resolve("wcmio:");
    assertNotNull(tag);
    assertEquals("wcmio:", tag.getTagID());

    tag = tagManager.resolve("wcmio");
    assertNull(tag);
  }

  private boolean containsPath(Tag[] tags, String path) {
    for (Tag tag : tags) {
      if (StringUtils.equals(tag.getPath(), path)) {
        return true;
      }
    }
    return false;
  }

}
