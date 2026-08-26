# 🛒 Cloud-Native E-Commerce Microservices Platform

[![Java 17](https://img.shields.io/badge/Java-17%2B-blue?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%20%2F%204.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-red?logo=apachekafka)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Multi--Container-2496ED?logo=docker)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-EKS-326CE5?logo=kubernetes)](https://kubernetes.io/)
[![Terraform](https://img.shields.io/badge/Terraform-IaC-7B42BC?logo=terraform)](https://www.terraform.io/)
[![Ansible](https://img.shields.io/badge/Ansible-Automation-EE0000?logo=ansible)](https://www.ansible.com/)
[![AWS CloudWatch](https://img.shields.io/badge/AWS-CloudWatch%20Observability-FF9900?logo=amazonaws)](https://aws.amazon.com/cloudwatch/)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?logo=githubactions)](https://github.com/features/actions)

A complete, enterprise-grade, event-driven **E-Commerce Microservices Platform** built with **Java 17 & Spring Boot**, decoupled with **Apache Kafka (KRaft)**, provisioned via **Terraform** on **AWS Cloud**, orchestrated with **Kubernetes (Amazon EKS)**, configured using **Ansible**, monitored via **AWS CloudWatch & Container Insights**, and continuously deployed through **GitHub Actions CI/CD**.

---

## 📑 Table of Contents

- [1. System Architecture](#1-system-architecture)
- [2. Microservices Summary](#2-microservices-summary)
- [3. Key Architectural Features](#3-key-architectural-features)
- [4. Event-Driven Architecture (Kafka)](#4-event-driven-architecture-kafka)
- [5. Complete DevOps & Cloud Roadmap (Parts A – S)](#5-complete-devops--cloud-roadmap-parts-a--s)
- [6. Project Structure](#6-project-structure)
- [7. Getting Started: Local Development](#7-getting-started-local-development)
- [8. Production Cloud Deployment (AWS EKS)](#8-production-cloud-deployment-aws-eks)
- [9. Configuration Management with Ansible](#9-configuration-management-with-ansible)
- [10. Observability, Monitoring & Alarms (CloudWatch)](#10-observability-monitoring--alarms-cloudwatch)
- [11. API Reference & Testing](#11-api-reference--testing)
- [12. Sub-Guides Index](#12-sub-guides-index)

---

## 1. System Architecture

```text
                                 +-------------------------+
                                 |   Clients & Frontends   |
                                 +------------+------------+
                                              |
                                              v
                              +-------------------------------+
                              |    AWS ALB / Ingress (80)     |
                              +---------------+---------------+
                                              |
                                              v
                              +-------------------------------+
                              |      API Gateway (:8080)      |
                              |  (Spring Cloud Gateway + JWT) |
                              +-------+-------+-------+-------+
                                      |       |       |
                 +--------------------+       |       +--------------------+
                 |                            |                            |
                 v                            v                            v
      +--------------------+       +--------------------+       +--------------------+
      |    User Service    |       |  Product Service   |       |   Order Service    |
      |      (:8081)       |       |      (:8082)       |       |      (:8083)       |
      +---------+----------+       +---------+----------+       +---------+----------+
                |                            |                            |
                v                            v                            | (Publishes Events)
      +--------------------+       +--------------------+                 v
      |   user_db (Postgres|       | product_db (Postgr)|        +-------------------+
      +--------------------+       +--------------------+        |  Kafka Event Bus  |
                                                                 |      (:9092)      |
                                                                 +---------+---------+
                                                                           |
                                              +----------------------------+
                                              | (Consumes Events)
                                              v
                                 +--------------------------+--------------------------+
                                 |                                                     |
                                 v                                                     v
                      +--------------------+                                +--------------------+
                      | Inventory Service  |                                |Notification Service|
                      |      (:8084)       |                                |      (:8085)       |
                      +---------+----------+                                +--------------------+
                                |
                                v
                      +--------------------+
                      |inventory_db(Postgr)|
                      +--------------------+
```

---

## 2. Microservices Summary

| Service | Port | Database | Primary Responsibility |
| :--- | :---: | :---: | :--- |
| **`api-gateway`** | `8080` | *None* | Central reverse proxy, routing, rate limiting, JWT token validation & relay. |
| **`user-service`** | `8081` | `user_db` | Authentication (JWT), User Registration, Profile Management, Role-Based Access (`CUSTOMER`, `ADMIN`). |
| **`product-service`** | `8082` | `product_db` | Product catalog management, categories, inventory price checks. |
| **`order-service`** | `8083` | `order_db` | Order placement, order cancellation, and publishing order events to Kafka. |
| **`inventory-service`**| `8084` | `inventory_db`| Asynchronous stock reservation, inventory validation, and auto-stock updates. |
| **`notification-service`**| `8085` | *None* | Consumes events and triggers customer notifications (Email / SMS / Push mock). |

---

## 3. Key Architectural Features

- **Database-per-Service Pattern**: Strict microservice isolation with dedicated PostgreSQL databases (`user_db`, `product_db`, `order_db`, `inventory_db`).
- **Defense-in-Depth Security**: Stateless JWT Authentication validated at the API Gateway and re-verified in downstream microservice security filters.
- **Event-Driven Resilience**: Order processing and inventory reduction are decoupled via Apache Kafka message queues, guaranteeing eventual consistency without blocking client HTTP requests.
- **Fault-Tolerant Infrastructure**: AWS RDS PostgreSQL Multi-AZ, EKS multi-node auto-scaling, and rolling updates with zero downtime.

---

## 4. Event-Driven Architecture (Kafka)

The platform utilizes three dedicated Kafka topics in KRaft mode:

```text
[order-service] ──(publishes)──► [order-created]   ──► [inventory-service] (reduces stock)
                                                   ──► [notification-service] (sends confirmation)

[order-service] ──(publishes)──► [order-cancelled] ──► [inventory-service] (restores stock)
                                                   ──► [notification-service] (sends cancellation)

[inventory-service] ─(publishes)─► [stock-updated] ──► [notification-service] (low stock alert)
```

---

## 5. Complete DevOps & Cloud Roadmap (Parts A – S)

This repository fulfills the complete end-to-end curriculum roadmap:

```text
SOFTWARE DEVELOPMENT
├── Part A — User Service (JWT & Spring Security)
├── Part B — Product Service (Catalog & CRUD APIs)
├── Part C — Order Service (Order Management & Event Publishing)
├── Part D — Kafka Event-Driven Integration
├── Part E — Inventory Service (Async Stock Processing)
├── Part F — Notification Service (Event Consumer)
├── Part G — API Gateway (Spring Cloud Gateway)
├── Part H — Service-to-Service Communication (REST + Async Kafka)
├── Part I — Configuration & Secrets Management
├── Part J — Unit & Integration Testing
└── Part K — Spring Boot Actuator (Health & Metrics Probes)

DEVOPS & CLOUD DEPLOYMENT
├── Part L — Docker Containerization (Multi-stage Dockerfiles)
├── Part M — Docker Compose Stack (Local 11-Container Environment)
├── Part N — CI/CD Pipeline (GitHub Actions Matrix Build, Trivy Scanner, GHCR)
├── Part O — Terraform Infrastructure as Code (VPC, EKS, RDS, ECR, IAM, SGs)
├── Part P — Kubernetes Orchestration (Deployments, RollingUpdate, HPA, Probes)
├── Part Q — AWS Cloud Architecture (EKS 4-Node Cluster, Managed RDS Postgres)
├── Part R — Ansible Configuration Management (System Hardening, Docker Engine, Bastions)
└── Part S — CloudWatch Observability (Container Insights, Fluent Bit, Alarms, Dashboard)
```

---

## 6. Project Structure

```text
.
├── .github/workflows/               # GitHub Actions CI/CD Pipelines
│   └── ci-cd.yml
├── ansible/                         # Part R: Ansible Configuration Management
│   ├── ansible.cfg                  # Global Ansible configuration
│   ├── run-ansible.ps1              # PowerShell containerized execution helper
│   ├── inventory/hosts.ini          # Target host inventories (local, staging, prod)
│   ├── roles/                       # Modular roles (common, docker, microservices, k8s_tools)
│   └── playbooks/site.yml           # Master orchestration playbook
├── api-gateway/                     # Spring Cloud API Gateway (Port 8080)
├── user-service/                    # Authentication & User Management (Port 8081)
├── product-service/                 # Product Catalog & Inventory (Port 8082)
├── order-service/                   # Order Management & Event Producer (Port 8083)
├── inventory-service/               # Stock Management & Consumer (Port 8084)
├── notification-service/            # Notification Consumer (Port 8085)
├── docker-compose.yml               # Local Multi-Container Docker Compose Stack
├── k8s/                             # Part P: Kubernetes & EKS Manifests
│   ├── kustomization.yaml           # Unified Kustomize deployment manifest
│   ├── ingress.yaml                 # AWS ALB Ingress routing rules
│   ├── kafka/                       # Kafka deployment & service
│   ├── create-databases-job.yaml    # Automated RDS multi-database init job
│   ├── cloudwatch-dashboard.json    # Part S: CloudWatch Dashboard JSON definition
│   └── */                           # Per-service Deployment, Service, ConfigMap, HPA
├── terraform/                       # Part O: Terraform Infrastructure as Code
│   ├── main.tf                      # Core provider & module orchestration
│   ├── terraform.tfvars             # Environment variables (Node sizes, AZs, CIDRs)
│   └── modules/                     # VPC, Security Groups, EKS, RDS, ECR, MSK
├── ANSIBLE_GUIDE.md                 # Part R Guide
├── CICD_GUIDE.md                    # Part N Guide
├── CLOUDWATCH_GUIDE.md              # Part S Guide
├── KUBERNETES_GUIDE.md              # Part P Guide
└── TERRAFORM_GUIDE.md               # Part O Guide
```

---

## 7. Getting Started: Local Development

### Prerequisites
- Java 17+ (JDK)
- Maven 3.8+
- Docker & Docker Compose

### 1. Clone Repository
```bash
git clone https://github.com/SachinthaNimesh370/Ecommerce-Microservices.git
cd Ecommerce-Microservices
```

### 2. Start Complete Stack with Docker Compose
```bash
docker compose up -d --build
```

### 3. Verify Container Health
```bash
docker compose ps
```

---

## 8. Production Cloud Deployment (AWS EKS)

### 1. Provision Infrastructure with Terraform
```powershell
cd terraform
terraform init
terraform plan
terraform apply -auto-approve
```

### 2. Connect `kubectl` to EKS Cluster
```powershell
aws eks update-kubeconfig --region us-east-1 --name ecommerce-eks-cluster-dev
```

### 3. Deploy Kubernetes Manifests
```powershell
kubectl apply -k k8s/
```

### 4. Check Cluster Pod Status
```powershell
kubectl get pods -n ecommerce
```

---

## 9. Configuration Management with Ansible

Automate server hardening, Docker engine configuration, and bastion tools:

```powershell
cd ansible

# Check playbook syntax
.\run-ansible.ps1 ansible-playbook playbooks/site.yml --syntax-check

# Run full end-to-end orchestration
.\run-ansible.ps1 ansible-playbook playbooks/site.yml
```

---

## 10. Observability, Monitoring & Alarms (CloudWatch)

### Components Deployed
- **Amazon CloudWatch Observability Add-on**: `cloudwatch-agent` and `fluent-bit` active on all cluster nodes.
- **Log Streams**: Centralized logging in `/aws/containerinsights/ecommerce-eks-cluster-dev/application`.
- **Live Metrics Dashboard**: `Ecommerce-Microservices-Observability` tracking Pod CPU/Memory, Node CPU, and RDS PostgreSQL health.
- **CloudWatch Alarms & SNS Alerting**:
  - `ecommerce-order-service-high-cpu` (Pod CPU $\ge$ 85%)
  - `ecommerce-eks-node-high-cpu` (Node CPU $\ge$ 85%)
  - `ecommerce-rds-high-cpu` (RDS CPU $\ge$ 80%)
  - `ecommerce-rds-low-memory` (Freeable Memory $\le$ 100MB)

---

## 11. API Reference & Testing

All external requests are routed through the API Gateway on port `8080`:

### Auth & User Endpoints (`user-service`)
* `POST /api/auth/register` — Register a new user
* `POST /api/auth/login` — Authenticate and receive Bearer JWT token
* `GET  /api/users/me` — Retrieve current authenticated user profile

### Product Endpoints (`product-service`)
* `GET    /api/products` — List all products (Public)
* `GET    /api/products/{id}` — Get product details (Public)
* `POST   /api/products` — Create new product (*Admin only*)
* `PUT    /api/products/{id}` — Update product (*Admin only*)
* `DELETE /api/products/{id}` — Delete product (*Admin only*)

### Order Endpoints (`order-service`)
* `POST /api/orders` — Create new order (Publishes `order-created` event)
* `GET  /api/orders` — List orders for authenticated customer
* `GET  /api/orders/{id}` — Get order status
* `PUT  /api/orders/{id}/cancel` — Cancel order (Publishes `order-cancelled` event)

---

## 12. Sub-Guides Index

For detailed deep-dives into specific DevOps stages:

* 📖 [**TERRAFORM_GUIDE.md**](file:///c:/Users/sachi/Desktop/user-service/TERRAFORM_GUIDE.md) — AWS Cloud Infrastructure as Code setup, costs & teardown.
* 📖 [**KUBERNETES_GUIDE.md**](file:///c:/Users/sachi/Desktop/user-service/KUBERNETES_GUIDE.md) — EKS Cluster manifests, RollingUpdates, and HPA autoscaling.
* 📖 [**CICD_GUIDE.md**](file:///c:/Users/sachi/Desktop/user-service/CICD_GUIDE.md) — GitHub Actions multi-service matrix pipeline, Maven builds, and Trivy security scanning.
* 📖 [**ANSIBLE_GUIDE.md**](file:///c:/Users/sachi/Desktop/user-service/ANSIBLE_GUIDE.md) — Ansible roles, inventories, and server configuration management.
* 📖 [**CLOUDWATCH_GUIDE.md**](file:///c:/Users/sachi/Desktop/user-service/CLOUDWATCH_GUIDE.md) — CloudWatch Container Insights, Fluent Bit logs, Alarms, and Dashboard.

---

## 👨‍💻 Author

**Sachintha Nimesh**  
GitHub: [@SachinthaNimesh370](https://github.com/SachinthaNimesh370)
