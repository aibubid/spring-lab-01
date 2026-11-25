package kz.iitu.springlab.web;

import kz.iitu.springlab.catalog.BookAlreadyArchivedException;
import kz.iitu.springlab.catalog.BookNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// Turns exceptions into RFC 9457 problem details (application/problem+json).
// The parent class already answers an invalid @Valid body with a 400 problem detail.
@RestControllerAdvice(assignableTypes = BookRestController.class)
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    ProblemDetail notFound(BookNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Book not found");
        return problem;
    }

    @ExceptionHandler(BookAlreadyArchivedException.class)
    ProblemDetail alreadyArchived(BookAlreadyArchivedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Book already archived");
        return problem;
    }
}
