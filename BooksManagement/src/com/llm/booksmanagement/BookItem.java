package com.llm.booksmanagement;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class BookItem {
    private UUID id;
    private ItemType itemType;
    private Date dateAdded;
    private BookItemState itemState;
    public BookItem()
    {
        this.id=UUID.randomUUID();
        this.dateAdded= Calendar.getInstance().getTime();
    }
    public BookItem(UUID id, ItemType type, BookItemState state){
        this.id=id;
        this.itemType=type;
        this.dateAdded= Calendar.getInstance().getTime();
        this.itemState=state;
    }
}
