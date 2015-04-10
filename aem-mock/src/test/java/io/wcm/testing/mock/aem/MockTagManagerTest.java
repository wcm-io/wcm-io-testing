package io.wcm.testing.mock.aem;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import io.wcm.testing.mock.aem.junit.AemContext;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.RangeIterator;
import com.day.cq.tagging.InvalidTagFormatException;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagConstants;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;

public class MockTagManagerTest {

  @Rule
  public AemContext context = new AemContext();

  private ResourceResolver resolver;

  private TagManager tagManager;

  private Page rootPage;

  @Before
  public void setUp() throws Exception {
    resolver = context.resourceResolver();
    context.load().json("/json-import-samples/tags.json", "/etc/tags");
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
    assertTrue(containsPath(pageTags, "/etc/tags/default/tagA"));
    assertTrue(containsPath(pageTags, "/etc/tags/wcmio/aem/api"));

    page = page.listChildren().next();
    pageTags = page.getTags();
    assertNotNull(pageTags);
    assertEquals(2, pageTags.length);
    assertTrue(containsPath(pageTags, "/etc/tags/default/tagB"));
    assertTrue(containsPath(pageTags, "/etc/tags/wcmio/nondescript"));

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
    assertTrue(containsPath(namespaces, "/etc/tags/default"));
    assertTrue(containsPath(namespaces, "/etc/tags/wcmio"));

    Iterator<Tag> namespacesIter = tagManager.getNamespacesIter();
    assertNotNull(namespacesIter);
    assertTrue(namespacesIter.hasNext());
    assertEquals("/etc/tags/default", namespacesIter.next().getPath());
    assertTrue(namespacesIter.hasNext());
    assertEquals("/etc/tags/wcmio", namespacesIter.next().getPath());
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
    assertEquals("/etc/tags/wcmio", existing.getPath());
    // verify that it didn't get overwritten
    assertEquals("WCM IO Tag Namespace", existing.getTitle());
    assertEquals("The WCM IO namespace for testing tag functionality", existing.getDescription());

    Tag newTag = tagManager.createTag("wcmio", "WCM Tag", "This exists in the default namespace", false);
    assertNotNull(newTag);
    assertEquals("/etc/tags/default/wcmio", newTag.getPath());
    assertEquals("WCM Tag", newTag.getTitle());
    assertEquals("This exists in the default namespace", newTag.getDescription());
    // verify that it wasn't auto saved
    assertTrue(resolver.hasChanges());
    context.resourceResolver().commit();

    newTag = tagManager.createTag("wcmio:new", null, null);
    assertNotNull(newTag);
    assertEquals("/etc/tags/wcmio/new", newTag.getPath());
    assertEquals(newTag.getName(), newTag.getTitle());
    assertNull(newTag.getDescription());
    // verify the auto-save
    assertFalse(resolver.hasChanges());
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
    // prove that there are pending changed
    assertTrue(resolver.hasChanges());

    try {
      resolver.commit();
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }

    tagManager.setTags(page.getContentResource(), startTags);
    currentTags = page.getTags();
    assertNotNull(currentTags);
    assertArrayEquals(startTags, currentTags);
    assertFalse(resolver.hasChanges());
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

    resources = tagManager.find("/content", new String[]{"wcmio:nondescript", "/etc/tags/default"}, false);
    assertNotNull(resources);
    assertEquals(0, resources.getPosition());
    assertTrue(resources.hasNext());
    assertEquals(1, resources.getSize());
    assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    assertFalse(resources.hasNext());
    assertEquals(1, resources.getPosition());

    resources = tagManager.find("/content", new String[]{"wcmio:nondescript", "/etc/tags/default"}, true);
    assertNotNull(resources);
    assertEquals(0, resources.getPosition());
    assertTrue(resources.hasNext());
    assertEquals(2, resources.getSize());
    assertEquals("/content/sample/en/jcr:content", resources.next().getPath());
    assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    assertFalse(resources.hasNext());
    assertEquals(2, resources.getPosition());

    resources = tagManager.find("/content", new String[]{"wcmio:nondescrip", "/etc/tags/default"}, true);
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
