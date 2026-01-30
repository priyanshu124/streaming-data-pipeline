resource "helm_release" "strimzi" {
  name       = "strimzi"
  namespace  = var.namespace
  repository = "https://strimzi.io/charts/"
  chart      = "strimzi-kafka-operator"
  version    = "0.39.0"
}

resource "helm_release" "kafka_ui" {
  name       = "kafka-ui"
  namespace  = var.namespace
  repository = "https://provectus.github.io/kafka-ui-charts"
  chart      = "kafka-ui"
  version    = "0.7.2"

  values = [
    yamlencode({
      # The 'envs' block in this chart version requires a 'config' sub-map
      envs = {
        config = {
          KAFKA_CLUSTERS_0_NAME             = "${var.cluster_name}-${var.environment}"
          KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS = "${var.cluster_name}-${var.environment}-kafka-bootstrap.${var.namespace}.svc:9092"
          DYNAMIC_CONFIG_ENABLED            = "true"
        }
      }

      service = {
        type = "NodePort"
        ports = {
          http = {
            nodePort = 30080
          }
        }
      }

      resources = {
        limits = {
          cpu    = "500m"
          memory = "512Mi"
        }
        requests = {
          cpu    = "250m"
          memory = "256Mi"
        }
      }
    })
  ]

  depends_on = [
    kubernetes_manifest.kafka_cluster
  ]
}