package com.coeux.todo.entities;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore; 

public record Activity(@JsonIgnore long id, UUID publicId, String name, String description,
                 MUser muser, ActivityType type, ActivityState state,
                ActivityStatus status, String[] tags, Media[] media, Label[] labels,
                Map<String, Object> extraData) {

   
}
