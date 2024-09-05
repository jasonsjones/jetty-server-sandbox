package com.jasonsjones.server;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.jetty.http.HttpScheme.HTTPS;
import static org.eclipse.jetty.http.HttpVersion.HTTP_1_1;

public class ApiServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiServer.class);

    public static void main(String... args) throws Exception {

        LOGGER.info("Setting up https configuration");
        HttpConfiguration httpsConfiguration = new HttpConfiguration();
        httpsConfiguration.setSecureScheme(HTTPS.asString());
        httpsConfiguration.setSecurePort(8443);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
        httpsConfiguration.setSendServerVersion(false);

        LOGGER.info("Setting up SSL (server) context factory");
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath("tutorialapi-server/src/main/resources/certs/site.p12");
        sslContextFactory.setKeyStoreType("PKCS12");
        sslContextFactory.setKeyStorePassword("changeit");
        sslContextFactory.setKeyManagerPassword("changeit");
        sslContextFactory.setTrustAll(true);

        LOGGER.info("Creating SSL and HTTP connection factories");
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HTTP_1_1.asString());
        HttpConnectionFactory httpsConnectionFactory = new HttpConnectionFactory(httpsConfiguration);
        Server server = new Server();

        LOGGER.info("Creating HTTPS connector");
        ServerConnector httpsConnector = new ServerConnector(server, sslConnectionFactory, httpsConnectionFactory);
        httpsConnector.setName("secure");
        httpsConnector.setPort(httpsConfiguration.getSecurePort());

        LOGGER.info("Creating servlet context handler");
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");
        Path webRootPath = new File("tutorialapi-server/src/main/resources/web").toPath().toRealPath();
        Resource baseResource = servletContextHandler.newResource(webRootPath.toUri());
        servletContextHandler.setBaseResource(baseResource);
        servletContextHandler.addServlet(DefaultServlet.class, "/");

        server.addConnector(httpsConnector);
        server.setHandler(servletContextHandler);

        LOGGER.info("Starting app server...");
        server.start();
        server.join();
    }
}
