variable "vpc_id" {
  type        = string
  description = "VPC ID"
}

variable "private_subnet_ids" {
  type        = list(string)
  description = "Private Subnet IDs"
}

variable "msk_sg_id" {
  type        = string
  description = "Security group ID for MSK"
}

variable "cluster_name" {
  type        = string
  description = "Kafka Cluster Name"
}

variable "environment" {
  type        = string
  description = "Environment name"
}
