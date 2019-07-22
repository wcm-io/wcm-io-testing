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

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_FOLDER;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static com.day.cq.dam.api.DamConstants.METADATA_FOLDER;
import static com.day.cq.dam.api.DamConstants.NT_DAM_ASSET;
import static com.day.cq.dam.api.DamConstants.NT_DAM_ASSETCONTENT;
import static com.day.cq.dam.api.DamConstants.ORIGINAL_FILE;
import static com.day.cq.dam.api.DamConstants.RENDITIONS_FOLDER;
import static com.day.cq.dam.api.DamConstants.TIFF_IMAGELENGTH;
import static com.day.cq.dam.api.DamConstants.TIFF_IMAGEWIDTH;

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
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.loader.ContentLoader;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.event.EventAdmin;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.api.DamEvent;
import com.day.cq.dam.api.Revision;
import com.day.image.Layer;

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
  public Asset createAsset(String assetPath, InputStream inputStream, String mimeType, boolean autoSave) {
    String assetContentPath = assetPath + "/" + JCR_CONTENT;
    String metadataPath = assetContentPath + "/" + METADATA_FOLDER;
    String renditionsPath = assetContentPath + "/" + RENDITIONS_FOLDER;

    try {
      // create asset
      createOrUpdateResource(assetPath, NT_DAM_ASSET, null);
      createOrUpdateResource(assetContentPath, NT_DAM_ASSETCONTENT, null);
      createOrUpdateResource(renditionsPath, NT_FOLDER, null);

      // store asset metadata
      Map<String, Object> metadataProps = new HashMap<>();

      // try to detect image with/height if input stream contains image data
      byte[] data = inputStream != null ? IOUtils.toByteArray(inputStream) : null;
      if (data != null) {
        try (InputStream is = new ByteArrayInputStream(data)) {
          try {
            Layer layer = new Layer(is);
            metadataProps.put(TIFF_IMAGEWIDTH, layer.getWidth());
            metadataProps.put(TIFF_IMAGELENGTH, layer.getHeight());
          }
          /*CHECKSTYLE:OFF*/ catch (Exception ex) { /*CHECKSTYLE:ON*/
            // ignore
          }
        }
      }

      createOrUpdateResource(metadataPath, NT_UNSTRUCTURED, metadataProps);

      // store original rendition
      if (data != null) {
        try (InputStream is = new ByteArrayInputStream(data)) {
          contentLoader.binaryFile(is, renditionsPath + "/" + ORIGINAL_FILE, mimeType);
        }
      }

      if (autoSave) {
        resourceResolver.commit();
      }

      // send DamEvent after asset creation
      eventAdmin.sendEvent(DamEvent.assetCreated(assetPath, resourceResolver.getUserID()).toEvent());

    }
    catch (IOException ex) {
      throw new RuntimeException("Unable to create asset at " + assetPath, ex);
    }

    return resourceResolver.getResource(assetPath).adaptTo(Asset.class);
  }

  private void createOrUpdateResource(String path, String jcrPrimaryType, Map<String, Object> props) {
    Resource resource = resourceResolver.getResource(path);
    if (resource == null) {
      // create new resource
      Map<String, Object> newResourceProps = new HashMap<>();
      newResourceProps.put(JCR_PRIMARYTYPE, jcrPrimaryType);
      if (props != null) {
        newResourceProps.putAll(props);
      }
      contentBuilder.resource(path, newResourceProps);
    }
    else if (props != null) {
      // update existing resource
      ModifiableValueMap existingResourceProps = resource.adaptTo(ModifiableValueMap.class);
      existingResourceProps.putAll(props);
    }
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
