"""
Payments endpoint.

BUG-15 FIX: Replaced individual Body(...) params with a proper Pydantic schema.
             Android sends a JSON body — individual Body() params require embed=True.
BUG-17 FIX: Use models.BookingStatus.COMPLETED instead of raw string "Confirmed".
"""
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from .... import models, schemas
from ....api.deps import get_db, get_current_user
from ....core.services import verify_razorpay_signature

router = APIRouter()


@router.post("/verify", response_model=schemas.PaymentVerifyResponse)
def verify_payment(
    request: schemas.PaymentVerifyRequest,   # BUG-15 FIX: Pydantic model, not Body(...)
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    """
    Verify a Razorpay payment signature and mark the booking as COMPLETED.
    Android sends: {razorpay_order_id, razorpay_payment_id, razorpay_signature}
    """
    # Verify signature
    is_valid = verify_razorpay_signature(
        request.razorpay_order_id,
        request.razorpay_payment_id,
        request.razorpay_signature,
    )
    if not is_valid:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid payment signature. Payment could not be verified.",
        )

    # Find the booking
    booking = (
        db.query(models.Booking)
        .filter(models.Booking.razorpay_order_id == request.razorpay_order_id)
        .first()
    )
    if not booking:
        raise HTTPException(status_code=404, detail="Booking not found for this order ID")
    if booking.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="You cannot verify another user's payment")
    if booking.status != models.BookingStatus.PENDING_PAYMENT:
        raise HTTPException(status_code=400, detail="This booking has already been processed")

    # BUG-17 FIX: Use enum value, not raw string "Confirmed"
    booking.status = models.BookingStatus.COMPLETED
    booking.razorpay_payment_id = request.razorpay_payment_id

    # Reduce room availability count
    room = (
        db.query(models.Room)
        .filter(models.Room.id == booking.room_id)
        .first()
    )
    if room and room.availability_count and room.availability_count > 0:
        room.availability_count -= 1

    try:
        db.commit()
    except Exception:
        db.rollback()
        raise

    return schemas.PaymentVerifyResponse(
        success=True,
        message="Payment verified successfully. Booking is now confirmed.",
    )
