package com.moh.yehia.catalog.service.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

public abstract class PlatformPrerequisiteContainers {

    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;
    static final KeycloakContainer KEYCLOAK_CONTAINER;


    static {
        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.1"))
                .withUsername("catalog_service")
                .withPassword("Password")
                .withDatabaseName("catalog_service");
        POSTGRESQL_CONTAINER.setPortBindings(List.of("5432:5432")); // to force test container to use the same port
        POSTGRESQL_CONTAINER.start();

        KEYCLOAK_CONTAINER = new KeycloakContainer("quay.io/keycloak/keycloak:26.5.4")
                .withRealmImportFile("/PolarBookshop-realm.json");
        KEYCLOAK_CONTAINER.setPortBindings(List.of("8080:8080", "9000:9000")); // to force test container to use the same port
        KEYCLOAK_CONTAINER.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> KEYCLOAK_CONTAINER.getAuthServerUrl() + "realms/PolarBookshop");
    }

    public String tokenEndpoint() {
        return KEYCLOAK_CONTAINER.getAuthServerUrl() + "realms/PolarBookshop/protocol/openid-connect/token";
    }
}
