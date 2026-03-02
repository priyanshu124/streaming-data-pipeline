package com.rtbplatform.organizer;

import com.rtbplatform.organizer.config.OrganizerProperties;
import io.minio.*;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileOrganizerTest {

    @Mock
    private MinioClient minioClient;

    private FileNameParser fileNameParser;
    private OrganizerProperties properties;
    private FileOrganizer fileOrganizer;

    @BeforeEach
    void setUp() {
        fileNameParser = new FileNameParser();
        
        // Set up properties
        properties = new OrganizerProperties();
        OrganizerProperties.SourceConfig source = new OrganizerProperties.SourceConfig();
        source.setBucket("raw-data");
        source.setPrefix("raw/");
        
        OrganizerProperties.DestinationConfig dest = new OrganizerProperties.DestinationConfig();
        dest.setBucket("rtb-analytics");
        dest.setPrefix("organized");
        
        properties.setSource(source);
        properties.setDestination(dest);
        
        fileOrganizer = new FileOrganizer(minioClient, properties, fileNameParser);
    }

    @Test
    void testOrganizeCompressedBz2Files() throws Exception {
        // Arrange: Mock files that should NOT be skipped
        List<String> fileNames = Arrays.asList(
            "imp.20131019.txt.bz2",
            "bid.20130311.txt.bz2",
            "clk.20130312.txt.bz2",
            "conv.20131025.txt.bz2"
        );
        
        mockMinioListObjects(fileNames);
        mockMinioCopyOperations();
        
        // Act
        int filesOrganized = fileOrganizer.organize();
        
        // Assert: The key test - all 4 compressed .bz2 files should be organized, NOT skipped
        assertEquals(4, filesOrganized, "All 4 compressed .txt.bz2 files should be organized (NOT skipped)");
        
        // Verify putObject was called for each file
        verify(minioClient, times(4)).putObject(any(PutObjectArgs.class));
        verify(minioClient, times(4)).getObject(any(GetObjectArgs.class));
    }

    @Test
    void testOrganizeUncompressedTxtFiles() throws Exception {
        // Arrange
        List<String> fileNames = Arrays.asList(
            "bid.20130311.txt",
            "clk.20130312.txt"
        );
        
        mockMinioListObjects(fileNames);
        mockMinioCopyOperations();
        
        // Act
        int filesOrganized = fileOrganizer.organize();
        
        // Assert
        assertEquals(2, filesOrganized, "Both uncompressed .txt files should be organized");
        verify(minioClient, times(2)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void testOrganizeMixedCompressedAndUncompressed() throws Exception {
        // Arrange
        List<String> fileNames = Arrays.asList(
            "bid.20130311.txt",
            "bid.20130312.txt.bz2",
            "imp.20131019.txt.bz2",
            "clk.20130315.txt"
        );
        
        mockMinioListObjects(fileNames);
        mockMinioCopyOperations();
        
        // Act
        int filesOrganized = fileOrganizer.organize();
        
        // Assert
        assertEquals(4, filesOrganized, "All mixed format files should be organized");
        verify(minioClient, times(4)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void testSkipsInvalidFiles() throws Exception {
        // Arrange: Mix of valid and invalid files
        List<String> fileNames = Arrays.asList(
            "bid.20130311.txt.bz2",  // Valid
            "invalid.csv",           // Invalid extension
            "noDot20130311.txt",     // Invalid format
            "clk.20130312.txt.bz2"   // Valid
        );
        
        mockMinioListObjects(fileNames);
        mockMinioCopyOperations();
        
        // Act
        int filesOrganized = fileOrganizer.organize();
        
        // Assert
        assertEquals(2, filesOrganized, "Only 2 valid files should be organized");
        verify(minioClient, times(2)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void testDestinationPathFormat() throws Exception {
        // Arrange
        List<String> fileNames = Arrays.asList("imp.20131019.txt.bz2");
        
        mockMinioListObjects(fileNames);
        mockMinioCopyOperations();
        
        // Act
        int filesOrganized = fileOrganizer.organize();
        
        // Assert: File should be organized
        assertEquals(1, filesOrganized, "imp.20131019.txt.bz2 should be successfully organized");
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void testEmptyBucket() throws Exception {
        // Arrange
        mockMinioListObjects(Arrays.asList());
        
        // Act
        int filesOrganized = fileOrganizer.organize();
        
        // Assert
        assertEquals(0, filesOrganized, "No files should be organized from empty bucket");
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
    }

    @Test
    void testSkipsDirectories() throws Exception {
        // Arrange: Create a mock with a directory item
        Iterable<Result<Item>> results = createMockResultsWithDirectory();
        
        when(minioClient.listObjects(any(ListObjectsArgs.class)))
            .thenReturn(results);
        mockMinioCopyOperations();
        
        // Act
        int filesOrganized = fileOrganizer.organize();
        
        // Assert
        assertEquals(0, filesOrganized, "Directories should be skipped");
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
    }

    // Helper methods

    private void mockMinioListObjects(List<String> fileNames) throws Exception {
        Iterable<Result<Item>> results = createMockResults(fileNames);
        
        when(minioClient.listObjects(any(ListObjectsArgs.class)))
            .thenReturn(results);
    }

    private void mockMinioCopyOperations() throws Exception {
        // Mock getObject to return a GetObjectResponse (which extends InputStream)
        GetObjectResponse mockResponse = mock(GetObjectResponse.class, withSettings().lenient());
        ByteArrayInputStream dummyStream = new ByteArrayInputStream("test data".getBytes());
        
        // Make the mock response delegate to the actual stream
        lenient().when(mockResponse.read()).thenAnswer(inv -> dummyStream.read());
        lenient().when(mockResponse.read(any(byte[].class))).thenAnswer(inv -> dummyStream.read(inv.getArgument(0)));
        lenient().when(mockResponse.read(any(byte[].class), anyInt(), anyInt()))
            .thenAnswer(inv -> dummyStream.read(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2)));
        lenient().doNothing().when(mockResponse).close();
        
        lenient().when(minioClient.getObject(any(GetObjectArgs.class)))
            .thenReturn(mockResponse);
        
        // Mock putObject to succeed
        lenient().when(minioClient.putObject(any(PutObjectArgs.class)))
            .thenReturn(null);
    }

    private Iterable<Result<Item>> createMockResults(List<String> fileNames) {
        return fileNames.stream()
            .map(fileName -> {
                try {
                    Item item = mock(Item.class);
                    when(item.objectName()).thenReturn("raw/" + fileName);
                    when(item.isDir()).thenReturn(false);
                    when(item.lastModified()).thenReturn(ZonedDateTime.now());
                    when(item.size()).thenReturn(1024L);
                    
                    Result<Item> result = mock(Result.class);
                    when(result.get()).thenReturn(item);
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .toList();
    }

    private Iterable<Result<Item>> createMockResultsWithDirectory() throws Exception {
        Item dirItem = mock(Item.class);
        when(dirItem.objectName()).thenReturn("raw/somedir/");
        when(dirItem.isDir()).thenReturn(true);
        
        Result<Item> result = mock(Result.class);
        when(result.get()).thenReturn(dirItem);
        
        return Arrays.asList(result);
    }
}
