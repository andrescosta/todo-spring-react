package com.coeux.todo.controllers;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coeux.todo.entities.Activity;
import com.coeux.todo.entities.ActivityType;
import com.coeux.todo.services.ActivityService;

import io.micrometer.observation.annotation.Observed;

@RestController
@RequestMapping("/v1/activities")
public class ActivityController {

    @Autowired
    ActivityService service;

    final private static Logger log = LoggerFactory.getLogger(ActivityController.class);

    @GetMapping
    @Observed(name = "user.activities", contextualName = "getting-user-activities", lowCardinalityKeyValues = {
            "userType", "userType2" })
    //TODO: remove userType
    public List<Activity> getActivities(Principal principal) {
        UsernamePasswordAuthenticationToken principal1 = (UsernamePasswordAuthenticationToken) principal;
        UUID publicId = UUID.fromString(((User) principal1.getPrincipal()).getUsername());
        return service.getActivitiesByMUser(publicId);
    }

    @GetMapping("/users/{publicId}?type={type}")
    public List<Activity> getActivitiesByType(@PathVariable UUID publicId, @PathVariable String type) {
        return service.getActivitiesByType(publicId, ActivityType.valueOf(type));
    }

    @GetMapping("/{publicId}")
    public Activity getActivity(@PathVariable UUID publicId) {
        return service.getActivityByPublicId(publicId);
    }

    @PostMapping
    @Observed(name = "user.activities", contextualName = "new-user-activity", lowCardinalityKeyValues = {
        "userType", "userType2" })
    //TODO: remove userType
    public Activity postActivity(Principal principal, @RequestBody Activity activity) {
        log.debug("Activity:" + activity);
        UsernamePasswordAuthenticationToken principal1 = (UsernamePasswordAuthenticationToken) principal;
        UUID publicId = UUID.fromString(((User) principal1.getPrincipal()).getUsername());

        return service.saveActivity(publicId, activity);
    }

    @PatchMapping("/{publicId}")
    public Activity patchActivity(@RequestBody Activity activity, @PathVariable UUID publicId) {
        return null;
    }

    @DeleteMapping("/{publicId}")
    @Observed(name = "user.activities", contextualName = "del-user-activity", lowCardinalityKeyValues = {
        "userType", "userType2" })
    //TODO: remove userType
    public void deleteActivity(@PathVariable UUID publicId) {
        log.debug("Deleting id:" + publicId);
        service.deleteActivity(publicId);
    }

}
