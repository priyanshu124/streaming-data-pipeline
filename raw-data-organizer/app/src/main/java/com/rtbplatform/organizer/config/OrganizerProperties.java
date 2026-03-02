package com.rtbplatform.organizer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rtb.organizer")
public class OrganizerProperties {

    private SourceConfig source = new SourceConfig();
    private DestinationConfig destination = new DestinationConfig();

    public SourceConfig getSource() {
        return source;
    }

    public void setSource(SourceConfig source) {
        this.source = source;
    }

    public DestinationConfig getDestination() {
        return destination;
    }

    public void setDestination(DestinationConfig destination) {
        this.destination = destination;
    }

    public static class SourceConfig {
        private String bucket;
        private String prefix;

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }

    public static class DestinationConfig {
        private String bucket;
        private String prefix;

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }
}
