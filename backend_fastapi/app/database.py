import os
from sqlalchemy import create_engine, event
from sqlalchemy.orm import declarative_base, sessionmaker
from sqlalchemy.pool import NullPool, QueuePool
from dotenv import load_dotenv
from .core.config import settings

load_dotenv()

SQLALCHEMY_DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./sql_app.db")

if SQLALCHEMY_DATABASE_URL.startswith("sqlite"):
    engine = create_engine(
        SQLALCHEMY_DATABASE_URL,
        connect_args={"check_same_thread": False},
    )

    # Enable WAL mode for high concurrent reads without database lock errors
    @event.listens_for(engine, "connect")
    def set_sqlite_pragma(dbapi_connection, connection_record):
        cursor = dbapi_connection.cursor()
        cursor.execute("PRAGMA journal_mode=WAL")
        cursor.execute("PRAGMA synchronous=NORMAL")
        cursor.close()

else:
    # For PostgreSQL / Production:
    # Use QueuePool for high concurrency (handles 10k users with burst overflow)
    # If explicitly configured (e.g. external PgBouncer in transaction mode), can use NullPool
    if settings.USE_NULLPOOL:
        engine = create_engine(
            SQLALCHEMY_DATABASE_URL,
            poolclass=NullPool,
            pool_pre_ping=True,
        )
    else:
        engine = create_engine(
            SQLALCHEMY_DATABASE_URL,
            poolclass=QueuePool,
            pool_size=settings.DB_POOL_SIZE,
            max_overflow=settings.DB_MAX_OVERFLOW,
            pool_timeout=settings.DB_POOL_TIMEOUT,
            pool_recycle=settings.DB_POOL_RECYCLE,
            pool_pre_ping=True,
        )

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

# NOTE: get_db() lives in api/deps.py — do NOT duplicate it here.
