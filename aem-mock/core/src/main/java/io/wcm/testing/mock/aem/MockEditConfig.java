/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;

import com.day.cq.commons.JSONItem;
import com.day.cq.wcm.api.components.DialogMode;
import com.day.cq.wcm.api.components.DropTarget;
import com.day.cq.wcm.api.components.EditConfig;
import com.day.cq.wcm.api.components.EditLayout;
import com.day.cq.wcm.api.components.InplaceEditingConfig;
import com.day.cq.wcm.api.components.Toolbar;

/**
 * Mock implementatoin of {@link EditConfig}.
 */
final class MockEditConfig implements EditConfig {

  private EditLayout layout = EditLayout.AUTO;
  private DialogMode dialogMode = DialogMode.AUTO;
  private InplaceEditingConfig inplaceEditingConfig;
  private String insertBehavior;
  private boolean empty;
  private String emptyText;
  private Boolean orderable;
  private Boolean deepCancel;
  private JSONItem liveRelationship;
  private Map<String, DropTarget> dropTargets = new HashMap<>();

  @Override
  public EditLayout getLayout() {
    return this.layout;
  }

  @Override
  public void setLayout(EditLayout layout) {
    this.layout = layout;
  }

  @Override
  public DialogMode getDialogMode() {
    return this.dialogMode;
  }

  @Override
  public void setDialogMode(DialogMode mode) {
    this.dialogMode = mode;
  }

  @Override
  public InplaceEditingConfig getInplaceEditingConfig() {
    return this.inplaceEditingConfig;
  }

  @Override
  public void setInplaceEditingConfig(InplaceEditingConfig inplaceEditingConfig) {
    this.inplaceEditingConfig = inplaceEditingConfig;
  }

  @Override
  public String getInsertBehavior() {
    return this.insertBehavior;
  }

  @Override
  public void setInsertBehavior(String behavior) {
    this.insertBehavior = behavior;
  }

  @Override
  public boolean isEmpty() {
    return this.empty;
  }

  @Override
  public void setEmpty(boolean empty) {
    this.empty = empty;
  }

  @Override
  public String getEmptyText() {
    return this.emptyText;
  }

  @Override
  public void setEmptyText(String text) {
    this.emptyText = text;
  }

  @Override
  public Boolean isOrderable() {
    return this.orderable;
  }

  @Override
  public void setOrderable(Boolean orderable) {
    this.orderable = orderable;
  }

  @Override
  public Boolean isDeepCancel() {
    return this.deepCancel;
  }

  @Override
  public void setDeepCancel(Boolean deepCancel) {
    this.deepCancel = deepCancel;
  }

  @Override
  public JSONItem getLiveRelationship() {
    return this.liveRelationship;
  }

  @Override
  public void setLiveRelationship(JSONItem liveRelationship) {
    this.liveRelationship = liveRelationship;
  }

  @Override
  public Map<String, DropTarget> getDropTargets() {
    return this.dropTargets;
  }


  // --- unsupported operations ---

  @Override
  public boolean isTargetingDisabled() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void write(JSONWriter out) throws JSONException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Toolbar getToolbar() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, String> getFormParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, Object> getFormParameterMap() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, String> getListeners() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isDefault() {
    throw new UnsupportedOperationException();
  }

}
