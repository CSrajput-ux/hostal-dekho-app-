from sqlalchemy import Column, String, Float, Boolean, Integer, ForeignKey, JSON, DateTime, Numeric, Date, Index
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
import uuid
import enum
from .database import Base


class UserRole(str, enum.Enum):
    STUDENT = "STUDENT"
    OWNER = "OWNER"
    ADMIN = "ADMIN"


class KycStatus(str, enum.Enum):
    PENDING = "PENDING"
    UNDER_REVIEW = "UNDER_REVIEW"
    VERIFIED = "VERIFIED"
    REJECTED = "REJECTED"


class PropertyType(str, enum.Enum):
    HOSTEL = "HOSTEL"
    PG = "PG"
    FLAT = "FLAT"


class GenderAllowed(str, enum.Enum):
    BOYS = "BOYS"
    GIRLS = "GIRLS"
    UNISEX = "UNISEX"


class BookingStatus(str, enum.Enum):
    # FIX: Consistent naming — "PENDING_PAYMENT" stored in DB
    PENDING_PAYMENT = "PENDING_PAYMENT"
    ACTIVE = "ACTIVE"
    CANCELLED = "CANCELLED"
    COMPLETED = "COMPLETED"


class User(Base):
    __tablename__ = "users"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    firebase_uid = Column(String, unique=True, index=True, nullable=True)
    username = Column(String, unique=True, index=True, nullable=True)
    hashed_password = Column(String, nullable=True)
    name = Column(String)
    email = Column(String, unique=True, index=True, nullable=True)
    mobile = Column(String, unique=True, index=True, nullable=True)
    role = Column(String, default=UserRole.STUDENT, index=True)
    profile_image = Column(String, nullable=True)
    auth_provider = Column(String, default="phone")  # "google", "phone", "email"
    kyc_status = Column(String, default=KycStatus.PENDING)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), index=True)
    last_login_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    properties = relationship("Property", back_populates="owner")
    bookings = relationship("Booking", back_populates="user")


class Property(Base):
    __tablename__ = "properties"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    owner_id = Column(String, ForeignKey("users.id"), index=True)
    name = Column(String, nullable=False)
    description = Column(String)
    address = Column(String)
    city = Column(String, index=True)
    state = Column(String)
    pincode = Column(String)
    latitude = Column(Float)
    longitude = Column(Float)
    type = Column(String, index=True)  # HOSTEL, PG, FLAT
    # NEW: Gender filter support — BOYS, GIRLS, UNISEX
    gender = Column(String, nullable=True, default=GenderAllowed.UNISEX, index=True)
    verified_badge = Column(Boolean, default=False)
    amenities = Column(JSON)
    # NEW: Property-level images (main display photos)
    images = Column(JSON, default=list)
    # NEW: Average rating for display (updated by review system)
    rating = Column(Float, nullable=True, index=True)
    # NEW: Is property currently active/available
    is_active = Column(Boolean, default=True, index=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), index=True)

    owner = relationship("User", back_populates="properties")
    rooms = relationship("Room", back_populates="property")
    bookings = relationship("Booking", back_populates="property")

    __table_args__ = (
        Index("idx_property_lat_lng", "latitude", "longitude"),
        Index("idx_property_search", "city", "type", "is_active"),
    )


class Room(Base):
    __tablename__ = "rooms"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    property_id = Column(String, ForeignKey("properties.id"), index=True)
    room_type = Column(String)  # Single, Double, Triple
    price = Column(Numeric(10, 2), index=True)
    availability_count = Column(Integer, default=0, index=True)
    images = Column(JSON, default=list)

    property = relationship("Property", back_populates="rooms")


class Booking(Base):
    __tablename__ = "bookings"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, ForeignKey("users.id"), index=True)
    property_id = Column(String, ForeignKey("properties.id"), index=True)
    room_id = Column(String, ForeignKey("rooms.id"), index=True)
    status = Column(String, default=BookingStatus.PENDING_PAYMENT, index=True)
    razorpay_order_id = Column(String, index=True)
    razorpay_payment_id = Column(String)
    amount_paid = Column(Numeric(10, 2))
    # NEW: Booking dates and duration
    check_in_date = Column(Date, nullable=True)
    duration_months = Column(Integer, nullable=True, default=1)
    # NEW: Denormalized customer name for quick display in owner dashboard
    customer_name = Column(String, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), index=True)

    user = relationship("User", back_populates="bookings")
    property = relationship("Property", back_populates="bookings")
    room = relationship("Room")


class KycDocument(Base):
    __tablename__ = "kyc_documents"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, ForeignKey("users.id"), index=True)
    document_type = Column(String) # AADHAAR, PAN, DRIVING_LICENSE
    document_url = Column(String)
    status = Column(String, default=KycStatus.PENDING, index=True)
    uploaded_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", backref="kyc_documents")


class SupportTicket(Base):
    __tablename__ = "support_tickets"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, ForeignKey("users.id"), index=True)
    subject = Column(String)
    description = Column(String)
    status = Column(String, default="OPEN", index=True) # OPEN, IN_PROGRESS, RESOLVED, CLOSED
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    user = relationship("User", backref="support_tickets")


class UserPropertyWishlist(Base):
    __tablename__ = "user_property_wishlist"

    user_id = Column(String, ForeignKey("users.id"), primary_key=True)
    property_id = Column(String, ForeignKey("properties.id"), primary_key=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", backref="wishlists")
    property = relationship("Property")


class BankAccount(Base):
    __tablename__ = "bank_accounts"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, ForeignKey("users.id"), index=True)
    account_holder_name = Column(String)
    account_number = Column(String)
    ifsc_code = Column(String)
    bank_name = Column(String)
    is_primary = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", backref="bank_accounts")


