#!/usr/bin/env bash
set -e

# ==============================================================================
# HostelDekho — Production Deployment Script (Linux VPS / Cloud Server)
# Usage: ./deploy.sh
# ==============================================================================

echo "=================================================="
echo " Starting HostelDekho Production Deployment...    "
echo "=================================================="

# Check Docker installation
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is not installed. Please install Docker first: https://docs.docker.com/engine/install/"
    exit 1
fi

# Ensure .env file exists
if [ ! -f .env ]; then
    if [ -f .env.production ]; then
        echo "[INFO] No .env found. Copying .env.production to .env..."
        cp .env.production .env
        echo "[ACTION REQUIRED] Please edit .env with your real JWT_SECRET and credentials, then rerun ./deploy.sh"
        exit 1
    else
        echo "ERROR: Neither .env nor .env.production found!"
        exit 1
    fi
fi

echo "[1/4] Pulling latest base images..."
docker compose pull db redis nginx || true

echo "[2/4] Building and launching Docker Compose stack..."
docker compose up -d --build --remove-orphans

echo "[3/4] Running database migrations..."
docker compose exec -T api alembic upgrade head

echo "[4/4] Verifying production health status..."
sleep 5
HEALTH_URL="http://localhost/api/health"
if command -v curl &> /dev/null; then
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL" || true)
    if [ "$RESPONSE" = "200" ]; then
        echo "=================================================="
        echo " ✓ HostelDekho Backend is LIVE and HEALTHY!       "
        echo "   API URL: http://localhost/                     "
        echo "   Health : http://localhost/api/health           "
        echo "=================================================="
    else
        echo "⚠ Healthcheck returned HTTP $RESPONSE. Checking container logs..."
        docker compose logs --tail=50 api
    fi
else
    echo "Deployment completed. Please verify health at: $HEALTH_URL"
fi
