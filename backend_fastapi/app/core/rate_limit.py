"""
Rate Limiting & Abuse Prevention for HostelDekho API.
Powered by SlowAPI with Redis storage or in-memory fallback.
Mitigates DDoS, brute-force OTP attempts, and search scraping.
"""
from slowapi import Limiter
from slowapi.util import get_remote_address
from starlette.requests import Request
from .config import settings


def get_real_client_ip(request: Request) -> str:
    """
    Extract real client IP taking into account reverse proxy headers
    (Cloudflare CF-Connecting-IP, X-Forwarded-For, X-Real-IP).
    """
    cf_ip = request.headers.get("CF-Connecting-IP")
    if cf_ip:
        return cf_ip.strip()

    x_forwarded = request.headers.get("X-Forwarded-For")
    if x_forwarded:
        # First IP in comma-separated chain is the original client
        return x_forwarded.split(",")[0].strip()

    x_real_ip = request.headers.get("X-Real-IP")
    if x_real_ip:
        return x_real_ip.strip()

    return get_remote_address(request)


# Configure storage uri
storage_uri = settings.REDIS_URL if settings.REDIS_URL else "memory://"

limiter = Limiter(
    key_func=get_real_client_ip,
    default_limits=[settings.RATE_LIMIT_GENERAL],
    storage_uri=storage_uri,
    strategy="fixed-window",
)
