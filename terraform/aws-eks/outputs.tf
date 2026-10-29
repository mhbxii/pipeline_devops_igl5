output "cluster_name" {
  value = aws_eks_cluster.demo.name
}

output "cluster_endpoint" {
  value = aws_eks_cluster.demo.endpoint
}

output "worker_type" {
  value = "1 x t3.medium, ON_DEMAND, fixed size 1"
}

output "kubeconfig_command" {
  description = "Run from the repository root, using a compatible project-local kubectl afterwards."
  value       = "aws eks update-kubeconfig --profile events-tache-lab --region us-east-1 --name ${aws_eks_cluster.demo.name} --alias events-tache-eks --kubeconfig .local/kubeconfig-eks"
}
