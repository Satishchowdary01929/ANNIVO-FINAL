package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entities.LoyaltyProfileEntity
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.SupportMessageEntity
import com.example.data.local.entities.SupportTicketEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object FirebaseSyncManager {
    private const val TAG = "FirebaseSyncManager"
    private const val PREFS_NAME = "firebase_sync_prefs"
    private const val KEY_API_KEY = "firebase_api_key"
    private const val KEY_PROJECT_ID = "firebase_project_id"
    private const val KEY_APP_ID = "firebase_app_id"

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _syncStatus = MutableStateFlow("Disconnected")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _activeProjectId = MutableStateFlow("")
    val activeProjectId: StateFlow<String> = _activeProjectId.asStateFlow()

    private val _syncLog = MutableStateFlow<List<String>>(emptyList())
    val syncLog: StateFlow<List<String>> = _syncLog.asStateFlow()

    var firestore: FirebaseFirestore? = null
        private set

    var auth: FirebaseAuth? = null
        private set

    fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        _syncLog.value = (listOf("[$timestamp] $message") + _syncLog.value).take(50)
    }

    // Try to auto-initialize on startup using BuildConfig or persisted credentials
    fun autoInitialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedApiKey = prefs.getString(KEY_API_KEY, "") ?: ""
        val savedProjectId = prefs.getString(KEY_PROJECT_ID, "") ?: ""
        val savedAppId = prefs.getString(KEY_APP_ID, "") ?: ""

        // Prioritize BuildConfig (from AI Studio Secrets panel via .env)
        val buildApiKey = try { BuildConfig.FIREBASE_API_KEY } catch (e: Exception) { "" }
        val buildProjectId = try { BuildConfig.FIREBASE_PROJECT_ID } catch (e: Exception) { "" }
        val buildAppId = try { BuildConfig.FIREBASE_APP_ID } catch (e: Exception) { "" }

        // Default Firebase values provided by Google Service config
        val defaultApiKey = "AIzaSyDfEFirAG1r2UIllUa588ECpW3AQN4H6cI"
        val defaultProjectId = "annivo-61d7a"
        val defaultAppId = "1:350936320240:android:90930fcec17037ef349a35"

        fun isPlaceholder(s: String) = s.isBlank() || s.contains("YOUR_") || s.contains("Placeholder")

        val finalApiKey = when {
            !isPlaceholder(buildApiKey) -> buildApiKey
            !isPlaceholder(savedApiKey) -> savedApiKey
            else -> defaultApiKey
        }
        val finalProjectId = when {
            !isPlaceholder(buildProjectId) -> buildProjectId
            !isPlaceholder(savedProjectId) -> savedProjectId
            else -> defaultProjectId
        }
        val finalAppId = when {
            !isPlaceholder(buildAppId) -> buildAppId
            !isPlaceholder(savedAppId) -> savedAppId
            else -> defaultAppId
        }

        if (finalApiKey.isNotBlank() && finalProjectId.isNotBlank() && finalAppId.isNotBlank()) {
            initializeDynamically(context, finalApiKey, finalProjectId, finalAppId, isAuto = true) { success, msg ->
                Log.d(TAG, "Auto-init result: success=$success, msg=$msg")
            }
        } else {
            addLog("Firebase is not configured. Ready for manual or secrets configuration.")
        }
    }

    fun initializeDynamically(
        context: Context,
        apiKey: String,
        projectId: String,
        appId: String,
        isAuto: Boolean = false,
        onResult: (Boolean, String) -> Unit
    ) {
        if (apiKey.isBlank() || projectId.isBlank() || appId.isBlank()) {
            onResult(false, "All Firebase fields are required for initialization!")
            return
        }

        try {
            // Delete previous default instance if any to allow re-initialization
            try {
                val existingApp = FirebaseApp.getInstance()
                existingApp.delete()
            } catch (e: Exception) {
                // Not initialized, fine
            }

            val options = FirebaseOptions.Builder()
                .setApiKey(apiKey)
                .setProjectId(projectId)
                .setApplicationId(appId)
                .build()

            val app = FirebaseApp.initializeApp(context, options)
            firestore = FirebaseFirestore.getInstance(app)
            auth = FirebaseAuth.getInstance(app)

            _isInitialized.value = true
            _activeProjectId.value = projectId
            _syncStatus.value = "Connected"

            // Save credentials to local SharedPrefs for persistence (only if manual input)
            if (!isAuto) {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                    .putString(KEY_API_KEY, apiKey)
                    .putString(KEY_PROJECT_ID, projectId)
                    .putString(KEY_APP_ID, appId)
                    .apply()
            }

            addLog("Connected to Firebase Project: $projectId")
            onResult(true, "Firebase successfully initialized! Connected to: $projectId")
        } catch (e: Exception) {
            _isInitialized.value = false
            _syncStatus.value = "Error"
            addLog("Failed to initialize Firebase: ${e.localizedMessage}")
            onResult(false, "Failed to initialize Firebase: ${e.localizedMessage}")
        }
    }

    fun disconnect(context: Context) {
        try {
            val existingApp = FirebaseApp.getInstance()
            existingApp.delete()
        } catch (e: Exception) {}

        firestore = null
        auth = null
        _isInitialized.value = false
        _activeProjectId.value = ""
        _syncStatus.value = "Disconnected"

        // Clear saved prefs
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        addLog("Firebase disconnected and local credentials cleared.")
    }

    // ==========================================
    // BACKEND SYNC OPERATIONS (FIRESTORE)
    // ==========================================

    fun syncProfile(profile: LoyaltyProfileEntity) {
        val fs = firestore ?: return
        val docId = profile.id.ifBlank { "default_user" }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = mapOf(
                    "id" to docId,
                    "name" to profile.name,
                    "email" to profile.email,
                    "phone" to profile.phone,
                    "totalPoints" to profile.totalPoints,
                    "tier" to profile.tier,
                    "totalSavedCommissionRs" to profile.totalSavedCommissionRs,
                    "homeAddress" to profile.homeAddress,
                    "workAddress" to profile.workAddress,
                    "savedCardName" to profile.savedCardName,
                    "savedCardNo" to profile.savedCardNo,
                    "savedUpi" to profile.savedUpi,
                    "isLoggedIn" to profile.isLoggedIn,
                    "hasCompletedProfile" to profile.hasCompletedProfile,
                    "lastUpdated" to System.currentTimeMillis()
                )

                fs.collection("profiles").document(docId).set(data)
                addLog("Synced Profile for '${profile.name}' to Firestore.")
            } catch (e: Exception) {
                addLog("Profile Sync Error: ${e.localizedMessage}")
            }
        }
    }

    fun syncOrder(order: OrderEntity) {
        val fs = firestore ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = mapOf(
                    "id" to order.id,
                    "restaurantId" to order.restaurantId,
                    "restaurantName" to order.restaurantName,
                    "timestamp" to order.timestamp,
                    "totalAmount" to order.totalAmount,
                    "status" to order.status,
                    "deliveryProgress" to order.deliveryProgress,
                    "loyaltyPointsEarned" to order.loyaltyPointsEarned,
                    "razorpayPaymentId" to order.razorpayPaymentId,
                    "deliveryAddress" to order.deliveryAddress
                )

                fs.collection("orders").document(order.id).set(data)
                addLog("Synced Order '${order.id}' to Firestore.")
            } catch (e: Exception) {
                addLog("Order Sync Error: ${e.localizedMessage}")
            }
        }
    }

    fun syncSupportTicket(ticket: SupportTicketEntity) {
        val fs = firestore ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = mapOf(
                    "id" to ticket.id,
                    "subject" to ticket.subject,
                    "category" to ticket.category,
                    "status" to ticket.status,
                    "description" to ticket.description,
                    "timestamp" to ticket.timestamp
                )

                fs.collection("tickets").document(ticket.id).set(data)
                addLog("Synced Ticket '${ticket.id}' to Firestore.")
            } catch (e: Exception) {
                addLog("Ticket Sync Error: ${e.localizedMessage}")
            }
        }
    }

    fun syncSupportMessage(message: SupportMessageEntity) {
        val fs = firestore ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val docId = "msg_${message.timestamp}_${(1000..9999).random()}"
                val data = mapOf(
                    "id" to docId,
                    "sender" to message.sender,
                    "text" to message.text,
                    "timestamp" to message.timestamp
                )

                fs.collection("support_messages").document(docId).set(data)
                addLog("Synced Chat Message to Firestore.")
            } catch (e: Exception) {
                addLog("Message Sync Error: ${e.localizedMessage}")
            }
        }
    }

    // Bulk sync existing database elements upon successful connection
    fun bulkSyncOfflineData(
        profile: LoyaltyProfileEntity?,
        ordersList: List<OrderEntity>,
        ticketsList: List<SupportTicketEntity>,
        messagesList: List<SupportMessageEntity>
    ) {
        if (firestore == null) return
        
        CoroutineScope(Dispatchers.IO).launch {
            addLog("Starting bulk synchronization of offline database...")
            
            profile?.let { syncProfile(it) }
            ordersList.forEach { syncOrder(it) }
            ticketsList.forEach { syncSupportTicket(it) }
            
            // Sync last 10 messages to avoid flood
            messagesList.takeLast(10).forEach { syncSupportMessage(it) }
            
            addLog("Offline data synchronization completed successfully.")
        }
    }
}
