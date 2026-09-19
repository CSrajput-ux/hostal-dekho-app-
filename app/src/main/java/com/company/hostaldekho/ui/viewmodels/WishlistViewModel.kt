package com.company.hostaldekho.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.hostaldekho.data.remote.dto.PropertySearchItem
import com.company.hostaldekho.data.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WishlistState {
    object Loading : WishlistState()
    data class Success(val items: List<PropertySearchItem>) : WishlistState()
    data class Error(val message: String) : WishlistState()
}

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val repository: WishlistRepository
) : ViewModel() {

    private val _wishlistState = MutableStateFlow<WishlistState>(WishlistState.Loading)
    val wishlistState: StateFlow<WishlistState> = _wishlistState

    init {
        loadWishlist()
    }

    fun loadWishlist() {
        viewModelScope.launch {
            _wishlistState.value = WishlistState.Loading
            val result = repository.getMyWishlist()
            result.onSuccess { items ->
                _wishlistState.value = WishlistState.Success(items)
            }.onFailure { error ->
                _wishlistState.value = WishlistState.Error(error.message ?: "Failed to load wishlist")
            }
        }
    }

    fun addToWishlist(propertyId: String) {
        viewModelScope.launch {
            repository.addToWishlist(propertyId)
            loadWishlist()
        }
    }

    fun removeFromWishlist(propertyId: String) {
        viewModelScope.launch {
            repository.removeFromWishlist(propertyId)
            loadWishlist()
        }
    }
}
