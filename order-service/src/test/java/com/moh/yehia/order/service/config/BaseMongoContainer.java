package com.moh.yehia.order.service.config;

import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

public abstract class BaseMongoContainer {
    static final MongoDBContainer MONGO_DB_CONTAINER;

    static {
        MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:4.4.29-focal"))
                .withReplicaSet();
        MONGO_DB_CONTAINER.setPortBindings(List.of("27017:27017")); // to force test container to use the same port
        MONGO_DB_CONTAINER.start();
    }
}
