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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.sling.models.factory.ModelFactory;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.modelsautoreg.ClasspathRegisteredModel;
import io.wcm.testing.mock.aem.modelsautoreg.ModelWithModelFactory;

@SuppressWarnings("null")
public class SlingModelsRegistrationTest {

  @Rule
  public AemContext context = new AemContext();

  @Test
  public void testSlingModelClasspathRegistered() {
    context.request().setAttribute("prop1", "myValue");
    ClasspathRegisteredModel model = context.request().adaptTo(ClasspathRegisteredModel.class);
    assertNotNull(model);
    assertEquals("myValue", model.getProp1());
  }

  @Test
  public void testCreationViaModelFactory() {
    context.request().setAttribute("prop1", "myValue");
    ClasspathRegisteredModel model = context.getService(ModelFactory.class).createModel(context.request(), ClasspathRegisteredModel.class);
    assertNotNull(model);
    assertEquals("myValue", model.getProp1());
  }

  @Test
  public void testModelWithModelFactoryReference() {
    ModelWithModelFactory model = context.request().adaptTo(ModelWithModelFactory.class);
    assertNotNull(model);
    assertNotNull(model.getModelFactory());
  }

  @Test
  public void testOsgiServiceWithModelFactoryReference() {
    OsgiServiceWithModelFactory service = context.registerInjectActivateService(new OsgiServiceWithModelFactory());
    assertNotNull(service.getModelFactory());
  }

}
