# pyrefly: ignore [missing-import]
from fastapi import FastAPI, Depends, Request
from fastapi.responses import JSONResponse
# pyrefly: ignore [missing-import]
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from sqlalchemy import func
from decimal import Decimal
from .api.v1.endpoints import properties, auth, bookings, payments, kyc, support, bank, wishlists
from .api.deps import get_db, get_current_user, require_roles
from .database import engine
from . import models, schemas
from .core.firebase import init_firebase
from .core.config import settings

# Fail fast when production configuration is unsafe. Development retains the
# convenient local defaults used by the Android emulator and test suite.
if settings.ENVIRONMENT == "production":
    settings.validate()

# BUG-26 FIX: Firebase init is now wrapped in try/except — will not crash if file missing
init_firebase()

# Create/update database tables (wrapped to prevent crash if DB is temporarily unavailable)
try:
    models.Base.metadata.create_all(bind=engine)
    print("[OK] Database tables created/verified successfully.")
    # Auto-migrate: add any missing columns across all tables in existing DB
    from sqlalchemy import inspect, text
    inspector = inspect(engine)
    with engine.begin() as conn:
        for table_name, table in models.Base.metadata.tables.items():
            if inspector.has_table(table_name):
                existing_cols = {col["name"] for col in inspector.get_columns(table_name)}
                for col in table.columns:
                    if col.name not in existing_cols:
                        col_type = "VARCHAR"
                        col_type_str = str(col.type).upper()
                        if "INT" in col_type_str:
                            col_type = "INTEGER"
                        elif "BOOL" in col_type_str:
                            col_type = "BOOLEAN"
                        elif "FLOAT" in col_type_str or "NUMERIC" in col_type_str or "DECIMAL" in col_type_str:
                            col_type = "FLOAT"
                        elif "DATE" in col_type_str or "TIME" in col_type_str:
                            col_type = "TIMESTAMP"
                        elif "JSON" in col_type_str:
                            col_type = "JSON"
                        try:
                            conn.execute(text(f"ALTER TABLE {table_name} ADD COLUMN {col.name} {col_type}"))
                            print(f"[MIGRATION] Added missing column '{col.name}' ({col_type}) to '{table_name}'.")
                        except Exception:
                            pass
except Exception as e:
    print(f"[WARNING] Could not create DB tables at startup: {e}")
    print("          Server will still start — DB will retry on first request.")


app = FastAPI(
    title="HostelDekho API",
    description="Backend API for HostelDekho — Find hostels, PGs, and flats near you.",
    version="1.0.0",
    docs_url="/docs" if settings.ENVIRONMENT != "production" else None,
    redoc_url="/redoc" if settings.ENVIRONMENT != "production" else None,
)


@app.exception_handler(Exception)
async def unhandled_exception_handler(_: Request, exc: Exception):
    # Do not expose stack traces or provider/database internals to clients.
    import logging
    logging.getLogger("hosteldekho").exception("Unhandled API exception", exc_info=exc)
    return JSONResponse(status_code=500, content={"detail": "Internal server error."})

