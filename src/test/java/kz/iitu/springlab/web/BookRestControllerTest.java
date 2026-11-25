package kz.iitu.springlab.web;

import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookNotFoundException;
import kz.iitu.springlab.catalog.BookService;
import kz.iitu.springlab.config.ApiSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Only the web layer: controller + ApiExceptionHandler + Jackson + the real API security chain.
// The service is replaced by a Mockito mock (@MockitoBean, not the old @MockBean).
@WebMvcTest(BookRestController.class)
@Import(ApiSecurityConfiguration.class)
class BookRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService service;

    // 1. 200 + the JSON fields the contract promises
    @Test
    @WithMockUser   // role USER is enough to read
    void existingBookIsReturnedAsJson() throws Exception {
        given(service.byId(1L)).willReturn(new Book(1L, "Dune", "Frank Herbert", 1965, true));

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune"))
                .andExpect(jsonPath("$.available").value(true));
    }

    // 2. 404 + problem detail from ApiExceptionHandler
    @Test
    @WithMockUser
    void missingBookBecomes404WithProblemDetail() throws Exception {
        given(service.byId(7L)).willThrow(new BookNotFoundException(7L));

        mockMvc.perform(get("/api/books/7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Book not found"))
                .andExpect(jsonPath("$.detail").value("Book 7 not found"));
    }

    // 3. 400: Bean Validation on the request body is really wired
    @Test
    @WithMockUser(roles = "ADMIN")   // only ADMIN may POST
    void invalidBodyIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "author": "Frank Herbert", "year": 3000}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(service, never()).create(any());   // the service was never reached
    }

    // Extra: the security chain is active in this slice, it was not switched off
    @Test
    void requestWithoutCredentialsIsRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isUnauthorized());
    }
}