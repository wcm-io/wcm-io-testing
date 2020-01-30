/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 - 2015 wcm.io
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

import static com.day.cq.tagging.TagConstants.NAMESPACE_DELIMITER;
import static com.day.cq.tagging.TagConstants.SEPARATOR;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;

import com.day.cq.commons.Filter;
import com.day.cq.commons.LanguageUtil;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagConstants;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.NameConstants;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Mock implementation of {@link Tag}.
 */
@SuppressWarnings("null")
class MockTag extends SlingAdaptable implements Tag, Comparable<Tag> {

  /** resource being represented as a Tag */
  private final Resource resource;

  @SuppressWarnings("unused")
  MockTag(@NotNull Resource resource) {
    if (resource == null) {
      throw new IllegalArgumentException("resource is null");
    }
    if (!resource.getPath().startsWith(MockTagManager.getTagRootPath() + "/")) {
      throw new IllegalArgumentException("Tags should exist under " + MockTagManager.getTagRootPath());
    }
    this.resource = resource;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Tag)) {
      return false;
    }
    Tag tag = (Tag)o;
    Resource tagResource = tag.adaptTo(Resource.class);
    return tagResource != null && StringUtils.equals(resource.getPath(), tagResource.getPath());
  }

  @Override
  public int hashCode() {
    return resource.getPath().hashCode();
  }

  @Override
  public int compareTo(Tag tag) {
    Resource tagResource = tag.adaptTo(Resource.class);
    if (tagResource != null) {
      return resource.getPath().compareTo(tagResource.getPath());
    }
    else {
      return -1;
    }
  }

  @Override
  public String toString() {
    StringBuilder string = new StringBuilder()
        .append("Tag [")
        .append("path=").append(getPath())
        .append(", title=").append(getTitle())
        .append(", desc=").append(getDescription())
        .append("]");
    return string.toString();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == Resource.class) {
      return (AdapterType)this.resource;
    }
    AdapterType result = super.adaptTo(type);
    if (result == null) {
      result = this.resource.adaptTo(type);
    }
    return result;
  }

  /**
   * Find all nodes tagged with this tag.
   */
  @Override
  @SuppressFBWarnings("STYLE")
  public Iterator<Resource> find() {
    TagManager tagManager = resource.getResourceResolver().adaptTo(TagManager.class);
    return tagManager.find(getPath());
  }

  @Override
  @SuppressFBWarnings("STYLE")
  public long getCount() {
    TagManager tagManager = resource.getResourceResolver().adaptTo(TagManager.class);
    return tagManager.find(getPath()).getSize();
  }

  @Override
  public String getDescription() {
    return resource.getValueMap().get(JcrConstants.JCR_DESCRIPTION, String.class);
  }

  @Override
  public long getLastModified() {
    Calendar lastMod = resource.getValueMap().get(NameConstants.PN_LAST_MOD, Calendar.class);
    if (lastMod == null) {
      // not documented, but testing actual behavior reveals that this should be 0
      return 0;
    }
    return lastMod.getTimeInMillis();
  }

  @Override
  public String getLastModifiedBy() {
    return resource.getValueMap().get(NameConstants.PN_LAST_MOD_BY, String.class);
  }

  @Override
  public String getLocalTagID() {
    if (isNamespace()) {
      return "";
    }
    String tagID = getTagID();
    tagID = tagID.substring(tagID.indexOf(NAMESPACE_DELIMITER) + 1);

    return tagID;
  }

  @Override
  public String getLocalizedTitle(Locale locale) {
    if (locale == null) {
      return null;
    }
    ValueMap properties = resource.getValueMap();
    String localeStr = locale.getLanguage() + "_" + locale.getCountry();
    String title = properties.get(StringUtils.lowerCase(JcrConstants.JCR_TITLE + "." + localeStr), String.class);
    if (title == null) {
      localeStr = locale.getLanguage();
      title = properties.get(StringUtils.lowerCase(JcrConstants.JCR_TITLE + "." + localeStr), String.class);
    }

    return escapeTitle(title);
  }

  @Override
  public Map<Locale, String> getLocalizedTitles() {
    ValueMap properties = resource.getValueMap();
    Map<Locale, String> localeTitles = new HashMap<Locale, String>();
    for (Map.Entry<String, Object> propEntry : properties.entrySet()) {
      String propName = propEntry.getKey();
      if (!propName.startsWith(JcrConstants.JCR_TITLE + ".")) {
        continue;
      }
      String localeName = propName.substring(JcrConstants.JCR_TITLE.length() + 1);
      Locale locale = LanguageUtil.getLocale(localeName);
      localeTitles.put(locale, propEntry.getValue().toString());
    }
    return localeTitles;
  }

  @Override
  public String getName() {
    return resource.getName();
  }

  @Override
  public Tag getNamespace() {
    if (isNamespace()) {
      return this;
    }

    Tag namespace = this;
    while (!namespace.isNamespace()) {
      namespace = namespace.getParent();
    }
    return namespace;
  }

  @Override
  @SuppressFBWarnings("STYLE")
  public Tag getParent() {
    if (isNamespace()) {
      return null;
    }
    return resource.getParent().adaptTo(Tag.class);
  }

  @Override
  public String getPath() {
    return resource.getPath();
  }

  @Override
  @SuppressFBWarnings("STYLE")
  public String getTagID() {
    StringBuilder tagID = new StringBuilder(resource.getPath().length());
    Tag tag = this;
    while (!tag.isNamespace()) {
      Resource tagResource = tag.adaptTo(Resource.class);
      if (tagID.length() != 0) {
        tagID.insert(0, "/");
      }
      tagID.insert(0, tagResource.getName());
      tag = tag.getParent();
    }

    tagID.insert(0, NAMESPACE_DELIMITER);
    tagID.insert(0, tag.adaptTo(Resource.class).getName());

    return tagID.toString();
  }

  @Override
  public String getTitle() {
    String title = resource.getValueMap().get(JcrConstants.JCR_TITLE, getName());
    return escapeTitle(title);
  }

  @Override
  public String getTitle(Locale locale) {
    String title = getLocalizedTitle(locale);
    if (title == null) {
      title = getTitle();
    }
    return title;
  }

  @Override
  @SuppressFBWarnings("STYLE")
  public boolean isNamespace() {
    return MockTagManager.getTagRootPath().equals(resource.getParent().getPath());
  }

  @Override
  public Iterator<Tag> listAllSubTags() {
    return listChildren(null, true);
  }

  @Override
  public Iterator<Tag> listChildren() {
    return listChildren(null, false);
  }

  private Iterator<Tag> listChildren(Filter<Tag> filter, boolean recurse) {
    Collection<Tag> tags = new LinkedList<Tag>();
    Queue<Resource> resources = new LinkedList<Resource>();
    CollectionUtils.addAll(resources, resource.listChildren());

    while (!resources.isEmpty()) {
      Resource tagResource = resources.poll();

      if (recurse) {
        CollectionUtils.addAll(resources, tagResource.listChildren());
      }

      Tag tag = tagResource.adaptTo(Tag.class);
      if (tag == null) {
        continue;
      }

      if ((filter == null) || filter.includes(tag)) {
        tags.add(tag);
      }
    }

    return tags.iterator();
  }

  @Override
  public Iterator<Tag> listChildren(Filter<Tag> filter) {
    return listChildren(filter, false);
  }

  @Override
  public String getXPathSearchExpression(String property) {
    String tagRoot = MockTagManager.getTagRootPath();
    String ns = getNamespace().getName();
    String relPath = StringUtils.substringAfter(getPath(), tagRoot + "/" + ns + "/");
    boolean isDefaultNamespace = StringUtils.equals(ns, TagConstants.DEFAULT_NAMESPACE);
    if (isDefaultNamespace) {
      return "(@" + property + "='" + relPath + "' "
          + "or @" + property + "='" + tagRoot + "/" + ns + "/" + relPath + "' "
          + "or jcr:like(@" + property + ", '" + relPath + "/%') or "
          + "jcr:like(@" + property + ", '" + tagRoot + "/" + ns + "/" + relPath + "/%'))";
    }
    else {
      return "(@" + property + "='" + ns + ":" + relPath + "' "
          + "or @" + property + "='" + tagRoot + "/" + ns + "/" + relPath + "' "
          + "or jcr:like(@" + property + ", '" + ns + ":" + relPath + "/%') or "
          + "jcr:like(@" + property + ", '" + tagRoot + "/" + ns + "/" + relPath + "/%'))";
    }
  }

  private static String escapeTitle(String title) {
    return StringUtils.replace(StringUtils.replace(title, SEPARATOR, " "), NAMESPACE_DELIMITER, " ");
  }

  // --- unsupported operations ---

  @Override
  public String getGQLSearchExpression(String arg0) {
    throw new UnsupportedOperationException("Unsupported operation");
  }

  @Override
  public Map<Locale, String> getLocalizedTitlePaths() {
    throw new UnsupportedOperationException("Unsupported operation");
  }

  @Override
  public String getTitlePath() {
    throw new UnsupportedOperationException("Unsupported operation");
  }

  @Override
  public String getTitlePath(Locale arg0) {
    throw new UnsupportedOperationException("Unsupported operation");
  }

}
