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
package io.wcm.testing.mock.aem.junit;

import io.wcm.testing.junit.rules.parameterized.Callback;
import io.wcm.testing.junit.rules.parameterized.ListGenerator;
import io.wcm.testing.mock.sling.MockSling;
import io.wcm.testing.mock.sling.ResourceResolverType;

import java.util.Arrays;

import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit rule for setting up and tearing down AEM context objects for unit tests.
 */
public final class AemContext extends AemContextImpl<AemContext> implements TestRule {

  private final AemContextCallback setUpCallback;
  private final AemContextCallback tearDownCallback;
  private final ResourceResolverType[] resourceResolverTypes;
  private final TestRule delegate;

  /**
   * Initialize AEM context.
   * <p>
   * If context is initialized with:
   * </p>
   * <ul>
   * <li>No resource resolver type - default is used {@link MockSling#DEFAULT_RESOURCERESOLVER_TYPE}.</li>
   * <li>One resource resolver type - exactly this is used.</li>
   * <li>More than one: all unit test methods are executed for all resource resolver types using {@link ListGenerator}.</li>
   * </ul>
   * @param resourceResolverTypes Resource resolver type(s).
   */
  public AemContext(final ResourceResolverType... resourceResolverTypes) {
    this(null, null, resourceResolverTypes);
  }

  /**
   * Initialize AEM context.
   * <p>
   * If context is initialized with:
   * </p>
   * <ul>
   * <li>No resource resolver type - default is used {@link MockSling#DEFAULT_RESOURCERESOLVER_TYPE}.</li>
   * <li>One resource resolver type - exactly this is used.</li>
   * <li>More than one: all unit test methods are executed for all resource resolver types using {@link ListGenerator}.</li>
   * </ul>
   * @param setUpCallback Allows the application to register an own callback function that is called after the built-in
   *          setup rules are executed.
   * @param resourceResolverTypes Resource resolver type(s).
   */
  public AemContext(final AemContextCallback setUpCallback, final ResourceResolverType... resourceResolverTypes) {
    this(setUpCallback, null, resourceResolverTypes);
  }

  /**
   * Initialize AEM context.
   * <p>
   * If context is initialized with:
   * </p>
   * <ul>
   * <li>No resource resolver type - default is used {@link MockSling#DEFAULT_RESOURCERESOLVER_TYPE}.</li>
   * <li>One resource resolver type - exactly this is used.</li>
   * <li>More than one: all unit test methods are executed for all resource resolver types using {@link ListGenerator}.</li>
   * </ul>
   * @param setUpCallback Allows the application to register an own callback function that is called after the built-in
   *          setup rules are executed.
   * @param tearDownCallback Allows the application to register an own callback function that is called before the
   *          built-in teardown rules are executed.
   * @param resourceResolverTypes Resource resolver type(s).
   */
  public AemContext(final AemContextCallback setUpCallback,
      final AemContextCallback tearDownCallback,
      final ResourceResolverType... resourceResolverTypes) {

    this.setUpCallback = setUpCallback;
    this.tearDownCallback = tearDownCallback;

    if (resourceResolverTypes == null || resourceResolverTypes.length == 0) {
      this.resourceResolverTypes = new ResourceResolverType[] {
          MockSling.DEFAULT_RESOURCERESOLVER_TYPE
      };
    }
    else {
      this.resourceResolverTypes = resourceResolverTypes;
    }

    if (this.resourceResolverTypes.length == 1) {
      // user default rule that directly executes each test method once
      setResourceResolverType(this.resourceResolverTypes[0]);
      this.delegate = new ExternalResource() {
        @Override
        protected void before() {
          AemContext.this.setUp();
          AemContext.this.executeSetUpCallback();
        }
        @Override
        protected void after() {
          AemContext.this.executeTearDownCallback();
          AemContext.this.tearDown();
        }
      };
    }
    else {
      // use ListGenerator rule that iterates over list of resource resolver types
      Callback<ResourceResolverType> parameterizedSetUpCallback = new Callback<ResourceResolverType>() {
        @Override
        public void execute(final ResourceResolverType currrentValue) {
          AemContext.this.setResourceResolverType(currrentValue);
          AemContext.this.setUp();
          AemContext.this.executeSetUpCallback();
        }
      };
      Callback<ResourceResolverType> parameterizedTearDownCallback = new Callback<ResourceResolverType>() {
        @Override
        public void execute(final ResourceResolverType currrentValue) {
          AemContext.this.executeTearDownCallback();
          AemContext.this.tearDown();
        }
      };
      this.delegate = new ListGenerator<ResourceResolverType>(Arrays.asList(this.resourceResolverTypes),
          parameterizedSetUpCallback, parameterizedTearDownCallback);
    }
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return this.delegate.apply(base, description);
  }

  private void executeSetUpCallback() {
    if (this.setUpCallback != null) {
      try {
        this.setUpCallback.execute(this);
      }
      catch (Throwable ex) {
        throw new RuntimeException("Executing setup callback failed.", ex);
      }
    }
  }

  private void executeTearDownCallback() {
    if (this.tearDownCallback != null) {
      try {
        this.tearDownCallback.execute(this);
      }
      catch (Throwable ex) {
        throw new RuntimeException("Executing setup callback failed.", ex);
      }
    }
  }

}
