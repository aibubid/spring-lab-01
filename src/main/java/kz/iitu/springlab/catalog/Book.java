package kz.iitu.springlab.catalog;

import jakarta.persistence.*;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(name = "published_year", nullable = false) // YEAR is a reserved word in H2
    private int year;

    @Column(nullable = false)
    private boolean available = true;   // false = the book is archived

    protected Book() { // for JPA
    }

    public Book(String title, String author, int year) {
        this.title = title;
        this.author = author;
        this.year = year;
    }

    // for unit tests, where there is no database to assign the id
    public Book(Long id, String title, String author, int year, boolean available) {
        this(title, author, year);
        this.id = id;
        this.available = available;
    }

    public void update(String title, String author, int year) {
        this.title = title;
        this.author = author;
        this.year = year;
    }

    public void archive() {
        this.available = false;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public boolean isAvailable() { return available; }

    @Override
    public String toString() {
        return "Book{id=" + id + ", title=" + title + ", available=" + available + "}";
    }
}
