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

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_DESCRIPTION;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LASTMODIFIED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static com.day.cq.wcm.api.NameConstants.NN_ICON_PNG;
import static com.day.cq.wcm.api.NameConstants.NN_THUMBNAIL_PNG;
import static com.day.cq.wcm.api.NameConstants.PN_RANKING;
import static com.day.cq.wcm.api.NameConstants.PN_SHORT_TITLE;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.jetbrains.annotations.NotNull;

import com.day.cq.wcm.api.Template;

/**
 * Mock implementation of {@link Template}.
 */
class MockTemplate extends ResourceWrapper implements Template {

  private static final String NN_INITIAL = "initial";
  private static final String NN_STRUCTURE = "structure";

  private final Resource resource;
  private final ValueMap properties;

  MockTemplate(@NotNull Resource resource) {
    super(resource);
    this.resource = resource;
    this.properties = resource.getValueMap();
  }

  @Override
  public String getPath() {
    return super.getPath();
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public String getTitle() {
    return this.properties.get(JCR_TITLE, String.class);
  }

  @Override
  public String getShortTitle() {
    return this.properties.get(PN_SHORT_TITLE, String.class);
  }

  @Override
  public String getDescription() {
    return this.properties.get(JCR_DESCRIPTION, String.class);
  }

  @Override
  public String getIconPath() {
    Resource iconResource = this.resource.getChild(NN_ICON_PNG);
    if (iconResource != null) {
      return iconResource.getPath();
    }
    else {
      return null;
    }
  }

  @Override
  public String getThumbnailPath() {
    Resource thumbnailResource = this.resource.getChild(NN_THUMBNAIL_PNG);
    if (thumbnailResource != null) {
      return thumbnailResource.getPath();
    }
    else {
      return null;
    }
  }

  @Override
  public Long getRanking() {
    return this.properties.get(PN_RANKING, Long.class);
  }

  @Override
  public int hashCode() {
    return getPath().hashCode();
  }

  // AEM 6.3
  public Calendar getLastModified() {
    return properties.get(JCR_LASTMODIFIED, Calendar.class);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MockTemplate)) {
      return false;
    }
    return StringUtils.equals(getPath(), ((MockTemplate)obj).getPath());
  }

  @Override
  @SuppressWarnings({ "unchecked", "null" })
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == Resource.class) {
      return (AdapterType)this.getResource();
    }
    return super.adaptTo(type);
  }

  @Override
  public boolean hasStructureSupport() {
    return resource.getChild(NN_STRUCTURE) != null;
  }

  @Override
  public String getInitialContentPath() {
    Resource initialContentResource;
    if (hasStructureSupport()) {
      initialContentResource = resource.getChild(NN_INITIAL + "/" + JCR_CONTENT);
    }
    else {
      initialContentResource = resource.getChild(JCR_CONTENT);
    }
    if (initialContentResource != null) {
      return initialContentResource.getPath();
    }
    else {
      return null;
    }
  }


  // --- unsupported operations ---

  @Override
  public boolean isAllowed(final String parentPath) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAllowedChild(final Template template) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void write(final JSONWriter jsonWriter) throws JSONException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPageTypePath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ValueMap getProperties() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAllowed(Resource arg0) {
    throw new UnsupportedOperationException();
  }

}
