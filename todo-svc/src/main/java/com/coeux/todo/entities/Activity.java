package com.coeux.todo.entities;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore; 

public record Activity(@JsonIgnore long id, UUID publicId, String name, String description,
                 MUser muser, ActivityType type, ActivityState state,
                ActivityStatus status, String[] tags, Media[] media, Label[] labels,
                Map<String, Object> extraData) {

                    public Activity withIDs(long id, UUID publicId){
                        return new Activity(id, publicId, this.name(), this.description(), null,
                        this.type(), this.state(), this.status(),
                        this.tags(), this.media, this.labels(), this.extraData());
                    }
                    public Activity withMedia(Media[] media){
                        return new Activity(this.id(), this.publicId(), this.name(), this.description(), null,
                        this.type(), this.state(), this.status(),
                        this.tags(), media, this.labels(), this.extraData());
                    }
                    public Activity withLabels(Label[] labels){
                        return new Activity(this.id(), this.publicId(), this.name(), this.description(), null,
                        this.type(), this.state(), this.status(),
                        this.tags(), this.media(), labels, this.extraData());
                    }

}
