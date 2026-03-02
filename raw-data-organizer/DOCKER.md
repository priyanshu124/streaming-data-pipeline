# Docker Setup Guide

## Dockerfile Overview

The [Dockerfile](Dockerfile) uses a **multi-stage build** approach:

1. **Builder Stage**: Compiles the Java application using Gradle
2. **Runtime Stage**: Runs the compiled JAR file using a lightweight JDK image

This results in a smaller final image (~400MB) by keeping build dependencies out of the runtime layer.

## Building the Image

### Local Build

```bash
# Build the image
docker build -t raw-data-organizer:latest .

# View image info
docker images raw-data-organizer
```

### With Custom Tags

```bash
# For publication to a registry
docker build -t myregistry.azurecr.io/raw-data-organizer:1.0 .
docker push myregistry.azurecr.io/raw-data-organizer:1.0
```

## Running Locally

### Option 1: Docker Compose (Recommended)

```bash
# Start MinIO and the organizer
docker-compose up

# View MinIO Web Console
# URL: http://localhost:9001
# Credentials: minio / minio123

# View organizer logs
docker-compose logs -f raw-data-organizer

# Stop services
docker-compose down
```

### Option 2: Manual Docker Run

```bash
# Start MinIO
docker run -d \
  -p 9000:9000 \
  -p 9001:9001 \
  -e MINIO_ROOT_USER=minio \
  -e MINIO_ROOT_PASSWORD=minio123 \
  minio/minio:latest \
  server /minio_root --console-address ":9001"

# Run the organizer
docker run \
  -e MINIO_ENDPOINT=http://host.docker.internal:9000 \
  -e MINIO_ACCESS_KEY=minio \
  -e MINIO_SECRET_KEY=minio123 \
  -e SOURCE_BUCKET=rtb-raw-data \
  -e SOURCE_PREFIX=incoming/ \
  -e DEST_BUCKET=rtb-organized \
  -e DEST_PREFIX=organized/ \
  raw-data-organizer:latest
```

## Configuration

Pass environment variables when running:

- `MINIO_ENDPOINT`: MinIO service URL (default: `http://minio:9000`)
- `MINIO_ACCESS_KEY`: MinIO access key
- `MINIO_SECRET_KEY`: MinIO secret key
- `SOURCE_BUCKET`: Source bucket name
- `SOURCE_PREFIX`: Source path prefix
- `DEST_BUCKET`: Destination bucket name
- `DEST_PREFIX`: Destination path prefix

## Troubleshooting

### Build fails

```bash
# Check Gradle issues
docker build --progress=plain .

# Check Java version compatibility
docker run --rm gradle:8.3-jdk17 java -version
```

### Container exits immediately

```bash
# Check logs
docker logs container-id

# Verify MinIO connectivity
docker run --rm --network host minioctl admin info
```

### Permission issues

```bash
# Run as root in container if needed
docker run --user root raw-data-organizer:latest
```
