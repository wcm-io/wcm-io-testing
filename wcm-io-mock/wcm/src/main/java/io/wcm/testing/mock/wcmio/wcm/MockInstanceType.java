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

import org.osgi.annotation.versioning.ProviderType;

import io.wcm.testing.mock.aem.context.AemContextImpl;
import io.wcm.wcm.commons.instancetype.InstanceTypeService;
import io.wcm.wcm.commons.util.RunMode;

/**
 * Allows to set AEM instance type (author or publish) in unit test.
 */
@ProviderType
public final class MockInstanceType {

  private MockInstanceType() {
    // static methods only
  }

  /**
   * Set current instance type to author instance.
   * This also sets the run mode "author".
   * @param context AEM context
   */
  public static void setAuthor(AemContextImpl context) {
    InstanceTypeService service = context.getService(InstanceTypeService.class);
    if (service instanceof MockInstanceTypeService) {
      ((MockInstanceTypeService)service).setAuthor();
    }
    context.runMode(RunMode.AUTHOR);
  }

  /**
   * Set current instance type to publish instance.
   * This also sets the run mode "publish".
   * @param context AEM context
   */
  public static void setPublish(AemContextImpl context) {
    InstanceTypeService service = context.getService(InstanceTypeService.class);
    if (service instanceof MockInstanceTypeService) {
      ((MockInstanceTypeService)service).setPublish();
    }
    context.runMode(RunMode.PUBLISH);
  }

}
