import logging
import os
import firebase_admin
from firebase_admin import credentials, auth
from .config import settings

logger = logging.getLogger("firebase_auth")


def init_firebase():
    """
    Initialize Firebase Admin SDK using service account certificate.
    """
    if firebase_admin._apps:
        return  # Already initialized

    service_account_path = settings.FIREBASE_SERVICE_ACCOUNT
    if os.path.exists(service_account_path):
        try:
            cred = credentials.Certificate(service_account_path)
            firebase_admin.initialize_app(cred)
            logger.info("Firebase Admin SDK initialized successfully.")
        except Exception as e:
            logger.error(f"Firebase Admin SDK initialization failed: {e}")
    else:
        logger.warning(
            f"Firebase service account file '{service_account_path}' not found. "
            "Server token verification will fail unless FIREBASE_SERVICE_ACCOUNT is set in .env"
        )


def verify_firebase_token(id_token: str):
    """
    Verify a Firebase ID token (Phone Auth or Google Sign-In).
    Returns decoded token dictionary or None if invalid.
    """
    if not firebase_admin._apps:
        init_firebase()

    if not firebase_admin._apps:
        logger.error("Firebase Admin SDK is not initialized.")
        return None

    try:
        decoded_token = auth.verify_id_token(id_token)
        logger.info(f"Firebase Token verified for UID: {decoded_token.get('uid')}")
        return decoded_token
    except Exception as e:
        logger.error(f"Firebase token verification failed: {e}")
        return None
