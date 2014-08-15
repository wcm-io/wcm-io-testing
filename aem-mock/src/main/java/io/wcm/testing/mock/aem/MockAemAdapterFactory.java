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

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;

/**
 * Mock adapter factory for AEM-related adaptions.
 */
public class MockAemAdapterFactory implements AdapterFactory {

  @Override
  public <AdapterType> AdapterType getAdapter(final Object adaptable, final Class<AdapterType> type) {
    if (adaptable instanceof Resource) {
      return getAdapter((Resource)adaptable, type);
    }
    if (adaptable instanceof ResourceResolver) {
      return getAdapter((ResourceResolver)adaptable, type);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private <AdapterType> AdapterType getAdapter(final Resource resource, final Class<AdapterType> type) {
    if (type == Page.class) {
      if (isPrimaryType(resource, NameConstants.NT_PAGE)) {
        return (AdapterType)new MockPage(resource);
      }
    }
    if (type == Template.class) {
      if (isPrimaryType(resource, NameConstants.NT_TEMPLATE)) {
        return (AdapterType)new MockTemplate(resource);
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private <AdapterType> AdapterType getAdapter(final ResourceResolver resolver, final Class<AdapterType> type) {
    if (type == PageManager.class) {
      return (AdapterType)new MockPageManager(resolver);
    }
    return null;
  }

  private boolean isPrimaryType(final Resource resource, final String primaryType) {
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
