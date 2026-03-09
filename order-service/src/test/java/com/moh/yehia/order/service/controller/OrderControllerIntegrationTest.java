package com.moh.yehia.order.service.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.moh.yehia.order.service.config.DataConfig;
import com.moh.yehia.order.service.config.PlatformPrerequisiteContainers;
import com.moh.yehia.order.service.constant.TestConstants;
import com.moh.yehia.order.service.model.Order;
import com.moh.yehia.order.service.model.OrderRequest;
import com.moh.yehia.order.service.model.OrderStatus;
import com.moh.yehia.order.service.repository.OrderRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import({DataConfig.class, TestChannelBinderConfiguration.class})
class OrderControllerIntegrationTest extends PlatformPrerequisiteContainers {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static MockWebServer mockWebServer;

    private static KeycloakToken customerToken;

    @BeforeAll
    static void generateAccessTokens() {
        // configure RestTemplate for calling keycloak to generate access tokens for testing secured endpoints
        System.out.println("token endpoint => " + tokenEndpoint());
        RestTemplate restTemplate = new RestTemplate();
        customerToken = authenticateWithKeycloak(restTemplate);
        System.out.println("Customer token: Authorization " + customerToken.accessToken);
    }

    @BeforeEach
    void setup() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start(9091);
        orderRepository.deleteAll();
    }

    @Test
    void givenEmptyListOfOrders_whenGetAllOrders_thenReturnEmptyList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken.accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("[]"))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    void givenListOfOrders_whenGetAllOrders_thenOrdersAreReturned() throws Exception {
        var order = getDefaultOrder();
        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken.accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Order> orders = objectMapper.readValue(contentAsString, objectMapper.getTypeFactory().constructCollectionType(List.class, Order.class));
        Assertions.assertThat(orders)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);
        Assertions.assertThat(orders.getFirst())
                .isNotNull();
        var retrievedOrder = orders.getFirst();
        Assertions.assertThat(retrievedOrder.id()).isNotNull();
        Assertions.assertThat(retrievedOrder.bookIsbn()).isNotNull();
        Assertions.assertThat(retrievedOrder.status()).isNotNull();
        Assertions.assertThat(retrievedOrder.createdDate()).isNotNull();
        Assertions.assertThat(retrievedOrder.lastModifiedDate()).isNotNull();
        Assertions.assertThat(retrievedOrder.version()).isGreaterThan(order.version());
    }

    @Test
    void givenExistingBook_whenSubmitOrder_thenOrderIsAccepted() throws Exception {
        var bookIsbn = "1234567890";
        var mockResponse = new MockResponse()
                .setBody(TestConstants.EXISTING_BOOK.formatted(bookIsbn))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json");
        mockWebServer.enqueue(mockResponse);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken.accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequest(bookIsbn, 2))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Order returnedOrderFromResponse = objectMapper.readValue(contentAsString, Order.class);
        Assertions.assertThat(returnedOrderFromResponse)
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("bookIsbn", bookIsbn)
                .hasFieldOrPropertyWithValue("status", OrderStatus.ACCEPTED);

        List<Order> orders = orderRepository.findAll();
        Assertions.assertThat(orders)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);
        Assertions.assertThat(orders.getFirst())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("bookIsbn", bookIsbn)
                .hasFieldOrPropertyWithValue("status", OrderStatus.ACCEPTED);
    }

    @Test
    void givenNonExistingBook_whenSubmitOrder_thenOrderIsRejected() throws Exception {
        var bookIsbn = "0987654321";
        var mockResponse = new MockResponse()
                .setBody("The book with ISBN " + bookIsbn + " was not found.")
                .setResponseCode(404);
        mockWebServer.enqueue(mockResponse);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken.accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequest(bookIsbn, 2))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Order returnedOrderFromResponse = objectMapper.readValue(contentAsString, Order.class);

        Assertions.assertThat(returnedOrderFromResponse)
                .isNotNull()
                .hasFieldOrPropertyWithValue("bookIsbn", bookIsbn)
                .hasFieldOrPropertyWithValue("status", OrderStatus.REJECTED);

        List<Order> orders = orderRepository.findAll();
        Assertions.assertThat(orders)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);
        Assertions.assertThat(orders.getFirst())
                .isNotNull()
                .hasFieldOrPropertyWithValue("bookIsbn", bookIsbn)
                .hasFieldOrPropertyWithValue("status", OrderStatus.REJECTED);
    }

    @Test
    void givenUnauthenticatedUser_whenGetAllOrders_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/orders"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    void givenUnauthenticatedUser_whenSubmitOrder_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequest("1234567890", 2))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    private Order getDefaultOrder() {
        return new Order(
                "order-id",
                "1234567890",
                "Test Book",
                19.95,
                1,
                OrderStatus.ACCEPTED,
                null,
                null,
                "e28b0395-6c30-4e47-88e3-9b447bfb7267",
                "e28b0395-6c30-4e47-88e3-9b447bfb7267",
                0
        );
    }


    @AfterEach
    void cleanUp() throws IOException {
        mockWebServer.shutdown();
        orderRepository.deleteAll();
    }

    private static KeycloakToken authenticateWithKeycloak(RestTemplate restTemplate) {
        // implement the logic for calling keycloak to generate access token for the given username and password
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(OAuth2ParameterNames.GRANT_TYPE, "password");
        formData.add(OAuth2ParameterNames.CLIENT_ID, "polar-test");
        formData.add(OAuth2Constants.USERNAME, "customer-user");
        formData.add(OAuth2Constants.PASSWORD, "password");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, httpHeaders);
        ResponseEntity<KeycloakToken> tokenResponseEntity = restTemplate.postForEntity(tokenEndpoint(), request, KeycloakToken.class);
        return tokenResponseEntity.getBody();
    }

    private record KeycloakToken(String accessToken) {
        @JsonCreator
        private KeycloakToken(@JsonProperty("access_token") final String accessToken) {
            this.accessToken = accessToken;
        }
    }
}