package com.moh.yehia.catalog.service.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.moh.yehia.catalog.service.config.DataConfig;
import com.moh.yehia.catalog.service.config.PlatformPrerequisiteContainers;
import com.moh.yehia.catalog.service.model.Book;
import com.moh.yehia.catalog.service.repository.BookRepository;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

    private static KeycloakToken customerToken;
    private static KeycloakToken adminToken;

    @BeforeAll
    static void generateAccessTokens() {
        // configure RestTemplate for calling keycloak to generate access tokens for testing secured endpoints
        System.out.println("token endpoint => " + tokenEndpoint());
        RestTemplate restTemplate = new RestTemplate();
        customerToken = authenticateWithKeycloak("customer-user", restTemplate);
        adminToken = authenticateWithKeycloak("admin", restTemplate);
        System.out.println("Customer token: Authorization " + customerToken.accessToken);
        System.out.println("Admin token: Authorization " + adminToken.accessToken);
    }

    @AfterEach
    void cleanUp() {
        bookRepository.deleteAll();
    }

    @Test
    void givenListOfBooks_whenGetAllBooks_thenBooksAreReturned() throws Exception {
        var book = getDefaultBook();
        bookRepository.save(book);
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
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken.accessToken())
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
        Assertions.assertThat(returnedBookFromResponse.createdBy()).isNotNull();
        Assertions.assertThat(returnedBookFromResponse.lastModifiedBy()).isNotNull();
        Assertions.assertThat(returnedBookFromResponse.version()).isGreaterThan(book.version());
    }

    @Test
    void givenExistingBook_whenDeleteBook_thenBookIsDeleted() throws Exception {
        var book = getDefaultBook();
        Book savedBook = bookRepository.save(book);

        mockMvc.perform(MockMvcRequestBuilders.delete("/books/{isbn}", savedBook.isbn())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken.accessToken()))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        Assertions.assertThat(bookRepository.findById(savedBook.id())).isEmpty();
    }

    @Test
    void givenExistingBook_whenUpdateBook_thenUpdatedBookIsReturned() throws Exception {
        var book = getDefaultBook();
        Book savedBook = bookRepository.save(book);

        var updatedBook = new Book(savedBook.id(), savedBook.isbn(), "Title updated", "Author updated", 13.5, savedBook.publisher(), savedBook.createdDate(), savedBook.lastModifiedDate(), savedBook.createdBy(), savedBook.lastModifiedBy(), savedBook.version());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/books/{isbn}", savedBook.isbn())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken.accessToken())
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

    // testing the security of the endpoints by trying to access them with a customer token which should not have access to them
    @Test
    void givenCustomerToken_whenAddBook_thenForbidden() throws Exception {
        var book = getDefaultBook();

        mockMvc.perform(MockMvcRequestBuilders.post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken.accessToken())
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void givenCustomerToken_whenUpdateBook_thenForbidden() throws Exception {
        var book = new Book(1L, "1234567890", "Title", "Author", 9.90, null, null, null, null, null, 0);
        mockMvc.perform(MockMvcRequestBuilders.put("/books/{isbn}", book.isbn())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void givenCustomerToken_whenDeleteBook_thenForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/books/00123456789")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken.accessToken()))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void givenUnauthenticated_whenAddBook_thenUnauthorized() throws Exception {
        var book = getDefaultBook();

        mockMvc.perform(MockMvcRequestBuilders.post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void givenUnauthenticated_whenUpdateBook_thenUnauthorized() throws Exception {
        var book = new Book(1L, "1234567890", "Title", "Author", 9.90, null, null, null, null, null, 0);
        mockMvc.perform(MockMvcRequestBuilders.put("/books/{isbn}", book.isbn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void givenUnauthenticated_whenDeleteBook_thenUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/books/00123456789"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print());
    }

    private @NotNull Book getDefaultBook() {
        return new Book(null, "1234567890", "Title", "Author", 9.90, null, null, null, null, null, 0);
    }

    private static KeycloakToken authenticateWithKeycloak(String username, RestTemplate restTemplate) {
        // implement the logic for calling keycloak to generate access token for the given username and password
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(OAuth2ParameterNames.GRANT_TYPE, "password");
        formData.add(OAuth2ParameterNames.CLIENT_ID, "polar-test");
        formData.add(OAuth2Constants.USERNAME, username);
        formData.add(OAuth2Constants.PASSWORD, "password");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, httpHeaders);
        ResponseEntity<KeycloakToken> tokenResponseEntity = restTemplate.postForEntity(tokenEndpoint(), request, KeycloakToken.class);
        return tokenResponseEntity.getBody();
    }

    private record KeycloakToken(String accessToken) {
        @JsonCreator
        private KeycloakToken(@JsonProperty("access_token") final String accessToken) {
            this.accessToken = accessToken;
        }
    }

}
