import os

os.environ.setdefault("DATABASE_URL", "sqlite:///./test_hosteldekho.db")
os.environ.setdefault("JWT_SECRET", "test-only-secret")
os.environ.setdefault("DEBUG_MODE", "false")
# Exercise the explicit local-development payment branch; the test never calls
# an external payment provider.
os.environ["RAZORPAY_KEY_ID"] = ""
os.environ["RAZORPAY_KEY_SECRET"] = ""

import pytest
from fastapi.testclient import TestClient

from app.database import Base, engine
from app.main import app
from app.api.v1.endpoints import bookings


@pytest.fixture(autouse=True)
def clean_database():
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
    yield
    Base.metadata.drop_all(bind=engine)


@pytest.fixture()
def client(monkeypatch):
    monkeypatch.setattr(
        bookings,
        "create_razorpay_order",
        lambda amount: {"id": f"order_test_{amount}"},
    )
    with TestClient(app) as test_client:
        yield test_client


def register(client: TestClient, username: str, email: str, role: str):
    response = client.post("/api/v1/auth/register", json={
        "username": username,
        "name": username,
        "email": email,
        "password": "Passw0rd!",
        "role": role,
    })
    assert response.status_code == 200, response.text
    return response.json()


def auth(response_json: dict):
    return {"Authorization": f"Bearer {response_json['access_token']}"}


def test_property_booking_crud_and_authorization(client: TestClient):
    student = register(client, "student", "student@example.com", "STUDENT")
    owner = register(client, "owner", "owner@example.com", "OWNER")

    forbidden = client.post("/api/v1/properties/", headers=auth(student), json={
        "name": "Blocked", "address": "Address", "city": "Delhi", "state": "DL",
        "pincode": "110001", "type": "HOSTEL",
    })
    assert forbidden.status_code == 403

    created = client.post("/api/v1/properties/", headers=auth(owner), json={
        "name": "Audit Hostel", "address": "1 Test Road", "city": "Delhi", "state": "DL",
        "pincode": "110001", "latitude": 28.6139, "longitude": 77.2090,
        "type": "HOSTEL", "gender": "UNISEX",
        "rooms": [{"room_type": "Single", "price": 8000, "availability_count": 2}],
    })
    assert created.status_code == 201, created.text
    property_data = created.json()
    room_id = property_data["rooms"][0]["id"]

    listing = client.get("/api/v1/properties/my", headers=auth(owner))
    assert listing.status_code == 200
    assert listing.json()["total"] == 1

    booking = client.post("/api/v1/bookings/", headers=auth(student), json={
        "property_id": property_data["id"], "room_id": room_id, "duration_months": 1,
    })
    assert booking.status_code == 200, booking.text
    assert booking.json()["status"] == "PENDING_PAYMENT"

    history = client.get("/api/v1/bookings/my", headers=auth(student))
    assert history.status_code == 200
    assert history.json()["total"] == 1

    owner_bookings = client.get("/api/v1/bookings/owner", headers=auth(owner))
    assert owner_bookings.status_code == 200
    assert owner_bookings.json()["total"] == 1

    bad_pair = client.post("/api/v1/bookings/", headers=auth(student), json={
        "property_id": "not-the-room-property", "room_id": room_id,
    })
    assert bad_pair.status_code == 404


def test_auth_profile_and_public_search(client: TestClient):
    student = register(client, "profileuser", "profile@example.com", "STUDENT")
    headers = auth(student)

    me = client.get("/api/v1/auth/me", headers=headers)
    assert me.status_code == 200
    assert me.json()["email"] == "profile@example.com"

    updated = client.put("/api/v1/auth/me", headers=headers, json={"name": "Updated User"})
    assert updated.status_code == 200
    assert updated.json()["name"] == "Updated User"

    search = client.get("/api/v1/properties/search", params={"location": "Delhi"})
    assert search.status_code == 200
    assert search.json() == {"total": 0, "properties": []}


def test_production_health_and_process_time(client: TestClient):
    response = client.get("/api/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert data["database"] == "healthy"
    assert "cache" in data
    assert "X-Process-Time" in response.headers


def test_distributed_otp_flow(client: TestClient):
    # Test send OTP
    response = client.post("/api/v1/auth/send-otp", json={"mobile": "9876543210"})
    assert response.status_code == 200
    assert "expires_in_seconds" in response.json()

    # Verify invalid OTP fails
    bad_verify = client.post("/api/v1/auth/verify-otp", json={"mobile": "9876543210", "code": "000000"})
    assert bad_verify.status_code == 401

    # Verify directly via cache helper
    from app.core.cache import cache
    test_code = "123456"
    cache.store_otp("9999988888", test_code, expire_seconds=300)
    assert cache.get_otp("9999988888") == test_code

    valid_verify = client.post("/api/v1/auth/verify-otp", json={"mobile": "9999988888", "code": test_code})
    assert valid_verify.status_code == 200
    assert "access_token" in valid_verify.json()
    # Code should be consumed
    assert cache.get_otp("9999988888") is None


def test_nearby_properties_and_caching(client: TestClient):
    owner = register(client, "nearbyowner", "nearbyowner@example.com", "OWNER")
    created = client.post("/api/v1/properties/", headers=auth(owner), json={
        "name": "Connaught Place PG", "address": "CP Inner Circle", "city": "Delhi", "state": "DL",
        "pincode": "110001", "latitude": 28.6315, "longitude": 77.2167,
        "type": "PG", "gender": "BOYS",
        "rooms": [{"room_type": "Single", "price": 12000, "availability_count": 3}],
    })
    assert created.status_code == 201

    # Search nearby Connaught Place (within 5000 meters)
    nearby_res = client.get("/api/v1/properties/nearby", params={
        "lat": 28.6300,
        "lng": 77.2150,
        "radius": 5000,
    })
    assert nearby_res.status_code == 200
    data = nearby_res.json()
    assert data["total"] >= 1
    assert data["properties"][0]["name"] == "Connaught Place PG"

    # Second call should hit cache cleanly
    cached_res = client.get("/api/v1/properties/nearby", params={
        "lat": 28.6300,
        "lng": 77.2150,
        "radius": 5000,
    })
    assert cached_res.status_code == 200
    assert cached_res.json()["total"] == data["total"]

