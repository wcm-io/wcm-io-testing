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
import java.nio.charset.StandardCharsets;
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
import org.apache.sling.testing.mock.osgi.MapUtil;
import org.apache.sling.testing.mock.sling.loader.ContentLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.service.event.EventAdmin;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.DamEvent;
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.testing.mock.aem.context.AemContextImpl;

/**
 * Helper class for building test content in the resource hierarchy with as less boilerplate code as possible.
 */
@ProviderType
@SuppressWarnings("null")
public final class ContentBuilder extends org.apache.sling.testing.mock.sling.builder.ContentBuilder {

  static final @NotNull String DUMMY_TEMPLATE = "/apps/sample/templates/template1";

  // cache generated dummy images in cache because often the a dummy image with the same parameter is reused.
  private static final Map<String, byte[]> DUMMY_IMAGE_CACHE = new HashMap<>();

  private final @Nullable AemContextImpl context;

  /**
   * @param resourceResolver Resource resolver
   */
  public ContentBuilder(@NotNull ResourceResolver resourceResolver) {
    super(resourceResolver);
    this.context = null;
  }

  /**
   * @param context Context
   */
  public ContentBuilder(@NotNull AemContextImpl context) {
    super(context.resourceResolver());
    this.context = context;
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @return Page object
   */
  public Page page(@NotNull String path) {
    return page(path, DUMMY_TEMPLATE, ValueMap.EMPTY);
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @param template Template
   * @return Page object
   */
  public Page page(@NotNull String path, @Nullable String template) {
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
  public Page page(@NotNull String path, @Nullable String template, @NotNull String title) {
    return page(path, template, ImmutableMap.<String, Object>builder()
        .put(NameConstants.PN_TITLE, title)
        .build());
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @param template Template
   * @param pageProperties Properties for <code>jcr:content</code> node.
   * @return Page object
   */
  public Page page(@NotNull String path, @Nullable String template, @NotNull Map<String, Object> pageProperties) {
    String parentPath = ResourceUtil.getParent(path);
    if (parentPath == null) {
      throw new IllegalArgumentException("Resource has no parent: " + path);
    }
    ensureResourceExists(parentPath);
    String name = ResourceUtil.getName(path);
    try {
      PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
      if (pageManager == null) {
        throw new RuntimeException("No page manager.");
      }
      Page page = pageManager.create(parentPath, name, template, name, true);
      if (!pageProperties.isEmpty()) {
        ModifiableValueMap props = page.getContentResource().adaptTo(ModifiableValueMap.class);
        if (props == null) {
          throw new RuntimeException("No ModifiableValueMap.");
        }
        props.putAll(pageProperties);
        resourceResolver.commit();
      }
      return page;
    }
    catch (WCMException | PersistenceException ex) {
      throw new RuntimeException("Unable to create page at " + path, ex);
    }
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @param template Template
   * @param pageProperties Properties for <code>jcr:content</code> node.
   * @return Page object
   */
  public Page page(@NotNull String path, @Nullable String template, @NotNull Object @NotNull... pageProperties) {
    return page(path, template, MapUtil.toMap(pageProperties));
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param parentPage Parent page of the newp age
   * @param name Child page name
   * @return Page object
   */
  public Page page(@NotNull Page parentPage, @NotNull String name) {
    return page(parentPage, name, DUMMY_TEMPLATE, ValueMap.EMPTY);
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param parentPage Parent page of the newp age
   * @param name Child page name
   * @param template Template
   * @return Page object
   */
  public Page page(@NotNull Page parentPage, @NotNull String name, @Nullable String template) {
    return page(parentPage, name, template, ValueMap.EMPTY);
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param parentPage Parent page of the newp age
   * @param name Child page name
   * @param template Template
   * @param title Page title
   * @return Page object
   */
  public Page page(@NotNull Page parentPage, @NotNull String name, @Nullable String template, @NotNull String title) {
    return page(parentPage, name, template, ImmutableMap.<String, Object>builder()
        .put(NameConstants.PN_TITLE, title)
        .build());
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param parentPage Parent page of the newp age
   * @param name Child page name
   * @param template Template
   * @param pageProperties Properties for <code>jcr:content</code> node.
   * @return Page object
   */
  public Page page(@NotNull Page parentPage, @NotNull String name, @Nullable String template, @NotNull Map<String, Object> pageProperties) {
    String path = parentPage.getPath() + "/" + StringUtils.stripStart(name, "/");
    return page(path, template, pageProperties);
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param parentPage Parent page of the newp age
   * @param name Child page name
   * @param template Template
   * @param pageProperties Properties for <code>jcr:content</code> node.
   * @return Page object
   */
  public Page page(@NotNull Page parentPage, @NotNull String name, @Nullable String template, @NotNull Object @NotNull... pageProperties) {
    return page(parentPage, name, template, MapUtil.toMap(pageProperties));
  }

  /**
   * Create DAM asset.
   * @param path Asset path
   * @param classpathResource Classpath resource URL for binary file.
   * @param mimeType Mime type
   * @return Asset
   */
  public Asset asset(@NotNull String path, @NotNull String classpathResource, @NotNull String mimeType) {
    return asset(path, classpathResource, mimeType, (Map<String, Object>)null);
  }

  /**
   * Create DAM asset.
   * @param path Asset path
   * @param classpathResource Classpath resource URL for binary file.
   * @param mimeType Mime type
   * @param metadata Asset metadata properties
   * @return Asset
   */
  public Asset asset(@NotNull String path, @NotNull String classpathResource, @NotNull String mimeType, @Nullable Map<String, Object> metadata) {
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
   * Create DAM asset.
   * @param path Asset path
   * @param classpathResource Classpath resource URL for binary file.
   * @param mimeType Mime type
   * @param metadata Asset metadata properties
   * @return Asset
   */
  public Asset asset(@NotNull String path, @NotNull String classpathResource, @NotNull String mimeType, @NotNull Object @NotNull... metadata) {
    return asset(path, classpathResource, mimeType, MapUtil.toMap(metadata));
  }

  /**
   * Create DAM asset with a generated dummy image. The image is empty.
   * @param path Asset path
   * @param width Dummy image width
   * @param height Dummy image height
   * @param mimeType Mime type
   * @return Asset
   */
  public Asset asset(@NotNull String path, int width, int height, @NotNull String mimeType) {
    return asset(path, width, height, mimeType, (Map<String, Object>)null);
  }

  /**
   * Create DAM asset with a generated dummy image. The image is empty.
   * @param path Asset path
   * @param width Dummy image width
   * @param height Dummy image height
   * @param mimeType Mime type
   * @param metadata Asset metadata properties
   * @return Asset
   */
  public Asset asset(@NotNull String path, int width, int height, @NotNull String mimeType, @Nullable Map<String, Object> metadata) {
    try (InputStream is = createDummyImage(width, height, mimeType)) {
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
   * @param metadata Asset metadata properties
   * @return Asset
   */
  public Asset asset(@NotNull String path, int width, int height, @NotNull String mimeType, @NotNull Object @NotNull... metadata) {
    return asset(path, width, height, mimeType, MapUtil.toMap(metadata));
  }

  /**
   * Create DAM asset.
   * @param path Asset path
   * @param inputStream Binary data for original rendition
   * @param mimeType Mime type
   * @return Asset
   */
  public Asset asset(@NotNull String path, @NotNull InputStream inputStream, @NotNull String mimeType) {
    return asset(path, inputStream, mimeType, (Map<String, Object>)null);
  }

  /**
   * Create DAM asset.
   * @param path Asset path
   * @param inputStream Binary data for original rendition
   * @param mimeType Mime type
   * @param metadata Asset metadata properties
   * @return Asset
   */
  public Asset asset(@NotNull String path, @NotNull InputStream inputStream, @NotNull String mimeType, @Nullable Map<String, Object> metadata) {
    AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);
    if (assetManager == null) {
      throw new RuntimeException("No asset manager.");
    }
    Asset asset = assetManager.createAsset(path, inputStream, mimeType, true);

    if (metadata != null && !metadata.isEmpty()) {
      String metadataPath = asset.getPath() + "/" + JcrConstants.JCR_CONTENT + "/" + DamConstants.METADATA_FOLDER;
      Resource metadataResource = resourceResolver.getResource(metadataPath);
      if (metadataResource == null) {
        metadataResource = resource(metadataPath);
      }
      ModifiableValueMap metadataProperties = metadataResource.adaptTo(ModifiableValueMap.class);
      if (metadataProperties == null) {
        throw new RuntimeException("No ModifiableValueMap.");
      }
      metadataProperties.putAll(metadata);
    }

    return asset;
  }

  /**
   * Create DAM asset.
   * @param path Asset path
   * @param inputStream Binary data for original rendition
   * @param mimeType Mime type
   * @param metadata Asset metadata properties
   * @return Asset
   */
  public Asset asset(@NotNull String path, @NotNull InputStream inputStream, @NotNull String mimeType, @NotNull Object @NotNull... metadata) {
    return asset(path, inputStream, mimeType, MapUtil.toMap(metadata));
  }

  /**
   * Create dummy image
   * @param width Width
   * @param height height
   * @param mimeType Mime type
   * @return Input stream
   */
  public static @NotNull InputStream createDummyImage(int width, int height, String mimeType) {
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
  public Rendition assetRendition(@NotNull Asset asset, @NotNull String name, @NotNull String classpathResource, @NotNull String mimeType) {
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
  public Rendition assetRendition(@NotNull Asset asset, @NotNull String name, int width, int height, @NotNull String mimeType) {
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
  public Rendition assetRendition(@NotNull Asset asset, String name, @NotNull InputStream inputStream, @NotNull String mimeType) {
    return asset.addRendition(name, inputStream, mimeType);
  }

  /**
   * Create DAM content fragment.
   * @param path Content fragment asset path
   * @param data Content fragment structured data
   * @return Content fragment
   */
  public ContentFragment contentFragmentStructured(@NotNull String path, @Nullable Map<String, Object> data) {
    return contentFragmentTextOrStructured(path, null, null, data);
  }

  /**
   * Create DAM content fragment.
   * @param path Content fragment asset path
   * @param data Content fragment structured data
   * @return Content fragment
   */
  public ContentFragment contentFragmentStructured(@NotNull String path, @NotNull Object @NotNull... data) {
    return contentFragmentStructured(path, MapUtil.toMap(data));
  }

  /**
   * Create DAM content fragment.
   * @param path Content fragment asset path
   * @param text Text value
   * @param mimeType Mime type of the value
   * @return Content fragment
   */
  public ContentFragment contentFragmentText(@NotNull String path, @NotNull String text, @NotNull String mimeType) {
    return contentFragmentTextOrStructured(path, text, mimeType, null);
  }

  @SuppressFBWarnings("STYLE")
  private ContentFragment contentFragmentTextOrStructured(@NotNull String path,
      @Nullable String text, @Nullable String mimeType,
      @Nullable Map<String, Object> data) {
    // create asset
    resource(path, JcrConstants.JCR_PRIMARYTYPE, DamConstants.NT_DAM_ASSET);
    resource(path + "/" + JcrConstants.JCR_CONTENT, JcrConstants.JCR_PRIMARYTYPE, DamConstants.NT_DAM_ASSETCONTENT);
    String renditionsPath = path + "/" + JcrConstants.JCR_CONTENT + "/" + DamConstants.RENDITIONS_FOLDER;
    resource(renditionsPath, JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_FOLDER);

    // store asset metadata
    Map<String, Object> metadataProps = new HashMap<>();
    if (mimeType != null) {
      metadataProps.put(DamConstants.DC_FORMAT, mimeType);
    }
    resource(path + "/" + JcrConstants.JCR_CONTENT + "/" + DamConstants.METADATA_FOLDER, metadataProps);

    // store text as original rendition
    if (text != null) {
      try (InputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))) {
        ContentLoader contentLoader = new ContentLoader(resourceResolver);
        contentLoader.binaryFile(is, renditionsPath + "/" + DamConstants.ORIGINAL_FILE, mimeType);
      }
      catch (IOException ex) {
        throw new RuntimeException("Unable to create content fragment at " + path, ex);
      }

      // create model/elements/main node
      resource(path + "/" + JcrConstants.JCR_CONTENT + "/model/elements/main",
          "name", "main",
          "jcr:title", "Main");
    }

    if (data != null) {
      // create data/master node
      resource(path + "/" + JcrConstants.JCR_CONTENT + "/data/master", data);
    }

    // send DamEvent after asset creation
    if (context != null) {
      EventAdmin eventAdmin = context.getService(EventAdmin.class);
      if (eventAdmin != null) {
        eventAdmin.sendEvent(DamEvent.assetCreated(path, resourceResolver.getUserID()).toEvent());
      }
    }

    return resourceResolver.getResource(path).adaptTo(ContentFragment.class);
  }

  /**
   * Adds a tag definition.
   * @param tagId Tag ID. May include namespace (separated by ":"). May include nested levels (separated by "/").
   * @return Tag
   */
  public Tag tag(@NotNull String tagId) {
    TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
    if (tagManager == null) {
      throw new RuntimeException("No tag manager.");
    }
    String tagTitle = ResourceUtil.getName(StringUtils.substringAfter(tagId, ":"));
    try {
      return tagManager.createTag(tagId, tagTitle, null, true);
    }
    catch (AccessControlException | InvalidTagFormatException ex) {
      throw new RuntimeException("Unable to create tag: " + tagId, ex);
    }
  }

  /**
   * Create child resource below the page's <code>jcr:content</code> resource. If parent resource(s) do not exist they
   * are created automatically using <code>nt:unstructured</code> nodes.
   * @param page Page to create resource in
   * @param name Child resource name
   * @return Resource object
   */
  public final @NotNull Resource resource(@NotNull Page page, @NotNull String name) {
    return resource(page, name, ValueMap.EMPTY);
  }

  /**
   * Create child resource below the page's <code>jcr:content</code> resource. If parent resource(s) do not exist they
   * are created automatically using <code>nt:unstructured</code> nodes.
   * @param page Page to create resource in
   * @param name Child resource name
   * @param properties Properties for resource.
   * @return Resource object
   */
  public final @NotNull Resource resource(@NotNull Page page, @NotNull String name, @NotNull Map<String, Object> properties) {
    String path = page.getContentResource().getPath() + "/" + StringUtils.stripStart(name, "/");
    return resource(path, properties);
  }

  /**
   * Create child resource below the page's <code>jcr:content</code> resource. If parent resource(s) do not exist they
   * are created automatically using <code>nt:unstructured</code> nodes.
   * @param page Page to create resource in
   * @param name Child resource name
   * @param properties Properties for resource.
   * @return Resource object
   */
  public final @NotNull Resource resource(@NotNull Page page, @NotNull String name, @NotNull Object @NotNull... properties) {
    return resource(page, name, MapUtil.toMap(properties));
  }

}
