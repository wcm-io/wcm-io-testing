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
package io.wcm.testing.mock.aem.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.loader.ContentLoader;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.Rendition;
import com.day.cq.tagging.InvalidTagFormatException;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.day.image.Layer;
import com.google.common.collect.ImmutableMap;

/**
 * Helper class for building test content in the resource hierarchy with as less boilerplate code as possible.
 */
@ProviderType
public final class ContentBuilder extends org.apache.sling.testing.mock.sling.builder.ContentBuilder {

  static final String DUMMY_TEMPLATE = "/apps/sample/templates/template1";

  // cache generated dummy images in cache because often the a dummy image with the same parameter is reused.
  private static final Map<String, byte[]> DUMMY_IMAGE_CACHE = new HashMap<>();

  /**
   * @param resourceResolver Resource resolver
   */
  public ContentBuilder(ResourceResolver resourceResolver) {
    super(resourceResolver);
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @return Page object
   */
  public Page page(String path) {
    return page(path, DUMMY_TEMPLATE, ValueMap.EMPTY);
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @param template Template
   * @return Page object
   */
  public Page page(String path, String template) {
    return page(path, template, ValueMap.EMPTY);
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @param template Template
   * @param title Page title
   * @return Page object
   */
  public Page page(String path, String template, String title) {
    return page(path, template, ImmutableMap.<String, Object>builder()
        .put(NameConstants.PN_TITLE, title)
        .build());
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @param template Template
   * @param contentProperties Properties for <code>jcr:content</code> node.
   * @return Page object
   */
  public Page page(String path, String template, Map<String, Object> contentProperties) {
    String parentPath = ResourceUtil.getParent(path);
    ensureResourceExists(parentPath);
    String name = ResourceUtil.getName(path);
    try {
      PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
      Page page = pageManager.create(parentPath, name, template, name, true);
      if (!contentProperties.isEmpty()) {
        ModifiableValueMap pageProperties = page.getContentResource().adaptTo(ModifiableValueMap.class);
        pageProperties.putAll(contentProperties);
        resourceResolver.commit();
      }
      return page;
    }
    catch (WCMException | PersistenceException ex) {
      throw new RuntimeException("Unable to create page at " + path, ex);
    }
  }

  /**
   * Create DAM asset.
   * @param path Asset path
   * @param classpathResource Classpath resource URL for binary file.
   * @param mimeType Mime type
   * @return Asset
   */
  public Asset asset(String path, String classpathResource, String mimeType) {
    return asset(path, classpathResource, mimeType, null);
  }

  /**
   * Create DAM asset.
   * @param path Asset path
   * @param classpathResource Classpath resource URL for binary file.
   * @param mimeType Mime type
   * @param metadata Asset metadata
   * @return Asset
   */
  public Asset asset(String path, String classpathResource, String mimeType, Map<String, Object> metadata) {
    try (InputStream is = ContentLoader.class.getResourceAsStream(classpathResource)) {
      if (is == null) {
        throw new IllegalArgumentException("Classpath resource not found: " + classpathResource);
      }
      return asset(path, is, mimeType, metadata);
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Create DAM asset with a generated dummy image. The image is empty.
   * @param path Asset path
   * @param width Dummy image width
   * @param height Dummy image height
   * @param mimeType Mime type
   * @return Asset
   */
  public Asset asset(String path, int width, int height, String mimeType) {
    return asset(path, width, height, mimeType, null);
  }

  /**
   * Create DAM asset with a generated dummy image. The image is empty.
   * @param path Asset path
   * @param width Dummy image width
   * @param height Dummy image height
   * @param mimeType Mime type
   * @param metadata Asset metadata
   * @return Asset
   */
  public Asset asset(String path, int width, int height, String mimeType, Map<String, Object> metadata) {
    try (InputStream is = createDummyImage(width, height, mimeType)) {
      return asset(path, is, mimeType, metadata);
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Create DAM asset.
   * @param path Asset path
   * @param inputStream Binary data for original rendition
   * @param mimeType Mime type
   * @return Asset
   */
  public Asset asset(String path, InputStream inputStream, String mimeType) {
    return asset(path, inputStream, mimeType, null);
  }

  /**
   * Create DAM asset.
   * @param path Asset path
   * @param inputStream Binary data for original rendition
   * @param mimeType Mime type
   * @param metadata Asset metadata
   * @return Asset
   */
  public Asset asset(String path, InputStream inputStream, String mimeType, Map<String, Object> metadata) {
    AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);
    Asset asset = assetManager.createAsset(path, inputStream, mimeType, true);

    if (metadata != null && !metadata.isEmpty()) {
      String metadataPath = asset.getPath() + "/" + JcrConstants.JCR_CONTENT + "/" + DamConstants.METADATA_FOLDER;
      Resource metadataResource = resourceResolver.getResource(metadataPath);
      if (metadataResource == null) {
        metadataResource = resource(metadataPath);
      }
      ModifiableValueMap metadataProperties = metadataResource.adaptTo(ModifiableValueMap.class);
      metadataProperties.putAll(metadata);
    }

    return asset;
  }

  /**
   * Create dummy image
   * @param width Width
   * @param height height
   * @param mimeType Mime type
   * @return Input stream
   */
  public static InputStream createDummyImage(int width, int height, String mimeType) {
    String key = width + "x" + height + ":" + mimeType;
    byte[] data = DUMMY_IMAGE_CACHE.get(key);
    if (data == null) {
      Layer layer = new Layer(width, height, null);
      try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
        double quality = StringUtils.equals(mimeType, "image/gif") ? 256d : 1.0d;
        layer.write(mimeType, quality, bos);
        data = bos.toByteArray();
        DUMMY_IMAGE_CACHE.put(key, data);
      }
      catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
    return new ByteArrayInputStream(data);
  }

  /**
   * Adds an rendition to DAM asset.
   * @param asset DAM asset
   * @param name Rendition name
   * @param classpathResource Classpath resource URL for binary file.
   * @param mimeType Mime type
   * @return Asset
   */
  public Rendition assetRendition(Asset asset, String name, String classpathResource, String mimeType) {
    try (InputStream is = ContentLoader.class.getResourceAsStream(classpathResource)) {
      if (is == null) {
        throw new IllegalArgumentException("Classpath resource not found: " + classpathResource);
      }
      return assetRendition(asset, name, is, mimeType);
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Adds an rendition with a generated dummy image to DAM asset. The image is empty.
   * @param asset DAM asset
   * @param name Rendition name
   * @param width Dummy image width
   * @param height Dummy image height
   * @param mimeType Mime type
   * @return Asset
   */
  public Rendition assetRendition(Asset asset, String name, int width, int height, String mimeType) {
    try (InputStream is = createDummyImage(width, height, mimeType)) {
      return assetRendition(asset, name, is, mimeType);
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Adds an rendition to DAM asset.
   * @param asset DAM asset
   * @param name Rendition name
   * @param inputStream Binary data for original rendition
   * @param mimeType Mime type
   * @return Asset
   */
  public Rendition assetRendition(Asset asset, String name, InputStream inputStream, String mimeType) {
    return asset.addRendition(name, inputStream, mimeType);
  }

  /**
   * Adds a tag definition.
   * @param tagId Tag ID. May include namespace (separated by ":"). May include nested levels (separated by "/").
   * @return Tag
   */
  public Tag tag(String tagId) {
    TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
    String tagTitle = ResourceUtil.getName(StringUtils.substringAfter(tagId, ":"));
    try {
      return tagManager.createTag(tagId, tagTitle, null, true);
    }
    catch (AccessControlException | InvalidTagFormatException ex) {
      throw new RuntimeException("Unable to create tag: " + tagId, ex);
    }
  }

}
