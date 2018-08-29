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
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.models.SlingBindingsModel;

@SuppressWarnings("null")
public class SlingBindingsTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private Page currentPage;
  private Resource currentResource;

  @Before
  public void setUp() throws Exception {
    context.addModelsForClasses(SlingBindingsModel.class);

    currentPage = context.create().page("/content/testPage");
    currentResource = context.create().resource(currentPage.getContentResource().getPath() + "/testResource",
        "sling:resourceType", "/apps/app1/components/component1");

    context.create().resource("/apps/app1/components/component1",
        JcrConstants.JCR_PRIMARYTYPE, NameConstants.NT_COMPONENT);
  }

  @Test
  public void testBindings() {
    context.currentResource(currentResource);

    SlingBindingsModel model = context.request().adaptTo(SlingBindingsModel.class);

    assertNotNull(model);

    assertNotNull(model.getResolver());
    assertNotNull(model.getResource());
    assertNotNull(model.getRequest());
    assertNotNull(model.getResponse());
    if (context.resourceResolverType() == ResourceResolverType.RESOURCERESOLVER_MOCK) {
      assertNull(model.getCurrentNode());
      assertNull(model.getcurrentSession());
    }
    else {
      assertNotNull(model.getCurrentNode());
      assertNotNull(model.getcurrentSession());
    }

    assertNotNull(model.getComponentContext());
    assertNull(model.getEditContext());
    assertNotNull(model.getProperties());
    assertNotNull(model.getPageManager());
    assertNotNull(model.getCurrentPage());
    assertNotNull(model.getResourcePage());
    assertNotNull(model.getPageProperties());
    assertNotNull(model.getComponent());
    assertNotNull(model.getDesigner());
    assertNotNull(model.getCurrentDesign());
    assertNotNull(model.getResourceDesign());
    assertNotNull(model.getCurrentStyle());
  }

  @Test
  public void testBindings_EditMode() {
    WCMMode.EDIT.toRequest(context.request());
    context.currentResource(currentResource);

    SlingBindingsModel model = context.request().adaptTo(SlingBindingsModel.class);

    assertNotNull(model);

    assertNotNull(model.getResolver());
    assertNotNull(model.getResource());
    assertNotNull(model.getRequest());
    assertNotNull(model.getResponse());
    if (context.resourceResolverType() == ResourceResolverType.RESOURCERESOLVER_MOCK) {
      assertNull(model.getCurrentNode());
      assertNull(model.getcurrentSession());
    }
    else {
      assertNotNull(model.getCurrentNode());
      assertNotNull(model.getcurrentSession());
    }

    assertNotNull(model.getComponentContext());
    assertNotNull(model.getEditContext());
    assertNotNull(model.getProperties());
    assertNotNull(model.getPageManager());
    assertNotNull(model.getCurrentPage());
    assertNotNull(model.getResourcePage());
    assertNotNull(model.getPageProperties());
    assertNotNull(model.getComponent());
    assertNotNull(model.getDesigner());
    assertNotNull(model.getCurrentDesign());
    assertNotNull(model.getResourceDesign());
    assertNotNull(model.getCurrentStyle());
  }

  /*
   -- Sling Models API 1.3.6/Impl 1.4.4 or higher required for these unit tests --
  @Test
  public void testBindingsModelFactory() throws Exception {
    context.currentResource(currentResource);
  
    ModelFactory modelFactory = context.getService(ModelFactory.class);
    ScriptBindingsModel model = modelFactory.getModelFromWrappedRequest(context.request(), context.currentResource(), ScriptBindingsModel.class);
  
    assertNotNull(model);
  
    assertNotNull(model.getResolver());
    assertNotNull(model.getResource());
    assertNotNull(model.getRequest());
    assertNotNull(model.getResponse());
    if (context.resourceResolverType() == ResourceResolverType.RESOURCERESOLVER_MOCK) {
      assertNull(model.getCurrentNode());
      assertNull(model.getcurrentSession());
    }
    else {
      assertNotNull(model.getCurrentNode());
      assertNotNull(model.getcurrentSession());
    }
  
    assertNotNull(model.getComponentContext());
    assertNull(model.getEditContext());
    assertNotNull(model.getProperties());
    assertNotNull(model.getPageManager());
    assertNotNull(model.getCurrentPage());
    assertNotNull(model.getResourcePage());
    assertNotNull(model.getPageProperties());
    assertNotNull(model.getComponent());
    assertNotNull(model.getDesigner());
    assertNotNull(model.getCurrentDesign());
    assertNotNull(model.getResourceDesign());
    assertNotNull(model.getCurrentStyle());
  }
  */

}
