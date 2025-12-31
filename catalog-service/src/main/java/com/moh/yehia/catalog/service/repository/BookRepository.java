package com.moh.yehia.catalog.service.repository;

import com.moh.yehia.catalog.service.model.Book;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends CrudRepository<@NonNull Book, @NonNull Long> {
    @NullMarked
    List<Book> findAll();

    Optional<Book> findByIsbn(String isbn);

    @Modifying
    @Transactional
    @Query("delete from book where isbn = :isbn")
    void deleteByIsbn(@Param("isbn") String isbn);

    boolean existsByIsbn(String isbn);
}
