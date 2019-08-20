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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamEvent;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockAssetManagerTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private DamEventHandler damEventHandler;

  @Before
  public void setUp() {
    damEventHandler = (DamEventHandler)context.registerService(EventHandler.class, new DamEventHandler());
  }

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

    List<DamEvent> damEvents = damEventHandler.getLastEvents(2);
    assertEquals(2, damEvents.size());

    assertEquals(DamEvent.Type.RENDITION_UPDATED, damEvents.get(0).getType());
    assertEquals(asset.getPath(), damEvents.get(0).getAssetPath());
    assertEquals(asset.getOriginal().getPath(), damEvents.get(0).getAdditionalInfo());
    assertEquals(DamEvent.Type.ASSET_CREATED, damEvents.get(1).getType());
    assertEquals(asset.getPath(), damEvents.get(1).getAssetPath());
  }

  @Test
  public void testCreateAssetWithoutOriginalRendition() throws IOException {
    String assetName = "myasset.gif";

    Asset asset = context.assetManager().createAsset(context.uniqueRoot().dam() + '/' + assetName, null, null, true);

    assertNotNull(asset);
    assertNull(asset.getOriginal());
    assertEquals(asset.getName(), assetName);
    assertEquals("", asset.getMimeType());

    Optional<DamEvent> damEvent = damEventHandler.getLastEvent();
    assertTrue(damEvent.isPresent());
    assertEquals(DamEvent.Type.ASSET_CREATED, damEvent.get().getType());
    assertEquals(asset.getPath(), damEvent.get().getAssetPath());

    // update asset with original rendition by calling createAsset method again
    InputStream testImage = openTestAsset();
    String mimeType = "image/gif";

    asset = context.assetManager().createAsset(context.uniqueRoot().dam() + '/' + assetName, testImage, "image/gif", true);

    assertNotNull(asset);
    assertNotNull(asset.getOriginal().getStream());
    assertTrue(IOUtils.contentEquals(openTestAsset(), asset.getOriginal().getStream()));
    assertEquals(asset.getName(), assetName);
    assertEquals(asset.getMimeType(), mimeType);
  }

  private InputStream openTestAsset() {
    return getClass().getClassLoader().getResourceAsStream("sample-image.gif");
  }

  static final class DamEventHandler implements EventHandler {

    private final List<DamEvent> events = new ArrayList<>();

    @Override
    public void handleEvent(Event event) {
      if (StringUtils.equals(event.getTopic(), DamEvent.EVENT_TOPIC)) {
        events.add(DamEvent.fromEvent(event));
      }
    }

    public List<DamEvent> getEvents() {
      return this.events;
    }

    public Optional<DamEvent> getLastEvent() {
      if (this.events.isEmpty()) {
        return Optional.empty();
      }
      else {
        return Optional.of(events.get(events.size() - 1));
      }
    }

    public List<DamEvent> getLastEvents(int number) {
      List<DamEvent> result = new ArrayList<>();
      for (int i = events.size() - number; i < events.size(); i++) {
        result.add(events.get(i));
      }
      return result;
    }

  }

}
