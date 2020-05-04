package com.llm.ui;

import com.llm.booksmanagement.*;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        IBookStore db=new BookStore();
        db.insertNewBookTitle(new BookTitle("1122-2321-33123",
                "bästa boken","Anessa Kurtagic",
                LocalDateTime.of(2015, Month.FEBRUARY, 20, 6, 30)));
        db.insertNewBookItem(new BookItem(UUID.randomUUID(), ItemType.Paper, BookItemState.New),"1122-2321-33123");
        db.insertNewBookItem(new BookItem(UUID.randomUUID(), ItemType.Paper, BookItemState.Good),"1122-2321-33123");
        db.insertNewBookItem(new BookItem(UUID.randomUUID(), ItemType.Paper, BookItemState.Bad),"1122-2321-33123");
        db.insertNewBookItem(new BookItem(UUID.randomUUID(), ItemType.Audio, BookItemState.New),"1122-2321-33123");

        db.insertNewBookTitle(new BookTitle("9992-2321-3123",
                "Äntligen Monarki igen!","Kungen är Bäst",
                LocalDateTime.of(2020, Month.FEBRUARY, 21, 2, 10)));
        db.insertNewBookItem(new BookItem(UUID.randomUUID(), ItemType.Paper, BookItemState.New),"9992-2321-3123");
        db.insertNewBookItem(new BookItem(UUID.randomUUID(), ItemType.Paper, BookItemState.Good),"9992-2321-3123");
        db.insertNewBookItem(new BookItem(UUID.randomUUID(), ItemType.Video, BookItemState.New),"9992-2321-3123");

        var res1=db.getBookTitleWithItems("1122-2321-33123");
        var res2=db.getBookTitleWithItems("9992-2321-3123");
    }
}
