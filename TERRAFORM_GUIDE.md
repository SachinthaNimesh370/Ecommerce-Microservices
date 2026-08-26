# Part O — Terraform Infrastructure as Code Guide

This document details the **Part O — Terraform** infrastructure implementation and step-by-step guide to setup AWS credentials, execute Terraform lifecycle commands, test on AWS Free Tier, and completely destroy/clean up resources after testing.

---

## 1. Overview & Architecture

Terraform provisions the entire cloud footprint required for the E-Commerce Microservices platform on AWS:

```text
Terraform Infrastructure (AWS)
├── VPC & Subnets (Public & Private Subnets across 2 Availability Zones, IGW, NAT Gateway)
├── Security Groups (EKS Cluster & Nodes, RDS PostgreSQL, MSK Kafka)
├── IAM (EKS Cluster Role, Managed Node Role, ECR Policies, OIDC IRSA)
├── ECR (6 Repositories: api-gateway, user-service, product-service, order-service, inventory-service, notification-service)
├── RDS (Amazon RDS PostgreSQL Database Instance db.t3.micro, Subnet Group, Parameter Group)
├── MSK (Amazon Managed Streaming for Apache Kafka Cluster - Optional toggle)
└── EKS (Amazon Elastic Kubernetes Service Cluster & Worker Node Group)
```

---

## 2. AWS Account & Credentials Setup Guide

To connect Terraform to your AWS Account, follow these steps to obtain access credentials:

### Step 2.1: Create AWS IAM User & Access Keys
1. Log in to the [AWS Management Console](https://aws.amazon.com/console/).
2. Navigate to **IAM** (Identity and Access Management) -> **Users**.
3. Click **Create User**:
   - **User Name**: `terraform-admin`
   - Select **Attach policies directly** and attach `AdministratorAccess` (or custom policies for VPC, EKS, RDS, ECR, IAM).
4. After user creation, click on `terraform-admin` -> **Security credentials** tab.
5. Scroll down to **Access keys** and click **Create access key**:
   - Use case: Select **Command Line Interface (CLI)**.
   - Check the acknowledgement checkbox and click **Next**.
6. **Save your credentials**:
   - **Access Key ID**: (e.g. `AKIAIOSFODNN7EXAMPLE`)
   - **Secret Access Key**: (e.g. `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`)

---

## 3. Tooling Installation (Windows)

### Option A: Install via PowerShell (winget)
Open PowerShell as Administrator and run:

```powershell
# Install Terraform CLI
winget install HashiCorp.Terraform

# Install AWS CLI
winget install Amazon.AWSCLI
```

> **Note**: After installing, close and reopen your PowerShell terminal window so your system `PATH` updates.

### Option B: Configure AWS Credentials in Terminal
In your PowerShell terminal, export your AWS Access Keys:

```powershell
$env:AWS_ACCESS_KEY_ID="YOUR_ACCESS_KEY_ID"
$env:AWS_SECRET_ACCESS_KEY="YOUR_SECRET_ACCESS_KEY"
$env:AWS_DEFAULT_REGION="us-east-1"
```

Or configure via AWS CLI:
```powershell
aws configure
# Enter Access Key ID, Secret Access Key, Default region (us-east-1), and Output format (json)
```

---

## 4. Free Tier & Cost Optimization Settings

To keep AWS costs minimal during testing:

| Resource | Terraform Setting | Free Tier Status |
| :--- | :--- | :--- |
| **RDS PostgreSQL** | `db_instance_class = "db.t3.micro"`, `db_allocated_storage = 20` | **100% Free** (750 hrs/mo for 12 months) |
| **ECR Repositories** | 6 microservice repos (`scan_on_push = true`) | **100% Free** (500MB storage free per month) |
| **VPC & Subnets** | Multi-AZ VPC, IGW, Subnets, Security Groups | **100% Free** |
| **EKS Worker Nodes** | `t3.micro` instance types (2 nodes) | Minimal compute cost |
| **Amazon MSK** | `enable_msk = false` (default) | Set `enable_msk = false` to skip MSK charges during initial tests |

---

## 5. Execution Steps: Running Terraform Commands

Navigate to the `terraform/` directory:

```powershell
cd c:\Users\sachi\Desktop\user-service\terraform
```

### Stage 1: Initialize Terraform (`terraform init`)
Downloads the AWS provider plugins (`hashicorp/aws` ~> 5.0) and initializes local child modules:

```powershell
terraform init
```

### Stage 2: Validate Syntax (`terraform validate`)
Checks the code syntax, variable bindings, and structural consistency:

```powershell
terraform validate
```

### Stage 3: Dry-Run Plan (`terraform plan`)
Generates an execution plan showing all resources that will be provisioned **without modifying AWS or spending money**:

```powershell
terraform plan
```

### Stage 4: Apply & Provision Infrastructure (`terraform apply`)
Deploys real resources to AWS (asks for user confirmation `yes`):

```powershell
terraform apply
```

To auto-approve execution:
```powershell
terraform apply -auto-approve
```

### Stage 5: Inspect Outputs (`terraform output`)
After deployment completes, retrieve resource URLs and endpoints:

```powershell
terraform output
```

Output includes:
- `rds_endpoint`: Connection string for PostgreSQL database
- `ecr_repository_urls`: ECR repository URLs for Docker image pushes
- `eks_cluster_endpoint`: Kubernetes API server endpoint
- `vpc_id`: VPC identifier

---

## 6. IMPORTANT: Resource Teardown & Deletion (`terraform destroy`)

> [!WARNING]
> **Must Remove AWS Resources After Testing**
> To avoid ongoing cloud charges, always destroy all provisioned instances and services once testing is complete!

Run the single teardown command:

```powershell
terraform destroy
```

When prompted `Enter a value:`, type `yes` and press Enter.

To destroy automatically without prompting:
```powershell
terraform destroy -auto-approve
```

### Verification Checklist After Teardown:
1. Check output message: `Destroy complete! Resources: XX destroyed.`
2. Verify AWS Console:
   - **EC2 / EKS**: No active worker node instances or clusters.
   - **RDS**: PostgreSQL instance deleted.
   - **VPC**: VPC and NAT Gateways removed.

---

## 7. CI/CD GitHub Actions Integration

When deploying via GitHub Actions, add your AWS credentials as Secrets in your GitHub Repository (**Settings** -> **Secrets and variables** -> **Actions**):

- `AWS_ACCESS_KEY_ID`: Your IAM user access key ID
- `AWS_SECRET_ACCESS_KEY`: Your IAM user secret access key
- `AWS_REGION`: `us-east-1`
