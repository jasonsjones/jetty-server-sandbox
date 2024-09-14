package com.jasonsjones.server;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import com.jasonsjones.rest.RestApi;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.jetty.http.HttpScheme.HTTPS;
import static org.eclipse.jetty.http.HttpVersion.HTTP_1_1;

public class ApiServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiServer.class);
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEYSTORE_PASSWORD = "changeit";
    private static final String WEB_ROOT_PATH = "tutorialapi-server/src/main/resources/web";

    public static void main(String... args) throws Exception {
        int port = Optional.ofNullable(System.getProperty("port")).map(Integer::parseInt).orElse(8443);
        String mode = Optional.ofNullable(System.getProperty("mode")).orElse("dev");
        LOGGER.info("Server mode: {}", mode);

        Config config = ConfigFactory.parseFile(new File(String.format("system-%s.properties", mode)));
        String keystorePath = config.getString("server.keystore.file");

        LOGGER.info("Setting up https configuration");
        HttpConfiguration httpsConfiguration = new HttpConfiguration();
        httpsConfiguration.setSecureScheme(HTTPS.asString());
        httpsConfiguration.setSecurePort(port);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
        httpsConfiguration.setSendServerVersion(false);
        httpsConfiguration.setSendDateHeader(false);

        LOGGER.info("Setting up SSL (server) context factory");
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystorePath);
        sslContextFactory.setKeyStoreType(KEYSTORE_TYPE);
        sslContextFactory.setKeyStorePassword(KEYSTORE_PASSWORD);
        sslContextFactory.setKeyManagerPassword(KEYSTORE_PASSWORD);
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
        Path webRootPath = new File(WEB_ROOT_PATH).toPath().toRealPath();
        Resource baseResource = servletContextHandler.newResource(webRootPath.toUri());
        servletContextHandler.setBaseResource(baseResource);
        servletContextHandler.addServlet(DefaultServlet.class, "/");

        ServletHolder apiServletHolder = servletContextHandler.addServlet(ServletContainer.class, "/api/*");
        apiServletHolder.setInitParameter("jakarta.ws.rs.Application", RestApi.class.getName());

        server.addConnector(httpsConnector);
        server.setHandler(servletContextHandler);

        LOGGER.info("Starting app server on port {}", port);
        server.start();
        server.join();
    }
}
