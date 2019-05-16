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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.ElementTemplate;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.adobe.cq.dam.cfm.VariationDef;
import com.adobe.cq.dam.cfm.VariationTemplate;
import com.adobe.cq.dam.cfm.VersionDef;
import com.adobe.cq.dam.cfm.VersionedContent;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;

/**
 * Mock implementation of {@link ContentFragment}.
 */
class MockContentFragment implements ContentFragment {

  private final Resource assetResource;
  private final Asset asset;
  private final Resource contentResource;
  private final ModifiableValueMap contentProps;
  private final Resource metadataResource;
  private final ModifiableValueMap metadataProps;
  private final ModifiableValueMap structuredDataProps;
  private final Resource modelElementsResource;

  MockContentFragment(Resource assetResource) {
    this.assetResource = assetResource;
    this.asset = assetResource.adaptTo(Asset.class);

    this.contentResource = assetResource.getChild(JcrConstants.JCR_CONTENT);
    if (this.contentResource == null) {
      throw new IllegalArgumentException("Missing jcr:content node.");
    }

    this.contentProps = contentResource.adaptTo(ModifiableValueMap.class);

    this.metadataResource = contentResource.getChild(DamConstants.METADATA_FOLDER);
    if (this.metadataResource == null) {
      throw new IllegalArgumentException("Missing jcr:content/metadata node.");
    }
    this.metadataProps = metadataResource.adaptTo(ModifiableValueMap.class);

    Resource structuredDataResource = contentResource.getChild("data/master");
    if (structuredDataResource != null) {
      this.structuredDataProps = structuredDataResource.adaptTo(ModifiableValueMap.class);
    }
    else {
      this.structuredDataProps = null;
    }

    this.modelElementsResource = contentResource.getChild("model/elements");
  }

  @Override
  public String getName() {
    return assetResource.getName();
  }

  @Override
  public String getTitle() {
    return contentProps.get(JcrConstants.JCR_TITLE, assetResource.getName());
  }

  @Override
  public String getDescription() {
    return contentProps.get(JcrConstants.JCR_DESCRIPTION, "");
  }

  @Override
  public Map<String, Object> getMetaData() {
    return metadataProps;
  }

  @Override
  public void setTitle(String title) throws ContentFragmentException {
    contentProps.put(JcrConstants.JCR_TITLE, title);
  }

  @Override
  public void setDescription(String description) throws ContentFragmentException {
    contentProps.put(JcrConstants.JCR_DESCRIPTION, description);
  }

  @Override
  public void setMetaData(String name, Object value) throws ContentFragmentException {
    metadataProps.put(name, value);
  }

  @Override
  @SuppressWarnings({ "null", "unchecked" })
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == Resource.class) {
      return (AdapterType)assetResource;
    }
    else if (type == Asset.class) {
      return (AdapterType)assetResource.adaptTo(Asset.class);
    }
    return null;
  }

  @Override
  public Iterator<ContentElement> getElements() {
    if (structuredDataProps != null) {
      return structuredDataProps.keySet().stream()
          .map(key -> (ContentElement)new MockContentFragment_ContentElement(this, key))
          .iterator();
    }
    else if (modelElementsResource != null) {
      return StreamSupport.stream(modelElementsResource.getChildren().spliterator(), false)
          .map(resource -> (ContentElement)new MockContentFragment_ContentElement(this, resource))
          .iterator();
    }
    else {
      return Collections.emptyIterator();
    }
  }

  @Override
  public ContentElement getElement(String elementName) {
    if (structuredDataProps != null) {
      if (structuredDataProps.containsKey(elementName)) {
        return new MockContentFragment_ContentElement(this, elementName);
      }
    }
    else if (modelElementsResource != null) {
      Resource resource = modelElementsResource.getChild(elementName);
      if (resource != null) {
        return new MockContentFragment_ContentElement(this, resource);
      }
    }
    return null;
  }

  @Override
  public boolean hasElement(String elementName) {
    if (structuredDataProps != null) {
      return structuredDataProps.containsKey(elementName);
    }
    else if (modelElementsResource != null) {
      return modelElementsResource.getChild(elementName) != null;
    }
    return false;
  }

  Asset getAsset() {
    return this.asset;
  }

  Resource getContentResource() {
    return this.contentResource;
  }

  ModifiableValueMap getStructuredDataProps() {
    return this.structuredDataProps;
  }


  // --- unsupported operations ---


  @Override
  public FragmentTemplate getTemplate() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ContentElement createElement(ElementTemplate template) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public VariationTemplate createVariation(String name, String title, String description) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<VariationDef> listAllVariations() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<Resource> getAssociatedContent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addAssociatedContent(Resource content) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeAssociatedContent(Resource content) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

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

}
