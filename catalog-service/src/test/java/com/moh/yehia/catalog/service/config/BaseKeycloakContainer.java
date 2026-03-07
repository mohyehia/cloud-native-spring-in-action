package com.moh.yehia.catalog.service.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

public abstract class BaseKeycloakContainer {
    static final KeycloakContainer KEYCLOAK_CONTAINER;

    static {
        KEYCLOAK_CONTAINER = new KeycloakContainer("quay.io/keycloak/keycloak:26.5.4")
                .withRealmImportFile("/PolarBookshop-realm.json");
        KEYCLOAK_CONTAINER.setPortBindings(List.of("8080:8080", "9000:9000")); // to force test container to use the same port
        KEYCLOAK_CONTAINER.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> KEYCLOAK_CONTAINER.getAuthServerUrl() + "realms/PolarBookshop");
    }
}
