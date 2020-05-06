package com.llm.membershiplending;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class LendingBasketEntity implements Comparable<LendingBasketEntity>  {
    private int memberId;
    private UUID bookItemId;
    private LocalDateTime lendingDate;
    public LendingBasketEntity(int mid, UUID bookItemId, LocalDateTime lendingDate){
        this.memberId=mid;
        this.bookItemId=bookItemId;
        this.lendingDate=lendingDate;
    }

    public int getMemberId() {
        return memberId;
    }

    public UUID getBookItemId() {
        return bookItemId;
    }

    public LocalDateTime getLendingDate() {
        return lendingDate;
    }

    @Override
    public int compareTo(LendingBasketEntity o) {
        return getLendingDate().compareTo(o.getLendingDate());
    }
}
