from typing import Generator
from fastapi import Depends, HTTPException, status  # pyrefly: ignore  # type: ignore
from fastapi.security import OAuth2PasswordBearer  # pyrefly: ignore  # type: ignore
from jose import jwt, JWTError  # pyrefly: ignore  # type: ignore
from sqlalchemy.orm import Session  # pyrefly: ignore  # type: ignore
from ..database import SessionLocal
from ..core.config import settings
from .. import models

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/v1/auth/firebase")


def get_db() -> Generator:
    """
    Yield a SQLAlchemy database session and ensure it is closed after use.
    BUG-02 FIX: This is the SINGLE source of get_db. Removed from database.py.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


async def get_current_user(
    db: Session = Depends(get_db), token: str = Depends(oauth2_scheme)
) -> models.User:
    """
    Decode the JWT Bearer token and return the authenticated user.
    BUG-01 FIX: Use settings.ALGORITHM directly — ALGORITHM is not exported from security.py.
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        # BUG-01 FIX: was `from ..core.security import ALGORITHM` — that doesn't exist
        payload = jwt.decode(
            token, settings.JWT_SECRET, algorithms=[settings.ALGORITHM]
        )
        user_id: str = payload.get("sub")
        token_type: str = payload.get("type", "access")
        if user_id is None or token_type != "access":
            raise credentials_exception
    except JWTError:
        raise credentials_exception

    user = db.query(models.User).filter(models.User.id == user_id).first()
    if user is None:
        raise credentials_exception
    return user


def require_roles(*roles: models.UserRole):
    """Return a dependency that limits an endpoint to the supplied roles."""
    allowed = {role.value for role in roles}

    async def role_guard(current_user: models.User = Depends(get_current_user)) -> models.User:
        role = current_user.role.value if hasattr(current_user.role, "value") else str(current_user.role)
        if role.upper() not in allowed:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="You do not have permission to perform this action.",
            )
        return current_user

    return role_guard
