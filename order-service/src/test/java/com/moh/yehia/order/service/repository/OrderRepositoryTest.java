package com.moh.yehia.order.service.repository;

import com.moh.yehia.order.service.config.BaseMongoContainer;
import com.moh.yehia.order.service.config.DataConfig;
import com.moh.yehia.order.service.model.Order;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;

@Import(DataConfig.class)
@DataMongoTest
class OrderRepositoryTest extends BaseMongoContainer {
    @Autowired
    private OrderRepository orderRepository;

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
    }

    @Test
    void givenOrder_whenSaveOrder_thenReturnSavedOrder() {
        var order = getDefaultOrder();
        Order savedOrder = orderRepository.save(order);
        Assertions.assertThat(savedOrder)
                .isNotNull();
        Assertions.assertThat(savedOrder.id()).isNotNull();
        Assertions.assertThat(savedOrder.bookIsbn()).isEqualTo(order.bookIsbn());
        Assertions.assertThat(savedOrder.bookName()).isEqualTo(order.bookName());
        Assertions.assertThat(savedOrder.bookPrice()).isEqualTo(order.bookPrice());
        Assertions.assertThat(savedOrder.quantity()).isEqualTo(order.quantity());
        Assertions.assertThat(savedOrder.version()).isGreaterThan(0);
        Assertions.assertThat(savedOrder.createdDate()).isNotNull();
        Assertions.assertThat(savedOrder.lastModifiedDate()).isNotNull();
    }

    @Test
    void givenEmptyOrders_whenFindAll_thenReturnEmptyList() {
        var orders = orderRepository.findAll();
        Assertions.assertThat(orders).isNotNull().isEmpty();
    }

    @Test
    void givenOrders_whenFindAll_thenReturnListOfOrders() {
        var order = getDefaultOrder();
        orderRepository.save(order);
        var orders = orderRepository.findAll();
        Assertions.assertThat(orders).isNotNull().isNotEmpty().hasSize(1);
    }

    private Order getDefaultOrder() {
        return new Order(
                null,
                "1234567890",
                "Sample Book",
                19.99,
                1,
                null,
                null,
                null,
                0
        );
    }
}
