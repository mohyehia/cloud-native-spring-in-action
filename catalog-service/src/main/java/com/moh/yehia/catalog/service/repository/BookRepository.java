package com.moh.yehia.catalog.service.repository;

import com.moh.yehia.catalog.service.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {
    List<Book> findAll();

    Optional<Book> findByIsbn(String isbn);

    Book save(Book book);

    void deleteByIsbn(String isbn);

    boolean existsByIsbn(String isbn);
}
