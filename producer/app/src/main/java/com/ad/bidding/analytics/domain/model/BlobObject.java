package com.ad.bidding.analytics.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString

public class BlobObject {

    private final String bucket;
    private final String objectKey;
    private final long size;
    private final boolean isDir;

    public String getFileName() {
        int idx = objectKey.lastIndexOf('/');
        return idx >= 0 ? objectKey.substring(idx + 1) : objectKey;
    }

    public String getParentPath() {
        int idx = objectKey.lastIndexOf('/');
        return idx >= 0 ? objectKey.substring(0, idx) : "";
    }
}