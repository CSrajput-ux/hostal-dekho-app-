"""
Properties endpoints.

Endpoints:
  GET  /nearby          — Find properties near lat/lng (Haversine formula)
  GET  /search          — Search by city/text with optional gender/type/price filters
  GET  /my              — Owner's own property listings (auth required)
  POST /                — Create a new property listing (owner auth required)
  GET  /{property_id}   — Full property details with rooms
"""
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session, selectinload
from sqlalchemy import text
from typing import Optional
from decimal import Decimal
import math
from ....api.deps import get_db, get_current_user, require_roles
from .... import models, schemas
from ....core.cache import cache

router = APIRouter()


# ─────────────────────────────────────────────────────────────────────────────
# GET /nearby  — Find properties near a lat/lng using Haversine formula
# ─────────────────────────────────────────────────────────────────────────────
@router.get("/nearby", response_model=schemas.PropertySearchResponse)
def get_nearby_properties(
    lat: float = Query(..., ge=-90, le=90, description="Latitude"),
    lng: float = Query(..., ge=-180, le=180, description="Longitude"),
    radius: float = Query(5000, gt=0, description="Search radius in meters"),
    limit: int = Query(20, ge=1, le=100),
    gender: Optional[str] = Query(None, description="BOYS, GIRLS, UNISEX"),
    type: Optional[str] = Query(None, description="HOSTEL, PG, FLAT"),
    db: Session = Depends(get_db),
):
    """
    Return properties within `radius` meters of (lat, lng).
    Results are sorted by distance ascending.
    Cached for high throughput under 10k users concurrency.
    """
    # High-speed cache lookup (rounded to ~110m for hotspot deduplication)
    cache_key = f"props:nearby:{round(lat, 3)}:{round(lng, 3)}:{int(radius)}:{limit}:{gender}:{type}"
    cached = cache.get(cache_key)
    if cached:
        return schemas.PropertySearchResponse(**cached)

    # Bounding box calculation for B-Tree index acceleration
    lat_deg_delta = radius / 111000.0
    cos_lat = math.cos(math.radians(lat))
    lng_deg_delta = radius / (111000.0 * max(0.01, abs(cos_lat)))
    min_lat, max_lat = lat - lat_deg_delta, lat + lat_deg_delta
    min_lng, max_lng = lng - lng_deg_delta, lng + lng_deg_delta

    items = []
    try:
        # Haversine formula using spatial bounding box to utilize latitude/longitude index
        query = text("""
            SELECT p.id, p.name, p.address, p.city, p.latitude, p.longitude,
                   p.type, p.gender, p.verified_badge, p.images, p.rating,
                   (6371000 * acos(
                       LEAST(1.0, GREATEST(-1.0, cos(radians(:lat)) * cos(radians(latitude)) *
                       cos(radians(longitude) - radians(:lng)) +
                       sin(radians(:lat)) * sin(radians(latitude))))
                   )) AS distance_m,
                   (SELECT MIN(price) FROM rooms WHERE property_id = p.id) AS starting_price
            FROM properties p
            WHERE latitude BETWEEN :min_lat AND :max_lat
              AND longitude BETWEEN :min_lng AND :max_lng
              AND (p.is_active IS NULL OR p.is_active = TRUE)
              AND (6371000 * acos(
                       LEAST(1.0, GREATEST(-1.0, cos(radians(:lat)) * cos(radians(latitude)) *
                       cos(radians(longitude) - radians(:lng)) +
                       sin(radians(:lat)) * sin(radians(latitude))))
                   )) <= :radius
            ORDER BY distance_m ASC
            LIMIT :limit
        """)

        results = db.execute(
            query, {
                "lat": lat, "lng": lng, "radius": radius, "limit": limit,
                "min_lat": min_lat, "max_lat": max_lat,
                "min_lng": min_lng, "max_lng": max_lng,
            }
        ).fetchall()

        for row in results:
            if gender and row.gender and row.gender.upper() != gender.upper():
                continue
            if type and row.type and row.type.upper() != type.upper():
                continue
            items.append(
                schemas.PropertySearchItem(
                    id=row.id,
                    name=row.name or "Unnamed Property",
                    address=row.address or "",
                    city=row.city or "",
                    latitude=row.latitude,
                    longitude=row.longitude,
                    type=row.type or "HOSTEL",
                    gender=row.gender or "UNISEX",
                    verified_badge=bool(row.verified_badge),
                    starting_price=row.starting_price,
                    distance_km=round(row.distance_m / 1000, 2) if row.distance_m is not None else None,
                    images=row.images if row.images else [],
                    rating=row.rating,
                )
            )
    except Exception:
        # Portable Python fallback (used for SQLite test suites lacking radians/acos)
        candidates = (
            db.query(models.Property)
            .options(selectinload(models.Property.rooms))
            .filter(
                models.Property.latitude.isnot(None),
                models.Property.longitude.isnot(None),
                (models.Property.is_active == True) | (models.Property.is_active == None),
            )
            .all()
        )
        calculated = []
        for p in candidates:
            if gender and p.gender and p.gender.upper() != gender.upper():
                continue
            if type and p.type and p.type.upper() != type.upper():
                continue
            dlat = math.radians(p.latitude - lat)
            dlng = math.radians(p.longitude - lng)
            a = (
                math.sin(dlat / 2) ** 2
                + math.cos(math.radians(lat)) * math.cos(math.radians(p.latitude)) * math.sin(dlng / 2) ** 2
            )
            c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
            dist_m = 6371000 * c
            if dist_m <= radius:
                min_p = min([r.price for r in p.rooms if r.price is not None], default=None) if p.rooms else None
                calculated.append((dist_m, p, min_p))

        calculated.sort(key=lambda x: x[0])
        for dist_m, p, min_price in calculated[:limit]:
            items.append(
                schemas.PropertySearchItem(
                    id=p.id,
                    name=p.name,
                    address=p.address or "",
                    city=p.city or "",
                    latitude=p.latitude,
                    longitude=p.longitude,
                    type=p.type or "HOSTEL",
                    gender=p.gender or "UNISEX",
                    verified_badge=bool(p.verified_badge),
                    starting_price=min_price,
                    distance_km=round(dist_m / 1000.0, 2),
                    images=p.images if p.images else [],
                    rating=p.rating,
                )
            )

    response = schemas.PropertySearchResponse(total=len(items), properties=items)
    cache.set(cache_key, response.model_dump(), expire_seconds=120)
    return response


