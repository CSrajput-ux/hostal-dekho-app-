"""
Distributed Cache & In-Memory Fallback Module for HostelDekho.
Supports Redis for multi-worker / multi-container production scaling,
with seamless in-memory TTL cache fallback for development and testing.
"""
import json
import logging
import time
import threading
from typing import Any, Optional
from .config import settings

logger = logging.getLogger("hosteldekho.cache")

# Global Redis client instance
_redis_client = None
_redis_available = False
_lock = threading.Lock()

# Thread-safe in-memory cache for fallback: {key: (value_json, expiry_timestamp)}
_memory_cache: dict[str, tuple[str, float]] = {}
_memory_lock = threading.Lock()


def _init_redis():
    global _redis_client, _redis_available
    if not settings.REDIS_URL:
        _redis_available = False
        return

    try:
        import redis
        _redis_client = redis.Redis.from_url(
            settings.REDIS_URL,
            decode_responses=True,
            socket_timeout=2.0,
            socket_connect_timeout=2.0,
            retry_on_timeout=True,
            health_check_interval=30,
        )
        _redis_client.ping()
        _redis_available = True
        logger.info("[CACHE] Successfully connected to Redis at %s", settings.REDIS_URL)
    except Exception as exc:
        _redis_available = False
        logger.warning("[CACHE] Redis connection failed (%s). Falling back to in-memory cache.", exc)


# Initialize on import
_init_redis()


class CacheService:
    @staticmethod
    def is_redis_active() -> bool:
        global _redis_available
        if _redis_client and _redis_available:
            try:
                _redis_client.ping()
                return True
            except Exception:
                _redis_available = False
        return False

    @staticmethod
    def get(key: str) -> Optional[Any]:
        if CacheService.is_redis_active():
            try:
                val = _redis_client.get(key)
                if val is not None:
                    return json.loads(val)
            except Exception as exc:
                logger.debug("Redis GET error on key %s: %s", key, exc)

        # Fallback to in-memory
        with _memory_lock:
            entry = _memory_cache.get(key)
            if entry:
                val_json, expires_at = entry
                if time.time() < expires_at:
                    return json.loads(val_json)
                else:
                    _memory_cache.pop(key, None)
        return None

    @staticmethod
    def set(key: str, value: Any, expire_seconds: Optional[int] = None) -> bool:
        ttl = expire_seconds or settings.CACHE_TTL_SECONDS
        val_json = json.dumps(value, default=str)

        if CacheService.is_redis_active():
            try:
                _redis_client.set(key, val_json, ex=ttl)
                return True
            except Exception as exc:
                logger.debug("Redis SET error on key %s: %s", key, exc)

        # Fallback to in-memory
        with _memory_lock:
            _memory_cache[key] = (val_json, time.time() + ttl)
            # Evict expired keys if dictionary grows large
            if len(_memory_cache) > 2000:
                now = time.time()
                keys_to_del = [k for k, (_, exp) in _memory_cache.items() if exp < now]
                for k in keys_to_del:
                    _memory_cache.pop(k, None)
        return True

    @staticmethod
    def delete(key: str) -> bool:
        if CacheService.is_redis_active():
            try:
                _redis_client.delete(key)
            except Exception:
                pass

        with _memory_lock:
            _memory_cache.pop(key, None)
        return True

    @staticmethod
    def delete_pattern(pattern: str) -> int:
        count = 0
        if CacheService.is_redis_active():
            try:
                keys = _redis_client.keys(pattern)
                if keys:
                    count += _redis_client.delete(*keys)
            except Exception as exc:
                logger.debug("Redis delete_pattern error: %s", exc)

        with _memory_lock:
            # Pattern matching for memory cache (simple prefix or suffix wildcard)
            prefix = pattern.rstrip("*")
            keys_to_del = [k for k in _memory_cache if k.startswith(prefix)]
            for k in keys_to_del:
                _memory_cache.pop(k, None)
                count += 1
        return count

    # ─────────────────────────────────────────────
    # Distributed OTP Management
    # ─────────────────────────────────────────────
    @staticmethod
    def store_otp(mobile: str, otp: str, expire_seconds: Optional[int] = None) -> bool:
        key = f"otp:{mobile}"
        ttl = expire_seconds or settings.OTP_EXPIRE_SECONDS
        return CacheService.set(key, {"otp": otp}, expire_seconds=ttl)

    @staticmethod
    def get_otp(mobile: str) -> Optional[str]:
        data = CacheService.get(f"otp:{mobile}")
        if data and isinstance(data, dict):
            return data.get("otp")
        return None

    @staticmethod
    def verify_and_delete_otp(mobile: str, code: str) -> bool:
        key = f"otp:{mobile}"
        data = CacheService.get(key)
        if not data or not isinstance(data, dict):
            return False
        if data.get("otp") == code:
            CacheService.delete(key)
            return True
        return False

    # ─────────────────────────────────────────────
    # Cache Invalidation Helpers
    # ─────────────────────────────────────────────
    @staticmethod
    def invalidate_properties_cache(property_id: Optional[str] = None):
        """Invalidate search, nearby, and detail caches upon property mutation."""
        CacheService.delete_pattern("props:search:*")
        CacheService.delete_pattern("props:nearby:*")
        if property_id:
            CacheService.delete(f"props:detail:{property_id}")


cache = CacheService()
