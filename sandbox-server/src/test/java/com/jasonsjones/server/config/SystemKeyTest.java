package com.jasonsjones.server.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SystemKeyTest {
    @Test
    public void testDefaultPort() {
        String expectedPort = "8443";
        Assertions.assertEquals(expectedPort, SystemKey.PORT.getDefaultValue());
    }

    @Test
    public void testDefaultMode() {
        String expectedMode = "dev";
        Assertions.assertEquals(expectedMode, SystemKey.MODE.getDefaultValue());
    }

    @Test
    public void testGetKey() {
        Assertions.assertEquals("port", SystemKey.PORT.getKey());
    }
}
