package io.wcm.testing.mock.aem;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.jcr.Session;

import org.apache.commons.collections.CollectionUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.RangeIterator;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.InvalidTagFormatException;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagConstants;
import com.day.cq.tagging.TagException;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.NameConstants;

/**
 * Mock implementation of {@link TagManager}.
 */
class MockTagManager implements TagManager {

  // https://docs.adobe.com/docs/en/aem/6-0/develop/ref/javadoc/com/day/cq/tagging/TagManager.html

  /** resource type for created tags */
  private static final String TAG_RESOURCE_TYPE = "cq/tagging/components/tag";

  /** Root location in the JCR where tags lie */
  static final String TAGS_ROOT = "/etc/tags";

  private final ResourceResolver resourceResolver;
  private final Logger log;

  public MockTagManager(ResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
    log = LoggerFactory.getLogger(TagManager.class);

    // create some basic tag structure now, to avoid complications occurring later around this
    initTagsStructure();
  }

  private void initTagsStructure() {
    Resource defaultNamespace = resourceResolver.getResource(TAGS_ROOT + "/" + TagConstants.DEFAULT_NAMESPACE);
    // if it's already existing, then don't proceed any further
    if (defaultNamespace != null) {
      return;
    }
    Map<String, Object> etcProperties = new HashMap<String, Object>();
    etcProperties.put(JcrConstants.JCR_PRIMARYTYPE, JcrResourceConstants.NT_SLING_FOLDER);

    Map<String, Object> tagsProperties = new HashMap<String, Object>();
    tagsProperties.put(JcrConstants.JCR_PRIMARYTYPE, JcrResourceConstants.NT_SLING_FOLDER);
    tagsProperties.put(JcrConstants.JCR_TITLE, "Tags");
    // locale strings that are recognized languages in child tags
    tagsProperties.put("languages", new String[]{"en", "de", "es", "fr", "it", "pt_br", "zh_cn", "ch_tw", "ja", "ko_kr"});

    try {
      ResourceUtil.getOrCreateResource(resourceResolver, "/etc", etcProperties, null, true);
      ResourceUtil.getOrCreateResource(resourceResolver, TAGS_ROOT, tagsProperties, null, true);
      createTag(TagConstants.DEFAULT_NAMESPACE_ID, "Standard Tags", null);
    }
    catch (PersistenceException | InvalidTagFormatException e) {
      log.error("Error creating tags tree", e);
    }
  }

  private String getPathFromID(String tagID) throws InvalidTagFormatException {
    if (tagID == null) {
      throw new InvalidTagFormatException("tagID is null");
    }
    if (tagID.contains(TagConstants.NAMESPACE_DELIMITER)) {
      // namespace mode
      String tagPath = tagID.replaceFirst(TagConstants.NAMESPACE_DELIMITER, "/");
      if (tagPath.contains(TagConstants.NAMESPACE_DELIMITER)) {
        throw new InvalidTagFormatException("tag ID contains multiple namespace declarations");
      }
      // remove any possible trailing slashes, such as the namespace only case
      if (tagPath.endsWith("/")) {
        tagPath = tagPath.substring(0, tagPath.length() - 1);
      }
      return TAGS_ROOT + "/" + tagPath;
    }
    else if (tagID.startsWith("/")) {
      // absolute path mode
      if (!tagID.startsWith(TAGS_ROOT)) {
        // TODO: seems reasonable, but is it worth enforcing?
        throw new InvalidTagFormatException("Tags are only allowed to be under " + TAGS_ROOT);
      }
      return tagID;
    }
    else {
      // default namespace mode
      return TAGS_ROOT + "/" + TagConstants.DEFAULT_NAMESPACE + "/" + tagID;
    }
  }

  @Override
  public boolean canCreateTag(String tagID) throws InvalidTagFormatException {
    String tagPath = getPathFromID(tagID);
    return resourceResolver.getResource(tagPath) == null;
  }

  @Override
  public Tag createTag(String tagID, String title, String description)
      throws AccessControlException, InvalidTagFormatException {
    return createTag(tagID, title, description, true);
  }

