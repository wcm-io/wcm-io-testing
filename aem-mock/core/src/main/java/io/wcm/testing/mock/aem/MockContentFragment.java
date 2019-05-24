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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.ElementTemplate;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.adobe.cq.dam.cfm.VariationDef;
import com.adobe.cq.dam.cfm.VariationTemplate;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.google.common.collect.ImmutableMap;

/**
 * Mock implementation of {@link ContentFragment}.
 */
class MockContentFragment extends MockContentFragment_Versionable implements ContentFragment {

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
          .map(key -> (ContentElement)new MockContentFragment_ContentElement_Structured(this, key, structuredDataProps))
          .iterator();
    }
    else if (modelElementsResource != null) {
      return StreamSupport.stream(modelElementsResource.getChildren().spliterator(), false)
          .map(resource -> (ContentElement)new MockContentFragment_ContentElement_Text(this, resource))
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
        return new MockContentFragment_ContentElement_Structured(this, elementName, structuredDataProps);
      }
    }
    else if (modelElementsResource != null) {
      //Fixing according to ContentFragment documentation for getElement method:
      //Parameters:
      //elementName - The name of the element; null or empty string for the "main" or "master" element
      //See https://helpx.adobe.com/experience-manager/6-4/sites/developing/using/reference-materials/javadoc/com/adobe/cq/dam/cfm/ContentFragment.html#getElement-java.lang.String-

      Resource resource = null;
      String validatedElementName = elementName;
      if(elementName == null || elementName.equals("")) {
    	validatedElementName = "main";
    	resource = modelElementsResource.getChild(elementName);
    	if (resource == null) {
          validatedElementName = "master";
          resource = modelElementsResource.getChild(elementName);
        }
      } else {
    	  resource = modelElementsResource.getChild(validatedElementName);
      }
      
      if (resource != null) {
        return new MockContentFragment_ContentElement_Text(this, resource);
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

  @Override
  public VariationTemplate createVariation(String name, String title, String description) throws ContentFragmentException {
    ResourceResolver resourceResolver = contentResource.getResourceResolver();
    try {
      Resource variations = ResourceUtil.getOrCreateResource(resourceResolver, contentResource.getPath() + "/model/variations",
          JcrConstants.NT_UNSTRUCTURED, JcrConstants.NT_UNSTRUCTURED, false);
      if (variations.getChild(name) != null) {
        throw new ContentFragmentException("Variation " + name + " already exists.");
      }
      Resource child = resourceResolver.create(variations, name, ImmutableMap.<String, Object>of(
          "name", name,
          JcrConstants.JCR_TITLE, StringUtils.defaultString(title, name),
          JcrConstants.JCR_DESCRIPTION, StringUtils.defaultString(description)));
      return new MockContentFragment_VariationDef(child);
    }
    catch (PersistenceException ex) {
      throw new ContentFragmentException("Unable to create variation: " + name, ex);
    }
  }

  @Override
  public Iterator<VariationDef> listAllVariations() {
    Resource variations = contentResource.getChild("model/variations");
    if (variations == null) {
      return Collections.emptyIterator();
    }
    return StreamSupport.stream(variations.getChildren().spliterator(), false)
        .map(resource -> (VariationDef)new MockContentFragment_VariationDef(resource))
        .iterator();
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

}
