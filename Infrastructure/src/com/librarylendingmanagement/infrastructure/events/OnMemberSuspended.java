package com.librarylendingmanagement.infrastructure.events;

public class OnMemberSuspended {
    private int memberId;
    public int getMemberId() {
        return memberId;
    }
    public OnMemberSuspended(int memberid)
    {
        this.memberId=memberid;
    }

}
