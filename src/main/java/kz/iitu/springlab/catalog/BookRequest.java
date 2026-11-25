package kz.iitu.springlab.catalog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// The body of POST and PUT /api/books. Bean Validation turns a bad body into 400.
public record BookRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 120) String author,
        @Min(1450) @Max(2100) int year) {
}
