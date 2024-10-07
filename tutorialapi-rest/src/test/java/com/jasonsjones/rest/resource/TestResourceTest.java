package com.jasonsjones.rest.resource;

import com.jasonsjones.rest.RestApi;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.LogManager;

public class TestResourceTest extends JerseyTest {

    static {
        LogManager.getLogManager().reset();
    }

    @Override
    protected Application configure() {
        return new RestApi();
    }

    @Test
    public void test() {
        Response res = target("/status/ping").request().get();

        Assertions.assertEquals(200, res.getStatus());
        Assertions.assertEquals("Pong", res.readEntity(String.class));
        Assertions.assertEquals("*", res.getHeaderString("Access-Control-Allow-Origin"));
        Assertions.assertEquals(
                "DELTETE, HEAD, GET, OPTIONS, PATCH, POST, PUT",
                res.getHeaderString("Access-Control-Allow-Methods"));
    }
}
