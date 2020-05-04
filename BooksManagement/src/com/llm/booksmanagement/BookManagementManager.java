package com.llm.booksmanagement;

public class BookManagementManager {
    //use cases for book
    IBookStore bookStore;
    public BookManagementManager(IBookStore bokStore){
        this.bookStore=bokStore;
    }

}
