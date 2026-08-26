variable "repository_names" {
  type        = list(string)
  description = "List of microservice ECR repository names"
}

variable "environment" {
  type        = string
  description = "Environment name"
}
