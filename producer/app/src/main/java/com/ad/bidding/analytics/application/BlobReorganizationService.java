package com.ad.bidding.analytics.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ad.bidding.analytics.domain.model.BlobHandler;
import com.ad.bidding.analytics.domain.model.BlobObject;
import com.ad.bidding.analytics.domain.ports.Source;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class BlobReorganizationService {
    private static final Logger log = LoggerFactory.getLogger(BlobReorganizationService.class);
    
    private final Source source;
    private final int parallelism;
    private final BlobHandler handler;


    public BlobReorganizationService(Source source, int parallelism, BlobHandler handler) {
        this.source = source;
        this.parallelism = parallelism;
        this.handler = handler;
    }


    public void reorganize(String sourcePrefix) {
        log.info("Starting blob processing for prefix '{}' with {} threads",
                sourcePrefix, parallelism);

        ExecutorService executor = new ThreadPoolExecutor(
                parallelism,
                parallelism,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(parallelism * 10),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        try (Stream<BlobObject> stream = source.listObjectsAsStream(sourcePrefix)) {

            stream.filter(blob -> !blob.isDir())
                  .filter(blob -> blob.getObjectKey().contains("/training"))
                  .forEach(blob ->
                          executor.submit(() -> safeHandle(blob))
                  );

        } catch (Exception e) {
            log.error("Failed during streaming objects", e);
        } finally {
            shutdownExecutor(executor);
        }

        log.info("Blob processing completed");
    }

    private void safeHandle(BlobObject blob) {
        try {
            handler.handle(blob);
        } catch (Exception e) {
            log.error("Failed to process blob {}", blob.getObjectKey(), e);
        }
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("Forcing shutdown of executor");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for executor shutdown", e);
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

}


