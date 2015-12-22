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

import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.commons.Filter;
import com.day.cq.commons.LanguageUtil;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.commons.DeepResourceIterator;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

/**
 * Mock implementation of {@link Page}.
 */
class MockPage extends SlingAdaptable implements Page {

  private final Resource resource;
  private final Resource contentResource;
  private final ResourceResolver resourceResolver;
  private final ValueMap properties;

  MockPage(final Resource resource) {
    this.resource = resource;
    this.contentResource = this.resource.getChild(JcrConstants.JCR_CONTENT);
    this.resourceResolver = resource.getResourceResolver();
    if (this.contentResource != null) {
      this.properties = this.contentResource.getValueMap();
    }
    else {
      this.properties = ValueMap.EMPTY;
    }
  }

  @Override
  public String getPath() {
    return this.resource.getPath();
  }

  @Override
  public String getName() {
    return this.resource.getName();
  }

  @Override
  public String getTitle() {
    return this.properties.get(JcrConstants.JCR_TITLE, String.class);
  }

  @Override
  public String getPageTitle() {
    return this.properties.get(NameConstants.PN_PAGE_TITLE, String.class);
  }

  @Override
  public String getNavigationTitle() {
    return this.properties.get(NameConstants.PN_NAV_TITLE, String.class);
  }

  @Override
  public String getDescription() {
    return this.properties.get(JcrConstants.JCR_DESCRIPTION, String.class);
  }

  @Override
  public PageManager getPageManager() {
    return this.resourceResolver.adaptTo(PageManager.class);
  }

  @Override
  public Resource getContentResource() {
    return this.contentResource;
  }

  @Override
  public Resource getContentResource(final String relPath) {
    return this.contentResource.getChild(relPath);
  }

  @Override
  public boolean hasChild(final String name) {
    return this.resource.getChild(name) != null;
  }

  @Override
  public int getDepth() {
    if (StringUtils.equals("/", this.resource.getPath())) {
      return 0;
    }
    else {
      return StringUtils.countMatches(this.resource.getPath(), "/");
    }
  }

  @Override
  public Page getParent() {
    Resource parentResource = this.resource.getParent();
    if (parentResource != null) {
      return parentResource.adaptTo(Page.class);
    }
    return null;
  }

  @Override
  public Page getParent(final int level) {
    String parentPath = ResourceUtil.getParent(this.resource.getPath(), level);
    return getPage(parentPath);
  }

  private Page getPage(final String path) {
    if (StringUtils.isNotEmpty(path)) {
      Resource pageResource = this.resourceResolver.getResource(path);
      if (pageResource != null) {
        return pageResource.adaptTo(Page.class);
      }
    }
    return null;
  }

  @Override
  public Page getAbsoluteParent(final int level) {
    String parentPath = Text.getAbsoluteParent(this.resource.getPath(), level);
    return getPage(parentPath);
  }

  @Override
  public ValueMap getProperties() {
    return this.properties;
  }

  @Override
  public ValueMap getProperties(final String relPath) {
    Resource childResource = getContentResource(relPath);
    if (childResource != null) {
      return childResource.getValueMap();
    }
    return null;
  }

  @Override
  public boolean isHideInNav() {
    return this.properties.get(NameConstants.PN_HIDE_IN_NAV, false);
  }

  @Override
  public boolean hasContent() {
    return this.contentResource != null;
  }

  @Override
  public boolean isValid() {
    return timeUntilValid() == 0L;
  }

  @Override
  public long timeUntilValid() {
    if (!hasContent()) {
      return Long.MIN_VALUE;
    }
    Calendar on = getOnTime();
    Calendar off = getOffTime();
    if (on == null && off == null) {
      return 0L;
    }
    long now = System.currentTimeMillis();
    long timeDiffOn = (on == null) ? 0L : on.getTimeInMillis() - now;
    if (timeDiffOn > 0L) {
      return timeDiffOn;
    }
    long timeDiffOff = (off == null) ? 0L : off.getTimeInMillis() - now;
    if (timeDiffOff < 0L) {
      return timeDiffOff;
    }
    return 0L;
  }

  @Override
  public Calendar getOnTime() {
    return this.properties.get(NameConstants.PN_ON_TIME, Calendar.class);
  }

