package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.daos.*
import com.example.data.local.entities.*

@Database(
    entities = [
        RestaurantEntity::class,
        FoodItemEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        LoyaltyProfileEntity::class,
        SupportMessageEntity::class,
        SupportTicketEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun orderDao(): OrderDao
    abstract fun loyaltyProfileDao(): LoyaltyProfileDao
    abstract fun supportMessageDao(): SupportMessageDao
    abstract fun supportTicketDao(): SupportTicketDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "annivo_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
