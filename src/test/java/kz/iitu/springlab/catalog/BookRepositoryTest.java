package kz.iitu.springlab.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

// Only the JPA slice: entities, repositories, Flyway and an embedded H2 database (in memory).
// Every test runs in a transaction that is rolled back at the end.
@DataJpaTest
class BookRepositoryTest {

    @Autowired
    BookRepository repository;

    @Autowired
    TestEntityManager em;

    // Positive case: finds the right rows, ignoring case, ordered by id
    @Test
    void findsAvailableBooksWhoseTitleContainsTheFragmentIgnoringCase() {
        em.persist(new Book("Dune", "Frank Herbert", 1965));
        em.persist(new Book("Dune Messiah", "Frank Herbert", 1969));
        em.flush();
        em.clear();   // force the query to hit the database

        var found = repository.findByTitleContainingIgnoreCaseAndAvailableTrueOrderByIdAsc("dUnE");

        assertThat(found).extracting(Book::getTitle)
                .containsExactly("Dune", "Dune Messiah");
    }

    // Negative case: an archived book and a book with another title must NOT be returned
    @Test
    void archivedBooksAndOtherTitlesAreExcluded() {
        em.persist(new Book("Dune", "Frank Herbert", 1965));
        Book children = em.persist(new Book("Children of Dune", "Frank Herbert", 1976));
        em.persist(new Book("Neuromancer", "William Gibson", 1984));

        children.archive();   // a change to a managed entity: an UPDATE waiting to be flushed
        em.flush();
        em.clear();

        var found = repository.findByTitleContainingIgnoreCaseAndAvailableTrueOrderByIdAsc("dune");

        assertThat(found).extracting(Book::getTitle)
                .containsExactly("Dune");
    }
}