package io.wcm.testing.mock.aem.junit5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AemContextExtension.class)
class AemContextTest {

    @BeforeEach
    void setUp(ResourceResolverMockAemContext context) {
        // TODO
    }

    @Test
    void test(JcrMockAemContext context) {
        // TODO
    }

}
