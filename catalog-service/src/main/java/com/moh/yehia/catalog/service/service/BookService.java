package com.moh.yehia.catalog.service.service;

import com.moh.yehia.catalog.service.exception.BookAlreadyExistsException;
import com.moh.yehia.catalog.service.exception.BookNotFoundException;
import com.moh.yehia.catalog.service.model.Book;
import com.moh.yehia.catalog.service.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findBooks() {
        return bookRepository.findAll();
    }

    public Book findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn).orElseThrow(() -> new BookNotFoundException(isbn));
    }

    public Book addBook(Book book) {
        if (bookRepository.existsByIsbn(book.isbn())) {
            throw new BookAlreadyExistsException(book.isbn());
        }
        return bookRepository.save(book);
    }

    public void deleteBook(String isbn) {
        bookRepository.deleteByIsbn(isbn);
    }

    public Book updateBook(String isbn, Book book) {
        return bookRepository.findByIsbn(isbn)
                .map(existingBook -> {
                    var bookToUpdate = new Book(existingBook.isbn(), book.title(), book.author(), book.price());
                    return bookRepository.save(bookToUpdate);
                })
                .orElseGet(() -> addBook(book));
    }
}
