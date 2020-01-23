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
package io.wcm.testing.mock.aem.junit5;

import static org.apache.sling.hamcrest.ResourceMatchers.props;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.apache.sling.api.resource.PersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test with generic context parameter in {@code BeforeAll}-method.
 */
@ExtendWith(AemContextExtension.class)
class BeforeAllGenericContextParamCommitRefreshTest {

  private static final String RESOURCE1_PATH = "/content/test";
  private static final String RESOURCE2_PATH = "/content/test2";

  @BeforeAll
  static void setUpAll(AemContext context) throws PersistenceException {
    context.create().resource(RESOURCE1_PATH, "prop1", "value1");
    // ensure resource1 is persisted in repository for all unit tests
    context.resourceResolver().commit();
  }

  @AfterEach
  void tearDown(AemContext context) {
    // reset uncommitted changes from individual unit tests
    context.resourceResolver().revert();
  }

  @Test
  void test1(AemContext context) {
    assertThat(context.resourceResolver().getResource(RESOURCE1_PATH), props("prop1", "value1"));

    // make sure not leftover of resource2 from other unit tests is present
    assertNull(context.resourceResolver().getResource(RESOURCE2_PATH));

    // create resource2 only for this unit test
    context.create().resource(RESOURCE2_PATH, "prop1", "value2");
    assertThat(context.resourceResolver().getResource(RESOURCE2_PATH), props("prop1", "value2"));
  }

  @Test
  void test2(AemContext context) {
    assertThat(context.resourceResolver().getResource(RESOURCE1_PATH), props("prop1", "value1"));

    // make sure not leftover of resource2 from other unit tests is present
    assertNull(context.resourceResolver().getResource(RESOURCE2_PATH));

    // create resource2 only for this unit test
    context.create().resource(RESOURCE2_PATH, "prop1", "value2");
    assertThat(context.resourceResolver().getResource(RESOURCE2_PATH), props("prop1", "value2"));
  }

}
