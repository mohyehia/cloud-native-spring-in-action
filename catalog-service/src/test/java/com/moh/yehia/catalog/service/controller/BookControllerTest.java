package com.moh.yehia.catalog.service.controller;

import com.moh.yehia.catalog.service.exception.BookNotFoundException;
import com.moh.yehia.catalog.service.model.Book;
import com.moh.yehia.catalog.service.service.BookService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@WebMvcTest(BookController.class)
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @Test
    void givenListOfBooks_whenGetAllBooks_thenBooksAreReturned() throws Exception {
        // given
        var book = new Book(1L, "1234567890", "Title", "Author", 9.90, Instant.now(), null, 1);

        //when
        BDDMockito.given(bookService.findBooks())
                .willReturn(List.of(book));

        // then
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/books"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();

        List<Book> books = objectMapper.readValue(contentAsString, objectMapper.getTypeFactory().constructCollectionType(List.class, Book.class));
        Assertions.assertThat(books)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);
        Assertions.assertThat(books.getFirst())
                .isNotNull()
                .usingRecursiveComparison().isEqualTo(book);
    }

    @Test
    void givenValidBook_whenAddBook_thenBookIsCreated() throws Exception {
        // given
        var book = new Book(null, "1234567890", "Title", "Author", 9.90, Instant.now(), null, 1);
        var savedBook = new Book(1L, "1234567890", "Title", "Author", 9.90, Instant.now(), Instant.now(), 1);

        // when
        BDDMockito.given(bookService.addBook(ArgumentMatchers.any(Book.class))).willReturn(savedBook);

        // then
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Book returnedBookFromResponse = objectMapper.readValue(contentAsString, Book.class);
        Assertions.assertThat(returnedBookFromResponse)
                .isNotNull()
                .isEqualTo(savedBook);
    }

    @Test
    void givenIsbn_whenDeleteBook_thenNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/books/1234567890"))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    void givenBook_whenUpdateBook_thenUpdatedBookIsReturned() throws Exception {
        // given
        var book = new Book(1L, "1234567890", "Title", "Author", 9.90, Instant.now(), Instant.now(), 1);

        // when
        BDDMockito.given(bookService.updateBook(ArgumentMatchers.eq("1234567890"), ArgumentMatchers.any(Book.class))).willReturn(book);

        // then
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/books/1234567890")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Book updatedBook = objectMapper.readValue(contentAsString, Book.class);
        Assertions.assertThat(updatedBook)
                .isNotNull()
                .isEqualTo(book);
    }

    @Test
    void givenBook_whenGetBookByIsbn_thenBookIsReturned() throws Exception {
        // given
        var book = new Book(1L, "1234567890", "Title", "Author", 9.90, Instant.now(), Instant.now(), 1);

        // when
        BDDMockito.given(bookService.findByIsbn(ArgumentMatchers.anyString()))
                .willReturn(book);

        // then
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/books/1234567890"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Book retrievedBook = objectMapper.readValue(contentAsString, Book.class);
        Assertions.assertThat(retrievedBook)
                .isNotNull()
                .isEqualTo(book);
    }

    @Test
    void givenNotFoundBook_whenGetBookByIsbn_thenExceptionIsReturned() throws Exception {
        // when
        BDDMockito.given(bookService.findByIsbn(ArgumentMatchers.anyString()))
                .willThrow(BookNotFoundException.class);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/books/1234567890"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}