package com.llm.booksmanagement;

import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookStoreTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getBookTitleWithItems_Correct() {

        var logger= mock(Logger.class);
        var bus= EventBus.getDefault();
        var db=mock(IBookStore.class);
        var mgr= new BookManagementManager(db,logger,bus);

        var now= LocalDateTime.now();
        var bookItemId= UUID.randomUUID();
        //String isbn, String title, String author, LocalDateTime releaseDate
        var book= new BookTitle("ISBN-1111","Test-Book-Title","Test-Author", now);
        book.addBookItem(new BookItem(bookItemId,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book));


        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(book);

        var storedBookInDB=mgr.getBookTitlebyIsbn(book.getIsbn());

        assertEquals(book.getIsbn(),storedBookInDB.getIsbn());
        assertEquals(book.getAvailableBookItems().size(),storedBookInDB.getAvailableBookItems().size());
        assertEquals(book.getAvailableBookItems().get(0).getId(),
                storedBookInDB.getAvailableBookItems().get(0).getId());

    }
    @Test
    void getBookTitleWithItems_BookNotFound() {

        var logger= mock(Logger.class);
        var bus= EventBus.getDefault();
        var db=mock(IBookStore.class);
        var mgr= new BookManagementManager(db,logger,bus);

        var now= LocalDateTime.now();
        var bookItemId= UUID.randomUUID();
        //String isbn, String title, String author, LocalDateTime releaseDate
        var book= new BookTitle("ISBN-1111","Test-Book-Title","Test-Author", now);
        book.addBookItem(new BookItem(bookItemId,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book));

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(book);

        var storedBookInDB=mgr.getBookTitlebyIsbn("aaaa");

        assertNull(storedBookInDB);

    }
    @Test
    public void addBookTitleToLibraryTest_Correct()
    {
        var logger= mock(Logger.class);
        var bus= EventBus.getDefault();
        var db=mock(IBookStore.class);
        var mgr= new BookManagementManager(db,logger,bus);

        var now= LocalDateTime.now();
        var bookItemId= UUID.randomUUID();
        //String isbn, String title, String author, LocalDateTime releaseDate
        var book1= new BookTitle("ISBN-1111","Test-Book-Title","Test-Author", now);
        book1.addBookItem(new BookItem(bookItemId,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book1));

        var book2= new BookTitle("ISBN-2111","Test-Book-Title","Test-Author", now);
        book2.addBookItem(new BookItem(bookItemId,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book2));

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(book1);
        doNothing().when(db).insertNewBookTitle(book2);

        assertEquals(ResultMessage.Ok,mgr.addBookTitleToLibrary(book2));
    }

}