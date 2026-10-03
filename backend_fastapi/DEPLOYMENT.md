# HostelDekho — Production Deployment & 10k Scaling Guide

This guide details how to deploy the HostelDekho backend to production and run it smoothly under a load of **10,000+ active users**.

---

## 1. Quick 1-Click Production Deployment with Docker Compose

The fastest way to deploy the entire production stack (FastAPI Gunicorn Multi-Worker + PostgreSQL 16 + Redis 7 + Nginx with Gzip & Rate Limiting) is using Docker Compose.

### Step 1: Configure Environment Variables
Copy `.env.production` to `.env`:
```bash
cp .env.production .env
```
Edit `.env` and configure:
1. `JWT_SECRET`: Generate a secure 32+ character key:
   ```bash
   python -c "import secrets; print(secrets.token_hex(32))"
   ```
2. `POSTGRES_PASSWORD`: Choose a strong database password.
3. `CORS_ORIGINS`: Set your production web client origins (e.g. `https://hosteldekho.com`).
4. `RAZORPAY_KEY_ID` & `RAZORPAY_KEY_SECRET`: Live credentials from Razorpay Dashboard.
5. `CLOUDINARY_*`: Cloudinary credentials for property photos.

### Step 2: Build & Start Containers
```bash
docker compose up -d --build
```

### Step 3: Run Database Migrations
```bash
docker compose exec api alembic upgrade head
```

### Step 4: Verify Deployment Health
```bash
curl http://localhost/api/health
```
Response:
```json
{
  "status": "healthy",
  "database": "healthy",
  "cache": "redis",
  "environment": "production",
  "debug_mode": false
}
```

---

## 2. Cloud Platform Deployment (Render / Railway / AWS / DigitalOcean)

### Option A: Render.com
1. Create a **PostgreSQL** instance on Render (Starter or Standard tier).
2. Create a **Redis** instance on Render or Upstash Redis.
3. Create a **Web Service**:
   - **Environment**: Docker
   - **Dockerfile Path**: `./Dockerfile`
   - **Health Check Path**: `/api/health`
   - **Environment Variables**:
     - `ENVIRONMENT`: `production`
     - `DEBUG_MODE`: `false`
     - `DATABASE_URL`: Your Render PostgreSQL External/Internal URL
     - `REDIS_URL`: Your Render/Upstash Redis URL
     - `JWT_SECRET`: 64-character secret hex
     - `WEB_CONCURRENCY`: `4`
     - `CORS_ORIGINS`: `https://app.yourdomain.com`

### Option B: Railway.app
1. Add PostgreSQL & Redis plugins to your Railway project.
2. Deploy the repository pointing to `backend_fastapi/Dockerfile`.
3. Set the environment variables in Railway dashboard.
4. Set custom domain and enable automatic HTTPS.

### Option C: AWS (ECS Fargate + RDS PostgreSQL + ElastiCache Redis)
1. **RDS PostgreSQL**: db.t4g.medium (2 vCPU, 4GB RAM) with `max_connections=200`.
2. **ElastiCache Redis**: cache.t4g.micro (1 vCPU, 0.5GB RAM).
3. **ECS Fargate Task**: 1 vCPU, 2GB RAM per task, minimum 2 tasks behind an Application Load Balancer (ALB).
4. **ALB Health Check**: `/api/health` with HTTP 200 expected.

---

## 3. High-Concurrency Sizing for 10,000 Users

| Metric | Target Specification |
|---|---|
| **Daily Active Users (DAU)** | 10,000 users |
| **Peak Concurrent Users** | 500 – 1,500 active users |
| **Peak Requests Per Second (RPS)** | 250 – 800 RPS |
| **FastAPI Worker Allocation** | 4 Gunicorn ASGI Workers per container (2 containers = 8 workers) |
| **Database Pool Size** | 25 base + 35 overflow per instance |
| **Redis Cache Hit Ratio** | > 85% for search and nearby hostel queries |
| **Response Latency (p95)** | < 45ms for cached queries, < 120ms for complex queries |
| **Bandwidth Optimization** | GZip compression enabled (reduces payload by 70%) |

---

## 4. Android Client Production Release Build

When building the release APK/Bundle for Android:
1. Update API Base URL to production HTTPS:
   ```bash
   ./gradlew assembleRelease -PPROD_API_BASE_URL="https://api.hosteldekho.com/api/v1/"
   ```
2. Replace debug Razorpay key in `app/build.gradle.kts` with your live Razorpay key (`rzp_live_...`).
3. Ensure `usesCleartextTraffic="false"` in `AndroidManifest.xml` for production HTTPS enforcement.

---

## 5. Load Testing (Locust)

Verify the 10,000 user concurrency capacity before launch:
```bash
cd backend_fastapi
pip install locust
locust -f locustfile.py --host=http://localhost:8000
```
Open `http://localhost:8089` in your browser:
- **Number of users**: `1000`
- **Spawn rate**: `50` users/sec
- Monitor RPS, error rate (must be 0%), and response times (p95 < 100ms).
