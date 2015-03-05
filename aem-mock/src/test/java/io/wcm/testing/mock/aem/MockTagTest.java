package io.wcm.testing.mock.aem;

import io.wcm.testing.mock.aem.junit.AemContext;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.Filter;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagConstants;
import com.day.cq.tagging.TagManager;

public class MockTagTest {

  private static class NothingFilter implements Filter<Tag> {
    @Override
    public boolean includes(Tag tag) {
      return false;
    }
  }

  @Rule
  public AemContext context = new AemContext();

  private ResourceResolver resolver;

  private TagManager tagManager;

  private Tag wcmio;
  private Tag aem;
  private Tag aemApi;
  private Tag nondescript;
  private Tag nondescript2;

  @Before
  public void setUp() throws Exception {
    resolver = context.resourceResolver();
    context.load().json("/json-import-samples/tags.json", "/etc/tags");
    context.load().json("/json-import-samples/content.json", "/content/sample/en");
    resolver.commit();

    tagManager = resolver.adaptTo(TagManager.class);
    wcmio = tagManager.resolve("wcmio:");
    aem = tagManager.resolve("wcmio:aem");
    aemApi = tagManager.resolve("wcmio:aem/api");
    nondescript = tagManager.resolve("wcmio:nondescript");
    nondescript2 = tagManager.resolve("wcmio:nondescript/nondescript2");
  }

  @Test
  public void testNamespaces() {
    Tag[] namespaces = tagManager.getNamespaces();
    Assert.assertNotNull(namespaces);
    for (Tag namespace : namespaces) {
      Assert.assertTrue(namespace.isNamespace());
      Assert.assertNull(namespace.getParent());
      Assert.assertEquals("", namespace.getLocalTagID());
      Assert.assertEquals(namespace.getName() + TagConstants.NAMESPACE_DELIMITER, namespace.getTagID());
      Assert.assertEquals(namespace, namespace.getNamespace());
      Assert.assertEquals("/etc/tags/" + namespace.getName(), namespace.getPath());
    }
  }

  @Test
  public void testListChildren() {
    Assert.assertNotNull(wcmio);
    Iterator<Tag> children = wcmio.listChildren();
    Assert.assertNotNull(children);
    int childCount = 0;
    while (children.hasNext()) {
      Tag childTag = children.next();
      Assert.assertNotNull(childTag);
      Assert.assertFalse(childTag.isNamespace());
      Assert.assertEquals(wcmio, childTag.getNamespace());
      Assert.assertEquals(wcmio, childTag.getParent());
      Assert.assertEquals(wcmio.getPath() + "/" + childTag.getName(), childTag.getPath());
      Assert.assertEquals(wcmio.getTagID() + childTag.getName(), childTag.getTagID());
      Assert.assertEquals(childTag.getName(), childTag.getLocalTagID());
      ++childCount;
    }
    Assert.assertEquals(3, childCount);

    Assert.assertNotNull(aem);
    children = aem.listChildren();
    Assert.assertNotNull(children);
    childCount = 0;
    while (children.hasNext()) {
      Tag childTag = children.next();
      Assert.assertNotNull(childTag);
      Assert.assertFalse(childTag.isNamespace());
      Assert.assertEquals(aem.getNamespace(), childTag.getNamespace());
      Assert.assertEquals(aem, childTag.getParent());
      Assert.assertEquals(aem.getPath() + "/" + childTag.getName(), childTag.getPath());
      Assert.assertEquals(aem.getTagID() + "/" + childTag.getName(), childTag.getTagID());
      Assert.assertEquals(aem.getName() + "/" + childTag.getName(), childTag.getLocalTagID());
      ++childCount;
    }
    Assert.assertEquals(2, childCount);

    children = aem.listChildren(new NothingFilter());
    Assert.assertNotNull(children);
    Assert.assertFalse(children.hasNext());

    children = wcmio.listAllSubTags();
    Assert.assertNotNull(children);
    childCount = 0;
    while (children.hasNext()) {
      Tag childTag = children.next();
      Assert.assertNotNull(childTag);
      Assert.assertFalse(childTag.isNamespace());
      Assert.assertNotNull(childTag.getParent());
      Assert.assertEquals(wcmio, childTag.getNamespace());
      Resource childTagResource = childTag.adaptTo(Resource.class);
      Assert.assertNotNull(childTagResource);
      Assert.assertEquals(childTagResource.getName(), childTag.getName());
      Assert.assertTrue(childTag.getPath().startsWith(wcmio.getPath() + "/"));
      Assert.assertTrue(childTag.getTagID().startsWith(wcmio.getTagID()));
      ++childCount;
    }
    Assert.assertEquals(6, childCount);

  }

