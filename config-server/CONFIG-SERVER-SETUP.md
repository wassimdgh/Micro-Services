# Config Server - GitLab Integration Setup

## Configuration Summary

The config-server has been successfully configured with the following settings:

### GitLab Repository Connection
- **Repository URL**: https://gitlab.com/wassimdaghfous3/microservice.git
- **Authentication**: Personal Access Token (OAuth2)
- **Branch**: main
- **Clone on Start**: Enabled

### Server Configuration
- **Port**: 8071
- **Eureka Registration**: Enabled
- **Eureka Server URL**: http://localhost:8070/eureka

### Configuration Files
The configuration has been migrated from `.properties` to `.yml` format as requested.

## How It Works

1. **Config Server starts** on port 8071
2. **Connects to GitLab** repository using your access token
3. **Clones the configuration files** from the repository
4. **Registers with Eureka** so other services can discover it
5. **Serves configuration** to microservices (ms-meteo, ms-arrosage, etc.)

## Configuration Files in GitLab Repository

Your GitLab repository should contain YAML configuration files for each microservice:
- `ms-meteo.yml` or `ms-meteo-local.yml`
- `ms-arrosage.yml` or `ms-arrosage-local.yml`
- `gateway-service.yml`
- `application.yml` (common configuration for all services)

## Starting the Config Server

### From Eclipse/STS:
1. Right-click on `ConfigServerApplication.java`
2. Run As → Spring Boot App
3. Wait for the message: "Started ConfigServerApplication"
4. Verify it's registered in Eureka at: http://localhost:8070

### From Command Line:
```bash
cd config-server
mvn spring-boot:run
```

## Endpoints

- **Health Check**: http://localhost:8071/actuator/health
- **Config for ms-meteo**: http://localhost:8071/ms-meteo/default
- **Config for ms-arrosage**: http://localhost:8071/ms-arrosage/default
- **Refresh Config**: POST http://localhost:8071/actuator/refresh

## Troubleshooting

If the config-server fails to start:
1. Verify Eureka Server is running on port 8070
2. Check GitLab repository is accessible
3. Verify the access token is valid
4. Check logs for Git clone errors

## Security Note

The GitLab access token is currently stored in the configuration file. For production:
- Use environment variables: `${GITLAB_TOKEN}`
- Use Spring Cloud Vault
- Use encrypted properties
