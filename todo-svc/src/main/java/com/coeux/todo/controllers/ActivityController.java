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
import com.coeux.todo.services.ActivityService;

import io.micrometer.observation.annotation.Observed;

@RestController
@RequestMapping("/v1/activities")
public class ActivityController {

    @Autowired
    ActivityService service;

    final private static Logger log = LoggerFactory.getLogger(ActivityController.class);

    @GetMapping
    @Observed(name = "get.user.activities", contextualName = "getting-user-activities" )
    public List<Activity> getActivities(Principal principal) {
        UUID publicId = getMUserPublicId(principal);
        return service.getActivitiesByMUser(publicId);
    }

    @GetMapping("/users/{publicId}?type={type}")
    public List<Activity> getActivitiesByType(Principal principal, @PathVariable UUID publicId,
            @PathVariable String type) {
        return null; //service.getActivitiesByType(publicId, ActivityType.valueOf(type));
    }

    @GetMapping("/{publicId}")
    @Observed(name = "get.one.user.activity", contextualName = "getting-one-activity")
    public Activity getActivity(Principal principal, @PathVariable UUID publicId) {
        UUID muserpublicId = getMUserPublicId(principal);
        return service.getActivityByPublicId(muserpublicId, publicId);
    }

    @PostMapping
    @Observed(name = "new.user.activities", contextualName = "new-user-activity", lowCardinalityKeyValues = {
            "userType", "userType2" })
    public Activity postActivity(Principal principal, @RequestBody Activity activity) {
        log.debug("Activity:" + activity);
        UUID publicId = getMUserPublicId(principal);

        var newactivity = service.saveActivity(publicId, activity);
        return newactivity;
    }

    @PatchMapping("/{publicId}")
    public Activity patchActivity(@RequestBody Activity activity, @PathVariable UUID publicId) {
        return null;
    }

    @DeleteMapping("/{publicId}")
    @Observed(name = "delete.user.activities", contextualName = "del-user-activity")
    public void deleteActivity(Principal principal, @PathVariable UUID publicId) {
        log.debug("Deleting id:" + publicId);
        UUID muserPublicId = getMUserPublicId(principal);
        service.deleteActivity(muserPublicId, publicId);
    }

    private UUID getMUserPublicId(Principal principal) {
        UsernamePasswordAuthenticationToken principal1 = (UsernamePasswordAuthenticationToken) principal;
        UUID muserPublicId = UUID.fromString(((User) principal1.getPrincipal()).getUsername());
        return muserPublicId;
    }

}
