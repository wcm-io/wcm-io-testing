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

import static io.wcm.testing.mock.aem.dam.MockAssetHandler.JPEG_MIME_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.dam.api.handler.AssetHandler;
import com.day.cq.dam.api.handler.store.AssetStore;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockAssetStoreTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private AssetStore underTest;

  @Before
  public void setUp() {
    underTest = context.getService(AssetStore.class);
  }

  @Test
  public void testAssetHandler_Invalid() {
    assertNull(underTest.getAssetHandler("invalid/type"));
  }

  @Test
  public void testGetAssetHandler_JPEG() throws Exception {
    assertNotNull(underTest.getAssetHandler(JPEG_MIME_TYPE));
  }

  @Test
  public void testGetAllAssetHandler() {
    AssetHandler[] all = underTest.getAllAssetHandler();
    assertEquals(1, all.length);
  }

}
