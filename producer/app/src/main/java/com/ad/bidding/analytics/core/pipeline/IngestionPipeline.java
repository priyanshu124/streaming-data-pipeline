package com.ad.bidding.analytics.core.pipeline;

import java.io.InputStream;

import com.ad.bidding.analytics.core.ports.BlobSource;
import com.ad.bidding.analytics.core.ports.EventParser;
import com.ad.bidding.analytics.core.ports.EventSink;
import com.ad.bidding.analytics.core.ports.FileDecoder;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IngestionPipeline {
    private final BlobSource source;
    private final FileDecoder decoder;
    private final EventParser parser;
    private final EventSink sink;

    public void run(String path) {
        try (InputStream raw = source.open(path);
             InputStream decoded = decoder.decode(raw)) {

            parser.parse(decoded).forEach(sink::publish);

        } catch (Exception e) {
            throw new RuntimeException("Pipeline failed", e);
        } finally {
            sink.flush();
        }
    }
        
}
