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
package io.wcm.testing.mock.aem.context;

import java.util.Map;

import javax.script.Bindings;

import org.apache.sling.scripting.api.BindingsValuesProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import io.wcm.testing.mock.aem.context.MockAemSlingBindings.SlingBindingsProperty;

/**
 * This {@link BindingsValuesProvider} is required additionally to the dynamic resolving of SlingBinding
 * properties in {@link MockAemSlingBindings} to also support models created by ModelFactory.getModelFromWrappedRequest.
 */
@Component(service = BindingsValuesProvider.class)
class MockAemBindingsValuesProvider implements BindingsValuesProvider {

  static final String PROPERTY_CONTEXT = "context";

  private AemContextImpl context;

  @Activate
  private void activate(Map<String, Object> config) {
    context = (AemContextImpl)config.get(PROPERTY_CONTEXT);
  }

  @Override
  public void addBindings(Bindings bindings) {
    for (SlingBindingsProperty property : SlingBindingsProperty.values()) {
      putProperty(bindings, property.key());
    }
  }

  private void putProperty(Bindings bindings, String key) {
    Object value = MockAemSlingBindings.resolveSlingBindingProperty(context, key);
    if (value != null) {
      bindings.put(key, value);
    }
  }

}
