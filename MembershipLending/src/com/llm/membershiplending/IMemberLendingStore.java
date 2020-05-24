package com.llm.membershiplending;

public interface IMemberLendingStore {
    void addNewLendingBasketForMember(MemberLending memberLending);

    int removeBookItemFromLending(ReturnLendBasket returnItems);

    MemberLending getMemberBorrowedBookItems(int memberId);

    Member getMember(int memberId);

    void addNewMember(Member member);

    int deleteMember(int memberId);

    void updateMemberCounters(int memberId, int delayedReturnNr,int suspendedTimesNr);



}
