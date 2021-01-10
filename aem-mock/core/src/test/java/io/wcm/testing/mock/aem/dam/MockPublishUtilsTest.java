/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.s7dam.utils.PublishUtils;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockPublishUtilsTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private PublishUtils underTest;

  @Before
  public void setUp() throws Exception {
    underTest = context.getService(PublishUtils.class);
    assertNotNull(underTest);
  }

  @Test
  public void testExternalizeImageDeliveryAssetResource() throws Exception {
    Asset asset = context.create().asset("/content/dam/test.jpg", 10, 10, "image/jpeg");
    Resource assetResource = asset.adaptTo(Resource.class);

    String[] result = underTest.externalizeImageDeliveryAsset(assetResource);
    assertArrayEquals(new String[] {
        MockPublishUtils.DUMMY_HOST,
        asset.getPath()
    }, result);
  }

}