# ─────────────────────────────────────────────────────────────────────────────
# GET /search  — Search properties by city / text query + filters
# ─────────────────────────────────────────────────────────────────────────────
@router.get("/search", response_model=schemas.PropertySearchResponse)
def search_properties(
    location: Optional[str] = Query(None, description="City name or location text"),
    type: Optional[str] = Query(None, description="Property type: HOSTEL, PG, FLAT"),
    gender: Optional[str] = Query(None, description="BOYS, GIRLS, UNISEX"),
    min_price: Optional[float] = Query(None, ge=0),
    max_price: Optional[float] = Query(None, ge=0),
    limit: int = Query(20, ge=1, le=100),
    page: int = Query(1, ge=1),
    db: Session = Depends(get_db),
):
    """
    Search properties by city/location text with optional filters.
    Optimized with Redis caching and selectinload to eliminate N+1 DB queries.
    """
    cache_key = f"props:search:{location}:{type}:{gender}:{min_price}:{max_price}:{limit}:{page}"
    cached = cache.get(cache_key)
    if cached:
        return schemas.PropertySearchResponse(**cached)

    query = db.query(models.Property).filter(
        (models.Property.is_active == True) | (models.Property.is_active == None)
    )

    if location:
        query = query.filter(
            models.Property.city.ilike(f"%{location}%") |
            models.Property.address.ilike(f"%{location}%") |
            models.Property.name.ilike(f"%{location}%")
        )
    if type:
        query = query.filter(models.Property.type.ilike(type))

    if gender:
        query = query.filter(
            (models.Property.gender.ilike(gender)) |
            (models.Property.gender == "UNISEX") |
            (models.Property.gender == None)
        )

    total = query.count()
    offset = (page - 1) * limit
    # CRITICAL SCALE FIX: Use selectinload to fetch all rooms in 1 query, avoiding N+1 roundtrips
    properties = query.options(selectinload(models.Property.rooms)).offset(offset).limit(limit).all()

    items = []
    for prop in properties:
        min_room_price = None
        if prop.rooms:
            prices = [r.price for r in prop.rooms if r.price is not None]
            min_room_price = min(prices) if prices else None

        if min_price is not None and min_room_price is not None and float(min_room_price) < min_price:
            continue
        if max_price is not None and min_room_price is not None and float(min_room_price) > max_price:
            continue

        items.append(
            schemas.PropertySearchItem(
                id=prop.id,
                name=prop.name,
                address=prop.address or "",
                city=prop.city or "",
                latitude=prop.latitude,
                longitude=prop.longitude,
                type=prop.type or "",
                gender=prop.gender,
                verified_badge=bool(prop.verified_badge),
                starting_price=min_room_price,
                distance_km=None,
                images=prop.images if prop.images else [],
                rating=prop.rating,
            )
        )

    response = schemas.PropertySearchResponse(total=total, properties=items)
    cache.set(cache_key, response.model_dump(), expire_seconds=120)
    return response


