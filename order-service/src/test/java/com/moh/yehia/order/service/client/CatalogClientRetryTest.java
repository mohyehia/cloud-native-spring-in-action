//package com.moh.yehia.order.service.client;
//
//import okhttp3.mockwebserver.MockResponse;
//import okhttp3.mockwebserver.MockWebServer;
//import org.junit.Assert;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestClient;
//import org.springframework.web.client.support.RestClientAdapter;
//import org.springframework.web.service.invoker.HttpServiceProxyFactory;
//
//import java.io.IOException;
//import java.time.Duration;
//
//@SpringBootTest
//class CatalogClientRetryTest {
//
//    private static MockWebServer mockWebServer;
//
//    @Autowired
//    private CatalogServiceClient catalogServiceClient;
//
//    @BeforeAll
//    static void startServer() throws IOException {
//        mockWebServer = new MockWebServer();
//        mockWebServer.start(); // Start here so the URL is available for beans
//    }
//
//    @AfterAll
//    static void stopServer() throws IOException {
//        mockWebServer.shutdown();
//    }
//
//    @TestConfiguration
//    static class TestConfig {
//        @Bean
//        public CatalogServiceClient catalogServiceClient(RestClient.Builder builder) {
//            // Using the static server's URL safely
//            SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
//            simpleClientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(1));
//            simpleClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(1));
//
//
//            var restClient = builder
//                    .requestFactory(simpleClientHttpRequestFactory)
//                    .baseUrl(mockWebServer.url("/").toString())
//                    .build();
//
//            return HttpServiceProxyFactory
//                    .builderFor(RestClientAdapter.create(restClient))
//                    .build()
//                    .createClient(CatalogServiceClient.class);
//        }
//    }
//
//    @Test
//    void shouldRetry_WhenBadGatewayExceptionIsRetrieved() {
//        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_GATEWAY.value()));
//        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_GATEWAY.value()));
//        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_GATEWAY.value()));
//        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_GATEWAY.value()));
//
//        try {
//            catalogServiceClient.getBookByIsbn("1234567890");
//        } catch (Exception e) {
//            System.out.println("Exception occurred: " + e.getMessage());
//        }
//
//        System.out.println("Total requests made: " + mockWebServer.getRequestCount());
//        // Verify that only 1 request was made (no retries)
//        assert (mockWebServer.getRequestCount() == 4);
//    }
//
//    @Test
//    void shouldNotRetry_WhenBadRequestExceptionIsRetrieved() {
//        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value()));
//        Assert.assertThrows(HttpClientErrorException.BadRequest.class, () -> catalogServiceClient.getBookByIsbn("1234567890"));
//        // Verify that only 1 request was made (no retries)
//        assert (mockWebServer.getRequestCount() == 1);
//    }
//}
