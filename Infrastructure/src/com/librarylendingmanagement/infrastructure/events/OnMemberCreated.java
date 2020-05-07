package com.librarylendingmanagement.infrastructure.events;

public class OnMemberCreated {
    private int memberId;
    private int maxItemsToBorrow;
    public OnMemberCreated(int memberId,int maxItemsToBorrow) {
        this.memberId = memberId;
        this.maxItemsToBorrow=maxItemsToBorrow;
    }

    public int getMemberId() {
        return memberId;
    }

    public int getMaxItemsToBorrow() {
        return maxItemsToBorrow;
    }
}
