package com.jasonsjones.server;

import org.eclipse.jetty.server.HttpConfiguration;

import static org.eclipse.jetty.http.HttpScheme.HTTPS;

public class ApiServer {
    public static void main(String... args) {
        System.out.println("Hello App Server!");

        HttpConfiguration httpsConfiguration = new HttpConfiguration();
        httpsConfiguration.setSecureScheme(HTTPS.asString());
    }
}