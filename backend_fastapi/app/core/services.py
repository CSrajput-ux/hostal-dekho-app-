import cloudinary
import cloudinary.uploader
from .config import settings


def _get_razorpay_client():
    """
    BUG-18 FIX: Lazy initialization — only create client when first needed.
    Prevents crash at import time when RAZORPAY_KEY_ID is not set.
    """
    import razorpay
    if not settings.RAZORPAY_KEY_ID or not settings.RAZORPAY_KEY_SECRET:
        raise RuntimeError(
            "Razorpay keys not configured. Set RAZORPAY_KEY_ID and "
            "RAZORPAY_KEY_SECRET in your .env file."
        )
    return razorpay.Client(auth=(settings.RAZORPAY_KEY_ID, settings.RAZORPAY_KEY_SECRET))


def _configure_cloudinary():
    """
    BUG-18 FIX: Lazy Cloudinary config — called only when upload is needed.
    """
    if not settings.CLOUDINARY_CLOUD_NAME:
        raise RuntimeError(
            "Cloudinary not configured. Set CLOUDINARY_CLOUD_NAME, "
            "CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET in your .env file."
        )
    cloudinary.config(
        cloud_name=settings.CLOUDINARY_CLOUD_NAME,
        api_key=settings.CLOUDINARY_API_KEY,
        api_secret=settings.CLOUDINARY_API_SECRET,
    )


def create_razorpay_order(amount: int, currency: str = "INR") -> dict:
    """Create a Razorpay order. Amount is in rupees (converted to paise internally)."""
    client = _get_razorpay_client()
    data = {
        "amount": amount * 100,   # Convert rupees → paise
        "currency": currency,
        "payment_capture": 1,
    }
    return client.order.create(data=data)


def verify_razorpay_signature(
    razorpay_order_id: str,
    razorpay_payment_id: str,
    razorpay_signature: str
) -> bool:
    """Verify Razorpay payment signature. Returns True if valid."""
    try:
        client = _get_razorpay_client()
        client.utility.verify_payment_signature({
            "razorpay_order_id": razorpay_order_id,
            "razorpay_payment_id": razorpay_payment_id,
            "razorpay_signature": razorpay_signature,
        })
        return True
    except Exception:
        return False


def upload_to_cloudinary(file, folder: str = "hosteldekho") -> str:
    """Upload a file to Cloudinary and return the secure URL."""
    _configure_cloudinary()
    result = cloudinary.uploader.upload(file, folder=folder)
    return result.get("secure_url", "")
