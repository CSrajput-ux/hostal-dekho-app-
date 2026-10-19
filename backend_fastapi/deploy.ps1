# ==============================================================================
# HostelDekho — Production Deployment Script for Windows PowerShell
# Usage: .\deploy.ps1
# ==============================================================================

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " Starting HostelDekho Production Deployment...    " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# Check Docker
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: Docker CLI not found. Please install Docker Desktop for Windows." -ForegroundColor Red
    exit 1
}

# Check .env
if (-not (Test-Path ".env")) {
    if (Test-Path ".env.production") {
        Write-Host "[INFO] Copying .env.production to .env..." -ForegroundColor Yellow
        Copy-Item ".env.production" ".env"
        Write-Host "[ACTION REQUIRED] Please edit .env with your real JWT_SECRET and credentials, then rerun .\deploy.ps1" -ForegroundColor Yellow
        exit 1
    } else {
        Write-Host "ERROR: .env not found." -ForegroundColor Red
        exit 1
    }
}

Write-Host "[1/4] Building and launching Docker Compose containers..." -ForegroundColor Green
docker compose up -d --build --remove-orphans

Write-Host "[2/4] Waiting for database initialization..." -ForegroundColor Green
Start-Sleep -Seconds 5

Write-Host "[3/4] Running database migrations..." -ForegroundColor Green
docker compose exec -T api alembic upgrade head

Write-Host "[4/4] Verifying health check..." -ForegroundColor Green
Start-Sleep -Seconds 3

try {
    $response = Invoke-RestMethod -Uri "http://localhost/api/health" -Method Get
    Write-Host "==================================================" -ForegroundColor Green
    Write-Host " ✓ HostelDekho Backend is LIVE and HEALTHY!       " -ForegroundColor Green
    Write-Host "   Status      : $($response.status)" -ForegroundColor Green
    Write-Host "   Database    : $($response.database)" -ForegroundColor Green
    Write-Host "   Cache       : $($response.cache)" -ForegroundColor Green
    Write-Host "   Environment : $($response.environment)" -ForegroundColor Green
    Write-Host "==================================================" -ForegroundColor Green
} catch {
    Write-Host "⚠ Healthcheck failed or server still starting: $_" -ForegroundColor Yellow
    Write-Host "Checking recent container logs:" -ForegroundColor Yellow
    docker compose logs --tail=30 api
}
