package com.llm.booksmanagement;

public interface IBookStore {
    void insertNewBookTitle(BookTitle bokTitle);

    void insertNewBookItem(BookItem bookItem, String bookIsbn);

    BookTitle getBookTitle(String isbn);

    BookTitle getBookTitleWithItems(String isbn);
}
