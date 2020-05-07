package com.librarylendingmanagement.infrastructure.events;

public class OnMemberSuspended {
    private int memberId;
    public OnMemberSuspended(int memberId) {
        this.memberId = memberId;
    }
    public int getMemberId() {
        return memberId;
    }

}
