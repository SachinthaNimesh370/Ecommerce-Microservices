# Part N — CI/CD Guide: E-Commerce Microservices

This document details the **Part N — CI/CD** implementation for the E-Commerce Microservices platform as specified in the `E-Commerce_Microservices_Development_Guide.pdf`.

---

## 1. Overview & Architecture

The CI/CD pipeline automates code validation, automated testing, container image construction, vulnerability scanning, and artifact deployment upon `git push` or `pull_request` events.

```text
Developer
   │
   ▼
git push
   │
   ▼
GitHub Actions Pipeline
   │
   ├──────────► 1. Maven Build & Automated Testing (JDK 17)
   ├──────────► 2. Docker Image Build (per Microservice)
   ├──────────► 3. Trivy Vulnerability & Security Scan
   └──────────► 4. Push to Container Registry (GHCR / Amazon ECR)
```

---

## 2. Microservices Covered

The pipeline uses a **GitHub Actions Matrix Strategy** to run parallel, isolated build and deployment jobs for all 6 microservices:

| Microservice | Path | Container Registry Image Name |
| :--- | :--- | :--- |
| **API Gateway** | `./api-gateway` | `ghcr.io/<owner>/api-gateway` |
| **User Service** | `./user-service` | `ghcr.io/<owner>/user-service` |
| **Product Service** | `./product-service` | `ghcr.io/<owner>/product-service` |
| **Order Service** | `./order-service` | `ghcr.io/<owner>/order-service` |
| **Inventory Service** | `./inventory-service` | `ghcr.io/<owner>/inventory-service` |
| **Notification Service** | `./notification-service` | `ghcr.io/<owner>/notification-service` |

---

## 3. Pipeline Stages & Execution Flow

### Stage 1: Build & Unit/Integration Tests
- **Environment**: JDK 17 (Eclipse Temurin)
- **Maven Dependency Caching**: Caches `~/.m2/repository` based on `pom.xml` hashes per service to optimize build times.
- **Command**: `./mvnw clean package -DskipTests=false`
- **Output**: Executable JAR files compiled in target directories.

### Stage 2: Source Code & Dependency Security Scanning
- **Tool**: Trivy Security Scanner (`aquasecurity/trivy-action`)
- **Scope**: Scans project source files and Maven dependencies for known vulnerabilities (CVEs).
- **Severity Level**: Filters for `CRITICAL` and `HIGH` severity vulnerabilities.

### Stage 3: Docker Image Compilation
- **Tool**: Docker Buildx (`docker/setup-buildx-action@v3`)
- **Tags**:
  - `ghcr.io/<owner>/<service>:<git-sha>`
  - `ghcr.io/<owner>/<service>:latest`

### Stage 4: Docker Container Security Scanning
- **Tool**: Trivy Image Scanner
- **Scope**: Scans the compiled Docker image filesystem and base image OS packages for vulnerabilities prior to registry publishing.

### Stage 5: Container Registry Publishing
- **Registry**: GitHub Container Registry (`ghcr.io`)
- **Authentication**: `docker/login-action@v3` utilizing `${{ secrets.GITHUB_TOKEN }}`.
- **Execution Condition**: Pushes automatically on push to target branches (`main`, `master`, `dockerUp`, `CICD`), skipping pushes on `pull_request` runs.

### Stage 6: Docker Compose Stack Validation
- Validates structural syntax of `docker-compose.yml` to verify multi-container setup consistency across all microservices and infrastructure components (PostgreSQL DBs, Kafka).

---

## 4. Workflow Configuration File

The pipeline is defined in [`.github/workflows/ci-cd.yml`](file:///c:/Users/sachi/Desktop/user-service/.github/workflows/ci-cd.yml).

### Workflow Triggers
```yaml
on:
  push:
    branches: [main, master, dockerUp, CICD]
  pull_request:
    branches: [main, master, CICD]
  workflow_dispatch:
```

---

## 5. Transitioning to Amazon ECR & AWS Phase (Parts O - Q)

When advancing to AWS EKS deployment (Parts O–Q), update `.github/workflows/ci-cd.yml` with the following AWS steps:

```yaml
- name: Configure AWS Credentials
  uses: aws-actions/configure-aws-credentials@v4
  with:
    aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
    aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
    aws-region: us-east-1

- name: Log in to Amazon ECR
  id: login-ecr
  uses: aws-actions/amazon-ecr-login@v2

- name: Push to Amazon ECR
  run: |
    ECR_REGISTRY=${{ steps.login-ecr.outputs.registry }}
    docker tag ghcr.io/${{ github.repository_owner }}/${{ matrix.service }}:${{ github.sha }} $ECR_REGISTRY/${{ matrix.service }}:${{ github.sha }}
    docker push $ECR_REGISTRY/${{ matrix.service }}:${{ github.sha }}
```

---

## 6. Verification & Local Testing

### Validating Docker Compose Stack
```bash
docker compose config
```

### Building Microservices Locally
```bash
cd user-service && ./mvnw clean package
cd ../product-service && ./mvnw clean package
cd ../order-service && ./mvnw clean package
cd ../inventory-service && ./mvnw clean package
cd ../notification-service && ./mvnw clean package
cd ../api-gateway && ./mvnw clean package
```
