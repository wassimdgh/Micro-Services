# CI/CD Pipeline Documentation

## Overview

This project uses GitHub Actions for continuous integration and deployment. The pipeline consists of multiple workflows that handle building, testing, security scanning, and deployment.

## Workflows

### 1. Main CI/CD Pipeline (`ci-cd.yml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches

**Jobs:**

#### Build Backend Services
- Builds all Spring Boot microservices
- Runs Maven tests
- Uploads artifacts for later stages
- Matrix strategy for parallel execution

#### Build Frontend
- Builds Angular application
- Runs linting (if configured)
- Creates production build
- Uploads build artifacts

#### Build Docker Images
- Only runs on push to `main`
- Builds Docker images for all services
- Pushes to GitHub Container Registry (ghcr.io)
- Uses BuildKit cache for faster builds

#### Security Scan
- Runs Trivy vulnerability scanner
- Scans filesystem and dependencies
- Uploads results to GitHub Security

#### Deploy to Staging
- Deploys to staging environment
- Waits for pods to be ready
- Verifies deployment health

#### Deploy to Production
- Requires manual approval (GitHub Environments)
- Deploys to production cluster
- Includes rollback capabilities

### 2. Docker Build Workflow (`docker-build.yml`)

**Purpose:** Build and push individual Docker images

**Triggers:**
- Manual workflow dispatch
- Push to `main` when service files change

**Features:**
- Parallel builds for all services
- Multi-platform support
- Layer caching

### 3. Code Quality Workflow (`code-quality.yml`)

**Purpose:** Ensure code quality standards

**Includes:**
- JUnit/Mockito tests
- Code coverage (JaCoCo)
- SonarCloud analysis (optional)
- Frontend linting and testing

## Setup Instructions

### 1. Repository Secrets

Configure the following secrets in GitHub Settings > Secrets:

#### Required Secrets:
```
GITHUB_TOKEN                 # Automatically provided by GitHub
KUBE_CONFIG_STAGING         # Base64-encoded kubeconfig for staging (optional)
KUBE_CONFIG_PROD            # Base64-encoded kubeconfig for production (optional)
```

#### Optional Secrets (for enhanced features):
```
DOCKER_USERNAME             # Docker Hub username (if using Docker Hub)
DOCKER_PASSWORD             # Docker Hub password
SONAR_TOKEN                # SonarCloud token for code analysis
CODECOV_TOKEN              # Codecov token for coverage reports
SLACK_WEBHOOK              # Slack webhook for notifications
```

### 2. Create Kubeconfig Secrets

To create base64-encoded kubeconfig:

```powershell
# Linux/Mac
cat ~/.kube/config | base64 -w 0

# Windows PowerShell
[Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes((Get-Content ~/.kube/config -Raw)))
```

Add the output as `KUBE_CONFIG_STAGING` or `KUBE_CONFIG_PROD` secret.

### 3. Configure GitHub Environments

Create two environments in GitHub Settings > Environments:

#### Staging Environment
- No protection rules required
- URL: `https://staging.irrigation-system.com`

#### Production Environment
- Enable "Required reviewers"
- Add deployment branch rule (only `main`)
- URL: `https://irrigation-system.com`

### 4. Container Registry Setup

The pipeline uses GitHub Container Registry (ghcr.io) by default:

1. Ensure packages visibility is set to public or configure access
2. Images are automatically tagged with:
   - `latest` (for main branch)
   - `main-<sha>` (commit SHA)
   - Branch name

### 5. Enable GitHub Actions

1. Go to repository Settings > Actions > General
2. Set "Actions permissions" to "Allow all actions"
3. Enable "Read and write permissions" for GITHUB_TOKEN

## Pipeline Flow

```
┌─────────────────────────────────────────────────────┐
│                   Push to main                       │
└────────────────────┬────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
        ▼                         ▼
┌───────────────┐         ┌──────────────┐
│ Build Backend │         │Build Frontend│
│  (Maven Test) │         │  (npm build) │
└───────┬───────┘         └──────┬───────┘
        │                        │
        └────────┬───────────────┘
                 │
                 ▼
        ┌────────────────┐
        │ Build Docker   │
        │    Images      │
        └────────┬───────┘
                 │
                 ▼
        ┌────────────────┐
        │ Security Scan  │
        │   (Trivy)      │
        └────────┬───────┘
                 │
                 ▼
        ┌────────────────┐
        │ Deploy Staging │
        │  (Auto)        │
        └────────┬───────┘
                 │
                 ▼
        ┌────────────────┐
        │ Deploy Prod    │
        │ (Approval)     │
        └────────────────┘
```

## Local Testing

### Test Backend Build
```powershell
# Test Maven build
cd authentification
mvn clean package
```

### Test Frontend Build
```powershell
# Test Angular build
cd frontend
npm install
npm run build:prod
```

### Test Docker Build
```powershell
# Build single service
docker build -t test/authentification:local ./authentification

# Build all services
docker-compose build
```

## Monitoring Deployments

### View Workflow Runs
- Go to Actions tab in GitHub
- Click on specific workflow run
- View logs for each job

### Check Deployment Status
```powershell
# Connect to cluster
kubectl config use-context <context-name>

# Check pods
kubectl get pods -n irrigation-system

# Check deployments
kubectl get deployments -n irrigation-system

# View logs
kubectl logs -f deployment/gateway-service -n irrigation-system
```

## Troubleshooting

### Build Failures

**Maven Build Fails:**
- Check Java version (must be 17)
- Verify dependencies in pom.xml
- Check test failures

**Frontend Build Fails:**
- Clear npm cache: `npm cache clean --force`
- Delete node_modules and reinstall
- Check TypeScript errors

### Deployment Failures

**Image Pull Errors:**
- Verify image exists in registry
- Check image tag in manifest
- Verify registry credentials

**Pod Not Starting:**
- Check pod logs: `kubectl logs <pod-name>`
- Verify resource limits
- Check database connectivity

### Security Scan Failures

**High/Critical Vulnerabilities:**
- Review Trivy output
- Update dependencies
- Apply security patches

## Best Practices

1. **Branch Protection:**
   - Require PR reviews before merging to main
   - Require status checks to pass
   - Enable branch protection rules

2. **Version Tagging:**
   - Tag releases: `git tag -a v1.0.0 -m "Release v1.0.0"`
   - Push tags: `git push origin --tags`

3. **Secrets Management:**
   - Rotate secrets regularly
   - Use environment-specific secrets
   - Never commit secrets to repository

4. **Monitoring:**
   - Monitor workflow execution times
   - Set up notifications for failures
   - Review security scan results

## Advanced Configuration

### Custom Build Matrix

Modify the matrix in `ci-cd.yml`:

```yaml
strategy:
  matrix:
    service: 
      - authentification
      - ms-meteo
      - ms-arrosage
    java-version: [17, 21]  # Test multiple Java versions
```

### Conditional Deployments

Add conditions to deployment jobs:

```yaml
deploy-production:
  if: github.event_name == 'push' && startsWith(github.ref, 'refs/tags/')
```

### Slack Notifications

Add notification step:

```yaml
- name: Notify Slack
  uses: slackapi/slack-github-action@v1
  with:
    webhook-url: ${{ secrets.SLACK_WEBHOOK }}
    payload: |
      {
        "text": "Deployment to production successful!"
      }
```

## Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [Spring Boot Best Practices](https://spring.io/guides)

## Support

For issues with the CI/CD pipeline:
1. Check workflow logs in GitHub Actions
2. Review this documentation
3. Create an issue in the repository
4. Contact DevOps team
