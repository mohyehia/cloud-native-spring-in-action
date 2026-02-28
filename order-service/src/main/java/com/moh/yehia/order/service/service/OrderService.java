package com.moh.yehia.order.service.service;

import com.moh.yehia.order.service.client.CatalogServiceClient;
import com.moh.yehia.order.service.event.OrderAcceptedMessage;
import com.moh.yehia.order.service.model.Book;
import com.moh.yehia.order.service.model.Order;
import com.moh.yehia.order.service.model.OrderStatus;
import com.moh.yehia.order.service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CatalogServiceClient catalogServiceClient;
    private final StreamBridge streamBridge;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(OrderService.class);

    public OrderService(OrderRepository orderRepository, CatalogServiceClient catalogServiceClient, StreamBridge streamBridge) {
        this.orderRepository = orderRepository;
        this.catalogServiceClient = catalogServiceClient;
        this.streamBridge = streamBridge;
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order submitOrder(String isbn, int quantity) {
        Book retrievedBook;
        try {
            retrievedBook = catalogServiceClient.getBookByIsbn(isbn);
        } catch (Exception e) {
            e.printStackTrace();
            retrievedBook = null;
        }
        Order order;
        if (retrievedBook == null) {
            order = rejectedOrder(quantity, isbn);
        } else {
            order = acceptedOrder(quantity, retrievedBook);
        }
        Order savedOrdered = orderRepository.save(order);
        publishOrderAcceptedEvent(savedOrdered);
        return savedOrdered;
    }

    private Order rejectedOrder(int quantity, String isbn) {
        return new Order(
                UUID.randomUUID().toString(),
                isbn,
                null,
                null,
                quantity,
                OrderStatus.REJECTED,
                null,
                null,
                0
        );
    }

    private Order acceptedOrder(int quantity, Book book) {
        return new Order(
                UUID.randomUUID().toString(),
                book.isbn(),
                book.title(),
                book.price(),
                quantity,
                OrderStatus.ACCEPTED,
                null,
                null,
                0
        );
    }

    private void publishOrderAcceptedEvent(Order order) {
        if (!order.status().equals(OrderStatus.ACCEPTED)) {
            return;
        }
        OrderAcceptedMessage orderAcceptedMessage = new OrderAcceptedMessage(order.id());
        LOGGER.info("Sending order accepted event with id: {}", order.id());
        boolean result = streamBridge.send("acceptOrder-out-0", orderAcceptedMessage);
        LOGGER.info("Result of sending data for order with id {}: {}", order.id(), result);
    }

    public void dispatchOrder(String orderId) {
        Optional<Order> order = orderRepository.findById(orderId)
                .map(this::buildDispatchedOrder);
        order.ifPresent(orderRepository::save);
    }

    private Order buildDispatchedOrder(Order order) {
        return new Order(
                order.id(),
                order.bookIsbn(),
                order.bookName(),
                order.bookPrice(),
                order.quantity(),
                OrderStatus.DISPATCHED,
                order.createdDate(),
                order.lastModifiedDate(),
                order.version());
    }
}
