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
package io.wcm.testing.mock.sling.contentimport;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.jcr.JsonItemWriter;

/**
 * Mounts a JSON file into a sling resource hierarchy.
 */
public class JsonImporter {

  private static final String REFERENCE = "jcr:reference:";
  private static final String PATH = "jcr:path:";

  private static final Set<String> IGNORED_NAMES = new HashSet<>();
  static {
    IGNORED_NAMES.add(JcrConstants.JCR_PRIMARYTYPE);
    IGNORED_NAMES.add(JcrConstants.JCR_MIXINTYPES);
    IGNORED_NAMES.add(JcrConstants.JCR_UUID);
    IGNORED_NAMES.add(JcrConstants.JCR_BASEVERSION);
    IGNORED_NAMES.add(JcrConstants.JCR_PREDECESSORS);
    IGNORED_NAMES.add(JcrConstants.JCR_SUCCESSORS);
    IGNORED_NAMES.add(JcrConstants.JCR_CREATED);
    IGNORED_NAMES.add("jcr:checkedOut");
  }

  private final ResourceResolver resourceResolver;
  private final DateFormat calendarFormat;

  /**
   * @param resourceResolver Resource resolver
   */
  public JsonImporter(final ResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
    this.calendarFormat = new SimpleDateFormat(JsonItemWriter.ECMA_DATE_FORMAT, JsonItemWriter.DATE_FORMAT_LOCALE);
  }

  /**
   * Mount content of JSON file.
   * @param classpathResource Classpath resource URL for JSON content
   * @param parentResource Parent resource
   * @param childName Name of child resource to create with JSON content
   * @return Resource
   * @throws IOException
   * @throws PersistenceException
   */
  public Resource importTo(final String classpathResource, final Resource parentResource, final String childName)
      throws IOException, PersistenceException {
    InputStream is = JsonImporter.class.getResourceAsStream(classpathResource);
    if (is == null) {
      throw new IllegalArgumentException("Classpath resource not found: " + classpathResource);
    }
    try {
      return importTo(is, parentResource, childName);
    }
    finally {
      is.close();
    }

  }

  /**
   * Mount content of JSON file.
   * Auto-create parent hierarchies as nt:unstrucured nodes if missing.
   * @param classpathResource Classpath resource URL for JSON content
   * @param destPath Path to import the JSON content to
   * @return Resource
   * @throws IOException
   * @throws PersistenceException
   */
  public Resource importTo(final String classpathResource, final String destPath)
      throws IOException, PersistenceException {
    return importTo(classpathResource, destPath, true);
  }

  /**
   * Mount content of JSON file.
   * @param classpathResource Classpath resource URL for JSON content
   * @param destPath Path to import the JSON content to
   * @param autoCreateParent Auto-create parent hierarchies as nt:unstrucured nodes if missing.
   * @return Resource
   * @throws IOException
   * @throws PersistenceException
   */
  public Resource importTo(final String classpathResource, final String destPath, final boolean autoCreateParent)
      throws IOException, PersistenceException {
    InputStream is = JsonImporter.class.getResourceAsStream(classpathResource);
    if (is == null) {
      throw new IllegalArgumentException("Classpath resource not found: " + classpathResource);
    }
    try {
      return importTo(is, destPath, autoCreateParent);
    }
    finally {
      is.close();
    }
  }

  /**
   * Mount content of JSON file.
   * @param inputStream Input stream with JSON content
   * @param parentResource Parent resource
   * @param childName Name of child resource to create with JSON content
   * @return Resource
   * @throws IOException
   * @throws PersistenceException
   */
  public Resource importTo(final InputStream inputStream, final Resource parentResource, final String childName)
      throws IOException, PersistenceException {
    return importTo(inputStream, parentResource.getPath() + "/" + childName);
  }

  /**
   * Mount content of JSON file.
   * Auto-create parent hierarchies as nt:unstrucured nodes if missing.
   * @param inputStream Input stream with JSON content
   * @param destPath Path to import the JSON content to
   * @return Resource
   * @throws IOException
   * @throws PersistenceException
   */
  public Resource importTo(final InputStream inputStream, final String destPath)
      throws IOException, PersistenceException {
    return importTo(inputStream, destPath, true);
  }

  /**
   * Mount content of JSON file
   * @param inputStream Input stream with JSON content
   * @param destPath Path to import the JSON content to
   * @param autoCreateParent Auto-create parent hierarchies as nt:unstrucured nodes if missing.
   * @return Resource
   * @throws IOException
   * @throws PersistenceException
   */
  public Resource importTo(final InputStream inputStream, final String destPath, final boolean autoCreateParent)
      throws IOException, PersistenceException {
    try {
      String parentPath = ResourceUtil.getParent(destPath);
      String childName = ResourceUtil.getName(destPath);

      Resource parentResource = this.resourceResolver.getResource(parentPath);
      if (parentResource == null) {
        if (autoCreateParent) {
          parentResource = createResourceHierarchy(parentPath);
        }
        else {
          throw new IllegalArgumentException("Parent resource does not exist: " + parentPath);
        }
      }
      if (parentResource.getChild(childName) != null) {
        throw new IllegalArgumentException("Resource does already exist: " + destPath);
      }

      String jsonString = convertToJsonString(inputStream).trim();
      JSONObject json = new JSONObject(jsonString);
      return this.createResource(parentResource, childName, json);
    }
    catch (JSONException je) {
      throw (IOException)new IOException(je.getMessage()).initCause(je);
    }
  }

