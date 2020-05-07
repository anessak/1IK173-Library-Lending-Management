package com.llm.membershiplending;

import java.util.ArrayList;

public class Member {
    private int memberId;
    private int delayedReturnBorrowedBooksCounter;
    private int suspendedTimesCounter;
    private int maximumNumberOfItemsOneCanBorrow;
    private MemberLending membersLendings;
    public Member(int memberId, int nrOfDelays,int nrOfSuspensions,int nrOfItemsCanBorrow)
    {
        this.memberId=memberId;
        this.delayedReturnBorrowedBooksCounter=nrOfDelays;
        this.suspendedTimesCounter=nrOfSuspensions;
        this.maximumNumberOfItemsOneCanBorrow=nrOfItemsCanBorrow;
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

    public MemberLending getMemberLendings() {
        return membersLendings;
    }

    public void setMembersLendings(MemberLending membersBorrowedItems) {
        this.membersLendings = membersBorrowedItems;
    }

    public void increaseSuspendedTimesCounter() {
        this.suspendedTimesCounter++;
    }

    public void increaseDelayedReturnBorrowedBooksCounter() {
        this.delayedReturnBorrowedBooksCounter++;
        if (this.delayedReturnBorrowedBooksCounter>=2)
            this.increaseSuspendedTimesCounter();
    }
}
