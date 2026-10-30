# =============================================================================
# EKS Cluster
# =============================================================================

resource "aws_eks_cluster" "main" {
  name     = var.cluster_name
  role_arn = var.eks_cluster_role_arn
  version  = var.kubernetes_version

  vpc_config {
    subnet_ids              = var.subnet_ids
    security_group_ids      = [aws_security_group.eks_cluster.id]
    endpoint_private_access = false
    endpoint_public_access  = true
  }

  access_config {
    authentication_mode = "API_AND_CONFIG_MAP"
  }

  tags = {
    Name = var.cluster_name
  }

  depends_on = [
    aws_ec2_tag.subnet_a_cluster,
    aws_ec2_tag.subnet_a_elb,
    aws_ec2_tag.subnet_b_cluster,
    aws_ec2_tag.subnet_b_elb,
  ]
}

# =============================================================================
# EKS Access Entry — grant voclabs role access to the cluster
# =============================================================================

resource "aws_eks_access_entry" "voclabs" {
  cluster_name  = aws_eks_cluster.main.name
  principal_arn = "arn:aws:iam::228000713549:role/voclabs"
  type          = "STANDARD"
}

resource "aws_eks_access_policy_association" "voclabs_admin" {
  cluster_name  = aws_eks_cluster.main.name
  principal_arn = "arn:aws:iam::228000713549:role/voclabs"
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"

  access_scope {
    type = "cluster"
  }
}

# =============================================================================
# EKS Node Group
# =============================================================================

resource "aws_eks_node_group" "main" {
  cluster_name    = aws_eks_cluster.main.name
  node_group_name = "events-nodes"
  node_role_arn   = var.eks_node_role_arn
  subnet_ids      = var.subnet_ids
  instance_types  = var.node_instance_types
  disk_size       = var.node_disk_size
  ami_type        = "AL2_x86_64"
  capacity_type   = "ON_DEMAND"

  scaling_config {
    desired_size = var.node_desired_size
    min_size     = var.node_min_size
    max_size     = var.node_max_size
  }

  tags = {
    Name = "events-nodes"
  }

  depends_on = [aws_eks_cluster.main]
}