  @Override
  public Calendar getOffTime() {
    return this.properties.get(NameConstants.PN_OFF_TIME, Calendar.class);
  }

  @Override
  public String getLastModifiedBy() {
    return this.properties.get(NameConstants.PN_PAGE_LAST_MOD_BY, String.class);
  }

  @Override
  public Calendar getLastModified() {
    return this.properties.get(NameConstants.PN_PAGE_LAST_MOD, Calendar.class);
  }

  @Override
  public String getVanityUrl() {
    return this.properties.get(NameConstants.PN_SLING_VANITY_PATH, String.class);
  }

  @Override
  public Template getTemplate() {
    String templatePath = this.properties.get(NameConstants.PN_TEMPLATE, String.class);
    if (StringUtils.isNotEmpty(templatePath)) {
      Resource templateResource = this.resourceResolver.getResource(templatePath);
      if (templateResource != null) {
        return templateResource.adaptTo(Template.class);
      }
    }
    return null;
  }

  @Override
  public Iterator<Page> listChildren() {
    return listChildren(null);
  }

  @Override
  public Iterator<Page> listChildren(final Filter<Page> filter) {
    return listChildren(filter, false);
  }

  @Override
  public Iterator<Page> listChildren(final Filter<Page> filter, final boolean deep) {
    Iterator<Resource> resources;
    if (deep) {
      resources = new DeepResourceIterator(resource);
    }
    else {
      resources = resource.getResourceResolver().listChildren(resource);
    }

    // transform resources to pages
    final Iterator<Page> pages = Iterators.transform(resources, new Function<Resource, Page>() {
      @Override
      public Page apply(Resource resourceItem) {
        return resourceItem.adaptTo(Page.class);
      }
    });

    // filter pages
    return Iterators.filter(pages, new Predicate<Page>() {
      @Override
      public boolean apply(Page pageItem) {
        return pageItem != null && (filter == null || filter.includes(pageItem));
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public <AdapterType> AdapterType adaptTo(final Class<AdapterType> type) {
    if (type == Resource.class) {
      return (AdapterType)this.resource;
    }
    AdapterType result = super.adaptTo(type);
    if (result == null) {
      result = this.resource.adaptTo(type);
    }
    return result;
  }

  @Override
  public Locale getLanguage(final boolean ignoreContent) {
    // check for language content property
    if (!ignoreContent) {
      InheritanceValueMap inheritanceValueMap = new HierarchyNodeInheritanceValueMap(getContentResource());
      String language = inheritanceValueMap.getInherited(JcrConstants.JCR_LANGUAGE, String.class);
      if (language != null) {
        Locale contentLocale = LanguageUtil.getLocale(language);
        if (contentLocale != null) {
          return contentLocale;
        }
      }
    }

    // check for lanugage in path
    Locale localeFromPath = getLocaleFromPath(this);
    if (localeFromPath != null) {
      return localeFromPath;
    }

    // fallback to default locale
    return Locale.getDefault();
  }

  private Locale getLocaleFromPath(Page page) {
    Locale locale = LanguageUtil.getLocale(page.getName());
    if (locale != null) {
      return locale;
    }
    Page parentPage = page.getParent();
    if (parentPage != null) {
      return getLocaleFromPath(parentPage);
    }
    else {
      return null;
    }
  }

  @Override
  public int hashCode() {
    return getPath().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MockPage)) {
      return false;
    }
    return StringUtils.equals(getPath(), ((MockPage)obj).getPath());
  }

  @Override
  public Tag[] getTags() {
    return resourceResolver.adaptTo(TagManager.class).getTags(contentResource);
  }

  @Override
  public String toString() {
    return "MockPage [path=" + resource.getPath() + ", props=" + properties + "]";
  }

  // Required for AEM 6.1 API
  public Calendar getDeleted() {
    return null;
  }

  // Required for AEM 6.1 API
  public String getDeletedBy() {
    return null;
  }


  // --- unsupported operations ---

  @Override
  public void lock() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLocked() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getLockOwner() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canUnlock() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unlock() throws WCMException {
    throw new UnsupportedOperationException();
  }

}
