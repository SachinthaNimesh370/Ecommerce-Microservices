# E-Commerce Microservices - Kubernetes & EKS Deployment Guide (Part P)

This guide provides complete instructions to deploy all 6 microservices to an AWS EKS (Elastic Kubernetes Service) cluster or local Minikube cluster using Kubernetes manifests.

---

## Architecture Overview

```
                        +----------------------+
                        |   AWS ALB / Ingress  |
                        +----------+-----------+
                                   |
                                   v
                        +----------------------+
                        |     api-gateway      |
                        |      (Port 8080)     |
                        +----+-----+-----+-----+
                             |     |     |
              +--------------+     |     +--------------+
              |                    |                    |
              v                    v                    v
    +------------------+  +------------------+  +------------------+
    |   user-service   |  | product-service  |  |  order-service   |
    |   (Port 8081)    |  |   (Port 8082)    |  |   (Port 8083)    |
    +--------+---------+  +--------+---------+  +--------+---------+
             |                     |                     |
             |                     |                     |
             v                     v                     v
    +------------------+  +------------------+  +------------------+
    | PostgreSQL DBs   |  | inventory-service|  |notification-serv |
    |  (user, product, |  |   (Port 8084)    |  |   (Port 8085)    |
    |  order, invent.) |  +--------+---------+  +------------------+
    +------------------+           |
                                   v
                             +-----------+
                             |   Kafka   |
                             +-----------+
```

---

## 1. Required Tools & Prerequisites

Ensure the following CLI tools are installed on your local machine:

1. **AWS CLI v2**: To interact with AWS services and configure EKS credentials.
   * Installation: `winget install Amazon.AWSCLI` (Windows) or [AWS CLI Install Guide](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html).
2. **Kubectl**: Kubernetes command-line tool.
   * Installation: `winget install Kubernetes.kubectl` or `choco install kubernetes-cli`.
3. **Eksctl** (Optional but recommended): EKS cluster management tool.
   * Installation: `choco install eksctl` or `winget install eksctl`.
4. **Helm**: Package manager for Kubernetes (needed for AWS Load Balancer Controller & Metrics Server).
   * Installation: `choco install kubernetes-helm` or `winget install Helm.Helm`.

---

## 2. Cluster Configuration & Credentials Management

### A. AWS Authentication & EKS Kubeconfig Setup
Run the following commands to authenticate your AWS session and update your local `kubeconfig`:

```bash
# 1. Configure AWS credentials
aws configure

# 2. Update kubeconfig to point to your EKS cluster
aws eks update-kubeconfig --region <AWS_REGION> --name ecommerce-eks-cluster
```

### B. Secure Secret Management

> [!IMPORTANT]
> Never commit raw plain-text passwords or secret keys to Git.

Create your production Kubernetes Secret directly via `kubectl`:

```bash
kubectl create namespace ecommerce --dry-run=client -o yaml | kubectl apply -f -

kubectl create secret generic ecommerce-secrets \
  --namespace=ecommerce \
  --from-literal=DB_PASSWORD="YourSecureDBPassword2026!" \
  --from-literal=JWT_SECRET="YourProductionJwtSecretKey64BytesLongHere"
```

---

## 3. Metrics Server & Ingress Controller Installation

Before deploying HPA and Ingress resources, deploy the required cluster add-ons:

### A. Install Kubernetes Metrics Server (Required for HPA)
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### B. Install AWS Load Balancer Controller (Required for Ingress)
```bash
helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=ecommerce-eks-cluster \
  --set serviceAccount.create=true \
  --set serviceAccount.name=aws-load-balancer-controller
```

---

## 4. Deploying the Microservices (Part P Manifests)

All Kubernetes manifests are organized under the [`k8s/`](file:///c:/Users/sachi/Desktop/user-service/k8s/) directory.

### Step 1: Update ECR Image URIs
Before applying, update the container image paths in each `deployment.yaml` to point to your AWS ECR repository:

Replace `<AWS_ACCOUNT_ID>` and `<AWS_REGION>` in:
- `k8s/api-gateway/deployment.yaml`
- `k8s/user-service/deployment.yaml`
- `k8s/product-service/deployment.yaml`
- `k8s/order-service/deployment.yaml`
- `k8s/inventory-service/deployment.yaml`
- `k8s/notification-service/deployment.yaml`

### Step 2: Apply All Manifests using Kustomize
Run the following single command from the project root:

```bash
kubectl apply -k k8s/
```

---

## 5. Verification & Monitoring Commands

### Check Deployments, Pods, and Services
```bash
kubectl get all -n ecommerce
```

### Check Horizontal Pod Autoscalers (HPA)
```bash
kubectl get hpa -n ecommerce
```

### Check Ingress Endpoint
```bash
kubectl get ingress -n ecommerce
```

### Inspect Pod Health Probes & Logs
```bash
# View pod liveness and readiness status
kubectl describe pod -l app=user-service -n ecommerce

# View logs for a specific service
kubectl logs -f -l app=api-gateway -n ecommerce
```

---

## 6. Zero-Downtime Rolling Update & Rollback

Every deployment is configured with `RollingUpdate` strategy (`maxSurge: 1`, `maxUnavailable: 0`).

### Trigger a Rolling Update
```bash
kubectl set image deployment/user-service user-service=<AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/user-service:v2 -n ecommerce
```

### Check Update Progress
```bash
kubectl rollout status deployment/user-service -n ecommerce
```

### Rollback if Failure Occurs
```bash
kubectl rollout undo deployment/user-service -n ecommerce
```
