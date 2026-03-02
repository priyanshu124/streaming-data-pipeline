package com.rtbplatform.organizer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify that compressed .txt.bz2 files are NOT skipped
 */
class FileNameParserIntegrationTest {

    private final FileNameParser parser = new FileNameParser();

    @Test
    void testCompressedBz2FilesAreNotSkipped() {
        // Test the exact file from the user's question: imp.20131019. txt.bz2
        ParsedFileName result = parser.parse("imp.20131019.txt.bz2");
        
        assertNotNull(result, "imp.20131019.txt.bz2 should NOT be skipped");
        assertEquals("2013-10-19", result.getDate());
        assertEquals("IMP", result.getLogType());
    }

    @Test
    void testMultipleCompressedFilesFromRealData() {
        // Test files that match the actual  data in MinIO
        String[] realFiles = {
            "bid.20130311.txt.bz2",
            "clk.20130311.txt.bz2",
            "imp.20130311.txt.bz2",
            "conv.20130311.txt.bz2",
            "bid.20131019.txt.bz2",
            "clk.20131019.txt.bz2",
            "imp.20131019.txt.bz2"
        };
        
        for (String fileName : realFiles) {
            ParsedFileName result = parser.parse(fileName);
            assertNotNull(result, fileName + " should NOT be skipped");
            assertTrue(result.getDate().matches("\\d{4}-\\d{2}-\\d{2}"), 
                "Date should be in YYYY-MM-DD format: " + result.getDate());
            assertTrue(result.getLogType().matches("[A-Z]+"),
                "Log type should be uppercase: " + result.getLogType());
        }
    }

    @Test
    void testBidFileNotSkipped() {
        ParsedFileName result = parser.parse("bid.20130311.txt.bz2");
        assertNotNull(result, "bid.20130311.txt.bz2 should NOT be skipped");
        assertEquals("2013-03-11", result.getDate());
        assertEquals("BID", result.getLogType());
    }

    @Test
    void testClkFileNotSkipped() {
        ParsedFileName result = parser.parse("clk.20130312.txt.bz2");
        assertNotNull(result, "clk.20130312.txt.bz2 should NOT be skipped");
        assertEquals("2013-03-12", result.getDate());
        assertEquals("CLK", result.getLogType());
    }

    @Test
    void testConvFileNotSkipped() {
        ParsedFileName result = parser.parse("conv.20131025.txt.bz2");
        assertNotNull(result, "conv.20131025.txt.bz2 should NOT be skipped");
        assertEquals("2013-10-25", result.getDate());
        assertEquals("CONV", result.getLogType());
    }

    @Test
    void testUncompressedFilesStillWork() {
        // Ensure uncompressed .txt files still work
        ParsedFileName result = parser.parse("bid.20130311.txt");
        assertNotNull(result, "bid.20130311.txt should still work");
        assertEquals("2013-03-11", result.getDate());
        assertEquals("BID", result.getLogType());
    }

    @Test
    void testInvalidFileIsSkipped() {
        // Invalid files should still return null (be skipped)
        assertNull(parser.parse("invalid.csv"), "invalid.csv should be skipped");
        assertNull(parser.parse("bid.20130311.pdf"), "bid.20130311.pdf should be skipped");
        assertNull(parser.parse("noDot.txt"), "noDot.txt should be skipped");
    }
}
