from pydantic import BaseModel, Field
from typing import List, Optional, Any
from decimal import Decimal


# ─────────────────────────────────────────────
# AUTH SCHEMAS
# ─────────────────────────────────────────────

class SendOtpRequest(BaseModel):
    mobile: str  # 10-digit Indian mobile number


class SendOtpResponse(BaseModel):
    detail: str
    expires_in_seconds: int
    debug_otp: Optional[str] = None


class VerifyOtpRequest(BaseModel):
    mobile: str
    code: str       # 6-digit OTP code
    name: Optional[str] = None
    email: Optional[str] = None


class GoogleAuthRequest(BaseModel):
    id_token: str


class RefreshRequest(BaseModel):
    refresh_token: str


class RegisterRequest(BaseModel):
    username: Optional[str] = None
    name: Optional[str] = None
    real_name: Optional[str] = None
    email: Optional[str] = None
    mobile: Optional[str] = None
    password: str
    confirm_password: Optional[str] = None
    role: Optional[str] = "STUDENT"

    class Config:
        extra = "allow"


class LoginRequest(BaseModel):
    username: Optional[str] = None
    email: Optional[str] = None
    login_id: Optional[str] = None   # Android sends this field as generic identifier
    password: str

    class Config:
        extra = "allow"


class UserRead(BaseModel):
    id: str
    firebase_uid: Optional[str] = None
    username: Optional[str] = None
    name: Optional[str] = None
    email: Optional[str] = None
    mobile: Optional[str] = None
    role: str
    profile_image: Optional[str] = None
    auth_provider: Optional[str] = None
    kyc_status: str
    created_at: str
    last_login_at: Optional[str] = None

    class Config:
        from_attributes = True


class UserUpdate(BaseModel):
    name: Optional[str] = None
    email: Optional[str] = None
    mobile: Optional[str] = None
    profile_image: Optional[str] = None

    class Config:
        extra = "allow"


class AuthResponse(BaseModel):
    # FIX: Use snake_case field names directly — Android uses @SerializedName("access_token")
    # Removed serialization_alias which was causing camelCase output confusing the client
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    user: UserRead

    class Config:
        from_attributes = True
        populate_by_name = True


# ─────────────────────────────────────────────
# PROPERTY SCHEMAS
# ─────────────────────────────────────────────

class RoomBase(BaseModel):
    id: str
    room_type: str
    price: Decimal
    availability_count: int
    images: List[str] = []


class RoomCreate(BaseModel):
    """Used when owner creates a new property with rooms."""
    room_type: str          # "Single", "Double", "Triple"
    price: Decimal
    availability_count: int = 0
    images: List[str] = []


class PropertyCreate(BaseModel):
    """Request body for POST /api/v1/properties/ — owner creates a new property listing."""
    name: str
    description: Optional[str] = None
    address: str
    city: str
    state: str
    pincode: str
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    type: str               # "HOSTEL", "PG", "FLAT"
    gender: Optional[str] = "UNISEX"   # "BOYS", "GIRLS", "UNISEX"
    amenities: Optional[dict] = None
    images: List[str] = []
    rooms: List[RoomCreate] = []


class PropertySearchItem(BaseModel):
    id: str
    name: str
    address: str
    city: str
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    type: str
    gender: Optional[str] = None       # NEW: gender filter
    verified_badge: bool = False
    starting_price: Optional[Decimal] = None
    distance_km: Optional[float] = None
    images: List[str] = []             # NEW: property images for card display
    rating: Optional[float] = None     # NEW: average rating


class PropertySearchResponse(BaseModel):
    total: int
    properties: List[PropertySearchItem]


class PropertyDetailResponse(BaseModel):
    id: str
    owner_id: str
    name: str
    description: Optional[str] = None
    address: str
    city: str
    state: Optional[str] = None
    pincode: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    type: str
    gender: Optional[str] = None       # NEW
    verified_badge: bool = False
    amenities: Optional[Any] = None
    images: List[str] = []             # NEW: property-level photos
    rating: Optional[float] = None     # NEW
    is_active: bool = True             # NEW
    rooms: List[RoomBase] = []
    created_at: str


