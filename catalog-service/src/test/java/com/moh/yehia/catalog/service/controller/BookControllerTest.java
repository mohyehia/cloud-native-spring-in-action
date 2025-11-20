package com.moh.yehia.catalog.service.controller;

import com.moh.yehia.catalog.service.exception.BookNotFoundException;
import com.moh.yehia.catalog.service.model.Book;
import com.moh.yehia.catalog.service.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(BookController.class)
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void givenNotFoundBook_whenGetBookByIdSent_thenExceptionIsReturned() throws Exception {
        BDDMockito.given(bookService.findByIsbn(ArgumentMatchers.anyString()))
                .willThrow(BookNotFoundException.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/books/1234567890"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void givenBook_whenGetBookByIdSent_thenBookIsReturned() throws Exception {
        var book = new Book("1234567890", "Title", "Author", 9.90);
        BDDMockito.given(bookService.findByIsbn(ArgumentMatchers.anyString()))
                .willReturn(book);
        mockMvc.perform(MockMvcRequestBuilders.get("/books/1234567890"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}