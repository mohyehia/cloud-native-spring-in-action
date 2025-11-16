package com.moh.yehia.catalog.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record Book(
        @NotBlank(message = "The ISBN is required")
        @Pattern(regexp = "^(\\d{10}|\\d{13})$", message = "The ISBN must be 10 or 13 digits long")
        String isbn,
        @NotBlank(message = "The book title is required") String title,
        @NotBlank(message = "The book author is required") String author,
        @NotNull(message = "The book price is required")
        @Positive(message = "The book price must be greater than zero") Double price) {
}
