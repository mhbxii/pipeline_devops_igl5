resource "kubernetes_namespace_v1" "events" {
  metadata {
    name = "events-terraform"
    labels = {
      "app.kubernetes.io/managed-by" = "terraform"
    }
  }
}

resource "kubernetes_secret_v1" "database" {
  metadata {
    name      = "events-db"
    namespace = kubernetes_namespace_v1.events.metadata[0].name
  }
  data = {
    password      = var.mysql_password
    root-password = var.mysql_root_password
  }
}

resource "kubernetes_config_map_v1" "app" {
  metadata {
    name      = "events-config"
    namespace = kubernetes_namespace_v1.events.metadata[0].name
  }
  data = {
    SPRING_DATASOURCE_URL      = "jdbc:mysql://mysql:3306/eventsProject?serverTimezone=UTC"
    SPRING_DATASOURCE_USERNAME = "events"
    LOGISTICS_LOG_LEVEL        = "INFO"
  }
}

resource "kubernetes_persistent_volume_claim_v1" "mysql" {
  metadata {
    name      = "mysql-data"
    namespace = kubernetes_namespace_v1.events.metadata[0].name
  }
  spec {
    access_modes = ["ReadWriteOnce"]
    resources {
      requests = { storage = "1Gi" }
    }
  }
  wait_until_bound = false
}

resource "kubernetes_deployment_v1" "mysql" {
  metadata {
    name      = "mysql"
    namespace = kubernetes_namespace_v1.events.metadata[0].name
  }
  spec {
    replicas = 1
    strategy {
      type = "Recreate"
    }
    selector {
      match_labels = { app = "mysql" }
    }
    template {
      metadata {
        labels = { app = "mysql" }
      }
      spec {
        container {
          name  = "mysql"
          image = "mysql:8.4"
          env {
            name  = "MYSQL_DATABASE"
            value = "eventsProject"
          }
          env {
            name  = "MYSQL_USER"
            value = "events"
          }
          env {
            name = "MYSQL_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.database.metadata[0].name
                key  = "password"
              }
            }
          }
          env {
            name = "MYSQL_ROOT_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.database.metadata[0].name
                key  = "root-password"
              }
            }
          }
          port {
            container_port = 3306
          }
          volume_mount {
            name       = "data"
            mount_path = "/var/lib/mysql"
          }
          resources {
            requests = { cpu = "100m", memory = "256Mi" }
            limits   = { cpu = "1", memory = "640Mi" }
          }
          startup_probe {
            exec {
              command = ["sh", "-c", "MYSQL_PWD=\"$MYSQL_PASSWORD\" mysql -u events -D eventsProject -e 'SELECT 1'"]
            }
            period_seconds    = 5
            failure_threshold = 60
          }
          readiness_probe {
            exec {
              command = ["sh", "-c", "MYSQL_PWD=\"$MYSQL_PASSWORD\" mysql -u events -D eventsProject -e 'SELECT 1'"]
            }
            period_seconds = 10
          }
        }
        volume {
          name = "data"
          persistent_volume_claim {
            claim_name = kubernetes_persistent_volume_claim_v1.mysql.metadata[0].name
          }
        }
      }
    }
  }
  timeouts {
    create = "8m"
    update = "8m"
  }
}

resource "kubernetes_service_v1" "mysql" {
  metadata {
    name      = "mysql"
    namespace = kubernetes_namespace_v1.events.metadata[0].name
  }
  spec {
    selector = { app = "mysql" }
    port {
      port        = 3306
      target_port = 3306
    }
  }
}

resource "kubernetes_deployment_v1" "app" {
  depends_on = [kubernetes_deployment_v1.mysql, kubernetes_service_v1.mysql]
  metadata {
    name      = "events-app"
    namespace = kubernetes_namespace_v1.events.metadata[0].name
  }
  spec {
    replicas = 1
    selector {
      match_labels = { app = "events-app" }
    }
    template {
      metadata {
        labels = { app = "events-app" }
      }
      spec {
        security_context {
          run_as_non_root = true
          run_as_user     = 1000
          run_as_group    = 1000
        }
        container {
          name              = "app"
          image             = var.image
          image_pull_policy = "IfNotPresent"
          port {
            name           = "http"
            container_port = 8089
          }
          env_from {
            config_map_ref {
              name = kubernetes_config_map_v1.app.metadata[0].name
            }
          }
          env {
            name = "SPRING_DATASOURCE_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.database.metadata[0].name
                key  = "password"
              }
            }
          }
          resources {
            requests = { cpu = "100m", memory = "256Mi" }
            limits   = { cpu = "1", memory = "512Mi" }
          }
          security_context {
            allow_privilege_escalation = false
            capabilities {
              drop = ["ALL"]
            }
          }
          startup_probe {
            http_get {
              path = "/events/actuator/health/liveness"
              port = "http"
            }
            period_seconds    = 5
            failure_threshold = 60
          }
          readiness_probe {
            http_get {
              path = "/events/actuator/health"
              port = "http"
            }
            period_seconds = 10
          }
          liveness_probe {
            http_get {
              path = "/events/actuator/health/liveness"
              port = "http"
            }
            period_seconds = 15
          }
        }
      }
    }
  }
  timeouts {
    create = "8m"
    update = "8m"
  }
}

resource "kubernetes_service_v1" "app" {
  metadata {
    name      = "events-app"
    namespace = kubernetes_namespace_v1.events.metadata[0].name
  }
  spec {
    type     = "NodePort"
    selector = { app = "events-app" }
    port {
      port        = 8089
      target_port = "http"
      node_port   = 30090
    }
  }
}
