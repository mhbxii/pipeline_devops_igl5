terraform {
  required_version = ">= 1.6, < 2.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
  backend "local" {}
}

provider "aws" {
  region              = var.aws_region
  profile             = var.aws_profile
  allowed_account_ids = [var.expected_account_id]
  default_tags {
    tags = {
      Project   = "events-tache"
      ManagedBy = "Terraform"
      Purpose   = "UniversityDemo"
    }
  }
}

# Existing lab resources are read, never imported or modified by this state.
data "aws_vpc" "lab" {
  default = true
}

data "aws_subnet" "cluster" {
  for_each = toset(["us-east-1a", "us-east-1b"])
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.lab.id]
  }
  availability_zone = each.key
  default_for_az    = true
}

data "aws_iam_role" "lab" {
  name = "LabRole"
}

resource "aws_eks_cluster" "demo" {
  name                = var.cluster_name
  role_arn            = data.aws_iam_role.lab.arn
  version             = "1.36"
  deletion_protection = false

  access_config {
    authentication_mode                         = "API_AND_CONFIG_MAP"
    bootstrap_cluster_creator_admin_permissions = true
  }
  upgrade_policy {
    support_type = "STANDARD"
  }
  vpc_config {
    subnet_ids              = [for subnet in data.aws_subnet.cluster : subnet.id]
    endpoint_private_access = true
    endpoint_public_access  = true
    public_access_cidrs     = var.operator_ipv4_cidrs
  }
}

resource "aws_launch_template" "worker" {
  name_prefix = "${var.cluster_name}-"
  description = "One small EKS demo worker; no SSH; no surplus CPU-credit charges"

  credit_specification {
    cpu_credits = "standard"
  }
  block_device_mappings {
    device_name = "/dev/xvda"
    ebs {
      volume_size           = 20
      volume_type           = "gp3"
      encrypted             = true
      delete_on_termination = true
    }
  }
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }
  tag_specifications {
    resource_type = "instance"
    tags = {
      Name    = var.cluster_name
      Project = "events-tache"
      Purpose = "UniversityDemo"
    }
  }
  tag_specifications {
    resource_type = "volume"
    tags = {
      Project = "events-tache"
      Purpose = "UniversityDemo"
    }
  }
}

resource "aws_eks_node_group" "demo" {
  cluster_name    = aws_eks_cluster.demo.name
  node_group_name = "demo-one-worker"
  node_role_arn   = data.aws_iam_role.lab.arn
  # One AZ keeps EBS volumes attachable after replacement of this single worker.
  subnet_ids     = [data.aws_subnet.cluster["us-east-1a"].id]
  instance_types = ["t3.medium"]
  ami_type       = "AL2023_x86_64_STANDARD"
  capacity_type  = "ON_DEMAND"
  version        = aws_eks_cluster.demo.version
  launch_template {
    id      = aws_launch_template.worker.id
    version = aws_launch_template.worker.latest_version
  }
  scaling_config {
    desired_size = 1
    min_size     = 1
    max_size     = 1
  }
  update_config {
    max_unavailable = 1
  }
}

resource "aws_eks_addon" "ebs" {
  cluster_name                = aws_eks_cluster.demo.name
  addon_name                  = "aws-ebs-csi-driver"
  addon_version               = "v1.67.2-eksbuild.1"
  resolve_conflicts_on_create = "NONE"
  resolve_conflicts_on_update = "NONE"
  configuration_values        = jsonencode({ controller = { replicaCount = 1 } })
  depends_on                  = [aws_eks_node_group.demo]
}
