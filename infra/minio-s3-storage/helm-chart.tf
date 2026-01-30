resource kubernetes_namespace "minio" {
  metadata {
    name = "minio"
  }
}

# --- 1. The MinIO Server (Helm) ---
resource "helm_release" "minio" {
  name             = "minio"
  repository       = "https://charts.min.io/"
  chart            = "minio"
  namespace        = kubernetes_namespace.minio.metadata[0].name

  values = [yamlencode({
    rootUser     = var.minio_root_user
    rootPassword = var.minio_root_password
    mode         = "standalone"
    
    service = {
      type = "LoadBalancer"
      port = 9000
    }
    consoleService = {
      type = "LoadBalancer"
      port = 9001
    }

    persistence = {
      enabled      = true
      storageClass = "hostpath" # Docker Desktop default
      size         = var.minio_storage_size
    }

    resources = {
      requests = { memory = "256Mi", cpu = "100m" }
    }
  })]
}

# --- 2. The Automation "Buffer" ---
# Gives Kubernetes time to actually start the process inside the pod
resource "time_sleep" "wait_for_minio" {
  depends_on      = [helm_release.minio]
  create_duration = "30s"
}