/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.day.cq.dam.api.DamConstants;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockContentFragmentTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  @Test
  public void testContentFragmentStructure() throws Exception {
    String assetPath = context.uniqueRoot().dam() + "/cfStructure";
    ContentFragment cf = context.create().contentFragmentStructured(assetPath,
        "param1", "value1", "param2", 123, "param3", true, "param4", new String[] { "v1", "v2" });
    assertNotNull(cf);

    cf.setTitle("myTitle");
    cf.setDescription("myDesc");
    cf.setMetaData("meta1", "value1");

    assertEquals("cfStructure", cf.getName());
    assertEquals("myTitle", cf.getTitle());
    assertEquals("myDesc", cf.getDescription());
    assertEquals("value1", cf.getMetaData().get("meta1"));

    assertTrue(cf.getElements().hasNext());
    assertTrue(cf.hasElement("param1"));

    assertEquals("value1", cf.getElement("param1").getContent());
    assertEquals("123", cf.getElement("param2").getContent());
    assertEquals("true", cf.getElement("param3").getContent());
    assertEquals("v1\nv2", cf.getElement("param4").getContent());
  }

  @Test
  public void testContentFragmentText() throws Exception {
    String assetPath = context.uniqueRoot().dam() + "/cfText";
    ContentFragment cf = context.create().contentFragmentText(assetPath,
        "<p>Text</p>", "text/html");
    assertNotNull(cf);

    cf.setTitle("myTitle");
    cf.setDescription("myDesc");
    cf.setMetaData("meta1", "value1");

    assertEquals("cfText", cf.getName());
    assertEquals("myTitle", cf.getTitle());
    assertEquals("myDesc", cf.getDescription());
    assertEquals("value1", cf.getMetaData().get("meta1"));
    assertEquals("text/html", cf.getMetaData().get(DamConstants.DC_FORMAT));

    assertTrue(cf.getElements().hasNext());
    assertTrue(cf.hasElement("main"));

    ContentElement contentElement = cf.getElement("main");
    assertEquals("<p>Text</p>", contentElement.getContent());
    assertEquals("text/html", contentElement.getContentType());
  }

}
