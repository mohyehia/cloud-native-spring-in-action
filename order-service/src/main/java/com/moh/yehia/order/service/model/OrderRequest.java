package com.moh.yehia.order.service.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OrderRequest(
        @NotBlank(message = "ISBN must not be blank")
        String isbn,
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 5, message = "Quantity must not exceed 5")
        int quantity
) {
}
