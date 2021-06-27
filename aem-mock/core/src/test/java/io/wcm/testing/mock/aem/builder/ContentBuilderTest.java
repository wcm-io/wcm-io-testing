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
package io.wcm.testing.mock.aem.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.Rendition;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.image.Layer;
import com.google.common.collect.ImmutableMap;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class ContentBuilderTest {

  private static final String TEMPLATE = "/apps/sample/templates/sample";

  private String contentRoot;
  private String damRoot;

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  @Before
  public void setUp() {
    contentRoot = context.uniqueRoot().content();
    damRoot = context.uniqueRoot().dam();
  }

  @Test
  public void testPage() {
    Page page = context.create().page(contentRoot + "/test1/page1");
    assertNotNull(page);
    assertEquals("page1", page.getName());
    assertEquals(ContentBuilder.DUMMY_TEMPLATE, page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
    assertEquals("page1", page.getTitle());
  }

  @Test
  public void testPageWithTemplate() {
    Page page = context.create().page(contentRoot + "/test1/page1", TEMPLATE);
    assertNotNull(page);
    assertEquals("page1", page.getName());
    assertEquals(TEMPLATE, page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
    assertEquals("page1", page.getTitle());
  }

  @Test
  public void testPageWithTitle() {
    Page page = context.create().page(contentRoot + "/test1/page1/subpage1", TEMPLATE, "Test Title");
    assertNotNull(page);
    assertEquals("subpage1", page.getName());
    assertEquals(TEMPLATE, page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
    assertEquals("Test Title", page.getTitle());
  }

  @Test
  public void testPageWithProperties() {
    Page page = context.create().page(contentRoot + "/test1/page2", TEMPLATE,
        NameConstants.PN_TITLE, "Test Title",
        "stringProp", "value1");
    assertNotNull(page);
    assertEquals("page2", page.getName());
    assertEquals(TEMPLATE, page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
    assertEquals("Test Title", page.getTitle());
    assertEquals("value1", page.getProperties().get("stringProp", String.class));
  }

  @Test
  public void testPage_withParentPage() {
    Page parentPage = context.create().page(contentRoot + "/test1");
    Page page = context.create().page(parentPage, "page1");
    assertNotNull(page);
    assertEquals("page1", page.getName());
    assertEquals(ContentBuilder.DUMMY_TEMPLATE, page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
    assertEquals("page1", page.getTitle());
  }

  @Test
  public void testPageWithTemplate_withParentPage() {
    Page parentPage = context.create().page(contentRoot + "/test1");
    Page page = context.create().page(parentPage, "page1", TEMPLATE);
    assertNotNull(page);
    assertEquals("page1", page.getName());
    assertEquals(TEMPLATE, page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
    assertEquals("page1", page.getTitle());
  }

  @Test
  public void testPageWithTitle_withParentPage() {
    Page parentPage = context.create().page(contentRoot + "/test1");
    Page page = context.create().page(parentPage, "page1/subpage1", TEMPLATE, "Test Title");
    assertNotNull(page);
    assertEquals("subpage1", page.getName());
    assertEquals(TEMPLATE, page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
    assertEquals("Test Title", page.getTitle());
  }

  @Test
  public void testPageWithProperties_withParentPage() {
    Page parentPage = context.create().page(contentRoot + "/test1");
    Page page = context.create().page(parentPage, "page2", TEMPLATE,
        NameConstants.PN_TITLE, "Test Title",
        "stringProp", "value1");
    assertNotNull(page);
    assertEquals("page2", page.getName());
    assertEquals(TEMPLATE, page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
    assertEquals("Test Title", page.getTitle());
    assertEquals("value1", page.getProperties().get("stringProp", String.class));
  }

  @Test
  @SuppressWarnings("unlikely-arg-type")
  public void testResource() {
    Resource resource = context.create().resource(contentRoot + "/test1/resource1");
    assertNotNull(resource);
    assertEquals("resource1", resource.getName());
    assertTrue(resource.getValueMap().isEmpty()
        || ImmutableMap.<String, Object>of(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED).equals(resource.getValueMap()));
  }

  @Test
  public void testResourceWithProperties() {
    Resource resource = context.create().resource(contentRoot + "/test1/resource2",
        NameConstants.PN_TITLE, "Test Title",
        "stringProp", "value1");
    assertNotNull(resource);
    assertEquals("resource2", resource.getName());
    assertEquals("Test Title", resource.getValueMap().get(NameConstants.PN_TITLE, String.class));
    assertEquals("value1", resource.getValueMap().get("stringProp", String.class));
  }

  @Test
  public void testAssetFromClasspath() throws Exception {
    Asset asset = context.create().asset(damRoot + "/sample1.gif", "/sample-image.gif", "image/gif");
    assertNotNull(asset);

    assertEquals(1, asset.getRenditions().size());
    assertEquals("sample1.gif", asset.getName());
    assertEquals("image/gif", asset.getOriginal().getMimeType());
    assertEquals("2", asset.getMetadataValue(DamConstants.TIFF_IMAGEWIDTH));
    assertEquals("2", asset.getMetadataValue(DamConstants.TIFF_IMAGELENGTH));

    try (InputStream is = asset.getOriginal().adaptTo(InputStream.class)) {
      Layer layer = new Layer(is);
      assertEquals(2, layer.getWidth());
      assertEquals(2, layer.getHeight());
    }

    Rendition rendition = context.create().assetRendition(asset, "sample2.gif", "/sample-image.gif", "image/gif");
    assertEquals("sample2.gif", rendition.getName());
    assertEquals("image/gif", rendition.getMimeType());
    assertEquals(2, asset.getRenditions().size());
  }

  @Test
  public void testAssetFromByteArray() throws Exception {
    Asset asset;
    try (InputStream is = new ByteArrayInputStream(new byte[] {
        0x01, 0x02, 0x03
    })) {
      asset = context.create().asset(damRoot + "/sample1.bin", is, "application/octet-stream");
      assertNotNull(asset);

      assertEquals(1, asset.getRenditions().size());
      assertEquals("sample1.bin", asset.getName());
      assertEquals("application/octet-stream", asset.getOriginal().getMimeType());
    }

    try (InputStream is = new ByteArrayInputStream(new byte[] {
        0x04, 0x05, 0x06
    })) {
      Rendition rendition = context.create().assetRendition(asset, "sample2.bin", is, "application/octet-stream");
      assertEquals("sample2.bin", rendition.getName());
      assertEquals("application/octet-stream", rendition.getMimeType());
      assertEquals(2, asset.getRenditions().size());
    }
  }

  @Test
  public void testAssetFromWidthHeight_Jpeg() throws Exception {
    Asset asset = context.create().asset(damRoot + "/sample1.jpg", 100, 50, "image/jpeg");
    assertNotNull(asset);

    assertEquals(1, asset.getRenditions().size());
    assertEquals("sample1.jpg", asset.getName());
    assertEquals("image/jpeg", asset.getOriginal().getMimeType());
    assertEquals("100", asset.getMetadataValue(DamConstants.TIFF_IMAGEWIDTH));
    assertEquals("50", asset.getMetadataValue(DamConstants.TIFF_IMAGELENGTH));

    Rendition rendition = context.create().assetRendition(asset, "sample2.jpg", 20, 20, "image/jpeg");
    assertEquals("sample2.jpg", rendition.getName());
    assertEquals("image/jpeg", rendition.getMimeType());
    assertEquals(2, asset.getRenditions().size());

    Rendition webEnabledRendition = context.create().assetRenditionWebEnabled(asset);
    assertEquals("cq5dam.web.1280.1280.jpg", webEnabledRendition.getName());
    assertEquals("image/jpeg", webEnabledRendition.getMimeType());
    assertEquals(3, asset.getRenditions().size());

    assertRatio(asset.getOriginal(), webEnabledRendition);
  }

  @Test
  public void testAssetFromWidthHeight_Jpeg_BigImage_WebEnabled_Ratio1() throws Exception {
    Asset asset = context.create().asset(damRoot + "/sample1.jpg", 2000, 1000, "image/jpeg");
    assertNotNull(asset);

    assertEquals("2000", asset.getMetadataValue(DamConstants.TIFF_IMAGEWIDTH));
    assertEquals("1000", asset.getMetadataValue(DamConstants.TIFF_IMAGELENGTH));

    Rendition webEnabledRendition = context.create().assetRenditionWebEnabled(asset);
    assertEquals("cq5dam.web.1280.1280.jpg", webEnabledRendition.getName());

    assertRatio(asset.getOriginal(), webEnabledRendition);
  }

  @Test
  public void testAssetFromWidthHeight_Jpeg_BigImage_WebEnabled_Ratio2() throws Exception {
    Asset asset = context.create().asset(damRoot + "/sample1.jpg", 1000, 2000, "image/jpeg");
    assertNotNull(asset);

    assertEquals("1000", asset.getMetadataValue(DamConstants.TIFF_IMAGEWIDTH));
    assertEquals("2000", asset.getMetadataValue(DamConstants.TIFF_IMAGELENGTH));

    Rendition webEnabledRendition = context.create().assetRenditionWebEnabled(asset);
    assertEquals("cq5dam.web.1280.1280.jpg", webEnabledRendition.getName());

    assertRatio(asset.getOriginal(), webEnabledRendition);
  }

  @Test
  public void testAssetFromWidthHeight_Gif() throws Exception {
    Asset asset = context.create().asset(damRoot + "/sample1.gif", 100, 50, "image/gif");
    assertNotNull(asset);

    assertEquals(1, asset.getRenditions().size());
    assertEquals("sample1.gif", asset.getName());
    assertEquals("image/gif", asset.getOriginal().getMimeType());
    assertEquals("100", asset.getMetadataValue(DamConstants.TIFF_IMAGEWIDTH));
    assertEquals("50", asset.getMetadataValue(DamConstants.TIFF_IMAGELENGTH));

    Rendition rendition = context.create().assetRendition(asset, "sample2.gif", 20, 20, "image/gif");
    assertEquals("sample2.gif", rendition.getName());
    assertEquals("image/gif", rendition.getMimeType());
    assertEquals(2, asset.getRenditions().size());
  }

  @Test
  public void testAssetFromWidthHeight_Tiff() throws Exception {
    Asset asset = context.create().asset(damRoot + "/sample1.tif", 100, 50, "image/tiff");
    assertNotNull(asset);

    assertEquals(1, asset.getRenditions().size());
    assertEquals("sample1.tif", asset.getName());
    assertEquals("image/tiff", asset.getOriginal().getMimeType());
    assertEquals("100", asset.getMetadataValue(DamConstants.TIFF_IMAGEWIDTH));
    assertEquals("50", asset.getMetadataValue(DamConstants.TIFF_IMAGELENGTH));

    Rendition rendition = context.create().assetRendition(asset, "sample2.tif", 20, 20, "image/tiff");
    assertEquals("sample2.tif", rendition.getName());
    assertEquals("image/tiff", rendition.getMimeType());
    assertEquals(2, asset.getRenditions().size());
  }

  @Test
  public void testAssetFromWidthHeight_SVG() throws Exception {
    Asset asset = context.create().asset(damRoot + "/sample1.svg", 100, 50, "image/svg+xml");
    assertNotNull(asset);

    assertEquals(1, asset.getRenditions().size());
    assertEquals("sample1.svg", asset.getName());
    assertEquals("image/svg+xml", asset.getOriginal().getMimeType());
    assertEquals("100", asset.getMetadataValue(DamConstants.TIFF_IMAGEWIDTH));
    assertEquals("50", asset.getMetadataValue(DamConstants.TIFF_IMAGELENGTH));

    Rendition rendition = context.create().assetRendition(asset, "sample2.svg", 20, 20, "image/svg+xml");
    assertEquals("sample2.svg", rendition.getName());
    assertEquals("image/svg+xml", rendition.getMimeType());
    assertEquals(2, asset.getRenditions().size());
  }

  @Test
  public void testAssetWithMetadata() throws Exception {
    Asset asset = context.create().asset(damRoot + "/sample1.jpg", 100, 50, "image/jpeg",
        "prop1", "value1", "prop2", 1);
    assertNotNull(asset);

    assertEquals(1, asset.getRenditions().size());
    assertEquals("sample1.jpg", asset.getName());
    assertEquals("image/jpeg", asset.getOriginal().getMimeType());
    assertEquals("100", asset.getMetadataValue(DamConstants.TIFF_IMAGEWIDTH));
    assertEquals("50", asset.getMetadataValue(DamConstants.TIFF_IMAGELENGTH));

    Rendition rendition = context.create().assetRendition(asset, "sample2.jpg", 20, 20, "image/jpeg");
    assertEquals("sample2.jpg", rendition.getName());
    assertEquals("image/jpeg", rendition.getMimeType());
    assertEquals(2, asset.getRenditions().size());

    assertEquals("value1", asset.getMetadata("prop1"));
    assertEquals("1", asset.getMetadataValue("prop2"));
  }

  @Test
  public void testTag() {
    Tag tag1 = context.create().tag("test:tag1");
    Tag tag2 = context.create().tag("test:tag1/tag2");

    assertEquals("test:tag1", tag1.getTagID());
    assertEquals("test:tag1/tag2", tag2.getTagID());

    assertEquals("tag1", tag1.getName());
    assertEquals("tag2", tag2.getName());
  }

  @Test
  public void testResourceInPage() {
    Page page = context.create().page(contentRoot + "/test1/page1");

    Resource resource1 = context.create().resource(page, "test1");
    assertNotNull(resource1);
    assertEquals(contentRoot + "/test1/page1/jcr:content/test1", resource1.getPath());

    Resource resource2 = context.create().resource(page, "/test2/test21",
        "prop1", "value1");
    assertNotNull(resource2);
    assertEquals(contentRoot + "/test1/page1/jcr:content/test2/test21", resource2.getPath());
    assertEquals("value1", resource2.getValueMap().get("prop1", String.class));
  }

  @SuppressWarnings("null")
  private void assertRatio(Rendition expected, Rendition actual) {
    Layer expectedLayer = expected.adaptTo(Resource.class).adaptTo(Layer.class);
    Layer actualLayer = actual.adaptTo(Resource.class).adaptTo(Layer.class);
    assertEquals("Ratio does not match",
        expectedLayer.getWidth() / expectedLayer.getHeight(),
        actualLayer.getWidth() / actualLayer.getHeight(), 0.0001d);
  }

}
