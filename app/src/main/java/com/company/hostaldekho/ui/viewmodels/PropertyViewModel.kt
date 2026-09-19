package com.company.hostaldekho.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.hostaldekho.data.remote.dto.BookingResponse
import com.company.hostaldekho.data.remote.dto.MyBookingsResponse
import com.company.hostaldekho.data.remote.dto.OwnerBookingsResponse
import com.company.hostaldekho.data.remote.dto.OwnerPropertyListResponse
import com.company.hostaldekho.data.remote.dto.OwnerStatsResponse
import com.company.hostaldekho.data.remote.dto.PropertyCreateRequest
import com.company.hostaldekho.data.remote.dto.PropertyRead
import com.company.hostaldekho.data.remote.dto.PropertySearchItem
import com.company.hostaldekho.data.repository.PropertyRepository
import com.company.hostaldekho.util.LocationManager
import com.company.hostaldekho.util.UserLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────── STATE CLASSES ──────────────────────────

sealed class PropertyListState {
    object Loading : PropertyListState()
    data class Success(val properties: List<PropertySearchItem>) : PropertyListState()
    data class Error(val message: String) : PropertyListState()
}

sealed class PropertyDetailState {
    object Loading : PropertyDetailState()
    data class Success(val property: PropertyRead) : PropertyDetailState()
    data class Error(val message: String) : PropertyDetailState()
}

sealed class BookingState {
    object Idle : BookingState()
    object Loading : BookingState()
    data class OrderCreated(val booking: BookingResponse) : BookingState()
    object Success : BookingState()
    data class Error(val message: String) : BookingState()
}

sealed class SubmitPropertyState {
    object Idle : SubmitPropertyState()
    object Loading : SubmitPropertyState()
    data class Success(val property: PropertyRead) : SubmitPropertyState()
    data class Error(val message: String) : SubmitPropertyState()
}

sealed class OwnerStatsState {
    object Loading : OwnerStatsState()
    data class Success(val stats: OwnerStatsResponse) : OwnerStatsState()
    data class Error(val message: String) : OwnerStatsState()
}

sealed class OwnerPropertiesState {
    object Loading : OwnerPropertiesState()
    data class Success(val data: OwnerPropertyListResponse) : OwnerPropertiesState()
    data class Error(val message: String) : OwnerPropertiesState()
}

sealed class MyBookingsState {
    object Loading : MyBookingsState()
    data class Success(val data: MyBookingsResponse) : MyBookingsState()
    data class Error(val message: String) : MyBookingsState()
}

sealed class OwnerBookingsState {
    object Loading : OwnerBookingsState()
    data class Success(val data: OwnerBookingsResponse) : OwnerBookingsState()
    data class Error(val message: String) : OwnerBookingsState()
}

// ─────────────────────────────────── VIEWMODEL ──────────────────────────────

