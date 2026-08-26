resource "aws_eks_cluster" "cluster" {
  name     = "${var.cluster_name}-${var.environment}"
  role_arn = var.cluster_role_arn
  version  = "1.34"

  vpc_config {
    subnet_ids              = var.subnet_ids
    security_group_ids      = [var.cluster_sg_id]
    endpoint_private_access = true
    endpoint_public_access  = true
  }

  tags = {
    Name = "${var.cluster_name}-${var.environment}"
  }
}

resource "aws_eks_node_group" "nodes" {
  cluster_name    = aws_eks_cluster.cluster.name
  node_group_name = "ecommerce-node-group-${var.environment}"
  node_role_arn   = var.node_role_arn
  subnet_ids      = var.subnet_ids

  ami_type        = "AL2023_x86_64_STANDARD"
  instance_types  = var.node_instance_types

  scaling_config {
    desired_size = var.desired_nodes
    max_size     = var.max_nodes
    min_size     = var.min_nodes
  }

  update_config {
    max_unavailable = 1
  }

  tags = {
    Name = "ecommerce-node-group-${var.environment}"
  }

  depends_on = [
    aws_eks_cluster.cluster
  ]
}

data "tls_certificate" "eks" {
  url = aws_eks_cluster.cluster.identity[0].oidc[0].issuer
}

resource "aws_iam_openid_connect_provider" "oidc" {
  count           = 1
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.eks.certificates[0].sha1_fingerprint]
  url             = aws_eks_cluster.cluster.identity[0].oidc[0].issuer
}