  private Resource createResourceHierarchy(final String path) throws PersistenceException {
    String parentPath = ResourceUtil.getParent(path);
    if (parentPath == null) {
      return null;
    }
    Resource parentResource = this.resourceResolver.getResource(parentPath);
    if (parentResource == null) {
      parentResource = createResourceHierarchy(parentPath);
    }
    Map<String, Object> props = new HashMap<>();
    props.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
    return this.resourceResolver.create(parentResource, ResourceUtil.getName(path), props);
  }

  private Resource createResource(final Resource parentResource, final String childName, final JSONObject jsonObject)
      throws JSONException, PersistenceException {

    // collect all properties first
    Map<String, Object> props = new HashMap<>();
    JSONArray names = jsonObject.names();
    for (int i = 0; names != null && i < names.length(); i++) {
      final String name = names.getString(i);
      if (!IGNORED_NAMES.contains(name)) {
        Object obj = jsonObject.get(name);
        if (!(obj instanceof JSONObject)) {
          this.setProperty(props, name, obj);
        }
      }
    }

    // validate JCR primary type
    Object primaryTypeObj = jsonObject.opt(JcrConstants.JCR_PRIMARYTYPE);
    String primaryType = null;
    if (primaryTypeObj != null) {
      primaryType = String.valueOf(primaryTypeObj);
    }
    if (primaryType == null) {
      primaryType = JcrConstants.NT_UNSTRUCTURED;
    }
    props.put(JcrConstants.JCR_PRIMARYTYPE, primaryType);

    // create resource
    Resource resource = this.resourceResolver.create(parentResource, childName, props);

    // add child resources
    for (int i = 0; names != null && i < names.length(); i++) {
      final String name = names.getString(i);
      if (!IGNORED_NAMES.contains(name)) {
        Object obj = jsonObject.get(name);
        if (obj instanceof JSONObject) {
          createResource(resource, name, (JSONObject)obj);
        }
      }
    }

    return resource;
  }

  private void setProperty(final Map<String, Object> props, final String name, final Object value) throws JSONException {
    if (value instanceof JSONArray) {
      // multivalue
      final JSONArray array = (JSONArray)value;
      if (array.length() > 0) {
        final Object[] values = new Object[array.length()];
        for (int i = 0; i < array.length(); i++) {
          values[i] = array.get(i);
        }

        if (values[0] instanceof Double || values[0] instanceof Float) {
          Double[] arrayValues = new Double[values.length];
          for (int i = 0; i < values.length; i++) {
            arrayValues[i] = (Double)values[i];
          }
          props.put(getName(name), arrayValues);
        }
        else if (values[0] instanceof Number) {
          Long[] arrayValues = new Long[values.length];
          for (int i = 0; i < values.length; i++) {
            arrayValues[i] = ((Number)values[i]).longValue();
          }
          props.put(getName(name), arrayValues);
        }
        else if (values[0] instanceof Boolean) {
          Boolean[] arrayValues = new Boolean[values.length];
          for (int i = 0; i < values.length; i++) {
            arrayValues[i] = (Boolean)values[i];
          }
          props.put(getName(name), arrayValues);
        }
        else {
          String[] arrayValues = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            arrayValues[i] = values[i].toString();
          }
          props.put(getName(name), arrayValues);
        }
      }
      else {
        props.put(getName(name), new String[0]);
      }

    }
    else {
      // single value
      if (value instanceof Double || value instanceof Float) {
        props.put(getName(name), value);
      }
      else if (value instanceof Number) {
        props.put(getName(name), ((Number)value).longValue());
      }
      else if (value instanceof Boolean) {
        props.put(getName(name), value);
      }
      else {
        String stringValue = value.toString();

        // check if value is a Calendar object
        Calendar calendar = tryParseCalendarValue(stringValue);
        if (calendar != null) {
          props.put(getName(name), calendar);
        }
        else {
          props.put(getName(name), stringValue);
        }

      }
    }
  }

  private String getName(final String name) {
    if (name.startsWith(REFERENCE)) {
      return name.substring(REFERENCE.length());
    }
    if (name.startsWith(PATH)) {
      return name.substring(PATH.length());
    }
    return name;
  }

  private String convertToJsonString(final InputStream inputStream) throws IOException {
    try {
      return IOUtils.toString(inputStream);
    }
    finally {
      inputStream.close();
    }
  }

  private Calendar tryParseCalendarValue(final String value) {
    if (StringUtils.isNotBlank(value)) {
      synchronized (this.calendarFormat) {
        try {
          Date date = this.calendarFormat.parse(value);
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          return calendar;
        }
        catch (ParseException ex) {
          // ignore
        }
      }
    }
    return null;
  }

}
