package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cuisine: String,
    val rating: Float,
    val deliveryTimeMin: Int,
    val costForTwo: Int,
    val distanceKm: Float,
    val isZeroCommission: Boolean = true, // ANNIVO standard: 100% Commission-free for life
    val isFavorite: Boolean = false,
    val imageUrl: String = ""
)

@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey val id: String,
    val restaurantId: String,
    val name: String,
    val price: Double,
    val description: String,
    val isVeg: Boolean,
    val rating: Float,
    val category: String, // Biryani, Pizza, Burger, Indian, Dessert etc.
    val imageUrl: String = ""
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val foodItemId: String,
    val restaurantId: String,
    val restaurantName: String,
    val name: String,
    val price: Double,
    val quantity: Int
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String, // ANV-XXXXXX
    val restaurantId: String,
    val restaurantName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val status: String, // "CONFIRMED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED"
    val deliveryProgress: Float, // 0.0f to 1.0f
    val loyaltyPointsEarned: Int,
    val razorpayPaymentId: String,
    val deliveryAddress: String,
    val isRated: Boolean = false
)

@Entity(tableName = "loyalty_profile")
data class LoyaltyProfileEntity(
    @PrimaryKey val id: String = "user_profile",
    val totalPoints: Int = 120, // Start with some bonus sign-up points!
    val tier: String = "Silver", // Bronze, Silver, Gold, Platinum
    val totalSavedCommissionRs: Double = 345.0, // The money the restaurants saved because of user's orders on ANNIVO
    
    // User Profile Information
    val name: String = "Satish Chowdary",
    val email: String = "satishchowdary1477@gmail.com",
    val password: String = "password123", // Stored locally for simple auth demo
    val phone: String = "9876543210",
    val homeAddress: String = "Flat 402, Sai Enclave, Madhapur, Hyderabad, Telangana, 500081",
    val workAddress: String = "Building 3, Mindspace IT Park, Hitec City, Hyderabad, Telangana, 500081",
    
    // Payment Options
    val savedCardName: String = "Satish Chowdary",
    val savedCardNo: String = "**** **** **** 4111",
    val savedUpi: String = "satish@okaxis",
    
    // Auth Session State
    val isLoggedIn: Boolean = false, // Start false so the user is forced to authenticate first
    val hasCompletedProfile: Boolean = false // Start false so signup users must complete their profile first
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String, // TKT-XXXXXX
    val subject: String,
    val category: String, // "Order Delayed", "Payment Failed", "Refund Status", "Other"
    val status: String, // "OPEN", "RESOLVED"
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_messages")
data class SupportMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "agent"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
