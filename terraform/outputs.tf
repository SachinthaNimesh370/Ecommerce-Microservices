output "vpc_id" {
  description = "The ID of the provisioned AWS VPC."
  value       = module.vpc.vpc_id
}

output "public_subnet_ids" {
  description = "List of Public Subnet IDs."
  value       = module.vpc.public_subnet_ids
}

output "private_subnet_ids" {
  description = "List of Private Subnet IDs."
  value       = module.vpc.private_subnet_ids
}

output "ecr_repository_urls" {
  description = "Map of ECR repository names to repository URLs."
  value       = module.ecr.repository_urls
}

output "rds_endpoint" {
  description = "Connection endpoint for Amazon RDS PostgreSQL instance."
  value       = module.rds.rds_endpoint
}

output "rds_database_name" {
  description = "Database name configured on Amazon RDS."
  value       = module.rds.rds_db_name
}

output "eks_cluster_endpoint" {
  description = "API Endpoint URL for the Amazon EKS Kubernetes Cluster."
  value       = module.eks.cluster_endpoint
}

output "eks_cluster_name" {
  description = "Name of the Amazon EKS Cluster."
  value       = module.eks.cluster_name
}

output "eks_cluster_arn" {
  description = "ARN of the Amazon EKS Cluster."
  value       = module.eks.cluster_arn
}

output "msk_bootstrap_brokers" {
  description = "Bootstrap broker connection string for Amazon MSK Kafka cluster."
  value       = var.enable_msk ? module.msk[0].bootstrap_brokers : "MSK Disabled (enable_msk = false)"
}
