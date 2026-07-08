package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.LoyaltyProfileEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.FoodViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: FoodViewModel) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("otp") } // otp, email, signup
    
    // Form States
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    
    // OTP simulation states
    var otpSent by remember { mutableStateOf(false) }
    var otpCountdown by remember { mutableStateOf(0) }
    var isSendingOtp by remember { mutableStateOf(false) }
    
    // Visual States
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Countdown Timer for OTP Resend
    LaunchedEffect(otpCountdown) {
        if (otpCountdown > 0) {
            delay(1000)
            otpCountdown--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AnnivoNavy,
                        AnnivoNavyLight
                    )
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))
            
            // Brand Logo & Header
            Text(
                text = "🍲",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "ANNIVO",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = SaffronOrange,
                letterSpacing = 1.sp
            )
            
            Text(
                text = "— Good Food. One Community. —",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Auth Selector Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Auth Tab Headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            Triple("otp", "OTP", Icons.Filled.Smartphone),
                            Triple("email", "Email", Icons.Filled.Email),
                            Triple("signup", "Signup", Icons.Filled.PersonAdd)
                        ).forEach { (tab, label, icon) ->
                            val selected = activeTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) SaffronOrange else Color.Transparent)
                                    .clickable {
                                        activeTab = tab
                                        errorMessage = null
                                        successMessage = null
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Feedbacks
                    if (errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (successMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmeraldBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = successMessage ?: "",
                                color = EmeraldGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Content based on tab
                    when (activeTab) {
                        "otp" -> {
                            Text(
                                text = "OTP Authentication",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AnnivoNavy,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Enter your phone number to receive a secure login OTP.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) phoneInput = it },
                                label = { Text("Phone Number (10 digits)") },
                                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = SaffronOrange) },
                                isError = errorMessage != null && !otpSent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 1,
                                enabled = !otpSent
                            )

                            if (otpSent) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) otpInput = it },
                                    label = { Text("6-Digit OTP") },
                                    leadingIcon = { Icon(Icons.Filled.LockOpen, contentDescription = null, tint = SaffronOrange) },
                                    isError = errorMessage != null && otpSent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("otp_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    maxLines = 1,
                                    supportingText = {
                                        Text("Use demo code '123456' to bypass instantly.")
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            if (!otpSent) {
                                Button(
                                    onClick = {
                                        if (phoneInput.length < 10) {
                                            errorMessage = "Please enter a valid 10-digit Indian phone number!"
                                        } else {
                                            scope.launch {
                                                isSendingOtp = true
                                                errorMessage = null
                                                delay(1200) // simulate OTP network send
                                                isSendingOtp = false
                                                otpSent = true
                                                otpCountdown = 59
                                                viewModel.generateAndSendOtp(phoneInput)
                                                successMessage = "Secure OTP sent successfully! Check notifications."
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("send_otp_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                                    enabled = !isSendingOtp
                                ) {
                                    if (isSendingOtp) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text("Send OTP", fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (otpInput.length < 6) {
                                            errorMessage = "Please enter a valid 6-digit OTP!"
                                        } else {
                                            viewModel.loginWithOtp(phoneInput, otpInput) { success, msg ->
                                                if (success) {
                                                    successMessage = msg
                                                    errorMessage = null
                                                } else {
                                                    errorMessage = msg
                                                    successMessage = null
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("verify_otp_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                                ) {
                                    Text("Verify & Login", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                TextButton(
                                    onClick = {
                                        if (otpCountdown == 0) {
                                            otpCountdown = 59
                                            viewModel.generateAndSendOtp(phoneInput)
                                            successMessage = "Resent secure OTP successfully! Check notifications."
                                        }
                                    },
                                    enabled = otpCountdown == 0,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (otpCountdown > 0) "Resend OTP in ${otpCountdown}s" else "Resend OTP",
                                        color = if (otpCountdown > 0) Color.Gray else SaffronOrange,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        "email" -> {
                            Text(
                                text = "Email Authentication",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AnnivoNavy,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Use your registered email and password to sign in.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = SaffronOrange) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = SaffronOrange) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_input"),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 1,
                                supportingText = {
                                    Text("Demo credentials: satishchowdary1477@gmail.com / password123")
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (emailInput.isBlank() || passwordInput.isBlank()) {
                                        errorMessage = "Please enter both email and password!"
                                    } else {
                                        viewModel.loginWithEmailAndPassword(emailInput, passwordInput) { success, msg ->
                                            if (success) {
                                                successMessage = msg
                                                errorMessage = null
                                            } else {
                                                errorMessage = msg
                                                successMessage = null
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("email_login_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                            ) {
                                Text("Sign In", fontWeight = FontWeight.Bold)
                            }
                        }
                        "signup" -> {
                            Text(
                                text = "Create Local Eatery Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AnnivoNavy,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Signup for direct menu discounts and earn rewards on every meal.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null, tint = SaffronOrange) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_name_input"),
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = SaffronOrange) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_email_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { if (it.all { char -> char.isDigit() }) phoneInput = it },
                                label = { Text("Phone Number") },
                                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = SaffronOrange) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_phone_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (nameInput.isBlank() || emailInput.isBlank() || phoneInput.length < 10) {
                                        errorMessage = "Please fill in all details with a valid 10-digit phone number!"
                                    } else {
                                        viewModel.signupNewUser(nameInput, emailInput, phoneInput) { success, msg ->
                                            if (success) {
                                                successMessage = msg
                                                errorMessage = null
                                            } else {
                                                errorMessage = msg
                                                successMessage = null
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("create_account_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                            ) {
                                Text("Create Account", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupProfileScreen(viewModel: FoodViewModel) {
    val context = LocalContext.current
    val loyaltyProfile by viewModel.loyaltyProfile.collectAsState()
    val scope = rememberCoroutineScope()

    // Form inputs initialized with profile values
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }
    var workAddress by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardNo by remember { mutableStateOf("") }
    var upi by remember { mutableStateOf("") }

    var isCapturingLocation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Sync state fields once profile flow loads
    LaunchedEffect(loyaltyProfile) {
        loyaltyProfile?.let { prof ->
            if (name.isBlank()) name = prof.name
            if (email.isBlank()) email = prof.email
            if (phone.isBlank()) phone = prof.phone
            if (homeAddress.isBlank()) homeAddress = prof.homeAddress
            if (workAddress.isBlank()) workAddress = prof.workAddress
            if (cardName.isBlank()) cardName = prof.savedCardName
            if (cardNo.isBlank()) cardNo = prof.savedCardNo
            if (upi.isBlank()) upi = prof.savedUpi
        }
    }

    // Permission launcher for location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            isCapturingLocation = true
            viewModel.autoCaptureLocation(context) { captured ->
                isCapturingLocation = false
                homeAddress = captured
                successMessage = "Location auto-captured successfully!"
                errorMessage = null
            }
        } else {
            errorMessage = "Location permissions denied. Please fill in manual address details below!"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AnnivoNavyBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Elegant Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(AnnivoNavy)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        text = "Complete Your Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Please set up your delivery address and payment methods to start ordering.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            // Form Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Feedback Notifications
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                if (successMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = successMessage ?: "",
                            color = EmeraldGreen,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Section 1: Basic Information
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = SaffronOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Basic Information", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AnnivoNavy)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Section 2: Location & Addresses
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PinDrop, contentDescription = null, tint = SaffronOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delivery Locations", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AnnivoNavy)
                            }
                            
                            // Auto-capture location button
                            Button(
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                                modifier = Modifier.height(32.dp)
                            ) {
                                if (isCapturingLocation) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Auto-Capture GPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Home address with auto-capture targetting
                        OutlinedTextField(
                            value = homeAddress,
                            onValueChange = { homeAddress = it },
                            label = { Text("Primary Delivery Address (Home)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 2,
                            supportingText = {
                                Text("Can be entered manually or auto-filled via GPS button above.")
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = workAddress,
                            onValueChange = { workAddress = it },
                            label = { Text("Secondary Delivery Address (Work - Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 2
                        )
                    }
                }

                // Section 3: Saved Payment Options
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Payment, contentDescription = null, tint = SaffronOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fast Checkout Payments", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AnnivoNavy)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = cardName,
                            onValueChange = { cardName = it },
                            label = { Text("Cardholder Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cardNo,
                            onValueChange = { cardNo = it },
                            label = { Text("Credit / Debit Card Number") },
                            placeholder = { Text("xxxx xxxx xxxx xxxx") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = upi,
                            onValueChange = { upi = it },
                            label = { Text("VPA / UPI ID (e.g. user@okhdfc)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Save button
                Button(
                    onClick = {
                        if (name.isBlank() || email.isBlank() || homeAddress.isBlank()) {
                            errorMessage = "Name, Email, and Primary Address (Home) are mandatory fields!"
                        } else {
                            viewModel.updateUserProfile(
                                name = name,
                                email = email,
                                phone = phone,
                                homeAddress = homeAddress,
                                workAddress = workAddress,
                                cardName = cardName,
                                cardNo = cardNo,
                                upi = upi
                            ) { success, msg ->
                                if (success) {
                                    errorMessage = null
                                    successMessage = msg
                                    // Move to explore screen
                                    viewModel.navigateTo("explore")
                                } else {
                                    errorMessage = msg
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_profile_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                ) {
                    Text("Save Profile & Continue ➔", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(viewModel: FoodViewModel) {
    val context = LocalContext.current
    val loyaltyProfile by viewModel.loyaltyProfile.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }
    var workAddress by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardNo by remember { mutableStateOf("") }
    var upi by remember { mutableStateOf("") }

    var isCapturingLocation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loyaltyProfile) {
        loyaltyProfile?.let { prof ->
            if (name.isBlank()) name = prof.name
            if (email.isBlank()) email = prof.email
            if (phone.isBlank()) phone = prof.phone
            if (homeAddress.isBlank()) homeAddress = prof.homeAddress
            if (workAddress.isBlank()) workAddress = prof.workAddress
            if (cardName.isBlank()) cardName = prof.savedCardName
            if (cardNo.isBlank()) cardNo = prof.savedCardNo
            if (upi.isBlank()) upi = prof.savedUpi
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            isCapturingLocation = true
            viewModel.autoCaptureLocation(context) { captured ->
                isCapturingLocation = false
                homeAddress = captured
                successMessage = "Location auto-captured successfully!"
                errorMessage = null
            }
        } else {
            errorMessage = "Location permissions denied. Please fill in manual address details!"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo("explore") }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = AnnivoNavy
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AnnivoNavyBg)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
            if (successMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(text = successMessage ?: "", color = EmeraldGreen, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }

            // Loyalty tier indicator
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = AnnivoNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆", fontSize = 36.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = loyaltyProfile?.tier?.uppercase() ?: "SILVER", color = SaffronOrange, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text(text = "${loyaltyProfile?.totalPoints ?: 120} Loyalty Points", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Earned on every direct order!", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                }
            }

            // Section 1: Personal Details
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Personal Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AnnivoNavy)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                }
            }

            // Section 2: Addresses
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Delivery Addresses", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AnnivoNavy)
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            if (isCapturingLocation) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp))
                            } else {
                                Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto GPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = homeAddress, onValueChange = { homeAddress = it }, label = { Text("Home Address (Primary)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), minLines = 2)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = workAddress, onValueChange = { workAddress = it }, label = { Text("Work Address (Secondary)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), minLines = 2)
                }
            }

            // Section 3: Saved Payments
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Options", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AnnivoNavy)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = cardName, onValueChange = { cardName = it }, label = { Text("Cardholder Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = cardNo, onValueChange = { cardNo = it }, label = { Text("Card Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = upi, onValueChange = { upi = it }, label = { Text("UPI ID (VPA)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                }
            }

            // Action Buttons
            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || homeAddress.isBlank()) {
                        errorMessage = "Name, Email, and Home Address are required!"
                    } else {
                        viewModel.updateUserProfile(name, email, phone, homeAddress, workAddress, cardName, cardNo, upi) { success, msg ->
                            if (success) {
                                errorMessage = null
                                successMessage = msg
                            } else {
                                errorMessage = msg
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Logout Button
            OutlinedButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Red),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = "Logout", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout Account", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
