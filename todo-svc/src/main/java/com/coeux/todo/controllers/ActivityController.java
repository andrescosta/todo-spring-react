package com.coeux.todo.controllers;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coeux.todo.data.ActivityRepository;
import com.coeux.todo.entities.Activity;
import com.coeux.todo.entities.ActivityType;

import io.micrometer.observation.annotation.Observed;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    @Autowired
    ActivityRepository repository;

    Logger logger = LoggerFactory.getLogger(ActivityController.class);

    @GetMapping
    @CrossOrigin(origins = "http://localhost:8080")
    @Observed(name = "user.activities", contextualName = "getting-user-activities", lowCardinalityKeyValues = {
            "userType", "userType2" })
    //TODO: remove userType
    public List<Activity> getActivities(Principal principal) {
        UsernamePasswordAuthenticationToken principal1 = (UsernamePasswordAuthenticationToken) principal;
        UUID publicId = UUID.fromString(((User) principal1.getPrincipal()).getUsername());
        return repository.getActivitiesByUser(publicId);
    }

    @GetMapping("/users/{publicId}?type={type}")
    @CrossOrigin(origins = "http://localhost:8080")
    public List<Activity> getActivitiesByType(@PathVariable UUID publicId, @PathVariable String type) {
        return repository.getActivitiesByType(publicId, ActivityType.valueOf(type));
    }

    @GetMapping("/{publicId}")
    @CrossOrigin(origins = "http://localhost:8080")
    public Activity getActivity(@PathVariable UUID publicId) {
        return repository.getActivityByPublicId(publicId);
    }

    @PostMapping
    @CrossOrigin(origins = "http://localhost:8080")
    public Activity postActivity(Principal principal, @RequestBody Activity activity) {
        UsernamePasswordAuthenticationToken principal1 = (UsernamePasswordAuthenticationToken) principal;
        UUID publicId = UUID.fromString(((User) principal1.getPrincipal()).getUsername());

        return repository.saveActivity(publicId, activity);
    }

    @PatchMapping("/{publicId}")
    @CrossOrigin(origins = "http://localhost:8080")
    public Activity patchActivity(@RequestBody Activity activity, @PathVariable UUID publicId) {
        return null;
    }

    @DeleteMapping("/{publicId}")
    @CrossOrigin(origins = "http://localhost:8080")
    public void deleteActivity(@PathVariable UUID publicId) {
        repository.deleteActivity(publicId);
    }

}
