package com.moh.yehia.catalog.service.model;

import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

@JsonTest
class BookJsonTest {
    @Autowired
    private JacksonTester<@NonNull Book> bookJacksonTester;

    @Test
    void givenBook_whenSerialize_thenCorrect() throws IOException {
        var book = new Book(1, "1234567890", "Title", "Author", 9.90, 1);
        var jsonContent = bookJacksonTester.write(book);
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("@.id").isEqualTo((int) book.id());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.isbn").isEqualTo(book.isbn());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.title").isEqualTo(book.title());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.author").isEqualTo(book.author());
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("@.price").isEqualTo(book.price());
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("@.version").isEqualTo(book.version());
    }

    @Test
    void givenJsonContent_whenDeserialize_thenCorrect() throws IOException {
        var jsonContent = """
                {   "id": 0,
                    "isbn": "1234567890",
                    "title": "Title",
                    "author": "Author",
                    "price": 9.90,
                    "version": 0
                }
                """;
        Assertions.assertThat(bookJacksonTester.parse(jsonContent))
                .usingRecursiveComparison()
                .isEqualTo(new Book(0, "1234567890", "Title", "Author", 9.90, 0));
    }
}
