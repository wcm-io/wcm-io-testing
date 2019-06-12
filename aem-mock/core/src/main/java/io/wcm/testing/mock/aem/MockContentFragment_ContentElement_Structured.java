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

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.ContentVariation;
import com.adobe.cq.dam.cfm.FragmentData;
import com.adobe.cq.dam.cfm.VariationDef;
import com.adobe.cq.dam.cfm.VariationTemplate;
import com.day.cq.commons.jcr.JcrConstants;

/**
 * Mock implementation of {@link ContentElement} for structured content.
 */
class MockContentFragment_ContentElement_Structured extends MockContentFragment_Versionable implements ContentElement {

  private final MockContentFragment contentFragment;
  private final String structuredDataKey;
  private final ModifiableValueMap structuredDataProps;

  MockContentFragment_ContentElement_Structured(MockContentFragment contentFragment, String structuredDataKey,
      ModifiableValueMap structuredDataProps) {
    this.contentFragment = contentFragment;
    this.structuredDataKey = structuredDataKey;
    this.structuredDataProps = structuredDataProps;
  }

  @Override
  public <AdapterType> @Nullable AdapterType adaptTo(@NotNull Class<AdapterType> type) {
    return contentFragment.adaptTo(type);
  }

  @Override
  public String getName() {
    return structuredDataKey;
  }

  @Override
  public String getTitle() {
    return structuredDataKey;
  }

  @Override
  public String getContent() {
    return getContent(structuredDataKey, structuredDataProps);
  }

  @Override
  public void setContent(String content, String contentType) throws ContentFragmentException {
    setContent(structuredDataKey, structuredDataProps, content);
  }

  static String getContent(String structuredDataKey, ModifiableValueMap structuredDataProps) {
    StringBuilder result = new StringBuilder();
    Object value = structuredDataProps.get(structuredDataKey);
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

  static void setContent(String structuredDataKey, ModifiableValueMap structuredDataProps, String content) {
    structuredDataProps.put(structuredDataKey, content);
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public ContentVariation createVariation(VariationTemplate template) throws ContentFragmentException {
    VariationDef variationDef = (VariationDef)template;
    return new MockContentFragment_ContentVariation_Structured(variationDef, structuredDataKey,
        getVariationStrucuturedDataProps(template.getName()));
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
        .map(def -> (ContentVariation)new MockContentFragment_ContentVariation_Structured(def, structuredDataKey,
            getVariationStrucuturedDataProps(def.getName())));
  }

  private ModifiableValueMap getVariationStrucuturedDataProps(String variationName) {
    Resource contentResource = contentFragment.getContentResource();
    try {
      return ResourceUtil.getOrCreateResource(contentResource.getResourceResolver(),
          contentResource.getPath() + "/data/" + variationName, JcrConstants.NT_UNSTRUCTURED,
          JcrConstants.NT_UNSTRUCTURED, false).adaptTo(ModifiableValueMap.class);
    }
    catch (PersistenceException ex) {
      throw new RuntimeException("Unable to create variation data.");
    }
  }

  @Override
  public void removeVariation(ContentVariation variation) throws ContentFragmentException {
    ModifiableValueMap props = getVariationStrucuturedDataProps(variation.getName());
    props.remove(structuredDataKey);
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
  public void setValue(FragmentData fragmentData) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

}
