# =============================================================================
# Data Sources — Reference existing default VPC and subnets
# =============================================================================

data "aws_vpc" "default" {
  id = var.vpc_id
}

data "aws_subnet" "subnet_a" {
  id = var.subnet_ids[0]
}

data "aws_subnet" "subnet_b" {
  id = var.subnet_ids[1]
}

# =============================================================================
# EKS-required tags on existing subnets
# =============================================================================

resource "aws_ec2_tag" "subnet_a_cluster" {
  resource_id = var.subnet_ids[0]
  key         = "kubernetes.io/cluster/${var.cluster_name}"
  value       = "shared"
}

resource "aws_ec2_tag" "subnet_a_elb" {
  resource_id = var.subnet_ids[0]
  key         = "kubernetes.io/role/elb"
  value       = "1"
}

resource "aws_ec2_tag" "subnet_b_cluster" {
  resource_id = var.subnet_ids[1]
  key         = "kubernetes.io/cluster/${var.cluster_name}"
  value       = "shared"
}

resource "aws_ec2_tag" "subnet_b_elb" {
  resource_id = var.subnet_ids[1]
  key         = "kubernetes.io/role/elb"
  value       = "1"
}

# =============================================================================
# Security Group for the EKS Cluster
# =============================================================================

resource "aws_security_group" "eks_cluster" {
  name        = "events-cluster-sg"
  description = "Security group for the EKS cluster"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "Allow HTTPS for kubectl"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "events-cluster-sg"
  }
}
