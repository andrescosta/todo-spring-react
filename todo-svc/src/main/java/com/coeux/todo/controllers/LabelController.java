package com.coeux.todo.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coeux.todo.data.LabelRepository;
import com.coeux.todo.entities.Label;

@RestController
@RequestMapping("/labels")
public class LabelController {

    @Autowired
    LabelRepository repository;

    @GetMapping("/users/{publicId}")
    public List<Label> getLabels(@PathVariable UUID publicId) {
        return repository.getLabelsByMUSerId(publicId);
    }

    @PostMapping
    public Label postLabel(@RequestBody Label label) {
        return repository.saveLabel(label);
    }


}
