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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.google.common.collect.ImmutableMap;

/**
 * Helper class for building test content in the resource hierarchy with as less boilerplate code as possible.
 */
public final class ContentBuilder {

  static final String DUMMY_TEMPLATE = "/apps/sample/templates/template1";

  private final ResourceResolver resourceResolver;

  /**
   * @param resourceResolver Resource resolver
   */
  public ContentBuilder(ResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @return Page object
   */
  public Page page(String path) {
    return page(path, DUMMY_TEMPLATE, ValueMap.EMPTY);
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @param template Template
   * @return Page object
   */
  public Page page(String path, String template) {
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
  public Page page(String path, String template, String title) {
    return page(path, template, ImmutableMap.<String, Object>builder()
        .put(NameConstants.PN_TITLE, title)
        .build());
  }

  /**
   * Create content page.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @param template Template
   * @param contentProperties Properties for <code>jcr:content</code> node.
   * @return Page object
   */
  public Page page(String path, String template, Map<String, Object> contentProperties) {
    String parentPath = ResourceUtil.getParent(path);
    ensureResourceExists(parentPath);
    String name = ResourceUtil.getName(path);
    try {
      PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
      Page page = pageManager.create(parentPath, name, template, name, true);
      if (!contentProperties.isEmpty()) {
        ModifiableValueMap pageProperties = page.getContentResource().adaptTo(ModifiableValueMap.class);
        pageProperties.putAll(contentProperties);
        resourceResolver.commit();
      }
      return page;
    }
    catch (WCMException | PersistenceException ex) {
      throw new RuntimeException("Unable to create page at " + path, ex);
    }
  }

  /**
   * Create resource.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @return Resource object
   */
  public Resource resource(String path) {
    return resource(path, ValueMap.EMPTY);
  }

  /**
   * Create resource.
   * If parent resource(s) do not exist they are created automatically using <code>nt:unstructured</code> nodes.
   * @param path Page path
   * @param properties Properties for resource.
   * @return Resource object
   */
  public Resource resource(String path, Map<String, Object> properties) {
    String parentPath = ResourceUtil.getParent(path);
    Resource parentResource = ensureResourceExists(parentPath);
    String name = ResourceUtil.getName(path);
    try {
      return resourceResolver.create(parentResource, name, properties);
    }
    catch (PersistenceException ex) {
      throw new RuntimeException("Unable to create page at " + path, ex);
    }
  }

  /**
   * Ensure that a resource exists at the given path. If not, it is created using <code>nt:unstructured</code> node
   * type.
   * @param path Resource path
   * @return Resource at path (existing or newly created)
   */
  private Resource ensureResourceExists(String path) {
    if (StringUtils.isEmpty(path) || StringUtils.equals(path, "/")) {
      return resourceResolver.getResource("/");
    }
    Resource resource = resourceResolver.getResource(path);
    if (resource != null) {
      return resource;
    }
    String parentPath = ResourceUtil.getParent(path);
    String name = ResourceUtil.getName(path);
    Resource parentResource = ensureResourceExists(parentPath);
    try {
      resource = resourceResolver.create(parentResource, name, ImmutableMap.<String, Object>builder()
          .put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED).build());
      resourceResolver.commit();
      return resource;
    }
    catch (PersistenceException ex) {
      throw new RuntimeException("Unable to create resource at " + path, ex);
    }
  }

}
