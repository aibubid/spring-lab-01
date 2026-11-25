package kz.iitu.springlab.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Variant 1: search by a fragment of the title, case-insensitive; archived books are not shown
    List<Book> findByTitleContainingIgnoreCaseAndAvailableTrueOrderByIdAsc(String fragment);
}
