package de.exit.ytdownloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppTest {

    @Test
    void testAppInstantiation() {
        App app = new App();
        assertNotNull(app);
    }
}
