package com.moh.yehia.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import(SecurityConfig.class)
class SecurityConfigTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    void whenLogoutNotAuthenticatedAndNoCsrfToken_ThenLogoutForbidden() {
        webTestClient
                .post()
                .uri("/logout")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void whenLogoutAuthenticatedAndNoCsrfToken_ThenLogoutForbidden() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockOidcLogin())
                .post()
                .uri("/logout")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void whenLogoutAuthenticatedAndWithCsrfToken_thenLogoutSuccess() {
        when(clientRegistrationRepository.findByRegistrationId(anyString()))
                .thenReturn(Mono.just(clientRegistration()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockOidcLogin())
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/logout")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    private ClientRegistration clientRegistration() {
        return ClientRegistration.withRegistrationId("keycloak")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("api-gateway")
                .authorizationUri("https://sss.workshop.com/auth")
                .tokenUri("https://sss.workshop.com/token")
                .redirectUri("https://workshop.com")
                .build();
    }
}