package com.moh.yehia.catalog.service.model;

import com.moh.yehia.catalog.service.config.ObjectMapperConfig;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.time.Instant;

@JsonTest
@Import(ObjectMapperConfig.class)
class BookJsonTest {
    @Autowired
    private JacksonTester<@NonNull Book> bookJacksonTester;

    @Test
    void givenBook_whenSerialize_thenCorrect() throws IOException {
        var book = new Book(null, "1234567890", "Title", "Author", 9.90, "publisher", Instant.now(), Instant.now(), "test-user", "test-updated-user", 1);
        var jsonContent = bookJacksonTester.write(book);
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("@.id").isEqualTo(book.id());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.isbn").isEqualTo(book.isbn());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.title").isEqualTo(book.title());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.author").isEqualTo(book.author());
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("@.price").isEqualTo(book.price());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.publisher").isEqualTo(book.publisher());
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("@.version").isEqualTo(book.version());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.createdBy").isEqualTo(book.createdBy());
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("@.lastModifiedBy").isEqualTo(book.lastModifiedBy());
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
                    "publisher": "publisher",
                    "price": 9.90,
                    "createdBy": "creator",
                    "lastModifiedBy": "modifier"
                }
                """;
        Assertions.assertThat(bookJacksonTester.parse(jsonContent))
                .usingRecursiveComparison()
                .isEqualTo(new Book(null, "1234567890", "Title", "Author", 9.90, "publisher", null, null, "creator", "modifier", 0));
    }
}
