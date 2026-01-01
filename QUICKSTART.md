# 🚀 Quick Start Guide

This guide will help you get the Irrigation Management Platform up and running quickly.

## ⚡ Prerequisites Check

Before starting, ensure you have:

- ✅ JDK 17 or higher (`java -version`)
- ✅ Maven 3.8+ (`mvn -version`)
- ✅ Node.js 18+ (`node -v`)
- ✅ MySQL 8.0 (`mysql --version`)
- ✅ Docker (optional) (`docker --version`)

## 🎯 Choose Your Setup Method

### Option 1: Local Development (Recommended for Development)

#### Step 1: Clone the Repository
```powershell
git clone https://github.com/wassimdgh/Micro-Services.git
cd Micro-Services
```

#### Step 2: Set Up Databases
```powershell
# Start MySQL and run:
mysql -u root -p < create-databases.sql
```

This creates three databases:
- `auth_db` - Authentication service
- `meteo_db` - Weather service
- `arrosage_db` - Irrigation service

#### Step 3: Start Services (In Order!)

**Important:** Services must be started in this specific order:

1. **Authentication Service** (Port 8081)
```powershell
.\0-start-authentification.bat
```
Wait for: "Started AuthentificationApplication"

2. **Eureka Server** (Port 8761)
```powershell
.\1-start-eureka.bat
```
Wait for: "Started EurekaServerApplication"
Access: http://localhost:8761

3. **Weather Service** (Port 8082)
```powershell
.\2-start-ms-meteo.bat
```
Wait for: "Started MsMeteoApplication"

4. **Irrigation Service** (Port 8083)
```powershell
.\3-start-ms-arrosage.bat
```
Wait for: "Started MsArrosageApplication"

5. **API Gateway** (Port 8080)
```powershell
.\4-start-gateway.bat
```
Wait for: "Started GatewayServiceApplication"

#### Step 4: Start Frontend
```powershell
cd frontend
npm install
npm start
```

Frontend will be available at: http://localhost:4200

### Option 2: Docker Compose (Recommended for Testing)

#### Quick Start
```powershell
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

#### Access Services
- Frontend: http://localhost:80
- API Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761

### Option 3: Kubernetes (Recommended for Production)

#### Prerequisites
- Kubernetes cluster running (Minikube, Kind, or cloud provider)
- kubectl configured

#### Deploy
```powershell
# Apply all manifests
kubectl apply -f k8s/

# Wait for services to be ready
kubectl wait --for=condition=ready pod --all -n irrigation-system --timeout=300s

# Check status
kubectl get pods -n irrigation-system
kubectl get services -n irrigation-system

# Get frontend URL
kubectl get ingress -n irrigation-system
```

## 🧪 Verify Installation

### Check Services Health

**Eureka Dashboard:**
Visit http://localhost:8761 and verify all services are registered:
- GATEWAY-SERVICE
- AUTHENTIFICATION
- MS-METEO
- MS-ARROSAGE

**Service Health Checks:**
```powershell
# Authentication
curl http://localhost:8081/actuator/health

# Weather
curl http://localhost:8082/actuator/health

# Irrigation
curl http://localhost:8083/actuator/health

# Gateway
curl http://localhost:8080/actuator/health
```

### Test API Endpoints

**Register a User:**
```powershell
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Login:**
```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Get Weather Data:**
```powershell
# Save the JWT token from login response
$token = "your-jwt-token-here"

curl http://localhost:8080/api/weather/current `
  -H "Authorization: Bearer $token"
```

## 📱 Using the Frontend

1. **Access the Application**
   - Navigate to http://localhost:4200
   
2. **Register an Account**
   - Click "Register"
   - Fill in username, email, password
   - Submit

3. **Login**
   - Use your credentials to login
   - You'll be redirected to the dashboard

4. **Explore Features**
   - **Dashboard**: Overview of system status
   - **Weather**: View weather forecasts
   - **Irrigation**: Create and manage irrigation schedules

## 🔧 Troubleshooting

### Service Won't Start

**Problem:** Port already in use
```powershell
# Find process using port
netstat -ano | findstr :8080

# Kill process
taskkill /PID <process-id> /F
```

**Problem:** Database connection error
- Verify MySQL is running
- Check credentials in `application.yml`
- Ensure databases are created

**Problem:** Services not registering with Eureka
- Ensure Eureka started first
- Check network connectivity
- Verify `application.yml` configuration

### Frontend Issues

**Problem:** Cannot connect to backend
- Check proxy configuration in `frontend/proxy.conf.json`
- Verify API Gateway is running on port 8080
- Check browser console for CORS errors

**Problem:** npm install fails
- Delete `node_modules` folder
- Delete `package-lock.json`
- Run `npm cache clean --force`
- Run `npm install` again

### Docker Issues

**Problem:** Container fails to start
```powershell
# View logs
docker-compose logs <service-name>

# Restart service
docker-compose restart <service-name>
```

**Problem:** Database connection timeout
- Increase healthcheck timeout in docker-compose.yml
- Wait longer for MySQL to initialize

## 📊 Default Credentials

**Database:**
- Username: `root`
- Password: `root123` (Change in production!)

**Application:**
- Create your own user via registration endpoint

## 🔐 Security Notes

**For Development:**
- Default credentials are used
- JWT secret is in configuration files
- CORS is permissive

**For Production:**
- Change all default passwords
- Use environment variables for secrets
- Configure proper CORS settings
- Enable HTTPS
- Use Kubernetes secrets

## 📚 Next Steps

1. ✅ Read the full [README.md](README.md)
2. ✅ Review [Architecture Documentation](docs/ARCHITECTURE.md)
3. ✅ Set up [CI/CD Pipeline](docs/CI-CD-PIPELINE.md)
4. ✅ Review [Deployment Guide](docs/DEPLOYMENT.md)
5. ✅ Check [Contributing Guidelines](CONTRIBUTING.md)

## 🆘 Getting Help

- **Issues:** https://github.com/wassimdgh/Micro-Services/issues
- **Discussions:** https://github.com/wassimdgh/Micro-Services/discussions
- **Documentation:** Check the `docs/` folder

## ✅ Checklist

- [ ] All prerequisites installed
- [ ] MySQL databases created
- [ ] All backend services running
- [ ] Eureka shows all services
- [ ] Frontend accessible
- [ ] Can register and login
- [ ] API endpoints responding

---

**Happy Coding! 🎉**
