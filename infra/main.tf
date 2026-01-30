module "kafka" {
  source       = "./kafka"
  environment  = var.environment
  cluster_name = var.cluster_name
  namespace    = kubernetes_namespace.kafka.metadata[0].name
}

module "minio-s3-storage" {
  source       = "./minio-s3-storage"
  cluster_name = var.cluster_name
  environment  = var.environment
}

module argocd {
  source      = "./argocd"
}