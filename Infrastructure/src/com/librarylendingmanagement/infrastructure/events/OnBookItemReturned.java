package com.librarylendingmanagement.infrastructure.events;

import java.util.UUID;

public class OnBookItemReturned {

    private UUID id;
    public OnBookItemReturned(UUID bookItem) {
        this.id=bookItem;
    }

    public UUID getId() {
        return id;
    }
}