  @Override
  public Tag createTag(String tagID, String title, String description, boolean autoSave)
      throws AccessControlException, InvalidTagFormatException {
    String tagPath = getPathFromID(tagID);
    Resource tagResource = resourceResolver.getResource(tagPath);
    if (tagResource != null) {
      return tagResource.adaptTo(Tag.class);
    }

    // ensure the parent exists first
    String parentTagPath = tagPath.substring(0, tagPath.lastIndexOf("/"));
    if (!TAGS_ROOT.equals(parentTagPath)) {
      createTag(parentTagPath, null, null, false);
    }

    // otherwise it needs to be made
    Map<String, Object> tagProps = new HashMap<String, Object>();
    tagProps.put(JcrConstants.JCR_PRIMARYTYPE, TagConstants.NT_TAG);
    tagProps.put(ResourceResolver.PROPERTY_RESOURCE_TYPE, TAG_RESOURCE_TYPE);
    if (title != null) {
      tagProps.put(JcrConstants.JCR_TITLE, title);
    }
    if (description != null) {
      tagProps.put(JcrConstants.JCR_DESCRIPTION, description);
    }
    tagProps.put(NameConstants.PN_LAST_MOD, Calendar.getInstance());
    tagProps.put(NameConstants.PN_LAST_MOD_BY, resourceResolver.getUserID());

    try {
      tagResource = ResourceUtil.getOrCreateResource(resourceResolver, tagPath, tagProps, null, autoSave);

      return tagResource.adaptTo(Tag.class);
    }
    catch (PersistenceException e) {
      log.error("failed to create tag", e);
      // throw this as a failure to indicate it failed
      throw new AccessControlException("failed to create tag");
    }
  }

  @Override
  public Tag createTagByTitle(String titlePath) throws AccessControlException, InvalidTagFormatException {
    return createTagByTitle(titlePath, true);
  }

  @Override
  public void deleteTag(Tag tag) throws AccessControlException {
    deleteTag(tag, true);
  }

  @Override
  public void deleteTag(Tag tag, boolean autoSave) throws AccessControlException {
    try {
      resourceResolver.delete(tag.adaptTo(Resource.class));
      if (autoSave) {
        resourceResolver.commit();
        resourceResolver.refresh();
      }
    } catch (PersistenceException e) {
      log.error("error deleting tag", e);
    }
  }

  @Override
  public RangeIterator<Resource> find(String tagID) {
    /* FIXME: this should be just /, but the mock framework seems to be
     * buggy around get/listChildren() from the / resource. */
    return find("/content", new String[]{tagID}, false);
  }

  @Override
  public RangeIterator<Resource> find(String basePath, String[] tagIDs) {
    return find(basePath, tagIDs, false);
  }

  @Override
  public RangeIterator<Resource> find(String basePath, String[] tagIDs, boolean oneMatchIsEnough) {
    Resource base = resourceResolver.getResource(basePath);
    if (base == null) {
      return new CollectionRangeIterator<Resource>(Collections.<Resource>emptyList());
    }

    Collection<String> tagPaths = new HashSet<String>(tagIDs.length);
    for (String tagID : tagIDs) {
      Tag tag = resolve(tagID);
      // clause - if tag does not exist, should return null.
      if (tag == null) {
        return null;
      }
      tagPaths.add(tag.adaptTo(Resource.class).getPath());
    }

    Queue<Resource> searchResources = new LinkedList<Resource>();
    searchResources.add(base);

    Collection<Resource> matchedResources = new ArrayList<Resource>();

    while (!searchResources.isEmpty()) {
      Resource resource = searchResources.poll();
      // add the children to search the entire tree
      CollectionUtils.addAll(searchResources, resource.listChildren());

      // now process the tags
      String[] resourceTags = resource.getValueMap().get(TagConstants.PN_TAGS, String[].class);
      if (resourceTags == null) {
        continue;
      }

      List<String> resourceTagPaths = new ArrayList<String>(resourceTags.length);
      try {
        for (String resourceTag : resourceTags) {
          resourceTagPaths.add(getPathFromID(resourceTag));
        }
      } catch (InvalidTagFormatException e) {
        log.error("invalid tag id encountered", e);
      }

      if (resourceTagPaths.isEmpty()) {
        continue;
      }

      boolean matches = false;
      if (oneMatchIsEnough) {
        // this is essentially an OR list, so break out on the first positive
        oneMatched:
          for (String tagPath : tagPaths) {
            for (String resourceTagPath : resourceTagPaths) {
              matches = doTagsMatch(resourceTagPath, tagPath);
              if (matches) {
                break oneMatched;
              }
            }
          }
      } else {
        // this is essentially an AND list, so break out on the first failure
        matches = true;
        for (String tagPath : tagPaths) {
          boolean tagMatched = false;
          for (Iterator<String> resourceTagPathIter = resourceTagPaths.iterator(); !tagMatched && resourceTagPathIter.hasNext();) {
            String resourceTagPath = resourceTagPathIter.next();
            tagMatched = doTagsMatch(resourceTagPath, tagPath);
          }
          // if no tag on the resource matched the current search tag, it fails the search
          if (!tagMatched) {
            matches = false;
            break;
          }
        }
      }

      if (matches) {
        matchedResources.add(resource);
      }
    }

    return new CollectionRangeIterator<Resource>(matchedResources);
  }

