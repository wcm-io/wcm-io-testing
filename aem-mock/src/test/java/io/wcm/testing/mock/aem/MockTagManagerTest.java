package io.wcm.testing.mock.aem;

import io.wcm.testing.mock.aem.junit.AemContext;

import java.util.Iterator;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Assert;
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
    Assert.assertNotNull(pageTags);
    Assert.assertEquals(2, pageTags.length);
    Assert.assertEquals("/etc/tags/default/tagA", pageTags[0].getPath());
    Assert.assertEquals("/etc/tags/wcmio/aem/api", pageTags[1].getPath());

    page = page.listChildren().next();
    pageTags = page.getTags();
    Assert.assertNotNull(pageTags);
    Assert.assertEquals(2, pageTags.length);
    Assert.assertEquals("/etc/tags/default/tagB", pageTags[0].getPath());
    Assert.assertEquals("/etc/tags/wcmio/nondescript", pageTags[1].getPath());

    page = page.listChildren().next();
    pageTags = page.getTags();
    Assert.assertNotNull(pageTags);
    Assert.assertEquals(0, pageTags.length);
  }

  @Test
  public void testGetNamespaces() {
    Tag[] namespaces = tagManager.getNamespaces();
    Assert.assertNotNull(namespaces);
    Assert.assertEquals(2, namespaces.length);
    Assert.assertEquals("/etc/tags/default", namespaces[0].getPath());
    Assert.assertEquals("/etc/tags/wcmio", namespaces[1].getPath());

    Iterator<Tag> namespacesIter = tagManager.getNamespacesIter();
    Assert.assertNotNull(namespacesIter);
    Assert.assertTrue(namespacesIter.hasNext());
    Assert.assertEquals("/etc/tags/default", namespacesIter.next().getPath());
    Assert.assertTrue(namespacesIter.hasNext());
    Assert.assertEquals("/etc/tags/wcmio", namespacesIter.next().getPath());
    Assert.assertFalse(namespacesIter.hasNext());
  }

  @Test
  public void testCanCreateTag() {
    try {
      Assert.assertFalse(tagManager.canCreateTag(TagConstants.DEFAULT_NAMESPACE_ID));
      Assert.assertFalse(tagManager.canCreateTag("wcmio:"));
      Assert.assertTrue(tagManager.canCreateTag("wcmio"));
    } catch (InvalidTagFormatException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testCreateTag() {
    try {
      Tag existing = tagManager.createTag("wcmio:", "foo", "bar");
      Assert.assertNotNull(existing);
      Assert.assertEquals("/etc/tags/wcmio", existing.getPath());
      // verify that it didn't get overwritten
      Assert.assertEquals("WCM IO Tag Namespace", existing.getTitle());
      Assert.assertEquals("The WCM IO namespace for testing tag functionality", existing.getDescription());

      Tag newTag = tagManager.createTag("wcmio", "WCM Tag", "This exists in the default namespace", false);
      Assert.assertNotNull(newTag);
      Assert.assertEquals("/etc/tags/default/wcmio", newTag.getPath());
      Assert.assertEquals("WCM Tag", newTag.getTitle());
      Assert.assertEquals("This exists in the default namespace", newTag.getDescription());
      // verify that it wasn't auto saved
      Assert.assertTrue(resolver.hasChanges());
      context.resourceResolver().commit();

      newTag = tagManager.createTag("wcmio:new", null, null);
      Assert.assertNotNull(newTag);
      Assert.assertEquals("/etc/tags/wcmio/new", newTag.getPath());
      Assert.assertEquals(newTag.getName(), newTag.getTitle());
      Assert.assertNull(newTag.getDescription());
      // verify the auto-save
      Assert.assertFalse(resolver.hasChanges());
    } catch (InvalidTagFormatException | PersistenceException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void setTags() {
    Page page = rootPage;
    Tag[] startTags = page.getTags();

    tagManager.setTags(page.getContentResource(), null, false);
    Tag[] currentTags = page.getTags();
    // prove that it's now empty
    Assert.assertNotNull(currentTags);
    Assert.assertEquals(0, currentTags.length);
    // prove that it wasn't empty to begin with
    Assert.assertNotEquals(startTags.length, currentTags.length);
    // prove that there are pending changed
    Assert.assertTrue(resolver.hasChanges());

    try {
      resolver.commit();
    } catch (PersistenceException e) {
      Assert.fail(e.getMessage());
    }

    tagManager.setTags(page.getContentResource(), startTags);
    currentTags = page.getTags();
    Assert.assertNotNull(currentTags);
    Assert.assertArrayEquals(startTags, currentTags);
    Assert.assertFalse(resolver.hasChanges());
  }

  @Test
  public void testFind() {
    RangeIterator<Resource> resources = tagManager.find("wcmio:");
    Assert.assertNotNull(resources);
    Assert.assertTrue(resources.hasNext());
    Assert.assertEquals(2, resources.getSize());
    Assert.assertEquals(0, resources.getPosition());
    Assert.assertEquals("/content/sample/en/jcr:content", resources.next().getPath());
    Assert.assertTrue(resources.hasNext());
    Assert.assertEquals(1, resources.getPosition());
    Assert.assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    Assert.assertFalse(resources.hasNext());
    Assert.assertEquals(2, resources.getPosition());

    resources = tagManager.find("/content/sample/en/toolbar", new String[]{"wcmio:"});
    Assert.assertNotNull(resources);
    Assert.assertEquals(0, resources.getPosition());
    Assert.assertTrue(resources.hasNext());
    Assert.assertEquals(1, resources.getSize());
    Assert.assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    Assert.assertFalse(resources.hasNext());
    Assert.assertEquals(1, resources.getPosition());

    resources = tagManager.find("/content", new String[]{"wcmio:nondescript", "/etc/tags/default"}, false);
    Assert.assertNotNull(resources);
    Assert.assertEquals(0, resources.getPosition());
    Assert.assertTrue(resources.hasNext());
    Assert.assertEquals(1, resources.getSize());
    Assert.assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    Assert.assertFalse(resources.hasNext());
    Assert.assertEquals(1, resources.getPosition());

    resources = tagManager.find("/content", new String[]{"wcmio:nondescript", "/etc/tags/default"}, true);
    Assert.assertNotNull(resources);
    Assert.assertEquals(0, resources.getPosition());
    Assert.assertTrue(resources.hasNext());
    Assert.assertEquals(2, resources.getSize());
    Assert.assertEquals("/content/sample/en/jcr:content", resources.next().getPath());
    Assert.assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    Assert.assertFalse(resources.hasNext());
    Assert.assertEquals(2, resources.getPosition());

    resources = tagManager.find("/content", new String[]{"wcmio:nondescrip", "/etc/tags/default"}, true);
    Assert.assertNull(resources);
  }

  @Test
  public void testResolve() {
    Tag tag = tagManager.resolve("wcmio:");
    Assert.assertNotNull(tag);
    Assert.assertEquals("wcmio:", tag.getTagID());

    tag = tagManager.resolve("wcmio");
    Assert.assertNull(tag);
  }
}
