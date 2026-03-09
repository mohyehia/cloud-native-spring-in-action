package com.moh.yehia.order.service.repository;

import com.moh.yehia.order.service.config.BaseMongoContainer;
import com.moh.yehia.order.service.config.DataConfig;
import com.moh.yehia.order.service.model.Order;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

@Import(DataConfig.class)
@DataMongoTest
class OrderRepositoryTest extends BaseMongoContainer {
    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    @WithMockUser("test-user")
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
        Assertions.assertThat(savedOrder.createdBy()).isNotNull().isEqualTo("test-user");
        Assertions.assertThat(savedOrder.lastModifiedBy()).isNotNull().isEqualTo("test-user");
    }

    @Test
    void givenUnauthenticatedUser_whenCreateOrder_thenNoAuditMetadata() {
        var order = getDefaultOrder();
        Order savedOrder = orderRepository.save(order);
        Assertions.assertThat(savedOrder)
                .isNotNull();
        Assertions.assertThat(savedOrder.createdBy()).isNull();
        Assertions.assertThat(savedOrder.lastModifiedBy()).isNull();
    }

    @Test
    void givenEmptyOrders_whenFindAll_thenReturnEmptyList() {
        var orders = orderRepository.findAll();
        Assertions.assertThat(orders).isNotNull().isEmpty();
    }

    @Test
    @WithMockUser("test-user")
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
                null,
                null,
                0
        );
    }
}
