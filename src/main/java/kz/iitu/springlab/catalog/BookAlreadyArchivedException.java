package kz.iitu.springlab.catalog;

public class BookAlreadyArchivedException extends RuntimeException {
    public BookAlreadyArchivedException(long id) {
        super("Book " + id + " is already archived");
    }
}
