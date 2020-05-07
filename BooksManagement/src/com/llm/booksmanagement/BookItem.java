package com.llm.booksmanagement;

import java.time.LocalDateTime;
import java.util.UUID;

public class BookItem {
    private UUID id;
    private ItemType itemType;
    private LocalDateTime dateAdded;
    private BookItemState itemState;
    private BookTitle referencedBook;
    public BookItem()
    {
        this.id=UUID.randomUUID();
        this.dateAdded= LocalDateTime.now();
    }
    public BookItem(UUID id, ItemType type){
        this.id=id;
        this.itemType=type;
        this.dateAdded= LocalDateTime.now();
        this.itemState=BookItemState.HomeInLibrary;
    }
    public BookItem(UUID id, ItemType type, BookItemState state, LocalDateTime date,BookTitle bookRef){
        this(id,type);
        this.itemState=state;
        this.dateAdded=date;
        this.referencedBook=bookRef;
    }

    public UUID getId() {
        return id;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public BookItemState getItemState() {
        return itemState;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public BookTitle getReferencedBook() {
        return referencedBook;
    }
}
