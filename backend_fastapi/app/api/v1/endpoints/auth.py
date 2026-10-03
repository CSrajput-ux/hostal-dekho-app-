"""
Auth endpoints for HostelDekho API.

Endpoints:
  POST /send-otp      — Send OTP to mobile number
  POST /verify-otp    — Verify OTP and return JWT tokens
  POST /refresh       — Refresh access token using refresh token
  POST /firebase      — Login/register via Firebase ID token (Phone Auth & Google Sign-In)
"""
import random
import time
import logging
from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException, Request, status
from sqlalchemy.orm import Session
from .... import models, schemas
from ....core.firebase import verify_firebase_token
from ....core.security import create_access_token, create_refresh_token, get_password_hash, verify_password
from ....api.deps import get_db, get_current_user
from ....core.config import settings
from ....core.cache import cache
from ....core.rate_limit import limiter
from jose import jwt, JWTError

logger = logging.getLogger("auth_endpoint")
router = APIRouter()

# ─────────────────────────────────────────────────────────────────────────────
# Distributed OTP Store (Redis in production, thread-safe memory fallback)
# ─────────────────────────────────────────────────────────────────────────────

def _generate_otp() -> str:
    return str(random.randint(100000, 999999))


def _store_otp(mobile: str, otp: str):
    cache.store_otp(mobile, otp, settings.OTP_EXPIRE_SECONDS)


def _verify_otp_code(mobile: str, code: str) -> bool:
    return cache.verify_and_delete_otp(mobile, code)



def _build_user_read(user: models.User) -> schemas.UserRead:
    role_str = (user.role.value if hasattr(user.role, "value") else str(user.role)).upper()
    kyc_str = (user.kyc_status.value if hasattr(user.kyc_status, "value") else str(user.kyc_status)).upper()
    return schemas.UserRead(
        id=user.id,
        firebase_uid=user.firebase_uid,
        username=getattr(user, "username", None) or "",
        name=user.name or getattr(user, "username", None) or "HostelDekho User",
        email=user.email or "",
        mobile=user.mobile or "",
        role=role_str,
        profile_image=user.profile_image or "",
        auth_provider=user.auth_provider or "phone",
        kyc_status=kyc_str,
        created_at=str(user.created_at),
        last_login_at=str(user.last_login_at) if user.last_login_at else None,
    )


def _build_auth_response(user: models.User) -> schemas.AuthResponse:
    access_token = create_access_token(subject=user.id)
    refresh_token = create_refresh_token(subject=user.id)
    return schemas.AuthResponse(
        access_token=access_token,
        refresh_token=refresh_token,
        token_type="bearer",
        user=_build_user_read(user),
    )


# ─────────────────────────────────────────────────────────────────────────────
# POST /send-otp
# ─────────────────────────────────────────────────────────────────────────────
@router.post("/send-otp", response_model=schemas.SendOtpResponse)
@limiter.limit(settings.RATE_LIMIT_AUTH)
def send_otp(request: Request, body: schemas.SendOtpRequest):
    """
    Send a 6-digit OTP to the given mobile number.
    In DEBUG_MODE the OTP is returned directly in the response for testing.
    Rate-limited to prevent SMS abuse.
    """
    mobile = body.mobile.strip()
    if len(mobile) != 10 or not mobile.isdigit():
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Mobile must be a 10-digit Indian number (without +91).",
        )

    otp = _generate_otp()
    _store_otp(mobile, otp)

    response = schemas.SendOtpResponse(
        detail=f"OTP sent to +91{mobile}",
        expires_in_seconds=settings.OTP_EXPIRE_SECONDS,
    )

    if settings.DEBUG_MODE:
        response.debug_otp = otp

    return response


# ─────────────────────────────────────────────────────────────────────────────
# POST /verify-otp
# ─────────────────────────────────────────────────────────────────────────────
@router.post("/verify-otp", response_model=schemas.AuthResponse)
@limiter.limit(settings.RATE_LIMIT_AUTH)
def verify_otp(request: Request, body: schemas.VerifyOtpRequest, db: Session = Depends(get_db)):
    """
    Verify the OTP for the given mobile number.
    Creates a new user if one does not exist.
    Returns access_token + refresh_token on success.
    """
    if not _verify_otp_code(body.mobile, body.code):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired OTP. Please request a new one.",
        )

    user = db.query(models.User).filter(models.User.mobile == body.mobile).first()
    now = datetime.utcnow()
    if not user:
        user = models.User(
            mobile=body.mobile,
            name=body.name or "HostelDekho User",
            email=body.email,
            role=models.UserRole.STUDENT,
            auth_provider="phone",
            last_login_at=now,
        )
        db.add(user)
        db.commit()
        db.refresh(user)
    else:
        user.last_login_at = now
        changed = True
        if body.name and not user.name:
            user.name = body.name
        if body.email and not user.email:
            user.email = body.email
        db.commit()
        db.refresh(user)

    return _build_auth_response(user)


