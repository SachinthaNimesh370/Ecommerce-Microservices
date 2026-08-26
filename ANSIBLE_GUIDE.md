# Part R — Ansible Configuration Management Guide

This guide details the **Part R — Ansible** configuration management and server automation implementation for the E-Commerce Microservices platform.

---

## 1. Overview & Architecture (Terraform vs. Ansible)

As outlined in the architecture roadmap, **Terraform** and **Ansible** serve distinct, complementary roles:

| Tool | Phase | Purpose | Managed Scope |
| :--- | :--- | :--- | :--- |
| **Terraform** | Infrastructure Provisioning | **Create Infrastructure** | VPC, Subnets, EKS Cluster, RDS PostgreSQL, Security Groups, IAM Roles, ECR |
| **Ansible** | Configuration Management | **Configure Infrastructure** | OS hardening, Docker CE engine, UFW Firewall, `.env` configs, Bastion tools, Microservices deployment |

```text
+-----------------------+     1. Provisions     +--------------------------------+
|       Terraform       | ────────────────────► |   Cloud Infrastructure (AWS)   |
| (Infrastructure as    |                       |   - VPC, RDS, EKS, EC2, IAM    |
|        Code)          |                       +---------------+----------------+
+-----------------------+                                       |
                                                                | 2. Passes Hosts/IPs
                                                                v
+-----------------------+     3. Configures     +--------------------------------+
|        Ansible        | ────────────────────► |      Servers & Environment     |
|    (Configuration     |                       |   - Docker & Containerd        |
|      Management)      |                       |   - Security Hardening & UFW   |
+-----------------------+                       |   - Bastion Admin Tooling      |
                                                +--------------------------------+
```

> **Note**: Managed AWS cloud services (e.g. AWS EKS control plane and Amazon MSK) are managed via Terraform; standalone servers, Docker hosts, CI/CD runners, and admin bastions are configured via Ansible.

---

## 2. Directory Structure

All Ansible configurations, roles, inventories, and playbooks are located in the [`ansible/`](file:///c:/Users/sachi/Desktop/user-service/ansible/) directory:

```text
ansible/
├── ansible.cfg                      # Global Ansible runtime configuration
├── inventory/
│   ├── hosts.ini                    # Inventory defining host groups (local, staging, prod, bastions)
│   └── group_vars/
│       └── all.yml                  # Global variables (ports, DB endpoints, Java/Docker versions)
├── roles/
│   ├── common/                      # Baseline packages, deployer user, UFW firewall rules
│   │   └── tasks/main.yml
│   ├── docker/                      # Docker CE engine, compose plugin, daemon log rotation
│   │   ├── tasks/main.yml
│   │   └── handlers/main.yml
│   ├── microservices/               # Copies compose stack, templates .env, starts containers
│   │   ├── tasks/main.yml
│   │   └── templates/env.j2
│   └── k8s_tools/                   # Installs kubectl, helm, and AWS CLI v2
│       └── tasks/main.yml
└── playbooks/
    ├── 01-system-setup.yml          # Playbook 1: Baseline setup & security
    ├── 02-docker-setup.yml          # Playbook 2: Docker & containerd setup
    ├── 03-deploy-microservices.yml  # Playbook 3: Microservices deployment
    ├── 04-k8s-prerequisites.yml     # Playbook 4: Kubernetes management tools
    └── site.yml                     # Master Playbook (Orchestrates all steps)
```

---

## 3. Inventory & Variables

### Target Hosts (`inventory/hosts.ini`)
Defines the target infrastructure groups:
* `[local]`: Local developer/management workstation.
* `[staging_servers]`: Staging standalone VM or Docker hosts.
* `[prod_servers]`: Production application servers.
* `[bastion_hosts]`: Kubernetes admin and jump boxes.

### Configuration Variables (`inventory/group_vars/all.yml`)
Centralized settings including:
* Microservices ports (`8080` API Gateway through `8085` Notification Service)
* Database connection endpoints (`db_host`, `db_port`, `db_user`)
* Kafka bootstrap servers (`kafka_bootstrap_servers`)
* Firewall allowed ports (`22`, `80`, `443`, `8080`)

---

## 4. Execution Guide & Playbook Commands

Navigate to the `ansible/` directory:
```bash
cd ansible
```

### Step 1: Ping / Connectivity Test (Ad-Hoc Command)
```bash
ansible all -m ping
```

### Step 2: Syntax Check
Validate syntax of playbooks before running:
```bash
ansible-playbook playbooks/site.yml --syntax-check
```

### Step 3: Dry-Run Execution (Check Mode)
Simulate execution without modifying the servers:
```bash
ansible-playbook playbooks/site.yml --check
```

### Step 4: Run Individual Modular Playbooks
```bash
# 1. System Setup & Firewall
ansible-playbook playbooks/01-system-setup.yml

# 2. Docker & Container Engine Setup
ansible-playbook playbooks/02-docker-setup.yml

# 3. Deploy E-Commerce Microservices Stack
ansible-playbook playbooks/03-deploy-microservices.yml

# 4. Install Kubernetes Tools (kubectl, helm, aws-cli)
ansible-playbook playbooks/04-k8s-prerequisites.yml
```

### Step 5: Full End-to-End Orchestration (Master Playbook)
Run the entire provisioning and configuration sequence:
```bash
ansible-playbook playbooks/site.yml
```

---

## 5. Idempotency & Verification

Every Ansible task is designed to be **idempotent** (running the playbook multiple times produces `changed=0` if no changes are required).

### Verify Running Microservices Containers on Host
```bash
ansible app_servers -m command -a "docker compose --project-directory /opt/ecommerce ps"
```

### Verify Service Health via API Gateway
```bash
curl -I http://<HOST_IP>:8080/actuator/health
```
