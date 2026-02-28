package com.moh.yehia.order.service.service;

import com.moh.yehia.order.service.config.BaseMongoContainer;
import com.moh.yehia.order.service.config.DataConfig;
import com.moh.yehia.order.service.constant.TestConstants;
import com.moh.yehia.order.service.event.OrderAcceptedMessage;
import com.moh.yehia.order.service.model.Order;
import com.moh.yehia.order.service.model.OrderStatus;
import com.moh.yehia.order.service.repository.OrderRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Import({DataConfig.class, TestChannelBinderConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceTest extends BaseMongoContainer {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    private static MockWebServer mockWebServer;

    @Autowired
    private OutputDestination outputDestination;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start(9091);
        orderRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() throws IOException {
        mockWebServer.shutdown();
        orderRepository.deleteAll();
    }

    @Test
    void givenEmptyListOfOrders_whenFindAllOrders_thenReturnEmptyList() {
        var orders = orderService.findAllOrders();
        Assertions.assertThat(orders)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void givenListOfOrders_whenFindAllOrders_thenOrdersAreReturned() {
        var order = getDefaultOrder();
        orderRepository.save(order);
        var orders = orderService.findAllOrders();
        Assertions.assertThat(orders)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);
    }

    @Test
    void givenExistingBook_whenSubmitOrder_thenOrderIsAccepted() {
        var bookIsbn = "1234567890";
        var mockResponse = new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(TestConstants.EXISTING_BOOK.formatted(bookIsbn));
        mockWebServer.enqueue(mockResponse);

        orderService.submitOrder(bookIsbn, 2);

        var orders = orderService.findAllOrders();
        Assertions.assertThat(orders)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .first()
                .extracting("bookIsbn", "bookName", "bookPrice", "quantity", "status")
                .containsExactly(bookIsbn, "Test Book", 19.99, 2, OrderStatus.ACCEPTED);

        String orderId = orders.getFirst().id();
        Assertions.assertThat(objectMapper.readValue(outputDestination.receive().getPayload(), OrderAcceptedMessage.class))
                .isEqualTo(new OrderAcceptedMessage(orderId));
    }

    @Test
    void givenNonExistingBook_whenSubmitOrder_thenOrderIsRejected() {
        var bookIsbn = "0987654321";
        var mockResponse = new MockResponse()
                .setBody("The book with ISBN " + bookIsbn + " was not found.")
                .setResponseCode(404);
        mockWebServer.enqueue(mockResponse);

        orderService.submitOrder(bookIsbn, 2);

        var orders = orderService.findAllOrders();
        Assertions.assertThat(orders)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .first()
                .extracting("bookIsbn", "bookName", "bookPrice", "quantity", "status")
                .containsExactly(bookIsbn, null, null, 2, OrderStatus.REJECTED);
    }

    private Order getDefaultOrder() {
        return new Order(
                "order-id",
                "1234567890",
                "Test Book",
                19.95,
                1,
                OrderStatus.REJECTED,
                null,
                null,
                0
        );
    }
}