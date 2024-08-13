package com.library.management.maids.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.library.management.maids.model.Book;
import com.library.management.maids.repository.BookRepository;
import com.library.management.maids.util.ErrorCode;
import com.library.management.maids.util.FileHelper;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "junit")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BooksControllerITest {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private static BookRepository bookRepository;

    public BooksControllerITest() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeAll
    private void setup() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Title #1");
        book.setAuthor("Author #1");
        book.setPublicationYear(2024);
        book.setQuantity(5);
        book.setIsbn("978-1-56619-909-4");

        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Title #2");
        book2.setAuthor("Author #2");
        book2.setPublicationYear(2024);
        book2.setQuantity(10);
        book2.setIsbn("978-1-56619-909-5");

        Book book3 = new Book();
        book3.setId(3L);
        book3.setTitle("Title #3");
        book3.setAuthor("Author #3");
        book3.setPublicationYear(2024);
        book3.setQuantity(10);
        book3.setIsbn("978-1-56619-909-6");

        bookRepository.saveAll(List.of(book, book2, book3));
    }

    @AfterAll
    private void teardown() {
        bookRepository.deleteAll();
    }


    @SuppressWarnings("unused")
    @ParameterizedTest(name = "Run {index} case: {0}")
    @MethodSource("booksValidData")
    void whenUseBooksAPI_thenCorrectResponse(String usecase, MockHttpServletRequestBuilder request, String expectedResponse) throws Exception {
        ResultActions result = mockMvc.perform(request
                .contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(content().json(expectedResponse));
    }

    private static Stream<Arguments> booksValidData() throws Exception {
        FileHelper fileHelper = new FileHelper();


        Book expectedResponse2 = fileHelper.readBookFromFile("/response/book/book_2.json");
        Book expectedResponse3 = fileHelper.readBookFromFile("/response/book/book_4.json");
        Book expectedResponse4 = fileHelper.readBookFromFile("/response/book/updated_book.json");

        List<Book> all = bookRepository.findAll();
        ObjectWriter ow = new ObjectMapper().writer();
        String json1 = ow.writeValueAsString(all);
        String json2 = ow.writeValueAsString(expectedResponse2);
        String json3 = ow.writeValueAsString(expectedResponse3);
        String json4 = ow.writeValueAsString(expectedResponse4);


        MockHttpServletRequestBuilder getAllBooksRequest = MockMvcRequestBuilders.get("/api/books");
        MockHttpServletRequestBuilder getBookTwoRequest = MockMvcRequestBuilders.get("/api/books/2");
        MockHttpServletRequestBuilder addBook = MockMvcRequestBuilders.post("/api/books").content(json3);
        MockHttpServletRequestBuilder updatedBook = MockMvcRequestBuilders.put("/api/books/2").content(json4);
        MockHttpServletRequestBuilder deleteBook = MockMvcRequestBuilders.delete("/api/books/4");

        return Stream.of(Arguments.of("Getting All Books", getAllBooksRequest, json1), Arguments.of("Getting Book #2", getBookTwoRequest, json2),
                Arguments.of("Adding Book #4", addBook, json3), Arguments.of("Updating Book #2 with Updated book", updatedBook, json4)
                , Arguments.of("Delete Book #4", deleteBook, json3));
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "Run {index} case: {0}")
    @MethodSource("booksWrongData")
    void whenUseAPIBooks_thenErrorResponse(String usecase, MockHttpServletRequestBuilder postRequest, String expectedResponse) throws Exception {
        ResultActions result = mockMvc.perform(postRequest
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", Is.is(ErrorCode.VALIDATION_ERROR))).andExpect(content().string(containsString(expectedResponse)));
    }

    private static Stream<Arguments> booksWrongData() throws Exception {
        FileHelper fileHelper = new FileHelper();

        Book badAuthorBook = fileHelper.readBookFromFile("/response/book/invalid/book_badAuthor.json");
        Book badISBNBook = fileHelper.readBookFromFile("/response/book/invalid/book_badISBN.json");
        Book badPubYearBook = fileHelper.readBookFromFile("/response/book/invalid/book_badPubYear.json");
        Book badTitleBook = fileHelper.readBookFromFile("/response/book/invalid/book_badTitle.json");


        ObjectWriter ow = new ObjectMapper().writer();
        String json1 = ow.writeValueAsString(badAuthorBook);
        String json2 = ow.writeValueAsString(badISBNBook);
        String json3 = ow.writeValueAsString(badPubYearBook);
        String json4 = ow.writeValueAsString(badTitleBook);

        String expectResponse1 = "Field error in object 'book' on field 'author': rejected value []; codes [NotBlank.book.author,NotBlank.author,NotBlank.java.lang.String,NotBlank]";
        String expectResponse2 = " [Field error in object 'book' on field 'isbn': rejected value [97]; codes [Pattern.book.isbn,Pattern.isbn,Pattern.java.lang.String,Pattern]";
        String expectResponse3 = "[Field error in object 'book' on field 'publicationYear': rejected value [200]; codes [Min.book.publicationYear,Min.publicationYear,Min.java.lang.Integer,Min]";
        String expectResponse4 = "[Field error in object 'book' on field 'title': rejected value []; codes [NotBlank.book.title,NotBlank.title,NotBlank.java.lang.String,NotBlank]";
        String expectResponse5 = "Field error in object 'book' on field 'author': rejected value []; codes [NotBlank.book.author,NotBlank.author,NotBlank.java.lang.String,NotBlank]";
        String expectResponse6 = " [Field error in object 'book' on field 'isbn': rejected value [97]; codes [Pattern.book.isbn,Pattern.isbn,Pattern.java.lang.String,Pattern]";
        String expectResponse7 = "[Field error in object 'book' on field 'publicationYear': rejected value [200]; codes [Min.book.publicationYear,Min.publicationYear,Min.java.lang.Integer,Min]";
        String expectResponse8 = "[Field error in object 'book' on field 'title': rejected value []; codes [NotBlank.book.title,NotBlank.title,NotBlank.java.lang.String,NotBlank]";


        MockHttpServletRequestBuilder addBook1 = MockMvcRequestBuilders.post("/api/books").content(json1);
        MockHttpServletRequestBuilder addBook2 = MockMvcRequestBuilders.post("/api/books").content(json2);
        MockHttpServletRequestBuilder addBook3 = MockMvcRequestBuilders.post("/api/books").content(json3);
        MockHttpServletRequestBuilder addBook4 = MockMvcRequestBuilders.post("/api/books").content(json4);
        MockHttpServletRequestBuilder updatedBook1 = MockMvcRequestBuilders.put("/api/books/2").content(json1);
        MockHttpServletRequestBuilder updatedBook2 = MockMvcRequestBuilders.put("/api/books/2").content(json2);
        MockHttpServletRequestBuilder updatedBook3 = MockMvcRequestBuilders.put("/api/books/2").content(json3);
        MockHttpServletRequestBuilder updatedBook4 = MockMvcRequestBuilders.put("/api/books/2").content(json4);

        return Stream.of(
                Arguments.of("Adding Book Bad Author", addBook1, expectResponse1),
                Arguments.of("Adding Book Bad ISBN", addBook2, expectResponse2),
                Arguments.of("Adding Book Bad Publication Year", addBook3, expectResponse3),
                Arguments.of("Adding Book Bad Title", addBook4, expectResponse4),
                Arguments.of("Updating Book with bad Author", updatedBook1, expectResponse5),
                Arguments.of("Updating Book with bad ISBN", updatedBook2, expectResponse6),
                Arguments.of("Updating Book with bad Publication Year", updatedBook3, expectResponse7),
                Arguments.of("Updating Book with bad Title", updatedBook4, expectResponse8));
    }

    @SuppressWarnings("unused")
    @Test
    void whenAddingTwoDuplicateBooks_ExceptionHappens() throws Exception {
        Book book = new Book();
        book.setId(5L);
        book.setTitle("Title #5");
        book.setAuthor("Author #5");
        book.setPublicationYear(2022);
        book.setQuantity(5);
        book.setIsbn("978-1-56619-909-4");

        ObjectWriter ow = new ObjectMapper().writer();
        String json1 = ow.writeValueAsString(book);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/books").content(json1)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk());

        ResultActions result2 = mockMvc.perform(MockMvcRequestBuilders.post("/api/books").content(json1)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isConflict());
    }

    @SuppressWarnings("unused")
    @Test
    void whenUnexpectedExceptionOccurWhileSaving_GeneralExceptionHappens() throws Exception {
        Book book = new Book();
        book.setId(6L);
        book.setTitle("Title #6");
        book.setAuthor("Author #6");
        book.setPublicationYear(2021);
        book.setQuantity(5);
        book.setIsbn("978-1-56619-909-4");

        ObjectWriter ow = new ObjectMapper().writer();
        String json1 = ow.writeValueAsString(book);

        Mockito.doThrow(new RuntimeException("Some Random Unexpected Exception")).when(bookRepository).save(Mockito.any(Book.class));

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/books").content(json1)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isInternalServerError());
    }

    @SuppressWarnings("unused")
    @Test
    void whenUnexpectedExceptionOccurWhileUpdating_thenGeneralExceptionHappens() throws Exception {
        ObjectWriter ow = new ObjectMapper().writer();

        Book book = new Book();
        book.setId(7L);
        book.setTitle("Title #7");
        book.setAuthor("Author #7");
        book.setPublicationYear(2021);
        book.setQuantity(5);
        book.setIsbn("978-1-56619-909-4");


        Optional<Book> optionalBook = bookRepository.findById(7L);
        Assertions.assertTrue(optionalBook.isEmpty());

        String json = ow.writeValueAsString(book);
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/books").content(json)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());

        Mockito.doThrow(new RuntimeException("Some Random Unexpected Exception")).when(bookRepository).save(Mockito.any(Book.class));


        Book updatedBook = new Book();
        updatedBook.setTitle("Title #7-Updated");
        updatedBook.setAuthor("Author #7-Updated");
        updatedBook.setPublicationYear(2021);
        updatedBook.setQuantity(5);
        updatedBook.setIsbn("978-1-56619-909-4");

        String json1 = ow.writeValueAsString(updatedBook);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/books/2").content(json1)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isInternalServerError());

    }

    @Test
    void whenUpdatingNonExistentBook_thenGeneralExceptionHappensAndRollBack() throws Exception {
        ObjectWriter ow = new ObjectMapper().writer();

        Book updatedBook = new Book();
        updatedBook.setTitle("Title #8-Updated");
        updatedBook.setAuthor("Author #8-Updated");
        updatedBook.setPublicationYear(2021);
        updatedBook.setQuantity(5);
        updatedBook.setIsbn("978-1-56619-909-4");

        String json1 = ow.writeValueAsString(updatedBook);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/books/100").content(json1)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());

    }

}
