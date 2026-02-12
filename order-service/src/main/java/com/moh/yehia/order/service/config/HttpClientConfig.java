package com.moh.yehia.order.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

import java.time.Duration;

@Configuration
@ImportHttpServices(group = "catalog-service", types = CatalogServiceClient.class)
public class HttpClientConfig {

    @Value("${catalog-service.connection-timeout}")
    private int connectionTimeout;

    @Value("${catalog-service.read-timeout}")
    private int readTimeout;

    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurer(@Value("${catalog-service.url}") String baseUrl) {
        // This bean activates the property binding for the named groups
        return groups -> groups.filterByName("catalog-service")
                .forEachClient((group, clientBuilder) -> {
                    clientBuilder.baseUrl(baseUrl);
                    SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
                    simpleClientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(connectionTimeout));
                    simpleClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(readTimeout));
                    clientBuilder.requestFactory(simpleClientHttpRequestFactory);
                });
    }
}