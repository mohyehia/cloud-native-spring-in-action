package com.moh.yehia.order.service.config;

import com.moh.yehia.order.service.model.Book;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface CatalogServiceClient {

    @ConcurrencyLimit(10)
    @Retryable(excludes = {HttpClientErrorException.BadRequest.class, HttpServerErrorException.GatewayTimeout.class}, maxRetries = 3, multiplier = 2)
    @GetExchange("/{isbn}")
    Book getBookByIsbn(@PathVariable String isbn);
}
