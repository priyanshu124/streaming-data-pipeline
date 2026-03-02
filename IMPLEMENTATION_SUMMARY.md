# Raw Data Organizer - Implementation Summary

## Overview

Successfully refactored the `producer/` module into a new `raw-data-organizer/` module with a clean hexagonal architecture. This module processes the complex iPinYou RTB dataset (4 schema variants across 6 seasons) and writes unified Parquet format to MinIO.

## Key Features Implemented

### 1. **Multi-Schema Support**
Handles all 4 iPinYou schema variants:
- **Schema A**: Season 1 bid logs (19 columns)
- **Schema B**: Season 2/3 bid logs (21 columns)  
- **Schema C**: Season 1 imp/clk/conv logs (22 columns)
- **Schema D**: Season 2/3 imp/clk/conv logs (24 columns)

### 2. **Unified Output Model**
Single `RawBidRecord` class represents all event types with proper null handling for schema-specific fields.

### 3. **Hexagonal Architecture**
```
Domain Layer:
├── Models (RawBidRecord, LogType, FileContext, IngestionReport)
├── Ports (RawFileReader, DataLakeWriter, TimestampParser)
└── Services (DataOrganizer, FileContextResolver)

Adapters:
├── Inbound (IpinyouFileReader, IpinyouTimestampParser)
└── Outbound (MinioParquetWriter, ManifestWriter)

Config & Infrastructure:
├── OrganizerProperties (YAML-driven config)
└── MinioBeansConfig (Spring integration)
```

### 4. **Spring Boot Integration**
- Configuration-driven via `application.yml`
- CommandLineRunner for K8s Job execution
- Proper dependency injection and bean management

## Project Structure

```
raw-data-organizer/
├── app/
│   ├── build.gradle                          # Gradle configuration
│   ├── src/main/java/com/rtbplatform/organizer/
│   │   ├── App.java                          # Spring Boot entry point
│   │   ├── adapter/
│   │   │   ├── inbound/
│   │   │   │   ├── IpinyouFileReader.java     # TSV parsing logic
│   │   │   │   └── IpinyouTimestampParser.java # Timestamp parsing
│   │   │   └── outbound/
│   │   │       ├── MinioParquetWriter.java    # Parquet writing
│   │   │       └── ManifestWriter.java        # Manifest JSON writing
│   │   ├── config/
│   │   │   ├── OrganizerProperties.java       # YAML config properties
│   │   │   └── MinioBeansConfig.java          # Spring bean definitions
│   │   └── domain/
│   │       ├── model/
│   │       │   ├── LogType.java               # Enum: BID, IMPRESSION, CLICK, CONVERSION
│   │       │   ├── RawBidRecord.java          # Unified domain model
│   │       │   ├── FileContext.java           # Value object: season/logtype/date
│   │       │   └── IngestionReport.java       # Metrics aggregation
│   │       ├── port/
│   │       │   ├── RawFileReader.java         # Inbound interface
│   │       │   ├── DataLakeWriter.java        # Outbound interface
│   │       │   └── TimestampParser.java       # Timestamp parsing interface
│   │       └── service/
│   │           ├── DataOrganizer.java         # Core orchestration
│   │           └── FileContextResolver.java   # Path → FileContext
│   └── src/main/resources/
│       └── application.yml                    # Spring Boot config
│   └── src/test/
│       ├── java/com/rtbplatform/organizer/
│       │   └── [Unit tests]
│       └── resources/
│           ├── sample_bid_s1.txt              # Season 1 bid sample
│           ├── sample_clk_s1.txt              # Season 1 click sample
│           └── sample_clk_s2.txt              # Season 2 click sample
├── build.gradle                              # Root Gradle config
├── settings.gradle                           # Gradle settings
├── gradle/wrapper/                           # Gradle wrapper
├── Dockerfile                                # Multi-stage Docker build
├── k8s/job.yml                               # Kubernetes Job manifest
└── README.md                                 # Module documentation
```

## Core Components

