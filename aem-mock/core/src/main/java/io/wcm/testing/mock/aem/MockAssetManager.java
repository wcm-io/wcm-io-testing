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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.loader.ContentLoader;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.event.EventAdmin;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.DamEvent;
import com.day.cq.dam.api.Revision;
import com.day.image.Layer;
import com.google.common.collect.ImmutableMap;

import io.wcm.testing.mock.aem.builder.ContentBuilder;

/**
 * Mock implementation of {@link AssetManager}
 */
@SuppressWarnings("null")
class MockAssetManager implements AssetManager {

  private final ResourceResolver resourceResolver;
  private final ContentBuilder contentBuilder;
  private final ContentLoader contentLoader;
  private final EventAdmin eventAdmin;

  MockAssetManager(@NotNull final ResourceResolver resourceResolver, EventAdmin eventAdmin) {
    this.resourceResolver = resourceResolver;
    this.contentBuilder = new ContentBuilder(resourceResolver);
    this.contentLoader = new ContentLoader(resourceResolver);
    this.eventAdmin = eventAdmin;
  }

  @Override
  public Asset createAsset(String path, InputStream inputStream, String mimeType, boolean autoSave) {
    try {
      // create asset
      contentBuilder.resource(path, ImmutableMap.<String, Object>builder()
          .put(JcrConstants.JCR_PRIMARYTYPE, DamConstants.NT_DAM_ASSET)
          .build());
      contentBuilder.resource(path + "/" + JcrConstants.JCR_CONTENT, ImmutableMap.<String, Object>builder()
          .put(JcrConstants.JCR_PRIMARYTYPE, DamConstants.NT_DAM_ASSETCONTENT)
          .build());
      String renditionsPath = path + "/" + JcrConstants.JCR_CONTENT + "/" + DamConstants.RENDITIONS_FOLDER;
      contentBuilder.resource(renditionsPath, ImmutableMap.<String, Object>builder()
          .put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_FOLDER)
          .build());

      // store asset metadata
      Map<String, Object> metadataProps = new HashMap<>();

      // try to detect image with/height if input stream contains image data
      byte[] data = IOUtils.toByteArray(inputStream);
      try (InputStream is = new ByteArrayInputStream(data)) {
        try {
          Layer layer = new Layer(is);
          metadataProps.put(DamConstants.TIFF_IMAGEWIDTH, layer.getWidth());
          metadataProps.put(DamConstants.TIFF_IMAGELENGTH, layer.getHeight());
        }
        /*CHECKSTYLE:OFF*/ catch (Exception ex) { /*CHECKSTYLE:ON*/
          // ignore
        }
      }

      contentBuilder.resource(path + "/" + JcrConstants.JCR_CONTENT + "/" + DamConstants.METADATA_FOLDER, metadataProps);

      // store original rendition
      try (InputStream is = new ByteArrayInputStream(data)) {
        contentLoader.binaryFile(is, renditionsPath + "/" + DamConstants.ORIGINAL_FILE, mimeType);
      }

      if (autoSave) {
        resourceResolver.commit();
      }

      // send DamEvent after asset creation
      eventAdmin.sendEvent(DamEvent.assetCreated(path, resourceResolver.getUserID()).toEvent());

    }
    catch (IOException ex) {
      throw new RuntimeException("Unable to create asset at " + path, ex);
    }

    return resourceResolver.getResource(path).adaptTo(Asset.class);
  }

  // --- unsupported operations ---

  @Override
  public Asset restore(String s) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Revision> getRevisions(String s, Calendar calendar) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public Asset createAssetForBinary(String s, boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Asset getAssetForBinary(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAssetForBinary(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Revision createRevision(Asset asset, String s, String s1) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public String assignAssetID(Asset asset) throws PathNotFoundException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  // AEM 6.5
  @SuppressWarnings("unused")
  public Asset createOrUpdateAsset(String arg0, Binary arg1, String arg2, boolean arg3) {
    throw new UnsupportedOperationException();
  }

}
