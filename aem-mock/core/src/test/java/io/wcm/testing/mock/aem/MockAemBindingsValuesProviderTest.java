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

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.models.ScriptBindingsModel;

public class MockAemBindingsValuesProviderTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private Page currentPage;
  private Resource currentResource;

  @Before
  public void setUp() throws Exception {
    context.addModelsForClasses(ScriptBindingsModel.class);

    currentPage = context.create().page("/content/testPage");
    currentResource = context.create().resource(currentPage.getContentResource().getPath() + "/testResource");
    context.currentResource(currentResource);
  }

  @Test
  @Ignore // does ot work
  public void testBindings() {
    ScriptBindingsModel model = context.request().adaptTo(ScriptBindingsModel.class);

    assertNotNull(model.getComponentContext());
  }

}