# ─────────────────────────────────────────────────────────────────────────────
# POST /refresh
# ─────────────────────────────────────────────────────────────────────────────
@router.post("/refresh", response_model=schemas.AuthResponse)
def refresh_token(request: schemas.RefreshRequest, db: Session = Depends(get_db)):
    """
    Accept a valid refresh token and issue a new access + refresh token pair.
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Invalid or expired refresh token.",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(
            request.refresh_token, settings.JWT_SECRET, algorithms=[settings.ALGORITHM]
        )
        user_id: str = payload.get("sub")
        token_type: str = payload.get("type", "")
        if user_id is None or token_type != "refresh":
            raise credentials_exception
    except JWTError:
        raise credentials_exception

    user = db.query(models.User).filter(models.User.id == user_id).first()
    if user is None:
        raise credentials_exception

    return _build_auth_response(user)


# ─────────────────────────────────────────────────────────────────────────────
# POST /firebase
# ─────────────────────────────────────────────────────────────────────────────
@router.post("/firebase", response_model=schemas.AuthResponse)
def firebase_login(request: schemas.GoogleAuthRequest, db: Session = Depends(get_db)):
    """
    Verify a Firebase ID Token (Google Sign-In or Phone Auth).
    Upserts user record setting firebase_uid, name, email, profile_image, auth_provider, and last_login_at.
    Returns custom JWT access + refresh tokens.
    """
    decoded_token = verify_firebase_token(request.id_token)
    if not decoded_token:
        logger.error("Failed to decode Firebase token.")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid Firebase token. Ensure Firebase is configured on the server.",
        )

    uid = decoded_token.get("uid")
    email = decoded_token.get("email")
    phone_number = decoded_token.get("phone_number")
    name = decoded_token.get("name", "HostelDekho User")
    picture = decoded_token.get("picture")
    firebase_auth_provider = decoded_token.get("firebase", {}).get("sign_in_provider", "google")

    logger.info(f"Verified Firebase Auth Token: UID={uid}, email={email}, provider={firebase_auth_provider}")

    now = datetime.utcnow()
    user = None

    # 1. Search by firebase_uid
    if uid:
        user = db.query(models.User).filter(models.User.firebase_uid == uid).first()

    # 2. Search by email if not found
    if not user and email:
        user = db.query(models.User).filter(models.User.email == email).first()

    # 3. Search by mobile if phone_number present
    mobile = None
    if not user and phone_number:
        mobile = phone_number.replace("+91", "").strip()
        if mobile.startswith("+"):
            mobile = mobile[3:]
        user = db.query(models.User).filter(models.User.mobile == mobile).first()

    if not user:
        if not mobile and phone_number:
            mobile = phone_number.replace("+91", "").strip()
        user = models.User(
            firebase_uid=uid,
            name=name,
            email=email,
            mobile=mobile,
            profile_image=picture,
            auth_provider="google" if "google" in firebase_auth_provider else "phone",
            role=models.UserRole.STUDENT,
            last_login_at=now,
        )
        db.add(user)
        db.commit()
        db.refresh(user)
        logger.info(f"Created new user via Firebase Auth: ID={user.id}, UID={uid}")
    else:
        # Update existing user attributes
        user.firebase_uid = uid
        user.last_login_at = now
        if name and not user.name:
            user.name = name
        if email and not user.email:
            user.email = email
        if picture:
            user.profile_image = picture
        if "google" in firebase_auth_provider:
            user.auth_provider = "google"
        db.commit()
        db.refresh(user)
        logger.info(f"Updated existing user login via Firebase Auth: ID={user.id}")

    return _build_auth_response(user)


# ─────────────────────────────────────────────────────────────────────────────
# POST /register (also /signup and /create-account)
# ─────────────────────────────────────────────────────────────────────────────
@router.post("/register", response_model=schemas.AuthResponse)
@router.post("/signup", response_model=schemas.AuthResponse)
@router.post("/create-account", response_model=schemas.AuthResponse)
@limiter.limit(settings.RATE_LIMIT_AUTH)
def register_user(request: Request, body: schemas.RegisterRequest, db: Session = Depends(get_db)):
    """
    Register a new user with username, email, password, and name.
    Supports /register, /signup, and /create-account paths.
    """
    now = datetime.utcnow()
    clean_username = body.username.strip().lstrip("@").strip() if body.username else None
    clean_email = body.email.strip().lower() if body.email else None
    clean_mobile = body.mobile.strip() if body.mobile else None

    if clean_email:
        existing_email = db.query(models.User).filter(models.User.email == clean_email).first()
        if existing_email:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="An account with this email address already exists.",
            )

    if clean_username:
        existing_username = db.query(models.User).filter(models.User.username == clean_username).first()
        if existing_username:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="This username is already taken. Please choose another.",
            )

    if clean_mobile:
        existing_mobile = db.query(models.User).filter(models.User.mobile == clean_mobile).first()
        if existing_mobile:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="An account with this mobile number already exists.",
            )

    hashed_pw = get_password_hash(body.password) if body.password else None
    user_name = body.name or body.real_name or clean_username or "HostelDekho User"

    user_role = models.UserRole.STUDENT
    if body.role:
        for role_enum in models.UserRole:
            if role_enum.value.lower() == body.role.lower():
                user_role = role_enum
                break

    user = models.User(
        username=clean_username,
        name=user_name,
        email=clean_email,
        mobile=clean_mobile,
        hashed_password=hashed_pw,
        role=user_role,
        auth_provider="email",
        last_login_at=now,
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    logger.info(f"Registered new user via Email/Password: ID={user.id}, Username={clean_username}, Email={clean_email}")

    return _build_auth_response(user)


# ─────────────────────────────────────────────────────────────────────────────
# POST /login (also /signin and /token)
# ─────────────────────────────────────────────────────────────────────────────
@router.post("/login", response_model=schemas.AuthResponse)
@router.post("/signin", response_model=schemas.AuthResponse)
@router.post("/token", response_model=schemas.AuthResponse)
@limiter.limit(settings.RATE_LIMIT_AUTH)
def login_user(request: Request, body: schemas.LoginRequest, db: Session = Depends(get_db)):
    """
    Login user using username/email/mobile and password.
    Supports /login, /signin, and /token paths.
    """
    # FIX: Android sends `login_id` as the generic identifier field
    identifier = (body.username or body.email or body.login_id or "").strip().lstrip("@").strip()
    if not identifier:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Please provide username or email.",
        )

    user = None
    user = db.query(models.User).filter(models.User.username == identifier).first()
    if not user:
        user = db.query(models.User).filter(models.User.email == identifier.lower()).first()
    if not user and identifier.isdigit():
        user = db.query(models.User).filter(models.User.mobile == identifier).first()

    if not user or not user.hashed_password:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username/email or password.",
        )

    if not verify_password(body.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username/email or password.",
        )

    user.last_login_at = datetime.utcnow()
    db.commit()
    db.refresh(user)
    logger.info(f"User logged in via Password: ID={user.id}, Username={user.username}")

    return _build_auth_response(user)


# ─────────────────────────────────────────────────────────────────────────────
# POST / GET / DELETE /logout (and /signout)
# ─────────────────────────────────────────────────────────────────────────────
@router.post("/logout", status_code=status.HTTP_200_OK)
@router.get("/logout", status_code=status.HTTP_200_OK)
@router.delete("/logout", status_code=status.HTTP_200_OK)
@router.post("/signout", status_code=status.HTTP_200_OK)
@router.get("/signout", status_code=status.HTTP_200_OK)
@router.delete("/signout", status_code=status.HTTP_200_OK)
@router.post("/user/logout", status_code=status.HTTP_200_OK)
@router.get("/user/logout", status_code=status.HTTP_200_OK)
@router.post("/users/logout", status_code=status.HTTP_200_OK)
@router.get("/users/logout", status_code=status.HTTP_200_OK)
def logout_user():
    """
    Logout endpoint.
    Since JWT tokens are stateless on the backend, this returns 200 OK
    so the client can clear its stored tokens and navigate to the login screen.
    """
    return {
        "success": True,
        "status": "success",
        "message": "Successfully logged out.",
        "detail": "Successfully logged out."
    }


# ─────────────────────────────────────────────────────────────────────────────
# GET /me  — Currently logged in user profile details
# ─────────────────────────────────────────────────────────────────────────────
@router.get("/me", response_model=schemas.UserRead)
@router.get("/profile", response_model=schemas.UserRead)
@router.get("/user/me", response_model=schemas.UserRead)
@router.get("/users/me", response_model=schemas.UserRead)
def get_me(
    current_user: models.User = Depends(get_current_user),
):
    """
    Return the details of the currently authenticated user based on their Bearer token.
    Supports GET /api/v1/auth/me, /api/auth/me, /api/v1/me, etc.
    """
    return _build_user_read(current_user)


# ─────────────────────────────────────────────────────────────────────────────
# PUT / PATCH /me  — Update profile details of currently logged in user
# ─────────────────────────────────────────────────────────────────────────────
@router.put("/me", response_model=schemas.UserRead)
@router.patch("/me", response_model=schemas.UserRead)
@router.put("/profile", response_model=schemas.UserRead)
@router.patch("/profile", response_model=schemas.UserRead)
def update_me(
    request: schemas.UserUpdate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    """
    Update profile details for the currently logged in user.
    """
    if request.name is not None:
        current_user.name = request.name
    if request.email is not None:
        current_user.email = request.email
    if request.mobile is not None:
        current_user.mobile = request.mobile
    if request.profile_image is not None:
        current_user.profile_image = request.profile_image

    db.commit()
    db.refresh(current_user)
    return _build_user_read(current_user)
