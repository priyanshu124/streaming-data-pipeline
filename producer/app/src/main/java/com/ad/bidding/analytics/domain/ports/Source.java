package com.ad.bidding.analytics.domain.ports;

import com.ad.bidding.analytics.domain.model.BlobObject;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

public interface Source {
    // List objects under prefix
    List<BlobObject> listObjects( String prefix);

    // Streaming list (for large datasets)
    Stream<BlobObject> listObjectsAsStream(String prefix);

    // Read single object as stream
    InputStream readObjectAsStream(BlobObject object);

    // Copy object (can change bucket + key)
    void copyObject(
            String sourceKey, 
            String destBucket, 
            String destKey
    );
}

