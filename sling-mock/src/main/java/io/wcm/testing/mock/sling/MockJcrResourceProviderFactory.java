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
package io.wcm.testing.mock.sling;

import java.lang.reflect.Method;

import javax.jcr.RepositoryException;

import org.apache.sling.jcr.resource.internal.helper.jcr.JcrResourceProviderFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

/**
 * Overrides some behavior of {@link JcrResourceProviderFactory} to allow usage in mocking.
 */
class MockJcrResourceProviderFactory extends JcrResourceProviderFactory {

  // make public
  @Override
  public void activate(final ComponentContext context) throws RepositoryException {
    super.activate(context);
  }

  // make public
  public void bindRepository(final ServiceReference ref) {
    try {
      Method superMethod = JcrResourceProviderFactory.class.getDeclaredMethod("bindRepository", ServiceReference.class);
      superMethod.setAccessible(true);
      superMethod.invoke(this, ref);
    }
    catch (Throwable ex) {
      throw new RuntimeException("Calling private method super.bindRepository failed.", ex);
    }
  }

}