  /**
   * Test matching of tags. <em>matching</em> is defined as either being equivalent to or starting with.
   * @param haystack the tag (absolute path) to be tested as matching the <code>needle<code> tag.
   * @param needle the tag (absolute path) to verify the <code>haystack</code> as matching.
   * @return state of <code>haystack</code> tag matching the <code>needle</code> tag.
   */
  private boolean doTagsMatch(String haystack, String needle) {
    // clause - sub tags are included when searching for a parent tag
    return haystack.equals(needle) || haystack.startsWith(needle + "/");
  }

  private List<Tag> getNamespacesList() {
    Resource tagRoot = resourceResolver.getResource(TAGS_ROOT);
    List<Tag> namespaces = new ArrayList<Tag>();
    for (Iterator<Resource> resources = tagRoot.listChildren(); resources.hasNext();) {
      Resource resource = resources.next();
      Tag tag = resource.adaptTo(Tag.class);
      if (tag != null) {
        namespaces.add(tag);
      }
    }
    return namespaces;
  }

  @Override
  public Tag[] getNamespaces() {
    List<Tag> namespaces = getNamespacesList();
    return namespaces.toArray(new Tag[namespaces.size()]);
  }

  @Override
  public Iterator<Tag> getNamespacesIter() {
    return getNamespacesList().iterator();
  }

  @Override
  public Session getSession() {
    return resourceResolver.adaptTo(Session.class);
  }

  @Override
  public Tag[] getTags(Resource resource) {
    return getTagsForSubtree(resource, true);
  }

  @Override
  public Tag[] getTagsForSubtree(Resource resource, boolean shallow) {
    Collection<Tag> tags = collectResourceTags(resource, !shallow);
    return tags.toArray(new Tag[tags.size()]);
  }

  private Collection<Tag> collectResourceTags(Resource resource, boolean recurse) {
    if (resource == null) {
      return Collections.emptyList();
    }
    Set<Tag> treeTags = new HashSet<Tag>();
    Queue<Resource> searchResources = new LinkedList<Resource>();
    searchResources.add(resource);

    while (!searchResources.isEmpty()) {
      Resource searchResource = searchResources.poll();

      if (recurse) {
        CollectionUtils.addAll(searchResources, searchResource.listChildren());
      }

      String[] tags = resource.getValueMap().get(TagConstants.PN_TAGS, String[].class);
      if (tags == null) {
        continue;
      }

      for (String tagStr : tags) {
        Tag tag = resolve(tagStr);
        if (tag != null) {
          treeTags.add(tag);
        }
      }
    }
    return treeTags;
  }

  @Override
  public Tag resolve(String tagID) {
    try {
      String path = getPathFromID(tagID);
      Resource tagResource = resourceResolver.getResource(path);
      if (tagResource != null) {
        return tagResource.adaptTo(Tag.class);
      }
    }
    catch (InvalidTagFormatException e) {
      // ignore
    }
    return null;
  }

  @Override
  public void setTags(Resource resource, Tag[] tags) {
    setTags(resource, tags, true);
  }

  @Override
  public void setTags(Resource resource, Tag[] tags, boolean autoSave) {
    ModifiableValueMap props = resource.adaptTo(ModifiableValueMap.class);
    if (tags == null) {
      props.remove(TagConstants.PN_TAGS);
    } else {
      String[] tagStrings = new String[tags.length];
      for (int i = 0; i < tags.length; ++i) {
        // 6.0 has appeared to have switched to storing (the shorter) tagIDs, from where 5.6 was storing absolute paths.
        tagStrings[i] = tags[i].getTagID();
      }
      props.put(TagConstants.PN_TAGS, tagStrings);
    }

    if (autoSave) {
      try {
        resourceResolver.commit();
      } catch (PersistenceException e) {
        log.error("failed to commit updates for setting tags", e);
      }
    }
  }


  // --- unsupported operations ---

  @Override
  public boolean canCreateTagByTitle(String tagTitlePath) throws InvalidTagFormatException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canCreateTagByTitle(String tagTitlePath, Locale locale) throws InvalidTagFormatException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Tag createTagByTitle(String titlePath, boolean autoSave) throws AccessControlException, InvalidTagFormatException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Tag createTagByTitle(String titlePath, Locale locale) throws AccessControlException, InvalidTagFormatException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RangeIterator<Resource> find(String basePath, List<String[]> tagSetIDs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FindResults findByTitle(String title) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void mergeTag(Tag tag, Tag destination) throws AccessControlException, TagException {
    throw new UnsupportedOperationException("Unsupported operation");
  }

  @Override
  public Tag moveTag(Tag tag, String destination) throws AccessControlException, InvalidTagFormatException, TagException {
    throw new UnsupportedOperationException("Unsupported operation");
  }

  @Override
  public Tag resolveByTitle(String tagTitlePath) {
    throw new UnsupportedOperationException("Unsupported operation");
  }

  @Override
  public Tag resolveByTitle(String tagTitlePath, Locale locale) {
    throw new UnsupportedOperationException("Unsupported operation");
  }

}
