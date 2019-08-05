/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import static io.wcm.testing.mock.aem.dam.MockAssetHandler.GIF_MIME_TYPE;
import static io.wcm.testing.mock.aem.dam.MockAssetHandler.JPEG_MIME_TYPE;
import static io.wcm.testing.mock.aem.dam.MockAssetHandler.PNG_MIME_TYPE;
import static io.wcm.testing.mock.aem.dam.MockAssetHandler.TIFF_MIME_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.handler.AssetHandler;
import com.day.cq.dam.api.handler.store.AssetStore;
import com.day.image.Layer;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockAssetHandlerTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private AssetStore assetStore;

  @Before
  public void setUp() {
    assetStore = context.getService(AssetStore.class);
    assertNotNull("Asset Store service not present", assetStore);
  }

  @Test
  public void testAssetHandler_JPEG() throws Exception {
    assertAssertHandlerImage("/dam/filetype/sample.jpg", JPEG_MIME_TYPE, 100, 50);
  }

  @Test
  public void testAssetHandler_GIF() throws Exception {
    assertAssertHandlerImage("/dam/filetype/sample.gif", GIF_MIME_TYPE, 100, 50);
  }

  @Test
  public void testAssetHandler_PNG() throws Exception {
    assertAssertHandlerImage("/dam/filetype/sample.png", PNG_MIME_TYPE, 100, 50);
  }

  @Test
  public void testAssetHandler_TIFF() throws Exception {
    assertAssertHandlerImage("/dam/filetype/sample.tif", TIFF_MIME_TYPE, 100, 50);
  }

  private void assertAssertHandlerImage(String classpathResource, String contentType, int width, int height)
      throws IOException {
    String filename = FilenameUtils.getName(classpathResource);
    Asset asset = context.create().asset("/content/dam/" + filename, classpathResource, contentType);

    AssetHandler assetHandler = assetStore.getAssetHandler(contentType);
    assertNotNull(assetHandler);

    BufferedImage bufferedImage = assetHandler.getImage(asset.getOriginal());
    Layer layer = new Layer(bufferedImage);
    assertEquals(width, layer.getWidth());
    assertEquals(height, layer.getHeight());
  }

}
