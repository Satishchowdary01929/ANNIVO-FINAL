package com.example.data.local.daos

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE id = :id")
    fun getRestaurantById(id: String): Flow<RestaurantEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<RestaurantEntity>)

    @Query("UPDATE restaurants SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: String, isFav: Boolean)
}

@Dao
interface FoodItemDao {
    @Query("SELECT * FROM food_items WHERE restaurantId = :restaurantId")
    fun getFoodItemsForRestaurant(restaurantId: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE category = :category")
    fun getFoodItemsByCategory(category: String): Flow<List<FoodItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItemEntity>)
}

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE foodItemId = :foodItemId")
    suspend fun updateCartItemQuantity(foodItemId: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE foodItemId = :foodItemId")
    suspend fun deleteCartItem(foodItemId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: String): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status, deliveryProgress = :progress WHERE id = :id")
    suspend fun updateOrderStatus(id: String, status: String, progress: Float)
}

@Dao
interface LoyaltyProfileDao {
    @Query("SELECT * FROM loyalty_profile WHERE id = 'user_profile'")
    fun getLoyaltyProfile(): Flow<LoyaltyProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: LoyaltyProfileEntity)
}

@Dao
interface SupportMessageDao {
    @Query("SELECT * FROM support_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<SupportMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: SupportMessageEntity)

    @Query("DELETE FROM support_messages")
    suspend fun clearSupportHistory()
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY timestamp DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Query("UPDATE support_tickets SET status = :status WHERE id = :id")
    suspend fun updateTicketStatus(id: String, status: String)

    @Query("DELETE FROM support_tickets")
    suspend fun clearSupportTickets()
}

