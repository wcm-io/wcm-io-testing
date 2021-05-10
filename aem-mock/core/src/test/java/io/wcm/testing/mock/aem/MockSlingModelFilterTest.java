/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.adobe.cq.export.json.SlingModelFilter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockSlingModelFilterTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private SlingModelFilter underTest;

  @Before
  public void setUp() {
    underTest = context.getService(SlingModelFilter.class);
  }

  @Test
  @SuppressWarnings("null")
  public void testFilterChildResources() {
    List<Resource> input = ImmutableList.of(mock(Resource.class), mock(Resource.class));
    List<Resource> output = ImmutableList.copyOf(underTest.filterChildResources(input));
    assertEquals(input, output);
  }

  @Test
  public void testFilterProperties() {
    Map<String, Object> input = ImmutableMap.of("prop1", "value1");
    Map<String, Object> output = underTest.filterProperties(input);
    assertEquals(input, output);
  }

}
