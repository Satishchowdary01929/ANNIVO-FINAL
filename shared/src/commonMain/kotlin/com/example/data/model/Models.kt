package com.example.data.model

data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val rating: Float,
    val deliveryTimeMin: Int,
    val costForTwo: Int,
    val distanceKm: Float,
    val isZeroCommission: Boolean = true,
    val isFavorite: Boolean = false,
    val imageUrl: String = ""
)

data class FoodItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val price: Double,
    val description: String,
    val isVeg: Boolean,
    val rating: Float,
    val category: String,
    val imageUrl: String = ""
)

data class CartItem(
    val foodItemId: String,
    val restaurantId: String,
    val restaurantName: String,
    val name: String,
    val price: Double,
    val quantity: Int
)

data class Order(
    val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val timestamp: Long,
    val totalAmount: Double,
    val status: String,
    val deliveryProgress: Float,
    val loyaltyPointsEarned: Int,
    val razorpayPaymentId: String,
    val deliveryAddress: String,
    val isRated: Boolean = false
)

data class LoyaltyProfile(
    val id: String = "user_profile",
    val totalPoints: Int = 120,
    val tier: String = "Silver",
    val totalSavedCommissionRs: Double = 345.0,
    val name: String = "Satish Chowdary",
    val email: String = "satishchowdary1477@gmail.com",
    val password: String = "password123",
    val phone: String = "9876543210",
    val homeAddress: String = "Flat 402, Sai Enclave, Madhapur, Hyderabad, Telangana, 500081",
    val workAddress: String = "Building 3, Mindspace IT Park, Hitec City, Hyderabad, Telangana, 500081",
    val savedCardName: String = "Satish Chowdary",
    val savedCardNo: String = "**** **** **** 4111",
    val savedUpi: String = "satish@okaxis",
    val isLoggedIn: Boolean = false,
    val hasCompletedProfile: Boolean = false
)

data class SupportTicket(
    val id: String,
    val subject: String,
    val category: String,
    val status: String,
    val description: String,
    val timestamp: Long
)

data class SupportMessage(
    val id: Int = 0,
    val sender: String,
    val text: String,
    val timestamp: Long
)
