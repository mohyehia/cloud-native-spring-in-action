package com.moh.yehia.apigateway.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.stream.Collectors;

@WebFluxTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    void whenUserUnauthenticated_thenUnauthorized() {
        webTestClient.get()
                .uri("/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void whenUserAuthenticated_thenReturnUser() {
        var expectedUser = new User("moh-yehia", "john", "doe", List.of("employee", "customer"));
        webTestClient.mutateWith(oidcLogin(expectedUser))
                .get()
                .uri("/user")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(User.class)
                .value(user -> {
                    Assertions.assertThat(user).isNotNull();
                    Assertions.assertThat(user.username()).isEqualTo(expectedUser.username());
                    Assertions.assertThat(user.firstName()).isEqualTo(expectedUser.firstName());
                    Assertions.assertThat(user.lastName()).isEqualTo(expectedUser.lastName());
                    Assertions.assertThat(user.roles()).containsExactlyInAnyOrderElementsOf(expectedUser.roles());
                });
    }

    private SecurityMockServerConfigurers.OidcLoginMutator oidcLogin(User user) {
        return SecurityMockServerConfigurers.mockOidcLogin()
                .idToken(
                        token -> token.claim(StandardClaimNames.PREFERRED_USERNAME, user.username())
                                .claim(StandardClaimNames.GIVEN_NAME, user.firstName())
                                .claim(StandardClaimNames.FAMILY_NAME, user.lastName())
                ).authorities(user.roles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));
    }
}