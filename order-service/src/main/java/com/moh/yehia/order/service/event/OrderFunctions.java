package com.moh.yehia.order.service.event;

import com.moh.yehia.order.service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class OrderFunctions {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderFunctions.class);

    @Bean
    public Consumer<OrderDispatchedMessage> dispatchOrder(OrderService orderService) {
        return orderDispatchedMessage -> {
            LOGGER.info("Dispatching order with ID: {}", orderDispatchedMessage.orderId());
            // Simulate dispatching logic here
            orderService.dispatchOrder(orderDispatchedMessage.orderId());
        };
    }
}
