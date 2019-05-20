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
package io.wcm.testing.mock.aem;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventAdmin;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagConstants;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.components.ComponentManager;
import com.day.cq.wcm.api.designer.Designer;

/**
 * Mock adapter factory for AEM-related adaptions.
 */
@Component(service = AdapterFactory.class,
    property = {
        AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.Resource",
        AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.ResourceResolver",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.Page",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.PageManager",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.Template",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.components.ComponentManager",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.dam.api.Asset",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.dam.api.AssetManager",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.dam.api.Rendition",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.tagging.TagManager",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.tagging.Tag",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.designer.Designer",
        AdapterFactory.ADAPTER_CLASSES + "=com.adobe.cq.dam.cfm.ContentFragment"
    })
@ProviderType
public class MockAemAdapterFactory implements AdapterFactory {

  @Reference
  private EventAdmin eventAdmin;

  @Override
  public @Nullable <AdapterType> AdapterType getAdapter(final @NotNull Object adaptable, final @NotNull Class<AdapterType> type) {
    if (adaptable instanceof Resource) {
      return getAdapter((Resource)adaptable, type);
    }
    if (adaptable instanceof ResourceResolver) {
      return getAdapter((ResourceResolver)adaptable, type);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private @Nullable <AdapterType> AdapterType getAdapter(@NotNull final Resource resource, @NotNull final Class<AdapterType> type) {
    if (type == Page.class && isPrimaryType(resource, NameConstants.NT_PAGE)) {
      return (AdapterType)new MockPage(resource);
    }
    if (type == Template.class && isPrimaryType(resource, NameConstants.NT_TEMPLATE)) {
      return (AdapterType)new MockTemplate(resource);
    }
    if (type == Asset.class && DamUtil.isAsset(resource)) {
      return (AdapterType)new MockAsset(resource, eventAdmin);
    }
    if (type == Rendition.class && DamUtil.isRendition(resource)) {
      return (AdapterType)new MockRendition(resource);
    }
    if (type == Tag.class && isPrimaryType(resource, TagConstants.NT_TAG)) {
      return (AdapterType)new MockTag(resource);
    }
    if (type == ContentFragment.class && DamUtil.isAsset(resource)) {
      return (AdapterType)new MockContentFragment(resource);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private @Nullable <AdapterType> AdapterType getAdapter(@NotNull final ResourceResolver resolver, @NotNull final Class<AdapterType> type) {
    if (type == PageManager.class) {
      return (AdapterType)new MockPageManager(resolver);
    }
    if (type == ComponentManager.class) {
      return (AdapterType)new MockComponentManager(resolver);
    }
    if (type == TagManager.class) {
      return (AdapterType)new MockTagManager(resolver);
    }
    if (type == Designer.class) {
      return (AdapterType)new MockDesigner();
    }
    if (type == AssetManager.class) {
      return (AdapterType)new MockAssetManager(resolver, eventAdmin);
    }
    return null;
  }

  private boolean isPrimaryType(@NotNull final Resource resource, final String primaryType) {
    Node node = resource.adaptTo(Node.class);
    if (node != null) {
      // JCR-based resource resolver
      try {
        return StringUtils.equals(node.getPrimaryNodeType().getName(), primaryType);
      }
      catch (RepositoryException ex) {
        // ignore
        return false;
      }
    }
    else {
      // sling resource resolver mock
      ValueMap props = resource.getValueMap();
      return StringUtils.equals(props.get(JcrConstants.JCR_PRIMARYTYPE, String.class), primaryType);
    }
  }

}
