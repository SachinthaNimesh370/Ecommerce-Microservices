# 📘 E-Commerce Microservices: Complete Developer & Engineering Handover Report

---

## 1. Executive Summary & Project Identification

* **Project Name**: Cloud-Native E-Commerce Microservices Platform
* **Repository**: [`SachinthaNimesh370/Ecommerce-Microservices`](https://github.com/SachinthaNimesh370/Ecommerce-Microservices)
* **Author / Engineer**: Sachintha Nimesh
* **Target Cloud**: Amazon Web Services (AWS us-east-1)
* **Architecture Style**: Event-Driven Microservices (Spring Boot + Apache Kafka + PostgreSQL + Kubernetes EKS)

---

## 2. Master Credentials & Technical Parameters Reference

> ⚠️ **CONFIDENTIAL DEVELOPER REFERENCE** — Keep this file secure within authorized workspace environments.

### A. AWS Cloud Infrastructure
| Parameter | Configured Value | Notes |
| :--- | :--- | :--- |
| **AWS Account ID** | `465139034981` | IAM Primary Account |
| **AWS Region** | `us-east-1` | US East (N. Virginia) |
| **VPC ID** | `vpc-02cf27fb0739beadf` | CIDR `10.0.0.0/16` |
| **EKS Cluster Name** | `ecommerce-eks-cluster-dev` | Kubernetes v1.34.x / EKS |
| **EKS Node Group** | `ecommerce-node-group-dev` | 4 × `t3.micro` EC2 instances (Free Tier optimized) |
| **Node IAM Role** | `ecommerce-eks-node-role-dev` | Attached policies: EKSWorker, EKSCNI, EC2ContainerReg, CloudWatchAgent |
| **EKS OIDC Provider** | `arn:aws:iam::465139034981:oidc-provider/oidc.eks.us-east-1.amazonaws.com/id/DF4B655821659E342B58FBEF6851E003` | Used for IRSA (IAM Roles for Service Accounts) |

### B. Amazon RDS PostgreSQL
| Parameter | Configured Value |
| :--- | :--- |
| **RDS Instance Identifier** | `ecommerce-postgres-dev` |
| **RDS Endpoint (Host)** | `ecommerce-postgres-dev.c2tmcgaiislf.us-east-1.rds.amazonaws.com` |
| **RDS Port** | `5432` |
| **Master Username** | `postgres` |
| **Master Password** | `NewStrongPassword2026!` |
| **Allocated Storage** | `20 GB` (gp2/gp3) |
| **Individual Databases Created** | `ecommerce_db`, `user_db`, `product_db`, `order_db`, `inventory_db` |
| **Security Group** | `sg-0b9ede456331a32ac` (Inbound port 5432 allowed from EKS Node SG `sg-0ac4df36e48e82984`) |

### C. Security Secrets & Token Specifications
| Secret Key | Value / Specification | Description |
| :--- | :--- | :--- |
| **`DB_PASSWORD`** | `NewStrongPassword2026!` | Applied to Kubernetes Secret `ecommerce-secrets` in namespace `ecommerce` |
| **`JWT_SECRET`** | `404E635266556A586E3272357538782F413F4428472B4B6250655368566D5970` | 256-bit HMAC SHA-256 secret key for signing & validating auth tokens |
| **Default User Roles** | `ROLE_CUSTOMER`, `ROLE_ADMIN` | Configured in `user-service` Spring Security filter chain |

### D. Microservices Network & Container Registry Ports
| Microservice | Container Port | Service Type | ECR Repository URI |
| :--- | :---: | :---: | :--- |
| **`api-gateway`** | `8080` | ClusterIP / LoadBalancer | `465139034981.dkr.ecr.us-east-1.amazonaws.com/api-gateway:latest` |
| **`user-service`** | `8081` | ClusterIP | `465139034981.dkr.ecr.us-east-1.amazonaws.com/user-service:latest` |
| **`product-service`** | `8082` | ClusterIP | `465139034981.dkr.ecr.us-east-1.amazonaws.com/product-service:latest` |
| **`order-service`** | `8083` | ClusterIP | `465139034981.dkr.ecr.us-east-1.amazonaws.com/order-service:latest` |
| **`inventory-service`**| `8084` | ClusterIP | `465139034981.dkr.ecr.us-east-1.amazonaws.com/inventory-service:latest` |
| **`notification-service`**| `8085` | ClusterIP | `465139034981.dkr.ecr.us-east-1.amazonaws.com/notification-service:latest` |
| **`kafka`** | `9092` | ClusterIP | Internal KRaft cluster (`kafka:9092`) |

---

## 3. End-to-End System Architecture

```text
                                 +-------------------------+
                                 |   Clients & Postman     |
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
      |   user_db (RDS)    |       |  product_db (RDS)  |        +-------------------+
      +--------------------+       +--------------------+        |  Kafka Event Bus  |
                                                                 |  (KRaft mode:9092)|
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
                      | inventory_db (RDS) |
                      +--------------------+
```

---

## 4. Comprehensive Troubleshooting & Problem-Solving Log

During the development and cloud deployment lifecycle, 14 major challenges were identified, diagnosed, and resolved:

### Issue 1: Kafka Local Docker Topic Creation Timeout
* **Symptom**: `TimeoutException: Timed out waiting for a node assignment` during local startup.
* **Root Cause**: Stale Kafka metadata state in local container volume when transitioning to KRaft mode.
* **Solution**: Cleaned stale containers and initialized Kafka 4.0.1 with explicit KRaft cluster IDs:
  ```powershell
  docker compose down -v
  docker compose up -d kafka
  ```

### Issue 2: ECR Docker Image Push Timeouts
* **Symptom**: `failed commit on ref: net/http: timeout awaiting response headers` when pushing multi-megabyte JAR layers.
* **Root Cause**: Network latency and transient connection resets on large initial layer uploads.
* **Solution**: Retried the push; Docker engine utilized already-cached layer blobs (`Layer already exists`), completing digest verification successfully.

### Issue 3: Terraform State File Lock Collision
* **Symptom**: `Error: Error acquiring the state lock: The process cannot access the file because another process has locked a portion of the file`.
* **Root Cause**: An active background `terraform apply -auto-approve` process (PID 12384) was already executing.
* **Solution**: Allowed the running operation to finish cleanly rather than force-killing state locks.

### Issue 4: EKS Insufficient Memory & Pod Pending State
* **Symptom**: `0/2 nodes are available: 2 Insufficient memory, Too many pods`. All 12 application pods remained in `Pending`.
* **Root Cause**: 2 × `t3.micro` nodes (1GB RAM each) did not have enough allocatable RAM after Kubernetes system overhead (`kube-system`, CoreDNS, CNI) to schedule 6 Spring Boot services × 2 replicas + Kafka.
* **Solution**: Tuned deployment memory requests to `requests: 50m CPU, 128Mi RAM` and scaled the node group to **4 × `t3.micro` worker nodes** in `terraform/terraform.tfvars`.

### Issue 5: `t3.medium` AWS Free-Tier Ineligibility Rejection
* **Symptom**: `InvalidParameterCombination: The specified instance type is not eligible for Free Tier`.
* **Root Cause**: The AWS account had strict Free-Tier enforcement preventing `t3.medium` provisioning.
* **Solution**: Ran `aws ec2 describe-instance-types --filters Name=free-tier-eligible,Values=true`, selected `t3.micro`, and distributed pod load across 4 nodes.

### Issue 6: Kubernetes `InvalidImageName` Pod Errors
* **Symptom**: Newly rolled-out pods failed with `InvalidImageName`.
* **Root Cause**: Deployment YAML manifests contained placeholder strings (`<AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/...`) spanning multiple lines.
* **Solution**: Executed `kubectl set image` with real ECR URLs (`465139034981.dkr.ecr.us-east-1.amazonaws.com/...`) and patched all YAML deployment files.

### Issue 7: Stale ReplicaSets Spawning Broken Pods
* **Symptom**: Even after correcting image paths, old broken pods kept spawning.
* **Root Cause**: 14 old ReplicaSets created during earlier test rollouts remained in the cluster with placeholder specs.
* **Solution**: Filtered and batch-deleted all stale ReplicaSets using PowerShell + `kubectl delete rs`.

### Issue 8: Missing Databases in Amazon RDS (`FATAL: database "order_db" does not exist`)
* **Symptom**: `GenericJDBCException: Unable to obtain isolated JDBC connection [FATAL: database "order_db" does not exist]`.
* **Root Cause**: Terraform provisions a single default database (`ecommerce_db`) in Amazon RDS, whereas the microservices architecture requires 4 distinct databases (`user_db`, `product_db`, `order_db`, `inventory_db`).
* **Solution**: Created and deployed a Kubernetes Job (`k8s/create-databases-job.yaml`) running `psql` to create all 4 databases on RDS.

### Issue 9: RDS Master Password Mismatch & Secret Rotation
* **Symptom**: Authentication failures (`password authentication failed for user "postgres"`).
* **Root Cause**: The database was reset with password `NewStrongPassword2026!`, but Kubernetes Secret `ecommerce-secrets` still contained the initial password.
* **Solution**: Updated Kubernetes Secret dynamically:
  ```powershell
  kubectl create secret generic ecommerce-secrets -n ecommerce `
    --from-literal=DB_PASSWORD="NewStrongPassword2026!" `
    --from-literal=JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250655368566D5970" `
    --dry-run=client -o yaml | kubectl apply -f -
  kubectl rollout restart deployment user-service product-service order-service inventory-service -n ecommerce
  ```

### Issue 10: Spring Boot Aggressive Health Probe Restarts
* **Symptom**: Pods restarted cyclically (`CrashLoopBackOff`) with `Liveness probe failed: connect: connection refused`.
* **Root Cause**: Spring Boot on `t3.micro` requires 60–90 seconds to bootstrap Hibernate, scan JPA entities, and open Tomcat. The initial `initialDelaySeconds: 40` killed the pod before startup finished.
* **Solution**: Increased probe delay in all deployments to `initialDelaySeconds: 90` (liveness) and `initialDelaySeconds: 75` (readiness) with `failureThreshold: 5`.

### Issue 11: Kafka JVM Memory OOMKilled
* **Symptom**: Kafka pod crashed with `Exit Code: 137` (Linux OOM killer).
* **Root Cause**: Default Apache Kafka JVM heap tried allocating 1GB+ RAM, exceeding container memory limits (`512Mi`).
* **Solution**: Added `KAFKA_HEAP_OPTS: "-Xmx256M -Xms256M"` and explicit `KAFKA_LOG_DIRS: "/tmp/kraft-combined-logs"` in `k8s/kafka/deployment.yaml`.

### Issue 12: Ingress ALB Missing Address (AWS Load Balancer Controller)
* **Symptom**: `kubectl get ingress -n ecommerce` showed empty `ADDRESS` after 5+ hours.
* **Root Cause**: AWS Load Balancer Controller was not installed on the EKS cluster.
* **Solution**:
  1. Created IAM Policy `AWSLoadBalancerControllerIAMPolicy`.
  2. Created IAM Role `AmazonEKSLoadBalancerControllerRole` with OIDC IRSA trust policy.
  3. Created and annotated ServiceAccount `aws-load-balancer-controller` in `kube-system`.
  4. Installed `cert-manager` v1.14.5.
  5. Deployed Helm chart `eks/aws-load-balancer-controller` with explicit `vpcId=vpc-02cf27fb0739beadf` and `region=us-east-1`.

### Issue 13: Ansible Windows Execution Limitation
* **Symptom**: `ansible : The term 'ansible' is not recognized as the name of a cmdlet`.
* **Root Cause**: Ansible control node requires a POSIX/Linux environment and does not run natively on Windows PowerShell.
* **Solution**: Built [`ansible/run-ansible.ps1`](file:///c:/Users/sachi/Desktop/user-service/ansible/run-ansible.ps1) to run Ansible seamlessly inside a containerized runner (`cytopia/ansible`), enabling full playbook execution on Windows.

### Issue 14: Ansible YAML Callback Plugin Removal
* **Symptom**: `[ERROR]: The 'community.general.yaml' callback plugin has been removed`.
* **Root Cause**: Modern Ansible 2.21+ superseded the old callback plugin.
* **Solution**: Updated [`ansible/ansible.cfg`](file:///c:/Users/sachi/Desktop/user-service/ansible/ansible.cfg) with `stdout_callback = default` and `[callback_default] result_format = yaml`.

---

## 5. Complete DevOps Roadmap Implementation Details

```text
[SOFTWARE DEVELOPMENT]
Part A: User Service           -> JWT Authentication, BCrypt, Spring Security, Role authorization
Part B: Product Service        -> Product entity, CRUD REST APIs, Category & Price validations
Part C: Order Service          -> Order lifecycle (PENDING -> CONFIRMED -> CANCELLED), Kafka Producer
Part D: Kafka Integration      -> KRaft event bus (order-created, order-cancelled, stock-updated)
Part E: Inventory Service      -> Asynchronous stock reduction & reservation Kafka consumer
Part F: Notification Service   -> Multi-topic event consumer, customer notification logging
Part G: API Gateway            -> Spring Cloud Gateway reverse proxy, JWT relay, route filters
Part H: Service Communication  -> Sync REST (Product checks) + Async Kafka (Order/Inventory events)
Part I: Configuration          -> Externalized environment variables & multi-profile application.yml
Part J: Testing                -> Unit tests, MockMvc tests, SpringBootTest integration suite
Part K: Actuator               -> Actuator health probes (/health/liveness, /health/readiness, metrics)

[DEVOPS & CLOUD ARCHITECTURE]
Part L: Docker                 -> Multi-stage Dockerfiles for all 6 microservices (JDK 17 Eclipse Temurin)
Part M: Docker Compose         -> 11-container local stack (6 apps + 4 DBs + Kafka KRaft)
Part N: GitHub Actions CI/CD   -> Parallel matrix build, Maven test caching, Trivy security scan, ECR push
Part O: Terraform IaC          -> AWS VPC, Subnets, RDS PostgreSQL, EKS Cluster, ECR repos, IAM roles
Part P: Kubernetes & EKS       -> Kustomize manifests, RollingUpdate zero-downtime, HPA, Probes, Ingress
Part Q: AWS Production Stack   -> EKS 4-Node Cluster + RDS PostgreSQL db.t3.micro in VPC us-east-1
Part R: Ansible Automation     -> 4 Modular Roles (common, docker, microservices, k8s_tools) + site.yml
Part S: CloudWatch Observability-> EKS Add-on, Container Insights, 4 Log Groups, Alarms, SNS, Dashboard
```

---

## 6. CloudWatch Observability & Alerting Architecture

### A. Active Log Groups in CloudWatch
* `/aws/containerinsights/ecommerce-eks-cluster-dev/application` (All microservices + Kafka logs)
* `/aws/containerinsights/ecommerce-eks-cluster-dev/dataplane` (Kubernetes system logs)
* `/aws/containerinsights/ecommerce-eks-cluster-dev/host` (Node host logs)
* `/aws/containerinsights/ecommerce-eks-cluster-dev/performance` (Cluster performance metrics)

### B. Configured CloudWatch Alarms
1. **`ecommerce-order-service-high-cpu`**: Triggers when `pod_cpu_utilization` $\ge$ **85%** for `order-service`.
2. **`ecommerce-eks-node-high-cpu`**: Triggers when `node_cpu_utilization` $\ge$ **85%** on cluster worker nodes.
3. **`ecommerce-rds-high-cpu`**: Triggers when RDS PostgreSQL `CPUUtilization` $\ge$ **80%**.
4. **`ecommerce-rds-low-memory`**: Triggers when RDS PostgreSQL `FreeableMemory` $\le$ **100 MB**.

### C. Alert Notification Target
* **Amazon SNS Topic**: `arn:aws:sns:us-east-1:465139034981:ecommerce-alerts-dev`
* **Subscriber Command**:
  ```powershell
  aws sns subscribe `
    --topic-arn arn:aws:sns:us-east-1:465139034981:ecommerce-alerts-dev `
    --protocol email `
    --notification-endpoint "your-email@example.com"
  ```

### D. Live CloudWatch Dashboard
* **Dashboard Name**: `Ecommerce-Microservices-Observability`
* **Direct Console URL**: [AWS CloudWatch Dashboard (us-east-1)](https://us-east-1.console.aws.amazon.com/cloudwatch/home?region=us-east-1#dashboards:name=Ecommerce-Microservices-Observability)

---

## 7. Master Verification & Execution Commands

### A. Verify Kubernetes Cluster Health
```powershell
# Check all running pods (Expected: all 1/1 Running, 0 restarts)
kubectl get pods -n ecommerce

# Check ClusterIP services
kubectl get svc -n ecommerce

# Check HPA autoscalers
kubectl get hpa -n ecommerce

# Check CloudWatch Observability daemonsets
kubectl get pods -n amazon-cloudwatch
```

### B. Verify Ansible Automation
```powershell
cd ansible

# Check syntax of all playbooks
.\run-ansible.ps1 ansible-playbook playbooks/site.yml --syntax-check

# Run ping test
.\run-ansible.ps1 ansible localhost -m ping

# Run master orchestration playbook
.\run-ansible.ps1 ansible-playbook playbooks/site.yml
```

### C. Verify CloudWatch Alarms
```powershell
aws cloudwatch describe-alarms --alarm-name-prefix "ecommerce-" --query "MetricAlarms[*].[AlarmName,StateValue]" --output table
```

---

## 8. Summary for Technical Interviews & Presentations

> *"In this project, I engineered a cloud-native, event-driven E-Commerce microservices platform using Java 17, Spring Boot, Spring Cloud Gateway, and Apache Kafka. I implemented multi-stage Docker containerization and a GitHub Actions CI/CD matrix pipeline with Trivy vulnerability scanning. Using Terraform, I provisioned an AWS infrastructure comprising a Multi-AZ VPC, Amazon RDS PostgreSQL, Amazon ECR, and an Amazon EKS cluster with IAM OIDC IRSA. I deployed the microservices using Kubernetes manifests configured with RollingUpdates, Horizontal Pod Autoscaling (HPA), and Actuator health probes. I automated environment configuration using Ansible roles and implemented a full observability layer using AWS CloudWatch Container Insights, Fluent Bit log streaming, CloudWatch Alarms, and Amazon SNS."*

---
*Report generated and validated for Developer Handover & Production Operations.*
