# ─── VPC Module ───────────────────────────────────────────────────────────────
module "vpc" {
  source               = "./modules/vpc"
  vpc_cidr             = var.vpc_cidr
  environment          = var.environment
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
}

# ─── Security Groups Module ──────────────────────────────────────────────────
module "security_groups" {
  source      = "./modules/security_groups"
  vpc_id      = module.vpc.vpc_id
  environment = var.environment
}

# ─── IAM Roles & Policies Module ─────────────────────────────────────────────
module "iam" {
  source      = "./modules/iam"
  environment = var.environment
}

# ─── Amazon ECR Repositories Module ──────────────────────────────────────────
module "ecr" {
  source           = "./modules/ecr"
  repository_names = var.ecr_repository_names
  environment      = var.environment
}

# ─── Amazon RDS PostgreSQL Module ───────────────────────────────────────────
module "rds" {
  source               = "./modules/rds"
  vpc_id               = module.vpc.vpc_id
  private_subnet_ids   = module.vpc.private_subnet_ids
  rds_sg_id            = module.security_groups.rds_sg_id
  db_name              = var.db_name
  db_username          = var.db_username
  db_password          = var.db_password
  db_instance_class    = var.db_instance_class
  db_allocated_storage = var.db_allocated_storage
  environment          = var.environment
}

# ─── Amazon MSK Kafka Module (Optional) ──────────────────────────────────────
module "msk" {
  count              = var.enable_msk ? 1 : 0
  source             = "./modules/msk"
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  msk_sg_id          = module.security_groups.msk_sg_id
  cluster_name       = var.msk_cluster_name
  environment        = var.environment
}

# ─── Amazon EKS Kubernetes Cluster Module ────────────────────────────────────
module "eks" {
  source              = "./modules/eks"
  cluster_name        = var.eks_cluster_name
  vpc_id              = module.vpc.vpc_id
  subnet_ids          = module.vpc.private_subnet_ids
  cluster_role_arn    = module.iam.eks_cluster_role_arn
  node_role_arn       = module.iam.eks_node_role_arn
  cluster_sg_id       = module.security_groups.eks_cluster_sg_id
  node_instance_types = var.eks_node_instance_types
  desired_nodes       = var.eks_desired_nodes
  min_nodes           = var.eks_min_nodes
  max_nodes           = var.eks_max_nodes
  environment         = var.environment
}
