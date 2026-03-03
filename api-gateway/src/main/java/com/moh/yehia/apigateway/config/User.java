package com.moh.yehia.apigateway.config;

import java.util.List;

public record User(String username, String firstName, String lastName, List<String> roles) {
}
