
## 1.1 Schema Registry Deployment 
# 1. First, create the User for the Schema Registry
resource "kubernetes_manifest" "schema_registry_user" {
  manifest = {
    apiVersion = "kafka.strimzi.io/v1beta2"
    kind       = "KafkaUser"
    metadata = {
      name      = "schema-registry-user"
      namespace = var.namespace
    }
    spec = {
      authentication = {
        type = "tls"
      }
    }
  }
}

# 2.  Schema Registry Deployment 
resource "kubernetes_manifest" "schema_registry" {
  depends_on = [kubernetes_manifest.kafka_cluster, kubernetes_manifest.schema_registry_user]

  manifest = {
    apiVersion = "apps/v1"
    kind       = "Deployment"
    metadata = {
      name      = "schema-registry"
      namespace = var.namespace
    }
    spec = {
      replicas = 1
      selector = {
        matchLabels = { app = "schema-registry" }
      }
      template = {
        metadata = {
          labels = { app = "schema-registry" }
        }
        spec = {
          enableServiceLinks = false
          containers = [{
            name  = "schema-registry"
            image = "confluentinc/cp-schema-registry:7.5.0"
            #command = ["sh", "-c", "sleep 3600"]
            ports = [{ containerPort = 8081 }]
            env = [
              {
                name  = "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS"
                value = "SSL://${kubernetes_manifest.kafka_cluster.object.metadata.name}-kafka-bootstrap:9093"
              },
              {
                name  = "SCHEMA_REGISTRY_HOST_NAME"
                value = "schema-registry"
              },
              {
                name  = "SCHEMA_REGISTRY_LISTENERS"
                value = "http://0.0.0.0:8081"
              },   
              {
                name  = "SCHEMA_REGISTRY_KAFKASTORE_SECURITY_PROTOCOL"
                value = "SSL"
              },
              # --- TRUSTSTORE (Verify Kafka) ---
              {
                name  = "SCHEMA_REGISTRY_KAFKASTORE_SSL_TRUSTSTORE_LOCATION"
                value = "/etc/certs/ca/ca.p12"
              },
              {
                name  = "SCHEMA_REGISTRY_KAFKASTORE_SSL_TRUSTSTORE_PASSWORD"
                valueFrom = {
                  secretKeyRef = {
                    name = "${kubernetes_manifest.kafka_cluster.object.metadata.name}-cluster-ca-cert"
                    key  = "ca.password"
                  }
                }
              },
              # --- KEYSTORE (Identify Registry to Kafka) ---
              {
                name  = "SCHEMA_REGISTRY_KAFKASTORE_SSL_KEYSTORE_LOCATION"
                value = "/etc/certs/user/user.p12"
              },
              {
                name  = "SCHEMA_REGISTRY_KAFKASTORE_SSL_KEYSTORE_PASSWORD"
                valueFrom = {
                  secretKeyRef = {
                    name = "schema-registry-user"
                    key  = "user.password"
                  }
                }
              }
            ]
            volumeMounts = [
              {
                name      = "cluster-ca-cert"
                mountPath = "/etc/certs/ca"
                readOnly  = true
              },
              {
                name      = "registry-user-cert"
                mountPath = "/etc/certs/user"
                readOnly  = true
              }
            ]
          }]
          volumes = [
            {
              name = "cluster-ca-cert"
              secret = {
                secretName = "${kubernetes_manifest.kafka_cluster.object.metadata.name}-cluster-ca-cert"
              }
            },
            {
              name = "registry-user-cert"
              secret = {
                secretName = "schema-registry-user"
              }
            }
          ]
        }
      }
    }
  }
}

### 1.2 Schema Registry Service
resource "kubernetes_manifest" "schema_registry_service" {
  manifest = {
    apiVersion = "v1"
    kind       = "Service"
    metadata = {
      name      = "schema-registry"
      namespace = var.namespace
    }
    spec = {
      ports = [{
        port       = 8081
        targetPort = 8081
      }]
      selector = {
        app = "schema-registry"
      }
    }
  }
}


##2.1 Schema Registry UI Deployment
resource "kubernetes_manifest" "schema_registry_ui" {
  manifest = {
    apiVersion = "apps/v1"
    kind       = "Deployment"
    metadata = {
      name      = "schema-registry-ui"
      namespace = var.namespace
    }
    spec = {
      replicas = 1
      selector = {
        matchLabels = { app = "schema-registry-ui" }
      }
      template = {
        metadata = {
          labels = { app = "schema-registry-ui" }
        }
        spec = {
          containers = [{
            name  = "schema-registry-ui"
            image = "landoop/schema-registry-ui:0.9.5"
            ports = [{ containerPort = 8000 }]
            env = [
              {
                name  = "SCHEMAREGISTRY_URL"
                # Use the internal service URL for the registry
                value = "http://schema-registry:8081"
              },
              {
                name  = "PROXY"
                value = "true" # Essential to avoid CORS issues in the browser
              }
            ]
          }]
        }
      }
    }
  }
}

##2.2 Schema Registry UI Service
resource "kubernetes_manifest" "schema_registry_ui_service" {
  manifest = {
    apiVersion = "v1"
    kind       = "Service"
    metadata = {
      name      = "schema-registry-ui"
      namespace = var.namespace
    }
    spec = {
      type = "ClusterIP"
      ports = [{ port = 80, targetPort = 8000 }]
      selector = { app = "schema-registry-ui" }
    }
  }
}

##2.3 Schema Registry UI Ingresss
resource "kubernetes_manifest" "schema_ui_ingress" {
  manifest = {
    apiVersion = "networking.k8s.io/v1"
    kind       = "Ingress"
    metadata = {
      name      = "schema-registry-ui-ingress"
      namespace = var.namespace
      annotations = {
        "kubernetes.io/ingress.class" = "nginx"
      }
    }
    spec = {
      rules = [{
        host = "schema-registry.local"
        http = {
          paths = [{
            path     = "/"
            pathType = "Prefix"
            backend = {
              service = {
                name = "schema-registry-ui"
                port = { number = 80 }
              }
            }
          }]
        }
      }]
    }
  }
}