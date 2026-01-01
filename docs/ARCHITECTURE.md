# Architecture Documentation

## System Architecture Overview

The Irrigation Management Platform follows a microservices architecture pattern with the following components:

## Architecture Diagram

```
                                    ┌─────────────────────┐
                                    │   End Users         │
                                    │   (Web Browsers)    │
                                    └──────────┬──────────┘
                                               │
                                               │ HTTPS
                                               │
                                    ┌──────────▼──────────┐
                                    │   Load Balancer     │
                                    │   (Kubernetes)      │
                                    └──────────┬──────────┘
                                               │
                        ┌──────────────────────┼──────────────────────┐
                        │                      │                      │
                        │                      │                      │
             ┌──────────▼──────────┐  ┌───────▼────────┐  ┌─────────▼─────────┐
             │  Angular Frontend   │  │  API Gateway   │  │  Eureka Discovery │
             │  (Port 80)          │  │  (Port 8080)   │  │  (Port 8761)      │
             └─────────────────────┘  └───────┬────────┘  └───────────────────┘
                                               │
                        ┌──────────────────────┼──────────────────────┐
                        │                      │                      │
             ┌──────────▼──────────┐  ┌───────▼────────┐  ┌─────────▼─────────┐
             │  Auth Service       │  │ Weather Service│  │ Irrigation Service│
             │  (Port 8081)        │  │ (Port 8082)    │  │  (Port 8083)      │
             └──────────┬──────────┘  └───────┬────────┘  └─────────┬─────────┘
                        │                      │                      │
                        └──────────────────────┼──────────────────────┘
                                               │
                                    ┌──────────▼──────────┐
                                    │   MySQL Cluster     │
                                    │   - auth_db         │
                                    │   - meteo_db        │
                                    │   - arrosage_db     │
                                    └─────────────────────┘
```

## Components

### 1. Frontend Layer

**Angular Frontend**
- **Technology**: Angular 17, TypeScript, Material Design
- **Purpose**: User interface and user experience
- **Features**:
  - Responsive design
  - Real-time weather visualization
  - Irrigation scheduling interface
  - User authentication
- **Communication**: REST API calls to Gateway

### 2. API Gateway Layer

**Spring Cloud Gateway**
- **Technology**: Spring Cloud Gateway
- **Purpose**: Single entry point for all client requests
- **Responsibilities**:
  - Request routing
  - Load balancing
  - Authentication/Authorization
  - Rate limiting
  - Circuit breaking
  - Request/Response transformation

### 3. Service Discovery

**Eureka Server**
- **Technology**: Netflix Eureka
- **Purpose**: Service registration and discovery
- **Features**:
  - Dynamic service registration
  - Health monitoring
  - Load balancing support
  - Failover handling

### 4. Microservices Layer

#### Authentication Service
- **Database**: auth_db
- **Responsibilities**:
  - User registration
  - Login/Logout
  - JWT token generation
  - Password management
  - Role-based access control

#### Weather Service
- **Database**: meteo_db
- **Responsibilities**:
  - Fetch weather data from external APIs
  - Store weather history
  - Provide weather forecasts
  - Weather alerts

#### Irrigation Service
- **Database**: arrosage_db
- **Responsibilities**:
  - Irrigation planning
  - Automatic scheduling
  - Zone management
  - Water consumption tracking

### 5. Data Layer

**MySQL Database Cluster**
- **Databases**:
  - `auth_db`: User credentials, roles, permissions
  - `meteo_db`: Weather data, forecasts, history
  - `arrosage_db`: Irrigation plans, zones, schedules

## Design Patterns

### 1. Microservices Pattern
- Each service is independently deployable
- Service isolation
- Technology diversity

### 2. API Gateway Pattern
- Single entry point
- Centralized cross-cutting concerns
- Backend for Frontend (BFF)

### 3. Service Registry Pattern
- Dynamic service discovery
- Client-side load balancing
- Health checking

### 4. Circuit Breaker Pattern
- Fault tolerance
- Graceful degradation
- Timeout handling

### 5. Database per Service
- Data isolation
- Independent scaling
- Technology choice per service

## Communication Patterns

### Synchronous Communication
- **HTTP/REST**: Primary communication protocol
- **JSON**: Data exchange format
- **Feign Clients**: Declarative REST clients

