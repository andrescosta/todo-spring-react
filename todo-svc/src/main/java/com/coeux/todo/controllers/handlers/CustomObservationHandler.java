package com.coeux.todo.controllers.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;

@Component
class CustomObservationHandler implements ObservationHandler<Observation.Context> {

    private static final Logger log = LoggerFactory.getLogger(CustomObservationHandler.class);

    private boolean checkContext(String name) {
        return name!=null && name.equals("user.activities");
    }

    @Override
    public void onStart(Observation.Context context) {
        if (!checkContext(context.getName())) return;
        log.info("Before running the observation for context [{}]", context.getName());
    }

    @Override
    public void onStop(Observation.Context context) {
        if (!checkContext(context.getName())) return;
        log.info("After running the observation for context [{}], userType [{}]", context.getName());
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return true;
    }

    /*private String getUserTypeFromContext(Observation.Context context) {
        return StreamSupport.stream(context.getLowCardinalityKeyValues().spliterator(), false)
                .filter(keyValue -> "userType".equals(keyValue.getKey()))
                .map(KeyValue::getValue)
                .findFirst()
                .orElse("UNKNOWN");
    }*/
}