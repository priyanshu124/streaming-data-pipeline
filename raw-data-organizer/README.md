# Raw Data Organizer

Simple file reorganizer that reads raw log files from MinIO and reorganizes them by date and log type.

## Purpose

Reorganizes flat MinIO directory structure into a date-partitioned structure:
```
Source:  bucket/prefix/filename.YYYYMMDD.txt
Result:  bucket/prefix/dt=YYYY-MM-DD/log_type=XXX/filename.YYYYMMDD.txt
```

No parsing, no transformation – just reorganization for downstream consumption.

## Configuration

Set these environment variables or update `application.yml`:

```bash
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

SOURCE_BUCKET=rtb-raw-data
SOURCE_PREFIX=ipinyou/raw

DEST_BUCKET=rtb-organized-data
DEST_PREFIX=ipinyou/v1
```

## Supported File Formats

- `bid.YYYYMMDD.txt`
- `impression.YYYYMMDD.txt` or `imp.YYYYMMDD.txt`
- `click.YYYYMMDD.txt`
- `conversion.YYYYMMDD.txt`

Parser extracts date and log type from filename, ignores others.

## Run

```bash
# Local
gradle bootRun

# Docker
docker run -e MINIO_ENDPOINT=http://minio:9000 raw-data-organizer:latest
```

## Output

Organized files in destination bucket:
```
dt=2013-03-11/log_type=BID/bid.20130311.txt
dt=2013-03-11/log_type=IMPRESSION/imp.20130311.txt
dt=2013-03-12/log_type=BID/bid.20130312.txt
...
```

Ready for producer module to parse and send to Kafka topics.
