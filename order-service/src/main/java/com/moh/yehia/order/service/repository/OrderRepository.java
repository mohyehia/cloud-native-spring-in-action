package com.moh.yehia.order.service.repository;

import com.moh.yehia.order.service.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
}
