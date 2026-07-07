package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.repository.FoodRepository
import com.example.data.remote.FirebaseSyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = FoodRepository(database)
        
        // Seed initial mock restaurants and food items
        viewModelScope.launch {
            repository.seedInitialDataIfNecessary()
        }
    }

    // Search & Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Active screen / Navigation helper within our single-view layout
    private val _currentScreen = MutableStateFlow("explore") // explore, restaurant_detail, cart, orders, rewards, support
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _selectedRestaurantId = MutableStateFlow<String?>(null)
    val selectedRestaurantId: StateFlow<String?> = _selectedRestaurantId.asStateFlow()

    // Observable Flows from DB
    val restaurants: StateFlow<List<RestaurantEntity>> = repository.restaurants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItemEntity>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loyaltyProfile: StateFlow<LoyaltyProfileEntity?> = repository.loyaltyProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val supportMessages: StateFlow<List<SupportMessageEntity>> = repository.supportMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supportTickets: StateFlow<List<SupportTicketEntity>> = repository.supportTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered restaurants
    val filteredRestaurants: StateFlow<List<RestaurantEntity>> = combine(
        restaurants,
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        list.filter { rest ->
            val matchesQuery = rest.name.contains(query, ignoreCase = true) || 
                               rest.cuisine.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || rest.cuisine.contains(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected restaurant food items flow
    val selectedRestaurantFoodItems: StateFlow<List<FoodItemEntity>> = _selectedRestaurantId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getFoodItemsForRestaurant(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedRestaurant: StateFlow<RestaurantEntity?> = _selectedRestaurantId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else restaurants.map { list -> list.find { it.id == id } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Cart Calculations
    val cartSubtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartDeliveryFee: StateFlow<Double> = cartSubtotal.map { sub ->
        if (sub == 0.0) 0.0 else if (sub > 300.0) 0.0 else 30.0 // Free delivery over Rs 300
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartGst: StateFlow<Double> = cartSubtotal.map { sub ->
        sub * 0.05 // 5% GST
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ANNIVO Commission savings (how much would have been taken by traditional food apps at ~25%)
    val cartCommissionSavings: StateFlow<Double> = cartSubtotal.map { sub ->
        sub * 0.25
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartGrandTotal: StateFlow<Double> = combine(
        cartSubtotal,
        cartDeliveryFee,
        cartGst
    ) { sub, delivery, gst ->
        sub + delivery + gst
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartLoyaltyPointsToEarn: StateFlow<Int> = cartGrandTotal.map { total ->
        (total / 10.0).toInt() // 1 point per 10 Rs
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Payment Processing States
    private val _isProcessingPayment = MutableStateFlow(false)
    val isProcessingPayment: StateFlow<Boolean> = _isProcessingPayment.asStateFlow()

    private val _paymentSuccess = MutableStateFlow(false)
    val paymentSuccess: StateFlow<Boolean> = _paymentSuccess.asStateFlow()

    private val _checkoutError = MutableStateFlow<String?>(null)
    val checkoutError: StateFlow<String?> = _checkoutError.asStateFlow()

    // Navigation functions
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun selectRestaurant(restaurantId: String) {
        _selectedRestaurantId.value = restaurantId
        _currentScreen.value = "restaurant_detail"
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    // Repository Operations wrapping Coroutines
    fun toggleFavoriteRestaurant(restaurantId: String, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(restaurantId, isFav)
        }
    }

    fun addFoodToCart(item: FoodItemEntity, restaurantName: String) {
        viewModelScope.launch {
            repository.addToCart(item, restaurantName)
        }
    }

    fun updateCartItemQty(foodItemId: String, qty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(foodItemId, qty)
        }
    }

    fun removeCartItem(foodItemId: String) {
        viewModelScope.launch {
            repository.removeFromCart(foodItemId)
        }
    }

    fun clearActiveCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Razorpay Secured Simulation Checkout
    fun checkoutWithRazorpay(deliveryAddress: String) {
        viewModelScope.launch {
            _isProcessingPayment.value = true
            _checkoutError.value = null
            _paymentSuccess.value = false

            // Simulate server network call & Razorpay gateway overhead delay
            delay(2000)

            val currentCart = cartItems.value
            if (currentCart.isEmpty()) {
                _checkoutError.value = "Your cart is empty!"
                _isProcessingPayment.value = false
                return@launch
            }

            val rId = currentCart.first().restaurantId
            val rName = currentCart.first().restaurantName
            val total = cartGrandTotal.value
            val savings = cartCommissionSavings.value
            val points = cartLoyaltyPointsToEarn.value
            val rPayId = "pay_rzp_${UUID.randomUUID().toString().substring(0, 12)}"

            val order = repository.createOrder(
                restaurantId = rId,
                restaurantName = rName,
                totalAmount = total,
                deliveryAddress = deliveryAddress,
                razorpayPaymentId = rPayId,
                pointsEarned = points,
                savedCommission = savings
            )

            _paymentSuccess.value = true
            _isProcessingPayment.value = false

            // Keep the success screen/dialog visible briefly, then switch screen
            delay(1200)
            _paymentSuccess.value = false
            navigateTo("orders")
        }
    }

    fun redeemRewardCoupon(pointsCost: Int, discount: Double) {
        viewModelScope.launch {
            repository.redeemLoyaltyVoucher(pointsCost, discount)
        }
    }

    fun sendSupportChat(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendSupportMessage(text)
        }
    }

    fun loginWithEmailAndPassword(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val profile = repository.loyaltyProfile.first()
            if (profile != null && profile.email.equals(email, ignoreCase = true) && profile.password == password) {
                repository.updateProfile(profile.copy(isLoggedIn = true, hasCompletedProfile = true))
                _currentScreen.value = "explore"
                onResult(true, "Welcome back, ${profile.name}!")
            } else {
                onResult(false, "Invalid email or password. Feel free to sign up!")
            }
        }
    }

    fun loginWithOtp(phone: String, otp: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (otp != "123456" && otp != "654321") {
                onResult(false, "Invalid OTP. Use demo code '123456'!")
                return@launch
            }
            val profile = repository.loyaltyProfile.first()
            if (profile != null) {
                val updated = if (profile.phone == phone) profile else profile.copy(phone = phone)
                repository.updateProfile(updated.copy(isLoggedIn = true, hasCompletedProfile = true))
                _currentScreen.value = "explore"
                onResult(true, "Welcome back!")
            } else {
                onResult(false, "Phone number not found. Please sign up!")
            }
        }
    }

    fun signupNewUser(name: String, email: String, phone: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val profile = repository.loyaltyProfile.first() ?: LoyaltyProfileEntity()
            val newProfile = profile.copy(
                name = name,
                email = email,
                phone = phone,
                isLoggedIn = true,
                hasCompletedProfile = false // Must setup profile first!
            )
            repository.updateProfile(newProfile)
            _currentScreen.value = "setup_profile"
            onResult(true, "Account created! Let's set up your profile.")
        }
    }

    fun updateUserProfile(
        name: String,
        email: String,
        phone: String,
        homeAddress: String,
        workAddress: String,
        cardName: String,
        cardNo: String,
        upi: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val profile = repository.loyaltyProfile.first() ?: LoyaltyProfileEntity()
            val updated = profile.copy(
                name = name,
                email = email,
                phone = phone,
                homeAddress = homeAddress,
                workAddress = workAddress,
                savedCardName = cardName,
                savedCardNo = cardNo,
                savedUpi = upi,
                hasCompletedProfile = true,
                isLoggedIn = true
            )
            repository.updateProfile(updated)
            onResult(true, "Profile updated successfully!")
        }
    }

    fun createSupportTicket(subject: String, category: String, description: String) {
        viewModelScope.launch {
            repository.createSupportTicket(subject, category, description)
        }
    }

    fun updateTicketStatus(ticketId: String, status: String) {
        viewModelScope.launch {
            repository.updateTicketStatus(ticketId, status)
        }
    }

    fun logout() {
        viewModelScope.launch {
            val profile = repository.loyaltyProfile.first()
            if (profile != null) {
                repository.updateProfile(profile.copy(isLoggedIn = false))
            }
            _currentScreen.value = "explore"
        }
    }

    fun autoCaptureLocation(context: android.content.Context, onCaptured: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    onCaptured("Flat 101, Anand Nilayam, Madhapur, Hyderabad, Telangana, 500081 (Mocked - Permission Required)")
                    return@launch
                }

                val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
                if (locationManager == null) {
                    onCaptured("Flat 101, Anand Nilayam, Madhapur, Hyderabad, Telangana, 500081")
                    return@launch
                }

                val providers = locationManager.getProviders(true)
                var bestLocation: android.location.Location? = null
                for (provider in providers) {
                    val loc = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                        bestLocation = loc
                    }
                }

                if (bestLocation != null) {
                    val lat = bestLocation.latitude
                    val lon = bestLocation.longitude
                    val geocoder = android.location.Geocoder(context, java.util.Locale.ENGLISH)
                    var addressText = ""
                    try {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            addressText = addr.getAddressLine(0) ?: "${addr.locality}, ${addr.adminArea}, India"
                        }
                    } catch (e: Exception) {
                        addressText = "Building 12, Cyber Gateway, Madhapur, Hyderabad, Telangana, 500081"
                    }
                    if (addressText.isBlank()) {
                        addressText = "Building 12, Cyber Gateway, Madhapur, Hyderabad, Telangana, 500081"
                    }
                    onCaptured(addressText)
                } else {
                    onCaptured("Plot 55, Image Gardens Road, Madhapur, Hyderabad, Telangana, 500081")
                }
            } catch (e: Exception) {
                onCaptured("Plot 55, Image Gardens Road, Madhapur, Hyderabad, Telangana, 500081")
            }
        }
    }

    fun triggerBulkSync() {
        viewModelScope.launch {
            val profile = repository.loyaltyProfile.first()
            val orders = repository.orders.first()
            val tickets = repository.supportTickets.first()
            val messages = repository.supportMessages.first()
            FirebaseSyncManager.bulkSyncOfflineData(profile, orders, tickets, messages)
        }
    }
}
