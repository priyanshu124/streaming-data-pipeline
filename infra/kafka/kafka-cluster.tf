resource "kubernetes_manifest" "kafka_cluster" {
  depends_on = [helm_release.strimzi]

  manifest = {
    apiVersion = "kafka.strimzi.io/v1beta2"
    kind       = "Kafka"
    metadata = {
      name      = "${var.cluster_name}-${var.environment}"
      namespace = var.namespace
    }
    spec = {
      kafka = {
        version  = "3.6.1"
        replicas = var.kafka_replicas

        listeners = [
          {
            name = "plain"
            port = 9092
            type = "internal"
            tls  = false
          },
          {
            name = "tls"
            port = 9093
            type = "internal"
            tls  = true
            authentication = { type = "tls" }
          }
        ]

        authorization = {
          type = "simple"
          # admin users here
          superUsers = [
            "User:admin-user",
            #"User:schema-registry-user" # Optional: Makes Registry setup easier
          ]
        }

        storage = {
          type         = "persistent-claim"
          size         = var.kafka_storage_size
          deleteClaim = var.delete_claim
        }

        config = {
          "offsets.topic.replication.factor"         = var.kafka_replicas
          "transaction.state.log.replication.factor" = var.kafka_replicas
          "min.insync.replicas"                      = 1
          "default.replication.factor"               = var.kafka_replicas
        }
      }

      zookeeper = {
        replicas = var.zookeeper_replicas
        storage = {
          type         = "persistent-claim"
          size         = var.zookeeper_storage_size
          deleteClaim = var.delete_claim
        }
      }

      entityOperator = {
        topicOperator = {}
        userOperator  = {}
      }
    }
  }
}