# ─────────────────────────────────────────────────────────────────────────────
# GET /my  — Owner's own listings (requires authentication)
# ─────────────────────────────────────────────────────────────────────────────
@router.get("/my", response_model=schemas.OwnerPropertyListResponse)
def get_my_properties(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(models.UserRole.OWNER, models.UserRole.ADMIN)),
):
    """
    Return all properties owned by the currently authenticated user.
    Uses selectinload to eagerly fetch rooms in a single query.
    """
    properties = (
        db.query(models.Property)
        .options(selectinload(models.Property.rooms))
        .filter(models.Property.owner_id == current_user.id)
        .order_by(models.Property.created_at.desc())
        .all()
    )

    items = []
    for prop in properties:
        total_rooms = len(prop.rooms) if prop.rooms else 0
        available_beds = sum(
            (r.availability_count or 0) for r in prop.rooms
        ) if prop.rooms else 0
        min_price = None
        if prop.rooms:
            prices = [r.price for r in prop.rooms if r.price is not None]
            min_price = min(prices) if prices else None

        items.append(
            schemas.OwnerPropertyItem(
                id=prop.id,
                name=prop.name,
                city=prop.city or "",
                type=prop.type or "",
                gender=prop.gender,
                verified_badge=bool(prop.verified_badge),
                is_active=bool(prop.is_active) if prop.is_active is not None else True,
                total_rooms=total_rooms,
                available_beds=available_beds,
                starting_price=min_price,
                images=prop.images if prop.images else [],
                rating=prop.rating,
                created_at=str(prop.created_at),
            )
        )

    return schemas.OwnerPropertyListResponse(total=len(items), properties=items)


# ─────────────────────────────────────────────────────────────────────────────
# POST /  — Create a new property listing (owner only)
# ─────────────────────────────────────────────────────────────────────────────
@router.post("/", response_model=schemas.PropertyDetailResponse, status_code=status.HTTP_201_CREATED)
def create_property(
    request: schemas.PropertyCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(models.UserRole.OWNER, models.UserRole.ADMIN)),
):
    """
    Create a new property listing with optional rooms.
    Invalidates property search caches immediately.
    """
    prop_type = request.type.upper() if request.type else "HOSTEL"
    prop_gender = request.gender.upper() if request.gender else "UNISEX"

    new_property = models.Property(
        owner_id=current_user.id,
        name=request.name,
        description=request.description,
        address=request.address,
        city=request.city,
        state=request.state,
        pincode=request.pincode,
        latitude=request.latitude,
        longitude=request.longitude,
        type=prop_type,
        gender=prop_gender,
        amenities=request.amenities,
        images=request.images,
        is_active=True,
    )
    try:
        db.add(new_property)
        db.flush()
        for room_data in request.rooms:
            db.add(models.Room(
                property_id=new_property.id,
                room_type=room_data.room_type,
                price=room_data.price,
                availability_count=room_data.availability_count,
                images=room_data.images,
            ))
        db.commit()
        db.refresh(new_property)
    except Exception:
        db.rollback()
        raise

    # Invalidate cache so new listing appears in search instantly
    cache.invalidate_properties_cache(new_property.id)

    rooms_response = [
        schemas.RoomBase(
            id=r.id,
            room_type=r.room_type or "Standard",
            price=r.price or Decimal("0"),
            availability_count=r.availability_count or 0,
            images=r.images or [],
        )
        for r in new_property.rooms
    ]

    return schemas.PropertyDetailResponse(
        id=new_property.id,
        owner_id=new_property.owner_id or "",
        name=new_property.name,
        description=new_property.description or "",
        address=new_property.address or "",
        city=new_property.city or "",
        state=new_property.state,
        pincode=new_property.pincode,
        latitude=new_property.latitude,
        longitude=new_property.longitude,
        type=new_property.type or "",
        gender=new_property.gender,
        verified_badge=bool(new_property.verified_badge),
        amenities=new_property.amenities,
        images=new_property.images or [],
        rating=new_property.rating,
        is_active=bool(new_property.is_active),
        rooms=rooms_response,
        created_at=str(new_property.created_at),
    )


# ─────────────────────────────────────────────────────────────────────────────
# GET /{id}  — Get full property details
# ─────────────────────────────────────────────────────────────────────────────
@router.get("/{property_id}", response_model=schemas.PropertyDetailResponse)
def get_property_by_id(
    property_id: str,
    db: Session = Depends(get_db),
):
    """Return full details for a single property with Redis caching."""
    cache_key = f"props:detail:{property_id}"
    cached = cache.get(cache_key)
    if cached:
        return schemas.PropertyDetailResponse(**cached)

    prop = (
        db.query(models.Property)
        .options(selectinload(models.Property.rooms))
        .filter(models.Property.id == property_id)
        .first()
    )
    if not prop:
        raise HTTPException(status_code=404, detail="Property not found")

    rooms = [
        schemas.RoomBase(
            id=r.id,
            room_type=r.room_type or "Standard",
            price=r.price or Decimal("0"),
            availability_count=r.availability_count or 0,
            images=r.images or [],
        )
        for r in prop.rooms
    ]

    response = schemas.PropertyDetailResponse(
        id=prop.id,
        owner_id=prop.owner_id or "",
        name=prop.name,
        description=prop.description or "",
        address=prop.address or "",
        city=prop.city or "",
        state=prop.state,
        pincode=prop.pincode,
        latitude=prop.latitude,
        longitude=prop.longitude,
        type=prop.type or "",
        gender=prop.gender,
        verified_badge=bool(prop.verified_badge),
        amenities=prop.amenities,
        images=prop.images or [],
        rating=prop.rating,
        is_active=bool(prop.is_active) if prop.is_active is not None else True,
        rooms=rooms,
        created_at=str(prop.created_at),
    )
    cache.set(cache_key, response.model_dump(), expire_seconds=300)
    return response
