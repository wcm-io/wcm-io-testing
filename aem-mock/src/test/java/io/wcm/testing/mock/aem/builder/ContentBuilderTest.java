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
import io.wcm.testing.mock.aem.junit.AemContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.image.Layer;
import com.google.common.collect.ImmutableMap;

public class ContentBuilderTest {

  private static final String TEMPLATE = "/apps/sample/templates/sample";

  @Rule
  public AemContext context = new AemContext(
      ResourceResolverType.JCR_MOCK,
      ResourceResolverType.RESOURCERESOLVER_MOCK
      );

  @Test
  public void testPage() {
    Page page = context.create().page("/content/test1/page1");
    assertNotNull(page);
    assertEquals("page1", page.getName());
    assertEquals(ContentBuilder.DUMMY_TEMPLATE, page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
    assertEquals("page1", page.getTitle());
  }

  @Test
  public void testPageWithTemplate() {
    Page page = context.create().page("/content/test1/page1", TEMPLATE);
    assertNotNull(page);
    assertEquals("page1", page.getName());
    assertEquals(TEMPLATE, page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
    assertEquals("page1", page.getTitle());
  }

  @Test
  public void testPageWithTitle() {
    Page page = context.create().page("/content/test1/page1/subpage1", TEMPLATE, "Test Title");
    assertNotNull(page);
    assertEquals("subpage1", page.getName());
    assertEquals(TEMPLATE, page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
    assertEquals("Test Title", page.getTitle());
  }

  @Test
  public void testPageWithProperties() {
    Page page = context.create().page("/content/test1/page2", TEMPLATE, ImmutableMap.<String, Object>builder()
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
  public void testResource() {
    Resource resource = context.create().resource("/content/test1/resource1");
    assertNotNull(resource);
    assertEquals("resource1", resource.getName());
    assertTrue(resource.getValueMap().isEmpty()
        || ImmutableMap.<String, Object>of(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED).equals(resource.getValueMap()));
  }

  @Test
  public void testResourceWithProperties() {
    Resource resource = context.create().resource("/content/test1/resource2", ImmutableMap.<String, Object>builder()
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
    Asset asset = context.create().asset("/content/dam/sample1.gif", "/sample-image.gif", "image/gif");
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
  }

  @Test
  public void testAssetFromByteArray() throws Exception {
    try (InputStream is = new ByteArrayInputStream(new byte[] {
        0x01, 0x02, 0x03
    })) {
      Asset asset = context.create().asset("/content/dam/sample1.bin", is, "application/octet-stream");
      assertNotNull(asset);

      assertEquals(1, asset.getRenditions().size());
      assertEquals("sample1.bin", asset.getName());
      assertEquals("application/octet-stream", asset.getOriginal().getMimeType());
    }
  }

  @Test
  public void testAssetFromWidthHeight_Jpeg() throws Exception {
    Asset asset = context.create().asset("/content/dam/sample1.jpg", 100, 50, "image/jpeg");
    assertNotNull(asset);

    assertEquals(1, asset.getRenditions().size());
    assertEquals("sample1.jpg", asset.getName());
    assertEquals("image/jpeg", asset.getOriginal().getMimeType());
    assertEquals("100", asset.getMetadataValue(DamConstants.TIFF_IMAGEWIDTH));
    assertEquals("50", asset.getMetadataValue(DamConstants.TIFF_IMAGELENGTH));
  }

  @Test
  public void testAssetFromWidthHeight_Gif() throws Exception {
    Asset asset = context.create().asset("/content/dam/sample1.gif", 100, 50, "image/gif");
    assertNotNull(asset);

    assertEquals(1, asset.getRenditions().size());
    assertEquals("sample1.gif", asset.getName());
    assertEquals("image/gif", asset.getOriginal().getMimeType());
    assertEquals("100", asset.getMetadataValue(DamConstants.TIFF_IMAGEWIDTH));
    assertEquals("50", asset.getMetadataValue(DamConstants.TIFF_IMAGELENGTH));
  }

}
