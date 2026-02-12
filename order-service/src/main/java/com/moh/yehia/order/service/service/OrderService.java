package com.moh.yehia.order.service.service;

import com.moh.yehia.order.service.config.CatalogServiceClient;
import com.moh.yehia.order.service.model.Book;
import com.moh.yehia.order.service.model.Order;
import com.moh.yehia.order.service.model.OrderStatus;
import com.moh.yehia.order.service.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CatalogServiceClient catalogServiceClient;

    public OrderService(OrderRepository orderRepository, CatalogServiceClient catalogServiceClient) {
        this.orderRepository = orderRepository;
        this.catalogServiceClient = catalogServiceClient;
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

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
        return orderRepository.save(order);
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
}
