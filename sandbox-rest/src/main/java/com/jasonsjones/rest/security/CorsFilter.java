package com.jasonsjones.rest.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext reqContext, ContainerResponseContext resContext) {
        resContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        resContext.getHeaders().add("Access-Control-Allow-Methods", "DELTETE, HEAD, GET, OPTIONS, PATCH, POST, PUT");
    }
}
