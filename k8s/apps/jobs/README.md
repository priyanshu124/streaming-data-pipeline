# Kubernetes Deployment Guide

This directory contains Kubernetes manifests for deploying the raw-data-organizer application.

## Prerequisites

- Kubernetes cluster with MinIO installed
- MinIO credentials stored in a Kubernetes secret
- Docker image: `raw-data-organizer:latest` (built and available in your cluster)

## Setup

### 1. Create MinIO Credentials Secret

```bash
kubectl create secret generic minio-credentials \
  --from-literal=access-key=minio \
  --from-literal=secret-key=minio123 \
  -n default
```

### 2. Build and Push Docker Image

```bash
# Build the Docker image
docker build -t raw-data-organizer:latest .

# For remote registry (adjust registry URL)
docker tag raw-data-organizer:latest myregistry.azurecr.io/raw-data-organizer:latest
docker push myregistry.azurecr.io/raw-data-organizer:latest
```

### 3. Deploy

#### Option A: Run as a one-time Job

```bash
kubectl apply -f job.yml
```

Monitor the job:
```bash
kubectl describe job raw-data-organizer
kubectl logs -f job/raw-data-organizer
```

#### Option B: Run as a Scheduled CronJob

```bash
kubectl apply -f cron-job.yml
```

Monitor the cronjob:
```bash
kubectl describe cronjob raw-data-organizer-scheduled
kubectl get jobs -l cronjob-name=raw-data-organizer-scheduled
```

## Configuration

Edit `job.yml` or `cron-job.yml` to customize:

- **MINIO_ENDPOINT**: MinIO service URL
- **SOURCE_BUCKET**: Source bucket name (default: `rtb-raw-data`)
- **SOURCE_PREFIX**: Source prefix/path (default: `incoming/`)
- **DEST_BUCKET**: Destination bucket name (default: `rtb-organized`)
- **DEST_PREFIX**: Destination prefix/path (default: `organized/`)
- **Schedule**: For CronJob, modify the `schedule` field (cron format)
- **Resources**: Adjust `cpu` and `memory` requests/limits as needed

## Cleanup

```bash
# Remove the Job
kubectl delete job raw-data-organizer

# Remove the CronJob
kubectl delete cronjob raw-data-organizer-scheduled

# Remove the ServiceAccount
kubectl delete serviceaccount raw-data-organizer
```

## Troubleshooting

### Pod fails to start

Check logs:
```bash
kubectl logs pod-name
kubectl describe pod pod-name
```

Common issues:
- MinIO secret not found: Ensure `minio-credentials` secret exists
- Image pull error: Ensure image is available in your cluster
- Connection refused: Verify MinIO endpoint and credentials

### Check MinIO connectivity from pod

```bash
# Start a debug pod
kubectl run -it --image=curlimages/curl debug-pod -- sh

# Test MinIO connection
curl -v http://minio:9000
```
