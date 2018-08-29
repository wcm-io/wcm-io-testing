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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.models.ScriptBindingsModel;

@SuppressWarnings("null")
public class MockAemBindingsValuesProviderTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private Page currentPage;
  private Resource currentResource;

  @Before
  public void setUp() throws Exception {
    context.addModelsForClasses(ScriptBindingsModel.class);

    currentPage = context.create().page("/content/testPage");
    currentResource = context.create().resource(currentPage.getContentResource().getPath() + "/testResource",
        "sling:resourceType", "/apps/app1/components/component1");

    context.create().resource("/apps/app1/components/component1",
        JcrConstants.JCR_PRIMARYTYPE, NameConstants.NT_COMPONENT);
  }

  @Test
  @Ignore // does not work because ResourceOverridingRequestWrapper - which does not magic copying of bindings - is not involved when just calling "adaptTo"
  public void testBindings() {
    context.currentPage(currentPage);
    context.currentResource(currentResource);

    ScriptBindingsModel model = context.request().adaptTo(ScriptBindingsModel.class);

    assertNotNull(model);
    assertNotNull(model.getComponentContext());
    assertNull(model.getEditContext());
    assertNotNull(model.getProperties());
    assertNotNull(model.getPageManager());
    assertNotNull(model.getCurrentPage());
    assertNotNull(model.getResourcePage());
    assertNotNull(model.getPageProperties());
    assertNotNull(model.getComponent());
  }

  /*
   -- Sling Models API 1.3.6/Impl 1.4.4 or higher required for these unit tests --
  @Test
  public void testBindingsModelFactory() throws Exception {
    context.currentPage(currentPage);
    context.currentResource(currentResource);
  
    ModelFactory modelFactory = context.getService(ModelFactory.class);
    ScriptBindingsModel model = modelFactory.getModelFromWrappedRequest(context.request(), context.currentResource(), ScriptBindingsModel.class);
  
    assertNotNull(model);
    assertNotNull(model.getComponentContext());
    assertNull(model.getEditContext());
    assertNotNull(model.getProperties());
    assertNotNull(model.getPageManager());
    assertNotNull(model.getCurrentPage());
    assertNotNull(model.getResourcePage());
    assertNotNull(model.getPageProperties());
    assertNotNull(model.getComponent());
  }
  
  @Test
  public void testBindingsModelFactory_EditMode() throws Exception {
    WCMMode.EDIT.toRequest(context.request());
    context.currentPage(currentPage);
    context.currentResource(currentResource);
  
    ModelFactory modelFactory = context.getService(ModelFactory.class);
    ScriptBindingsModel model = modelFactory.getModelFromWrappedRequest(context.request(), context.currentResource(), ScriptBindingsModel.class);
  
    assertNotNull(model);
    assertNotNull(model.getComponentContext());
    assertNotNull(model.getEditContext());
    assertNotNull(model.getProperties());
    assertNotNull(model.getPageManager());
    assertNotNull(model.getCurrentPage());
    assertNotNull(model.getResourcePage());
    assertNotNull(model.getPageProperties());
    assertNotNull(model.getComponent());
  }
  */

}