# CORS — restrict origins in production
app.add_middleware(
    CORSMiddleware,
    # Native Android requests do not send an Origin. Browser access is limited
    # to explicit origins; development has no browser CORS requirement.
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# API Routers — all prefixed with /api/v1/
# BUG-05 FIX: properties router registered at /api/v1/properties
#             so Android can call /api/v1/properties/nearby and /api/v1/properties/search
app.include_router(auth.router,       prefix="/api/v1/auth",       tags=["Authentication"])
app.include_router(auth.router,       prefix="/api/auth",          tags=["Authentication (Legacy)"])
app.include_router(auth.router,       prefix="/api/v1",            tags=["Authentication (Direct)"])
app.include_router(auth.router,       prefix="/api",               tags=["Authentication (Direct Legacy)"])
app.include_router(properties.router, prefix="/api/v1/properties", tags=["Properties"])
app.include_router(bookings.router,   prefix="/api/v1/bookings",   tags=["Bookings"])
app.include_router(payments.router,   prefix="/api/v1/payments",   tags=["Payments"])
app.include_router(kyc.router,        prefix="/api/v1/kyc",        tags=["KYC"])
app.include_router(support.router,    prefix="/api/v1/support",    tags=["Support"])
app.include_router(bank.router,       prefix="/api/v1/bank",       tags=["Bank Accounts"])
app.include_router(wishlists.router,  prefix="/api/v1/wishlists",  tags=["Wishlists"])


@app.get("/", tags=["Health"])
def read_root():
    return {"message": "Welcome to HostelDekho API", "version": "1.0.0", "status": "running"}


@app.get("/api/health", tags=["Health"])
def health_check():
    return {"status": "healthy", "debug_mode": settings.DEBUG_MODE}


@app.post("/logout", tags=["Authentication"])
@app.get("/logout", tags=["Authentication"])
@app.delete("/logout", tags=["Authentication"])
@app.post("/signout", tags=["Authentication"])
@app.get("/signout", tags=["Authentication"])
@app.delete("/signout", tags=["Authentication"])
def root_logout():
    return {
        "success": True,
        "status": "success",
        "message": "Successfully logged out.",
        "detail": "Successfully logged out.",
    }


@app.get("/me", response_model=schemas.UserRead, tags=["Authentication"])
@app.get("/profile", response_model=schemas.UserRead, tags=["Authentication"])
def root_get_me(current_user: models.User = Depends(get_current_user)):
    from .api.v1.endpoints.auth import _build_user_read
    return _build_user_read(current_user)


@app.get("/api/v1/owner/stats", response_model=schemas.OwnerStatsResponse, tags=["Owner"])
def get_owner_stats(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(models.UserRole.OWNER, models.UserRole.ADMIN)),
):
    """
    Return aggregated stats for the owner dashboard:
    total_properties, active_properties, total_bookings, active_bookings,
    total_earnings, monthly_earnings.
    """
    from datetime import datetime, timedelta

    # Get owner's properties
    all_properties = db.query(models.Property).filter(
        models.Property.owner_id == current_user.id
    ).all()
    total_properties = len(all_properties)
    active_properties = sum(1 for p in all_properties if p.is_active)

    # Get all property IDs
    prop_ids = [p.id for p in all_properties]

    if not prop_ids:
        return schemas.OwnerStatsResponse(
            total_properties=0,
            active_properties=0,
            total_bookings=0,
            active_bookings=0,
            total_earnings=Decimal("0"),
            monthly_earnings=Decimal("0"),
        )

    # Bookings stats
    all_bookings = db.query(models.Booking).filter(
        models.Booking.property_id.in_(prop_ids)
    ).all()
    total_bookings = len(all_bookings)
    active_bookings = sum(
        1 for b in all_bookings if b.status == models.BookingStatus.ACTIVE
    )

    # Earnings
    total_earnings = sum(
        (b.amount_paid or Decimal("0")) for b in all_bookings
        if b.status in [models.BookingStatus.ACTIVE, models.BookingStatus.COMPLETED]
    )

    # Monthly earnings (current month)
    now = datetime.utcnow()
    month_start = now.replace(day=1, hour=0, minute=0, second=0, microsecond=0)
    monthly_earnings = sum(
        (b.amount_paid or Decimal("0")) for b in all_bookings
        if b.status in [models.BookingStatus.ACTIVE, models.BookingStatus.COMPLETED]
        and b.created_at and b.created_at >= month_start
    )

    return schemas.OwnerStatsResponse(
        total_properties=total_properties,
        active_properties=active_properties,
        total_bookings=total_bookings,
        active_bookings=active_bookings,
        total_earnings=Decimal(str(total_earnings)),
        monthly_earnings=Decimal(str(monthly_earnings)),
    )
