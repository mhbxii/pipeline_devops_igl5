variable "kubeconfig_path" {
  type        = string
  default     = "../.local/kube/host-config"
  description = "Kubeconfig accessible by the machine running Terraform."
}

variable "image" {
  type        = string
  default     = "events-logistics:local"
  description = "Application image already loaded into kind or accessible in a registry."
}

variable "mysql_password" {
  type        = string
  sensitive   = true
  description = "Application database password. Supplied via TF_VAR_mysql_password."
  validation {
    condition     = length(var.mysql_password) >= 16
    error_message = "Use a database password of at least 16 characters."
  }
}

variable "mysql_root_password" {
  type        = string
  sensitive   = true
  description = "MySQL administrator password. Supplied via TF_VAR_mysql_root_password."
  validation {
    condition     = length(var.mysql_root_password) >= 16
    error_message = "Use a root password of at least 16 characters."
  }
}
