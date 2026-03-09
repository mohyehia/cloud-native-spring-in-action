package com.moh.yehia.catalog.service.repository;

import com.moh.yehia.catalog.service.config.BasePostgresqlContainer;
import com.moh.yehia.catalog.service.config.DataConfig;
import com.moh.yehia.catalog.service.model.Book;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

@DataJdbcTest
@Import(DataConfig.class)
class BookRepositoryTest extends BasePostgresqlContainer {
    @Autowired
    private BookRepository bookRepository;

    @AfterEach
    void cleanUp() {
        bookRepository.deleteAll();
    }

    @Test
    @WithMockUser("test-user")
    void givenBook_whenSaveBook_thenReturnSavedBook() {
        var book = getDefaultBook();
        Book savedBook = bookRepository.save(book);
        Assertions.assertThat(savedBook)
                .isNotNull();
        Assertions.assertThat(savedBook.id()).isNotNull();
        Assertions.assertThat(savedBook.isbn()).isEqualTo(book.isbn());
        Assertions.assertThat(savedBook.title()).isEqualTo(book.title());
        Assertions.assertThat(savedBook.author()).isEqualTo(book.author());
        Assertions.assertThat(savedBook.price()).isEqualTo(book.price());
        Assertions.assertThat(savedBook.version()).isGreaterThan(0);
        Assertions.assertThat(savedBook.createdBy()).isNotNull().isEqualTo("test-user");
        Assertions.assertThat(savedBook.lastModifiedBy()).isNotNull().isEqualTo("test-user");
        Assertions.assertThat(savedBook.createdDate()).isNotNull();
        Assertions.assertThat(savedBook.lastModifiedDate()).isNotNull();
    }

    @Test
    void givenEmptyBooks_whenFindAll_thenReturnEmptyList() {
        var books = bookRepository.findAll();
        Assertions.assertThat(books).isNotNull().isEmpty();
    }

    @Test
    void givenUnauthenticatedUser_whenCreateBook_thenNoAuditMetadata() {
        var book = getDefaultBook();
        Book savedBook = bookRepository.save(book);
        Assertions.assertThat(savedBook)
                .isNotNull();
        Assertions.assertThat(savedBook.createdBy()).isNull();
        Assertions.assertThat(savedBook.lastModifiedBy()).isNull();
    }

    @Test
    void givenBooks_whenFindAll_thenReturnListOfBooks() {
        var book = getDefaultBook();
        bookRepository.save(book);
        var books = bookRepository.findAll();
        Assertions.assertThat(books).isNotNull().isNotEmpty().hasSize(1);
    }

    @Test
    void givenBook_whenFindByIsbn_thenReturnBook() {
        var book = getDefaultBook();
        bookRepository.save(book);
        var foundBook = bookRepository.findByIsbn(book.isbn());
        Assertions.assertThat(foundBook).isPresent();
        Assertions.assertThat(foundBook.get().isbn()).isEqualTo(book.isbn());
    }

    @Test
    void givenNotExistingBook_whenFindByIsbn_thenReturnEmptyOptional() {
        var notFoundBook = bookRepository.findByIsbn("1234567890");
        Assertions.assertThat(notFoundBook).isEmpty();
    }

    @Test
    void givenBook_whenExistsByIsbn_thenReturnBook() {
        var book = getDefaultBook();
        bookRepository.save(book);
        var exists = bookRepository.existsByIsbn(book.isbn());
        Assertions.assertThat(exists).isTrue();
    }

    @Test
    void givenNotExistingBook_whenExistsByIsbn_thenReturnFalse() {
        var exists = bookRepository.existsByIsbn("1234567890");
        Assertions.assertThat(exists).isFalse();
    }

    @Test
    void givenExistingBook_whenDeleteByIsbn_thenBookIsDeleted() {
        var book = getDefaultBook();
        Book savedBook = bookRepository.save(book);
        Assertions.assertThat(savedBook).isNotNull();

        bookRepository.deleteByIsbn(book.isbn());
        var exists = bookRepository.existsByIsbn(book.isbn());
        Assertions.assertThat(exists).isFalse();
    }

    private @NotNull Book getDefaultBook() {
        return new Book(null, "1234567890", "Title", "Author", 9.90, null, null, null, null, null, 0);
    }
}