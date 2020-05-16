package com.llm.ui;

import com.librarylendingmanagement.infrastructure.events.OnMemberCreated;
import com.llm.booksmanagement.BookManagementManager;
import com.llm.booksmanagement.BookStore;
import com.llm.booksmanagement.IBookStore;
import com.llm.membershipadmin.*;
import com.llm.membershiplending.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        _bookMgr = new BookManagementManager(db2,logger2);

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
}
