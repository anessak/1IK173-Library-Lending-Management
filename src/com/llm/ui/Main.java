package com.llm.ui;

import com.llm.booksmanagement.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

public class Main {
    private static Logger logger = LogManager.getLogger("RollingFileLogger");

    public static void main(String[] args) {
        IBookStore db=new BookStore(logger);
        BookManagementManager bm= new BookManagementManager(db,logger);

        var bookTitleA=new BookTitle("1022-2321-33123",
                "bästa boken","Anessa Kurtagic",
                LocalDateTime.of(2015, Month.FEBRUARY, 20, 6, 30));

        bookTitleA.addBookItem(new BookItem(UUID.randomUUID(), ItemType.Paper, BookItemState.HomeInLibrary));
        bookTitleA.addBookItem(new BookItem(UUID.randomUUID(), ItemType.Paper, BookItemState.HomeInLibrary));
        bookTitleA.addBookItem(new BookItem(UUID.randomUUID(), ItemType.Paper, BookItemState.BorrowedToMember));
        bookTitleA.addBookItem(new BookItem(UUID.randomUUID(), ItemType.Audio, BookItemState.HomeInLibrary));

        var bookTitleB= new BookTitle("9992-2321-31230",
                "Äntligen Monarki igen!","Kungen är Bäst",
                LocalDateTime.of(2020, Month.FEBRUARY, 21, 2, 10));
        bookTitleB.addBookItem(new BookItem(UUID.randomUUID(), ItemType.Paper, BookItemState.HomeInLibrary));
        bookTitleB.addBookItem(new BookItem(UUID.randomUUID(), ItemType.Paper, BookItemState.HomeInLibrary));
        bookTitleB.addBookItem(new BookItem(UUID.randomUUID(), ItemType.Video, BookItemState.Lost));

        bm.addBookTitleToLibrary(bookTitleA);
        bm.addBookTitleToLibrary(bookTitleB);

        var res1=bm.searchBookTitlebyIsbn("1022-");
        var res2=bm.searchBookTitlebyIsbn("9992-2321-31230");
    }
}