### Asynchronous Communication (Optional)
- **RabbitMQ**: Message broker
- **Events**: Domain events for loose coupling

## Security Architecture

### Authentication Flow
```
1. User → Frontend: Login credentials
2. Frontend → Gateway → Auth Service: POST /api/auth/login
3. Auth Service → Database: Validate credentials
4. Auth Service → Frontend: JWT token
5. Frontend → Gateway: Subsequent requests with JWT
6. Gateway → Validates JWT → Routes to services
```

### Security Measures
- JWT-based authentication
- BCrypt password hashing
- Role-based access control (RBAC)
- HTTPS encryption
- API rate limiting
- CORS configuration

## Scalability Strategy

### Horizontal Scaling
- Multiple instances of each service
- Load balancing via Kubernetes
- Stateless services

### Vertical Scaling
- Resource allocation per service
- JVM tuning
- Database optimization

### Caching Strategy
- Application-level caching (Spring Cache)
- Database query optimization
- CDN for static assets

## Resilience Patterns

### Circuit Breaker
- Prevent cascading failures
- Automatic recovery
- Fallback mechanisms

### Retry Logic
- Exponential backoff
- Max retry attempts
- Idempotent operations

### Timeout Configuration
- Connection timeout
- Read timeout
- Circuit breaker timeout

## Monitoring Architecture

### Health Checks
- Spring Boot Actuator endpoints
- Kubernetes liveness/readiness probes
- Database connection health

### Metrics Collection
- Prometheus metrics
- Custom business metrics
- JVM metrics

### Logging
- Structured logging (JSON)
- Correlation IDs
- Centralized log aggregation (ELK stack)

### Distributed Tracing
- Spring Cloud Sleuth
- Zipkin integration
- Request flow visualization

## Deployment Architecture

### Kubernetes Deployment
```
Namespace: irrigation-system
├── ConfigMaps (application configs)
├── Secrets (credentials, keys)
├── PersistentVolumeClaims (data storage)
├── StatefulSet (MySQL)
├── Deployments
│   ├── config-server
│   ├── eureka-server
│   ├── gateway-service
│   ├── authentification
│   ├── ms-meteo
│   ├── ms-arrosage
│   └── frontend
├── Services (ClusterIP, LoadBalancer)
└── Ingress (external access)
```

### Resource Allocation
| Service | CPU Request | Memory Request | Replicas |
|---------|-------------|----------------|----------|
| Frontend | 100m | 128Mi | 2 |
| Gateway | 500m | 512Mi | 3 |
| Auth | 500m | 512Mi | 2 |
| Weather | 500m | 512Mi | 2 |
| Irrigation | 500m | 512Mi | 2 |
| Eureka | 500m | 512Mi | 2 |
| MySQL | 1000m | 1Gi | 1 |

## Data Flow Examples

### User Login Flow
```
1. User enters credentials in Angular app
2. Frontend POST /api/auth/login → Gateway
3. Gateway routes to Auth Service
4. Auth Service validates against auth_db
5. Auth Service generates JWT token
6. Token returned to Frontend
7. Frontend stores token in localStorage
8. All subsequent requests include JWT in header
```

### Weather-Based Irrigation Flow
```
1. Irrigation Service requests weather forecast
2. Gateway routes to Weather Service
3. Weather Service fetches from external API
4. Weather data saved to meteo_db
5. Weather Service returns forecast
6. Irrigation Service calculates irrigation needs
7. Irrigation plan saved to arrosage_db
8. Notification sent to user
```

## Technology Stack Summary

| Layer | Technology |
|-------|------------|
| Frontend | Angular 17, TypeScript, Material UI |
| API Gateway | Spring Cloud Gateway |
| Microservices | Spring Boot 3.2.4, Java 17 |
| Service Discovery | Netflix Eureka |
| Database | MySQL 8.0 |
| Container | Docker |
| Orchestration | Kubernetes |
| CI/CD | GitHub Actions |
| Monitoring | Prometheus, Grafana |
| Logging | ELK Stack |

## Future Enhancements

1. **Caching Layer**: Redis for session management
2. **Message Queue**: RabbitMQ for async processing
3. **API Documentation**: OpenAPI/Swagger
4. **Service Mesh**: Istio for advanced traffic management
5. **Observability**: Grafana, Jaeger for tracing
6. **Security**: OAuth2, Keycloak integration
