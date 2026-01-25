package com.moh.yehia.order.service.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
public record Order(
        @Id
        String id, String bookIsbn, String bookName, Double bookPrice, Integer quantity, OrderStatus status,
        @CreatedDate Instant createdDate,
        @LastModifiedDate Instant lastModifiedDate,
        @Version int version) {
}
