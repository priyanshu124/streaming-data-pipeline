
# 1. Create the Schema Registry User (Generates the certificates)
resource "kubernetes_manifest" "registry_user" {
  manifest = {
    apiVersion = "kafka.strimzi.io/v1beta2"
    kind       = "KafkaUser"
    metadata = {
      name      = "schema-registry-user"
      namespace = var.namespace
      labels = {
        "strimzi.io/cluster" = kubernetes_manifest.kafka_cluster.object.metadata.name
      }
    }
    spec = {
      authentication = { type = "tls" }
      authorization = {
        type = "simple"
        acls = [
          # 1. Manage the primary schema storage topic
          {
            resource = {
              type = "topic"
              name = "_schemas"
              patternType = "literal"
            }
            operations = ["All", "DescribeConfigs"]
            host = "*"
          },
          # 2. Manage metadata encoding topic
          {
            resource = {
              type = "topic"
              name = "_schema_encoders"
              patternType = "literal"
            }
            operations = ["All"]
            host = "*"
          },
          # 3. Consumer group for Registry coordination
          {
            resource = {
              type = "group"
              name = "schema-registry"
              patternType = "literal"
            }
            operations = ["Read"]
            host = "*"
          },
          # 4. Required for cluster discovery and version checking
          {
            resource = {
              type = "cluster"
              name = "kafka-cluster"
              patternType = "literal"
            }
            operations = ["Describe"]
            host = "*"
          }
        ]
      }
    }
  }
}

# 2. Create the producer User (Generates the certificates)
resource "kubernetes_manifest" "producer_user" {
  manifest = {
    apiVersion = "kafka.strimzi.io/v1beta2"
    kind       = "KafkaUser"
    metadata = {
      name      = "java-producer-user"
      namespace = var.namespace
      labels = {
        "strimzi.io/cluster" = kubernetes_manifest.kafka_cluster.object.metadata.name
      }
    }
    spec = {
      authentication = { type = "tls" }
    }
  }
}

