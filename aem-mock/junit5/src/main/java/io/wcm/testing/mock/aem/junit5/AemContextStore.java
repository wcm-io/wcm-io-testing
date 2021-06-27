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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

/**
 * Helper class managing storage of {@link AemContext} in extension context store.
 */
final class AemContextStore {

  private static final Namespace AEM_CONTEXT_NAMESPACE = Namespace.create(AemContextExtension.class);
  private static final Class<ResourceResolverMockAemContext> DEFAULT_AEM_CONTEXT_TYPE = ResourceResolverMockAemContext.class;
  private static final String BEFORE_ALL_SUFFIX = "_BeforeAll";

  private AemContextStore() {
    // static methods only
  }

  /**
   * Get {@link AemContext} from extension context store.
   * @param extensionContext Extension context
   * @return AemContext or null
   */
  @SuppressWarnings("null")
  public static @Nullable AemContext getAemContext(@NotNull ExtensionContext extensionContext) {
    Class<?> testClass = extensionContext.getTestClass().orElse(null);
    if (testClass == null) {
      return null;
    }
    // try to get existing context from current extension context, or any parent extension context (in case of nested tests)
    return Optional.ofNullable(AemContextStore.getStore(extensionContext).get(testClass, AemContext.class))
        .orElseGet(() -> extensionContext.getParent().map(AemContextStore::getAemContext).orElse(null));
  }

  /**
   * Get {@link AemContext} from extension context store - if it does not exist create a new one and store it.
   * @param extensionContext Extension context
   * @return AemContext (never null)
   */
  public static @NotNull AemContext getOrCreateAemContext(@NotNull ExtensionContext extensionContext, Optional<Class<?>> aemContextType) {
    AemContext context = getAemContext(extensionContext);
    if (context == null) {
      context = createAemContext(aemContextType);
      storeAemContext(extensionContext, context);
    }
    return context;
  }

  /**
   * Removes {@link AemContext} from extension context store (if it exists).
   * @param extensionContext Extension context
   */
  public static void removeAemContext(@NotNull ExtensionContext extensionContext) {
    Class<?> testClass = extensionContext.getRequiredTestClass();
    getStore(extensionContext).remove(testClass);
  }

  /**
   * Store {@link AemContext} in extension context store.
   * @param extensionContext Extension context
   * @param aemContext AEM context
   */
  public static void storeAemContext(@NotNull ExtensionContext extensionContext, @NotNull AemContext aemContext) {
    Class<?> testClass = extensionContext.getRequiredTestClass();
    getStore(extensionContext).put(testClass, aemContext);
  }

  private static Store getStore(ExtensionContext context) {
    return context.getStore(AEM_CONTEXT_NAMESPACE);
  }

  private static AemContext createAemContext(Optional<Class<?>> aemContextType) {
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

  /**
   * Get "before-all" state of test class cached in extension store.
   * @param extensionContext Extension context
   * @return State or null
   */
  public static @Nullable Boolean getBeforeAllState(@NotNull ExtensionContext extensionContext) {
    Class<?> testClass = extensionContext.getRequiredTestClass();
    return getStore(extensionContext).get(testClass.getName() + BEFORE_ALL_SUFFIX, Boolean.class);
  }

  /**
   * Stores "before-all" state of test class in extension store.
   * @param extensionContext Extension context
   * @param state State
   */
  public static void storeBeforeAllState(@NotNull ExtensionContext extensionContext, @NotNull Boolean state) {
    Class<?> testClass = extensionContext.getRequiredTestClass();
    getStore(extensionContext).put(testClass.getName() + BEFORE_ALL_SUFFIX, state);
  }

}
