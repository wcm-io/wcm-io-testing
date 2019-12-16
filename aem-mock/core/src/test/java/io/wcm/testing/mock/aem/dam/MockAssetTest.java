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
package io.wcm.testing.mock.aem.dam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.service.event.EventHandler;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.DamEvent;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.UIHelper;
import com.day.cq.wcm.foundation.WCMRenditionPicker;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.dam.MockAssetManagerTest.DamEventHandler;
import io.wcm.testing.mock.aem.junit.AemContext;

@SuppressWarnings("null")
public class MockAssetTest {

  private static final byte[] BINARY_DATA = new byte[] {
      0x01, 0x02, 0x03, 0x04, 0x05
  };

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private Asset asset;
  private DamEventHandler damEventHandler;

  @Before
  public void setUp() throws Exception {
    context.load().json("/json-import-samples/dam.json", "/content/dam/sample");

    Resource resource = this.context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg");
    this.asset = resource.adaptTo(Asset.class);

    damEventHandler = (DamEventHandler)context.registerService(EventHandler.class, new DamEventHandler());
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
    assertEquals("Scott Reynolds", asset.getMetadataValueFromJcr(DamConstants.DC_TITLE));
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

  private void doTestAddRemoveRendition(final String renditionName) {
    InputStream is = new ByteArrayInputStream(BINARY_DATA);
    Rendition rendition = asset.addRendition(renditionName, is, "application/octet-stream");

    assertNotNull(rendition);
    assertNotNull(asset.getRendition(renditionName));
    Resource resource = context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg/jcr:content/renditions/" + renditionName);
    assertNotNull(resource);

    Optional<DamEvent> damEvent = damEventHandler.getLastEvent();
    assertTrue(damEvent.isPresent());
    assertEquals(DamEvent.Type.RENDITION_UPDATED, damEvent.get().getType());
    assertEquals(asset.getPath(), damEvent.get().getAssetPath());
    assertEquals(rendition.getPath(), damEvent.get().getAdditionalInfo());

    asset.removeRendition(renditionName);

    assertNull(asset.getRendition(renditionName));
    resource = context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg/jcr:content/renditions/" + renditionName);
    assertNull(resource);

    damEvent = damEventHandler.getLastEvent();
    assertTrue(damEvent.isPresent());
    assertEquals(DamEvent.Type.RENDITION_REMOVED, damEvent.get().getType());
    assertEquals(asset.getPath(), damEvent.get().getAssetPath());
    assertEquals(rendition.getPath(), damEvent.get().getAdditionalInfo());
  }

  @Test
  public void testAddRemoveRendition() throws Exception {
    doTestAddRemoveRendition("test.bin");
  }

  @Test
  public void testRenditionListMutable() throws Exception {
    // make sure rendition list is modifiable by calling getBestfitRendition which does a sort on it
    UIHelper.getBestfitRendition(asset, 100);
  }

  @Test
  public void testBatchMode() throws Exception {
    if (context.resourceResolverType() == ResourceResolverType.JCR_MOCK) {
      // resource resolver revert not support for JCR_MOCK - skip test
      return;
    }

    // when batch mode is set to true ResourceResolver commit isn't called keeping the changes transient
    asset.setBatchMode(true);
    assertTrue(asset.isBatchMode());
    doTestAddRemoveRendition("testwithbatchmode.bin");
    assertTrue(context.resourceResolver().hasChanges());

    context.resourceResolver().revert();

    // when batch mode is set to false ResourceResolver commit is called and there are no more pending changes
    asset.setBatchMode(false);
    assertFalse(asset.isBatchMode());
    doTestAddRemoveRendition("testwithoutbatchmode.bin");
    assertFalse(context.resourceResolver().hasChanges());
  }

  @Test
  public void testGetID() {
    if (context.resourceResolverType() == ResourceResolverType.JCR_OAK) {
      assertNotNull(asset.getID());
    }
    else {
      assertEquals("442d55b6-d534-4faf-9394-c9c20d095985", asset.getID());
    }
  }

  @Test
  public void testRemoveNonExistingRendition() {
    asset.removeRendition("non-existing");
  }

}
