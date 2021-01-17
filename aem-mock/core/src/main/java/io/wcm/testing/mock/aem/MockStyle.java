/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;

import com.day.cq.wcm.api.designer.Cell;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Style;
import com.drew.lang.annotations.Nullable;

/**
 * Mock implementation of {@link Style}.
 */
class MockStyle implements Style {

  private final ValueMap props;
  private final Design design;

  /**
   * @param props Value map for style properties
   * @param design Design
   */
  MockStyle(@NotNull ValueMap props, @Nullable Design design) {
    this.props = props;
    this.design = design;
  }

  /**
   * @param props Value map for style properties
   */
  MockStyle(@NotNull ValueMap props) {
    this(props, (Design)null);
  }

  @Override
  public Design getDesign() {
    return design;
  }


  // --- delegate methods to ValueMap ---

  @Override
  @SuppressWarnings("null")
  public <T> T get(String name, Class<T> type) {
    return this.props.get(name, type);
  }

  @Override
  @SuppressWarnings("null")
  public <T> T get(String name, T defaultValue) {
    return this.props.get(name, defaultValue);
  }

  @Override
  public int size() {
    return this.props.size();
  }

  @Override
  public boolean isEmpty() {
    return this.props.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return this.props.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return this.props.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    return this.props.get(key);
  }

  @Override
  public Object put(String key, Object value) {
    return this.props.put(key, value);
  }

  @Override
  public Object remove(Object key) {
    return this.props.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> m) {
    this.props.putAll(m);
  }

  @Override
  public void clear() {
    this.props.clear();
  }

  @Override
  public Set<String> keySet() {
    return this.props.keySet();
  }

  @Override
  public Collection<Object> values() {
    return this.props.values();
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return this.props.entrySet();
  }

  @Override
  public boolean equals(Object o) {
    return this.props.equals(o);
  }

  @Override
  public int hashCode() {
    return this.props.hashCode();
  }


  // --- unsupported operations ---

  @Override
  public Cell getCell() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDefiningPath(String arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Resource getDefiningResource(String arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Style getSubStyle(String arg0) {
    throw new UnsupportedOperationException();
  }

}
