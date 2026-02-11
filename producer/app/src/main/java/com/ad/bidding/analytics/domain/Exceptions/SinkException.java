package com.ad.bidding.analytics.domain.Exceptions;

public class SinkException extends RuntimeException {
    public SinkException(String message, Throwable cause) {
        super(message, cause);
    }
}

