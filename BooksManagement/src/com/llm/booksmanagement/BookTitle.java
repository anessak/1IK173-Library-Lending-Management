package com.llm.booksmanagement;

import java.util.ArrayList;
import java.util.Date;

public class BookTitle {
    private String isbn;
    private String title;
    private String author;
    private Date releaseDate;
    private ArrayList<BookItem> availableBookItems;
    public BookTitle(){
        this.availableBookItems= new ArrayList<>();
    }
}
