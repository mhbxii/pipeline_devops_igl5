terraform {
  required_version = ">= 1.6, < 2.0"
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.38.0"
    }
  }
}

provider "kubernetes" {
  config_path    = pathexpand(var.kubeconfig_path)
  config_context = "kind-events-tache"
}

variable "kubeconfig_path" {
  description = "Project-local kubeconfig; never committed."
  type        = string
  default     = "../.local/kubeconfig"
}

resource "kubernetes_namespace_v1" "monitoring" {
  metadata {
    name = "events-monitoring"
    labels = {
      "app.kubernetes.io/managed-by" = "terraform"
      "project"                      = "events-tache"
    }
  }
}

resource "kubernetes_resource_quota_v1" "monitoring" {
  metadata {
    name      = "monitoring-budget"
    namespace = kubernetes_namespace_v1.monitoring.metadata[0].name
  }
  spec {
    hard = {
      "pods"             = "4"
      "requests.memory"  = "512Mi"
      "limits.memory"    = "1Gi"
      "requests.storage" = "2Gi"
    }
  }
}

resource "kubernetes_config_map_v1" "prometheus" {
  metadata {
    name      = "prometheus"
    namespace = kubernetes_namespace_v1.monitoring.metadata[0].name
  }
  data = {
    "prometheus.yml" = yamlencode({
      global = { scrape_interval = "15s" }
      scrape_configs = [{
        job_name     = "events"
        metrics_path = "/events/actuator/prometheus"
        static_configs = [{
          targets = ["events.events-tache.svc.cluster.local:8089"]
        }]
      }]
    })
  }
}

resource "kubernetes_persistent_volume_claim_v1" "prometheus" {
  metadata {
    name      = "prometheus-data"
    namespace = kubernetes_namespace_v1.monitoring.metadata[0].name
  }
  wait_until_bound = false
  spec {
    access_modes = ["ReadWriteOnce"]
    resources {
      requests = { storage = "1Gi" }
    }
  }
}

resource "kubernetes_deployment_v1" "prometheus" {
  metadata {
    name      = "prometheus"
    namespace = kubernetes_namespace_v1.monitoring.metadata[0].name
  }
  spec {
    replicas = 1
    strategy { type = "Recreate" }
    selector { match_labels = { app = "prometheus" } }
    template {
      metadata { labels = { app = "prometheus" } }
      spec {
        security_context { fs_group = 65534 }
        container {
          name  = "prometheus"
          image = "prom/prometheus:v3.5.0"
          args  = ["--config.file=/etc/prometheus/prometheus.yml", "--storage.tsdb.path=/prometheus", "--storage.tsdb.retention.time=6h"]
          port { container_port = 9090 }
          resources {
            requests = { cpu = "50m", memory = "128Mi" }
            limits   = { cpu = "500m", memory = "256Mi" }
          }
          readiness_probe {
            http_get {
              path = "/-/ready"
              port = 9090
            }
          }
          volume_mount {
            name       = "config"
            mount_path = "/etc/prometheus"
            read_only  = true
          }
          volume_mount {
            name       = "data"
            mount_path = "/prometheus"
          }
        }
        volume {
          name = "config"
          config_map { name = kubernetes_config_map_v1.prometheus.metadata[0].name }
        }
        volume {
          name = "data"
          persistent_volume_claim { claim_name = kubernetes_persistent_volume_claim_v1.prometheus.metadata[0].name }
        }
      }
    }
  }
}

resource "kubernetes_service_v1" "prometheus" {
  metadata {
    name      = "prometheus"
    namespace = kubernetes_namespace_v1.monitoring.metadata[0].name
  }
  spec {
    selector = { app = "prometheus" }
    port {
      port        = 9090
      target_port = 9090
    }
  }
}

output "managed_namespace" {
  value = kubernetes_namespace_v1.monitoring.metadata[0].name
}
