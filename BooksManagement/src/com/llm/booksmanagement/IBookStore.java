package com.llm.booksmanagement;

import java.util.ArrayList;
import java.util.UUID;

public interface IBookStore {
    void insertNewBookTitle(BookTitle bokTitle);

    void insertNewBookItem(BookItem bookItem, String bookIsbn);

    ArrayList<BookTitle> searchBooksByIsbn(String isbn);

    BookTitle getBookTitleWithItems(String isbn);

    BookItem getBookItem(UUID bookItemId);

    void updateBookState(UUID bookItemId, BookItemState itemState);

    void deleteBook(String isbn);

    void deleteBookItem(UUID itemId);

    void updateBookTitle(BookTitle bookTitle);

    void updateBookItem(UUID bookIsbn, BookItem bookItem);

    BookTitle getBookTitleByItem(UUID bookItemId);
}
