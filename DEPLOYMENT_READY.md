# HostelDekho — Complete Production Deployment Guide

HostelDekho application ko production me deploy karne ke liye sari configurations aur automation scripts ready kar di gayi hain. Ye guide **Backend (FastAPI + PostgreSQL + Redis)** aur **Android Mobile App (Release APK & AAB)** dono ke deployment ko step-by-step explain karti hai.

---

## 🚀 Quick Summary of Deployment Assets

| Component | Target Platform | Files Prepared |
|---|---|---|
| **1-Click Backend Cloud** | Render.com | [`render.yaml`](render.yaml) |
| **PaaS Cloud** | Railway.app | [`backend_fastapi/railway.json`](backend_fastapi/railway.json), [`backend_fastapi/Procfile`](backend_fastapi/Procfile) |
| **Self-Hosted VPS** | AWS EC2 / DigitalOcean / Hetzner | [`backend_fastapi/deploy.sh`](backend_fastapi/deploy.sh), [`backend_fastapi/docker-compose.yml`](backend_fastapi/docker-compose.yml) |
| **Windows Server / Local** | Windows PowerShell | [`backend_fastapi/deploy.ps1`](backend_fastapi/deploy.ps1) |
| **CI / CD Pipelines** | GitHub Actions | [`.github/workflows/backend-ci-cd.yml`](.github/workflows/backend-ci-cd.yml), [`.github/workflows/android-release.yml`](.github/workflows/android-release.yml) |
| **Android Release Signing** | Play Store / Sideload APK | [`generate_keystore.bat`](generate_keystore.bat), [`generate_keystore.sh`](generate_keystore.sh), [`app/build.gradle.kts`](app/build.gradle.kts) |

---

## 1. Backend Deployment (3 Ready Options)

### Option A: Render.com (Recommended — 1-Click Setup)

Render par repository connect karke 1-click me FastAPI backend, PostgreSQL, aur Redis deploy ho jayega:

1. Apne GitHub repo ko Render par push karein.
2. [Render Dashboard](https://dashboard.render.com/) me jayein -> **New** -> **Blueprint**.
3. Apna repository select karein (`render.yaml` automatically detect ho jayega).
4. Render automatically create karega:
   - **FastAPI Docker Web Service** (Auto-healing, multi-worker Gunicorn)
   - **Managed PostgreSQL Database**
   - **Managed Redis Cache**
5. Dashboard me sensitive keys fill karein:
   - `RAZORPAY_KEY_ID` & `RAZORPAY_KEY_SECRET`
   - `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`
6. Deploy par click karein. Health check URL: `https://<your-service>.onrender.com/api/health`

---

### Option B: Railway.app (Fast & Modern)

1. [Railway.app](https://railway.app/) me jayein -> **New Project** -> **Deploy from GitHub repo**.
2. Service Settings me **Root Directory** ko `backend_fastapi` set karein.
3. Database ke liye Railway me **PostgreSQL** aur **Redis** plugin add karein.
4. Environment Variables me values paste karein:
   - `DATABASE_URL`: `${{Postgres.DATABASE_URL}}`
   - `REDIS_URL`: `${{Redis.REDIS_URL}}`
   - `JWT_SECRET`: 32+ character random string
   - `ENVIRONMENT`: `production`
   - `DEBUG_MODE`: `false`
5. Railway automatically `railway.json` aur `Dockerfile` se build karke live URL generate kar dega.

---

### Option C: VPS / AWS EC2 / DigitalOcean (Full Control)

Agar aap apne khud ke Linux server (Ubuntu 22.04 / 24.04) par run kar rahe hain:

```bash
# 1. Server par repository clone karein
git clone <your-repo-url>
cd "Hostal dekho/backend_fastapi"

# 2. Environment file configure karein
cp .env.production .env
nano .env   # Update JWT_SECRET, RAZORPAY keys, Neon/PostgreSQL credentials

# 3. 1-Command Automated Deploy chalayein
chmod +x deploy.sh
./deploy.sh
```

`deploy.sh` script automatically:
- Docker aur Compose check karega.
- PostgreSQL 16 (Tuned for 10k connections) aur Redis 7 start karega.
- Multi-stage Docker image build karega.
- Alembic database migrations run karega (`alembic upgrade head`).
- Nginx reverse proxy (Gzip + Rate limiting) start karega.
- Deep health endpoint test karega (`/api/health`).

---

## 2. Android Mobile App — Production Release Build

### Step 1: Keystore Generate Karein (Release Signing)

Agar aapke paas abhi release keystore nahi hai, toh project root me diya gaya script run karein:

**Windows par:**
```cmd
generate_keystore.bat
```

**Linux / Mac par:**
```bash
chmod +x generate_keystore.sh
./generate_keystore.sh
```

Ye script `hosteldekho-release.jks` generate karega.

---

### Step 2: Signing Properties Set Karein

Apne `gradle.properties` file me ya system environment variables me ye lines add karein:

```properties
RELEASE_KEYSTORE_PATH=c:/Users/jkgga/Music/Hostal dekho/hosteldekho-release.jks
RELEASE_KEYSTORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=hosteldekho
RELEASE_KEY_PASSWORD=your_key_password
```

---

### Step 3: Production APK & Play Store Bundle (AAB) Build Karein

#### Option 1: Signed Release APK (Direct Device Installation & Testing)
```bash
./gradlew.bat app:assembleRelease -PPROD_API_BASE_URL="https://your-backend-api.onrender.com/api/v1/"
```
Output APK location:
`app/build/outputs/apk/release/app-release.apk`

#### Option 2: Google Play Store Bundle (.aab)
```bash
./gradlew.bat app:bundleRelease -PPROD_API_BASE_URL="https://your-backend-api.onrender.com/api/v1/"
```
Output AAB location:
`app/build/outputs/bundle/release/app-release.aab`

---

## 3. Pre-Flight Production Checklist

Launch karne se pehle ye verify karein:

- [ ] **Database Connection**: Neon PostgreSQL ya server database active hai aur `alembic upgrade head` apply ho chuka hai.
- [ ] **JWT_SECRET**: `.env` me 32+ characters ka strong secure token hai (`python -c "import secrets; print(secrets.token_hex(32))"`).
- [ ] **DEBUG_MODE**: Production me `DEBUG_MODE=false` set hai (OTP debug responses hide rehte hain).
- [ ] **Razorpay Live Keys**: Test keys (`rzp_test_...`) ki jagah Live Keys (`rzp_live_...`) daali gayi hain.
- [ ] **Firebase OAuth**: Production keystore ka **SHA-1** fingerprint Google Cloud / Firebase Console me add kiya gaya hai (taki Google Sign-In release build me bhi kaam kare).
- [ ] **Cloudinary**: Property photo uploads ke liye Cloudinary credentials active hain.
- [ ] **Health Check**: `GET /api/health` status `healthy` return kar raha hai.
