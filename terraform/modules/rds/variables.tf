variable "vpc_id" {
  type        = string
  description = "VPC ID"
}

variable "private_subnet_ids" {
  type        = list(string)
  description = "Private Subnet IDs for RDS subnet group"
}

variable "rds_sg_id" {
  type        = string
  description = "Security group ID for RDS"
}

variable "db_name" {
  type        = string
  description = "PostgreSQL Database Name"
}

variable "db_username" {
  type        = string
  description = "Master Username"
}

variable "db_password" {
  type        = string
  description = "Master Password"
  sensitive   = true
}

variable "db_instance_class" {
  type        = string
  description = "RDS Instance Type (e.g. db.t3.micro)"
}

variable "db_allocated_storage" {
  type        = number
  description = "Allocated Storage in GB"
}

variable "environment" {
  type        = string
  description = "Environment name"
}
