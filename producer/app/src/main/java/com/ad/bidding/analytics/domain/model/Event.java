package com.ad.bidding.analytics.domain.model;

public class Event {
    private final EventType type;
    private final Object payload;
    private final long eventTime;

    public Event(EventType type, Object payload, long eventTime) {
        this.type = type;
        this.payload = payload;
        this.eventTime = eventTime;
    }

    public EventType getType() { return type; }
    public Object getPayload() { return payload; }
    public long getEventTime() { return eventTime; }
}
