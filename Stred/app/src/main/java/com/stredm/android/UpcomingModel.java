package com.stredm.android;

import java.util.List;

/**
 * Created by oscarlafarga on 9/19/14.
 */
public class UpcomingModel {

    public List<Model> soonestEvents;
    public List<Model> closestEvents;

    public UpcomingModel(List<Model> soonestEvents, List<Model> closestEvents) {
        this.soonestEvents = soonestEvents;
        this.closestEvents = closestEvents;
    }
}
