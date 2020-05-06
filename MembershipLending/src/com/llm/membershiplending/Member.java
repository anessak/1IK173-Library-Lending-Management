package com.llm.membershiplending;

import java.util.ArrayList;

public class Member {
    private int memberId;
    private int delayedReturnBorrowedBooksCounter;
    private int suspendedTimesCounter;
    private int maximumNumberOfItemsOneCanBorrow;

    public Member(int mid, int dr,int st,int maxItemsNr)
    {
        this.memberId=mid;
        this.delayedReturnBorrowedBooksCounter=dr;
        this.suspendedTimesCounter=st;
        this.maximumNumberOfItemsOneCanBorrow=maxItemsNr;
    }



    public int getMemberId() {
        return memberId;
    }

    public int getDelayedReturnBorrowedBooksCounter() {
        return delayedReturnBorrowedBooksCounter;
    }

    public int getMaximumNumberOfItemsOneCanBorrow() {
        return maximumNumberOfItemsOneCanBorrow;
    }

    public int getSuspendedTimesCounter() {
        return suspendedTimesCounter;
    }

}
