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

class BookManagementManagerTest {

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
    @Test
    public void addBookTitleToLibraryTest_ShouldReturnErrorForNullArgument()
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

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(book1);
        doNothing().when(db).insertNewBookTitle(book1);

        assertEquals(ResultMessage.Error,mgr.addBookTitleToLibrary(null));
    }
    @Test
    public void addBookTitleToLibraryTest_ShouldReturnError()
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

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(book1);
        doNothing().when(db).insertNewBookTitle(book1);

        assertEquals(ResultMessage.Conflict,mgr.addBookTitleToLibrary(book1));
    }
    @Test
    public void addNewBookItemsForBok_Correct()
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

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(book1);
        doNothing().when(db).insertNewBookTitle(book1);

        assertEquals(ResultMessage.Ok,mgr.addNewBookItemsForBok(book1));
    }
    @Test
    public void addNewBookItemsForBok_ShouldReturnErrorOrNotFound()
    {
        var logger= mock(Logger.class);
        var bus= EventBus.getDefault();
        var db=mock(IBookStore.class);
        var mgr= new BookManagementManager(db,logger,bus);

        var now= LocalDateTime.now();
        var bookItemId= UUID.randomUUID();
        //String isbn, String title, String author, LocalDateTime releaseDate
        var book1= new BookTitle("ISBN-1111","Test-Book-Title","Test-Author", now);
        var bookItem1=new BookItem(bookItemId,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book1);
        book1.addBookItem(bookItem1);

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(null);
        doNothing().when(db).insertNewBookItem(bookItem1,book1.getIsbn());

        assertEquals(ResultMessage.NotFound,mgr.addNewBookItemsForBok(book1));
        assertEquals(ResultMessage.Error,mgr.addNewBookItemsForBok(null));
    }
    @Test
    public void updateBookTitle_MultipleTests_ShouldReturnOkOrError()
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

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(book1);
        doNothing().when(db).updateBookTitle(book1);

        assertEquals(ResultMessage.Ok,mgr.updateBookTitle(book1,book1.getIsbn()));
        assertEquals(ResultMessage.Error,mgr.updateBookTitle(null,book1.getIsbn()));
    }
    @Test
    public void updateBookTitle_MultipleTests_ShouldReturnNotFound()
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

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(null);
        doNothing().when(db).updateBookTitle(book1);

        assertEquals(ResultMessage.NotFound,mgr.updateBookTitle(book1,book1.getIsbn()));
    }
    @Test
    public void removeBookFromRegistry_MultipleTests_ShouldReturnOkOrError()
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

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(book1);
        when(db.deleteBook(book1.getIsbn())).thenReturn(0);

        assertEquals(ResultMessage.Ok,mgr.removeBookFromRegistry(book1));
        assertEquals(ResultMessage.Error,mgr.removeBookFromRegistry(null));

    }
    @Test
    public void removeBookFromRegistry_ShouldReturnNotFound()
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

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(null);
        when(db.deleteBook(book1.getIsbn())).thenReturn(0);

        assertEquals(ResultMessage.NotFound,mgr.removeBookFromRegistry(book1));
    }
    @Test
    public void removeBookFromRegistry_ShouldReturnError()
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

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(book1);
        when(db.deleteBook(book1.getIsbn())).thenReturn(-1);

        assertEquals(ResultMessage.Error,mgr.removeBookFromRegistry(book1));
    }
    @Test
    public void removeBookFromRegistryViaIsbn_Correct()
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

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(book1);
        when(db.deleteBook(book1.getIsbn())).thenReturn(0);

        assertEquals(ResultMessage.Ok,mgr.removeBookFromRegistryWithIsbn(book1.getIsbn()));

    }
    @Test
    public void removeBookFromRegistryViaIsbn_ShouldReturnNotFound()
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

        when(db.getBookTitleWithItems("ISBN-1111")).thenReturn(null);
        when(db.deleteBook(book1.getIsbn())).thenReturn(0);

        assertEquals(ResultMessage.NotFound,mgr.removeBookFromRegistryWithIsbn(book1.getIsbn()));
    }
    @Test
    public void removeBookItemFromRegistry_Correct()
    {
        var logger= mock(Logger.class);
        var bus= EventBus.getDefault();
        var db=mock(IBookStore.class);
        var mgr= new BookManagementManager(db,logger,bus);

        var now= LocalDateTime.now();
        var bookItemId= UUID.randomUUID();
        //String isbn, String title, String author, LocalDateTime releaseDate
        var book1= new BookTitle("ISBN-1111","Test-Book-Title","Test-Author", now);
        var bookItem1=new BookItem(bookItemId,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book1);
        book1.addBookItem(bookItem1);

        when(db.getBookTitleByItem(bookItemId)).thenReturn(book1);
        when(db.getBookItem(bookItemId)).thenReturn(bookItem1);
        when(db.deleteBookItem(bookItemId)).thenReturn(0);

        assertEquals(ResultMessage.Ok,mgr.removeBookItemFromRegistry(bookItemId));
    }
    @Test
    public void removeBookItemFromRegistry_ShouldReturnNotFound()
    {
        var logger= mock(Logger.class);
        var bus= EventBus.getDefault();
        var db=mock(IBookStore.class);
        var mgr= new BookManagementManager(db,logger,bus);

        var now= LocalDateTime.now();
        var bookItemId= UUID.randomUUID();
        //String isbn, String title, String author, LocalDateTime releaseDate
        var book1= new BookTitle("ISBN-1111","Test-Book-Title","Test-Author", now);
        var bookItem1=new BookItem(bookItemId,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book1);
        book1.addBookItem(bookItem1);

        when(db.getBookTitleByItem(bookItemId)).thenReturn(book1);
        when(db.getBookItem(bookItemId)).thenReturn(null);
        when(db.deleteBookItem(bookItemId)).thenReturn(0);

        assertEquals(ResultMessage.NotFound,mgr.removeBookItemFromRegistry(bookItemId));
    }
    @Test
    public void removeBookItemFromRegistry_ShouldReturnError()
    {
        var logger= mock(Logger.class);
        var bus= EventBus.getDefault();
        var db=mock(IBookStore.class);
        var mgr= new BookManagementManager(db,logger,bus);

        var now= LocalDateTime.now();
        var bookItemId= UUID.randomUUID();
        //String isbn, String title, String author, LocalDateTime releaseDate
        var book1= new BookTitle("ISBN-1111","Test-Book-Title","Test-Author", now);
        var bookItem1=new BookItem(bookItemId,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book1);
        book1.addBookItem(bookItem1);

        when(db.getBookTitleByItem(bookItemId)).thenReturn(book1);
        when(db.getBookItem(bookItemId)).thenReturn(bookItem1);
        when(db.deleteBookItem(bookItemId)).thenReturn(-1);

        assertEquals(ResultMessage.Error,mgr.removeBookItemFromRegistry(bookItemId));
    }
}