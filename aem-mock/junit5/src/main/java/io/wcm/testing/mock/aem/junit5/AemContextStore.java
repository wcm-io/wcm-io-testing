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
package io.wcm.testing.mock.aem.junit5;

import java.lang.reflect.Constructor;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

/**
 * Helper class managing storage of {@link AemContext} in extension context store.
 */
final class AemContextStore {

  private static final Namespace AEM_CONTEXT_NAMESPACE = Namespace.create(AemContextExtension.class);
  private static final Class<ResourceResolverMockAemContext> DEFAULT_AEM_CONTEXT_TYPE = ResourceResolverMockAemContext.class;

  private AemContextStore() {
    // static methods only
  }

  /**
   * Get {@link AemContext} from extension context store.
   * @param extensionContext Extension context
   * @param testInstance Test instance
   * @return AemContext or null
   */
  @SuppressWarnings("null")
  public static AemContext getAemContext(ExtensionContext extensionContext, Object testInstance) {
    return getStore(extensionContext).get(testInstance, AemContext.class);
  }

  /**
   * Get {@link AemContext} from extension context store - if it does not exist create a new one and store it.
   * @param extensionContext Extension context
   * @param testInstance Test instance
   * @return AemContext (never null)
   */
  public static AemContext getOrCreateAemContext(ExtensionContext extensionContext, Object testInstance,
      Optional<Class<?>> aemContextType) {
    AemContext context = getAemContext(extensionContext, testInstance);
    if (context == null) {
      context = createAemContext(extensionContext, aemContextType);
      storeAemContext(extensionContext, testInstance, context);
    }
    return context;
  }

  /**
   * Removes {@link AemContext} from extension context store (if it exists).
   * @param extensionContext Extension context
   * @param testInstance Test instance
   */
  public static void removeAemContext(ExtensionContext extensionContext, Object testInstance) {
    getStore(extensionContext).remove(testInstance);
  }

  /**
   * Store {@link AemContext} in extension context store.
   * @param extensionContext Extension context
   * @param testInstance Test instance
   * @param aemContext AEM context
   */
  public static void storeAemContext(ExtensionContext extensionContext, Object testInstance, AemContext aemContext) {
    getStore(extensionContext).put(testInstance, aemContext);
  }

  private static Store getStore(ExtensionContext context) {
    return context.getStore(AEM_CONTEXT_NAMESPACE);
  }

  private static AemContext createAemContext(ExtensionContext extensionContext,
      Optional<Class<?>> aemContextType) {
    Class<?> type = aemContextType.orElse(DEFAULT_AEM_CONTEXT_TYPE);
    if (type == AemContext.class) {
      type = DEFAULT_AEM_CONTEXT_TYPE;
    }
    try {
      Constructor constructor = ((Class<?>)type).getConstructor();
      AemContext aemContext = (AemContext)constructor.newInstance();
      aemContext.setUpContext();
      return aemContext;
    }
    // CHECKSTYLE:OFF
    catch (Exception ex) {
      // CHECKSTYLE:ON
      throw new IllegalStateException("Could not create " + type.getName() + " instance.", ex);
    }
  }

}
