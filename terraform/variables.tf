variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

variable "aws_profile" {
  description = "AWS CLI profile to use"
  type        = string
  default     = "terraform"
}

variable "cluster_name" {
  description = "Name of the EKS cluster"
  type        = string
  default     = "events-cluster"
}

variable "kubernetes_version" {
  description = "Kubernetes version for the EKS cluster"
  type        = string
  default     = "1.32"
}

variable "eks_cluster_role_arn" {
  description = "ARN of the existing IAM role for the EKS cluster"
  type        = string
  default     = "arn:aws:iam::228000713549:role/c231366a5819305l18006910t1w228000-LabEksClusterRole-FKw4SP1nvxL2"
}

variable "eks_node_role_arn" {
  description = "ARN of the existing IAM role for the EKS node group"
  type        = string
  default     = "arn:aws:iam::228000713549:role/c231366a5819305l18006910t1w228000713-LabEksNodeRole-5qPX0IPXdKCx"
}

variable "vpc_id" {
  description = "ID of the existing default VPC"
  type        = string
  default     = "vpc-0f5db0c7d9a256c1e"
}

variable "subnet_ids" {
  description = "List of existing subnet IDs for the EKS cluster"
  type        = list(string)
  default     = ["subnet-0b61181e7b28d1cbe", "subnet-0af2fffb43772f438"]
}

variable "node_instance_types" {
  description = "Instance types for the EKS node group"
  type        = list(string)
  default     = ["t3.medium"]
}

variable "node_desired_size" {
  description = "Desired number of nodes in the node group"
  type        = number
  default     = 2
}

variable "node_min_size" {
  description = "Minimum number of nodes in the node group"
  type        = number
  default     = 1
}

variable "node_max_size" {
  description = "Maximum number of nodes in the node group"
  type        = number
  default     = 3
}

variable "node_disk_size" {
  description = "Disk size in GB for each node"
  type        = number
  default     = 20
}
