package com.ad.bidding.analytics.adapters.source;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.minio.*;
import io.minio.messages.Item;

import com.ad.bidding.analytics.domain.Exceptions.SourceException;
import com.ad.bidding.analytics.domain.model.BlobObject;
import com.ad.bidding.analytics.domain.ports.Source;

public class MinioSource implements Source {

    private static final Logger log = LoggerFactory.getLogger(MinioSource.class);
    private final MinioClient client;
    private final String bucket;

    public MinioSource(MinioClient client, String bucket) {
        this.client = client;
        this.bucket = bucket;
    }

    @Override
    public List<BlobObject> listObjects(String prefix) {
        List<BlobObject> objects = new ArrayList<>();
        try {
            Iterable<Result<Item>> obj = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(prefix != null ? prefix : "")
                            .recursive(true)
                            .build()
            );
            for (Result<Item> r : obj) {
                Item item = r.get();
                objects.add(new BlobObject(bucket, item.objectName(), item.size(), item.isDir()));
            }
        } catch (Exception e) {
            log.error("Failed to list objects in bucket {} with prefix {}", bucket, prefix, e);
            throw new SourceException("Failed to list objects", e);
        }
        return objects;
    }


    @Override
    public void copyObject(String sourceKey, String destBucket, String destKey) {
        try {
            log.info("Copying object {} -> {}/{}", sourceKey, destBucket, destKey);
            client.copyObject(CopyObjectArgs.builder()
                    .source(io.minio.CopySource.builder()
                            .bucket(bucket)
                            .object(sourceKey)
                            .build())
                    .bucket(destBucket)
                    .object(destKey)
                    .build());
            log.info("Copy successful: {} -> {}/{}", sourceKey, destBucket, destKey);
        } catch (Exception e) {
            log.error("Failed to copy object {} -> {}/{}", sourceKey, destBucket, destKey, e);
            throw new SourceException("Failed to copy object", e);
        }
    }

    @Override
    public InputStream readObjectAsStream(BlobObject object) {
        try {
            log.debug("Reading object as stream: {}/{}", object.getBucket(), object.getObjectKey());
            return client.getObject(
                    GetObjectArgs.builder()
                            .bucket(object.getBucket())
                            .object(object.getObjectKey())
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to read object as stream: {}/{}", object.getBucket(), object.getObjectKey(), e);
            throw new SourceException("Failed to read object as stream", e);
        }
    }

    @Override
    public Stream<BlobObject> listObjectsAsStream(String prefix) {
        try {
        Iterable<Result<Item>> results = client.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix != null ? prefix : "")
                        .recursive(true)
                        .build()
        );

            // Convert Iterable<Result<Item>> to Stream<BlobObject>
            return StreamSupport.stream(results.spliterator(), false)
                    .map(result -> {
                        try {
                            Item item = result.get();
                            return new BlobObject(bucket, item.objectName(), item.size(), item.isDir());
                        } catch (Exception e) {
                            log.error("Error reading item from MinIO", e);
                            throw new SourceException("Error reading item from MinIO",  e);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to stream objects for prefix {}", prefix, e);
            throw new SourceException("Failed to stream objects", e);
        }
    }

}
