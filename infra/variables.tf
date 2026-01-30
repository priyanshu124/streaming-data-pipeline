# Global variables used across modules/operators
variable "environment" {
  description = "Deployment environment (dev/prod/staging)"
  type        = string
  default     = "dev"
}

variable "cluster_name" {
  description = "Base name for clusters (used in naming namespaces and resources)"
  type        = string
  default     = "ad-bidding-analytics"
}