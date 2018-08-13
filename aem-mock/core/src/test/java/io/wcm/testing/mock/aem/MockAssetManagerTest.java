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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.dam.api.Asset;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockAssetManagerTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  @Test
  public void testCreateAsset() throws IOException {
    InputStream testImage = openTestAsset();
    String assetName = "myasset.gif";
    String mimeType = "image/gif";

    Asset asset = context.assetManager().createAsset(context.uniqueRoot().dam() + '/' + assetName, testImage, "image/gif", true);

    assertNotNull(asset);
    assertNotNull(asset.getOriginal().getStream());
    assertTrue(IOUtils.contentEquals(openTestAsset(), asset.getOriginal().getStream()));
    assertEquals(asset.getName(), assetName);
    assertEquals(asset.getMimeType(), mimeType);
  }

  private InputStream openTestAsset() {
    return getClass().getClassLoader().getResourceAsStream("sample-image.gif");
  }
}
