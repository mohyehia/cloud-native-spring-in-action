package com.moh.yehia.catalog.service.controller;

import com.moh.yehia.catalog.service.config.DataConfig;
import com.moh.yehia.catalog.service.config.PlatformPrerequisiteContainers;
import com.moh.yehia.catalog.service.model.Book;
import com.moh.yehia.catalog.service.repository.BookRepository;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(DataConfig.class)
class BookControllerIntegrationTest extends PlatformPrerequisiteContainers {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

//    @BeforeAll
//    static void generateAccessTokens() {
//        // configure resttemplate for calling keycloak to generate access tokens for testing secured endpoints
//        RestTemplate restTemplate = new RestTemplate();
//        String tokenEndpoint = "http://localhost:8080/realms/PolarBookshop/protocol/openid-connect/token";
////        String accessToken = restTemplate.postForObject(tokenEndpoint, "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret, KeycloakTokenResponse.class).getAccessToken();
//    }

    @AfterEach
    void cleanUp() {
        bookRepository.deleteAll();
    }

    @Test
    void givenListOfBooks_whenGetAllBooks_thenBooksAreReturned() throws Exception {
        var book = getDefaultBook();
        Book savedBook = bookRepository.save(book);
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
                .isNotNull();
        var retrievedBook = books.getFirst();
        Assertions.assertThat(retrievedBook.id()).isNotNull();
        Assertions.assertThat(retrievedBook.isbn()).isNotNull();
        Assertions.assertThat(retrievedBook.createdDate()).isNotNull();
        Assertions.assertThat(retrievedBook.lastModifiedDate()).isNotNull();
        Assertions.assertThat(retrievedBook.version()).isGreaterThan(book.version());
    }

    @Test
    void givenValidBook_whenAddBook_thenBookIsCreated() throws Exception {
        var book = getDefaultBook();

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Book returnedBookFromResponse = objectMapper.readValue(contentAsString, Book.class);
        Assertions.assertThat(returnedBookFromResponse)
                .isNotNull();

        Assertions.assertThat(returnedBookFromResponse.id()).isNotNull();
        Assertions.assertThat(returnedBookFromResponse.isbn()).isNotNull();
        Assertions.assertThat(returnedBookFromResponse.createdDate()).isNotNull();
        Assertions.assertThat(returnedBookFromResponse.lastModifiedDate()).isNotNull();
        Assertions.assertThat(returnedBookFromResponse.version()).isGreaterThan(book.version());
    }

    @Test
    void givenExistingBook_whenDeleteBook_thenBookIsDeleted() throws Exception {
        var book = getDefaultBook();
        Book savedBook = bookRepository.save(book);

        mockMvc.perform(MockMvcRequestBuilders.delete("/books/{isbn}", savedBook.isbn()))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        Assertions.assertThat(bookRepository.findById(savedBook.id())).isEmpty();
    }

    @Test
    void givenExistingBook_whenUpdateBook_thenUpdatedBookIsReturned() throws Exception {
        var book = getDefaultBook();
        Book savedBook = bookRepository.save(book);

        var updatedBook = new Book(savedBook.id(), savedBook.isbn(), "Title updated", "Author updated", 13.5, savedBook.publisher(), savedBook.createdDate(), savedBook.lastModifiedDate(), savedBook.version());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/books/{isbn}", savedBook.isbn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Book retrievedBook = objectMapper.readValue(contentAsString, Book.class);
        Assertions.assertThat(retrievedBook)
                .isNotNull();
        Assertions.assertThat(retrievedBook.id()).isNotNull();
        Assertions.assertThat(retrievedBook.isbn()).isNotNull();
        Assertions.assertThat(retrievedBook.title()).isNotNull().isEqualTo(updatedBook.title());
        Assertions.assertThat(retrievedBook.author()).isNotNull().isEqualTo(updatedBook.author());
        Assertions.assertThat(retrievedBook.price()).isNotNull().isEqualTo(updatedBook.price());
        Assertions.assertThat(retrievedBook.lastModifiedDate()).isAfter(savedBook.lastModifiedDate());
        Assertions.assertThat(retrievedBook.version()).isGreaterThan(savedBook.version());
    }

    @Test
    void givenExistingBook_whenGetByIsbn_thenBookIsReturned() throws Exception {
        var book = getDefaultBook();
        Book savedBook = bookRepository.save(book);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/books/{isbn}", book.isbn()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Book retrievedBook = objectMapper.readValue(contentAsString, Book.class);

        Assertions.assertThat(retrievedBook)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", savedBook.id())
                .hasFieldOrPropertyWithValue("isbn", savedBook.isbn())
                .hasFieldOrPropertyWithValue("title", savedBook.title())
                .hasFieldOrPropertyWithValue("author", savedBook.author())
                .hasFieldOrPropertyWithValue("price", savedBook.price())
                .hasFieldOrPropertyWithValue("version", savedBook.version());
    }

    @Test
    void givenNotFoundBook_whenGetBookByIsbn_thenExceptionIsReturned() throws Exception {
        String isbn = "1234567890";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/books/{isbn}", isbn))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Assertions.assertThat(contentAsString)
                .isNotNull()
                .isEqualTo("The book with ISBN " + isbn + " was not found.");
    }

    private @NotNull Book getDefaultBook() {
        return new Book(null, "1234567890", "Title", "Author", 9.90, null, null, null, 0);
    }


}
