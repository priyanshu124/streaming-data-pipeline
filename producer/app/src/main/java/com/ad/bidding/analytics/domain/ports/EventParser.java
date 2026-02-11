package com.ad.bidding.analytics.domain.ports;

import java.io.InputStream;
import java.util.stream.Stream;

import com.ad.bidding.analytics.domain.model.Event;
import com.ad.bidding.analytics.domain.model.EventType;

public interface EventParser {
    boolean supports(EventType type);
    Stream<Event> parse(InputStream input);

}
