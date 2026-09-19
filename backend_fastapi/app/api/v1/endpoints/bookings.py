"""
Bookings endpoints.

Endpoints:
  POST /       — Create a booking (student, auth required)
  GET  /my     — Current user's booking history (student, auth required)
  GET  /owner  — All bookings for owner's properties (owner, auth required)
"""
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from .... import models, schemas
from ....api.deps import get_db, get_current_user, require_roles
from ....core.services import create_razorpay_order

router = APIRouter()


# ─────────────────────────────────────────────────────────────────────────────
# POST /  — Create a booking + Razorpay order
# ─────────────────────────────────────────────────────────────────────────────
@router.post("/", response_model=schemas.BookingResponse)
def create_booking(
    request: schemas.BookingCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(models.UserRole.STUDENT)),
):
    """
    Create a booking and a Razorpay order for payment.
    Returns razorpay_order_id which the Android app uses to launch Razorpay checkout.
    """
    # Validate room
    room = (
        db.query(models.Room)
        .filter(models.Room.id == request.room_id)
        .first()
    )
    if not room:
        raise HTTPException(status_code=404, detail="Room not found")

    if not room.availability_count or room.availability_count <= 0:
        raise HTTPException(status_code=400, detail="No beds available in this room")

    # Validate property
    property_ = (
        db.query(models.Property)
        .filter(models.Property.id == request.property_id)
        .first()
    )
    if not property_:
        raise HTTPException(status_code=404, detail="Property not found")
    if room.property_id != property_.id:
        raise HTTPException(status_code=400, detail="Room does not belong to the selected property")
    if property_.is_active is False:
        raise HTTPException(status_code=400, detail="Property is not currently accepting bookings")

    # Create Razorpay order
    try:
        order = create_razorpay_order(amount=int(room.price))
    except RuntimeError:
        # Razorpay not configured — use mock order for development
        order = {"id": f"order_dev_{room.id[:8]}"}
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to create payment order: {str(e)}"
        )

    # Parse check_in_date
    check_in = None
    if request.check_in_date:
        try:
            from datetime import date
            check_in = date.fromisoformat(request.check_in_date)
        except Exception:
            check_in = None

    booking = models.Booking(
        user_id=current_user.id,
        property_id=request.property_id,
        room_id=request.room_id,
        status=models.BookingStatus.PENDING_PAYMENT,
        razorpay_order_id=order["id"],
        amount_paid=room.price,
        check_in_date=check_in,
        duration_months=request.duration_months or 1,
        # Denormalize customer name for quick owner dashboard display
        customer_name=current_user.name or current_user.username or "Guest",
    )
    try:
        db.add(booking)
        db.commit()
        db.refresh(booking)
    except Exception:
        db.rollback()
        raise

    return schemas.BookingResponse(
        id=booking.id,
        status=booking.status,
        razorpay_order_id=booking.razorpay_order_id,
        amount=booking.amount_paid,
    )


# ─────────────────────────────────────────────────────────────────────────────
# GET /my  — Student's booking history
# ─────────────────────────────────────────────────────────────────────────────
@router.get("/my", response_model=schemas.MyBookingsResponse)
def get_my_bookings(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    """
    Return all bookings made by the currently authenticated student.
    Used by BookingsScreen in the Android app.
    """
    bookings = (
        db.query(models.Booking)
        .filter(models.Booking.user_id == current_user.id)
        .order_by(models.Booking.created_at.desc())
        .all()
    )

    items = []
    for b in bookings:
        property_name = ""
        property_address = ""
        room_type = ""

        if b.property:
            property_name = b.property.name or ""
            property_address = b.property.address or ""
        if b.room:
            room_type = b.room.room_type or ""

        items.append(
            schemas.MyBookingItem(
                id=b.id,
                property_id=b.property_id or "",
                property_name=property_name,
                property_address=property_address,
                room_type=room_type,
                status=b.status,
                amount_paid=b.amount_paid,
                check_in_date=str(b.check_in_date) if b.check_in_date else None,
                duration_months=b.duration_months,
                created_at=str(b.created_at),
            )
        )

    return schemas.MyBookingsResponse(total=len(items), bookings=items)


# ─────────────────────────────────────────────────────────────────────────────
# GET /owner  — All bookings for owner's properties
# ─────────────────────────────────────────────────────────────────────────────
@router.get("/owner", response_model=schemas.OwnerBookingsResponse)
def get_owner_bookings(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(models.UserRole.OWNER, models.UserRole.ADMIN)),
):
    """
    Return all bookings for properties owned by the authenticated user.
    Used by OwnerBookingsScreen and OwnerDashboardScreen.
    """
    # Get all property IDs owned by this user
    owner_property_ids = [
        p.id for p in db.query(models.Property.id)
        .filter(models.Property.owner_id == current_user.id)
        .all()
    ]

    if not owner_property_ids:
        return schemas.OwnerBookingsResponse(total=0, bookings=[])

    bookings = (
        db.query(models.Booking)
        .filter(models.Booking.property_id.in_(owner_property_ids))
        .order_by(models.Booking.created_at.desc())
        .all()
    )

    items = []
    for b in bookings:
        property_name = b.property.name if b.property else ""
        room_type = b.room.room_type if b.room else ""
        # Get customer info from related user
        customer_name = b.customer_name or ""
        customer_mobile = None
        if b.user:
            customer_name = customer_name or b.user.name or b.user.username or "Guest"
            customer_mobile = b.user.mobile

        items.append(
            schemas.OwnerBookingItem(
                id=b.id,
                customer_name=customer_name,
                customer_mobile=customer_mobile,
                property_name=property_name,
                room_type=room_type,
                status=b.status,
                amount_paid=b.amount_paid,
                check_in_date=str(b.check_in_date) if b.check_in_date else None,
                duration_months=b.duration_months,
                created_at=str(b.created_at),
            )
        )

    return schemas.OwnerBookingsResponse(total=len(items), bookings=items)
