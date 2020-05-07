package com.librarylendingmanagement.infrastructure.events;

public class OnMemberBreachedRegulation {
    private int memberId;
    public OnMemberBreachedRegulation(int memberId) {
        this.memberId = memberId;
    }
    public int getMemberId() {
        return memberId;
    }

}
