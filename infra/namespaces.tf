resource "kubernetes_namespace" "kafka" {
  metadata {
    name = "${var.cluster_name}-kafka"
  }
}

resource "kubernetes_namespace" "observability" {
  metadata {
    name = "${var.cluster_name}-observability"
  }
}