### RawBidRecord (Domain Model)
Unified representation of all iPinYou events:
```java
String bidId
long eventTimestampMs
String logType              // "BID", "IMPRESSION", "CLICK", "CONVERSION"
int season                  // 1, 2, or 3
String ipinyouId           
String userAgent
String ipAddress
//... 20+ more fields with proper null handling
```

Builder pattern for clean construction:
```java
new RawBidRecord.Builder()
    .bidId(bidId)
    .eventTimestampMs(epochMs)
    .logType("BID")
    .season(1)
    // ... set all fields
    .build()
```

### IpinyouFileReader (File Parsing)
- Reads tab-separated TSV files
- Auto-detects schema variant from FileContext (season + logType)
- Dispatches to appropriate parser:
  - `parseBidLog()` for BID logs (Schema A/B)
  - `parseImpClkConvLog()` for IMP/CLK/CONV logs (Schema C/D)
- Handles null fields: empty string "" or literal "null" → null
- Converts timestamp "20130311172101557" → epoch milliseconds

### FileContextResolver (Path Parsing)
Extracts metadata from file paths:
```
/data/ipinyou/training1st/bid.20130311.txt
                          ↓
FileContext(season=1, LogType.BID, date=2013-03-11)
```

### DataOrganizer (Core Service)
Orchestrates the full pipeline:
1. Scans all season directories
2. Reads and parses files (single-threaded)
3. Partitions records by (date, logType)
4. Writes partitions to MinIO in parallel (8 threads by default)
5. Generates ingestion manifest

### MinioParquetWriter (Output Adapter)
- Converts RawBidRecord → Avro GenericRecord
- Writes Parquet format to MinIO
- S3 key structure: `ipinyou/v1/dt=YYYY-MM-DD/log_type=BID/part_0000.parquet`

## Configuration

### application.yml
```yaml
rtb:
  organizer:
    source:
      base-path: /data/ipinyou
      directories: [training1st, training2nd, training3rd, ...]
    minio:
      endpoint: http://minio.minio.svc.cluster.local:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket: rtb-raw-data
      base-prefix: ipinyou/v1
    parquet:
      row-group-size: 134217728  # 128 MB
      compression: SNAPPY
    validation:
      skip-malformed-rows: true
      log-malformed-rows: true
```

### Environment Variables
Override YAML properties via env vars:
```bash
export SOURCE_BASE_PATH=/data/ipinyou
export MINIO_ENDPOINT=http://minio:9000
export MINIO_ACCESS_KEY=minioadmin
export MINIO_SECRET_KEY=minioadmin
export MINIO_BUCKET=rtb-raw-data
```

## Build & Run

### Gradle Build
```bash
cd raw-data-organizer
gradle clean build      # Compile, test, package
gradle bootJar          # Create executable JAR
```

### Docker Build
```bash
docker build -t raw-data-organizer:latest .
docker run --rm \
  -e SOURCE_BASE_PATH=/data \
  -e MINIO_ENDPOINT=http://minio:9000 \
  -v /path/to/ipinyou:/data \
  raw-data-organizer:latest
```

### Kubernetes Deployment
```bash
# Ensure MinIO credentials secret exists
kubectl create secret generic minio-credentials \
  --from-literal=access-key=minioadmin \
  --from-literal=secret-key=minioadmin

# Deploy job
kubectl apply -f raw-data-organizer/k8s/job.yml

# Monitor
kubectl logs -f job/raw-data-organizer
kubectl describe job raw-data-organizer
```

## Testing

### Unit Tests
```bash
gradle test

# Run specific test
gradle test --tests IpinyouFileReaderTest
```

### Test Fixtures
Sample data files in `app/src/test/resources/`:
- `sample_bid_s1.txt` - 2 Season 1 bid records (19 cols)
- `sample_clk_s1.txt` - 2 Season 1 click records (22 cols)
- `sample_clk_s2.txt` - 2 Season 2 click records (24 cols)

Tests verify:
- Correct column mapping per schema variant
- Null handling (empty string, "null" literal)
- Timestamp parsing
- Season/date extraction from paths

## Critical Parsing Rules (from Task 1 Spec)

