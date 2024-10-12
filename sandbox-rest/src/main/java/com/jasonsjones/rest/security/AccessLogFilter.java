package com.jasonsjones.rest.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AccessLogFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger("access-log");

    @Override
    public void filter(ContainerRequestContext context) {
        String user = "Guest";
        String method = context.getMethod();
        String path = context.getUriInfo().getAbsolutePath().getPath();

        LOGGER.info("{} => {} {}", user, method, path);
    }
}
