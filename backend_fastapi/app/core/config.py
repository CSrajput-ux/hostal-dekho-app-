import os
from dotenv import load_dotenv

load_dotenv()


class Settings:
    PROJECT_NAME: str = "HostelDekho"
    ENVIRONMENT: str = os.getenv("ENVIRONMENT", "development").lower()
    DATABASE_URL: str = os.getenv("DATABASE_URL", "")

    FIREBASE_SERVICE_ACCOUNT: str = os.getenv(
        "FIREBASE_SERVICE_ACCOUNT", "firebaseServiceAccount.json"
    )

    CLOUDINARY_CLOUD_NAME: str = os.getenv("CLOUDINARY_CLOUD_NAME", "")
    CLOUDINARY_API_KEY: str = os.getenv("CLOUDINARY_API_KEY", "")
    CLOUDINARY_API_SECRET: str = os.getenv("CLOUDINARY_API_SECRET", "")

    RAZORPAY_KEY_ID: str = os.getenv("RAZORPAY_KEY_ID", "")
    RAZORPAY_KEY_SECRET: str = os.getenv("RAZORPAY_KEY_SECRET", "")

    # BUG-30 FIX: No insecure fallback — must be set in .env
    JWT_SECRET: str = os.getenv("JWT_SECRET", "")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7       # 7 days
    REFRESH_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 30     # 30 days

    # OTP config
    OTP_EXPIRE_SECONDS: int = 300   # 5 minutes
    # OTPs must never be returned by default. Enable this explicitly only in a
    # local development environment.
    DEBUG_MODE: bool = os.getenv("DEBUG_MODE", "false").lower() == "true"
    CORS_ORIGINS: list[str] = [
        origin.strip() for origin in os.getenv("CORS_ORIGINS", "").split(",") if origin.strip()
    ]

    # ─────────────────────────────────────────────
    # Scalability & High-Concurrency (10k Users)
    # ─────────────────────────────────────────────
    REDIS_URL: str = os.getenv("REDIS_URL", "")
    CACHE_TTL_SECONDS: int = int(os.getenv("CACHE_TTL_SECONDS", "180"))  # 3 minutes default

    # Database Connection Pool Settings
    # QueuePool sized for high concurrent throughput with graceful burst handling
    DB_POOL_SIZE: int = int(os.getenv("DB_POOL_SIZE", "25"))
    DB_MAX_OVERFLOW: int = int(os.getenv("DB_MAX_OVERFLOW", "35"))
    DB_POOL_TIMEOUT: int = int(os.getenv("DB_POOL_TIMEOUT", "30"))
    DB_POOL_RECYCLE: int = int(os.getenv("DB_POOL_RECYCLE", "1800"))
    USE_NULLPOOL: bool = os.getenv("USE_NULLPOOL", "false").lower() == "true"

    # Rate Limiting (Anti-abuse & DDoS mitigation)
    RATE_LIMIT_AUTH: str = os.getenv("RATE_LIMIT_AUTH", "5/minute")
    RATE_LIMIT_SEARCH: str = os.getenv("RATE_LIMIT_SEARCH", "60/minute")
    RATE_LIMIT_GENERAL: str = os.getenv("RATE_LIMIT_GENERAL", "200/minute")

    def validate(self):
        if not self.JWT_SECRET:
            raise ValueError(
                "JWT_SECRET environment variable is not set. "
                "Please add it to your .env file."
            )
        if not self.DATABASE_URL:
            raise ValueError(
                "DATABASE_URL environment variable is not set. "
                "Please add it to your .env file."
            )
        if self.ENVIRONMENT == "production":
            if self.DEBUG_MODE:
                raise ValueError("DEBUG_MODE must be false in production.")
            if len(self.JWT_SECRET) < 32:
                raise ValueError("JWT_SECRET must be at least 32 characters in production.")
            if not self.CORS_ORIGINS:
                raise ValueError("CORS_ORIGINS must list trusted origins in production.")


settings = Settings()
