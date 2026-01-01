# Deployment Guide

## Table of Contents
- [Prerequisites](#prerequisites)
- [Local Development](#local-development)
- [Docker Deployment](#docker-deployment)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Production Considerations](#production-considerations)

## Prerequisites

### For Local Development
- JDK 17 or higher
- Maven 3.8+
- Node.js 18+ and npm
- MySQL 8.0+
- Git

### For Docker Deployment
- Docker 20.10+
- Docker Compose 2.0+

### For Kubernetes Deployment
- kubectl configured
- Kubernetes cluster (v1.24+)
- Helm 3+ (optional)

## Local Development

### 1. Database Setup

```powershell
# Start MySQL
# Run the database creation script
mysql -u root -p < create-databases.sql
```

### 2. Start Services

```powershell
# Start in order
.\0-start-authentification.bat
.\1-start-eureka.bat
.\2-start-ms-meteo.bat
.\3-start-ms-arrosage.bat
.\4-start-gateway.bat
```

### 3. Start Frontend

```powershell
cd frontend
npm install
npm start
```

### 4. Access Services

- Frontend: http://localhost:4200
- Eureka: http://localhost:8761
- Gateway: http://localhost:8080
- Auth Service: http://localhost:8081

## Docker Deployment

### Build Images

```powershell
# Build all services
docker-compose build

# Or build individual service
docker build -t irrigation/auth-service ./authentification
```

### Run with Docker Compose

```powershell
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

## Kubernetes Deployment

### 1. Prerequisites

Ensure you have:
- A running Kubernetes cluster
- kubectl configured to access your cluster
- Sufficient resources (minimum 8GB RAM, 4 CPUs)

### 2. Create Namespace

```powershell
kubectl apply -f k8s/00-namespace.yaml
```

### 3. Configure Secrets

Edit `k8s/02-secret.yaml` and encode your secrets:

```powershell
# Encode a secret
$plainText = "your-secret-value"
$bytes = [System.Text.Encoding]::UTF8.GetBytes($plainText)
$encoded = [Convert]::ToBase64String($bytes)
Write-Output $encoded
```

Apply the secrets:
```powershell
kubectl apply -f k8s/02-secret.yaml
```

### 4. Deploy Infrastructure

```powershell
# ConfigMaps
kubectl apply -f k8s/01-configmap.yaml

# Persistent Storage
kubectl apply -f k8s/03-pvc.yaml

# MySQL Database
kubectl apply -f k8s/04-mysql.yaml

# RabbitMQ (if needed)
kubectl apply -f k8s/05-rabbitmq.yaml

# Wait for databases to be ready
kubectl wait --for=condition=ready pod -l app=mysql -n irrigation-system --timeout=120s
```

### 5. Deploy Config and Discovery Services

```powershell
# Config Server
kubectl apply -f k8s/06-config-server.yaml

# Eureka Server
kubectl apply -f k8s/07-eureka-server.yaml

# Wait for services to be ready
kubectl wait --for=condition=ready pod -l app=eureka-server -n irrigation-system --timeout=120s
```

### 6. Deploy Microservices

```powershell
# Gateway
kubectl apply -f k8s/08-gateway-service.yaml

# Weather Service
kubectl apply -f k8s/09-ms-meteo.yaml

# Irrigation Service
kubectl apply -f k8s/10-ms-arrosage.yaml

# Wait for all pods
kubectl wait --for=condition=ready pod --all -n irrigation-system --timeout=300s
```

### 7. Deploy Frontend and Ingress

```powershell
# Frontend
kubectl apply -f k8s/11-frontend.yaml

# Ingress
kubectl apply -f k8s/12-ingress.yaml
```

### 8. Verify Deployment

```powershell
# Check all pods
kubectl get pods -n irrigation-system

# Check services
kubectl get services -n irrigation-system

# Check ingress
kubectl get ingress -n irrigation-system

# View logs
kubectl logs -f deployment/gateway-service -n irrigation-system
```

### 9. Access the Application

```powershell
# Get ingress IP
kubectl get ingress -n irrigation-system

# If using Minikube
minikube service gateway-service -n irrigation-system

# Port forward for testing
kubectl port-forward service/gateway-service 8080:8080 -n irrigation-system
```

## Production Considerations

### High Availability

1. **Multiple Replicas:**
```yaml
spec:
  replicas: 3  # Increase for each service
```

2. **Pod Disruption Budgets:**
```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: gateway-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: gateway-service
```

### Resource Management

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

### Health Checks

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
```

### Monitoring

1. **Prometheus:**
```powershell
kubectl apply -f monitoring/prometheus.yaml
```

2. **Grafana:**
```powershell
kubectl apply -f monitoring/grafana.yaml
```

### Backup Strategy

1. **Database Backups:**
```powershell
# Create backup
kubectl exec -it mysql-0 -n irrigation-system -- mysqldump -u root -p --all-databases > backup.sql

# Schedule with CronJob
kubectl apply -f k8s/backup-cronjob.yaml
```

2. **Configuration Backups:**
```powershell
# Backup all configs
kubectl get configmap -n irrigation-system -o yaml > configmaps-backup.yaml
kubectl get secret -n irrigation-system -o yaml > secrets-backup.yaml
```

### Scaling

```powershell
# Manual scaling
kubectl scale deployment gateway-service --replicas=5 -n irrigation-system

# Auto-scaling
kubectl autoscale deployment gateway-service --min=2 --max=10 --cpu-percent=80 -n irrigation-system
```

### Updates and Rollbacks

```powershell
# Update image
kubectl set image deployment/gateway-service gateway-service=new-image:tag -n irrigation-system

# Check rollout status
kubectl rollout status deployment/gateway-service -n irrigation-system

# Rollback if needed
kubectl rollout undo deployment/gateway-service -n irrigation-system

# View history
kubectl rollout history deployment/gateway-service -n irrigation-system
```

### Security

1. **Network Policies:**
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-gateway-to-services
spec:
  podSelector:
    matchLabels:
      app: gateway-service
  policyTypes:
  - Egress
  egress:
  - to:
    - podSelector:
        matchLabels:
          tier: backend
```

2. **Pod Security Standards:**
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: irrigation-system
  labels:
    pod-security.kubernetes.io/enforce: restricted
```

### Troubleshooting

```powershell
# Describe pod
kubectl describe pod <pod-name> -n irrigation-system

# View logs
kubectl logs <pod-name> -n irrigation-system

# Get events
kubectl get events -n irrigation-system --sort-by='.lastTimestamp'

# Execute into pod
kubectl exec -it <pod-name> -n irrigation-system -- /bin/sh

# Check resource usage
kubectl top pods -n irrigation-system
kubectl top nodes
```

## Clean Up

```powershell
# Delete all resources
kubectl delete namespace irrigation-system

# Or delete individual resources
kubectl delete -f k8s/
```
