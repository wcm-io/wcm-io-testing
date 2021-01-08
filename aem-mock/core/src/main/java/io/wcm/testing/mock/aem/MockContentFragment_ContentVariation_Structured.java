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

import org.apache.sling.api.resource.ModifiableValueMap;

import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.ContentVariation;
import com.adobe.cq.dam.cfm.FragmentData;
import com.adobe.cq.dam.cfm.SyncStatus;
import com.adobe.cq.dam.cfm.VariationDef;

/**
 * Mock implementation of {@link ContentVariation}.
 */
class MockContentFragment_ContentVariation_Structured extends MockContentFragment_Versionable implements ContentVariation {

  private final VariationDef variationDef;
  private final String structuredDataKey;
  private final ModifiableValueMap structuredDataProps;

  MockContentFragment_ContentVariation_Structured(VariationDef variationDef,
      String structuredDataKey, ModifiableValueMap structuredDataProps) {
    this.variationDef = variationDef;
    this.structuredDataKey = structuredDataKey;
    this.structuredDataProps = structuredDataProps;
  }

  @Override
  public String getName() {
    return variationDef.getName();
  }

  @Override
  public String getDescription() {
    return variationDef.getDescription();
  }

  @Override
  public String getTitle() {
    return variationDef.getTitle();
  }

  @Override
  public String getContent() {
    return MockContentFragment_ContentElement_Structured.getContent(structuredDataKey, structuredDataProps);
  }

  @Override
  public void setContent(String content, String mimeType) throws ContentFragmentException {
    MockContentFragment_ContentElement_Structured.setContent(structuredDataKey, structuredDataProps, content);
  }

  @Override
  public String getContentType() {
    return null;
  }


  // --- unsupported operations ---

  @Override
  public void synchronize() throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public SyncStatus getSyncStatus() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTitle(String title) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDescription(String description) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public FragmentData getValue() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setValue(FragmentData arg0) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

}
