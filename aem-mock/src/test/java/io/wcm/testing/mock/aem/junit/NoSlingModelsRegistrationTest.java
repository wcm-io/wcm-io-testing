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
package io.wcm.testing.mock.aem.junit;

import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;

import io.wcm.testing.mock.aem.modelsautoreg.ClasspathRegisteredModel;

public class NoSlingModelsRegistrationTest {

    @Rule
  public AemContext context = new AemContextBuilder()
      .registerSlingModelsFromClassPath(false)
      .build();

    @Test
    public void testSlnigModelClasspathRegistered() {
        context.request().setAttribute("prop1", "myValue");
        ClasspathRegisteredModel model = context.request().adaptTo(ClasspathRegisteredModel.class);
        // expect null because ClasspathRegisteredModel should not be registered automatically from classpath
        assertNull(model);
    }

}
