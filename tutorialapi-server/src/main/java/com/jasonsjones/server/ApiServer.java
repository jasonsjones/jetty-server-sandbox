package com.jasonsjones.server;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import com.jasonsjones.rest.RestApi;
import com.jasonsjones.server.config.ConfigKey;
import com.jasonsjones.server.config.SystemKey;
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

    private static final String PROPERTY_FILE = "system-%s.properties";
    private static final String ROOT_CONTEXT = "/";
    private static final String API_PATTERN = "/api/*";
    private static final String APPLICATION_INIT_PARAM_KEY = "jakarta.ws.rs.Application";

    public static void main(String... args) throws Exception {
        int port = Integer.parseInt(
                Optional
                        .ofNullable(System.getProperty(SystemKey.PORT.getKey()))
                        .orElse(SystemKey.PORT.getDefaultValue()));
        String mode = Optional
                .ofNullable(System.getProperty(SystemKey.MODE.getKey()))
                .orElse(SystemKey.MODE.getDefaultValue());

        Config config = ConfigFactory.parseFile(new File(String.format(PROPERTY_FILE, mode)));

        LOGGER.info("Setting up https configuration");
        HttpConfiguration httpsConfiguration = new HttpConfiguration();
        httpsConfiguration.setSecureScheme(HTTPS.asString());
        httpsConfiguration.setSecurePort(port);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
        httpsConfiguration.setSendServerVersion(false);
        httpsConfiguration.setSendDateHeader(false);

        LOGGER.info("Setting up SSL (server) context factory");
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(config.getString(ConfigKey.SERVER_KEYSTORE_FILE.getKey()));
        sslContextFactory.setKeyStoreType(config.getString(ConfigKey.SERVER_KEYSTORE_TYPE.getKey()));
        sslContextFactory.setKeyStorePassword(config.getString(ConfigKey.SERVER_KEYSTORE_PASSWORD.getKey()));
        sslContextFactory.setKeyManagerPassword(config.getString(ConfigKey.SERVER_KEYSTORE_PASSWORD.getKey()));
        sslContextFactory.setTrustAll(true);

        LOGGER.info("Creating SSL and HTTP connection factories");
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HTTP_1_1.asString());
        HttpConnectionFactory httpsConnectionFactory = new HttpConnectionFactory(httpsConfiguration);
        Server server = new Server();

        LOGGER.info("Creating HTTPS connector");
        ServerConnector httpsConnector = new ServerConnector(server, sslConnectionFactory, httpsConnectionFactory);
        httpsConnector.setPort(httpsConfiguration.getSecurePort());

        LOGGER.info("Creating servlet context handler");
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(ROOT_CONTEXT);
        LOGGER.info("Web root: {}", config.getString(ConfigKey.SERVER_WEB_ROOT.getKey()));
        Path webRootPath = new File(config.getString(ConfigKey.SERVER_WEB_ROOT.getKey())).toPath().toRealPath();
        Resource baseResource = servletContextHandler.newResource(webRootPath.toUri());
        servletContextHandler.setBaseResource(baseResource);
        servletContextHandler.addServlet(DefaultServlet.class, ROOT_CONTEXT);

        ServletHolder apiServletHolder = servletContextHandler.addServlet(ServletContainer.class, API_PATTERN);
        apiServletHolder.setInitParameter(APPLICATION_INIT_PARAM_KEY, RestApi.class.getName());

        server.addConnector(httpsConnector);
        server.setHandler(servletContextHandler);

        LOGGER.info("Starting app server on port {}", port);
        server.start();
        server.join();
    }
}
