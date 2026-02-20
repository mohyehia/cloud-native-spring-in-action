package com.moh.yehia.order.service.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

@JsonTest
class OrderRequestJsonTest {
    @Autowired
    private JacksonTester<OrderRequest> orderRequestJacksonTester;

    @Test
    void givenOrderRequest_whenSerialize_thenCorrect() throws IOException {
        var orderRequest = new OrderRequest("1234567890", 2);
        var jsonContent = orderRequestJacksonTester.write(orderRequest);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.isbn").isEqualTo(orderRequest.isbn());
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("@.quantity").isEqualTo(orderRequest.quantity());
    }

    @Test
    void givenJsonContent_whenDeserialize_thenCorrect() throws IOException {
        var jsonContent = """
                {
                    "isbn": "1234567890",
                    "quantity": 2
                }
                """;
        Assertions.assertThat(orderRequestJacksonTester.parse(jsonContent))
                .usingRecursiveComparison()
                .isEqualTo(new OrderRequest("1234567890", 2));
    }
}
