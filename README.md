# 🌱 Irrigation Management Microservices Platform

A comprehensive cloud-native microservices architecture for intelligent irrigation management, built with Spring Boot, Angular, and deployed on Kubernetes.

[![Build Status](https://github.com/wassimdgh/Micro-Services/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/wassimdgh/Micro-Services/actions)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-17.0-red.svg)](https://angular.io/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-326CE5.svg)](https://kubernetes.io/)

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Microservices](#microservices)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Development](#development)
- [Deployment](#deployment)
- [API Documentation](#api-documentation)
- [CI/CD Pipeline](#cicd-pipeline)
- [Monitoring & Observability](#monitoring--observability)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

This project implements a modern microservices architecture for an irrigation management system. It provides:

- **Weather Integration**: Real-time weather data collection and analysis
- **Smart Irrigation**: Automated irrigation planning based on weather conditions
- **User Management**: Secure authentication and authorization
- **Cloud-Native**: Designed for Kubernetes deployment
- **Scalable**: Horizontally scalable microservices architecture

## 🏗️ Architecture

```
┌─────────────────┐
│   Angular UI    │
│   (Frontend)    │
└────────┬────────┘
         │
         ▼
┌────────────────────┐
│   API Gateway      │
│  (Spring Cloud)    │
└────────┬───────────┘
         │
    ┌────┴────────────────────┬─────────────┐
    ▼                         ▼             ▼
┌───────────┐        ┌──────────────┐  ┌──────────────┐
│   Auth    │        │   Weather    │  │  Irrigation  │
│  Service  │        │   Service    │  │   Service    │
└─────┬─────┘        └──────┬───────┘  └──────┬───────┘
      │                     │                  │
      ▼                     ▼                  ▼
┌──────────────────────────────────────────────────┐
│              MySQL Database Cluster               │
└──────────────────────────────────────────────────┘

         ┌──────────────────┐
         │ Eureka Discovery │
         │     Server       │
         └──────────────────┘

         ┌──────────────────┐
         │  Config Server   │
         │   (External)     │
         └──────────────────┘
```

## 🔧 Microservices

### 1. **Authentication Service** (`authentification/`)
- User registration and login
- JWT token generation and validation
- Role-based access control (RBAC)
- Password encryption with BCrypt
- **Port**: 8081

### 2. **Weather Service** (`ms-meteo/`)
- Weather data collection from external APIs
- Weather forecasting
- Historical weather data storage
- Weather alerts and notifications
- **Port**: 8082

### 3. **Irrigation Service** (`ms-arrosage/`)
- Smart irrigation planning
- Automatic scheduling based on weather
- Zone-based irrigation management
- Water consumption tracking
- **Port**: 8083

### 4. **API Gateway** (`gateway-service/`)
- Centralized entry point
- Request routing and load balancing
- Authentication and authorization
- Rate limiting and circuit breaking
- **Port**: 8080

### 5. **Eureka Server** (`eureka-server/`)
- Service discovery and registration
- Health monitoring
- Load balancing support
- **Port**: 8761

### 6. **Config Server** (`config-server/`)
- Centralized configuration management
- Environment-specific configurations
- Dynamic configuration updates
- **Port**: 8888

### 7. **Frontend** (`frontend/`)
- Angular 17 single-page application
- Responsive Material Design UI
- Real-time weather visualization
- Interactive irrigation scheduling
- **Port**: 4200 (dev) / 80 (prod)

## 🛠️ Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.4**
- **Spring Cloud 2023.0.1**
- **Spring Data JPA**
- **Spring Security**
- **MySQL 8.0**
- **Maven 3.8+**
- **Lombok**
- **JWT (JSON Web Tokens)**

### Frontend
- **Angular 17**
- **TypeScript 5.2**
- **Angular Material**
- **RxJS**
- **Chart.js**
- **NGINX** (production server)

### DevOps & Infrastructure
- **Docker & Docker Compose**
- **Kubernetes**
- **GitHub Actions** (CI/CD)
- **Helm** (optional)
- **Prometheus & Grafana** (monitoring)
- **RabbitMQ** (message broker)

## 📦 Prerequisites

### For Local Development
- **JDK 17** or higher
- **Maven 3.8+**
- **Node.js 18+** and npm
- **MySQL 8.0**
- **Docker** and Docker Compose
- **Git**

### For Kubernetes Deployment
- **kubectl** configured
- **Kubernetes cluster** (Minikube, Kind, or cloud provider)
- **Docker** for building images

## 🚀 Quick Start

### Option 1: Local Development (Windows)

1. **Clone the repository**
```powershell
git clone https://github.com/wassimdgh/Micro-Services.git
cd Micro-Services
```

2. **Set up MySQL databases**
```powershell
mysql -u root -p < create-databases.sql
```

3. **Start services in order**
```powershell
# Start Authentication Service
.\0-start-authentification.bat

# Start Eureka Server
.\1-start-eureka.bat

# Start Weather Service
.\2-start-ms-meteo.bat

# Start Irrigation Service
.\3-start-ms-arrosage.bat

# Start API Gateway
.\4-start-gateway.bat
```

4. **Start Frontend**
```powershell
cd frontend
npm install
npm start
```

5. **Access the application**
- Frontend: http://localhost:4200
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080

### Option 2: Docker Compose

```powershell
docker-compose up -d
```

### Option 3: Kubernetes Deployment

```powershell
# Apply all Kubernetes manifests
kubectl apply -f k8s/

# Wait for pods to be ready
kubectl wait --for=condition=ready pod --all -n irrigation-system --timeout=300s

# Get service URLs
kubectl get ingress -n irrigation-system
```

## 💻 Development

### Building Individual Services

```powershell
# Build all microservices
mvn clean package -DskipTests

# Build specific service
cd authentification
mvn clean package

# Build Docker image
docker build -t irrigation/auth-service:latest .
```

### Running Tests

```powershell
# Run all tests
mvn test

# Run tests for specific service
cd ms-meteo
mvn test

# Run with coverage
mvn test jacoco:report
```

### Database Migrations

Each microservice manages its own database schema:
- **auth_db**: Authentication service database
- **meteo_db**: Weather service database
- **arrosage_db**: Irrigation service database

Run `create-databases.sql` to initialize all databases.

## 🚢 Deployment

### Kubernetes Deployment Order

The `k8s/` directory contains all Kubernetes manifests. Apply them in order:

1. **Namespace & ConfigMaps**
```powershell
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-configmap.yaml
kubectl apply -f k8s/02-secret.yaml
```

2. **Persistent Storage & Databases**
```powershell
kubectl apply -f k8s/03-pvc.yaml
kubectl apply -f k8s/04-mysql.yaml
kubectl apply -f k8s/05-rabbitmq.yaml
```

3. **Configuration & Service Discovery**
```powershell
kubectl apply -f k8s/06-config-server.yaml
kubectl apply -f k8s/07-eureka-server.yaml
```

4. **Microservices**
```powershell
kubectl apply -f k8s/08-gateway-service.yaml
kubectl apply -f k8s/09-ms-meteo.yaml
kubectl apply -f k8s/10-ms-arrosage.yaml
```

5. **Frontend & Ingress**
```powershell
kubectl apply -f k8s/11-frontend.yaml
kubectl apply -f k8s/12-ingress.yaml
```

### Environment Variables

Configure the following environment variables in `k8s/02-secret.yaml`:

```yaml
MYSQL_ROOT_PASSWORD: <base64-encoded>
MYSQL_PASSWORD: <base64-encoded>
JWT_SECRET: <base64-encoded>
WEATHER_API_KEY: <base64-encoded>
```

## 📚 API Documentation

### Authentication Service
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `GET /api/auth/validate` - Validate JWT token

### Weather Service
- `GET /api/weather/current` - Get current weather
- `GET /api/weather/forecast` - Get weather forecast
- `GET /api/weather/history` - Get historical data

### Irrigation Service
- `GET /api/irrigation/plans` - Get irrigation plans
- `POST /api/irrigation/plans` - Create irrigation plan
- `PUT /api/irrigation/plans/{id}` - Update plan
- `DELETE /api/irrigation/plans/{id}` - Delete plan
- `POST /api/irrigation/execute/{id}` - Execute irrigation

For detailed API documentation, visit:
- Swagger UI: `http://localhost:8080/swagger-ui.html` (when running locally)

## 🔄 CI/CD Pipeline

### GitHub Actions Workflows

The project includes automated CI/CD pipelines:

#### **Build & Test** (`.github/workflows/ci-cd.yml`)
- Triggered on push to `main` and pull requests
- Builds all microservices
- Runs unit and integration tests
- Performs code quality checks
- Builds Docker images
- Pushes images to Docker Hub/GitHub Container Registry

#### **Deploy to Kubernetes** (`.github/workflows/deploy.yml`)
- Triggered on successful build
- Deploys to staging/production environments
- Performs health checks
- Automatic rollback on failure

### Setting Up CI/CD

1. **Configure GitHub Secrets**:
   - `DOCKER_USERNAME`
   - `DOCKER_PASSWORD`
   - `KUBE_CONFIG`
   - `MYSQL_PASSWORD`
   - `JWT_SECRET`

2. **Enable GitHub Actions** in repository settings

3. **Push to trigger pipeline**
```powershell
git add .
git commit -m "Deploy to production"
git push origin main
```

## 📊 Monitoring & Observability

### Health Checks
Each microservice exposes Spring Boot Actuator endpoints:

```
/actuator/health
/actuator/info
/actuator/metrics
/actuator/prometheus
```

### Logging
Structured logging with correlation IDs for request tracing across services.

### Metrics
- Prometheus metrics collection
- Grafana dashboards for visualization
- Custom business metrics

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Java Code Conventions
- Use ESLint/Prettier for TypeScript/JavaScript
- Write meaningful commit messages
- Add tests for new features

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Wassim** - *Initial work* - [wassimdgh](https://github.com/wassimdgh)

## 🙏 Acknowledgments

- Spring Boot and Spring Cloud teams
- Angular team
- Kubernetes community
- All contributors and supporters

## 📞 Support

For issues and questions:
- Create an [Issue](https://github.com/wassimdgh/Micro-Services/issues)
- Contact: [Your Email]

## 🗺️ Roadmap

- [ ] Add Prometheus/Grafana monitoring
- [ ] Implement distributed tracing (Zipkin/Jaeger)
- [ ] Add API rate limiting
- [ ] Implement caching layer (Redis)
- [ ] Add notification service (Email/SMS)
- [ ] Mobile app integration
- [ ] Advanced weather prediction with ML
- [ ] Multi-language support

---

**Built with ❤️ using Spring Boot, Angular, and Kubernetes**
