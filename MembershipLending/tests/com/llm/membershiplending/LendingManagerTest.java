package com.llm.membershiplending;

import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LendingManagerTest {

    @Test
    void lendBookItems_WhenMemeberHasNoLendings() {
        var logger= mock(Logger.class);
        var bus=EventBus.getDefault();
        IMemberLendingStore db = mock(MemberLendingStore.class,withSettings()
                .useConstructor(logger).defaultAnswer(Answers.RETURNS_MOCKS));
        LendingManager mgr= new LendingManager(db,logger,bus);
        LendingScheduler.Init(db,logger,bus);


        var member=new Member(1000,0,0,6);
        ArrayList<LendingBasketEntity> lbe= new ArrayList<>();
        lbe.add(new LendingBasketEntity(200, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 20, 6, 30)));
        lbe.add(new LendingBasketEntity(200, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 20, 6, 30)));
        var ml=new MemberLending(100,lbe);

        when(db.getMember(member.getMemberId())).thenReturn(member);
        doNothing().when(db).addNewLendingBasketForMember(ml);
        when(db.getMemberBorrowedBookItems(member.getMemberId())).thenReturn(ml);

        var searchResult=mgr.searchMemberBorrowedItems(member.getMemberId());

        assertEquals(ml.getMemberId(),searchResult.getMemberId() );
        assertEquals(ml.getBookItemsIdWithDate().size(),searchResult.getBookItemsIdWithDate().size() );
        assertEquals(ml.getBookItemsIdWithDate().get(0).getBookItemId()
                ,searchResult.getBookItemsIdWithDate().get(0).getBookItemId());
        assertEquals(ml.getBookItemsIdWithDate().get(0).getLendingDate()
                ,searchResult.getBookItemsIdWithDate().get(0).getLendingDate());
    }
    @Test
    void lendBookItems_WhenMemberNotFoundShoudReturnError() {
        var logger= mock(Logger.class);
        var bus=EventBus.getDefault();

        IMemberLendingStore db = mock(MemberLendingStore.class,withSettings()
                .useConstructor(logger).defaultAnswer(Answers.RETURNS_MOCKS));
        LendingManager mgr= new LendingManager(db,logger,bus);

        var member=new Member(1000,0,0,1);
        ArrayList<LendingBasketEntity> lbe= new ArrayList<>();
        lbe.add(new LendingBasketEntity(200, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 20, 6, 30)));
        lbe.add(new LendingBasketEntity(200, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 20, 6, 30)));
        var ml=new MemberLending(100,lbe);

        when(db.getMember(ml.getMemberId())).thenReturn(null);

        assertEquals("No Member Was found",mgr.lendBookItems(ml) );
    }
    @Test
    void lendBookItems_WhenMemberTriesToBorrowToManyBooksReturnError() {
        var logger= mock(Logger.class);
        var bus=EventBus.getDefault();

        IMemberLendingStore db = mock(MemberLendingStore.class,withSettings()
                .useConstructor(logger).defaultAnswer(Answers.RETURNS_MOCKS));
        LendingManager mgr= new LendingManager(db,logger,bus);

        var member=new Member(1000,0,0,1);
        ArrayList<LendingBasketEntity> bookItemsManFörsökerLåna= new ArrayList<>();
        bookItemsManFörsökerLåna.add(new LendingBasketEntity(200, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 20, 6, 30)));
        bookItemsManFörsökerLåna.add(new LendingBasketEntity(200, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 20, 6, 30)));
        var ml=new MemberLending(100,bookItemsManFörsökerLåna);

        ArrayList<LendingBasketEntity> bookItemSomManHarredanUtlånat= new ArrayList<>();
        bookItemSomManHarredanUtlånat.add(new LendingBasketEntity(200, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 10, 6, 30)));

        when(db.getMember(member.getMemberId())).thenReturn(member);
        doNothing().when(db).addNewLendingBasketForMember(ml);
        when(db.getMemberBorrowedBookItems(member.getMemberId())).thenReturn(new MemberLending(100,bookItemSomManHarredanUtlånat));


        assertEquals("du har försökt låna för många böcker ta bort 2 stycken",mgr.lendBookItems(ml) );
    }
    @Test
    public void getMemberAndCurrentBorrowedBookItems_Correct() {
        var logger= mock(Logger.class);
        var bus=EventBus.getDefault();
        IMemberLendingStore db = mock(MemberLendingStore.class,withSettings()
                .useConstructor(logger).defaultAnswer(Answers.RETURNS_MOCKS));
        LendingManager mgr= new LendingManager(db,logger,bus);
        LendingScheduler.Init(db,logger,bus);

        var member=new Member(1000,0,0,6);
        ArrayList<LendingBasketEntity> lånadeBöcker= new ArrayList<>();
        lånadeBöcker.add(new LendingBasketEntity(200, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 10, 6, 30)));
        var ml=new MemberLending(100,lånadeBöcker);

        when(db.getMember(member.getMemberId())).thenReturn(member);
        when(db.getMemberBorrowedBookItems(member.getMemberId())).thenReturn(ml);

        member.setMembersLendings(ml);
        assertEquals(member,mgr.getMemberAndCurrentBorrowedBookItems(member.getMemberId()) );

    }
    @Test
    public void getMemberAndCurrentBorrowedBookItems_WithNoBorrowedBooks() {
        var logger= mock(Logger.class);
        var bus=EventBus.getDefault();
        IMemberLendingStore db = mock(MemberLendingStore.class,withSettings()
                .useConstructor(logger).defaultAnswer(Answers.RETURNS_MOCKS));
        LendingManager mgr= new LendingManager(db,logger,bus);
        LendingScheduler.Init(db,logger,bus);

        var member=new Member(1000,0,0,6);

        when(db.getMember(member.getMemberId())).thenReturn(member);
        when(db.getMemberBorrowedBookItems(member.getMemberId())).thenReturn(null);

        assertEquals(member,mgr.getMemberAndCurrentBorrowedBookItems(member.getMemberId()) );
    }
    @Test
    public void getMemberAndCurrentBorrowedBookItem_WithNoMemberShouldReturnError() {
        var logger= mock(Logger.class);
        var bus=EventBus.getDefault();
        IMemberLendingStore db = mock(MemberLendingStore.class,withSettings()
                .useConstructor(logger).defaultAnswer(Answers.RETURNS_MOCKS));
        LendingManager mgr= new LendingManager(db,logger,bus);
        LendingScheduler.Init(db,logger,bus);

        var member=new Member(1000,0,0,6);

        when(db.getMember(member.getMemberId())).thenReturn(null);
        when(db.getMemberBorrowedBookItems(member.getMemberId())).thenReturn(null);

        assertEquals(null,mgr.getMemberAndCurrentBorrowedBookItems(member.getMemberId()) );

    }
    @Test
    public void searchMemberBorrowedItems_Correct() {
        var logger= mock(Logger.class);
        var bus=EventBus.getDefault();
        IMemberLendingStore db = mock(MemberLendingStore.class,withSettings()
                .useConstructor(logger).defaultAnswer(Answers.RETURNS_MOCKS));
        LendingManager mgr= new LendingManager(db,logger,bus);
        LendingScheduler.Init(db,logger,bus);

        var member=new Member(1000,0,0,6);
        ArrayList<LendingBasketEntity> lånadeBöcker= new ArrayList<>();
        lånadeBöcker.add(new LendingBasketEntity(200, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 10, 6, 30)));
        var ml=new MemberLending(100,lånadeBöcker);

        when(db.getMember(member.getMemberId())).thenReturn(member);
        when(db.getMemberBorrowedBookItems(member.getMemberId())).thenReturn(ml);

        member.setMembersLendings(ml);
        assertEquals(ml,mgr.searchMemberBorrowedItems(member.getMemberId()) );

    }
    @Test
    public void searchMemberBorrowedItems_NoMemberFound() {
        var logger= mock(Logger.class);
        var bus=EventBus.getDefault();
        IMemberLendingStore db = mock(MemberLendingStore.class,withSettings()
                .useConstructor(logger).defaultAnswer(Answers.RETURNS_MOCKS));
        LendingManager mgr= new LendingManager(db,logger,bus);
        LendingScheduler.Init(db,logger,bus);

        var member=new Member(1000,0,0,6);

        when(db.getMember(member.getMemberId())).thenReturn(null);

        assertEquals(null,mgr.searchMemberBorrowedItems(member.getMemberId()) );

    }
    @Test
    public void returnBorrowedItem_Correct() {
        var logger= mock(Logger.class);
        var bus=EventBus.getDefault();
        IMemberLendingStore db = mock(MemberLendingStore.class,withSettings()
                .useConstructor(logger).defaultAnswer(Answers.RETURNS_MOCKS));
        LendingManager mgr= new LendingManager(db,logger,bus);
        LendingScheduler.Init(db,logger,bus);

        var member=new Member(1000,0,0,6);
        ArrayList<UUID> returnBöckerIDs= new ArrayList<>();
        var uuid= UUID.randomUUID();
        returnBöckerIDs.add(uuid);
        var rl=new ReturnLendBasket(100,returnBöckerIDs,
                LocalDateTime.of(2019, Month.OCTOBER, 10, 6, 30));

        when(db.getMember(member.getMemberId())).thenReturn(member);
        when(db.removeBookItemFromLending(rl)).thenReturn(0);

        assertEquals(0,mgr.returnBorrowedItem(rl) );
    }
    @Test
    public void returnBorrowedItem_NoMemberFoundShouldReturnError() {
        var logger= mock(Logger.class);
        var bus=EventBus.getDefault();
        IMemberLendingStore db = mock(MemberLendingStore.class,withSettings()
                .useConstructor(logger).defaultAnswer(Answers.RETURNS_MOCKS));
        LendingManager mgr= new LendingManager(db,logger,bus);
        LendingScheduler.Init(db,logger,bus);

        ArrayList<UUID> returnBöckerIDs= new ArrayList<>();
        var uuid= UUID.randomUUID();
        returnBöckerIDs.add(uuid);
        var rl=new ReturnLendBasket(100,returnBöckerIDs,
                LocalDateTime.of(2019, Month.OCTOBER, 10, 6, 30));

        when(db.getMember(100)).thenReturn(null);

        assertEquals(-1,mgr.returnBorrowedItem(rl) );

    }
    @Test
    public void returnBorrowedItem_DataBaseRemovalReturnsError() {
        var logger= mock(Logger.class);
        var bus=EventBus.getDefault();
        IMemberLendingStore db = mock(MemberLendingStore.class,withSettings()
                .useConstructor(logger).defaultAnswer(Answers.RETURNS_MOCKS));
        LendingManager mgr= new LendingManager(db,logger,bus);
        LendingScheduler.Init(db,logger,bus);

        var member=new Member(1000,0,0,6);
        ArrayList<UUID> returnBöckerIDs= new ArrayList<>();
        var uuid= UUID.randomUUID();
        returnBöckerIDs.add(uuid);
        var rl=new ReturnLendBasket(100,returnBöckerIDs,
                LocalDateTime.of(2019, Month.OCTOBER, 10, 6, 30));

        when(db.getMember(member.getMemberId())).thenReturn(member);
        when(db.removeBookItemFromLending(rl)).thenReturn(-1);

        assertEquals(-1,mgr.returnBorrowedItem(rl) );
    }
}