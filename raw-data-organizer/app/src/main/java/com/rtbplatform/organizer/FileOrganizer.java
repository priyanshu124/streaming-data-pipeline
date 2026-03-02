package com.rtbplatform.organizer;

import io.minio.*;
import io.minio.messages.Item;
import org.springframework.stereotype.Service;

import com.rtbplatform.organizer.config.OrganizerProperties;

import java.io.InputStream;

@Service
public class FileOrganizer {

    private final MinioClient minioClient;
    private final OrganizerProperties properties;
    private final FileNameParser fileNameParser;

    public FileOrganizer(MinioClient minioClient, OrganizerProperties properties, FileNameParser fileNameParser) {
        this.minioClient = minioClient;
        this.properties = properties;
        this.fileNameParser = fileNameParser;
    }

    public int organize() throws Exception {
        OrganizerProperties.SourceConfig source = properties.getSource();
        OrganizerProperties.DestinationConfig dest = properties.getDestination();

        String sourceBucket = source.getBucket();
        String sourcePrefix = source.getPrefix();
        String destBucket = dest.getBucket();
        String destPrefix = dest.getPrefix();

        int filesOrganized = 0;

        // List all source files
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(sourceBucket)
                        .prefix(sourcePrefix)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();
            if (item.isDir()) continue;

            String objectKey = item.objectName();
            String fileName = objectKey.substring(objectKey.lastIndexOf('/') + 1);

            // Parse filename to extract date and log type
            ParsedFileName parsed = fileNameParser.parse(fileName);
            if (parsed == null) {
                System.out.println("Skipping unrecognized file: " + fileName);
                continue;
            }

            // Build destination path: dt=YYYY-MM-DD/log_type=XXX/filename
            String destPath = String.format("%s/dt=%s/log_type=%s/%s",
                    destPrefix,
                    parsed.getDate(),
                    parsed.getLogType(),
                    fileName
            );

            // Copy file to destination
            copyFile(sourceBucket, objectKey, destBucket, destPath);
            filesOrganized++;

            System.out.println("Organized: " + fileName + " → " + destPath);
        }

        return filesOrganized;
    }

    private void copyFile(String sourceBucket, String sourceKey, String destBucket, String destKey) throws Exception {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(sourceBucket)
                        .object(sourceKey)
                        .build()
        )) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(destBucket)
                            .object(destKey)
                            .stream(stream, -1, 10485760) // 10MB part size
                            .build()
            );
        }
    }
}
