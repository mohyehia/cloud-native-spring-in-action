package com.moh.yehia.order.service.model;

import java.time.Instant;

public record Book(
        Long id,
        String isbn,
        String title,
        String author,
        Double price,
        String publisher,
        Instant createdDate,
        Instant lastModifiedDate,
        int version) {
}
