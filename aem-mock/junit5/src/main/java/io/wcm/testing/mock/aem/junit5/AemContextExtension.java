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

import static io.wcm.testing.mock.aem.junit5.ReflectionUtil.getAnnotatedMethod;
import static io.wcm.testing.mock.aem.junit5.ReflectionUtil.getField;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * JUnit 5 extension that allows to inject {@link AemContext} (or subclasses of it) parameters in test methods,
 * and ensures that the context is set up and teared down properly for each test method.
 */
public final class AemContextExtension implements ParameterResolver, TestInstancePostProcessor,
    BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback, AfterTestExecutionCallback {

  /**
   * Checks if test class has a {@link AemContext} or derived field.
   * If it has and is not instantiated, create an new {@link AemContext} and store it in the field.
   * If it is already instantiated reuse this instance and use it for all test methods.
   */
  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {
    if (!isBeforeAllContext(extensionContext)) {
      Field aemContextField = getField(testInstance, AemContext.class);
      if (aemContextField != null) {
        setAemContextInStore(extensionContext, aemContextField, testInstance);
      }
    }
  }

  private void setAemContextInStore(@NotNull ExtensionContext extensionContext,
      @NotNull Field aemContextField, @Nullable Object testInstance) throws IllegalAccessException {
    AemContext aemContext = (AemContext)aemContextField.get(testInstance);
    if (aemContext != null) {
      if (!aemContext.isSetUp()) {
        aemContext.setUpContext();
      }
      AemContextStore.storeAemContext(extensionContext, aemContext);
    }
    else {
      aemContext = AemContextStore.getOrCreateAemContext(extensionContext, Optional.of(aemContextField.getType()));
      aemContextField.set(testInstance, aemContext);
    }
  }

  /**
   * Support parameter injection for test methods of parameter type is derived from {@link AemContext}.
   */
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return AemContext.class.isAssignableFrom(parameterContext.getParameter().getType());
  }

  /**
   * Resolve (or create) {@link AemContext} instance for test method parameter.
   */
  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    AemContext aemContext = AemContextStore.getOrCreateAemContext(extensionContext,
        getAemContextType(parameterContext, extensionContext));
    if (paramIsNotInstanceOfExistingContext(parameterContext, aemContext)) {
      throw new ParameterResolutionException(
          "Found AemContext instance of type: " + aemContext.getClass().getName() + "\n"
              + "Required is: " + parameterContext.getParameter().getType().getName() + "\n"
              + "Verify that all test lifecycle methods (@BeforeEach, @Test, @AfterEach) "
              + "use the same AemContext type.");
    }
    return aemContext;
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    if (isBeforeAllContext(extensionContext)) {
      Field aemContextField = getField(extensionContext.getRequiredTestClass(), AemContext.class);
      if (aemContextField != null) {
        setAemContextInStore(extensionContext, aemContextField, null);
      }
      applyAemContext(extensionContext, aemContext -> {
        // call context plugins setup after @BeforeAll methods were called
        /* please note: in JUnit5 there is no callback to be called after all @BeforeAll methods are called
         * so we call it before @BeforeAll execution to make sure the plugin code is called at all */
        aemContext.getContextPlugins().executeAfterSetUpCallback(aemContext);
      });
    }
  }

  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    if (!isBeforeAllContext(extensionContext)) {
      applyAemContext(extensionContext, aemContext -> {
        // call context plugins setup after @BeforeEach methods were called
        aemContext.getContextPlugins().executeAfterSetUpCallback(aemContext);
      });
    }
  }

  @Override
  public void afterTestExecution(ExtensionContext extensionContext) {
    if (!isBeforeAllContext(extensionContext)) {
      applyAemContext(extensionContext, aemContext -> {
        // call context plugins setup before @AfterEach methods are called
        aemContext.getContextPlugins().executeBeforeTearDownCallback(aemContext);
      });
    }
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    if (!isBeforeAllContext(extensionContext)) {
      applyAemContext(extensionContext, aemContext -> {
        // call context plugins setup after @AfterEach methods were called
        aemContext.getContextPlugins().executeAfterTearDownCallback(aemContext);

        // tear down and remove context
        aemContext.tearDownContext();
        AemContextStore.removeAemContext(extensionContext);
      });
    }
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) throws Exception {
    if (isBeforeAllContext(extensionContext)) {
      applyAemContext(extensionContext, aemContext -> {
        // call context plugins setup before @AfterAll methods are called
        /* please note: in JUnit5 there is no callback to be called before all @AfterAll methods are called
         * so we call it after @AfterAll execution to make sure the plugin code is called at all */
        aemContext.getContextPlugins().executeBeforeTearDownCallback(aemContext);

        // call context plugins setup after @AfterAll methods were called
        aemContext.getContextPlugins().executeAfterTearDownCallback(aemContext);

        // tear down and remove context
        aemContext.tearDownContext();
        AemContextStore.removeAemContext(extensionContext);
      });
    }
  }

  private void applyAemContext(ExtensionContext extensionContext, Consumer<AemContext> consumer) {
    AemContext aemContext = AemContextStore.getAemContext(extensionContext);
    if (aemContext != null) {
      consumer.accept(aemContext);
    }
  }

  private Optional<Class<?>> getAemContextType(ParameterContext parameterContext, ExtensionContext extensionContext) {
    // If a @BeforeEach or @AfterEach method has only a generic AemContext parameter check if
    // test method has a more specific parameter and use this
    if (isTestInstance(extensionContext) && isAbstractAemContext(parameterContext)) {
      return getParameterFromTestMethod(extensionContext, AemContext.class);
    }
    else {
      return Optional.of(parameterContext.getParameter().getType());
    }
  }

  /**
   * On @BeforeAll is no test instance available
   * @return {@code true} if test instance is available
   */
  private boolean isTestInstance(ExtensionContext extensionContext) {
    return extensionContext.getTestInstance().isPresent();
  }

  private boolean isAbstractAemContext(ParameterContext parameterContext) {
    return parameterContext.getParameter().getType().equals(AemContext.class);
  }

  private boolean paramIsNotInstanceOfExistingContext(ParameterContext parameterContext, AemContext aemContext) {
    return !parameterContext.getParameter().getType().isInstance(aemContext);
  }

  private Optional<Class<?>> getParameterFromTestMethod(ExtensionContext extensionContext, Class<?> type) {
    return Arrays.stream(extensionContext.getRequiredTestMethod().getParameterTypes())
        .filter(type::isAssignableFrom)
        .findFirst();
  }

  /**
   * <p>
   * Checks if a "before-all" context is used in this class.
   * </p>
   * <p>
   * In this case the context is initialized/set up once before all tests, and teared down once after all tests.
   * Otherwise setup and teardown of the context happens for each test run.
   * </p>
   * <p>
   * The "before-all" state is assumed if a) a static AemContext field exists or b) a method annotated with
   * '@BeforeAll' exists with AemContext parameter.
   * </p>
   * @param extensionContext Extension context
   * @return true for "before-all" context.
   */
  private boolean isBeforeAllContext(@NotNull ExtensionContext extensionContext) {
    Boolean state = AemContextStore.getBeforeAllState(extensionContext);
    if (state == null) {
      state = false;
      Class<?> testClass = extensionContext.getRequiredTestClass();

      // check for static aem context field
      Field aemContextField = getField(testClass, AemContext.class);
      if (aemContextField != null && Modifier.isStatic(aemContextField.getModifiers())) {
        state = true;
      }
      else {
        // check for static method with BeforeAll annotation
        Method method = getAnnotatedMethod(testClass, BeforeAll.class, AemContext.class);
        if (method != null && Modifier.isStatic(method.getModifiers())) {
          state = true;
        }
      }
      // cache state in extension store
      AemContextStore.storeBeforeAllState(extensionContext, state);
    }
    return state;
  }

}
