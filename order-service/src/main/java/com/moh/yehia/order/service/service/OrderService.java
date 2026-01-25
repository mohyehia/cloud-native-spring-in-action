package com.moh.yehia.order.service.service;

import com.moh.yehia.order.service.model.Order;
import com.moh.yehia.order.service.model.OrderStatus;
import com.moh.yehia.order.service.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public Order submitOrder(String isbn, int quantity) {
        Order order = new Order(
                UUID.randomUUID().toString(),
                isbn,
                "Sample Book Name",
                29.99,
                quantity,
                OrderStatus.REJECTED,
                null,
                null,
                0
        );
        return orderRepository.save(order);
    }
}
