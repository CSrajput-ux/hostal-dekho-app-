"""
Production Gunicorn Configuration for HostelDekho API.
Optimized for 10k+ concurrent active users and high-throughput ASGI workloads.
"""
import multiprocessing
import os

# Server socket
bind = os.getenv("BIND", "0.0.0.0:8000")
backlog = int(os.getenv("GUNICORN_BACKLOG", "2048"))

# Worker processes
# Standard formula: (2 * CPU cores) + 1. Can be overridden via WEB_CONCURRENCY env var.
cores = multiprocessing.cpu_count()
workers = int(os.getenv("WEB_CONCURRENCY", max(2, cores * 2 + 1)))
worker_class = "uvicorn.workers.UvicornWorker"
worker_connections = int(os.getenv("WORKER_CONNECTIONS", "2048"))

# Worker lifecycle & memory leak prevention
# Automatically restart workers after N requests to prevent memory fragmentation
max_requests = int(os.getenv("GUNICORN_MAX_REQUESTS", "5000"))
max_requests_jitter = int(os.getenv("GUNICORN_MAX_REQUESTS_JITTER", "500"))

# Timeouts
timeout = int(os.getenv("GUNICORN_TIMEOUT", "60"))
keepalive = int(os.getenv("GUNICORN_KEEPALIVE", "5"))
graceful_timeout = int(os.getenv("GUNICORN_GRACEFUL_TIMEOUT", "30"))

# Logging
loglevel = os.getenv("LOG_LEVEL", "info")
accesslog = "-"
errorlog = "-"
access_log_format = '%(h)s %(l)s %(u)s %(t)s "%(r)s" %(s)s %(b)s "%(f)s" "%(a)s" %(D)sµs'

# Process naming
proc_name = "hosteldekho_api"


def on_starting(server):
    server.log.info(
        f"[STARTUP] HostelDekho Gunicorn starting with {workers} workers on {bind}"
    )


def post_worker_init(worker):
    worker.log.info(f"[WORKER] Worker spawned (PID: {worker.pid})")


def worker_int(worker):
    worker.log.info(f"[WORKER] Worker received INT/QUIT signal (PID: {worker.pid})")
