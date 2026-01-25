package com.moh.yehia.order.service.controller;

import com.moh.yehia.order.service.model.Order;
import com.moh.yehia.order.service.model.OrderRequest;
import com.moh.yehia.order.service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getOrders() {
        return orderService.findAllOrders();
    }

    @PostMapping
    public Order submitOrder(@RequestBody @Valid OrderRequest orderRequest) {
        return orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity());
    }
}
