package com.moh.yehia.dispatcher.service.functions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
class FunctionsStreamIntegrationTests {
    @Autowired
    private InputDestination inputDestination;

    @Autowired
    private OutputDestination outputDestination;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenOrder_whenOrderAccepted_thenOrderDispatched() throws Exception {
        // Given
        Message<OrderAcceptedMessage> inputMessage = MessageBuilder.withPayload(new OrderAcceptedMessage("order-123")).build();

        Message<OrderDispatchedMessage> expectedOutputMessage = MessageBuilder.withPayload(new OrderDispatchedMessage("order-123")).build();

        // When
        inputDestination.send(inputMessage);

        // Then
        OrderDispatchedMessage orderDispatchedMessage = objectMapper.readValue(outputDestination.receive().getPayload(), OrderDispatchedMessage.class);

        Assertions.assertThat(orderDispatchedMessage).isEqualTo(expectedOutputMessage.getPayload());
    }
}
