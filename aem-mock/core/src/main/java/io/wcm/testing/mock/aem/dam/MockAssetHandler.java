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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.osgi.service.component.annotations.Component;

import com.adobe.granite.asset.api.AssetRelation;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetHandlerException;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.handler.AssetHandler;
import com.day.cq.dam.api.metadata.ExtractedMetadata;
import com.day.cq.dam.api.thumbnail.ThumbnailConfig;
import com.day.image.Layer;

/**
 * Mock implementation of {@link AssetHandler}.
 */
@Component(service = AssetHandler.class)
public final class MockAssetHandler implements AssetHandler {

  static final String JPEG_MIME_TYPE = "image/jpeg";
  static final String PNG_MIME_TYPE = "image/png";
  static final String GIF_MIME_TYPE = "image/gif";
  static final String TIFF_MIME_TYPE = "image/tiff";
  static final String SVG_MIME_TYPE = "image/svg+xml";

  private static final String[] MIME_TYPES = new String[] {
      JPEG_MIME_TYPE,
      PNG_MIME_TYPE,
      GIF_MIME_TYPE,
      TIFF_MIME_TYPE,
      SVG_MIME_TYPE
  };

  @Override
  public String[] getMimeTypes() {
    return MIME_TYPES;
  }

  @Override
  public BufferedImage getImage(Rendition rendition) throws IOException {
    return getImage(rendition, null);
  }

  @Override
  public BufferedImage getImage(Rendition rendition, Dimension maxDimension) throws IOException {
    try (InputStream is = rendition.getStream()) {
      Layer layer = new Layer(is, maxDimension);
      return layer.getImage();
    }
  }

  @Override
  public boolean canHandleSubAssets() {
    return false;
  }


  // --- unsupported operations ---

  @Override
  public void createThumbnails(Asset asset) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void createThumbnails(Asset asset, Collection<ThumbnailConfig> configs) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void createThumbnails(Asset asset, Rendition rendition, Collection<ThumbnailConfig> configs) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void createThumbnails(Node node, Node renditionFolder, Session session, List<Integer[]> dimensions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void createThumbnailsExt(Node node, Node renditionFolder, Session session, List<Map<String, Object>> thumbnailConfigs) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void exportAsset(Node asset, OutputStream os) throws AssetHandlerException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void exportAsset(Asset asset, OutputStream os) throws AssetHandlerException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExtractedMetadata extractMetadata(Node node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExtractedMetadata extractMetadata(Asset asset) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BufferedImage getImage(Node node) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<? extends AssetRelation> processRelated(Asset asset) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> processSubAssets(Asset asset) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> processSubAssets(Node asset, Session session) {
    throw new UnsupportedOperationException();
  }

}
