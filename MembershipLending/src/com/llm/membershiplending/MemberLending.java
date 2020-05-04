package com.llm.membershiplending;

import java.util.ArrayList;

public class MemberLending {
    private int memberId;
    private ArrayList<LendingBasket> lendingItems;
    private int delayedReturnBorrowedBooksCounter;
    private int suspendedTimesCounter;

    public MemberLending()
    {
        this.delayedReturnBorrowedBooksCounter=0;
        this.suspendedTimesCounter=0;
        this.lendingItems= new ArrayList<>();
    }

    public int maximumNumberOfItemsCanBorrow()
    {
        //kolla efter rollen och returnera
        //kanske updatera efter när man lägger ny medlem?
        return 0;
    }

}
