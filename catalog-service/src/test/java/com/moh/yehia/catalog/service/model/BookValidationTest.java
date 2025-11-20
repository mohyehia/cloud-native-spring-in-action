package com.moh.yehia.catalog.service.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

class BookValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.getValidator();
        }
    }

    @Test
    void whenAllFieldsCorrect_thenValidationSucceeds() {
        var book = new Book("1234567890", "Title", "Author", 9.90);
        Set<ConstraintViolation<Book>> constraintViolations = validator.validate(book);
        Assertions.assertThat(constraintViolations).isEmpty();
    }

    @Test
    void whenFieldsAreMissing_thenValidationFails() {
        var book = new Book(null, "", "", 0.0);
        Set<ConstraintViolation<Book>> constraintViolations = validator.validate(book);
        Assertions.assertThat(constraintViolations).isNotEmpty()
                .hasSize(4);
        constraintViolations.forEach(constraintViolation -> {
            Assertions.assertThat(constraintViolation.getMessage()).isNotEmpty();
        });
    }

}