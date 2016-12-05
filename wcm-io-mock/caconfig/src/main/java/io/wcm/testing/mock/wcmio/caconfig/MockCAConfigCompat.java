/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2016 wcm.io
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
package io.wcm.testing.mock.wcmio.caconfig;

import java.util.Set;

import org.apache.jackrabbit.util.Text;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.config.api.Parameter;
import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.config.spi.ParameterProvider;
import io.wcm.config.spi.helpers.AbstractAbsoluteParentConfigurationFinderStrategy;
import io.wcm.config.spi.helpers.AbstractParameterProvider;

/**
 * Helps setting up a mock environment for wcm.io Configuration (compatibility mode).
 */
@ProviderType
public final class MockCAConfigCompat {

  private MockCAConfigCompat() {
    // static methods only
  }

  /**
   * {@link ConfigurationFinderStrategy} that supports one or multiple fixed levels in content hierarchy where
   * configurations are supported.
   * @param applicationId Application ID
   * @param levels List of absolute levels where configuration is supported.
   *          Levels are used in the same way as {@link Text#getAbsoluteParent(String, int)}.
   *          Example:<br>
   *          <code>Text.getAbsoluteParent("/foo/bar/test", 1) == "/foo/bar"</code>
   * @return Configuration finder strategy
   */
  public static ConfigurationFinderStrategy configurationFinderStrategyAbsoluteParent(
      final String applicationId, final int... levels) {
    return new AbstractAbsoluteParentConfigurationFinderStrategy(applicationId, levels) {
      // nothing to override
    };
  }

  /**
   * {@link ParameterProvider} providing list of parameters from given parameter set.
   * @param parameters Parameter set
   * @return Parameter provider
   */
  public static ParameterProvider parameterProvider(final Set<Parameter<?>> parameters) {
    return new AbstractParameterProvider(parameters) {
      // nothing to override
    };
  }

  /**
   * {@link ParameterProvider} providing list of parameters from given from reading all public static fields from a
   * given class definition.
   * @param type Class definition
   * @return Parameter provider
   */
  public static ParameterProvider parameterProvider(final Class<?> type) {
    return new AbstractParameterProvider(type) {
      // nothing to override
    };
  }

}
