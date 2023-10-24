package com.coeux.todo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coeux.todo.data.ActivityRepository;
import com.coeux.todo.data.LabelRepository;
import com.coeux.todo.data.MUserRepository;
import com.coeux.todo.data.MediaRepository;
import com.coeux.todo.entities.Activity;
import com.coeux.todo.entities.ActivityType;
import com.coeux.todo.entities.Label;
import com.coeux.todo.entities.Media;

@Service
public class ActivityService {

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    MUserRepository muserRepository;

    @Autowired
    LabelRepository labelRepository;

    @Autowired
    MediaRepository mediaRepository;

    public List<Activity> getActivitiesByMUser(UUID publicId) {
        return activityRepository.getActivitiesByUser(publicId);
    }

    public Activity saveActivity(UUID publicId, Activity activity) {
        var user = muserRepository.getOrAddMUser(publicId);
        var newactivity = activityRepository.saveActivity(user, activity);

        if (activity.media() != null) {
            var newMedia = mediaRepository.saveMedia(newactivity.id(), activity.media());
            newactivity = newactivity.withMedia(newMedia);
        }
        if (activity.labels() != null) {
            labelRepository.associateLabels(newactivity.id(), activity.labels());
        }

        return newactivity;
    }

    public void deleteActivity(UUID muserPublicId, UUID publicId) {
        var activity = getActivityByPublicId(muserPublicId, publicId);
        if (activity != null) {
            activityRepository.deleteActivity(publicId);
        }
    }

    public Activity getActivityByPublicId(UUID muserPublicId, UUID publicId) {
        return fillRelations(activityRepository.getActivityByPublicId(muserPublicId, publicId));
    }

    public List<Activity> getActivitiesByType(UUID publicId, ActivityType type) {

        return fillRelations(activityRepository.getActivitiesByType(publicId, type));
    }

    protected List<Activity> fillRelations(List<Activity> activities) {
        List<Activity> l = new ArrayList<>();
        for (var activity : activities){
            l.add(fillRelations(activity));
        }
        return l;
    }

    protected Activity fillRelations(Activity activity) {

        var media = mediaRepository.getMediaByActivity(activity.id()).toArray(Media[]::new);
        var labels = labelRepository.getLabelsByActivity(activity.id()).toArray(Label[]::new);

        activity = activity.withMedia(media);
        activity = activity.withLabels(labels);

        return activity;
    }
}
