package com.moh.yehia.catalog.service.service;

import com.moh.yehia.catalog.service.config.BasePostgresqlContainer;
import com.moh.yehia.catalog.service.config.DataConfig;
import com.moh.yehia.catalog.service.exception.BookAlreadyExistsException;
import com.moh.yehia.catalog.service.exception.BookNotFoundException;
import com.moh.yehia.catalog.service.model.Book;
import com.moh.yehia.catalog.service.repository.BookRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(DataConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookServiceTest extends BasePostgresqlContainer {
    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @AfterEach
    void cleanUp() {
        bookRepository.deleteAll();
    }

    @Test
    void givenEmptyListOfBooks_whenFindBooks_thenReturnEmptyList() {
        var books = bookService.findBooks();
        Assertions.assertThat(books)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void givenListOfBooks_whenFindBooks_thenBooksAreReturned() {
        var book = new Book(null, "1234567890", "Title", "Author", 9.90, null, null, 0);
        bookRepository.save(book);

        var books = bookService.findBooks();
        Assertions.assertThat(books)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);
    }

    @Test
    void givenNonExistingBook_whenFindByIsbn_thenBookIsNotFound() {
        String isbn = "1234567890";
        Assertions.assertThatThrownBy(() -> bookService.findByIsbn(isbn))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("The book with ISBN " + isbn + " was not found.");
    }

    @Test
    void givenExistingBook_whenFindByIsbn_thenBookIsReturned() {
        var book = new Book(null, "1234567890", "Title", "Author", 9.90, null, null, 0);
        Book savedBook = bookRepository.save(book);

        Book retrievedBook = bookService.findByIsbn(book.isbn());

        Assertions.assertThat(retrievedBook)
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("id", savedBook.id())
                .hasFieldOrPropertyWithValue("isbn", savedBook.isbn())
                .hasFieldOrPropertyWithValue("title", savedBook.title())
                .hasFieldOrPropertyWithValue("author", savedBook.author())
                .hasFieldOrPropertyWithValue("price", savedBook.price())
                .hasFieldOrPropertyWithValue("version", savedBook.version());
    }

    @Test
    void givenExistingBook_whenAddBook_thenBookAlreadyExistsExceptionIsThrown() {
        var book = new Book(null, "1234567890", "Title", "Author", 9.90, null, null, 0);
        bookService.addBook(book);

        Assertions.assertThatThrownBy(() -> bookService.addBook(book))
                .isInstanceOf(BookAlreadyExistsException.class)
                .hasMessage("A book with ISBN " + book.isbn() + " already exists.");
    }

    @Test
    void givenNewBook_whenAddBook_thenBookIsCreated() {
        var book = new Book(null, "1234567890", "Title", "Author", 9.90, null, null, 0);
        Book savedBook = bookService.addBook(book);
        Assertions.assertThat(savedBook)
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("isbn", book.isbn())
                .hasFieldOrPropertyWithValue("title", book.title())
                .hasFieldOrPropertyWithValue("author", book.author())
                .hasFieldOrPropertyWithValue("price", book.price());

        Assertions.assertThat(savedBook.createdDate()).isNotNull();
        Assertions.assertThat(savedBook.lastModifiedDate()).isNotNull();
        Assertions.assertThat(savedBook.version()).isGreaterThan(book.version());
    }

    @Test
    void givenExistingBook_whenDeleteBook_thenBookIsDeleted() {
        var book = new Book(null, "1234567890", "Title", "Author", 9.90, null, null, 0);
        bookService.addBook(book);

        bookService.deleteBook(book.isbn());

        Assertions.assertThat(bookRepository.findByIsbn(book.isbn())).isEmpty();
    }

    @Test
    void givenNonExistingBook_whenUpdateBook_thenNewBookIsAdded() {
        var book = new Book(null, "1234567890", "Title", "Author", 9.90, null, null, 0);
        Book savedBook = bookService.updateBook(book.isbn(), book);
        Assertions.assertThat(savedBook)
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("isbn", book.isbn())
                .hasFieldOrPropertyWithValue("title", book.title())
                .hasFieldOrPropertyWithValue("author", book.author())
                .hasFieldOrPropertyWithValue("price", book.price());

        Assertions.assertThat(savedBook.createdDate()).isNotNull();
        Assertions.assertThat(savedBook.lastModifiedDate()).isNotNull();
        Assertions.assertThat(savedBook.version()).isGreaterThan(book.version());
    }

    @Test
    void givenExistingBook_whenUpdateBook_thenBookIsUpdated() {
        var book = new Book(null, "1234567890", "Title", "Author", 9.90, null, null, 0);
        Book savedBook = bookService.addBook(book);

        var updatedBook = new Book(savedBook.id(), savedBook.isbn(), "Title updated", "Author updated", 13.5, savedBook.createdDate(), savedBook.lastModifiedDate(), savedBook.version());
        Book retrievedBook = bookService.updateBook(book.isbn(), updatedBook);

        Assertions.assertThat(retrievedBook)
                .isNotNull()
                .hasFieldOrPropertyWithValue("isbn", updatedBook.isbn())
                .hasFieldOrPropertyWithValue("title", updatedBook.title())
                .hasFieldOrPropertyWithValue("author", updatedBook.author())
                .hasFieldOrPropertyWithValue("price", updatedBook.price());

        Assertions.assertThat(retrievedBook.lastModifiedDate()).isAfter(savedBook.lastModifiedDate());
        Assertions.assertThat(retrievedBook.version()).isGreaterThan(savedBook.version());
    }
}