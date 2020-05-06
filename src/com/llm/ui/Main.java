package com.llm.ui;

import com.llm.booksmanagement.*;
import com.llm.membershiplending.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.UUID;

public class Main {


    public static void main(String[] args) throws Exception {
        addMemberLendings();
        addBooks();
    }
    public static void addMemberLendings() throws Exception {
        Logger logger = LogManager.getLogger("MembershipLending");
        IMemberLendingStore db= new MemberLendingStore(logger);
        LendingManager mgr= new LendingManager(db,logger);
        //Medlem med id 1000 får ha max 6 böcker utlånade
        db.addNewMember(new Member(1000,0,0,6));

        //Medlem med id 2000 får ha max 2 böcker utlånade
        db.addNewMember(new Member(2000,0,0,2));

        ArrayList<LendingBasketEntity> liu1= new ArrayList<>();
        liu1.add(new LendingBasketEntity(1000,
                UUID.randomUUID(),
                LocalDateTime.of(2019, Month.FEBRUARY, 20, 6, 30)));
        liu1.add(new LendingBasketEntity(1000,
                UUID.randomUUID(),
                LocalDateTime.of(2019, Month.FEBRUARY, 20, 6, 30)));
        liu1.add(new LendingBasketEntity(1000,
                UUID.randomUUID(),
                LocalDateTime.of(2019, Month.FEBRUARY, 20, 6, 30)));
        liu1.add(new LendingBasketEntity(1000,
                UUID.randomUUID(),
                LocalDateTime.of(2019, Month.FEBRUARY, 20, 6, 30)));
        var li1=new MemberLending(1000,liu1);

        ArrayList<LendingBasketEntity> liu2= new ArrayList<>();
        liu2.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.MARCH, 20, 6, 30)));
        liu2.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.MARCH, 20, 6, 30)));

        var li2=new MemberLending(1000,liu2);

        ArrayList<LendingBasketEntity> liu3= new ArrayList<>();
        liu3.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.DECEMBER, 20, 6, 30)));
        liu3.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.DECEMBER, 20, 6, 30)));

        var li3=new MemberLending(1000,liu3);

        ArrayList<LendingBasketEntity> liu4= new ArrayList<>();
        liu4.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.APRIL, 20, 6, 30)));
        liu4.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.APRIL, 20, 6, 30)));

        liu4.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.APRIL, 20, 6, 30)));

        var li4= new MemberLending(1000,liu4);

        ArrayList<LendingBasketEntity> liu5= new ArrayList<>();
        liu5.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.MAY, 20, 6, 30)));
        liu5.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.MAY, 20, 6, 30)));
        liu5.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.MAY, 20, 6, 30)));
        liu5.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.MAY, 20, 6, 30)));
        liu5.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.MAY, 20, 6, 30)));
        liu5.add(new LendingBasketEntity(1000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.MAY, 20, 6, 30)));

        var li5= new MemberLending(1000,liu5);

        ArrayList<LendingBasketEntity> liu6= new ArrayList<>();
        liu6.add(new LendingBasketEntity(2000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 20, 6, 30)));
        liu6.add(new LendingBasketEntity(2000, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 20, 6, 30)));
        var li6=new MemberLending(1000,liu3);

        mgr.lendBookItems(li1);
        mgr.lendBookItems(li2);
        mgr.lendBookItems(li3);
        mgr.lendBookItems(li4);
        mgr.lendBookItems(li5);
        mgr.lendBookItems(li6);

        var result= mgr.searchMemberBorrowedItems(1000);

    }
    public static void addBooks()
    {
        Logger logger = LogManager.getLogger("BooksRolFileAppndr");
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
