package com.moh.yehia.order.service.event;

import com.moh.yehia.order.service.config.BaseMongoContainer;
import com.moh.yehia.order.service.config.DataConfig;
import com.moh.yehia.order.service.model.Order;
import com.moh.yehia.order.service.model.OrderStatus;
import com.moh.yehia.order.service.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.MessageBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({DataConfig.class, TestChannelBinderConfiguration.class})
class OrderFunctionsTest extends BaseMongoContainer {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InputDestination inputDestination;


    @Test
    void givenOrderDispatchedMessage_whenDispatchOrder_thenOrderIsDispatched() {
        // Given
        var order = getDefaultOrder();
        Order savedOrder = orderRepository.save(order);

        // When
        inputDestination.send(MessageBuilder.withPayload(new OrderDispatchedMessage(savedOrder.id())).build());

        // Then
        var foundOrder = orderRepository.findById(savedOrder.id());
        Assertions.assertThat(foundOrder).isPresent();
        Assertions.assertThat(foundOrder.get().status()).isEqualTo(OrderStatus.DISPATCHED);
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