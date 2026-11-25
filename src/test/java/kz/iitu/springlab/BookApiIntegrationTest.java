package kz.iitu.springlab;

import com.fasterxml.jackson.databind.JsonNode;
import kz.iitu.springlab.catalog.BookRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

// The whole application on a random port + a real PostgreSQL in Docker.
// Flyway runs V1 and V2 against an empty PostgreSQL; users come from the database (HTTP Basic).
// RestTestClient exists only in Spring Boot 4, so on Boot 3.4 we use TestRestTemplate.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BookApiIntegrationTest {

    @Container          // static -> one container for the whole class
    @ServiceConnection  // writes spring.datasource.url/username/password for us
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    TestRestTemplate rest;

    TestRestTemplate admin() { return rest.withBasicAuth("admin", "admin123"); }
    TestRestTemplate user()  { return rest.withBasicAuth("user", "user123"); }

    @Test
    void createsReadsArchivesAndSearchesABookOnRealPostgres() {
        // create (ADMIN)
        ResponseEntity<JsonNode> created = admin().postForEntity("/api/books",
                new BookRequest("Dune", "Frank Herbert", 1965), JsonNode.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long id = created.getBody().get("id").asLong();

        // read back (USER)
        ResponseEntity<JsonNode> read = user().getForEntity("/api/books/" + id, JsonNode.class);
        assertThat(read.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(read.getBody().get("title").asText()).isEqualTo("Dune");

        // archive, then archive again -> 409 (variant 1 branch, end to end)
        ResponseEntity<JsonNode> archived = admin().postForEntity("/api/books/" + id + "/archive", null, JsonNode.class);
        assertThat(archived.getBody().get("available").asBoolean()).isFalse();
        ResponseEntity<JsonNode> again = admin().postForEntity("/api/books/" + id + "/archive", null, JsonNode.class);
        assertThat(again.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // the derived query on PostgreSQL no longer finds the archived book
        ResponseEntity<JsonNode> search = user().getForEntity("/api/books/search?q=dune", JsonNode.class);
        assertThat(search.getBody().size()).isZero();
    }

    @Test
    void flywaySeedDataIsThereAndAnonymousRequestsAreRejected() {
        ResponseEntity<JsonNode> list = user().getForEntity("/api/books", JsonNode.class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody().findValuesAsText("title")).contains("Effective Java", "Clean Code");

        assertThat(rest.getForEntity("/api/books", String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}