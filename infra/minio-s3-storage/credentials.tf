
resource "kubernetes_secret" "minio_creds" {
  metadata {
    name      = "minio-creds"
    namespace = kubernetes_namespace.minio.metadata[0].name
  }

  data = {
    rootUser     = var.minio_root_user
    rootPassword = var.minio_root_password
  }

  type = "Opaque"
}
