package com.rtbplatform.organizer;

public class ParsedFileName {
    private final String date;        // YYYY-MM-DD
    private final String logType;     // BID, IMPRESSION, CLICK, CONVERSION

    public ParsedFileName(String date, String logType) {
        this.date = date;
        this.logType = logType;
    }

    public String getDate() {
        return date;
    }

    public String getLogType() {
        return logType;
    }
}
