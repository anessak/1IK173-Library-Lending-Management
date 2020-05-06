package com.llm.membershiplending;

import java.util.ArrayList;

public interface IMemberLendingStore {
    void addNewLendingBasketForMember(MemberLending memberLending);

    void removeBookItemFromLending(ReturnLendBasket returnItems);

    MemberLending getMemberBorrowedBookItems(int memberId);

    Member getMember(int memberId);

    void addNewMember(Member member);

    void updateDelayedReturnCounter(int memberId, int delayedReturnNr);

    void updateSuspendedCounter(int memberId, int suspendedTimesNr);
}
