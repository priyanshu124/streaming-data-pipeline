package com.ad.bidding.analytics;

import com.ad.bidding.analytics.adapters.blobHandlers.ReorganizeByDate;
import com.ad.bidding.analytics.adapters.source.MinioSource;
import com.ad.bidding.analytics.application.BlobReorganizationService;
import com.ad.bidding.analytics.domain.configs.SourceConfigs;

import io.minio.MinioClient;

public class App {
   public static void main(String[] args) {

        SourceConfigs sourceConfigs = new SourceConfigs();

        MinioClient client = MinioClient.builder()
                .endpoint(sourceConfigs.getEndpoint())
                .credentials(sourceConfigs.getAccessKey(), sourceConfigs.getSecretKey())
                .build();

        String sourceBucket = "ad-bidding-analytics-raw-data";
        String sourcePrefix = "ipinyou.contest.dataset";
        String destBucket = "ad-bidding-analytics";
        String destPrefix = "raw-layer";

        MinioSource minioSource = new MinioSource(client, sourceBucket);

        ReorganizeByDate handler =
                new ReorganizeByDate(minioSource, destBucket, destPrefix);

        BlobReorganizationService service =
                new BlobReorganizationService(
                        minioSource,       // root prefix
                        8,         // threads
                        handler
                );

        service.reorganize(sourcePrefix);
    } 
}
