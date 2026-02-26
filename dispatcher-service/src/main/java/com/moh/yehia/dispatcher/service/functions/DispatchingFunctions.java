package com.moh.yehia.dispatcher.service.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class DispatchingFunctions {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatchingFunctions.class);

    @Bean
    public Function<OrderAcceptedMessage, String> pack() {
        return orderAcceptedMessage -> {
            LOGGER.info("Packing order with ID: {}", orderAcceptedMessage.orderId());
            // Simulate packing logic here
            return orderAcceptedMessage.orderId();
        };
    }

    @Bean
    public Function<String, OrderDispatchedMessage> label() {
        return orderId -> {
            LOGGER.info("Labeling order with ID: {}", orderId);
            // Simulate labeling logic here
            return new OrderDispatchedMessage(orderId);
        };
    }
}
