package kz.iitu.springlab.web;

import jakarta.validation.Valid;
import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookRequest;
import kz.iitu.springlab.catalog.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController                 // = @Controller + @ResponseBody on every method
@RequestMapping("/api/books")
public class BookRestController {

    private final BookService service;

    public BookRestController(BookService service) {
        this.service = service;
    }

    // GET /api/books?author=...&limit=...  -> 200
    @GetMapping
    public List<Book> list(@RequestParam(required = false) String author,
                           @RequestParam(defaultValue = "10") int limit) {
        return service.findAll(author).stream().limit(limit).toList();
    }

    // Variant 1: GET /api/books/search?q=... -> always 200 (empty array if nothing found)
    @GetMapping("/search")
    public List<Book> search(@RequestParam String q) {
        return service.searchByTitle(q);
    }

    // GET /api/books/{id} -> 200, or 404 with a problem detail (ApiExceptionHandler)
    @GetMapping("/{id}")
    public Book find(@PathVariable long id) {
        return service.byId(id);
    }

    // POST /api/books -> 201 Created + Location, or 400 if the body is invalid
    @PostMapping
    public ResponseEntity<Book> create(@Valid @RequestBody BookRequest request) {
        Book saved = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/books/" + saved.getId()))
                .body(saved);
    }

    // PUT /api/books/{id} -> 200, 400 or 404
    @PutMapping("/{id}")
    public Book replace(@PathVariable long id, @Valid @RequestBody BookRequest request) {
        return service.replace(id, request);
    }

    // DELETE /api/books/{id} -> 204 or 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/books/{id}/archive -> 200, 404, or 409 if already archived
    @PostMapping("/{id}/archive")
    public Book archive(@PathVariable long id) {
        return service.archive(id);
    }
}
