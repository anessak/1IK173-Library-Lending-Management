package com.librarylendingmanagement.infrastructure.events;

import java.util.UUID;

public class OnNewBookItemAdded {

    private UUID BookItem;

    public OnNewBookItemAdded(UUID bookItem) {
        this.BookItem = bookItem;
    }
}
