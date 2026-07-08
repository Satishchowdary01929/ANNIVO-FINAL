package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.*
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.ui.viewmodel.FoodViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// 1. MAIN NAVIGATION ENTRY POINT
// ==========================================
@Composable
fun MainAppScreen(viewModel: FoodViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val loyaltyProfile by viewModel.loyaltyProfile.collectAsState()
    val cartCount by viewModel.cartItems.map { it.sumOf { item -> item.quantity } }.collectAsState(0)
    val headsUpMessage by viewModel.headsUpMessage.collectAsState()

    val isLoggedIn = loyaltyProfile?.isLoggedIn == true
    val hasCompletedProfile = loyaltyProfile?.hasCompletedProfile == true

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isLoggedIn) {
            AuthScreen(viewModel = viewModel)
        } else if (!hasCompletedProfile) {
            SetupProfileScreen(viewModel = viewModel)
        } else {
            Scaffold(
            bottomBar = {
                NavigationBar(
                    tonalElevation = 8.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "explore" || currentScreen == "restaurant_detail" || currentScreen == "profile",
                        onClick = { viewModel.navigateTo("explore") },
                        icon = { Icon(Icons.Filled.Storefront, contentDescription = "Restaurants") },
                        label = { Text("Eats", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SaffronOrange,
                            selectedTextColor = SaffronOrange,
                            indicatorColor = SaffronLight.copy(alpha = 0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == "cart",
                        onClick = { viewModel.navigateTo("cart") },
                        icon = {
                            BadgedBox(badge = {
                                if (cartCount > 0) {
                                    Badge(containerColor = SaffronOrange) {
                                        Text(cartCount.toString(), color = Color.White)
                                    }
                                }
                            }) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
                            }
                        },
                        label = { Text("Cart", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SaffronOrange,
                            selectedTextColor = SaffronOrange,
                            indicatorColor = SaffronLight.copy(alpha = 0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == "orders",
                        onClick = { viewModel.navigateTo("orders") },
                        icon = { Icon(Icons.Filled.DirectionsRun, contentDescription = "Orders") },
                        label = { Text("Tracking", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SaffronOrange,
                            selectedTextColor = SaffronOrange,
                            indicatorColor = SaffronLight.copy(alpha = 0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == "rewards",
                        onClick = { viewModel.navigateTo("rewards") },
                        icon = { Icon(Icons.Filled.CardGiftcard, contentDescription = "Rewards") },
                        label = { Text("Rewards", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SaffronOrange,
                            selectedTextColor = SaffronOrange,
                            indicatorColor = SaffronLight.copy(alpha = 0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == "support",
                        onClick = { viewModel.navigateTo("support") },
                        icon = { Icon(Icons.Filled.SupportAgent, contentDescription = "Support") },
                        label = { Text("Support", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SaffronOrange,
                            selectedTextColor = SaffronOrange,
                            indicatorColor = SaffronLight.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "ScreenTransition"
                ) { targetScreen ->
                    when (targetScreen) {
                        "explore" -> ExploreScreen(viewModel)
                        "restaurant_detail" -> RestaurantDetailScreen(viewModel)
                        "cart" -> CartScreen(viewModel)
                        "orders" -> OrdersScreen(viewModel)
                        "rewards" -> RewardsScreen(viewModel)
                        "support" -> SupportScreen(viewModel)
                        "profile" -> ProfileSettingsScreen(viewModel)
                    }
                }
            }
        }
    }

    // Heads-up Notification Banner overlay sliding down from top
    headsUpMessage?.let { otp ->
        HeadsUpNotificationBanner(otp = otp) {
            viewModel.dismissHeadsUpMessage()
        }
    }
}
}

@Composable
fun HeadsUpNotificationBanner(otp: String, onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(otp) {
        visible = true
        delay(6000) // Keep visible for 6 seconds
        visible = false
        delay(300) // Let exit animation finish
        onDismiss()
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)) // Sleek Dark Charcoal
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular SMS message icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF2E6FF2), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sms,
                        contentDescription = "SMS",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MESSAGES • Just Now",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E6FF2),
                            letterSpacing = 1.sp
                        )
                        IconButton(
                            onClick = {
                                visible = false
                                onDismiss()
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ANNIVO Secure login verification code:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = otp,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. EXPLORE RESTAURANTS SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(viewModel: FoodViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredList by viewModel.filteredRestaurants.collectAsState()
    val loyaltyProfile by viewModel.loyaltyProfile.collectAsState()

    val categories = listOf(
        Pair("All", "🍔"),
        Pair("Biryani", "🍲"),
        Pair("South Indian", "🫓"),
        Pair("North Indian", "🥘"),
        Pair("Pizza", "🍕"),
        Pair("Dessert", "🍰")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Custom Banner Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AnnivoHorizontalLogo(
                    iconSize = 32.dp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                val deliveryAddress = loyaltyProfile?.homeAddress?.takeIf { it.isNotBlank() } ?: "DLF Phase 3, Gurugram"
                Text(
                    text = "Deliver to 📍 $deliveryAddress",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = { viewModel.navigateTo("profile") }
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Profile",
                    tint = SaffronOrange,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Premium Local Food Delivery Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🍲",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DIRECT FROM LOCAL KITCHENS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Delicious dishes from India's top local chefs delivered hot and fresh to your doorstep. Safe, hygienic packaging & secure payments!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("restaurant_search_input"),
            placeholder = { Text("Search biryani, pizza, Punjabi, thali...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SaffronOrange,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            )
        )

        // Categories Chips List
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat.first
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCategory(cat.first) },
                    label = { Text("${cat.second} ${cat.first}", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaffronOrange,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) SaffronOrange else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                    )
                )
            }
        }

        // Restaurant Title List
        Text(
            text = "Popular Indian Delicacies Near You",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🥘", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No restaurants found matching details", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { rest ->
                    RestaurantCard(restaurant = rest, onClick = { viewModel.selectRestaurant(rest.id) }, onFavToggle = { viewModel.toggleFavoriteRestaurant(rest.id, !rest.isFavorite) })
                }
            }
        }
    }
}

@Composable
fun RestaurantCard(
    restaurant: RestaurantEntity,
    onClick: () -> Unit,
    onFavToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .testTag("restaurant_card_${restaurant.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SaffronLight, SaffronOrange)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Visual Indicator based on Type (since Coil loads URLs, we show beautifully drawn custom icons)
                val emoji = when (restaurant.imageUrl) {
                    "biryani" -> "🍲"
                    "south_indian" -> "🫓"
                    "north_indian" -> "🥘"
                    "pizza" -> "🍕"
                    "dessert" -> "🍰"
                    else -> "🍱"
                }

                Text(emoji, fontSize = 64.sp)

                // Annivo Assured Premium Quality Tag Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SaffronOrange)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ANNIVO ASSURED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }

                // Distance overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("${restaurant.distanceKm} km", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Favorite icon toggle
                IconButton(
                    onClick = onFavToggle,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (restaurant.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (restaurant.isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = "Rating", tint = SaffronOrange, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(restaurant.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = restaurant.cuisine,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccessTime, contentDescription = "Delivery time", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${restaurant.deliveryTimeMin} mins", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Text(
                        text = "₹${restaurant.costForTwo} for two",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. RESTAURANT DETAILS & MENU SCREEN
// ==========================================
@Composable
fun RestaurantDetailScreen(viewModel: FoodViewModel) {
    val restaurant by viewModel.selectedRestaurant.collectAsState()
    val menuItems by viewModel.selectedRestaurantFoodItems.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    restaurant?.let { rest ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header backdrop with title overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SaffronOrange, SaffronAccent)
                        )
                    )
            ) {
                // Back button
                IconButton(
                    onClick = { viewModel.navigateTo("explore") },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("back_to_restaurants_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                // Decorative food graphics
                Text(
                    text = "🇮🇳🍛🍲🍕",
                    fontSize = 42.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Info overlay on bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = rest.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Text(
                        text = rest.cuisine,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // Annivo Premium Quality Assured Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = "Annivo Quality Assured",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Prepared under strict hygiene guidelines, packaged with extreme care, and delivered fresh to your doorstep from ${rest.name}.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 14.sp
                    )
                }
            }

            // Menu Section Title
            Text(
                text = "Full Menu",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Menu List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(menuItems) { food ->
                    val cartMatch = cartItems.find { it.foodItemId == food.id }
                    val qtyInCart = cartMatch?.quantity ?: 0

                    FoodItemRow(
                        foodItem = food,
                        qtyInCart = qtyInCart,
                        onAddClick = { viewModel.addFoodToCart(food, rest.name) },
                        onQtyChange = { qty -> viewModel.updateCartItemQty(food.id, qty) }
                    )
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SaffronOrange)
        }
    }
}

@Composable
fun FoodItemRow(
    foodItem: FoodItemEntity,
    qtyInCart: Int,
    onAddClick: () -> Unit,
    onQtyChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("food_item_row_${foodItem.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Veg / Non-veg indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .border(1.dp, if (foodItem.isVeg) EmeraldGreen else Color.Red)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (foodItem.isVeg) EmeraldGreen else Color.Red)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (foodItem.isVeg) "VEG" else "NON-VEG",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (foodItem.isVeg) EmeraldGreen else Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = foodItem.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "₹${foodItem.price}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = SaffronOrange
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = foodItem.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Add button / counter box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(100.dp)
            ) {
                // Custom food visual (drawn simple)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SaffronLight.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (foodItem.category) {
                            "Biryani" -> "🍲"
                            "Pizza" -> "🍕"
                            "North Indian" -> "🥘"
                            "South Indian" -> "🫓"
                            "Dessert" -> "🍰"
                            else -> "🍔"
                        },
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (qtyInCart > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(SaffronOrange)
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "-",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .clickable { onQtyChange(qtyInCart - 1) }
                                .padding(horizontal = 8.dp)
                                .testTag("decrease_qty_${foodItem.id}")
                        )
                        Text(
                            text = qtyInCart.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "+",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .clickable { onQtyChange(qtyInCart + 1) }
                                .padding(horizontal = 8.dp)
                                .testTag("increase_qty_${foodItem.id}")
                        )
                    }
                } else {
                    Button(
                        onClick = onAddClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .testTag("add_item_button_${foodItem.id}"),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                    ) {
                        Text("ADD", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. CART & BILLING CHECKOUT SCREEN
// ==========================================
@Composable
fun CartScreen(viewModel: FoodViewModel) {
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val deliveryFee by viewModel.cartDeliveryFee.collectAsState()
    val gst by viewModel.cartGst.collectAsState()
    val grandTotal by viewModel.cartGrandTotal.collectAsState()
    val pointsToEarn by viewModel.cartLoyaltyPointsToEarn.collectAsState()

    // Razorpay Secured states
    val isProcessingPayment by viewModel.isProcessingPayment.collectAsState()
    val paymentSuccess by viewModel.paymentSuccess.collectAsState()

    var deliveryAddress by remember { mutableStateOf("House 147, DLF Phase 3, Sector 24, Gurugram, Haryana - 122002") }
    var showRazorpayGateway by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart", tint = SaffronOrange)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Secure Checkout",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your cart is empty!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add delicious meals with direct raw pricing.", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.navigateTo("explore") },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                    ) {
                        Text("Browse Restaurants", color = Color.White)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Cart Items Section
                Text(
                    text = "Items from ${cartItems.first().restaurantName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("₹${item.price} each", fontSize = 11.sp, color = Color.Gray)
                        }

                        // Small plus-minus editor
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CreamGrey)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "-",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { viewModel.updateCartItemQty(item.foodItemId, item.quantity - 1) }
                                    .padding(horizontal = 6.dp)
                            )
                            Text(
                                text = item.quantity.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Text(
                                text = "+",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { viewModel.updateCartItemQty(item.foodItemId, item.quantity + 1) }
                                    .padding(horizontal = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "₹${item.price * item.quantity}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delivery address
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.MyLocation, contentDescription = "Address", tint = SaffronOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delivery Destination", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delivery_address_input"),
                            textStyle = TextStyle(fontSize = 12.sp),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Billing Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Bill Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item Subtotal", fontSize = 13.sp, color = Color.Gray)
                            Text("₹$subtotal", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Nominal Delivery Fee", fontSize = 13.sp, color = Color.Gray)
                            if (deliveryFee == 0.0) {
                                Text("FREE (Order > ₹300)", color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("₹$deliveryFee", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("GST (5%)", fontSize = 13.sp, color = Color.Gray)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", gst)}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }



                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CardGiftcard, contentDescription = "Loyalty points", tint = SaffronOrange, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Earns +$pointsToEarn Points", color = SaffronOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "₹${String.format(Locale.ENGLISH, "%.2f", grandTotal)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = SaffronOrange
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Razorpay secure checkout activation
                        Button(
                            onClick = { showRazorpayGateway = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("razorpay_checkout_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = RazorpayBlue),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = "Secure Checkout", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pay Securely via Razorpay",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Razorpay Indian Payment Gateway overlay simulation
    if (showRazorpayGateway) {
        RazorpayCheckoutDialog(
            amount = grandTotal,
            isProcessing = isProcessingPayment,
            isSuccess = paymentSuccess,
            onClose = { showRazorpayGateway = false },
            onPayAuthorized = {
                viewModel.checkoutWithRazorpay(deliveryAddress)
            }
        )
    }
}

@Composable
fun RazorpayCheckoutDialog(
    amount: Double,
    isProcessing: Boolean,
    isSuccess: Boolean,
    onClose: () -> Unit,
    onPayAuthorized: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isProcessing) onClose() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("razorpay_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header (Razorpay deep navy color branding)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RazorpayBlue)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Razorpay Secure",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RazorpayAccent)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("SANDBOX", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Merchant: ANNIVO FOODS",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Amount Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CreamGrey)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Payable Amount", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Text(
                        text = "₹${String.format(Locale.ENGLISH, "%.2f", amount)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = RazorpayBlue
                    )
                }

                if (isProcessing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = RazorpayAccent)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Securing transaction, connecting with bank...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (isSuccess) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Payment Authorized Successfully!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Select Payment Mode (Simulated Gateway)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Payment Options
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PaymentOptionRow(icon = Icons.Filled.Send, title = "UPI Instant Checkout (GPay, PhonePe, Paytm)", subtitle = "Direct instant mobile payment transfer")
                            PaymentOptionRow(icon = Icons.Filled.CreditCard, title = "Credit / Debit Cards (Visa, Mastercard, RuPay)", subtitle = "Save cards securely for seamless pay")
                            PaymentOptionRow(icon = Icons.Filled.AccountBalance, title = "Net Banking", subtitle = "SBI, HDFC, ICICI, Axis and major banks")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Trigger Payment Auth Button
                        Button(
                            onClick = onPayAuthorized,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("razorpay_pay_confirm"),
                            colors = ButtonDefaults.buttonColors(containerColor = RazorpayBlue)
                        ) {
                            Text("PASTED TEST CREDENTIALS & PAY", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "🔒 PCI-DSS Compliant. 256-Bit SSL Encrypted.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentOptionRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = RazorpayBlue, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RazorpayBlue)
            Text(subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

// ==========================================
// 5. ORDERS & LIVE MAP TRACKING SCREEN
// ==========================================
@Composable
fun OrdersScreen(viewModel: FoodViewModel) {
    val ordersList by viewModel.orders.collectAsState()
    var selectedOrder by remember { mutableStateOf<OrderEntity?>(null) }

    // Auto-select most recent active order if none selected
    LaunchedEffect(ordersList) {
        if (selectedOrder == null && ordersList.isNotEmpty()) {
            selectedOrder = ordersList.first()
        } else if (selectedOrder != null && ordersList.isNotEmpty()) {
            selectedOrder = ordersList.find { it.id == selectedOrder!!.id } ?: ordersList.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.DirectionsRun, contentDescription = "Tracking", tint = SaffronOrange)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Real-Time Tracking & Orders",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        if (ordersList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏍️", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No orders placed yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Simulate an order in the Cart screen to track live!", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // If an order is selected, show its live real-time tracker
                selectedOrder?.let { activeOrder ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.3f)
                            .background(CreamGrey)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Current Active Tracker Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = activeOrder.id,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            color = SaffronOrange
                                        )
                                        Text(
                                            text = "From: ${activeOrder.restaurantName}",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (activeOrder.status == "DELIVERED") EmeraldBg else SaffronLight.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = activeOrder.status,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (activeOrder.status == "DELIVERED") EmeraldGreen else SaffronOrange
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Real-Time ETA
                                val etaMin = when (activeOrder.status) {
                                    "CONFIRMED" -> "20-25 mins"
                                    "PREPARING" -> "15-18 mins"
                                    "OUT_FOR_DELIVERY" -> "5-8 mins"
                                    "DELIVERED" -> "Delivered!"
                                    else -> "Calculating..."
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Estimated Arrival", fontSize = 11.sp, color = Color.Gray)
                                        Text(etaMin, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepCharcoal)
                                    }
                                    Text(
                                        text = "Direct Raw Price: ₹${String.format(Locale.ENGLISH, "%.2f", activeOrder.totalAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SaffronOrange
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // LIVE DELIVERY TRACKING SIMULATION CANVAS
                                Text("Live Delivery Tracker Route", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))

                                LiveTrackerCanvas(progress = activeOrder.deliveryProgress)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Linear Progress stages indicator
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TrackerStageNode(label = "Confirm", isCompleted = activeOrder.deliveryProgress >= 0.15f, iconStr = "📝")
                                    TrackerStageNode(label = "Kitchen", isCompleted = activeOrder.deliveryProgress >= 0.45f, iconStr = "🍳")
                                    TrackerStageNode(label = "On Road", isCompleted = activeOrder.deliveryProgress >= 0.75f, iconStr = "🏍️")
                                    TrackerStageNode(label = "Enjoy", isCompleted = activeOrder.deliveryProgress >= 1.0f, iconStr = "😋")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Customer Details / Address Info
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Delivery Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("📍 Address:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color.Gray)
                                Text(activeOrder.deliveryAddress, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("💳 Payment ID (Razorpay Secure):", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color.Gray)
                                Text(activeOrder.razorpayPaymentId, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("🎁 Loyalty Points Earned:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color.Gray)
                                    Text("+${activeOrder.loyaltyPointsEarned} Club Points", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SaffronOrange)
                                }
                            }
                        }
                    }
                }

                // Previous Orders History List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Your Order History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ordersList) { order ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedOrder?.id == order.id) SaffronLight.copy(alpha = 0.15f) else CreamGrey)
                                    .border(
                                        width = 1.dp,
                                        color = if (selectedOrder?.id == order.id) SaffronOrange else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedOrder = order }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(order.restaurantName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date(order.timestamp)),
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(order.id, fontSize = 10.sp, color = Color.Gray)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${String.format(Locale.ENGLISH, "%.2f", order.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = order.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.status == "DELIVERED") EmeraldGreen else SaffronOrange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveTrackerCanvas(progress: Float) {
    // Canvas drawing path from Restaurant to Home with a moving motorcycle Emoji
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(EmeraldBg.copy(alpha = 0.5f))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val startX = 40f
            val endX = width - 40f
            val midY = height / 2f

            // Draw route background line
            drawLine(
                color = Color.LightGray,
                start = Offset(startX, midY),
                end = Offset(endX, midY),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )

            // Draw completed route green line
            val currentX = startX + (endX - startX) * progress
            drawLine(
                color = EmeraldGreen,
                start = Offset(startX, midY),
                end = Offset(currentX, midY),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }

        // Draw start icon overlay (Restaurant)
        Box(modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)) {
            Text("🏬", fontSize = 18.sp)
        }

        // Draw end icon overlay (Home)
        Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)) {
            Text("🏠", fontSize = 18.sp)
        }

        // Animated driver icon sliding across the screen
        val widthScale = (progress * 0.82f) + 0.05f
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(widthScale),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .shadow(2.dp, CircleShape)
                    .background(SaffronOrange, CircleShape)
                    .padding(4.dp)
            ) {
                Text("🏍️", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun TrackerStageNode(label: String, isCompleted: Boolean, iconStr: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isCompleted) EmeraldGreen else Color.LightGray.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(iconStr, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Medium,
            color = if (isCompleted) EmeraldGreen else Color.Gray
        )
    }
}

// ==========================================
// 6. LOYALTY REWARDS CLUB SCREEN
// ==========================================
@Composable
fun RewardsScreen(viewModel: FoodViewModel) {
    val loyaltyProfile by viewModel.loyaltyProfile.collectAsState()

    val coupons = listOf(
        Triple(100, 50.0, "ANVREWARD50"),
        Triple(180, 100.0, "ANVREWARD100"),
        Triple(250, 150.0, "ANVREWARD150")
    )

    var redeemedCode by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CardGiftcard, contentDescription = "Rewards", tint = SaffronOrange)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ANNIVO Loyalty Club",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        loyaltyProfile?.let { profile ->
            // Beautiful Saffron Orange Loyalty Club Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SaffronOrange),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ANNIVO ELITE CLUB",
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${profile.tier} Tier Member",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🇮🇳 Active Partner",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "POINTS BALANCE",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${profile.totalPoints} pts",
                                fontWeight = FontWeight.Black,
                                color = GoldYellow,
                                fontSize = 32.sp
                            )
                        }

                        Text(
                            text = "10 ₹ Spent = 1 Pt",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Redeem Coupons Section
            Text(
                text = "Redeem Point Vouchers",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Redeemed Pop-up alert
            redeemedCode?.let { code ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldBg),
                    border = BorderStroke(1.dp, EmeraldLight)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Congratulations! Promo Unlocked 🎉", fontWeight = FontWeight.Bold, color = EmeraldGreen, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, EmeraldGreen)
                                .background(Color.White)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(code, fontWeight = FontWeight.Black, color = EmeraldGreen, fontSize = 16.sp, letterSpacing = 2.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Use this coupon code on checkout to save extra!", fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { redeemedCode = null },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Text("Done", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                coupons.forEach { coupon ->
                    val canAfford = profile.totalPoints >= coupon.first
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CreamGrey)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("₹${coupon.second.toInt()} Discount Coupon", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Costs: ${coupon.first} loyalty points", fontSize = 11.sp, color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                viewModel.redeemRewardCoupon(coupon.first, coupon.second)
                                redeemedCode = coupon.third
                            },
                            enabled = canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAfford) SaffronOrange else Color.LightGray
                            )
                        ) {
                            Text(
                                text = if (canAfford) "Redeem" else "Locked",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SaffronOrange)
            }
        }
    }
}

// ==========================================
// 7. 24/7 CUSTOMER SUPPORT SCREEN
// ==========================================
@Composable
fun SupportScreen(viewModel: FoodViewModel) {
    val messages by viewModel.supportMessages.collectAsState()
    val tickets by viewModel.supportTickets.collectAsState()
    var currentText by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("chat") } // chat, tickets

    // Raise Ticket form states
    var showCreateTicketDialog by remember { mutableStateOf(false) }
    var ticketSubject by remember { mutableStateOf("") }
    var ticketCategory by remember { mutableStateOf("Order Delayed") }
    var ticketDescription by remember { mutableStateOf("") }

    val supportFaqs = listOf(
        "What are the delivery charges?",
        "How secure is Razorpay here?",
        "Where can I track active orders?",
        "How do I unlock more loyalty points?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.SupportAgent, contentDescription = "Support Desk", tint = SaffronOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ANNIVO Support Hub (24/7)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        // Tab Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CreamGrey)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                Pair("chat", "Chat Support"),
                Pair("tickets", "Ticket Tracker")
            ).forEach { (tabId, label) ->
                val selected = activeTab == tabId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) SaffronOrange else Color.Transparent)
                        .clickable { activeTab = tabId }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activeTab == "chat") {
            // Subheader core message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldBg)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "⚡ Real-time chat with Razorpay and Order Support experts. Instant automated resolution.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldGreen
                )
            }

            // Scrollable Messages Box
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AnnivoBrandLogo(
                                iconSize = 80.dp,
                                showSubtitles = true,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
                items(messages) { msg ->
                    val isUser = msg.sender == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) SaffronOrange else CreamGrey
                            ),
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isUser) 12.dp else 0.dp,
                                bottomEnd = if (isUser) 0.dp else 12.dp
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = if (isUser) Color.White else DeepCharcoal,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(msg.timestamp)),
                                    fontSize = 9.sp,
                                    color = if (isUser) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            // Suggestion Chips Box
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(supportFaqs) { faq ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SaffronLight.copy(alpha = 0.15f))
                            .border(1.dp, SaffronOrange.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.sendSupportChat(faq)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(faq, fontSize = 11.sp, color = SaffronOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Input Line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("support_input"),
                    placeholder = { Text("Describe your support query...", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronOrange,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (currentText.isNotBlank()) {
                            viewModel.sendSupportChat(currentText)
                            currentText = ""
                        }
                    },
                    modifier = Modifier
                        .background(SaffronOrange, CircleShape)
                        .testTag("support_send_button")
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        } else {
            // Ticket Tracker Tab
            Box(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Action summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE SUPPORT TICKETS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        
                        Button(
                            onClick = { showCreateTicketDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Raise Ticket", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    if (tickets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎫", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No Support Tickets Raised", fontWeight = FontWeight.Bold, color = AnnivoNavy)
                                Text("Raise a ticket to report issues about payments, deliveries, or orders.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(tickets) { ticket ->
                                val isOpen = ticket.status == "OPEN"
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(AnnivoNavy.copy(alpha = 0.1f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(ticket.id, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AnnivoNavy)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(if (isOpen) SaffronOrange.copy(alpha = 0.15f) else EmeraldBg)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = ticket.status,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isOpen) SaffronOrange else EmeraldGreen
                                                    )
                                                }
                                            }
                                            
                                            Text(
                                                text = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date(ticket.timestamp)),
                                                fontSize = 9.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = ticket.subject,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = AnnivoNavy
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(SaffronLight.copy(alpha = 0.1f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Category: ${ticket.category}",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = SaffronOrange
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = ticket.description,
                                            fontSize = 12.sp,
                                            color = DeepCharcoal.copy(alpha = 0.8f),
                                            lineHeight = 16.sp
                                        )

                                        if (isOpen) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                OutlinedButton(
                                                    onClick = { viewModel.updateTicketStatus(ticket.id, "RESOLVED") },
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, EmeraldGreen),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Mark as Resolved", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Ticket Dialog
    if (showCreateTicketDialog) {
        AlertDialog(
            onDismissRequest = { showCreateTicketDialog = false },
            title = {
                Text(
                    text = "Raise Support Ticket",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AnnivoNavy
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = ticketSubject,
                        onValueChange = { ticketSubject = it },
                        label = { Text("Ticket Subject (e.g. Delayed Delivery)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 1
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Ticket Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Order Delayed", "Payment Failed", "Refund Status", "Other").forEach { category ->
                            val isSelected = ticketCategory == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SaffronOrange else CreamGrey)
                                    .clickable { ticketCategory = category }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category.replace(" ", "\n"),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = ticketDescription,
                        onValueChange = { ticketDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ticketSubject.isNotBlank() && ticketDescription.isNotBlank()) {
                            viewModel.createSupportTicket(ticketSubject, ticketCategory, ticketDescription)
                            // Clear form
                            ticketSubject = ""
                            ticketDescription = ""
                            ticketCategory = "Order Delayed"
                            showCreateTicketDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Submit Ticket", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateTicketDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
