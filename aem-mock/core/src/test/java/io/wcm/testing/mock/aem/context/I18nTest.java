/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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

import java.util.ResourceBundle;

import org.apache.sling.testing.mock.sling.MockResourceBundle;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.i18n.I18n;

import io.wcm.testing.mock.aem.junit.AemContext;

/**
 * Test AEM I18n object with resource bundle support from sling-mock.
 */
public class I18nTest {

  @Rule
  public AemContext context = new AemContext();

  @Test
  public void testI18n() {
    ResourceBundle resourceBundle = context.request().getResourceBundle(null);
    ((MockResourceBundle)resourceBundle).put("key1", "value1");

    I18n i18n = new I18n(resourceBundle);

    assertEquals("value1", i18n.get("key1"));
    assertEquals("key2", i18n.get("key2"));
  }

}
