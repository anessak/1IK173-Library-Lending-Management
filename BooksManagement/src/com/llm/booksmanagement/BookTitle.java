package com.llm.booksmanagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

public class BookTitle {
    private String isbn;
    private String title;
    private String author;
    private LocalDateTime releaseDate;
    private ArrayList<BookItem> availableBookItems;
    public BookTitle(){
        this.availableBookItems= new ArrayList<>();
    }
    public BookTitle(String isbn, String title, String author, LocalDateTime releaseDate){
        this();
        this.isbn=isbn;
        this.title=title;
        this.author=author;
        this.releaseDate=releaseDate;

    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public ArrayList<BookItem> getAvailableBookItems() {
        return availableBookItems;
    }

    public void addBookItem(BookItem bi)
    {
        this.availableBookItems.add(bi);
    }
}
