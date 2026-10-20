variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "cluster_name" {
  type    = string
  default = "events-cluster"
}

# us-east-1e doesn't support EKS control planes, so it's excluded
variable "azs" {
  type    = list(string)
  default = ["us-east-1a", "us-east-1b", "us-east-1c", "us-east-1d"]
}

variable "instance_type" {
  type    = string
  default = "t3.medium"
}

variable "node_port" {
  type    = number
  default = 30000
}