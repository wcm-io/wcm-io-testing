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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.ContentVariation;
import com.adobe.cq.dam.cfm.VariationTemplate;
import com.adobe.cq.dam.cfm.VersionDef;
import com.adobe.cq.dam.cfm.VersionedContent;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Rendition;

/**
 * Mock implementation of {@link ContentElement}.
 */
class MockContentFragment_ContentElement implements ContentElement {

  private final MockContentFragment contentFragment;
  private final Resource textElementResource;
  private final String structuredDataKey;

  MockContentFragment_ContentElement(MockContentFragment contentFragment, Resource textElementResource) {
    this.contentFragment = contentFragment;
    this.textElementResource = textElementResource;
    this.structuredDataKey = null;
  }

  MockContentFragment_ContentElement(MockContentFragment contentFragment, String structuredDataKey) {
    this.contentFragment = contentFragment;
    this.textElementResource = null;
    this.structuredDataKey = structuredDataKey;
  }

  @Override
  public <AdapterType> @Nullable AdapterType adaptTo(@NotNull Class<AdapterType> type) {
    return contentFragment.adaptTo(type);
  }

  @Override
  public String getName() {
    if (textElementResource != null) {
      return textElementResource.getName();
    }
    else {
      return structuredDataKey;
    }
  }

  @Override
  public String getTitle() {
    if (textElementResource != null) {
      return textElementResource.getValueMap().get(JcrConstants.JCR_TITLE, getName());
    }
    else {
      return structuredDataKey;
    }
  }

  @Override
  public String getContent() {
    if (textElementResource != null) {
      return getTextContent();
    }
    else {
      return getStructuredContent();
    }
  }

  private String getTextContent() {
    Rendition rendition = getTextRendition();
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

  private Rendition getTextRendition() {
    if (StringUtils.equals(textElementResource.getName(), "main")) {
      return contentFragment.getAsset().getOriginal();
    }
    else {
      return contentFragment.getAsset().getRendition(textElementResource.getName());
    }
  }


  private String getStructuredContent() {
    StringBuilder result = new StringBuilder();
    Object value = contentFragment.getStructuredDataProps().get(structuredDataKey);
    if (value != null) {
      if (value.getClass().isArray()) {
        int length = Array.getLength(value);
        boolean first = true;
        for (int i = 0; i < length; i++) {
          if (first) {
            first = false;
          }
          else {
            result.append("\n");
          }
          result.append(Array.get(value, i).toString());
        }
      }
      else {
        result.append(value.toString());
      }
    }
    return result.toString();
  }

  @Override
  public String getContentType() {
    if (textElementResource != null) {
      Rendition rendition = getTextRendition();
      if (rendition != null) {
        return rendition.getMimeType();
      }
    }
    return null;
  }


  // --- unsupported operations ---

  @Override
  public VersionDef createVersion(String label, String comment) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public VersionedContent getVersionedContent(VersionDef version) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<VersionDef> listVersions() throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ContentVariation createVariation(VariationTemplate template) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ContentVariation getResolvedVariation(String variationName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ContentVariation getVariation(String variationName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<ContentVariation> getVariations() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeVariation(ContentVariation variation) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setContent(String content, String contentType) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

}
