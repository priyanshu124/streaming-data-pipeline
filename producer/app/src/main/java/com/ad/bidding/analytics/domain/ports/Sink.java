package com.ad.bidding.analytics.domain.ports;

import com.ad.bidding.analytics.domain.model.Event;

public interface Sink {
    void send(Event event);

}
