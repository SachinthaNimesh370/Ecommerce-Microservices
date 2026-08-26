output "repository_urls" {
  description = "Map of ECR repository names to repository URLs"
  value       = { for k, repo in aws_ecr_repository.services : k => repo.repository_url }
}
