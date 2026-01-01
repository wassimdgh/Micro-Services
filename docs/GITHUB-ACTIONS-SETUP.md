# GitHub Actions Setup Guide

This document explains how to configure GitHub Actions for your repository.

## Required GitHub Secrets

To enable the CI/CD pipeline, you need to configure the following secrets in your GitHub repository.

### How to Add Secrets

1. Go to your repository on GitHub: https://github.com/wassimdgh/Micro-Services
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each secret below

## Required Secrets

### 1. Container Registry (Automatically Available)

**`GITHUB_TOKEN`**
- **Description:** Automatically provided by GitHub
- **Usage:** Authentication for GitHub Container Registry
- **Action:** No configuration needed - automatically available

### 2. Kubernetes Configuration (Optional - For Deployment)

**`KUBE_CONFIG_STAGING`**
- **Description:** Base64-encoded kubeconfig for staging environment
- **How to create:**
```powershell
# PowerShell
[Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes((Get-Content ~/.kube/config -Raw)))
```
- **Usage:** Deploy to staging Kubernetes cluster

**`KUBE_CONFIG_PROD`**
- **Description:** Base64-encoded kubeconfig for production environment
- **How to create:** Same as staging
- **Usage:** Deploy to production Kubernetes cluster

### 3. Code Quality Tools (Optional)

**`SONAR_TOKEN`**
- **Description:** SonarCloud authentication token
- **How to get:**
  1. Go to https://sonarcloud.io
  2. Sign up with GitHub account
  3. Go to Account → Security → Generate token
- **Usage:** Code quality analysis

**`CODECOV_TOKEN`**
- **Description:** Codecov authentication token
- **How to get:**
  1. Go to https://codecov.io
  2. Sign up with GitHub account
  3. Add your repository
  4. Copy the upload token
- **Usage:** Code coverage reporting

## Optional Secrets

### Docker Hub (If using Docker Hub instead of GitHub Container Registry)

**`DOCKER_USERNAME`**
- Your Docker Hub username

**`DOCKER_PASSWORD`**
- Your Docker Hub password or access token

### Notifications

**`SLACK_WEBHOOK`**
- **Description:** Slack webhook URL for notifications
- **How to get:**
  1. Go to your Slack workspace
  2. Create an incoming webhook
  3. Copy the webhook URL
- **Usage:** Send deployment notifications to Slack

## GitHub Environments Setup

### Create Staging Environment

1. Go to **Settings** → **Environments**
2. Click **New environment**
3. Name: `staging`
4. Configure:
   - ✅ No protection rules
   - ✅ Environment secrets (if different from repository secrets)
   - ✅ Environment URL: `https://staging.your-domain.com`

### Create Production Environment

1. Click **New environment**
2. Name: `production`
3. Configure protection rules:
   - ✅ **Required reviewers:** Add team members
   - ✅ **Wait timer:** 5 minutes (optional)
   - ✅ **Deployment branches:** Only `main` branch
   - ✅ Environment URL: `https://your-domain.com`

## Workflow Permissions

1. Go to **Settings** → **Actions** → **General**
2. Under "Workflow permissions":
   - ✅ Select **Read and write permissions**
   - ✅ Check **Allow GitHub Actions to create and approve pull requests**
3. Click **Save**

## Enable GitHub Actions

1. Go to **Actions** tab
2. If prompted, click **I understand my workflows, go ahead and enable them**
3. Workflows will run automatically on:
   - Push to `main` or `develop` branches
   - Pull requests to `main` or `develop` branches

## Verify Setup

### Check Workflow Status

1. Go to **Actions** tab
2. You should see the workflows:
   - ✅ CI/CD Pipeline
   - ✅ Docker Build and Push
   - ✅ Code Quality

### Test the Pipeline

Make a small change and push:

```powershell
# Make a change
echo "# Test" >> README.md

# Commit and push
git add README.md
git commit -m "Test CI/CD pipeline"
git push origin main
```

### Monitor the Build

1. Go to **Actions** tab
2. Click on the running workflow
3. View real-time logs
4. Check for any errors

## Disable Workflows (If Needed)

If you want to disable certain workflows temporarily:

1. Go to **.github/workflows/**
2. Rename the file extension from `.yml` to `.yml.disabled`
3. Commit and push

Or in GitHub UI:
1. Go to **Actions**
2. Select the workflow
3. Click **•••** → **Disable workflow**

## Troubleshooting

### Workflow Not Running

**Check:**
- Workflows are enabled in repository settings
- File is in `.github/workflows/` directory
- File has `.yml` extension
- YAML syntax is correct

### Permission Errors

**Solution:**
- Verify workflow permissions (Settings → Actions → General)
- Ensure `GITHUB_TOKEN` has write permissions
- Check environment protection rules

### Deployment Fails

**Check:**
- Kubernetes secrets are correctly base64-encoded
- Cluster is accessible from GitHub Actions
- Namespaces exist in cluster
- Resource limits are not exceeded

### Build Fails

**Check:**
- Java version is 17
- Maven dependencies are available
- Node.js version is 18+
- Tests are passing locally

## Security Best Practices

1. **Never commit secrets** to the repository
2. **Use environment-specific secrets** when needed
3. **Rotate secrets regularly**
4. **Use minimal permissions** for tokens
5. **Enable branch protection** on main branch
6. **Require code reviews** before merging
7. **Use environments** for production deployments

## GitHub Container Registry

Images are automatically pushed to GitHub Container Registry:

**Image URLs:**
- `ghcr.io/wassimdgh/authentification:latest`
- `ghcr.io/wassimdgh/ms-meteo:latest`
- `ghcr.io/wassimdgh/ms-arrosage:latest`
- `ghcr.io/wassimdgh/gateway-service:latest`
- `ghcr.io/wassimdgh/eureka-server:latest`
- `ghcr.io/wassimdgh/config-server:latest`
- `ghcr.io/wassimdgh/frontend:latest`

### Make Images Public

1. Go to your profile → **Packages**
2. Click on a package
3. **Package settings** → **Change visibility**
4. Select **Public**

## Next Steps

1. ✅ Configure all required secrets
2. ✅ Set up environments (staging/production)
3. ✅ Enable workflows
4. ✅ Test with a commit
5. ✅ Configure branch protection
6. ✅ Set up notifications (optional)
7. ✅ Review deployment logs

## Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [GitHub Environments](https://docs.github.com/en/actions/deployment/targeting-different-environments/using-environments-for-deployment)

---

Need help? Open an issue: https://github.com/wassimdgh/Micro-Services/issues
