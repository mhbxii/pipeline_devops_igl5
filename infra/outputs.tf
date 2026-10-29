output "namespace" {
  value = kubernetes_namespace_v1.events.metadata[0].name
}

output "health_url" {
  value = "http://localhost:8091/events/actuator/health"
}
