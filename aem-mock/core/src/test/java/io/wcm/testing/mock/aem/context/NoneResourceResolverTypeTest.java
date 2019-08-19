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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.testing.mock.aem.junit.AemContext;

@SuppressWarnings("null")
public class NoneResourceResolverTypeTest {

  @Rule
  public AemContext context = new AemContext(ResourceResolverType.NONE);

  private ResourceProvider<?> resourceProvider = mock(ResourceProvider.class);

  @Test
  public void testResourceResolver() {
    // register dummy resource provider because otherwise ResourceResolverFactory get's not activated
    // with latest sling resource resolver implementation
    context.registerService(ResourceProvider.class, resourceProvider,
        ResourceProvider.PROPERTY_ROOT, "/");

    Resource root = context.resourceResolver().getResource("/");
    assertTrue(root instanceof SyntheticResource);
  }

}
