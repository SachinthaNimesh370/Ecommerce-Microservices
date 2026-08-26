resource "aws_db_subnet_group" "rds" {
  name       = "ecommerce-rds-subnet-group-${var.environment}"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "ecommerce-rds-subnet-group-${var.environment}"
  }
}

resource "aws_db_parameter_group" "postgres15" {
  name   = "ecommerce-pg15-params-${var.environment}"
  family = "postgres15"

  parameter {
    name  = "log_connections"
    value = "1"
  }

  tags = {
    Name = "ecommerce-pg15-params-${var.environment}"
  }
}

resource "aws_db_instance" "postgres" {
  identifier             = "ecommerce-postgres-${var.environment}"
  engine                 = "postgres"
  engine_version         = "15.7"
  instance_class         = var.db_instance_class
  allocated_storage      = var.db_allocated_storage
  max_allocated_storage  = 50
  storage_type           = "gp2"
  db_name                = var.db_name
  username               = var.db_username
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.rds.name
  parameter_group_name   = aws_db_parameter_group.postgres15.name
  vpc_security_group_ids = [var.rds_sg_id]
  skip_final_snapshot    = true
  publicly_accessible    = false

  tags = {
    Name = "ecommerce-postgres-${var.environment}"
  }
}
