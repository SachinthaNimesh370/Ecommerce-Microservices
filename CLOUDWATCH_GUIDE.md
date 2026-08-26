# Part S — CloudWatch Observability & Monitoring Guide

This guide details the **Part S — CloudWatch** implementation for the E-Commerce Microservices platform deployed on AWS EKS.

---

## 1. Overview & Architecture

The CloudWatch monitoring layer provides centralized logging, metrics collection, real-time dashboards, and threshold-based automated alerting for all 6 microservices, PostgreSQL RDS, and Kubernetes worker nodes.

```
+-----------------------------------------------------------------------------------+
|                               AWS EKS Cluster                                     |
|                                                                                   |
|  +--------------+  +--------------+  +--------------+  +---------------------+    |
|  | api-gateway  |  | user-service |  |product-serv. |  |   order-service     |    |
|  +-------+------+  +-------+------+  +-------+------+  +----------+----------+    |
|          |                 |                 |                    |               |
|          v                 v                 v                    v               |
|  +-----------------------------------------------------------------------------+  |
|  |             Amazon CloudWatch Observability DaemonSets                      |  |
|  |             (CloudWatch Agent + Fluent Bit on each Node)                    |  |
|  +-------------------------------------+---------------------------------------+  |
+----------------------------------------|------------------------------------------+
                                         |
                                         v
                         +-------------------------------+
                         |        AWS CloudWatch         |
                         |  - Log Groups (/application)  |
                         |  - Metrics (ContainerInsights)|
                         |  - Observability Dashboard    |
                         +---------------+---------------+
                                         |
                                         | Threshold Exceeded (e.g. CPU >= 85%)
                                         v
                         +-------------------------------+
                         |      CloudWatch Alarms        |
                         |   (order-service, RDS, Node)  |
                         +---------------+---------------+
                                         |
                                         v
                         +-------------------------------+
                         |       Amazon SNS Topic        |
                         |    (ecommerce-alerts-dev)     |
                         +---------------+---------------+
                                         |
                                         v
                                  DevOps Engineer
```

---

## 2. Resources Implemented

### A. AWS EKS Addon: Amazon CloudWatch Observability
* **Addon**: `amazon-cloudwatch-observability`
* **Namespace**: `amazon-cloudwatch`
* **Components**:
  * `cloudwatch-agent` (DaemonSet): Collects cluster, pod, and node performance metrics into the `ContainerInsights` metric namespace.
  * `fluent-bit` (DaemonSet): Ships application container logs (`stdout`/`stderr`) directly to CloudWatch Logs.

### B. CloudWatch Log Groups
| Log Group Name | Purpose |
| :--- | :--- |
| `/aws/containerinsights/ecommerce-eks-cluster-dev/application` | Application logs for all 6 Spring Boot microservices and Kafka |
| `/aws/containerinsights/ecommerce-eks-cluster-dev/dataplane` | Kubernetes dataplane logs (kubelet, kube-proxy, runtime) |
| `/aws/containerinsights/ecommerce-eks-cluster-dev/host` | Node host logs (`/var/log/messages`, `/var/log/dmesg`) |
| `/aws/containerinsights/ecommerce-eks-cluster-dev/performance` | High-resolution performance metrics events |

### C. CloudWatch Alarms
| Alarm Name | Metric | Threshold | Action |
| :--- | :--- | :--- | :--- |
| `ecommerce-order-service-high-cpu` | `pod_cpu_utilization` (`order-service`) | **>= 85%** for 2 periods | Publishes to SNS `ecommerce-alerts-dev` |
| `ecommerce-rds-high-cpu` | `CPUUtilization` (`ecommerce-postgres-dev`) | **>= 80%** for 2 periods | Publishes to SNS `ecommerce-alerts-dev` |
| `ecommerce-rds-low-memory` | `FreeableMemory` (`ecommerce-postgres-dev`) | **<= 100 MB** for 2 periods | Publishes to SNS `ecommerce-alerts-dev` |
| `ecommerce-eks-node-high-cpu` | `node_cpu_utilization` (Cluster Nodes) | **>= 85%** for 2 periods | Publishes to SNS `ecommerce-alerts-dev` |

### D. Centralized Observability Dashboard
* **Dashboard Name**: `Ecommerce-Microservices-Observability`
* **Widgets**:
  1. Microservices CPU Utilization (`api-gateway`, `user-service`, `product-service`, `order-service`, `inventory-service`, `notification-service`)
  2. Microservices Memory Utilization
  3. RDS PostgreSQL CPU Utilization
  4. RDS PostgreSQL Freeable Memory
  5. RDS Active Database Connections
  6. EKS Cluster Nodes CPU Utilization
  7. Microservices Container Restarts

---

## 3. Subscribing Your Email to Alerts

To receive email notifications whenever an alarm triggers:

```powershell
aws sns subscribe `
  --topic-arn arn:aws:sns:us-east-1:465139034981:ecommerce-alerts-dev `
  --protocol email `
  --notification-endpoint "your-email@example.com"
```
*(Check your inbox and click "Confirm subscription" in the email received from AWS).*

---

## 4. Verification Commands

### Check CloudWatch Observability Pods
```powershell
kubectl get pods -n amazon-cloudwatch
```

### View Live Microservice Logs in CloudWatch
```powershell
aws logs filter-log-events `
  --log-group-name "/aws/containerinsights/ecommerce-eks-cluster-dev/application" `
  --filter-pattern "order-service" `
  --limit 20
```

### Check Alarms Status
```powershell
aws cloudwatch describe-alarms --alarm-name-prefix "ecommerce-" --query "MetricAlarms[*].[AlarmName,StateValue]" --output table
```

### View CloudWatch Dashboard in AWS Console
Navigate to [AWS CloudWatch Console](https://us-east-1.console.aws.amazon.com/cloudwatch/home?region=us-east-1#dashboards:name=Ecommerce-Microservices-Observability) to view the live dashboard.
