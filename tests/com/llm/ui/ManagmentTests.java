package com.llm.ui;

import com.llm.booksmanagement.*;
import com.llm.membershipadmin.*;
import com.llm.membershiplending.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

public class ManagmentTests {
    private MembershipManager _membershipMgr;
    private LendingManager _lendingMgr;
    private BookManagementManager _bookMgr;
    @BeforeEach
    public void init()
    {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        EventBus eventBus = EventBus.getDefault();

        Logger logger = LogManager.getLogger("MembershipRolFileAppndr");
        IMembershipStore db= new MembershipStore(logger);
        _membershipMgr = new MembershipManager(db,logger,eventBus);


        Logger logger1 = LogManager.getLogger("MembershipLending");
        IMemberLendingStore db1= new MemberLendingStore(logger1);
        _lendingMgr= new LendingManager(db1,logger1,eventBus);
        LendingScheduler.Init(db1,logger1,eventBus);

        Logger logger2 = LogManager.getLogger("BooksRolFileAppndr");
        IBookStore db2=new BookStore(logger2);
        _bookMgr = new BookManagementManager(db2,logger2,eventBus);

    }
    @AfterEach
    public void deleteDataFromSqliteDB()
    {
        var bookDatabase = new File("BooksManagement/resources/BooksDB.db");
        var memberDatabase = new File("MembershipAdministration/resources/MembersDB.db");
        var lendingDatabase = new File("MembershipLending/resources/MemberShipLendingDB.db");
        if (bookDatabase.delete()) {
            System.out.println("Deleted the file: " + bookDatabase.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }
        if (memberDatabase.delete()) {
            System.out.println("Deleted the file: " + memberDatabase.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }
        if (lendingDatabase.delete()) {
            System.out.println("Deleted the file: " + lendingDatabase.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }
    }
    @Test
    void lendBookItems_WhenMemeberHasNoLendings() {

        var nowDate=LocalDateTime.now();
        var member=new com.llm.membershipadmin.Member(1000,"ssn",
                "fname","ename", MemberRole.Undergraduate,
                MemberStatus.Active,"pwd", nowDate);

        _membershipMgr.registerNewLibraryMember(member);

        var memberFromDB=_membershipMgr.getMemberById(1000);
        var memberFromLendingDb=_lendingMgr.getMemberAndCurrentBorrowedBookItems(1000);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(member.getMemberId(),memberFromDB.getMemberId());
        assertEquals(member.getMemberId(),memberFromLendingDb.getMemberId());

        _membershipMgr.removeMember(1000);
        assertEquals(null,_membershipMgr.getMemberById(1000));

        _lendingMgr.deleteMember(1000);
        assertEquals(null,_lendingMgr.getMemberAndCurrentBorrowedBookItems(1000));

    }
    @Test
    public void LendingAndReturnTest()
    {
        //Create book with bookitem
        var now=LocalDateTime.now();
        var bookItemId=UUID.randomUUID();
        //String isbn, String title, String author, LocalDateTime releaseDate
        var book= new BookTitle("ISBN-1111","Test-Book-Title","Test-Author", now);
        book.addBookItem(new BookItem(bookItemId,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book));
        _bookMgr.addBookTitleToLibrary(book);
        //Verify book after creation
        var storedBookInDB=_bookMgr.getBookTitlebyIsbn(book.getIsbn());
        assertEquals(book.getIsbn(),storedBookInDB.getIsbn());
        assertEquals(book.getAvailableBookItems().size(),storedBookInDB.getAvailableBookItems().size());
        assertEquals(book.getAvailableBookItems().get(0).getId(),
                storedBookInDB.getAvailableBookItems().get(0).getId());

        //Create member
        var nowDate=LocalDateTime.now();
        var member=new com.llm.membershipadmin.Member(1000,"ssn",
                "fname","ename", MemberRole.Undergraduate,
                MemberStatus.Active,"pwd", nowDate);
        _membershipMgr.registerNewLibraryMember(member);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //verify member after creation
        var memberFromDB=_membershipMgr.getMemberById(1000);
        var memberFromLendingDb=_lendingMgr.getMemberAndCurrentBorrowedBookItems(1000);
        assertEquals(member.getMemberId(),memberFromDB.getMemberId());
        assertEquals(member.getMemberId(),memberFromLendingDb.getMemberId());
        assertEquals(0,memberFromLendingDb.getDelayedReturnBorrowedBooksCounter());
        assertEquals(0,memberFromLendingDb.getSuspendedTimesCounter());
        assertEquals(3,memberFromLendingDb.getMaximumNumberOfItemsOneCanBorrow());

        //Lend book
        var arrayOfBookItemssToLend= new ArrayList<LendingBasketEntity>();
        arrayOfBookItemssToLend.add(new LendingBasketEntity(1000,bookItemId,LocalDateTime.now()));
        var lending= new MemberLending(1000,arrayOfBookItemssToLend);
        _lendingMgr.lendBookItems(lending);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Verify lendings after lending
        var memberLendings= _lendingMgr.getMemberAndCurrentBorrowedBookItems(1000);
        var availableBooksAfterLending= _bookMgr.getBookTitlebyIsbn(book.getIsbn());
        assertEquals(1000,memberLendings.getMemberId());
        assertEquals(0,memberLendings.getDelayedReturnBorrowedBooksCounter());
        assertEquals(0,memberLendings.getSuspendedTimesCounter());
        assertEquals(3,memberLendings.getMaximumNumberOfItemsOneCanBorrow());
        assertEquals(bookItemId,
                memberLendings.getMemberLendings().getBookItemsIdWithDate().get(0).getBookItemId());
        assertEquals(BookItemState.BorrowedToMember ,availableBooksAfterLending.getAvailableBookItems().get(0).getItemState());

        //Wait for 20 sec for bookitem return time to expire (default 15 seconds for test purpose)
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        var memberLendingsAfterDateToReturnBookAccured=
                _lendingMgr.getMemberAndCurrentBorrowedBookItems(1000);
        assertEquals(1,memberLendingsAfterDateToReturnBookAccured.getDelayedReturnBorrowedBooksCounter());
        assertEquals(0,memberLendingsAfterDateToReturnBookAccured.getSuspendedTimesCounter());
        assertEquals(3,memberLendingsAfterDateToReturnBookAccured.getMaximumNumberOfItemsOneCanBorrow());

        //return book
        var returnedItemsList= new ArrayList<UUID>();
        returnedItemsList.add(bookItemId);
        _lendingMgr.returnBorrowedItem(new ReturnLendBasket(1000,returnedItemsList,LocalDateTime.now()));
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        var borrowedBooksAfterReturn=_lendingMgr.getMemberAndCurrentBorrowedBookItems(1000);
        var booksAfterReturnLending= _bookMgr.getBookTitlebyIsbn(book.getIsbn());

        assertEquals(0,borrowedBooksAfterReturn.getMemberLendings().getBookItemsIdWithDate().size());
        assertEquals(BookItemState.HomeInLibrary,booksAfterReturnLending.getAvailableBookItems().get(0).getItemState());
    }
    @Test
    public void LendingAndReturnSuspendUserTest()
    {
        //Create book with bookitem
        var now=LocalDateTime.now();
        var bookItemId1=UUID.randomUUID();
        var bookItemId2=UUID.randomUUID();
        var bookItemId3=UUID.randomUUID();
        //String isbn, String title, String author, LocalDateTime releaseDate
        var book= new BookTitle("ISBN-1111","Test-Book-Title","Test-Author", now);
        book.addBookItem(new BookItem(bookItemId1,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book));
        book.addBookItem(new BookItem(bookItemId2,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book));
        book.addBookItem(new BookItem(bookItemId3,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book));
        _bookMgr.addBookTitleToLibrary(book);
        //Verify book after creation
        var storedBookInDB=_bookMgr.getBookTitlebyIsbn(book.getIsbn());
        assertEquals(book.getIsbn(),storedBookInDB.getIsbn());
        assertEquals(book.getAvailableBookItems().size(),storedBookInDB.getAvailableBookItems().size());
        assertEquals(book.getAvailableBookItems().get(0).getId(),
                storedBookInDB.getAvailableBookItems().get(0).getId());

        //Create member
        var nowDate=LocalDateTime.now();
        var member=new com.llm.membershipadmin.Member(1000,"ssn",
                "fname","ename", MemberRole.Undergraduate,
                MemberStatus.Active,"pwd", nowDate);
        _membershipMgr.registerNewLibraryMember(member);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //verify member after creation
        var memberFromDB=_membershipMgr.getMemberById(1000);
        var memberFromLendingDb=_lendingMgr.getMemberAndCurrentBorrowedBookItems(1000);
        assertEquals(member.getMemberId(),memberFromDB.getMemberId());
        assertEquals(member.getMemberId(),memberFromLendingDb.getMemberId());
        assertEquals(0,memberFromLendingDb.getDelayedReturnBorrowedBooksCounter());
        assertEquals(0,memberFromLendingDb.getSuspendedTimesCounter());
        assertEquals(3,memberFromLendingDb.getMaximumNumberOfItemsOneCanBorrow());

        //Lend book
        var arrayOfBookItemssToLend= new ArrayList<LendingBasketEntity>();
        arrayOfBookItemssToLend.add(new LendingBasketEntity(1000,bookItemId1,LocalDateTime.now()));
        var lending= new MemberLending(1000,arrayOfBookItemssToLend);
        _lendingMgr.lendBookItems(lending);

        var arrayOfBookItemssToLend1= new ArrayList<LendingBasketEntity>();
        arrayOfBookItemssToLend1.add(new LendingBasketEntity(1000,bookItemId2,LocalDateTime.now()));
        var lending1= new MemberLending(1000,arrayOfBookItemssToLend1);
        _lendingMgr.lendBookItems(lending1);

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Verify lendings after lending
        var memberLendings= _lendingMgr.getMemberAndCurrentBorrowedBookItems(1000);
        var memberAfterSuspension=_membershipMgr.getMemberById(1000);
        assertEquals(1000,memberLendings.getMemberId());
        assertEquals(2,memberLendings.getDelayedReturnBorrowedBooksCounter());
        assertEquals(1,memberLendings.getSuspendedTimesCounter());
        assertEquals(3,memberLendings.getMaximumNumberOfItemsOneCanBorrow());
        assertEquals(MemberStatus.Suspended, memberAfterSuspension.getMemberStatus());
    }
    @Test
    public void LendingAndReturnSuspendAndDeleteUserTest()
    {
        //Create book with bookitem
        var now=LocalDateTime.now();
        var bookItemId1=UUID.randomUUID();
        var bookItemId2=UUID.randomUUID();
        var bookItemId3=UUID.randomUUID();
        var bookItemId4=UUID.randomUUID();
        var bookItemId5=UUID.randomUUID();
        var bookItemId6=UUID.randomUUID();

        //String isbn, String title, String author, LocalDateTime releaseDate
        var book= new BookTitle("ISBN-1111","Test-Book-Title","Test-Author", now);
        book.addBookItem(new BookItem(bookItemId1,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book));
        book.addBookItem(new BookItem(bookItemId2,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book));
        book.addBookItem(new BookItem(bookItemId3,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book));
        book.addBookItem(new BookItem(bookItemId4,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book));
        book.addBookItem(new BookItem(bookItemId5,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book));
        book.addBookItem(new BookItem(bookItemId6,
                ItemType.Paper,BookItemState.HomeInLibrary,now,book));
        _bookMgr.addBookTitleToLibrary(book);
        //Verify book after creation
        var storedBookInDB=_bookMgr.getBookTitlebyIsbn(book.getIsbn());
        assertEquals(book.getIsbn(),storedBookInDB.getIsbn());
        assertEquals(book.getAvailableBookItems().size(),storedBookInDB.getAvailableBookItems().size());
        assertEquals(book.getAvailableBookItems().get(0).getId(),
                storedBookInDB.getAvailableBookItems().get(0).getId());

        //Create member
        var nowDate=LocalDateTime.now();
        var member=new com.llm.membershipadmin.Member(1000,"ssn",
                "fname","ename", MemberRole.Undergraduate,
                MemberStatus.Active,"pwd", nowDate);
        _membershipMgr.registerNewLibraryMember(member);

        //verify member after creation
        var memberFromDB=_membershipMgr.getMemberById(1000);
        var memberFromLendingDb=_lendingMgr.getMemberAndCurrentBorrowedBookItems(1000);
        assertEquals(member.getMemberId(),memberFromDB.getMemberId());
        assertEquals(member.getMemberId(),memberFromLendingDb.getMemberId());
        assertEquals(0,memberFromLendingDb.getDelayedReturnBorrowedBooksCounter());
        assertEquals(0,memberFromLendingDb.getSuspendedTimesCounter());
        assertEquals(3,memberFromLendingDb.getMaximumNumberOfItemsOneCanBorrow());

        //Lend book
        var arrayOfBookItemssToLend= new ArrayList<LendingBasketEntity>();
        arrayOfBookItemssToLend.add(new LendingBasketEntity(1000,bookItemId1,LocalDateTime.now()));
        var lending= new MemberLending(1000,arrayOfBookItemssToLend);
        _lendingMgr.lendBookItems(lending);

        var arrayOfBookItemssToLend1= new ArrayList<LendingBasketEntity>();
        arrayOfBookItemssToLend1.add(new LendingBasketEntity(1000,bookItemId2,LocalDateTime.now()));
        var lending1= new MemberLending(1000,arrayOfBookItemssToLend1);
        _lendingMgr.lendBookItems(lending1);

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Verify lendings after första lending
        var memberLendings= _lendingMgr.getMemberAndCurrentBorrowedBookItems(1000);
        var memberAfterSuspension=_membershipMgr.getMemberById(1000);
        assertEquals(1000,memberLendings.getMemberId());
        assertEquals(2,memberLendings.getDelayedReturnBorrowedBooksCounter());
        assertEquals(1,memberLendings.getSuspendedTimesCounter());
        assertEquals(3,memberLendings.getMaximumNumberOfItemsOneCanBorrow());
        assertEquals(MemberStatus.Suspended, memberAfterSuspension.getMemberStatus());

        //Return books
        var returnedItemsList= new ArrayList<UUID>();
        returnedItemsList.add(bookItemId1);
        returnedItemsList.add(bookItemId2);
        _lendingMgr.returnBorrowedItem(new ReturnLendBasket(1000,returnedItemsList,LocalDateTime.now()));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Lend secontime


        var arrayOfBookItemssToLend2= new ArrayList<LendingBasketEntity>();
        arrayOfBookItemssToLend2.add(new LendingBasketEntity(1000,bookItemId3,LocalDateTime.now()));
        var lending2= new MemberLending(1000,arrayOfBookItemssToLend2);
        var r1=_lendingMgr.lendBookItems(lending2);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        var arrayOfBookItemssToLend3= new ArrayList<LendingBasketEntity>();
        arrayOfBookItemssToLend3.add(new LendingBasketEntity(1000,bookItemId4,LocalDateTime.now()));
        var lending3= new MemberLending(1000,arrayOfBookItemssToLend3);
        var r2=_lendingMgr.lendBookItems(lending3);

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Verify lendings efter andra lending
        var memberLendingsSuspend= _lendingMgr.getMemberAndCurrentBorrowedBookItems(1000);
        var memberAfterSecondSuspension=_membershipMgr.getMemberById(1000);
        assertEquals(1000,memberLendingsSuspend.getMemberId());
        assertEquals(4,memberLendingsSuspend.getDelayedReturnBorrowedBooksCounter());
        assertEquals(2,memberLendingsSuspend.getSuspendedTimesCounter());
        assertEquals(3,memberLendingsSuspend.getMaximumNumberOfItemsOneCanBorrow());
        assertEquals(null, memberAfterSecondSuspension);
    }


}
