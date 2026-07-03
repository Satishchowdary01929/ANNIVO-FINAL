package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class FoodRepository(private val db: AppDatabase) {

    private val restaurantDao = db.restaurantDao()
    private val foodItemDao = db.foodItemDao()
    private val cartItemDao = db.cartItemDao()
    private val orderDao = db.orderDao()
    private val loyaltyProfileDao = db.loyaltyProfileDao()
    private val supportMessageDao = db.supportMessageDao()
    private val supportTicketDao = db.supportTicketDao()
 
    val restaurants: Flow<List<RestaurantEntity>> = restaurantDao.getAllRestaurants()
    val cartItems: Flow<List<CartItemEntity>> = cartItemDao.getCartItems()
    val orders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val loyaltyProfile: Flow<LoyaltyProfileEntity?> = loyaltyProfileDao.getLoyaltyProfile()
    val supportMessages: Flow<List<SupportMessageEntity>> = supportMessageDao.getAllMessages()
    val supportTickets: Flow<List<SupportTicketEntity>> = supportTicketDao.getAllTickets()

    suspend fun toggleFavorite(restaurantId: String, isFav: Boolean) {
        restaurantDao.updateFavorite(restaurantId, isFav)
    }

    fun getFoodItemsForRestaurant(restaurantId: String): Flow<List<FoodItemEntity>> {
        return foodItemDao.getFoodItemsForRestaurant(restaurantId)
    }

    suspend fun addToCart(item: FoodItemEntity, restaurantName: String, qty: Int = 1) {
        val existing = cartItems.first().find { it.foodItemId == item.id }
        if (existing != null) {
            cartItemDao.updateCartItemQuantity(item.id, existing.quantity + qty)
        } else {
            cartItemDao.insertCartItem(
                CartItemEntity(
                    foodItemId = item.id,
                    restaurantId = item.restaurantId,
                    restaurantName = restaurantName,
                    name = item.name,
                    price = item.price,
                    quantity = qty
                )
            )
        }
    }

    suspend fun updateCartQuantity(foodItemId: String, quantity: Int) {
        if (quantity <= 0) {
            cartItemDao.deleteCartItem(foodItemId)
        } else {
            cartItemDao.updateCartItemQuantity(foodItemId, quantity)
        }
    }

    suspend fun removeFromCart(foodItemId: String) {
        cartItemDao.deleteCartItem(foodItemId)
    }

    suspend fun clearCart() {
        cartItemDao.clearCart()
    }

    // Seeds initial popular restaurants & menu items if DB is empty
    suspend fun seedInitialDataIfNecessary() {
        val currentRestaurants = restaurantDao.getAllRestaurants().first()
        if (currentRestaurants.isNotEmpty()) return

        val initialRestaurants = listOf(
            RestaurantEntity(
                id = "rest_1",
                name = "Royal Biryani House",
                cuisine = "Biryani, Kebabs & Mughal",
                rating = 4.8f,
                deliveryTimeMin = 25,
                costForTwo = 400,
                distanceKm = 2.4f,
                imageUrl = "biryani"
            ),
            RestaurantEntity(
                id = "rest_2",
                name = "Saravana Bhavan",
                cuisine = "South Indian Breakfast & Filter Coffee",
                rating = 4.7f,
                deliveryTimeMin = 15,
                costForTwo = 250,
                distanceKm = 1.2f,
                imageUrl = "south_indian"
            ),
            RestaurantEntity(
                id = "rest_3",
                name = "Pind Balluchi",
                cuisine = "North Indian, Punjabi Curry & Tandoor",
                rating = 4.5f,
                deliveryTimeMin = 30,
                costForTwo = 500,
                distanceKm = 4.1f,
                imageUrl = "north_indian"
            ),
            RestaurantEntity(
                id = "rest_4",
                name = "The Pizza Central",
                cuisine = "Gourmet Woodfired Pizzas & Pastas",
                rating = 4.6f,
                deliveryTimeMin = 35,
                costForTwo = 600,
                distanceKm = 3.8f,
                imageUrl = "pizza"
            ),
            RestaurantEntity(
                id = "rest_5",
                name = "Cream & Fudge",
                cuisine = "Desserts, Premium Ice Creams & Waffles",
                rating = 4.9f,
                deliveryTimeMin = 20,
                costForTwo = 300,
                distanceKm = 1.9f,
                imageUrl = "dessert"
            )
        )

        val initialFoodItems = listOf(
            // Royal Biryani House
            FoodItemEntity(
                id = "food_101",
                restaurantId = "rest_1",
                name = "Special Hyderabadi Chicken Biryani",
                price = 280.0,
                description = "Slow-cooked fragrant basmati rice with succulent chicken marinated in traditional spices.",
                isVeg = false,
                rating = 4.9f,
                category = "Biryani"
            ),
            FoodItemEntity(
                id = "food_102",
                restaurantId = "rest_1",
                name = "Veg Dum Biryani",
                price = 220.0,
                description = "Delectable vegetable medley layered with basmati rice, saffron and fresh mint.",
                isVeg = true,
                rating = 4.6f,
                category = "Biryani"
            ),
            FoodItemEntity(
                id = "food_103",
                restaurantId = "rest_1",
                name = "Double Ka Meetha",
                price = 90.0,
                description = "Classic Hyderabadi bread pudding soaked in saffron-infused milk and dry fruits.",
                isVeg = true,
                rating = 4.7f,
                category = "Dessert"
            ),
            // Saravana Bhavan
            FoodItemEntity(
                id = "food_201",
                restaurantId = "rest_2",
                name = "Ghee Roast Masala Dosa",
                price = 120.0,
                description = "Crispy golden crepe brushed with pure ghee, stuffed with potato masala. Served with 3 chutneys & piping hot sambar.",
                isVeg = true,
                rating = 4.8f,
                category = "South Indian"
            ),
            FoodItemEntity(
                id = "food_202",
                restaurantId = "rest_2",
                name = "Steamed Idli Combo (2 Pcs)",
                price = 80.0,
                description = "Soft, fluffy steamed rice cakes served with traditional sambar and coconut chutney.",
                isVeg = true,
                rating = 4.5f,
                category = "South Indian"
            ),
            FoodItemEntity(
                id = "food_203",
                restaurantId = "rest_2",
                name = "Filter Coffee",
                price = 50.0,
                description = "Authentic chicory-blend filter coffee brewed fresh in traditional brass tumbler.",
                isVeg = true,
                rating = 4.9f,
                category = "Drinks"
            ),
            // Pind Balluchi
            FoodItemEntity(
                id = "food_301",
                restaurantId = "rest_3",
                name = "Butter Chicken Special",
                price = 320.0,
                description = "Tender tandoori chicken cooked in a rich, creamy tomato butter gravy.",
                isVeg = false,
                rating = 4.8f,
                category = "North Indian"
            ),
            FoodItemEntity(
                id = "food_302",
                restaurantId = "rest_3",
                name = "Paneer Butter Masala",
                price = 260.0,
                description = "Fresh cottage cheese cubes in a rich, mild sweet creamy gravy.",
                isVeg = true,
                rating = 4.6f,
                category = "North Indian"
            ),
            FoodItemEntity(
                id = "food_303",
                restaurantId = "rest_3",
                name = "Garlic Naan (1 Pc)",
                price = 60.0,
                description = "Tandoor baked flatbread flavored with fresh garlic and butter.",
                isVeg = true,
                rating = 4.4f,
                category = "North Indian"
            ),
            // The Pizza Central
            FoodItemEntity(
                id = "food_401",
                restaurantId = "rest_4",
                name = "Farmhouse Special Pizza (10\")",
                price = 380.0,
                description = "Overloaded with mozzarella, bell peppers, fresh tomatoes, mushrooms, and sweet corn.",
                isVeg = true,
                rating = 4.7f,
                category = "Pizza"
            ),
            FoodItemEntity(
                id = "food_402",
                restaurantId = "rest_4",
                name = "Smoked Chicken & BBQ Pizza (10\")",
                price = 420.0,
                description = "Juicy smoked chicken breast, sweet BBQ sauce drizzle, and fresh red onions.",
                isVeg = false,
                rating = 4.8f,
                category = "Pizza"
            ),
            // Cream & Fudge
            FoodItemEntity(
                id = "food_501",
                restaurantId = "rest_5",
                name = "Death by Chocolate",
                price = 180.0,
                description = "Rich chocolate cake topped with vanilla scoop, hot fudge, cherries, and chocolate chips.",
                isVeg = true,
                rating = 4.9f,
                category = "Dessert"
            ),
            FoodItemEntity(
                id = "food_502",
                restaurantId = "rest_5",
                name = "Mango Alphonso Tub",
                price = 150.0,
                description = "Creamy pure Alphonso mango pulp premium ice cream.",
                isVeg = true,
                rating = 4.7f,
                category = "Dessert"
            )
        )

        restaurantDao.insertRestaurants(initialRestaurants)
        foodItemDao.insertFoodItems(initialFoodItems)

        // Seed default loyalty profile
        if (loyaltyProfileDao.getLoyaltyProfile().first() == null) {
            loyaltyProfileDao.insertOrUpdateProfile(LoyaltyProfileEntity())
        }

        // Seed initial support tickets
        if (supportTicketDao.getAllTickets().first().isEmpty()) {
            supportTicketDao.insertTicket(
                SupportTicketEntity(
                    id = "TKT-824051",
                    subject = "Inquiry regarding Loyalty Tier perks",
                    category = "Other",
                    status = "RESOLVED",
                    description = "I wanted to understand how to upgrade from Silver to Gold and what are the direct benefits.",
                    timestamp = System.currentTimeMillis() - 86400000 * 2 // 2 days ago
                )
            )
            supportTicketDao.insertTicket(
                SupportTicketEntity(
                    id = "TKT-194825",
                    subject = "Delayed delivery of Biryani from Punjabi Dhaba",
                    category = "Order Delayed",
                    status = "OPEN",
                    description = "My order is delayed by 15 mins. Please track the courier.",
                    timestamp = System.currentTimeMillis() - 3600000 // 1 hour ago
                )
            )
        }

        // Seed initial greeting message
        if (supportMessageDao.getAllMessages().first().isEmpty()) {
            supportMessageDao.insertMessage(
                SupportMessageEntity(
                    sender = "agent",
                    text = "Namaste! Welcome to ANNIVO Support. 🙏 We are here 24/7. Ask us about your orders, delivery status, or Razorpay transactions!",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Creates an order simulating payment and starting active tracking
    suspend fun createOrder(
        restaurantId: String,
        restaurantName: String,
        totalAmount: Double,
        deliveryAddress: String,
        razorpayPaymentId: String,
        pointsEarned: Int,
        savedCommission: Double
    ): OrderEntity {
        val orderId = "ANV-${(100000..999999).random()}"
        val newOrder = OrderEntity(
            id = orderId,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            timestamp = System.currentTimeMillis(),
            totalAmount = totalAmount,
            status = "CONFIRMED",
            deliveryProgress = 0.15f,
            loyaltyPointsEarned = pointsEarned,
            razorpayPaymentId = razorpayPaymentId,
            deliveryAddress = deliveryAddress
        )

        // Insert order
        orderDao.insertOrder(newOrder)

        // Clear cart
        cartItemDao.clearCart()

        // Update Loyalty profile (Adding points and summing commission saved)
        val profile = loyaltyProfile.first() ?: LoyaltyProfileEntity()
        val newPoints = profile.totalPoints + pointsEarned
        val newSavedCommission = profile.totalSavedCommissionRs + savedCommission
        val newTier = when {
            newPoints >= 1000 -> "Platinum"
            newPoints >= 500 -> "Gold"
            newPoints >= 250 -> "Silver"
            else -> "Bronze"
        }
        loyaltyProfileDao.insertOrUpdateProfile(
            profile.copy(
                totalPoints = newPoints,
                tier = newTier,
                totalSavedCommissionRs = newSavedCommission
            )
        )

        // Trigger asynchronous real-time simulation updates
        simulateOrderProgress(orderId)

        return newOrder
    }

    private fun simulateOrderProgress(orderId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // CONFIRMED at 0.15f -> PREPARING after 8 seconds
            delay(8000)
            orderDao.updateOrderStatus(orderId, "PREPARING", 0.45f)

            // PREPARING at 0.45f -> OUT_FOR_DELIVERY after 12 seconds
            delay(12000)
            orderDao.updateOrderStatus(orderId, "OUT_FOR_DELIVERY", 0.75f)

            // OUT_FOR_DELIVERY at 0.75f -> DELIVERED after 12 seconds
            delay(12000)
            orderDao.updateOrderStatus(orderId, "DELIVERED", 1.0f)
        }
    }

    suspend fun redeemLoyaltyVoucher(costPoints: Int, discountAmount: Double) {
        val profile = loyaltyProfile.first() ?: return
        if (profile.totalPoints >= costPoints) {
            val updatedPoints = profile.totalPoints - costPoints
            val updatedTier = when {
                updatedPoints >= 1000 -> "Platinum"
                updatedPoints >= 500 -> "Gold"
                updatedPoints >= 250 -> "Silver"
                else -> "Bronze"
            }
            loyaltyProfileDao.insertOrUpdateProfile(
                profile.copy(totalPoints = updatedPoints, tier = updatedTier)
            )
        }
    }

    suspend fun sendSupportMessage(text: String) {
        // Insert user message
        supportMessageDao.insertMessage(
            SupportMessageEntity(
                sender = "user",
                text = text,
                timestamp = System.currentTimeMillis()
            )
        )

        // Simulate support bot response
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            val replyText = generateSupportReply(text)
            supportMessageDao.insertMessage(
                SupportMessageEntity(
                    sender = "agent",
                    text = replyText,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private fun generateSupportReply(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("refund") || q.contains("cancel") -> {
                "Since ANNIVO utilizes secure Razorpay integration, any canceled orders are instantly refunded! Your refund (if applicable) will reflect in your payment source within 2-3 business hours. Can I assist you with a specific order ID?"
            }
            q.contains("charge") || q.contains("delivery") || q.contains("fee") -> {
                "ANNIVO keeps delivery charges extremely low and transparent! Delivery is completely FREE on all orders above ₹300. For orders below ₹300, a nominal charge of ₹30 is applied to support our local delivery fleet."
            }
            q.contains("razorpay") || q.contains("payment") || q.contains("fail") -> {
                "Our payments are fully secured via the Razorpay gateway. We support UPI (GPay, PhonePe), credit cards, debit cards, and netbanking. If a transaction fails but money is debited, Razorpay auto-reverses it within 24 hours. Your transaction is completely secure!"
            }
            q.contains("tracking") || q.contains("where is my food") || q.contains("delay") || q.contains("order") -> {
                "You can track your active deliveries in real-time under the 'Orders' tab. You'll see the exact step-by-step courier progress (Confirmed ➔ Preparing ➔ Out for Delivery ➔ Arrived). If your driver is delayed, please hold tight, we make sure they drive safely!"
            }
            q.contains("rewards") || q.contains("loyalty") || q.contains("points") -> {
                "Every ₹10 spent on ANNIVO earns you 1 Loyalty Point! You can view and redeem your points directly in the Rewards tab for exciting food discount vouchers and free item coupons."
            }
            else -> {
                "Thanks for reaching out! A human ANNIVO support agent is always online. Your query is being logged, and we'll resolve any issues instantly. For immediate food tracking or Razorpay inquiries, explore our main navigation tabs! 🍔🏍️"
            }
        }
    }

    suspend fun createSupportTicket(subject: String, category: String, description: String): SupportTicketEntity {
        val ticketId = "TKT-${(100000..999999).random()}"
        val ticket = SupportTicketEntity(
            id = ticketId,
            subject = subject,
            category = category,
            status = "OPEN",
            description = description,
            timestamp = System.currentTimeMillis()
        )
        supportTicketDao.insertTicket(ticket)
        
        // Also insert an automated support response related to the ticket
        supportMessageDao.insertMessage(
            SupportMessageEntity(
                sender = "agent",
                text = "Ticket $ticketId has been created successfully under category '$category'. Our support team is reviewing your issue regarding: \"$subject\". We will update you here!",
                timestamp = System.currentTimeMillis()
            )
        )
        return ticket
    }

    suspend fun updateTicketStatus(ticketId: String, status: String) {
        supportTicketDao.updateTicketStatus(ticketId, status)
    }

    suspend fun updateProfile(profile: LoyaltyProfileEntity) {
        loyaltyProfileDao.insertOrUpdateProfile(profile)
    }
}