class OwnerPropertyItem(BaseModel):
    """Used in owner's 'My Listings' screen."""
    id: str
    name: str
    city: str
    type: str
    gender: Optional[str] = None
    verified_badge: bool = False
    is_active: bool = True
    total_rooms: int = 0
    available_beds: int = 0
    starting_price: Optional[Decimal] = None
    images: List[str] = []
    rating: Optional[float] = None
    created_at: str


class OwnerPropertyListResponse(BaseModel):
    total: int
    properties: List[OwnerPropertyItem]


# ─────────────────────────────────────────────
# BOOKING SCHEMAS
# ─────────────────────────────────────────────

class BookingCreate(BaseModel):
    property_id: str
    room_id: str
    check_in_date: Optional[str] = None    # "YYYY-MM-DD" format
    duration_months: Optional[int] = 1


class BookingResponse(BaseModel):
    id: str
    status: str
    razorpay_order_id: Optional[str] = None
    amount: Decimal


class MyBookingItem(BaseModel):
    """Single booking item for student's booking history."""
    id: str
    property_id: str
    property_name: str
    property_address: str
    room_type: str
    status: str
    amount_paid: Optional[Decimal] = None
    check_in_date: Optional[str] = None
    duration_months: Optional[int] = None
    created_at: str


class MyBookingsResponse(BaseModel):
    total: int
    bookings: List[MyBookingItem]


class OwnerBookingItem(BaseModel):
    """Single booking item for owner's bookings dashboard."""
    id: str
    customer_name: Optional[str] = None
    customer_mobile: Optional[str] = None
    property_name: str
    room_type: str
    status: str
    amount_paid: Optional[Decimal] = None
    check_in_date: Optional[str] = None
    duration_months: Optional[int] = None
    created_at: str


class OwnerBookingsResponse(BaseModel):
    total: int
    bookings: List[OwnerBookingItem]


class OwnerStatsResponse(BaseModel):
    """Summary stats for owner dashboard."""
    total_properties: int
    active_properties: int
    total_bookings: int
    active_bookings: int
    total_earnings: Decimal
    monthly_earnings: Decimal


# ─────────────────────────────────────────────
# PAYMENT SCHEMAS
# ─────────────────────────────────────────────

class PaymentVerifyRequest(BaseModel):
    razorpay_order_id: str
    razorpay_payment_id: str
    razorpay_signature: str


class PaymentVerifyResponse(BaseModel):
    success: bool
    message: str


# ─────────────────────────────────────────────
# GENERIC RESPONSE WRAPPER
# ─────────────────────────────────────────────

class BaseResponse(BaseModel):
    success: bool
    message: str
    data: Any

# ─────────────────────────────────────────────
# KYC SCHEMAS
# ─────────────────────────────────────────────

class KycDocumentCreate(BaseModel):
    document_type: str # AADHAAR, PAN, DRIVING_LICENSE
    document_url: str


class KycDocumentResponse(BaseModel):
    id: str
    document_type: str
    document_url: str
    status: str
    uploaded_at: str

    class Config:
        from_attributes = True


# ─────────────────────────────────────────────
# SUPPORT TICKET SCHEMAS
# ─────────────────────────────────────────────

class SupportTicketCreate(BaseModel):
    subject: str
    description: str


class SupportTicketResponse(BaseModel):
    id: str
    subject: str
    description: str
    status: str
    created_at: str
    updated_at: str

    class Config:
        from_attributes = True


# ─────────────────────────────────────────────
# BANK ACCOUNT SCHEMAS
# ─────────────────────────────────────────────

class BankAccountCreate(BaseModel):
    account_holder_name: str
    account_number: str
    ifsc_code: str
    bank_name: str
    is_primary: bool = True


class BankAccountResponse(BaseModel):
    id: str
    account_holder_name: str
    account_number: str
    ifsc_code: str
    bank_name: str
    is_primary: bool
    created_at: str

    class Config:
        from_attributes = True


# ─────────────────────────────────────────────
# WISHLIST SCHEMAS
# ─────────────────────────────────────────────

class WishlistResponse(BaseModel):
    user_id: str
    property_id: str
    created_at: str

    class Config:
        from_attributes = True

