package com.moh.yehia.order.service.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

class OrderRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.getValidator();
        }
    }

    @Test
    void whenAllFieldsCorrect_thenValidationSucceeds() {
        var orderRequest = new OrderRequest("1234567890", 2);
        Set<ConstraintViolation<OrderRequest>> constraintViolations = validator.validate(orderRequest);
        Assertions.assertThat(constraintViolations).isEmpty();
    }

    @Test
    void givenEmptyIsbn_whenValidate_thenValidationFails() {
        var orderRequest = new OrderRequest("", 2);
        Set<ConstraintViolation<OrderRequest>> constraintViolations = validator.validate(orderRequest);
        Assertions.assertThat(constraintViolations).isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(constraintViolations.iterator().next().getMessage())
                .isNotEmpty()
                .isEqualTo("ISBN must not be blank");
    }

    @Test
    void givenInvalidQuantity_whenValidate_thenValidationFails() {
        Set<ConstraintViolation<OrderRequest>> constraintViolations = validator.validate(new OrderRequest("1234567890", 0));
        Assertions.assertThat(constraintViolations).isNotEmpty()
                .hasSize(1);
        Assertions.assertThat(constraintViolations.iterator().next().getMessage())
                .isNotEmpty()
                .isEqualTo("Quantity must be at least 1");
    }

    @Test
    void givenInvalidOrderRequest_whenValidate_thenValidationFails() {
        Set<ConstraintViolation<OrderRequest>> constraintViolations = validator.validate(new OrderRequest("", 0));
        Assertions.assertThat(constraintViolations).isNotEmpty()
                .hasSize(2);
        constraintViolations.forEach(constraintViolation -> Assertions.assertThat(constraintViolation.getMessage()).isNotEmpty());
    }

}