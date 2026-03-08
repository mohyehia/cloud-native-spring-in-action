package com.moh.yehia.order.service.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mongodb.MongoDBContainer;

public abstract class PlatformPrerequisiteContainers {

    static final KeycloakContainer KEYCLOAK_CONTAINER = BaseKeycloakContainer.KEYCLOAK_CONTAINER;
    static final MongoDBContainer MONGO_DB_CONTAINER = BaseMongoContainer.MONGO_DB_CONTAINER;


    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> KEYCLOAK_CONTAINER.getAuthServerUrl() + "/realms/PolarBookshop");
    }

    public static String tokenEndpoint() {
        return KEYCLOAK_CONTAINER.getAuthServerUrl() + "/realms/PolarBookshop/protocol/openid-connect/token";
    }
}