@HiltViewModel
class PropertyViewModel @Inject constructor(
    private val repository: PropertyRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    // ── Core property browsing ──
    private val _listState = MutableStateFlow<PropertyListState>(PropertyListState.Loading)
    val listState: StateFlow<PropertyListState> = _listState

    private val _detailState = MutableStateFlow<PropertyDetailState>(PropertyDetailState.Loading)
    val detailState: StateFlow<PropertyDetailState> = _detailState

    // ── Booking / Payment ──
    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState: StateFlow<BookingState> = _bookingState

    // ── Student booking history ──
    private val _myBookingsState = MutableStateFlow<MyBookingsState>(MyBookingsState.Loading)
    val myBookingsState: StateFlow<MyBookingsState> = _myBookingsState

    // ── Owner dashboard ──
    private val _ownerStatsState = MutableStateFlow<OwnerStatsState>(OwnerStatsState.Loading)
    val ownerStatsState: StateFlow<OwnerStatsState> = _ownerStatsState

    private val _ownerPropertiesState = MutableStateFlow<OwnerPropertiesState>(OwnerPropertiesState.Loading)
    val ownerPropertiesState: StateFlow<OwnerPropertiesState> = _ownerPropertiesState

    private val _ownerBookingsState = MutableStateFlow<OwnerBookingsState>(OwnerBookingsState.Loading)
    val ownerBookingsState: StateFlow<OwnerBookingsState> = _ownerBookingsState

    // ── Add Property ──
    private val _submitPropertyState = MutableStateFlow<SubmitPropertyState>(SubmitPropertyState.Idle)
    val submitPropertyState: StateFlow<SubmitPropertyState> = _submitPropertyState

    // ── Location ──
    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation.asStateFlow()

    // ──────────────────────────────────────────────
    // Location
    // ──────────────────────────────────────────────

    fun startLocationUpdates() {
        viewModelScope.launch {
            locationManager.getLocationUpdates().collect { location ->
                if (_userLocation.value == null) {
                    _userLocation.value = location
                    fetchNearbyProperties(location.latitude, location.longitude)
                }
            }
        }
    }

    fun selectManualLocation(locationName: String) {
        _userLocation.value = UserLocation(
            latitude = 20.5937,
            longitude = 78.9629,
            accuracy = 0f,
            city = locationName,
            state = "India",
            country = "India"
        )
        val queryCity = if (locationName == "All India" || locationName == "All") null else locationName.substringBefore(",").trim()
        searchProperties(location = queryCity)
    }

    // ──────────────────────────────────────────────
    // Property Browsing (Student)
    // ──────────────────────────────────────────────

    fun fetchNearbyProperties(
        lat: Double,
        lng: Double,
        radius: Double = 5000.0,
        gender: String? = null,
        type: String? = null
    ) {
        viewModelScope.launch {
            _listState.value = PropertyListState.Loading
            val result = repository.getNearbyProperties(lat, lng, radius, gender = gender, type = type)
            result.onSuccess { response ->
                _listState.value = PropertyListState.Success(response.properties)
            }.onFailure { error ->
                _listState.value = PropertyListState.Error(
                    error.message ?: "Failed to load nearby properties"
                )
            }
        }
    }

    fun searchProperties(
        location: String? = null,
        type: String? = null,
        gender: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null
    ) {
        viewModelScope.launch {
            _listState.value = PropertyListState.Loading
            val result = repository.searchProperties(
                location = location,
                type = type,
                gender = gender,
                minPrice = minPrice,
                maxPrice = maxPrice
            )
            result.onSuccess { response ->
                _listState.value = PropertyListState.Success(response.properties)
            }.onFailure { error ->
                _listState.value = PropertyListState.Error(
                    error.message ?: "Failed to load properties"
                )
            }
        }
    }

    fun getPropertyDetails(id: String) {
        viewModelScope.launch {
            _detailState.value = PropertyDetailState.Loading
            val result = repository.getPropertyById(id)
            result.onSuccess { property ->
                _detailState.value = PropertyDetailState.Success(property)
            }.onFailure { error ->
                _detailState.value = PropertyDetailState.Error(
                    error.message ?: "Failed to load property details"
                )
            }
        }
    }

    // ──────────────────────────────────────────────
    // Booking / Payment (Student)
    // ──────────────────────────────────────────────

    fun createBooking(
        propertyId: String,
        roomId: String,
        checkInDate: String? = null,
        durationMonths: Int = 1
    ) {
        viewModelScope.launch {
            _bookingState.value = BookingState.Loading
            val result = repository.createBooking(propertyId, roomId, checkInDate, durationMonths)
            result.onSuccess { booking ->
                _bookingState.value = BookingState.OrderCreated(booking)
            }.onFailure { error ->
                _bookingState.value = BookingState.Error(
                    error.message ?: "Failed to initiate booking"
                )
            }
        }
    }

    fun onPaymentSuccess(orderId: String, paymentId: String, signature: String) {
        viewModelScope.launch {
            _bookingState.value = BookingState.Loading
            val result = repository.verifyPayment(orderId, paymentId, signature)
            result.onSuccess {
                _bookingState.value = BookingState.Success
            }.onFailure { error ->
                _bookingState.value = BookingState.Error(
                    error.message ?: "Payment verification failed"
                )
            }
        }
    }

    fun onPaymentError(message: String) {
        _bookingState.value = BookingState.Error(message)
    }

    fun resetBookingState() {
        _bookingState.value = BookingState.Idle
    }

    /** Load the student's own booking history for BookingsScreen. */
    fun loadMyBookings() {
        viewModelScope.launch {
            _myBookingsState.value = MyBookingsState.Loading
            val result = repository.getMyBookings()
            result.onSuccess { data ->
                _myBookingsState.value = MyBookingsState.Success(data)
            }.onFailure { error ->
                _myBookingsState.value = MyBookingsState.Error(
                    error.message ?: "Failed to load your bookings"
                )
            }
        }
    }

    // ──────────────────────────────────────────────
    // Owner Dashboard
    // ──────────────────────────────────────────────

    /** Load owner stats for dashboard cards (earnings, bookings count, etc). */
    fun loadOwnerStats() {
        viewModelScope.launch {
            _ownerStatsState.value = OwnerStatsState.Loading
            val result = repository.getOwnerStats()
            result.onSuccess { stats ->
                _ownerStatsState.value = OwnerStatsState.Success(stats)
            }.onFailure { error ->
                _ownerStatsState.value = OwnerStatsState.Error(
                    error.message ?: "Failed to load dashboard stats"
                )
            }
        }
    }

    /** Load owner's listed properties for 'My Listings' section. */
    fun loadOwnerProperties() {
        viewModelScope.launch {
            _ownerPropertiesState.value = OwnerPropertiesState.Loading
            val result = repository.getMyProperties()
            result.onSuccess { data ->
                _ownerPropertiesState.value = OwnerPropertiesState.Success(data)
            }.onFailure { error ->
                _ownerPropertiesState.value = OwnerPropertiesState.Error(
                    error.message ?: "Failed to load your properties"
                )
            }
        }
    }

    /** Load all bookings across owner's properties for OwnerBookingsScreen. */
    fun loadOwnerBookings() {
        viewModelScope.launch {
            _ownerBookingsState.value = OwnerBookingsState.Loading
            val result = repository.getOwnerBookings()
            result.onSuccess { data ->
                _ownerBookingsState.value = OwnerBookingsState.Success(data)
            }.onFailure { error ->
                _ownerBookingsState.value = OwnerBookingsState.Error(
                    error.message ?: "Failed to load bookings"
                )
            }
        }
    }

    // ──────────────────────────────────────────────
    // Add Property (Owner)
    // ──────────────────────────────────────────────

    /** Submit a new property listing to backend. Called from AddPropertyScreen. */
    fun submitProperty(request: PropertyCreateRequest) {
        viewModelScope.launch {
            _submitPropertyState.value = SubmitPropertyState.Loading
            val result = repository.createProperty(request)
            result.onSuccess { property ->
                _submitPropertyState.value = SubmitPropertyState.Success(property)
                // Refresh owner's property list after successful creation
                loadOwnerProperties()
            }.onFailure { error ->
                _submitPropertyState.value = SubmitPropertyState.Error(
                    error.message ?: "Failed to submit property"
                )
            }
        }
    }

    fun resetSubmitPropertyState() {
        _submitPropertyState.value = SubmitPropertyState.Idle
    }
}
