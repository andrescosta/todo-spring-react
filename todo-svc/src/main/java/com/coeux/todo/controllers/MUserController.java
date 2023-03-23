package com.coeux.todo.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coeux.todo.data.MUserRepository;
import com.coeux.todo.entities.Activity;
import com.coeux.todo.entities.MUser;

@RestController
@RequestMapping("/musers")
public class MUserController {

    @Autowired
    MUserRepository repository;

    @GetMapping
    public Activity[] getMUSers() {
        return null;
    }

    @GetMapping("/{user_id}")
    public MUser getMUser(@PathVariable UUID id) {
        return null;
    }

    @PostMapping
    public MUser postMUser(@RequestBody MUser user) {
        return repository.saveMUser(user);
    }

    @PatchMapping("/{muser_id}")
    public MUser patchMUser(@RequestBody MUser muser, @PathVariable UUID id) {
        return null;
    }

    @DeleteMapping("/{muser_id}")
    public MUser deleteMUser(@PathVariable UUID id) {
        return null;
    }

}
