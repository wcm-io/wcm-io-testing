/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.testing.mock.wcmio.wcm;

import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import io.wcm.wcm.commons.instancetype.InstanceTypeService;
import io.wcm.wcm.commons.util.RunMode;

class MockInstanceTypeService implements InstanceTypeService {

  private boolean isAuthor;

  MockInstanceTypeService() {
    setPublish();
  }

  @Override
  public boolean isAuthor() {
    return isAuthor;
  }

  @Override
  public boolean isPublish() {
    return !isAuthor;
  }

  @Override
  public @NotNull Set<String> getRunModes() {
    return Collections.singleton(isAuthor ? RunMode.AUTHOR : RunMode.PUBLISH);
  }

  public void setAuthor() {
    isAuthor = true;
  }

  public void setPublish() {
    isAuthor = false;
  }

}
