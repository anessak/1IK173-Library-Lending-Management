package com.llm.membershiplending;

import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LendingManagerTest {

    @Test
    void searchMemberBorrowedItems() {
    }

    @Test
    void returnBorrowedItem() {
    }

    @Test
    void lendBookItems() {
        var logger= mock(Logger.class);

        IMemberLendingStore db = mock(MemberLendingStore.class,withSettings()
                .useConstructor("databas").defaultAnswer(Answers.RETURNS_MOCKS));
        LendingManager mgr= new LendingManager(db,logger);

        var member=new Member(1000,0,0,6);
        ArrayList<LendingBasketEntity> lbe= new ArrayList<>();
        lbe.add(new LendingBasketEntity(200, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 20, 6, 30)));
        lbe.add(new LendingBasketEntity(200, UUID.randomUUID(),
                LocalDateTime.of(2019, Month.OCTOBER, 20, 6, 30)));
        var ml=new MemberLending(100,lbe);


        when(db.getMemberBorrowedBookItems(member.getMemberId())).thenReturn(ml);

        var searchResult=mgr.searchMemberBorrowedItems(member.getMemberId());

        assertEquals(ml.getMemberId(),searchResult.getMemberId() );
        assertEquals(ml.getBookItemIds().size(),searchResult.getBookItemIds().size() );
        assertEquals(ml.getBookItemIds().get(0).getBookItemId()
                ,searchResult.getBookItemIds().get(0).getBookItemId());
        assertEquals(ml.getBookItemIds().get(0).getLendingDate()
                ,searchResult.getBookItemIds().get(0).getLendingDate());
    }
}