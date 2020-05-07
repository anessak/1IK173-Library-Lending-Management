package com.librarylendingmanagement.infrastructure.events;

import java.util.UUID;

public class OnBookItemLended {

    private UUID id;
    public OnBookItemLended(UUID bookItem) {
        this.id=bookItem;
    }

    public UUID getId() {
        return id;
    }
}
