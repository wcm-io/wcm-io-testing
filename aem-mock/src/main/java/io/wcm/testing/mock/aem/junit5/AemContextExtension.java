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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * JUnit 5 extension that allows to inject {@link AemContext} (or subclasses of it) parameters in test methods,
 * and ensures that the context is set up and teared down properly for each test method.
 */
public final class AemContextExtension implements ParameterResolver, TestInstancePostProcessor, AfterEachCallback {

  /**
   * Checks if test class has a {@link AemContext} or derived field.
   * If it has and is not instantiated, create an new {@link AemContext} and store it in the field.
   * If it is already instantiated reuse this instance and use it for all test methods.
   */
  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {
    Field aemContextField = getAemContextFieldFromTestInstance(testInstance);
    if (aemContextField != null) {
      AemContext context = (AemContext)aemContextField.get(testInstance);
      if (context != null) {
        AemContextStore.storeAemContext(extensionContext, testInstance, context);
      }
      else {
        context = AemContextStore.getOrCreateAemContext(extensionContext, testInstance,
            Optional.of(aemContextField.getType()));
        aemContextField.set(testInstance, context);
      }
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
    AemContext aemContext = AemContextStore.getOrCreateAemContext(extensionContext, extensionContext.getRequiredTestInstance(),
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

  /**
   * Tear down {@link AemContext} after test is complete.
   */
  @Override
  public void afterEach(ExtensionContext extensionContext) {
    AemContext aemContext = AemContextStore.getAemContext(extensionContext, extensionContext.getRequiredTestInstance());
    if (aemContext != null) {
      aemContext.tearDownContext();
      AemContextStore.removeAemContext(extensionContext, extensionContext.getRequiredTestInstance());
    }
  }

  private Optional<Class<?>> getAemContextType(ParameterContext parameterContext, ExtensionContext extensionContext) {
    // If a @BeforeEach or @AfterEach method has only a generic AemContext parameter check if test method has a more specific parameter and use this
    if (isAbstractAemContext(parameterContext)) {
      return getAemContextTypeFromTestMethod(extensionContext);
    }
    else {
      return Optional.of(parameterContext.getParameter().getType());
    }
  }

  private boolean isAbstractAemContext(ParameterContext parameterContext) {
    return parameterContext.getParameter().getType().equals(AemContext.class);
  }

  private boolean paramIsNotInstanceOfExistingContext(ParameterContext parameterContext, AemContext aemContext) {
    return !parameterContext.getParameter().getType().isInstance(aemContext);
  }

  private Optional<Class<?>> getAemContextTypeFromTestMethod(ExtensionContext extensionContext) {
    return Arrays.stream(extensionContext.getRequiredTestMethod().getParameterTypes())
        .filter(clazz -> AemContext.class.isAssignableFrom(clazz))
        .findFirst();
  }

  private Field getAemContextFieldFromTestInstance(Object testInstance) {
    return Arrays.stream(testInstance.getClass().getDeclaredFields())
        .filter(field -> AemContext.class.isAssignableFrom(field.getType()))
        .findFirst()
        .orElse(null);
  }

}
