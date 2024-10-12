package com.jasonsjones.rest;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestApi extends ResourceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestApi.class);

    public RestApi() {
        packages(RestApi.class.getPackageName());

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                LOGGER.info("Configuring binder...");
            }
        });
    }
}
