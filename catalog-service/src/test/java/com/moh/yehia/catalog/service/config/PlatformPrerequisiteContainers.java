package com.moh.yehia.catalog.service.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PlatformPrerequisiteContainers {

    static final KeycloakContainer KEYCLOAK_CONTAINER = BaseKeycloakContainer.KEYCLOAK_CONTAINER;
    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = BasePostgresqlContainer.POSTGRESQL_CONTAINER;


    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> KEYCLOAK_CONTAINER.getAuthServerUrl() + "/realms/PolarBookshop");
    }

    public static String tokenEndpoint() {
        return KEYCLOAK_CONTAINER.getAuthServerUrl() + "/realms/PolarBookshop/protocol/openid-connect/token";
    }
}
