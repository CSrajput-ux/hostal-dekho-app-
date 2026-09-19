<div align="center">

<img src="logo.png" alt="Hostal Dekho Logo" width="120" height="120" style="border-radius: 24px"/>

# 🏠 Hostal Dekho

### *Find Hostels, PGs & Flats Near You — Instantly*

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple?logo=kotlin)](https://kotlinlang.org)
[![FastAPI](https://img.shields.io/badge/Backend-FastAPI-teal?logo=fastapi)](https://fastapi.tiangolo.com)
[![Firebase](https://img.shields.io/badge/Auth-Firebase-orange?logo=firebase)](https://firebase.google.com)
[![Razorpay](https://img.shields.io/badge/Payment-Razorpay-blue)](https://razorpay.com)
[![SQLite](https://img.shields.io/badge/Database-SQLite-lightblue?logo=sqlite)](https://sqlite.org)

[Features](#-features) • [Architecture](#-architecture) • [Screens](#-screens) • [API Docs](#-api-endpoints) • [Setup](#-setup) • [Database](#-database-schema)

</div>

---

## 📖 About

**Hostal Dekho** is a full-stack Android application that connects students looking for hostels, PGs, and flats with property owners. Students can search, filter, book, and pay for accommodations — while owners can list properties, manage bookings, and track earnings — all from a single platform.

---

## ✨ Features

### 👨‍🎓 Student Features
| Feature | Description |
|---------|-------------|
| 🔍 **Smart Search** | Search by city, filter by type (Hostel/PG/Flat), gender, price range |
| 📍 **Nearby Discovery** | GPS-based hostel discovery within configurable radius |
| 🏠 **Property Detail** | Full property view with rooms, amenities, images, ratings |
| 📅 **Online Booking** | Book rooms with check-in date and duration |
| 💳 **Razorpay Payment** | Secure in-app payment with verification |
| ❤️ **Wishlist** | Save favorite properties to wishlist |
| 📋 **Booking History** | View all past and active bookings |
| 🔔 **Notifications** | Booking status notifications |
| 💸 **Payment & Refund** | View payment history and refund status |
| 👤 **Profile Management** | Edit name, email, phone, gender |
| 🔐 **OTP Login** | Firebase Phone Auth + Google Sign-In |

### 🏢 Owner Features
| Feature | Description |
|---------|-------------|
| 📊 **Dashboard** | Real-time stats — earnings, bookings, active properties |
| ➕ **Add Property** | List hostels/PG/flat with rooms, amenities, images |
| 📋 **My Listings** | View and manage all listed properties |
| 📅 **Booking Management** | See all bookings across all properties |
| 🏦 **Bank Details** | Save bank account for payouts |
| 📄 **KYC Verification** | Upload Aadhaar, PAN, property proof |
| 🎫 **Support Tickets** | Raise and track complaints |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    HOSTAL DEKHO APP                          │
├──────────────────────────┬──────────────────────────────────┤
│   📱 Android App          │   🖥️ FastAPI Backend             │
│   (Kotlin + Jetpack       │   (Python + SQLite)             │
│    Compose)               │                                  │
├──────────────────────────┼──────────────────────────────────┤
│  UI Layer                │  API Layer                       │
│  ├── Screens (33)        │  ├── /api/v1/auth                │
│  ├── ViewModels (8)      │  ├── /api/v1/properties          │
│  └── Components          │  ├── /api/v1/bookings            │
│                          │  ├── /api/v1/payments            │
│  Data Layer              │  ├── /api/v1/kyc                 │
│  ├── Repositories (7)    │  ├── /api/v1/support             │
│  ├── API Service         │  ├── /api/v1/bank                │
│  ├── DTOs / Schemas      │  ├── /api/v1/wishlists           │
│  └── TokenManager        │  └── /api/v1/owner/stats         │
│                          │                                  │
│  DI: Hilt                │  Auth: Firebase + JWT            │
│  HTTP: Retrofit + OkHttp │  Payment: Razorpay               │
│  Images: Coil            │  ORM: SQLAlchemy                 │
└──────────────────────────┴──────────────────────────────────┘
```

### 📐 Clean Architecture (MVVM)

```
┌─────────────────────────────────────────────────┐
│                  UI LAYER                        │
│  Screens (Jetpack Compose) → ViewModels          │
│  AuthViewModel  PropertyViewModel  BankViewModel │
│  KycViewModel   SupportViewModel  WishlistVM     │
└─────────────────┬───────────────────────────────┘
                  │ StateFlow / collectAsState
┌─────────────────▼───────────────────────────────┐
│               DOMAIN LAYER                       │
│  Repositories: Auth, Property, Bank, Kyc,        │
│                Support, Wishlist, Register        │
└─────────────────┬───────────────────────────────┘
                  │ suspend functions
┌─────────────────▼───────────────────────────────┐
│               DATA LAYER                         │
│  HostelDekhoApiService (Retrofit Interface)      │
│  AuthInterceptor (JWT auto-attach)               │
│  TokenManager (SharedPreferences)                │
│  DTOs / Schemas.kt                               │
└─────────────────────────────────────────────────┘
```

---

## 📱 Screens

### Student Flow
```
LandingScreen ──────────────────────────────────────────────┐
     │                                                       │
     ├──► SearchScreen ──► DetailScreen ──► PropertyBookingScreen
     │                                            │
     │                                       [Razorpay SDK]
     │                                            │
     ├──► LoginScreen ──► OtpScreen         ──► MainScreen
     │         └──► RegisterScreen
     │
     └──► ProfileScreen
              ├──► PersonalInformationScreen
              ├──► WishlistedHostelsScreen
              ├──► BookingsScreen
              ├──► NotificationScreen
              ├──► PaymentRefundScreen
              ├──► FollowedPropertiesScreen
              ├──► SecurityScreen
              └──► SettingsScreen
```

### Owner Flow
```
OwnerMainScreen (OwnerHub)
     │
     ├──► OwnerDashboardScreen   [stats: earnings, bookings]
     ├──► MyListingsScreen       [all properties]
     ├──► OwnerBookingsScreen    [all bookings]
     ├──► AddPropertyScreen      [list new property]
     ├──► BankDetailsScreen      [payout account]
     ├──► KycVerificationScreen  [Aadhaar, PAN]
     └──► OwnerSettingsScreen
               └──► ComplaintScreen
```

---

## 🗄️ Database Schema

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│      users       │     │    properties    │     │      rooms       │
├──────────────────┤     ├──────────────────┤     ├──────────────────┤
│ id (PK)          │─┐   │ id (PK)          │─┐   │ id (PK)          │
│ firebase_uid     │ │   │ owner_id (FK)    │◄┘   │ property_id (FK) │◄┐
│ username         │ └──►│ name             │     │ room_type        │ │
│ name             │     │ address / city   │     │ price            │ │
│ email            │     │ latitude/lng     │     │ availability_cnt │ │
│ mobile           │     │ type (H/PG/FLAT) │     │ images (JSON)    │ │
│ role             │     │ gender           │─────┤──────────────────┘ │
│ kyc_status       │     │ amenities (JSON) │     └────────────────────┘
│ auth_provider    │     │ images (JSON)    │
│ created_at       │     │ rating           │
└──────────────────┘     │ is_active        │
        │                │ verified_badge   │
        │                └──────────────────┘
        │                         │
┌───────▼──────────┐     ┌────────▼─────────┐
│     bookings     │     │  kyc_documents   │
├──────────────────┤     ├──────────────────┤
│ id (PK)          │     │ id (PK)          │
│ user_id (FK)     │     │ user_id (FK)     │
│ property_id (FK) │     │ document_type    │
│ room_id (FK)     │     │ document_url     │
│ status           │     │ status           │
│ amount_paid      │     │ uploaded_at      │
│ check_in_date    │     └──────────────────┘
│ duration_months  │
│ razorpay_order_id│     ┌──────────────────┐
│ created_at       │     │  bank_accounts   │
└──────────────────┘     ├──────────────────┤
                         │ id (PK)          │
┌──────────────────┐     │ user_id (FK)     │
│ support_tickets  │     │ account_holder   │
├──────────────────┤     │ account_number   │
│ id (PK)          │     │ ifsc_code        │
│ user_id (FK)     │     │ bank_name        │
│ subject          │     │ is_primary       │
│ description      │     └──────────────────┘
│ status (OPEN...) │
└──────────────────┘     ┌──────────────────┐
                         │ user_wishlists   │
                         ├──────────────────┤
                         │ user_id (FK)     │
                         │ property_id (FK) │
                         │ created_at       │
                         └──────────────────┘
```

---

## 🔌 API Endpoints

### 🔐 Authentication (`/api/v1/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/send-otp` | Send OTP to mobile |
| POST | `/verify-otp` | Verify OTP → JWT tokens |
| POST | `/login` | Login with credentials |
| POST | `/firebase` | Google Sign-In |
| POST | `/refresh` | Refresh access token |
| GET | `/me` | Get current user profile |
| PUT | `/me` | Update current user profile |

### 🏠 Properties (`/api/v1/properties`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/nearby?lat=&lng=&radius=` | GPS-based search |
| GET | `/search?location=&type=&gender=` | Filter search |
| GET | `/{id}` | Full property detail + rooms |
| POST | `/` | Create new listing (owner) |
| GET | `/my` | Owner's properties list |

### 📅 Bookings (`/api/v1/bookings`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create booking + Razorpay order |
| GET | `/my` | Student's booking history |
| GET | `/owner` | All bookings for owner's properties |

### 💳 Payments (`/api/v1/payments`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/verify` | Verify Razorpay payment signature |

### 📊 Owner Stats (`/api/v1`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/owner/stats` | Dashboard: earnings, bookings count |

### 🏦 Bank (`/api/v1/bank`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Add bank account (owner) |
| GET | `/my` | Get owner's bank accounts |

### 📄 KYC (`/api/v1/kyc`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Upload KYC document |
| GET | `/my` | Get user's KYC documents |

### 🎫 Support (`/api/v1/support`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create complaint ticket |
| GET | `/my` | Get user's tickets |

### ❤️ Wishlists (`/api/v1/wishlists`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/{property_id}` | Add to wishlist |
| DELETE | `/{property_id}` | Remove from wishlist |
| GET | `/my` | Get all wishlisted properties |

---

## 🔄 User Journey Flows

### Student Booking Flow
```
[Open App]
    │
    ▼
[LandingScreen] ←── GPS Location / Manual City Select
    │
    ▼
[Property Cards] ── tap ──► [DetailScreen]
                                   │
                              [Book Now]
                                   │
                                   ▼
                          [PropertyBookingScreen]
                                   │
                          [Create Booking API]
                          POST /api/v1/bookings/
                                   │
                          [Razorpay Checkout SDK]
                                   │
                    ┌──── Success ──┴── Failure ────┐
                    ▼                               ▼
          [Verify Payment]               [Show Error]
          POST /api/v1/payments/verify
                    │
                    ▼
          [Booking Confirmed ✅]
```

### Owner Dashboard Flow
```
[Owner Login]
    │
    ▼
[OwnerMainScreen]
    ├──► [Dashboard] ── GET /owner/stats
    │         └── Total Properties, Bookings, Earnings
    │
    ├──► [My Listings] ── GET /properties/my
    │         └── All property cards with status
    │
    ├──► [Add Property] ── POST /properties/
    │         └── Name, Address, Type, Rooms, Amenities
    │
    ├──► [Bookings] ── GET /bookings/owner
    │         └── All customer bookings
    │
    └──► [Settings]
              ├── Bank Details ── POST+GET /bank/
              └── KYC ── POST+GET /kyc/
```

### Auth Flow
```
[LoginScreen]
    │
    ├──[Phone OTP]──► Firebase Phone Auth
    │                       │
    │               [OtpScreen] enter 6-digit OTP
    │                       │
    │               Firebase verify ──► POST /auth/firebase
    │
    ├──[Google]──► Credential Manager ──► POST /auth/firebase
    │
    └──[Email/Password]──► POST /auth/login
                                │
                        [JWT: access_token + refresh_token]
                                │
                        [Saved in SharedPreferences]
                        [Auto-attached via AuthInterceptor]
```

---

## 🛠️ Tech Stack

### Android (Frontend)
| Technology | Purpose |
|-----------|---------|
| **Kotlin** | Primary language |
| **Jetpack Compose** | UI framework |
| **Hilt (Dagger)** | Dependency Injection |
| **Retrofit + OkHttp** | HTTP client |
| **Gson** | JSON serialization |
| **Firebase Auth** | Phone OTP + Google Sign-In |
| **Razorpay SDK** | In-app payment |
| **Coil** | Image loading |
| **Navigation Compose** | Screen navigation |
| **Coroutines + Flow** | Async state management |
| **AndroidX Credential Manager** | Google Sign-In |

### Backend (Python FastAPI)
| Technology | Purpose |
|-----------|---------|
| **FastAPI** | REST API framework |
| **SQLAlchemy** | ORM |
| **SQLite** | Database |
| **Pydantic** | Schema validation |
| **Firebase Admin SDK** | Token verification |
| **python-jose** | JWT generation/validation |
| **bcrypt** | Password hashing |
| **Uvicorn** | ASGI server |

---

## 📁 Project Structure

```
Hostal Dekho/
├── 📱 app/                              # Android App
│   └── src/main/java/com/company/hostaldekho/
│       ├── data/
│       │   ├── remote/
│       │   │   ├── HostelDekhoApiService.kt  # 25+ API endpoints
│       │   │   ├── AuthInterceptor.kt         # JWT auto-attach
│       │   │   └── dto/Schemas.kt             # All DTOs
│       │   ├── repository/
│       │   │   ├── AuthRepository.kt
│       │   │   ├── PropertyRepository.kt
│       │   │   ├── BankRepository.kt
│       │   │   ├── KycRepository.kt
│       │   │   ├── SupportRepository.kt
│       │   │   └── WishlistRepository.kt
│       │   └── local/TokenManager.kt
│       ├── ui/
│       │   ├── screens/ (33 screens)
│       │   ├── viewmodels/
│       │   │   ├── AuthViewModel.kt
│       │   │   ├── PropertyViewModel.kt
│       │   │   ├── BankViewModel.kt
│       │   │   ├── KycViewModel.kt
│       │   │   ├── SupportViewModel.kt
│       │   │   └── WishlistViewModel.kt
│       │   ├── components/DesignSystem.kt    # Reusable UI components
│       │   └── theme/
│       ├── navigation/NavGraph.kt           # All 33+ routes
│       └── di/NetworkModule.kt              # Hilt DI
│
└── 🖥️ backend_fastapi/
    └── app/
        ├── main.py                          # FastAPI app + routers
        ├── models.py                        # SQLAlchemy models
        ├── schemas.py                       # Pydantic schemas
        ├── database.py                      # DB connection
        ├── api/v1/endpoints/
        │   ├── auth.py                      # Authentication
        │   ├── properties.py                # Property CRUD
        │   ├── bookings.py                  # Booking management
        │   ├── payments.py                  # Razorpay verify
        │   ├── bank.py                      # Bank accounts
        │   ├── kyc.py                       # KYC documents
        │   ├── support.py                   # Support tickets
        │   └── wishlists.py                 # Wishlist CRUD
        └── core/
            ├── config.py                    # Settings
            ├── firebase.py                  # Firebase init
            └── security.py                  # JWT helpers
```

---

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio (Hedgehog or later)
- Python 3.10+
- Android device / emulator (API 26+)

### 🖥️ Backend Setup

```bash
# 1. Navigate to backend
cd backend_fastapi

# 2. Create virtual environment
python -m venv .venv
.venv\Scripts\activate          # Windows
# source .venv/bin/activate     # Mac/Linux

# 3. Install dependencies
pip install -r requirements.txt

# 4. Setup environment
copy .env.example .env
# Edit .env with your values:
# SECRET_KEY=your-secret-key
# RAZORPAY_KEY_ID=rzp_test_...
# RAZORPAY_KEY_SECRET=...
# DEBUG_MODE=True

# 5. Run server
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Backend will be available at:
- API: `http://localhost:8000`
- Swagger Docs: `http://localhost:8000/docs`

### 📱 Android Setup

```bash
# 1. Open project in Android Studio

# 2. Set BASE_URL in app/build.gradle.kts
# For emulator: http://10.0.2.2:8000/
# For real device: http://<your-pc-ip>:8000/

# 3. Add your google-services.json to app/ folder
#    (Download from Firebase Console)

# 4. Build & Run
./gradlew assembleDebug
```

---

## 🔐 Environment Variables

Create `backend_fastapi/.env`:

```env
# Security
SECRET_KEY=your-super-secret-key-min-32-chars
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=60
REFRESH_TOKEN_EXPIRE_DAYS=30

# Database
DATABASE_URL=sqlite:///./sql_app.db

# Environment
ENVIRONMENT=development
DEBUG_MODE=True

# Razorpay (get from razorpay.com/dashboard)
RAZORPAY_KEY_ID=rzp_test_xxxxxxxxxxxx
RAZORPAY_KEY_SECRET=your_razorpay_secret

# Firebase (path to service account JSON)
FIREBASE_CREDENTIALS_PATH=./firebase-adminsdk.json

# CORS (for browser access)
CORS_ORIGINS=["http://localhost:3000"]
```

---

## 📊 Frontend ↔ Backend Connection Status

| Screen | Backend API | Status |
|--------|------------|--------|
| LoginScreen | `POST /auth/login`, `/auth/firebase` | ✅ Connected |
| OtpScreen | `POST /auth/send-otp`, `/auth/verify-otp` | ✅ Connected |
| LandingScreen | `GET /properties/nearby` | ✅ Connected |
| SearchScreen | `GET /properties/search` | ✅ Connected |
| DetailScreen | `GET /properties/{id}` | ✅ Connected |
| PropertyBookingScreen | `POST /bookings/` + Razorpay | ✅ Connected |
| BookingsScreen | `GET /bookings/my` | ✅ Connected |
| PersonalInformationScreen | `GET+PUT /auth/me` | ✅ Connected |
| OwnerDashboardScreen | `GET /owner/stats` | ✅ Connected |
| OwnerBookingsScreen | `GET /bookings/owner` | ✅ Connected |
| MyListingsScreen | `GET /properties/my` | ✅ Connected |
| AddPropertyScreen | `POST /properties/` | ✅ Connected |
| ComplaintScreen | `POST /support/` | ✅ Connected |
| BankDetailsScreen | `GET+POST /bank/` | ✅ Connected |
| KycVerificationScreen | `GET+POST /kyc/` | ✅ Connected |
| WishlistedHostelsScreen | `GET /wishlists/my` | ✅ Connected |
| NotificationScreen | `GET /bookings/my` (derived) | ✅ Connected |
| PaymentRefundScreen | `GET /bookings/my` (derived) | ✅ Connected |
| FollowedPropertiesScreen | `GET /wishlists/my` | ✅ Connected |

---

## 👥 User Roles

| Role | Permissions |
|------|------------|
| **STUDENT** | Browse, search, book, pay, wishlist, profile |
| **OWNER** | All Student permissions + list properties, manage bookings, view stats, bank/KYC |
| **ADMIN** | All permissions + admin panel access |

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License.

---

<div align="center">

**Made with ❤️ by CSrajput-ux**

[⭐ Star this repo](https://github.com/CSrajput-ux/hostal-dekho-app-) • [🐛 Report Bug](https://github.com/CSrajput-ux/hostal-dekho-app-/issues) • [💡 Request Feature](https://github.com/CSrajput-ux/hostal-dekho-app-/issues)

</div>
