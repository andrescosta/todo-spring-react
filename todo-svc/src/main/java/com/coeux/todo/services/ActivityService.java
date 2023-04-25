package com.coeux.todo.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coeux.todo.data.ActivityRepository;
import com.coeux.todo.data.MUserRepository;
import com.coeux.todo.entities.Activity;
import com.coeux.todo.entities.ActivityType;

@Service
public class ActivityService {

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    MUserRepository muserRepository;

    public List<Activity> getActivitiesByMUser(UUID publicId) {
        return activityRepository.getActivitiesByUser(publicId);
    }

    public Activity saveActivity(UUID publicId, Activity activity) {
        var user = muserRepository.getOrAddMUser(publicId);
        return activityRepository.saveActivity(user, activity);
    }

    public void deleteActivity(UUID muserPublicId, UUID publicId) {
        var activity = getActivityByPublicId(muserPublicId, publicId);
        if (activity != null) {
            activityRepository.deleteActivity(publicId);
        }
    }

    public Activity getActivityByPublicId(UUID muserPublicId, UUID publicId) {
        return activityRepository.getActivityByPublicId(muserPublicId, publicId);
    }

    public List<Activity> getActivitiesByType(UUID publicId, ActivityType type) {

        return activityRepository.getActivitiesByType(publicId, type);
    }
}
