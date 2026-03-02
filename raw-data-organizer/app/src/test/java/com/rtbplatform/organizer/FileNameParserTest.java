package com.rtbplatform.organizer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileNameParserTest {

    private final FileNameParser parser = new FileNameParser();

    @Test
    void testParseValidFile() {
        ParsedFileName result = parser.parse("bid.20130311.txt");
        assertNotNull(result);
        assertEquals("2013-03-11", result.getDate());
        assertEquals("BID", result.getLogType());
    }

    @Test
    void testParseConvertLogTypeToUppercase() {
        ParsedFileName result = parser.parse("impression.20231225.txt");
        assertEquals("IMPRESSION", result.getLogType());
        assertEquals("2023-12-25", result.getDate());
    }

    @Test
    void testParseInvalidExtension() {
        assertNull(parser.parse("bid.20130311.csv"));
    }

    @Test
    void testParseInvalidDate() {
        assertNull(parser.parse("bid.20131332.txt"));
    }

    @Test
    void testParseMissingDot() {
        assertNull(parser.parse("bid20130311.txt"));
    }

    @Test
    void testParseInvalidFormat() {
        assertNull(parser.parse("invalid.txt"));
    }
}
