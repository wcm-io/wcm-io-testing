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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.Filter;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagConstants;
import com.day.cq.tagging.TagManager;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockTagTest {
  private static final String XPATH_PROPERTY = "@p";
  private static final String XPATH_PROPERTY_IN_JCR_CONTENT = "jcr:content/@p";
  private static final String XPATH_PROPERTY_IN_SUB_SUB_NODE = "jcr:content/test/@p";
  private static final Map<String, String> TAG_PROPERTY_CONVERSIONS = ImmutableMap.<String, String>builder()
      .put(XPATH_PROPERTY_IN_JCR_CONTENT, XPATH_PROPERTY_IN_JCR_CONTENT)
      .put("jcr:content/p", XPATH_PROPERTY_IN_JCR_CONTENT)
      .put("@jcr:content/p", XPATH_PROPERTY_IN_JCR_CONTENT)
      .put("p", XPATH_PROPERTY)
      .put("@p", XPATH_PROPERTY)
      .put(XPATH_PROPERTY_IN_SUB_SUB_NODE, XPATH_PROPERTY_IN_SUB_SUB_NODE)
      .put("@jcr:content/test/p", XPATH_PROPERTY_IN_SUB_SUB_NODE)
      .put("@jcr:content/@test/p", "jcr:content/_x0040_test/@p")
      .put("123", "@_x0031_23")
      .build();

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private String tagRoot;
  private ResourceResolver resolver;
  private TagManager tagManager;

  private Tag wcmio;
  private Tag aem;
  private Tag aemApi;
  private Tag nondescript;
  private Tag nondescript2;

  private static final Locale LOCALE_PT_BR = new Locale("pt", "br");

  @Before
  public void setUp() throws Exception {
    tagRoot = MockTagManager.getTagRootPath();
    resolver = context.resourceResolver();
    context.load().json("/json-import-samples/tags.json", tagRoot);
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
    assertNotNull(namespaces);
    for (Tag namespace : namespaces) {
      assertTrue(namespace.isNamespace());
      assertNull(namespace.getParent());
      assertEquals("", namespace.getLocalTagID());
      assertEquals(namespace.getName() + TagConstants.NAMESPACE_DELIMITER, namespace.getTagID());
      assertEquals(namespace, namespace.getNamespace());
      assertEquals(tagRoot + "/" + namespace.getName(), namespace.getPath());
    }
  }

  @Test
  public void testListChildren() {
    assertNotNull(wcmio);
    Iterator<Tag> children = wcmio.listChildren();
    assertNotNull(children);
    int childCount = 0;
    while (children.hasNext()) {
      Tag childTag = children.next();
      assertNotNull(childTag);
      assertFalse(childTag.isNamespace());
      assertEquals(wcmio, childTag.getNamespace());
      assertEquals(wcmio, childTag.getParent());
      assertEquals(wcmio.getPath() + "/" + childTag.getName(), childTag.getPath());
      assertEquals(wcmio.getTagID() + childTag.getName(), childTag.getTagID());
      assertEquals(childTag.getName(), childTag.getLocalTagID());
      ++childCount;
    }
    assertEquals(3, childCount);

    assertNotNull(aem);
    children = aem.listChildren();
    assertNotNull(children);
    childCount = 0;
    while (children.hasNext()) {
      Tag childTag = children.next();
      assertNotNull(childTag);
      assertFalse(childTag.isNamespace());
      assertEquals(aem.getNamespace(), childTag.getNamespace());
      assertEquals(aem, childTag.getParent());
      assertEquals(aem.getPath() + "/" + childTag.getName(), childTag.getPath());
      assertEquals(aem.getTagID() + "/" + childTag.getName(), childTag.getTagID());
      assertEquals(aem.getName() + "/" + childTag.getName(), childTag.getLocalTagID());
      ++childCount;
    }
    assertEquals(2, childCount);

    children = aem.listChildren(new NothingFilter());
    assertNotNull(children);
    assertFalse(children.hasNext());

    children = wcmio.listAllSubTags();
    assertNotNull(children);
    childCount = 0;
    while (children.hasNext()) {
      Tag childTag = children.next();
      assertNotNull(childTag);
      assertFalse(childTag.isNamespace());
      assertNotNull(childTag.getParent());
      assertEquals(wcmio, childTag.getNamespace());
      Resource childTagResource = childTag.adaptTo(Resource.class);
      assertNotNull(childTagResource);
      assertEquals(childTagResource.getName(), childTag.getName());
      assertTrue(childTag.getPath().startsWith(wcmio.getPath() + "/"));
      assertTrue(childTag.getTagID().startsWith(wcmio.getTagID()));
      ++childCount;
    }
    assertEquals(6, childCount);

  }

  @Test
  public void testLocalizedTitles() {
    assertNull(wcmio.getLocalizedTitle(null));
    assertNull(wcmio.getLocalizedTitle(Locale.ENGLISH));
    Map<Locale, String> localizedTitles = wcmio.getLocalizedTitles();
    assertNotNull(localizedTitles);
    assertEquals(0, localizedTitles.size());

    assertNull(aem.getLocalizedTitle(null));
    assertNull(aem.getLocalizedTitle(Locale.ENGLISH));
    localizedTitles = aem.getLocalizedTitles();
    assertNotNull(localizedTitles);
    assertEquals(0, localizedTitles.size());

    assertNull(aemApi.getLocalizedTitle(null));
    assertEquals("English AEM API", aemApi.getLocalizedTitle(Locale.ENGLISH));
    assertEquals("AEM API for US", aemApi.getLocalizedTitle(Locale.US));
    assertEquals("German AEM API", aemApi.getLocalizedTitle(Locale.GERMAN));
    assertEquals("AEM API for Germany", aemApi.getLocalizedTitle(Locale.GERMANY));
    assertEquals("Japanese AEM API", aemApi.getLocalizedTitle(Locale.JAPANESE));
    assertEquals("Japanese AEM API", aemApi.getLocalizedTitle(Locale.JAPAN));
    assertEquals("Portuguese (Brazil) AEM API  with special chars", aemApi.getLocalizedTitle(LOCALE_PT_BR));
    localizedTitles = aemApi.getLocalizedTitles();
    assertNotNull(localizedTitles);
    assertEquals(6, localizedTitles.size());
    assertEquals("English AEM API", localizedTitles.get(Locale.ENGLISH));
    assertEquals("AEM API for US", localizedTitles.get(Locale.US));
    assertEquals("German AEM API", localizedTitles.get(Locale.GERMAN));
    assertEquals("AEM API for Germany", localizedTitles.get(Locale.GERMANY));
    assertEquals("Japanese AEM API", localizedTitles.get(Locale.JAPANESE));
    assertEquals("Portuguese (Brazil) AEM API/:with special chars", localizedTitles.get(LOCALE_PT_BR));
    assertEquals(null, localizedTitles.get(Locale.JAPAN));

  }

  @Test
  public void testTitle() {
    assertEquals("AEM", aem.getTitle());
    assertEquals("AEM", aem.getTitle(Locale.ENGLISH));
    assertEquals("AEM", aem.getTitle(Locale.US));

    assertEquals("AEM API", aemApi.getTitle());
    assertEquals("English AEM API", aemApi.getTitle(Locale.ENGLISH));
    assertEquals("AEM API for US", aemApi.getTitle(Locale.US));
  }

  @Test
  public void testLastModified() {
    assertEquals(1282593744000L, wcmio.getLastModified());
    assertEquals(1282672944000L, aem.getLastModified());
    assertEquals(0, aemApi.getLastModified());
  }

  @Test
  public void testLastModifiedBy() {
    assertEquals("admin", wcmio.getLastModifiedBy());
    assertEquals("wcmio", aem.getLastModifiedBy());
    assertEquals(null, aemApi.getLastModifiedBy());
  }

  @Test
  public void testDescription() {
    assertEquals("The WCM IO namespace for testing tag functionality", wcmio.getDescription());
    assertEquals("Tag representing AEM", aem.getDescription());
    assertEquals(null, nondescript.getDescription());
  }

  @Test
  public void testCount() {
    assertEquals(2, wcmio.getCount());
    assertEquals(1, aem.getCount());
    assertEquals(1, nondescript.getCount());
    assertEquals(0, nondescript2.getCount());
  }

  @Test
  public void testFind() {
    Iterator<Resource> resources = wcmio.find();
    assertNotNull(resources);
    assertTrue(resources.hasNext());
    assertEquals("/content/sample/en/jcr:content", resources.next().getPath());
    assertTrue(resources.hasNext());
    assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    assertFalse(resources.hasNext());

    resources = aem.find();
    assertNotNull(resources);
    assertTrue(resources.hasNext());
    assertEquals("/content/sample/en/jcr:content", resources.next().getPath());
    assertFalse(resources.hasNext());

    resources = nondescript.find();
    assertNotNull(resources);
    assertTrue(resources.hasNext());
    assertEquals("/content/sample/en/toolbar/jcr:content", resources.next().getPath());
    assertFalse(resources.hasNext());

    resources = nondescript2.find();
    assertNotNull(resources);
    assertFalse(resources.hasNext());
  }

  @Test
  public void testGetXPathSearchExpression() throws Exception {
    Tag tag = tagManager.createTag("test:tag1", "Tag 1", null);
    assertTagExpression(tag, "test:tag1", "test/tag1");
  }

  @Test
  public void testGetXPathSearchExpression_2Level() throws Exception {
    Tag tag = tagManager.createTag("test:tag1/tag2", "Tag 2", null);
    assertTagExpression(tag, "test:tag1/tag2", "test/tag1/tag2");
  }

  @Test
  public void testGetXPathSearchExpression_DefaultNamespace() throws Exception {
    Tag tag = tagManager.createTag("tag3", "Tag 3", null);
    assertTagExpression(tag, "tag3", "default/tag3");
  }

  private void assertTagExpression(@NotNull final Tag tag,
                                   @NotNull final String tagId,
                                   @NotNull final String tagPath) {
    for (final Map.Entry<String, String> entry : TAG_PROPERTY_CONVERSIONS.entrySet()) {
      final String property = entry.getKey();
      final String expectedXpathProperty = entry.getValue();
      assertEquals(
          "(" + expectedXpathProperty + "='" + tagId + "'" +
              " or " + expectedXpathProperty + "='" + tagRoot + "/" + tagPath + "'" +
              " or jcr:like(" + expectedXpathProperty + ", '" + tagId + "/%')" +
              " or jcr:like(" + expectedXpathProperty + ", '" + tagRoot + "/" + tagPath + "/%'" +
              "))",
          tag.getXPathSearchExpression(property)
      );
    }
  }

  private static class NothingFilter implements Filter<Tag> {
    @Override
    public boolean includes(Tag tag) {
      return false;
    }
  }

}
