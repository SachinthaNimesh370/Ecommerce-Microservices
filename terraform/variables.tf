variable "aws_region" {
  description = "The AWS region where resources will be provisioned."
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Deployment environment name (e.g. dev, staging, prod)."
  type        = string
  default     = "dev"
}

variable "vpc_cidr" {
  description = "CIDR block for the Virtual Private Cloud (VPC)."
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "List of Availability Zones to deploy subnets into."
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for the Public Subnets."
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for the Private Subnets."
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.11.0/24"]
}

variable "ecr_repository_names" {
  description = "List of microservice repository names for Amazon ECR."
  type        = list(string)
  default     = [
    "api-gateway",
    "user-service",
    "product-service",
    "order-service",
    "inventory-service",
    "notification-service"
  ]
}

variable "db_name" {
  description = "Database name for Amazon RDS PostgreSQL."
  type        = string
  default     = "ecommerce_db"
}

variable "db_username" {
  description = "Master username for Amazon RDS PostgreSQL."
  type        = string
  default     = "postgres"
}

variable "db_password" {
  description = "Master password for Amazon RDS PostgreSQL."
  type        = string
  sensitive   = true
  default     = "PostgresSecure2026!"
}

variable "db_instance_class" {
  description = "RDS instance class (db.t3.micro is AWS Free Tier eligible)."
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "Allocated storage in GB for RDS instance (20 GB is AWS Free Tier eligible)."
  type        = number
  default     = 20
}

variable "eks_cluster_name" {
  description = "Name of the Amazon EKS Kubernetes Cluster."
  type        = string
  default     = "ecommerce-eks-cluster"
}

variable "eks_node_instance_types" {
  description = "Instance types for the EKS worker node group (t3.micro keeps compute costs minimal for testing)."
  type        = list(string)
  default     = ["t3.micro"]
}

variable "eks_desired_nodes" {
  description = "Desired number of worker nodes."
  type        = number
  default     = 2
}

variable "eks_min_nodes" {
  description = "Minimum number of worker nodes."
  type        = number
  default     = 1
}

variable "eks_max_nodes" {
  description = "Maximum number of worker nodes."
  type        = number
  default     = 3
}

variable "enable_msk" {
  description = "Toggle Amazon MSK Kafka creation. Set to false for minimal cost during initial testing."
  type        = bool
  default     = false
}

variable "msk_cluster_name" {
  description = "Name of the Amazon MSK cluster."
  type        = string
  default     = "ecommerce-kafka-cluster"
}
