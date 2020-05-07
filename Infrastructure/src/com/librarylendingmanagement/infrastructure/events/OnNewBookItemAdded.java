package com.librarylendingmanagement.infrastructure.events;

import java.util.UUID;

public class OnNewBookItemAdded {

    private UUID id;
    public OnNewBookItemAdded(UUID bookItem) {
        this.id=bookItem;
    }
}
