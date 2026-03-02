package com.rtbplatform.organizer;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class FileNameParser {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Parse filename to extract date and log type.
     * Expected format: logtype.YYYYMMDD.txt or logtype.YYYYMMDD.txt.bz2
     * Example: bid.20130311.txt → date=2013-03-11, logType=BID
     * Example: bid.20130311.txt.bz2 → date=2013-03-11, logType=BID
     *
     * @param fileName the filename (without path)
     * @return ParsedFileName or null if filename doesn't match expected format
     */
    public ParsedFileName parse(String fileName) {
        // Remove extension (.txt or .txt.bz2)
        String baseName;
        if (fileName.endsWith(".txt.bz2")) {
            baseName = fileName.replace(".txt.bz2", "");
        } else if (fileName.endsWith(".txt")) {
            baseName = fileName.replace(".txt", "");
        } else {
            return null;
        }

        String[] parts = baseName.split("\\.");
        if (parts.length != 2) {
            return null;
        }

        String logTypeStr = parts[0].toUpperCase();
        System.out.println("Parsed log type: " + logTypeStr);
        String dateStr = parts[1];
        System.err.println("Parsed date string: " + dateStr);

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
