package com.moh.yehia.order.service.constant;

public class TestConstants {
    private TestConstants() {
    }

    public static final String EXISTING_BOOK = """
            {
                "id": 1,
                "isbn": "%s",
                "title": "Test Book",
                "author": "Test Author",
                "price": 19.99,
                "publisher": "Test Publisher",
                "createdDate": "2024-01-01T00:00:00Z",
                "lastModifiedDate": "2024-01-01T00:00:00Z",
                "version": 1
            }
            """;
}
