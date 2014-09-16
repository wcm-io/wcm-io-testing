/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.testing.mock.sling.resourceresolvertype.jackrabbit.contentimport;

import io.wcm.testing.mock.sling.MockSling;
import io.wcm.testing.mock.sling.ResourceResolverType;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.testing.jcr.RepositoryUtil;
import org.junit.Ignore;

//TEST IS DISABLED currently, it does not work with jackrabbit repository yet
@Ignore
public class DamJsonImporterTest extends io.wcm.testing.mock.sling.loader.DamContentLoaderTest {

  @Override
  protected ResourceResolverType getResourceResolverType() {
    return ResourceResolverType.JCR_JACKRABBIT;
  }

  @Override
  protected ResourceResolver newResourceResolver() {
    ResourceResolver resolver = MockSling.newResourceResolver(getResourceResolverType());

    // register sling node types
    try {
      RepositoryUtil.registerSlingNodeTypes(resolver.adaptTo(Session.class));
    }
    catch (IOException | RepositoryException ex) {
      throw new RuntimeException("Unable to register sling node types.", ex);
    }

    return resolver;
  }

}
