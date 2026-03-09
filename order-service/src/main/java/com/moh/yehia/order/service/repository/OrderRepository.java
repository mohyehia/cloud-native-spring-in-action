package com.moh.yehia.order.service.repository;

import com.moh.yehia.order.service.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findAllByCreatedBy(String userId);
}
