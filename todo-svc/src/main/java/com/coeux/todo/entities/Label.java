package com.coeux.todo.entities;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Label(@JsonIgnore long id, UUID publicId, String name, String description, @JsonIgnore MUser muser) {

    public Label withIDs(long id, UUID publicId){
        return new Label(id, publicId, this.name(), this.description(), null);

    }
}
