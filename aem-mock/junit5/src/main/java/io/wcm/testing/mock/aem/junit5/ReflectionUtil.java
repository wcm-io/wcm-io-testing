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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper methods for reflection access of test classes.
 */
final class ReflectionUtil {

  private ReflectionUtil() {
    // static methods only
  }

  /**
   * Gets declared field of given type (or subtype) of given class or it's superclasses.
   * The field is made accessible as well.
   * @param testInstance Test instance
   * @param type Field type
   * @return Field or null
   */
  static @Nullable Field getField(@NotNull Object testInstance, @NotNull Class<?> type) {
    return getField(testInstance.getClass(), type);
  }

  /**
   * Gets declared field of given type (or subtype) of given class or it's superclasses.
   * The field is made accessible as well.
   * @param testClass Test class
   * @param type Field type
   * @return Field or null
   */
  static @Nullable Field getField(@Nullable Class<?> testClass, @NotNull Class<?> type) {
    if (testClass == null) {
      return null;
    }

    Field field = Arrays.stream(testClass.getDeclaredFields())
        .filter(item -> type.isAssignableFrom(item.getType()))
        .findFirst()
        .orElse(null);

    if (field == null) {
      return getField(testClass.getSuperclass(), type);
    }

    field.setAccessible(true);
    return field;
  }

  /**
   * Gets method annotated with given annotation and a parameter of given type of given class or it's superclasses.
   * @param testClass Test class
   * @param annotationClass Annotation class
   * @return Method or null
   */
  static <T extends Annotation> @Nullable Method getAnnotatedMethod(@Nullable Class<?> testClass,
      @NotNull Class<T> annotationClass,
      @NotNull Class<?> parameterType) {
    if (testClass == null) {
      return null;
    }

    Method method = Arrays.stream(testClass.getDeclaredMethods())
        .filter(item -> item.getAnnotation(annotationClass) != null)
        .filter(item -> hasParameter(item, parameterType))
        .findFirst()
        .orElse(null);

    if (method == null) {
      return getAnnotatedMethod(testClass.getSuperclass(), annotationClass, parameterType);
    }

    return method;
  }

  private static boolean hasParameter(Method method, @NotNull Class<?> parameterType) {
    return Arrays.stream(method.getParameters())
        .filter(item -> parameterType.isAssignableFrom(item.getType()))
        .findFirst()
        .isPresent();
  }

}
