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
package io.wcm.testing.mock.aem;

import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;

/**
 * Mock implementation of {@link Designer}.
 * This returns no designs at all (not even a default design).
 */
class MockDesigner implements Designer {

  @Override
  public String getDesignPath(Page page) {
    return null;
  }

  @Override
  public Design getDesign(Page page) {
    return null;
  }

  @Override
  public boolean hasDesign(String id) {
    return false;
  }

  @Override
  public Design getDesign(String id) {
    return null;
  }

  @Override
  public Style getStyle(Resource res) {
    return null;
  }

  @Override
  public Style getStyle(Resource res, String cellPath) {
    return null;
  }

}
