variable "cluster_name" {
  type        = string
  description = "EKS Cluster Name"
}

variable "vpc_id" {
  type        = string
  description = "VPC ID"
}

variable "subnet_ids" {
  type        = list(string)
  description = "Subnet IDs for EKS Cluster & Managed Node Group"
}

variable "cluster_role_arn" {
  type        = string
  description = "IAM Role ARN for EKS Control Plane"
}

variable "node_role_arn" {
  type        = string
  description = "IAM Role ARN for EKS Node Group"
}

variable "cluster_sg_id" {
  type        = string
  description = "Security group ID for EKS Cluster"
}

variable "node_instance_types" {
  type        = list(string)
  description = "Instance types for node group"
}

variable "desired_nodes" {
  type        = number
  description = "Desired number of worker nodes"
}

variable "min_nodes" {
  type        = number
  description = "Minimum number of worker nodes"
}

variable "max_nodes" {
  type        = number
  description = "Maximum number of worker nodes"
}

variable "environment" {
  type        = string
  description = "Environment name"
}
