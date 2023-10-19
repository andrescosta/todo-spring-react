package com.coeux.todo.entities;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record MUser(@JsonIgnore long id, UUID publicId, String name, String email, @JsonIgnore ExtraInfo extraInfo,
        Activity[] activities) {

    public MUser(long id) {
        this(id, null, null, null, null, null);
    }
    public MUser(UUID publicId) {
        this(0, publicId, null, null, null, null);
    }

    public MUser withIDs(long id, UUID publicId){
        return new MUser(id, publicId, this.name(), this.email(), null,
        null);
    }
}
