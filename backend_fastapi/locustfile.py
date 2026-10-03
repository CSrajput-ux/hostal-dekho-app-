"""
High-Concurrency Load Testing Suite for HostelDekho (10,000 Users Simulation).
Run using Locust:
    pip install locust
    locust -f locustfile.py --host=http://localhost:8000
"""
from locust import HttpUser, task, between
import random

CITIES = ["Delhi", "Mumbai", "Bangalore", "Kota", "Pune", "Hyderabad", "Jaipur", "Noida", "Indore"]
TYPES = ["HOSTEL", "PG", "FLAT"]
GENDERS = ["BOYS", "GIRLS", "UNISEX"]


class HostelDekhoStudentUser(HttpUser):
    # Simulates realistic think time between actions: 1 to 3 seconds
    wait_time = between(1, 3)

    def on_start(self):
        """Simulate user login or visitor initialization."""
        self.client.headers.update({"Accept-Encoding": "gzip, deflate"})
        self.cached_property_ids = []

    @task(50)
    def search_properties_by_city(self):
        """Simulate frequent hostel searching (primary load on system)."""
        city = random.choice(CITIES)
        prop_type = random.choice(TYPES)
        gender = random.choice(GENDERS)

        with self.client.get(
            f"/api/v1/properties/search?location={city}&type={prop_type}&gender={gender}&limit=20&page=1",
            catch_response=True,
            name="/properties/search",
        ) as response:
            if response.status_code == 200:
                data = response.json()
                props = data.get("properties", [])
                if props:
                    self.cached_property_ids = [p["id"] for p in props[:5]]
                response.success()
            else:
                response.failure(f"Search failed with code {response.status_code}")

    @task(30)
    def search_nearby_properties(self):
        """Simulate GPS nearby searches."""
        # Delhi area coordinates with slight random jitter
        lat = 28.6139 + random.uniform(-0.05, 0.05)
        lng = 77.2090 + random.uniform(-0.05, 0.05)

        with self.client.get(
            f"/api/v1/properties/nearby?lat={lat:.4f}&lng={lng:.4f}&radius=5000&limit=20",
            catch_response=True,
            name="/properties/nearby",
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Nearby search failed with code {response.status_code}")

    @task(15)
    def view_property_detail(self):
        """Simulate opening a hostel detail page."""
        if self.cached_property_ids:
            prop_id = random.choice(self.cached_property_ids)
            with self.client.get(
                f"/api/v1/properties/{prop_id}",
                catch_response=True,
                name="/properties/{id}",
            ) as response:
                if response.status_code in [200, 404]:
                    response.success()
                else:
                    response.failure(f"Detail view failed with code {response.status_code}")

    @task(4)
    def health_check_ping(self):
        """Load balancer / monitoring health check."""
        self.client.get("/api/health", name="/api/health")

    @task(1)
    def send_otp_attempt(self):
        """Simulate user login / OTP generation."""
        dummy_mobile = f"98{random.randint(10000000, 99999999)}"
        self.client.post(
            "/api/v1/auth/send-otp",
            json={"mobile": dummy_mobile},
            name="/auth/send-otp",
        )
