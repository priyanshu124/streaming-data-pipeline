package com.ad.bidding.analytics.adapters.blobHandlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ad.bidding.analytics.domain.model.BlobHandler;
import com.ad.bidding.analytics.domain.model.BlobObject;
import com.ad.bidding.analytics.domain.ports.Source;


public class ReorganizeByDate implements BlobHandler {

    private static final Logger log = LoggerFactory.getLogger(ReorganizeByDate.class);
    private static final Pattern DATE_PATTERN = Pattern.compile(".*?(\\d{8}).*");

    private final Source source;
    private final String destBucket;
    private final String destPrefix;

    public ReorganizeByDate(Source source,
                                 String destBucket,
                                 String destPrefix) {
        this.source = source;
        this.destBucket = destBucket;
        this.destPrefix = destPrefix;
    }

    @Override
    public void handle(BlobObject blob) {
        // Implementation to reorganize the blob by date
        String filename = blob.getFileName();
        Matcher matcher = DATE_PATTERN.matcher(filename);

        if (!matcher.matches()) {
            log.warn("No date found in filename {}, skipping", filename);
            return;
        }

        String date = matcher.group(1);
        String newKey = destPrefix + "/" + date + "/" + filename;

        log.debug("Copying {} -> {}/{}", blob.getObjectKey(), destBucket, newKey);

        source.copyObject(
                blob.getObjectKey(),
                destBucket,
                newKey
        );

    }
    
}
