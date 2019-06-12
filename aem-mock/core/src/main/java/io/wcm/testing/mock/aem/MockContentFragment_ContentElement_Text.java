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
package io.wcm.testing.mock.aem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.ContentVariation;
import com.adobe.cq.dam.cfm.FragmentData;
import com.adobe.cq.dam.cfm.VariationDef;
import com.adobe.cq.dam.cfm.VariationTemplate;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.Rendition;

/**
 * Mock implementation of {@link ContentElement} for text-based content.
 */
class MockContentFragment_ContentElement_Text extends MockContentFragment_Versionable implements ContentElement {

  private final MockContentFragment contentFragment;
  private final Resource textElementResource;
  private final Asset asset;

  MockContentFragment_ContentElement_Text(MockContentFragment contentFragment, Resource textElementResource) {
    this.contentFragment = contentFragment;
    this.textElementResource = textElementResource;
    this.asset = contentFragment.getAsset();
  }

  @Override
  public <AdapterType> @Nullable AdapterType adaptTo(@NotNull Class<AdapterType> type) {
    return contentFragment.adaptTo(type);
  }

  @Override
  public String getName() {
    return textElementResource.getName();
  }

  @Override
  public String getTitle() {
    return textElementResource.getValueMap().get(JcrConstants.JCR_TITLE, getName());
  }

  @Override
  public String getContent() {
    return getContent(asset, getRenditionName());
  }

  @Override
  public void setContent(String content, String contentType) throws ContentFragmentException {
    setContent(asset, getRenditionName(), content, contentType);
  }

  @Override
  public String getContentType() {
    return getContentType(asset, getRenditionName());
  }

  static String getContent(Asset asset, String renditionName) {
    Rendition rendition = asset.getRendition(renditionName);
    if (rendition != null) {
      try (InputStream is = rendition.getStream()) {
        return IOUtils.toString(is, StandardCharsets.UTF_8);
      }
      catch (IOException ex) {
        throw new RuntimeException("Unable to get content fragment text from " + rendition.getPath());
      }
    }
    return null;
  }

  static void setContent(Asset asset, String renditionName, String content, String contentType) throws ContentFragmentException {
    Rendition rendition = asset.getRendition(renditionName);
    if (rendition != null) {
      asset.removeRendition(rendition.getName());
    }
    try (ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
      asset.addRendition(renditionName, is, contentType);
    }
    catch (IOException ex) {
      throw new ContentFragmentException("Unable to ass rendition.", ex);
    }
  }

  static String getContentType(Asset asset, String renditionName) {
    Rendition rendition = asset.getRendition(renditionName);
    if (rendition != null) {
      return rendition.getMimeType();
    }
    return null;
  }

  private String getRenditionName() {
    if (StringUtils.equals(textElementResource.getName(), "main")) {
      return DamConstants.ORIGINAL_FILE;
    }
    else {
      return textElementResource.getName();
    }
  }

  @Override
  public ContentVariation createVariation(VariationTemplate template) throws ContentFragmentException {
    VariationDef variationDef = (VariationDef)template;
    return new MockContentFragment_ContentVariation_Text(variationDef, asset);
  }

  @Override
  public ContentVariation getVariation(String variationName) {
    return getVariationsStream()
        .filter(variation -> StringUtils.equals(variation.getName(), variationName))
        .findFirst().orElse(null);
  }

  @Override
  public Iterator<ContentVariation> getVariations() {
    return getVariationsStream().iterator();
  }

  private Stream<ContentVariation> getVariationsStream() {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(contentFragment.listAllVariations(), Spliterator.ORDERED), false)
        .map(def -> (ContentVariation)new MockContentFragment_ContentVariation_Text(def, asset));
  }

  @Override
  public void removeVariation(ContentVariation variation) throws ContentFragmentException {
    Rendition rendition = asset.getRendition(variation.getName());
    if (rendition != null) {
      asset.removeRendition(rendition.getName());
    }
  }


  // --- unsupported operations ---

  @Override
  public ContentVariation getResolvedVariation(String variationName) {
    throw new UnsupportedOperationException();
  }

  // AEM 6.4/6.5
  @Override
  public FragmentData getValue() {
    throw new UnsupportedOperationException();
  }
  
  // AEM 6.4/6.5
  public void setValue(FragmentData arg0) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

}
