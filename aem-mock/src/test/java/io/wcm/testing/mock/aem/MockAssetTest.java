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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.Rendition;
import com.day.cq.wcm.foundation.WCMRenditionPicker;

public class MockAssetTest {

  private static final byte[] BINARY_DATA = new byte[] {
    0x01, 0x02, 0x03, 0x04, 0x05
  };

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private Asset asset;

  @Before
  public void setUp() throws Exception {
    context.load().json("/json-import-samples/dam.json", "/content/dam/sample");

    Resource resource = this.context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg");
    this.asset = resource.adaptTo(Asset.class);
  }

  @Test
  public void testProperties() {
    assertEquals("scott_reynolds.jpg", asset.getName());
    assertEquals("/content/dam/sample/portraits/scott_reynolds.jpg", asset.getPath());
    assertEquals(1368001317000L, asset.getLastModified());
    assertEquals("admin", asset.getModifier());
    assertEquals("image/jpeg", asset.getMimeType());
    assertNotNull(asset.hashCode());
  }

  @Test
  public void testMetadata() {
    assertEquals(807L, asset.getMetadata().get(DamConstants.TIFF_IMAGEWIDTH));
    assertEquals(595L, asset.getMetadata(DamConstants.TIFF_IMAGELENGTH));
    assertEquals("Scott Reynolds", asset.getMetadataValue(DamConstants.DC_TITLE));
  }

  @Test
  public void testRenditions() {
    List<Rendition> renditions = asset.getRenditions();
    assertEquals(4, renditions.size());
    assertTrue(hasRendition(renditions, "cq5dam.thumbnail.48.48.png"));
    assertEquals("original", asset.getOriginal().getName());
    assertEquals("original", asset.getRendition(new WCMRenditionPicker()).getName());
  }

  private boolean hasRendition(List<Rendition> renditions, String renditionName) {
    for (Rendition rendition : renditions) {
      if (StringUtils.equals(rendition.getName(), renditionName)) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void testEquals() throws Exception {
    Asset asset1 = this.context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg").adaptTo(Asset.class);
    Asset asset2 = this.context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg").adaptTo(Asset.class);

    assertTrue(asset1.equals(asset2));
  }

  @Test
  public void testAddRemoveRendition() throws Exception {
    InputStream is = new ByteArrayInputStream(BINARY_DATA);
    Rendition rendition = asset.addRendition("test.bin", is, "application/octet-stream");

    assertNotNull(rendition);
    assertNotNull(asset.getRendition("test.bin"));
    Resource resource = context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg/jcr:content/renditions/test.bin");
    assertNotNull(resource);

    asset.removeRendition("test.bin");

    assertNull(asset.getRendition("test.bin"));
    resource = context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg/jcr:content/renditions/test.bin");
    assertNull(resource);
  }

}
