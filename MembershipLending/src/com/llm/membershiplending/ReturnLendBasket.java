package com.llm.membershiplending;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class ReturnLendBasket {
    private int memberId;
    private ArrayList<UUID> bookItemIds;
    private LocalDateTime returnDate;
    public ReturnLendBasket(int mid, ArrayList<UUID> bookItemsId, LocalDateTime returnDate){
        this.memberId=mid;
        this.bookItemIds=bookItemsId;
        this.returnDate=returnDate;
    }

    public int getMemberId() {
        return memberId;
    }

    public ArrayList<UUID> getBookItemIds() {
        return bookItemIds;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }
}
