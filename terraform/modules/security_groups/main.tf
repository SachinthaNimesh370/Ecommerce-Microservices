# Security Group for EKS Control Plane
resource "aws_security_group" "eks_cluster" {
  name        = "ecommerce-eks-cluster-sg-${var.environment}"
  description = "Security group for EKS control plane"
  vpc_id      = var.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "ecommerce-eks-cluster-sg-${var.environment}"
  }
}

# Security Group for EKS Worker Nodes
resource "aws_security_group" "eks_nodes" {
  name        = "ecommerce-eks-nodes-sg-${var.environment}"
  description = "Security group for worker nodes in EKS cluster"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
  }

  ingress {
    from_port       = 1025
    to_port         = 65535
    protocol        = "tcp"
    security_groups = [aws_security_group.eks_cluster.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "ecommerce-eks-nodes-sg-${var.environment}"
  }
}

# Allow EKS cluster to reach worker nodes
resource "aws_security_group_rule" "cluster_to_nodes" {
  type                     = "ingress"
  from_port                = 443
  to_port                  = 443
  protocol                 = "tcp"
  security_group_id        = aws_security_group.eks_cluster.id
  source_security_group_id = aws_security_group.eks_nodes.id
}

# Security Group for Amazon RDS PostgreSQL
resource "aws_security_group" "rds" {
  name        = "ecommerce-rds-sg-${var.environment}"
  description = "Allow inbound PostgreSQL access from EKS nodes"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.eks_nodes.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "ecommerce-rds-sg-${var.environment}"
  }
}

# Security Group for Amazon MSK Kafka
resource "aws_security_group" "msk" {
  name        = "ecommerce-msk-sg-${var.environment}"
  description = "Allow inbound Kafka traffic from EKS nodes"
  vpc_id      = var.vpc_id

  # Plaintext Kafka Broker Port
  ingress {
    from_port       = 9092
    to_port         = 9092
    protocol        = "tcp"
    security_groups = [aws_security_group.eks_nodes.id]
  }

  # TLS Encrypted Kafka Broker Port
  ingress {
    from_port       = 9094
    to_port         = 9094
    protocol        = "tcp"
    security_groups = [aws_security_group.eks_nodes.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "ecommerce-msk-sg-${var.environment}"
  }
}