  @Test
  public void testLocalizedTitles() {
    Assert.assertNull(wcmio.getLocalizedTitle(null));
    Assert.assertNull(wcmio.getLocalizedTitle(Locale.ENGLISH));
    Map<Locale, String> localizedTitles = wcmio.getLocalizedTitles();
    Assert.assertNotNull(localizedTitles);
    Assert.assertEquals(0, localizedTitles.size());

    Assert.assertNull(aem.getLocalizedTitle(null));
    Assert.assertNull(aem.getLocalizedTitle(Locale.ENGLISH));
    localizedTitles = aem.getLocalizedTitles();
    Assert.assertNotNull(localizedTitles);
    Assert.assertEquals(0, localizedTitles.size());

    Assert.assertNull(aemApi.getLocalizedTitle(null));
    Assert.assertEquals("English AEM API", aemApi.getLocalizedTitle(Locale.ENGLISH));
    Assert.assertEquals("AEM API for US", aemApi.getLocalizedTitle(Locale.US));
    Assert.assertEquals("German AEM API", aemApi.getLocalizedTitle(Locale.GERMAN));
    Assert.assertEquals("AEM API for Germany", aemApi.getLocalizedTitle(Locale.GERMANY));
    Assert.assertEquals("Japanese AEM API", aemApi.getLocalizedTitle(Locale.JAPANESE));
    Assert.assertEquals("Japanese AEM API", aemApi.getLocalizedTitle(Locale.JAPAN));
    localizedTitles = aemApi.getLocalizedTitles();
    Assert.assertNotNull(localizedTitles);
    Assert.assertEquals(5, localizedTitles.size());
    Assert.assertEquals("English AEM API", localizedTitles.get(Locale.ENGLISH));
    Assert.assertEquals("AEM API for US", localizedTitles.get(Locale.US));
    Assert.assertEquals("German AEM API", localizedTitles.get(Locale.GERMAN));
    Assert.assertEquals("AEM API for Germany", localizedTitles.get(Locale.GERMANY));
    Assert.assertEquals("Japanese AEM API", localizedTitles.get(Locale.JAPANESE));
    Assert.assertEquals(null, localizedTitles.get(Locale.JAPAN));

  }

  @Test
  public void testTitle() {
    Assert.assertEquals("AEM", aem.getTitle());
    Assert.assertEquals("AEM", aem.getTitle(Locale.ENGLISH));
    Assert.assertEquals("AEM", aem.getTitle(Locale.US));

    Assert.assertEquals("AEM API", aemApi.getTitle());
    Assert.assertEquals("English AEM API", aemApi.getTitle(Locale.ENGLISH));
    Assert.assertEquals("AEM API for US", aemApi.getTitle(Locale.US));
  }

  @Test
  public void testLastModified() {
    Assert.assertEquals(1282593744000l, wcmio.getLastModified());
    Assert.assertEquals(1282672944000l, aem.getLastModified());
    Assert.assertEquals(0, aemApi.getLastModified());
  }

  @Test
  public void testLastModifiedBy() {
    Assert.assertEquals("admin", wcmio.getLastModifiedBy());
    Assert.assertEquals("wcmio", aem.getLastModifiedBy());
    Assert.assertEquals(null, aemApi.getLastModifiedBy());
  }

  @Test
  public void testDescription() {
    Assert.assertEquals("The WCM IO namespace for testing tag functionality", wcmio.getDescription());
    Assert.assertEquals("Tag representing AEM", aem.getDescription());
    Assert.assertEquals(null, nondescript.getDescription());
  }

  @Test
  public void testCount() {
    Assert.assertEquals(2, wcmio.getCount());
    Assert.assertEquals(1, aem.getCount());
    Assert.assertEquals(1, nondescript.getCount());
    Assert.assertEquals(0, nondescript2.getCount());
  }

  @Test
  public void testFind() {
    Iterator<Resource> resources = wcmio.find();
    Assert.assertNotNull(resources);
    Assert.assertTrue(resources.hasNext());
    Assert.assertEquals("/content/sample/en/jcr:content", resources.next().getPath());
    Assert.assertTrue(resources.hasNext());
    Assert.assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    Assert.assertFalse(resources.hasNext());

    resources = aem.find();
    Assert.assertNotNull(resources);
    Assert.assertTrue(resources.hasNext());
    Assert.assertEquals("/content/sample/en/jcr:content", resources.next().getPath());
    Assert.assertFalse(resources.hasNext());

    resources = nondescript.find();
    Assert.assertNotNull(resources);
    Assert.assertTrue(resources.hasNext());
    Assert.assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    Assert.assertFalse(resources.hasNext());

    resources = nondescript2.find();
    Assert.assertNotNull(resources);
    Assert.assertFalse(resources.hasNext());
  }
}
