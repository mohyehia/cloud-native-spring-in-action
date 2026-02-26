package com.moh.yehia.dispatcher.service.functions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@FunctionalSpringBootTest
class DispatchingFunctionsTest {

    @Autowired
    private FunctionCatalog functionCatalog;

    @Test
    void packFunctionShouldWork() {
        Function<OrderAcceptedMessage, String> pack = functionCatalog.lookup(Function.class, "pack");
        String orderId = "order_123";
        String packedOrderId = pack.apply(new OrderAcceptedMessage(orderId));
        assertEquals(orderId, packedOrderId);
    }

    @Test
    void labelFunctionShouldWork() {
        Function<String, OrderDispatchedMessage> label = functionCatalog.lookup(Function.class, "label");
        String orderId = "order_123";
        OrderDispatchedMessage dispatchedOrder = label.apply(orderId);
        assertEquals(new OrderDispatchedMessage(orderId), dispatchedOrder);
    }

    @Test
    void packAndLabelFunctionsShouldWork() {
        Function<OrderAcceptedMessage, OrderDispatchedMessage> packAndLabel = functionCatalog.lookup(Function.class, "pack|label");
        String orderId = "order_123";
        OrderDispatchedMessage dispatchedOrder = packAndLabel.apply(new OrderAcceptedMessage(orderId));
        assertEquals(new OrderDispatchedMessage(orderId), dispatchedOrder);
    }

}