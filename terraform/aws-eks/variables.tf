variable "aws_region" {
  type    = string
  default = "us-east-1"
  validation {
    condition     = var.aws_region == "us-east-1"
    error_message = "This reviewed lab configuration is limited to us-east-1."
  }
}

variable "aws_profile" {
  description = "Dedicated local lab profile; null when a trusted agent injects temporary credentials."
  type        = string
  default     = null
}

variable "expected_account_id" {
  description = "Account guard supplied in an ignored local tfvars file. No credentials belong here."
  type        = string
  sensitive   = true
  validation {
    condition     = can(regex("^[0-9]{12}$", var.expected_account_id))
    error_message = "An explicit twelve-digit lab account is required."
  }
}

variable "cluster_name" {
  type    = string
  default = "events-tache-ayyoub"
  validation {
    condition     = can(regex("^events-tache-[a-z0-9-]{1,60}$", var.cluster_name))
    error_message = "Use a dedicated events-tache- project name."
  }
}

variable "operator_ipv4_cidrs" {
  description = "Current workstation/CI egress IPv4 /32 addresses allowed to reach the public EKS API."
  type        = list(string)
  validation {
    condition = length(var.operator_ipv4_cidrs) > 0 && alltrue([
      for cidr in var.operator_ipv4_cidrs : can(cidrhost(cidr, 0)) && endswith(cidr, "/32") && !strcontains(cidr, ":")
    ])
    error_message = "Provide explicit IPv4 /32 addresses; no world-open API CIDR."
  }
}
