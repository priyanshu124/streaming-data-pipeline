package com.rtbplatform.organizer;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class FileNameParser {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Parse filename to extract date and log type.
     * Expected format: logtype.YYYYMMDD.txt
     * Example: bid.20130311.txt → date=2013-03-11, logType=BID
     *
     * @param fileName the filename (without path)
     * @return ParsedFileName or null if filename doesn't match expected format
     */
    public ParsedFileName parse(String fileName) {
        if (!fileName.endsWith(".txt")) {
            return null;
        }

        String[] parts = fileName.replace(".txt", "").split("\\.");
        if (parts.length != 2) {
            return null;
        }

        String logTypeStr = parts[0].toUpperCase();
        String dateStr = parts[1];

        // Validate and convert date
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);
            String formattedDate = date.toString(); // YYYY-MM-DD

            return new ParsedFileName(formattedDate, logTypeStr);
        } catch (Exception e) {
            return null;
        }
    }
}
