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
package io.wcm.testing.junit.rules.parameterized;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Callback interface for setup/teardown actions.
 * @param <T> Parameter type
 */
@ConsumerType
public interface Callback<T> {

  /**
   * Execute action
   * @param currentValue Current parameter value
   */
  void execute(T currentValue);

}
