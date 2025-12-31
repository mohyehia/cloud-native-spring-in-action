package com.moh.yehia.catalog.service.model;

import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.time.Instant;

@JsonTest
class BookJsonTest {
    @Autowired
    private JacksonTester<@NonNull Book> bookJacksonTester;

    @Test
    void givenBook_whenSerialize_thenCorrect() throws IOException {
        var book = new Book(null, "1234567890", "Title", "Author", 9.90, Instant.now(), Instant.now(), 1);
        var jsonContent = bookJacksonTester.write(book);
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("@.id").isEqualTo(book.id());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.isbn").isEqualTo(book.isbn());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.title").isEqualTo(book.title());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.author").isEqualTo(book.author());
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("@.price").isEqualTo(book.price());
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("@.version").isEqualTo(book.version());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.createdDate").isEqualTo(book.createdDate().toString());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.lastModifiedDate").isEqualTo(book.lastModifiedDate().toString());
    }

    @Test
    void givenJsonContent_whenDeserialize_thenCorrect() throws IOException {
        var jsonContent = """
                {   "id": null,
                    "isbn": "1234567890",
                    "title": "Title",
                    "author": "Author",
                    "price": 9.90,
                    "version": 0
                }
                """;
        Assertions.assertThat(bookJacksonTester.parse(jsonContent))
                .usingRecursiveComparison()
                .isEqualTo(new Book(null, "1234567890", "Title", "Author", 9.90, null, null, 0));
    }
}
