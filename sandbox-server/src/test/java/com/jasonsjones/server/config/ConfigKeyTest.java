package com.jasonsjones.server.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigKeyTest {
    @Test
    public void testGetKey() {
        String expected = "server.keystore.file";
        String actual = ConfigKey.SERVER_KEYSTORE_FILE.getKey();

        Assertions.assertEquals(expected, actual);
    }
}
