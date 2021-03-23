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

import java.util.Calendar;

import org.jetbrains.annotations.NotNull;

import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.ContentVariation;
import com.adobe.cq.dam.cfm.FragmentData;
import com.adobe.cq.dam.cfm.SyncStatus;
import com.adobe.cq.dam.cfm.VariationDef;
import com.day.cq.dam.api.Asset;

/**
 * Mock implementation of {@link ContentVariation}.
 */
class MockContentFragment_ContentVariation_Text extends MockContentFragment_Versionable implements ContentVariation {

  private final VariationDef variationDef;
  private final Asset asset;

  MockContentFragment_ContentVariation_Text(VariationDef variationDef, Asset asset) {
    this.variationDef = variationDef;
    this.asset = asset;
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
    return MockContentFragment_ContentElement_Text.getContent(asset, getName());
  }

  @Override
  public void setContent(String content, String contentType) throws ContentFragmentException {
    MockContentFragment_ContentElement_Text.setContent(asset, getName(), content, contentType);
  }

  @Override
  public String getContentType() {
    return MockContentFragment_ContentElement_Text.getContentType(asset, getName());
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
  public void setValue(FragmentData fragmentDataq) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  // latest AEM Cloud API
  public @NotNull Calendar getCreated() {
    throw new UnsupportedOperationException();
  }

}
