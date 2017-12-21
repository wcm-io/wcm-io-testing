package io.wcm.testing.mock.aem.junit5;

import io.wcm.testing.mock.aem.context.AemContextImpl;

public abstract class AemContext extends AemContextImpl {

    protected void setUpContext() {
        super.setUp();
    }

    protected void tearDownContext() {
        super.tearDown();
    }

}
