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
