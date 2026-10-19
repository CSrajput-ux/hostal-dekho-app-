#!/usr/bin/env bash
set -e

echo "=================================================="
echo " Starting HostelDekho Backend Production Services "
echo "=================================================="

# Check and run database migrations
if [ "$RUN_MIGRATIONS" != "false" ]; then
    echo "[HostelDekho] Running database migrations via Alembic..."
    if alembic upgrade head; then
        echo "[HostelDekho] ✓ Database migrations applied successfully."
    else
        echo "[HostelDekho] ⚠ Warning: Migrations failed or already applied. Proceeding..."
    fi
fi

# Execute the main container command (Gunicorn / Uvicorn)
echo "[HostelDekho] Launching application server: $@"
exec "$@"
