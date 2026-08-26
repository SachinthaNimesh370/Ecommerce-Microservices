terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Optional S3 Backend for remote state management:
  # backend "s3" {
  #   bucket         = "ecommerce-terraform-state-bucket"
  #   key            = "dev/terraform.tfstate"
  #   region         = "us-east-1"
  #   dynamodb_table = "ecommerce-terraform-locks"
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "E-Commerce-Microservices"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}
