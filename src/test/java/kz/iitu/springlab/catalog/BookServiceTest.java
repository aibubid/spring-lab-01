package kz.iitu.springlab.catalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// No Spring context: the repository is a Mockito mock, the service is a plain object.
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepository repository;

    @InjectMocks
    BookService service;

    // 1. Happy path
    @Test
    void returnsTheBookWhenItExists() {
        given(repository.findById(1L))
                .willReturn(Optional.of(new Book(1L, "Dune", "Frank Herbert", 1965, true)));

        Book found = service.byId(1L);

        assertThat(found.getTitle()).isEqualTo("Dune");
        assertThat(found.isAvailable()).isTrue();
    }

    // 2. Not-found path
    @Test
    void missingBookThrowsBookNotFound() {
        given(repository.findById(7L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.byId(7L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book 7 not found");
    }

    // 3. Interaction check with an ArgumentCaptor
    @Test
    void archivingABookStoresItAsUnavailable() {
        given(repository.findById(1L))
                .willReturn(Optional.of(new Book(1L, "Dune", "Frank Herbert", 1965, true)));

        service.archive(1L);

        var saved = ArgumentCaptor.forClass(Book.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(1L);
        assertThat(saved.getValue().isAvailable()).isFalse();
    }

    // 4. Variant 1: the branch that is easy to forget
    @Test
    void archivingAnAlreadyArchivedBookIsRejectedAndNothingIsSaved() {
        given(repository.findById(2L))
                .willReturn(Optional.of(new Book(2L, "Old Atlas", "Unknown", 1990, false)));

        assertThatThrownBy(() -> service.archive(2L))
                .isInstanceOf(BookAlreadyArchivedException.class)
                .hasMessage("Book 2 is already archived");
        verify(repository, never()).save(any());
    }

    // Variant 1 extra requirement: a parameterized test with at least three cases
    @ParameterizedTest(name = "author filter \"{0}\" finds \"{1}\"")
    @CsvSource({
            "bloch,  Effective Java",
            "BLOCH,  Effective Java",
            "martin, Clean Code",
            "Walls,  Spring in Action"
    })
    void filtersByAPartOfTheAuthorIgnoringCase(String author, String expectedTitle) {
        given(repository.findAll()).willReturn(List.of(
                new Book(1L, "Effective Java", "Joshua Bloch", 2018, true),
                new Book(2L, "Clean Code", "Robert Martin", 2008, true),
                new Book(3L, "Spring in Action", "Craig Walls", 2022, true)));

        assertThat(service.findAll(author))
                .extracting(Book::getTitle)
                .containsExactly(expectedTitle);
    }
}