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

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static com.day.cq.wcm.api.NameConstants.NT_TEMPLATE;
import static io.wcm.testing.mock.aem.MockTemplate.NN_INITIAL;
import static io.wcm.testing.mock.aem.MockTemplate.NN_POLICIES;
import static io.wcm.testing.mock.aem.MockTemplate.NN_STRUCTURE;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.wcm.api.policies.ContentPolicyMapping;
import com.google.common.collect.ImmutableMap;

import io.wcm.testing.mock.aem.builder.ContentBuilder;

/**
 * Implements a very simplified storage concept for storing and resolving content policies and
 * their mappings. Basically it stores one global content policy per resource type, and one mapping
 * for it. This is usually enough for unit tests.
 */
public final class MockContentPolicyStorage {

  static final String RT_CONTENTPOLICY = "wcm/core/components/policy/policy";
  static final String RT_CONTENT_POLICY_MAPPING = "wcm/core/components/policies/mapping";
  static final String RT_CONTENT_POLICY_MAPPINGS = "wcm/core/components/policies/mappings";
  static final String PN_POLICY = "cq:policy";

  static final String MOCK_POLICIES_PATH = "/conf/$aem-mock$/settings/wcm/policies";
  static final String MOCK_TEMPLATE_PATH = "/conf/$aem-mock$/settings/wcm/templates/$mock-template$";
  static final String MOCK_POLICY_NAME = "$mock-policy";

  private MockContentPolicyStorage() {
    // static methods only
  }

  /**
   * Creates a mocked content policy with the given properties and maps it to all content resources with the given
   * resource type. This is a shortcut to easily test your components with a content policy.
   * @param resourceType Resource type that should be mapped to the content policy
   * @param properties Properties for the content policy
   * @param resourceResolver Resource resolver
   * @return New content policy mapping
   */
  public static @NotNull ContentPolicyMapping storeContentPolicyMapping(@NotNull String resourceType,
      @NotNull Map<String, Object> properties, @NotNull ResourceResolver resourceResolver) {

    String relativeResourceType = makeResourceTypeRelative(resourceType);

    // ensure mock template exists
    ContentBuilder builder = new ContentBuilder(resourceResolver);
    ensureMockTemplate(resourceResolver, builder);

    // store content policy
    String relativePolicyPath = relativeResourceType + "/" + MOCK_POLICY_NAME;
    String policyPath = MOCK_POLICIES_PATH + "/" + relativePolicyPath;
    storeResource(policyPath, properties, resourceResolver, builder);

    // store policy mapping
    Map<String, Object> policyMappingProperties = ImmutableMap.<String, Object>of(
        PROPERTY_RESOURCE_TYPE, RT_CONTENT_POLICY_MAPPING,
        PN_POLICY, relativePolicyPath);
    String policyMappingPath = buildPolicyMappingPath(relativeResourceType);
    Resource mappingResource = storeResource(policyMappingPath, policyMappingProperties, resourceResolver, builder);

    ContentPolicyMapping mapping = mappingResource.adaptTo(ContentPolicyMapping.class);
    if (mapping == null) {
      throw new RuntimeException("Unable to map to ContentPolicyMapping.");
    }
    return mapping;
  }

  /**
   * Get content policy mapping that was stored for the given resource type.
   * @param resourceType Resource type
   * @param resourceResolver Resource resolver
   * @return Content policy mapping or null if none found
   */
  public static @Nullable ContentPolicyMapping getContentPolicyMapping(@NotNull String resourceType,
      @NotNull ResourceResolver resourceResolver) {
    String relativeResourceType = makeResourceTypeRelative(resourceType);
    String policyMappingPath = buildPolicyMappingPath(relativeResourceType);
    Resource resource = resourceResolver.getResource(policyMappingPath);
    if (resource != null) {
      return resource.adaptTo(ContentPolicyMapping.class);
    }
    return null;
  }

  /**
   * Build path to policy mapping for given resource type.
   * @param relativeResourceType Relative resource type
   * @return Path to policy mapping
   */
  private static String buildPolicyMappingPath(@NotNull String relativeResourceType) {
    return MOCK_TEMPLATE_PATH + "/" + NN_POLICIES + "/" + JCR_CONTENT + "/" + relativeResourceType;
  }

  /**
   * Writes properties to resource. Overwrites existing resources.
   * @param path Path
   * @param properties Properties
   * @param resourceResolver Resource resolver
   * @param builder Content builder
   * @return Resource
   */
  private static Resource storeResource(@NotNull String path, @NotNull Map<String, Object> properties,
      @NotNull ResourceResolver resourceResolver, @NotNull ContentBuilder builder) {

    // delete resource if it exists already
    Resource resource = resourceResolver.getResource(path);
    if (resource != null) {
      try {
        resourceResolver.delete(resource);
      }
      catch (PersistenceException ex) {
        throw new RuntimeException("Unable to delete resource " + path, ex);
      }
    }

    // create resource
    return builder.resource(path, properties);
  }

  /**
   * Ensures that the mock template to store policy mappings in exits. If not, it is created.
   * @param resourceResolver Resource resolver
   * @param builder Content builder
   */
  private static void ensureMockTemplate(@NotNull ResourceResolver resourceResolver, @NotNull ContentBuilder builder) {
    Resource template = resourceResolver.getResource(MOCK_TEMPLATE_PATH);
    if (template == null) {
      template = builder.resource(MOCK_TEMPLATE_PATH,
          JCR_PRIMARYTYPE, NT_TEMPLATE);
      builder.resource(template, JCR_CONTENT,
          JCR_PRIMARYTYPE, "cq:PageContent",
          JCR_TITLE, template.getName());
      builder.page(template.getPath() + "/" + NN_INITIAL);
      builder.page(template.getPath() + "/" + NN_POLICIES);
      builder.page(template.getPath() + "/" + NN_STRUCTURE);
    }
  }

  /**
   * Ensures the given resource is relative (not starting with /apps, /libs, or /)
   * @param resourceType Resource type
   * @return Relative resource type
   */
  private static @NotNull String makeResourceTypeRelative(@NotNull String resourceType) {
    if (StringUtils.startsWith(resourceType, "/apps/")) {
      return StringUtils.substringAfter(resourceType, "/apps/");
    }
    else if (StringUtils.startsWith(resourceType, "/libs/")) {
      return StringUtils.substringAfter(resourceType, "/libs/");
    }
    else if (StringUtils.startsWith(resourceType, "/")) {
      return StringUtils.substringAfter(resourceType, "/");
    }
    return resourceType;
  }

}