1. **Timestamp Format**: "20130311172101557" (YYYYMMDDHHmmSSsss, 17 chars) → parse to UTC epoch ms
2. **Null Fields**: Empty string "" or literal "null" → map to null
3. **Log Type Detection**: 
   - Bid logs: infer from filename prefix
   - Imp/clk/conv logs: from col 2 numeric code (1=IMP, 2=CLK, 3=CONV)
4. **Season Detection**: From directory name (training1st → 1, training2nd → 2, etc.)
5. **Date Extraction**: From filename prefix (bid.20130311.txt → 2013-03-11)
6. **Column Mapping**: Varies by schema - properly dispatched in parseBidLog() and parseImpClkConvLog()

## Output Structure (MinIO)

```
s3://rtb-raw-data/
└── ipinyou/
    └── v1/
        ├── dt=2013-03-11/
        │   ├── log_type=BID/
        │   │   └── part_0000.parquet
        │   ├── log_type=IMPRESSION/
        │   │   └── part_0000.parquet
        │   ├── log_type=CLICK/
        │   │   └── part_0000.parquet
        │   └── log_type=CONVERSION/
        │       └── part_0000.parquet
        ├── dt=2013-06-06/
        │   └── ... (same structure)
        └── _metadata/
            └── ingestion_manifest.json     # Metrics & timing
```

## Dependencies

Key Maven/Gradle dependencies:
- **Spring Boot 3.2.0**: Application framework
- **MinIO 8.5.7**: S3-compatible storage client
- **Parquet 1.13.1**: Columnar storage format
- **Avro 1.11.3**: Schema & serialization
- **Hadoop 3.3.6**: Distributed file system support
- **Jackson 2.16.1**: JSON/YAML processing
- **SLF4J 2.0.9 + Logback 1.4.14**: Logging

## Next Steps

### 1. Create Avro Schema Registry Integration
Once kafka-producer is built:
```java
new IpinyouFileReader(timestampParser, schemaRegistry)
```

### 2. Add Bloom Filters / Indexing
For faster queries on MinIO:
```
s3://rtb-raw-data/ipinyou/v1/_indices/bid_id.bloom
```

### 3. Implement Incremental Ingestion
Track processed files in manifest to skip on rerun:
```json
{
  "processed_files": ["training1st/bid.20130311.txt", ...]
}
```

### 4. Add Data Quality Metrics
Extend IngestionReport with data profile:
```java
report.increment("duplicates.bid_id", duplicateCount)
```

## Troubleshooting

### MinIO Connection Refused
```bash
# Check MinIO is running
kubectl get pods -n minio
kubectl port-forward svc/minio 9000:9000 -n minio
```

### File Not Found
```bash
# Verify source directory has correct structure
ls -la /data/ipinyou/training1st/bid*.txt
```

### Out of Memory
Increase in Dockerfile or Job spec:
```yaml
env:
- name: JVM_OPTS
  value: "-Xmx4g -Xms2g"
```

### Parquet Write Failures
Check MinIO bucket exists:
```bash
mc mb minio/rtb-raw-data
```

## Architecture Decisions

1. **Hexagonal Pattern**: Clean separation between domain logic and infrastructure, enabling easy testing and future changes to storage/parsing backends.

2. **Single Unified Schema**: All variants mapped to one schema upstream, simplifying downstream Kafka producers and analytics.

3. **ParallelPartitionWrites**: Read phase is single-threaded (to maintain order within files), write phase is parallelized (8 threads by default) for throughput.

4. **Spring Boot**: Standard for Spring ecosystem, enables rapid Kubernetes integration and operational tooling.

5. **Parquet + Avro**: Column-oriented format needed for efficient data lake querying; Avro schema for schema evolution.

6. **Configuration-Driven**: All environment-specific settings via YAML + env vars, no code changes for dev/prod.

## Next: kafka-producer Module

The kafka-producer module will:
1. Read organized Parquet from MinIO
2. Deserialize using shared Avro schema
3. Publish to Kafka topics (bid, impression, click, conversion)
4. Leverage same domain models (RawBidRecord) for consistency
