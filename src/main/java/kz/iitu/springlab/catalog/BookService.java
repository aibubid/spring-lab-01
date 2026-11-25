package kz.iitu.springlab.catalog;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    // author == null -> all books; otherwise filter by author (case-insensitive, part of the name)
    public List<Book> findAll(String author) {
        return repository.findAll().stream()
                .filter(b -> author == null || author.isBlank()
                        || b.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .sorted(Comparator.comparing(Book::getId))
                .toList();
    }

    public Book byId(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    public Book create(BookRequest request) {
        return repository.save(new Book(request.title(), request.author(), request.year()));
    }

    @Transactional
    public Book replace(long id, BookRequest request) {
        Book book = byId(id);
        book.update(request.title(), request.author(), request.year());
        return repository.save(book);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        repository.deleteById(id);
    }

    // Lab 12, variant 1: archiving. Archiving an already archived book is an error, not a silent save.
    @Transactional
    public Book archive(long id) {
        Book book = byId(id);
        if (!book.isAvailable()) {
            throw new BookAlreadyArchivedException(id);
        }
        book.archive();
        return repository.save(book);
    }

    // Variant 1: search by a fragment of the title, case-insensitive
    public List<Book> searchByTitle(String q) {
        return repository.findByTitleContainingIgnoreCaseAndAvailableTrueOrderByIdAsc(q);
    }
}
