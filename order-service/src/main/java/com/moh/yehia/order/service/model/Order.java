package com.moh.yehia.order.service.model;

import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
public record Order(
        @Id
        String id, String bookIsbn, String bookName, Double bookPrice, Integer quantity, OrderStatus status,
        @CreatedDate Instant createdDate,
        @LastModifiedDate Instant lastModifiedDate,
        @CreatedBy String createdBy,
        @LastModifiedBy String lastModifiedBy,
        @Version int version) {
}
