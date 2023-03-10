package com.coeux.todo.entities;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;


public record Media(
@JsonIgnore long id, UUID publicId,
String name, String description,
MediaType type, URI uri,@JsonIgnore ExtraInfo extraInfo,
Map<String, Object> extraData)
{

}
