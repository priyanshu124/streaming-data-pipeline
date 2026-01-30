
variable "cluster_name" {
  description = "Cluster base name"
  type        = string
}

variable "environment" {
  description = "Deployment environment (e.g., dev, staging, prod)"
  type        = string
  default     = "dev"
}


variable "minio_root_user" {
  description = "MinIO root username"
  type        = string
  default = "minio"
}

variable "minio_root_password" {
  description = "MinIO root password"
  type        = string
  sensitive   = true
  default     = "minio123"
}

variable "minio_storage_size" {
  description = "Persistent volume size for MinIO"
  type        = string
  default     = "50Gi"
}

variable "minio_replicas" {
  description = "Number of MinIO replicas"
  type        = number
  default     = 1
}

variable "s3_endpoint" {
  description = "S3 Endpoint URL for MinIO"
  type        = string
  default     = "http://localhost:9000" 
}