package com.moh.yehia.order.service.client;

import com.moh.yehia.order.service.constant.TestConstants;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.io.IOException;
import java.time.Duration;

class CatalogServiceClientTest {
    private MockWebServer mockWebServer;
    private CatalogServiceClient catalogServiceClient;

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(9091);

        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(1));
        simpleClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(1));

        var restClient = RestClient.builder()
                .requestFactory(simpleClientHttpRequestFactory)
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        catalogServiceClient = proxyFactory.createClient(CatalogServiceClient.class);
    }

    @Test
    void givenBook_whenBookExists_thenReturnBook() {
        var bookIsbn = "1234567890";
        var mockResponse = new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(TestConstants.EXISTING_BOOK.formatted(bookIsbn));
        mockWebServer.enqueue(mockResponse);

        var book = catalogServiceClient.getBookByIsbn(bookIsbn);

        Assertions.assertThat(book)
                .isNotNull()
                .extracting("isbn", "title", "author", "price", "publisher", "version")
                .containsExactly(bookIsbn, "Test Book", "Test Author", 19.99, "Test Publisher", 1);
    }

    @Test
    void givenBook_whenBookDoesNotExist_thenThrowException() {
        var bookIsbn = "0987654321";
        var mockResponse = new MockResponse()
                .setBody("The book with ISBN " + bookIsbn + " was not found.")
                .setResponseCode(404);
        mockWebServer.enqueue(mockResponse);

        Assertions.assertThatThrownBy(() -> catalogServiceClient.getBookByIsbn(bookIsbn))
                .isInstanceOf(HttpClientErrorException.NotFound.class)
                .hasMessageContaining("The book with ISBN " + bookIsbn + " was not found.");
    }

    @Test
    void whenServerTimeout_thenThrowException() {
        var bookIsbn = "1234567890";
        var mockResponse = new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(TestConstants.EXISTING_BOOK.formatted(bookIsbn))
                .setBodyDelay(3, java.util.concurrent.TimeUnit.SECONDS); // Simulate a delay longer than the client's timeout
        mockWebServer.enqueue(mockResponse);

        Assertions.assertThatThrownBy(() -> catalogServiceClient.getBookByIsbn(bookIsbn))
                .isInstanceOf(RestClientException.class)
                .hasRootCauseInstanceOf(java.net.SocketTimeoutException.class);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
}