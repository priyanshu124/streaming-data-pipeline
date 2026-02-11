package com.ad.bidding.analytics.domain.ports;

import java.io.InputStream;

public interface FileDecoder {
    boolean supports(String objectKey);
    InputStream decode(InputStream input) throws Exception;

}
