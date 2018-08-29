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
import java.util.Map;

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
    Page page = context.create().page(contentRoot + "/test1/page2", TEMPLATE, ImmutableMap.<String, Object>builder()
        .put(NameConstants.PN_TITLE, "Test Title")
        .put("stringProp", "value1")
        .build());
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
    Resource resource = context.create().resource(contentRoot + "/test1/resource2", ImmutableMap.<String, Object>builder()
        .put(NameConstants.PN_TITLE, "Test Title")
        .put("stringProp", "value1")
        .build());
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
  public void testAssetWithMetadata() throws Exception {
    Map<String, Object> metadata = ImmutableMap.of("prop1", "value1", "prop2", 1);
    Asset asset = context.create().asset(damRoot + "/sample1.jpg", 100, 50, "image/jpeg", metadata);
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

}
