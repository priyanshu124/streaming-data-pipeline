variable "environment" {
  description = "Environment (dev/prod)"
  type        = string
}

variable "cluster_name" {
  description = "Cluster base name"
  type        = string
}

variable "namespace" {
  description = "Namespace where Kafka and Strimzi operator will be deployed"
  type        = string
}


variable "kafka_replicas" {
  description = "Number of Kafka broker replicas"
  type        = number
  default     = 1
}

variable "zookeeper_replicas" {
  description = "Number of Zookeeper replicas"
  type        = number
  default     = 1
}

variable "kafka_storage_size" {
  description = "Kafka persistent volume size"
  type        = string
  default     = "10Gi"
}

variable "zookeeper_storage_size" {
  description = "Zookeeper persistent volume size"
  type        = string
  default     = "5Gi"
}

variable "delete_claim" {
  description = "Whether to delete PVs on cluster destroy"
  type        = bool
  default     = true
}