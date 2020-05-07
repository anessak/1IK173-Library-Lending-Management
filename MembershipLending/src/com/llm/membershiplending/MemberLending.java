package com.llm.membershiplending;

import java.util.ArrayList;

public class MemberLending  {
    private int memberId;
    private ArrayList<LendingBasketEntity> bookItemIdsWithDate;

    public MemberLending(int mid)
    {
        this.memberId=mid;
        this.bookItemIdsWithDate=new ArrayList<>();
    }
    public MemberLending(int mid, ArrayList<LendingBasketEntity> bookItemIds){
        this.memberId=mid;
        this.bookItemIdsWithDate=bookItemIds;
    }

    public int getMemberId() {
        return memberId;
    }
    public ArrayList<LendingBasketEntity> getBookItemsIdWithDate() {
        return this.bookItemIdsWithDate;
    }


    public void addBookItem(LendingBasketEntity bookItemId){
        if (this.bookItemIdsWithDate==null)
            this.bookItemIdsWithDate=new ArrayList<>();
        this.bookItemIdsWithDate.add(bookItemId);
    }


}
