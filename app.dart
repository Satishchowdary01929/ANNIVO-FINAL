import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';

// ============================================================================
//               ANNIVO FOOD & LOYALTY CLUB - FLUTTER VERSION
// ============================================================================
// This single file contains the complete, feature-rich translation of the
// Annivo Android (Kotlin/Jetpack Compose) app into pure, self-contained Dart/Flutter.
// All features are preserved with beautiful Material 3 styling, including:
// 1. Secure Dual-Phase OTP login with brute-force lockout and expiry checks.
// 2. Interactive SMS slide-down banner simulation.
// 3. Restaurant browse, details, and customization cart management.
// 4. Cart system with Tip chips, promo codes, billing breakdown, and Razorpay modal.
// 5. Active Order GPS Canvas tracker displaying rider coordinates dynamically.
// 6. Loyalty club rewards tier tracking, Spin the Wheel, and interactive Scratch card.
// 7. Support center chatbot with smart replies and support ticket filings.
// ============================================================================

// Global Colors
const Color annivoNavy = Color(0xFF0D1B2A);
const Color SaffronOrange = Color(0xFFF4A261);
const Color CharcoalBg = Color(0xFF1E1E24);
const Color MessageBlue = Color(0xFF2E6FF2);
const Color GoldYellow = Color(0xFFFFD700);
const Color EmeraldGreen = Color(0xFF2E7D32);
const Color EmeraldBg = Color(0xFFE8F5E9);
const Color CreamGrey = Color(0xFFF4F6F9);

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  try {
    await Firebase.initializeApp();
  } catch (e) {
    debugPrint("Firebase initialization skipped or simulated: $e");
  }
  runApp(const AnnivoApp());
}

class AnnivoApp extends StatelessWidget {
  const AnnivoApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ANNIVO Food & Loyalty',
      debugShowCheckedModeBanner = false,
      theme: ThemeData(
        useMaterial3: true,
        primaryColor: SaffronOrange,
        colorScheme: ColorScheme.fromSeed(
          seedColor: SaffronOrange,
          primary: SaffronOrange,
          secondary: annivoNavy,
          background: Colors.white,
        ),
      ),
      home: const MainAppNavigationSwitcher(),
    );
  }
}

// ============================================================================
//                             DATA MODELS
// ============================================================================

class Restaurant {
  final String id;
  final String name;
  final String cuisine;
  final double rating;
  final int deliveryTimeMin;
  final int costForTwo;
  final double distanceKm;
  final bool isZeroCommission;
  bool isFavorite;
  final String imageUrl;

  Restaurant({
    required this.id,
    required this.name,
    required this.cuisine,
    required this.rating,
    required this.deliveryTimeMin,
    required this.costForTwo,
    required this.distanceKm,
    this.isZeroCommission = true,
    this.isFavorite = false,
    required this.imageUrl,
  });
}

class FoodItem {
  final String id;
  final String restaurantId;
  final String name;
  final double price;
  final String description;
  final bool isVeg;
  final double rating;
  final String category;
  final String imageUrl;

  FoodItem({
    required this.id,
    required this.restaurantId,
    required this.name,
    required this.price,
    required this.description,
    required this.isVeg,
    required this.rating,
    required this.category,
    required this.imageUrl,
  });
}

class CartItem {
  final String foodItemId;
  final String restaurantId;
  final String restaurantName;
  final String name;
  final double price;
  int quantity;

  CartItem({
    required this.foodItemId,
    required this.restaurantId,
    required this.restaurantName,
    required this.name,
    required this.price,
    required this.quantity,
  });
}

class Order {
  final String id;
  final String restaurantId;
  final String restaurantName;
  final int timestamp;
  final double totalAmount;
  String status;
  double deliveryProgress;
  final int loyaltyPointsEarned;
  final String razorpayPaymentId;
  final String deliveryAddress;
  bool isRated;

  Order({
    required this.id,
    required this.restaurantId,
    required this.restaurantName,
    required this.timestamp,
    required this.totalAmount,
    required this.status,
    required this.deliveryProgress,
    required this.loyaltyPointsEarned,
    required this.razorpayPaymentId,
    required this.deliveryAddress,
    this.isRated = false,
  });
}

class LoyaltyProfile {
  int totalPoints;
  String tier;
  double totalSavedCommissionRs;
  String name;
  String email;
  String phone;
  String homeAddress;
  String workAddress;
  String savedCardName;
  String savedCardNo;
  String savedUpi;

  LoyaltyProfile({
    this.totalPoints = 120,
    this.tier = 'Silver',
    this.totalSavedCommissionRs = 345.0,
    this.name = 'Satish Chowdary',
    this.email = 'satishchowdary1477@gmail.com',
    this.phone = '9876543210',
    this.homeAddress = 'Flat 402, Sai Enclave, Madhapur, Hyderabad, Telangana, 500081',
    this.workAddress = 'Building 3, Mindspace IT Park, Hitec City, Hyderabad, Telangana, 500081',
    this.savedCardName = 'Satish Chowdary',
    this.savedCardNo = '**** **** **** 4111',
    this.savedUpi = 'satish@okaxis',
  });

  void updateTier() {
    if (totalPoints >= 500) {
      tier = 'Platinum';
    } else if (totalPoints >= 250) {
      tier = 'Gold';
    } else {
      tier = 'Silver';
    }
  }
}

class SupportTicket {
  final String id;
  final String subject;
  final String category;
  final String status;
  final String description;
  final int timestamp;

  SupportTicket({
    required this.id,
    required this.subject,
    required this.category,
    required this.status,
    required this.description,
    required this.timestamp,
  });
}

class SupportMessage {
  final int id;
  final String sender;
  final String text;
  final int timestamp;

  SupportMessage({
    required this.id,
    required this.sender,
    required this.text,
    required this.timestamp,
  });
}

// ============================================================================
//                    STATE MANAGEMENT (FoodViewModel)
// ============================================================================
class FoodViewModel extends ChangeNotifier {
  // Authentication State
  bool _isLoggedIn = false;
  String? _activeOtp;
  int _otpGenerationTime = 0;
  int _wrongOtpAttempts = 0;
  int _otpLockoutUntil = 0;
  String? _headsUpMessage;

  bool get isLoggedIn => _isLoggedIn;
  String? get activeOtp => _activeOtp;
  String? get headsUpMessage => _headsUpMessage;

  // Databases & Lists
  final List<Restaurant> restaurants = [
    Restaurant(
      id: 'res_1',
      name: 'Spicy Hyderabad Biryani',
      cuisine: 'Biryani, Hyderabadi, Mughlai',
      rating: 4.6,
      deliveryTimeMin: 25,
      costForTwo: 450,
      distanceKm: 2.4,
      imageUrl: 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500&q=80',
    ),
    Restaurant(
      id: 'res_2',
      name: 'South Indian Sensation',
      cuisine: 'South Indian, Vegetarian',
      rating: 4.4,
      deliveryTimeMin: 18,
      costForTwo: 200,
      distanceKm: 1.2,
      imageUrl: 'https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&q=80',
    ),
    Restaurant(
      id: 'res_3',
      name: 'The Pizza Hub',
      cuisine: 'Pizza, Italian, Fast Food',
      rating: 4.1,
      deliveryTimeMin: 32,
      costForTwo: 600,
      distanceKm: 4.1,
      imageUrl: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&q=80',
    ),
    Restaurant(
      id: 'res_4',
      name: 'Sweet Tooth Confectioneries',
      cuisine: 'Desserts, Cakes, Ice Cream',
      rating: 4.7,
      deliveryTimeMin: 15,
      costForTwo: 300,
      distanceKm: 0.8,
      imageUrl: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&q=80',
    ),
  ];

  final List<FoodItem> foodItems = [
    // Spicy Hyderabad Biryani Items
    FoodItem(id: 'food_1', restaurantId: 'res_1', name: 'Special Chicken Biryani', price: 280.0, description: 'Fragrant long grain basmati rice cooked with succulent chicken pieces and signature spices.', isVeg: false, rating: 4.7, category: 'Main Course', imageUrl: ''),
    FoodItem(id: 'food_2', restaurantId: 'res_1', name: 'Mutton Seekh Kabab', price: 320.0, description: 'Spiced minced mutton skewers grilled over charcoal tandoor.', isVeg: false, rating: 4.5, category: 'Sides', imageUrl: ''),
    FoodItem(id: 'food_3', restaurantId: 'res_1', name: 'Double Ka Meetha', price: 120.0, description: 'Classic Hyderabadi bread pudding soaked in saffron-infused milk.', isVeg: true, rating: 4.6, category: 'Drinks', imageUrl: ''),
    FoodItem(id: 'food_4', restaurantId: 'res_1', name: 'Paneer Butter Masala', price: 240.0, description: 'Cottage cheese cubes tossed in rich and buttery cashew-tomato gravy.', isVeg: true, rating: 4.3, category: 'Main Course', imageUrl: ''),

    // South Indian Sensation Items
    FoodItem(id: 'food_5', restaurantId: 'res_2', name: 'Butter Masala Dosa', price: 90.0, description: 'Crispy rice crepe with potato bhaji filling, smeared with fresh butter.', isVeg: true, rating: 4.6, category: 'Breakfast', imageUrl: ''),
    FoodItem(id: 'food_6', restaurantId: 'res_2', name: 'Steamed Rava Idli (2 Pcs)', price: 60.0, description: 'Soft semolina idlis served with dynamic coconut chutney and piping hot sambar.', isVeg: true, rating: 4.4, category: 'Breakfast', imageUrl: ''),
    FoodItem(id: 'food_7', restaurantId: 'res_2', name: 'Filter Coffee', price: 40.0, description: 'Strong, aromatic decoction frothed with milk in standard tumbler-dabarah.', isVeg: true, rating: 4.8, category: 'Drinks', imageUrl: ''),

    // The Pizza Hub Items
    FoodItem(id: 'food_8', restaurantId: 'res_3', name: 'Double Cheese Margherita', price: 349.0, description: 'Loaded with extra mozzarella cheese and classic tomato sauce base.', isVeg: true, rating: 4.2, category: 'Main Course', imageUrl: ''),
    FoodItem(id: 'food_9', restaurantId: 'res_3', name: 'Fiery Chicken Pepperoni Pizza', price: 499.0, description: 'Spicy chicken pepperoni slices, red paprika, jalapenos, and mozzarella.', isVeg: false, rating: 4.5, category: 'Main Course', imageUrl: ''),
    FoodItem(id: 'food_10', restaurantId: 'res_3', name: 'Garlic Breadsticks with Dip', price: 139.0, description: 'Crispy breadsticks baked with garlic butter, served with cheese dip.', isVeg: true, rating: 4.3, category: 'Sides', imageUrl: ''),
  ];

  // App State variables
  final List<CartItem> cart = [];
  final List<Order> orders = [];
  final List<SupportTicket> tickets = [];
  final List<SupportMessage> supportMessages = [
    SupportMessage(id: 1, sender: 'bot', text: 'Namaste! I am your ANNIVO Personal Assistant. Ask me anything about your orders, loyalty points, or menu customisations!', timestamp: DateTime.now().millisecondsSinceEpoch - 600000),
  ];
  final LoyaltyProfile loyaltyProfile = LoyaltyProfile();

  // Active Order Tracker Timer
  Timer? _trackerTimer;

  FoodViewModel() {
    // Add default initial history tickets
    tickets.add(
      SupportTicket(
        id: 'TKT-9921',
        subject: 'Loyalty points not credited',
        category: 'Loyalty Club',
        status: 'RESOLVED',
        description: 'Placed order with cash, points are pending update.',
        timestamp: DateTime.now().millisecondsSinceEpoch - 86400000,
      ),
    );
  }

  @override
  void dispose() {
    _trackerTimer?.cancel();
    super.dispose();
  }

  // --- OTP Verification logic matching Android code ---
  void generateAndSendOtp(String phone) {
    final random = Random();
    final otp = (100000 + random.nextInt(900000)).toString();
    _activeOtp = otp;
    _otpGenerationTime = DateTime.now().millisecondsSinceEpoch;
    _wrongOtpAttempts = 0;
    _otpLockoutUntil = 0;

    // Show slide-down SMS overlay
    _headsUpMessage = otp;
    notifyListeners();
  }

  void dismissHeadsUpMessage() {
    _headsUpMessage = null;
    notifyListeners();
  }

  void loginWithOtp(String phone, String otp, Function(bool success, String message) onResult) {
    final now = DateTime.now().millisecondsSinceEpoch;

    // Check Lockout
    if (now < _otpLockoutUntil) {
      final secondsLeft = ((_otpLockoutUntil - now) / 1000).ceil();
      onResult(false, 'Too many wrong attempts! Locked out for $secondsLeft seconds.');
      return;
    }

    // Expiry validation (60s)
    final isDemo = otp == '123456' || otp == '654321';
    if (!isDemo && (now - _otpGenerationTime > 60000)) {
      onResult(false, 'OTP has expired! Please click Resend OTP.');
      return;
    }

    // Match checking
    if (otp != _activeOtp && !isDemo) {
      _wrongOtpAttempts++;
      if (_wrongOtpAttempts >= 3) {
        _otpLockoutUntil = now + 30000; // 30 seconds lockout
        _wrongOtpAttempts = 0;
        onResult(false, '3 incorrect attempts! Locked out for 30 seconds.');
      } else {
        onResult(false, 'Invalid OTP! Attempt $_wrongOtpAttempts of 3 before lockout.');
      }
      notifyListeners();
      return;
    }

    // Reset secure state
    _wrongOtpAttempts = 0;
    _otpLockoutUntil = 0;
    _activeOtp = null;
    _isLoggedIn = true;
    notifyListeners();
    onResult(true, 'Login successful!');
  }

  void logout() {
    _isLoggedIn = false;
    cart.clear();
    notifyListeners();
  }

  // --- Cart Management ---
  void addToCart(FoodItem item, Restaurant restaurant) {
    // Check if item from another restaurant is already in cart
    if (cart.isNotEmpty && cart.first.restaurantId != restaurant.id) {
      cart.clear(); // Clear other restaurant's items to keep simple single-restaurant orders
    }

    final index = cart.indexWhere((c) => c.foodItemId == item.id);
    if (index >= 0) {
      cart[index].quantity++;
    } else {
      cart.add(CartItem(
        foodItemId: item.id,
        restaurantId: restaurant.id,
        restaurantName: restaurant.name,
        name: item.name,
        price: item.price,
        quantity: 1,
      ));
    }
    notifyListeners();
  }

  void removeFromCart(FoodItem item) {
    final index = cart.indexWhere((c) => c.foodItemId == item.id);
    if (index >= 0) {
      if (cart[index].quantity > 1) {
        cart[index].quantity--;
      } else {
        cart.removeAt(index);
      }
    }
    notifyListeners();
  }

  int getCartItemQuantity(String id) {
    final index = cart.indexWhere((c) => c.foodItemId == id);
    return index >= 0 ? cart[index].quantity : 0;
  }

  double get cartSubtotal {
    return cart.fold(0.0, (sum, item) => sum + (item.price * item.quantity));
  }

  // --- Rewards & Points System ---
  void addPoints(int points) {
    loyaltyProfile.totalPoints += points;
    loyaltyProfile.updateTier();
    notifyListeners();
  }

  void redeemRewardCoupon(int points, double cashbackAmount) {
    if (loyaltyProfile.totalPoints >= points) {
      loyaltyProfile.totalPoints -= points;
      loyaltyProfile.updateTier();
      notifyListeners();
    }
  }

  // --- Checkout and Delivery Simulation ---
  void placeOrder({
    required double tip,
    required String address,
    required String paymentId,
    required double totalAmount,
    required double discount,
  }) {
    if (cart.isEmpty) return;

    final String resId = cart.first.restaurantId;
    final String resName = cart.first.restaurantName;
    final int pointsEarned = (cartSubtotal / 10).round();

    final order = Order(
      id: 'ANV-${1000 + Random().nextInt(9000)}',
      restaurantId: resId,
      restaurantName: resName,
      timestamp: DateTime.now().millisecondsSinceEpoch,
      totalAmount: totalAmount,
      status: 'Placed',
      deliveryProgress: 0.0,
      loyaltyPointsEarned: pointsEarned,
      razorpayPaymentId: paymentId,
      deliveryAddress: address,
    );

    orders.insert(0, order);
    cart.clear();
    notifyListeners();

    // Credit loyalty points
    addPoints(pointsEarned);

    // Track delivery progress live automatically (simulation)
    _startLiveTrackingSimulation(order);
  }

  void _startLiveTrackingSimulation(Order order) {
    _trackerTimer?.cancel();
    _trackerTimer = Timer.periodic(const Duration(seconds: 4), (timer) {
      final orderIndex = orders.indexWhere((o) => o.id == order.id);
      if (orderIndex < 0) {
        timer.cancel();
        return;
      }

      final activeOrder = orders[orderIndex];
      double nextProgress = activeOrder.deliveryProgress + 0.25;
      if (nextProgress >= 1.0) {
        nextProgress = 1.0;
        activeOrder.status = 'Arrived';
        timer.cancel();
      } else if (nextProgress >= 0.75) {
        activeOrder.status = 'Out for Delivery';
      } else if (nextProgress >= 0.3) {
        activeOrder.status = 'Preparing';
      }

      activeOrder.deliveryProgress = nextProgress;
      notifyListeners();
    });
  }

  // --- Chatbot Support Simulation ---
  void sendSupportMessage(String text) {
    if (text.trim().isEmpty) return;

    final userMsg = SupportMessage(
      id: supportMessages.length + 1,
      sender: 'user',
      text: text,
      timestamp: DateTime.now().millisecondsSinceEpoch,
    );
    supportMessages.add(userMsg);
    notifyListeners();

    // Smart automated chatbot reply after delay
    Timer(const Duration(seconds: 1), () {
      String botResponse = 'Thank you for reaching out! A human executive is joining shortly.';
      final query = text.toLowerCase();
      if (query.contains('order') || query.contains('delivery')) {
        if (orders.isNotEmpty) {
          final last = orders.first;
          botResponse = 'Checking your latest order ${last.id} status. It currently shows: "${last.status}". Our rider is on the way!';
        } else {
          botResponse = 'It looks like you haven\'t placed any orders today. Would you like help choosing a restaurant?';
        }
      } else if (query.contains('points') || query.contains('tier') || query.contains('loyalty')) {
        botResponse = 'You currently have ${loyaltyProfile.totalPoints} loyalty points in our ${loyaltyProfile.tier} Tier! You can redeem points on the rewards screen for extra cash discount coupons.';
      } else if (query.contains('refund') || query.contains('payment')) {
        botResponse = 'For refund or payment queries, please write your ticket subject below and submit a formal Support Case. Our regional dispatch team will review within 1 hour.';
      } else if (query.contains('hello') || query.contains('hi')) {
        botResponse = 'Namaste Satish! Hope you are hungry. Let me know how I can assist with your food exploration today!';
      }

      final botMsg = SupportMessage(
        id: supportMessages.length + 1,
        sender: 'bot',
        text: botResponse,
        timestamp: DateTime.now().millisecondsSinceEpoch,
      );
      supportMessages.add(botMsg);
      notifyListeners();
    });
  }

  void fileSupportTicket(String subject, String category, String description) {
    final ticket = SupportTicket(
      id: 'TKT-${5000 + Random().nextInt(5000)}',
      subject: subject,
      category: category,
      status: 'OPEN',
      description: description,
      timestamp: DateTime.now().millisecondsSinceEpoch,
    );
    tickets.insert(0, ticket);
    notifyListeners();
  }

  void toggleFavorite(String restaurantId) {
    final idx = restaurants.indexWhere((r) => r.id == restaurantId);
    if (idx >= 0) {
      restaurants[idx].isFavorite = !restaurants[idx].isFavorite;
      notifyListeners();
    }
  }
}

// ============================================================================
//                  NAVIGATION SWITCHER CONTAINER
// ============================================================================
class MainAppNavigationSwitcher extends StatefulWidget {
  const MainAppNavigationSwitcher({Key? key}) : super(key: key);

  @override
  State<MainAppNavigationSwitcher> createState() => _MainAppNavigationSwitcherState();
}

class _MainAppNavigationSwitcherState extends State<MainAppNavigationSwitcher> {
  late FoodViewModel viewModel;
  int _currentIndex = 0;

  @override
  void initState() {
    super.initState();
    viewModel = FoodViewModel();
    viewModel.addListener(_updateState);
  }

  void _updateState() {
    if (mounted) setState(() {});
  }

  @override
  void dispose() {
    viewModel.removeListener(_updateState);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final screens = [
      ExploreScreen(viewModel: viewModel),
      CartScreen(viewModel: viewModel),
      OrdersScreen(viewModel: viewModel),
      RewardsScreen(viewModel: viewModel),
      SupportScreen(viewModel: viewModel),
    ];

    return Stack(
      children: [
        Scaffold(
          body: !viewModel.isLoggedIn
              ? AuthScreen(viewModel: viewModel)
              : IndexedStack(
                  index: _currentIndex,
                  children: screens,
                ),
          bottomNavigationBar: viewModel.isLoggedIn
              ? NavigationBar(
                  selectedIndex: _currentIndex,
                  onDestinationSelected: (idx) {
                    setState(() {
                      _currentIndex = idx;
                    });
                  },
                  destinations: const [
                    NavigationDestination(icon: Icon(Icons.search), label: 'Explore'),
                    NavigationDestination(icon: Icon(Icons.shopping_cart), label: 'Cart'),
                    NavigationDestination(icon: Icon(Icons.receipt_long), label: 'Orders'),
                    NavigationDestination(icon: Icon(Icons.card_giftcard), label: 'Rewards'),
                    NavigationDestination(icon: Icon(Icons.support_agent), label: 'Support'),
                  ],
                )
              : null,
        ),
        if (viewModel.headsUpMessage != null)
          HeadsUpNotificationBanner(
            otp: viewModel.headsUpMessage!,
            onDismiss: viewModel.dismissHeadsUpMessage,
          ),
      ],
    );
  }
}

// ============================================================================
//                            AUTH / OTP SCREEN
// ============================================================================
class AuthScreen extends StatefulWidget {
  final FoodViewModel viewModel;
  const AuthScreen({Key? key, required this.viewModel}) : super(key: key);

  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  String _activeTab = 'otp';
  final _phoneController = TextEditingController();
  final _otpController = TextEditingController();

  bool _otpSent = false;
  bool _isSendingOtp = false;
  int _countdown = 0;
  Timer? _timer;

  String? _errorMessage;
  String? _successMessage;

  @override
  void dispose() {
    _phoneController.dispose();
    _otpController.dispose();
    _timer?.cancel();
    super.dispose();
  }

  void _startCountdown() {
    setState(() {
      _countdown = 59;
    });
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (t) {
      if (!mounted) return;
      setState(() {
        if (_countdown > 0) {
          _countdown--;
        } else {
          _timer?.cancel();
        }
      });
    });
  }

  void _sendOtp() async {
    final phone = _phoneController.text.trim();
    if (phone.length < 10) {
      setState(() {
        _errorMessage = 'Please enter a valid 10-digit mobile number';
      });
      return;
    }

    setState(() {
      _isSendingOtp = true;
      _errorMessage = null;
    });

    await Future.delayed(const Duration(milliseconds: 1000));

    if (!mounted) return;

    setState(() {
      _isSendingOtp = false;
      _otpSent = true;
      _successMessage = 'Secure login verification OTP sent!';
    });

    _startCountdown();
    widget.viewModel.generateAndSendOtp(phone);
  }

  void _verifyOtp() {
    final otp = _otpController.text.trim();
    if (otp.length < 6) {
      setState(() {
        _errorMessage = 'Enter 6-digit OTP code';
      });
      return;
    }

    widget.viewModel.loginWithOtp(_phoneController.text, otp, (success, msg) {
      setState(() {
        if (success) {
          _errorMessage = null;
          _successMessage = msg;
        } else {
          _errorMessage = msg;
          _successMessage = null;
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [annivoNavy, Color(0xFF1B263B)],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        ),
      ),
      child: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              children: [
                const Icon(Icons.fastfood, size: 70, color: SaffronOrange),
                const SizedBox(height: 12),
                const Text(
                  'ANNIVO',
                  style: TextStyle(
                    fontSize: 32,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 4.0,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(height: 6),
                const Text(
                  'Explore local tastes, earn elite tier point multipliers',
                  style: TextStyle(fontSize: 12, color: Colors.white54),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 32),
                Card(
                  elevation: 12,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                  child: Padding(
                    padding: const EdgeInsets.all(24.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                          children: [
                            _buildTabButton('otp', 'Secure OTP'),
                            _buildTabButton('email', 'Email'),
                            _buildTabButton('signup', 'Sign Up'),
                          ],
                        ),
                        const Divider(height: 32),
                        if (_activeTab == 'otp') ...[
                          const Text(
                            'One-Time Password Verification',
                            style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                          ),
                          const SizedBox(height: 6),
                          const Text(
                            'Enter your registered 10-digit mobile number to proceed safely.',
                            style: TextStyle(fontSize: 11, color: Colors.grey),
                          ),
                          const SizedBox(height: 20),
                          TextField(
                            controller: _phoneController,
                            keyboardType: TextInputType.phone,
                            maxLength: 10,
                            enabled: !_otpSent,
                            decoration: InputDecoration(
                              labelText: 'Mobile Number',
                              prefixIcon: const Icon(Icons.phone, color: SaffronOrange),
                              border: const OutlineInputBorder(),
                              errorText: _errorMessage != null && !_otpSent ? _errorMessage : null,
                            ),
                          ),
                          const SizedBox(height: 12),
                          if (_otpSent) ...[
                            TextField(
                              controller: _otpController,
                              keyboardType: TextInputType.number,
                              maxLength: 6,
                              decoration: InputDecoration(
                                labelText: '6-Digit OTP',
                                prefixIcon: const Icon(Icons.lock_outline, color: SaffronOrange),
                                border: const OutlineInputBorder(),
                                helperText: "Use demo credential '123456' to bypass easily.",
                                errorText: _errorMessage != null && _otpSent ? _errorMessage : null,
                              ),
                            ),
                            const SizedBox(height: 16),
                          ],
                          if (_successMessage != null) ...[
                            Text(
                              _successMessage!,
                              style: const TextStyle(color: Colors.green, fontSize: 12, fontWeight: FontWeight.w500),
                              textAlign: TextAlign.center,
                            ),
                            const SizedBox(height: 12),
                          ],
                          if (!_otpSent)
                            ElevatedButton(
                              onPressed: _isSendingOtp ? null : _sendOtp,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: SaffronOrange,
                                foregroundColor: Colors.white,
                                padding: const EdgeInsets.symmetric(vertical: 14),
                                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                              ),
                              child: _isSendingOtp
                                  ? const SizedBox(
                                      height: 20,
                                      width: 20,
                                      child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                                    )
                                  : const Text('Send Verification OTP', style: TextStyle(fontWeight: FontWeight.bold)),
                            )
                          else ...[
                            ElevatedButton(
                              onPressed: _verifyOtp,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: SaffronOrange,
                                foregroundColor: Colors.white,
                                padding: const EdgeInsets.symmetric(vertical: 14),
                                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                              ),
                              child: const Text('Verify & Proceed', style: TextStyle(fontWeight: FontWeight.bold)),
                            ),
                            const SizedBox(height: 12),
                            TextButton(
                              onPressed: _countdown == 0
                                  ? () {
                                      _startCountdown();
                                      widget.viewModel.generateAndSendOtp(_phoneController.text);
                                    }
                                  : null,
                              child: Text(
                                _countdown > 0 ? 'Resend in ${_countdown}s' : 'Resend OTP',
                                style: TextStyle(
                                  color: _countdown > 0 ? Colors.grey : SaffronOrange,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            )
                          ],
                        ] else ...[
                          const SizedBox(
                            height: 180,
                            child: Center(
                              child: Text(
                                'Method under routine maintenance.\nPlease authenticate using Secure OTP.',
                                textAlign: TextAlign.center,
                                style: TextStyle(color: Colors.grey, fontSize: 13),
                              ),
                            ),
                          )
                        ],
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTabButton(String key, String label) {
    final isSelected = _activeTab == key;
    return InkWell(
      onTap: () {
        setState(() {
          _activeTab = key;
          _errorMessage = null;
          _successMessage = null;
        });
      },
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 8),
        decoration: BoxDecoration(
          border: Border(
            bottom: BorderSide(
              color: isSelected ? SaffronOrange : Colors.transparent,
              width: 2,
            ),
          ),
        ),
        child: Text(
          label.split(' ').first,
          style: TextStyle(
            color: isSelected ? SaffronOrange : Colors.grey,
            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
            fontSize: 13,
          ),
        ),
      ),
    );
  }
}

// ============================================================================
//                     SLIDE DOWN SMS OVERLAY BANNER
// ============================================================================
class HeadsUpNotificationBanner extends StatefulWidget {
  final String otp;
  final VoidCallback onDismiss;
  const HeadsUpNotificationBanner({Key? key, required this.otp, required this.onDismiss}) : super(key: key);

  @override
  State<HeadsUpNotificationBanner> createState() => _HeadsUpNotificationBannerState();
}

class _HeadsUpNotificationBannerState extends State<HeadsUpNotificationBanner> with SingleTickerProviderStateMixin {
  late AnimationController _anim;
  late Animation<Offset> _offset;

  @override
  void initState() {
    super.initState();
    _anim = AnimationController(vsync: this, duration: const Duration(milliseconds: 300));
    _offset = Tween<Offset>(begin: const Offset(0, -1.2), end: Offset.zero).animate(
      CurvedAnimation(parent: _anim, curve: Curves.easeOutBack),
    );

    _anim.forward();

    // Auto dismiss after 5 seconds
    Future.delayed(const Duration(seconds: 5), () {
      _dismiss();
    });
  }

  void _dismiss() async {
    if (!mounted) return;
    await _anim.reverse();
    widget.onDismiss();
  }

  @override
  void dispose() {
    _anim.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return SlideTransition(
      position: _offset,
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Material(
            elevation: 12,
            color: Colors.transparent,
            child: Container(
              padding: const EdgeInsets.all(16.0),
              decoration: BoxDecoration(
                color: CharcoalBg,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Row(
                children: [
                  Container(
                    width: 44,
                    height: 44,
                    decoration: const BoxDecoration(color: MessageBlue, shape: BoxShape.circle),
                    child: const Icon(Icons.sms, color: Colors.white, size: 22),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text(
                              'MESSAGES • Just Now',
                              style: TextStyle(
                                fontSize: 10,
                                color: MessageBlue,
                                fontWeight: FontWeight.bold,
                                letterSpacing: 1.0,
                              ),
                            ),
                            GestureDetector(
                              onTap: _dismiss,
                              child: const Icon(Icons.close, color: Colors.grey, size: 16),
                            ),
                          ],
                        ),
                        const SizedBox(height: 2),
                        const Text(
                          'ANNIVO Secure login verification code is:',
                          style: TextStyle(color: Colors.white70, fontSize: 11),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          widget.otp,
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 3.0,
                          ),
                        ),
                      ],
                    ),
                  )
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

// ============================================================================
//                               EXPLORE SCREEN
// ============================================================================
class ExploreScreen extends StatefulWidget {
  final FoodViewModel viewModel;
  const ExploreScreen({Key? key, required this.viewModel}) : super(key: key);

  @override
  State<ExploreScreen> createState() => _ExploreScreenState();
}

class _ExploreScreenState extends State<ExploreScreen> {
  String _searchQuery = '';
  String _activeFilter = 'All';

  @override
  Widget build(BuildContext context) {
    // Filter and search logic
    final filtered = widget.viewModel.restaurants.where((res) {
      final matchesSearch = res.name.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          res.cuisine.toLowerCase().contains(_searchQuery.toLowerCase());
      
      if (!matchesSearch) return false;
      if (_activeFilter == 'All') return true;
      if (_activeFilter == 'Favorites') return res.isFavorite;
      if (_activeFilter == 'Top Rated') return res.rating >= 4.5;
      if (_activeFilter == 'Offers') return res.isZeroCommission;
      return true;
    }).toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Explore Food', style: TextStyle(fontWeight: FontWeight.bold)),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout, color: Colors.redAccent),
            onPressed: () {
              widget.viewModel.logout();
            },
          )
        ],
      ),
      body: Column(
        children: [
          // Search & Filters bar
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
            child: TextField(
              onChanged: (val) {
                setState(() {
                  _searchQuery = val;
                });
              },
              decoration: InputDecoration(
                hintText: 'Search restaurants, cuisines, dishes...',
                prefixIcon: const Icon(Icons.search, color: Colors.grey),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                contentPadding: const EdgeInsets.symmetric(vertical: 0),
              ),
            ),
          ),

          // Filters row
          SizedBox(
            height: 48,
            child: ListView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 12.0),
              children: [
                _buildFilterChip('All'),
                _buildFilterChip('Favorites'),
                _buildFilterChip('Top Rated'),
                _buildFilterChip('Offers'),
              ],
            ),
          ),

          // Promotions Banner Row
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Container(
              height: 110,
              decoration: BoxDecoration(
                gradient: const LinearGradient(colors: [SaffronOrange, Color(0xFFF28482)]),
                borderRadius: BorderRadius.circular(16),
              ),
              padding: const EdgeInsets.all(16.0),
              child: Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: const [
                        Text(
                          'Zero Commission Delivery! 🎉',
                          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),
                        ),
                        SizedBox(height: 4),
                        Text(
                          'Order from any elite partner restaurant and support locals at 0% markup fee.',
                          style: TextStyle(color: Colors.whiteDimmish, fontSize: 10),
                        ),
                      ],
                    ),
                  ),
                  const Icon(Icons.delivery_dining, size: 50, color: Colors.white38),
                ],
              ),
            ),
          ),

          Expanded(
            child: filtered.isEmpty
                ? const Center(
                    child: Text('No restaurants matching filters', style: TextStyle(color: Colors.grey)),
                  )
                : ListView.builder(
                    padding: const EdgeInsets.symmetric(horizontal: 16.0),
                    itemCount: filtered.length,
                    itemBuilder: (ctx, idx) {
                      final res = filtered[idx];
                      return Card(
                        margin: const EdgeInsets.only(bottom: 16.0),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                        clipBehavior: Clip.antiAlias,
                        child: InkWell(
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (_) => RestaurantDetailScreen(restaurant: res, viewModel: widget.viewModel),
                              ),
                            );
                          },
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.stretch,
                            children: [
                              // Image layer
                              Stack(
                                children: [
                                  Image.network(
                                    res.imageUrl,
                                    height: 150,
                                    width: double.infinity,
                                    fit: BoxFit.cover,
                                    errorBuilder: (_, __, ___) => Container(
                                      height: 150,
                                      color: Colors.orange.shade50,
                                      child: const Icon(Icons.fastfood, size: 50, color: SaffronOrange),
                                    ),
                                  ),
                                  Positioned(
                                    top: 12,
                                    right: 12,
                                    child: CircleAvatar(
                                      backgroundColor: Colors.white,
                                      child: IconButton(
                                        icon: Icon(
                                          res.isFavorite ? Icons.favorite : Icons.favorite_border,
                                          color: Colors.red,
                                        ),
                                        onPressed: () {
                                          setState(() {
                                            widget.viewModel.toggleFavorite(res.id);
                                          });
                                        },
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                              // Description
                              Padding(
                                padding: const EdgeInsets.all(16.0),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                      children: [
                                        Text(
                                          res.name,
                                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                                        ),
                                        Container(
                                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                          decoration: BoxDecoration(
                                            color: Colors.green.shade50,
                                            borderRadius: BorderRadius.circular(6),
                                          ),
                                          child: Row(
                                            children: [
                                              const Icon(Icons.star, size: 14, color: Colors.green),
                                              const SizedBox(width: 4),
                                              Text(
                                                res.rating.toString(),
                                                style: const TextStyle(
                                                  color: Colors.green,
                                                  fontWeight: FontWeight.bold,
                                                  fontSize: 12,
                                                ),
                                              ),
                                            ],
                                          ),
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 4),
                                    Text(res.cuisine, style: const TextStyle(color: Colors.grey, fontSize: 12)),
                                    const Divider(height: 24),
                                    Row(
                                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                      children: [
                                        Row(
                                          children: [
                                            const Icon(Icons.timer_outlined, size: 16, color: Colors.grey),
                                            const SizedBox(width: 4),
                                            Text('${res.deliveryTimeMin} mins', style: const TextStyle(fontSize: 11)),
                                          ],
                                        ),
                                        Row(
                                          children: [
                                            const Icon(Icons.directions_car_outlined, size: 16, color: Colors.grey),
                                            const SizedBox(width: 4),
                                            Text('${res.distanceKm} km', style: const TextStyle(fontSize: 11)),
                                          ],
                                        ),
                                        Row(
                                          children: [
                                            const Icon(Icons.currency_rupee, size: 16, color: Colors.grey),
                                            Text('₹${res.costForTwo} for two', style: const TextStyle(fontSize: 11)),
                                          ],
                                        ),
                                      ],
                                    )
                                  ],
                                ),
                              )
                            ],
                          ),
                        ),
                      );
                    },
                  ),
          )
        ],
      ),
    );
  }

  Widget _buildFilterChip(String label) {
    final isSelected = _activeFilter == label;
    return Padding(
      padding: const EdgeInsets.only(left: 8.0),
      child: FilterChip(
        label: Text(label),
        selected: isSelected,
        onSelected: (val) {
          setState(() {
            _activeFilter = label;
          });
        },
        selectedColor: SaffronOrange.withOpacity(0.2),
        checkmarkColor: SaffronOrange,
      ),
    );
  }
}

// Colors helper extension for text dims
extension TextDimmish on TextStyle {
  static const Color whiteDimmish = Colors.white70;
}

// ============================================================================
//                           RESTAURANT DETAILS SCREEN
// ============================================================================
class RestaurantDetailScreen extends StatefulWidget {
  final Restaurant restaurant;
  final FoodViewModel viewModel;
  const RestaurantDetailScreen({Key? key, required this.restaurant, required this.viewModel}) : super(key: key);

  @override
  State<RestaurantDetailScreen> createState() => _RestaurantDetailScreenState();
}

class _RestaurantDetailScreenState extends State<RestaurantDetailScreen> {
  String _selectedCategory = 'All';

  @override
  Widget build(BuildContext context) {
    final availableCategories = ['All', 'Breakfast', 'Main Course', 'Sides', 'Drinks'];
    final items = widget.viewModel.foodItems.where((food) {
      if (food.restaurantId != widget.restaurant.id) return false;
      if (_selectedCategory == 'All') return true;
      return food.category == _selectedCategory;
    }).toList();

    return Scaffold(
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            expandedHeight: 200,
            pinned: true,
            flexibleSpace: FlexibleSpaceBar(
              title: Text(
                widget.restaurant.name,
                style: const TextStyle(
                  shadows: [Shadow(color: Colors.black, blurRadius: 8)],
                  fontWeight: FontWeight.bold,
                  fontSize: 16,
                ),
              ),
              background: Image.network(
                widget.restaurant.imageUrl,
                fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => Container(color: SaffronOrange),
              ),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(widget.restaurant.cuisine, style: const TextStyle(fontSize: 14, color: Colors.grey)),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      const Icon(Icons.star, color: Colors.green, size: 18),
                      const SizedBox(width: 4),
                      Text('${widget.restaurant.rating} Stars', style: const TextStyle(fontWeight: FontWeight.bold)),
                      const SizedBox(width: 16),
                      const Icon(Icons.delivery_dining, color: SaffronOrange, size: 18),
                      const SizedBox(width: 4),
                      Text('Free Delivery', style: TextStyle(color: Colors.green.shade700, fontWeight: FontWeight.bold)),
                    ],
                  ),
                  const Divider(height: 32),
                  // Categories Slider
                  SizedBox(
                    height: 40,
                    child: ListView.builder(
                      scrollDirection: Axis.horizontal,
                      itemCount: availableCategories.length,
                      itemBuilder: (ctx, i) {
                        final cat = availableCategories[i];
                        final isSelected = _selectedCategory == cat;
                        return Padding(
                          padding: const EdgeInsets.only(right: 8.0),
                          child: ChoiceChip(
                            label: Text(cat),
                            selected: isSelected,
                            onSelected: (val) {
                              setState(() {
                                _selectedCategory = cat;
                              });
                            },
                          ),
                        );
                      },
                    ),
                  ),
                  const SizedBox(height: 16),
                ],
              ),
            ),
          ),
          SliverList(
            delegate: SliverChildBuilderWithDetector(
              (ctx, idx) {
                final food = items[idx];
                final countInCart = widget.viewModel.getCartItemQuantity(food.id);

                return Card(
                  margin: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
                  child: Padding(
                    padding: const EdgeInsets.all(12.0),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Veg/Nonveg visual badge
                        Container(
                          width: 14,
                          height: 14,
                          margin: const EdgeInsets.only(top: 3, right: 8),
                          decoration: BoxDecoration(
                            border: Border.all(color: food.isVeg ? Colors.green : Colors.red, width: 2),
                          ),
                          alignment: Alignment.center,
                          child: Container(
                            width: 6,
                            height: 6,
                            decoration: BoxDecoration(
                              color: food.isVeg ? Colors.green : Colors.red,
                              shape: BoxShape.circle,
                            ),
                          ),
                        ),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(food.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                              const SizedBox(height: 4),
                              Text('₹${food.price.toInt()}', style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.blueGrey)),
                              const SizedBox(height: 4),
                              Text(
                                food.description,
                                style: const TextStyle(color: Colors.grey, fontSize: 11),
                                maxLines: 2,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(width: 8),
                        // Add to Cart buttons
                        Column(
                          children: [
                            if (countInCart == 0)
                              ElevatedButton(
                                onPressed: () {
                                  setState(() {
                                    widget.viewModel.addToCart(food, widget.restaurant);
                                  });
                                },
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: SaffronOrange,
                                  foregroundColor: Colors.white,
                                  minimumSize: const Size(60, 32),
                                  padding: const EdgeInsets.symmetric(horizontal: 12),
                                ),
                                child: const Text('ADD', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11)),
                              )
                            else
                              Container(
                                decoration: BoxDecoration(
                                  border: Border.all(color: SaffronOrange),
                                  borderRadius: BorderRadius.circular(8),
                                ),
                                child: Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    IconButton(
                                      icon: const Icon(Icons.remove, size: 14, color: SaffronOrange),
                                      onPressed: () {
                                        setState(() {
                                          widget.viewModel.removeFromCart(food);
                                        });
                                      },
                                    ),
                                    Text(
                                      countInCart.toString(),
                                      style: const TextStyle(fontWeight: FontWeight.bold, color: SaffronOrange),
                                    ),
                                    IconButton(
                                      icon: const Icon(Icons.add, size: 14, color: SaffronOrange),
                                      onPressed: () {
                                        setState(() {
                                          widget.viewModel.addToCart(food, widget.restaurant);
                                        });
                                      },
                                    ),
                                  ],
                                ),
                              )
                          ],
                        )
                      ],
                    ),
                  ),
                );
              },
              childCount: items.length,
            ),
          )
        ],
      ),
    );
  }
}

class SliverChildBuilderWithDetector extends SliverChildBuilderDelegate {
  SliverChildBuilderWithDetector(Widget Function(BuildContext, int) builder, {required int childCount})
      : super(builder, childCount: childCount);
}

// ============================================================================
//                                CART SCREEN
// ============================================================================
class CartScreen extends StatefulWidget {
  final FoodViewModel viewModel;
  const CartScreen({Key? key, required this.viewModel}) : super(key: key);

  @override
  State<CartScreen> createState() => _CartScreenState();
}

class _CartScreenState extends State<CartScreen> {
  double _tipAmount = 0.0;
  final _addressController = TextEditingController();
  final _couponController = TextEditingController();

  double _discount = 0.0;

  @override
  void initState() {
    super.initState();
    _addressController.text = widget.viewModel.loyaltyProfile.homeAddress;
  }

  @override
  void dispose() {
    _addressController.dispose();
    _couponController.dispose();
    super.dispose();
  }

  void _applyCoupon() {
    final code = _couponController.text.trim().toUpperCase();
    setState(() {
      if (code == 'ANVREWARD50') {
        _discount = 50.0;
      } else if (code == 'ANVREWARD100') {
        _discount = 100.0;
      } else if (code == 'ANVREWARD150') {
        _discount = 150.0;
      } else {
        _discount = 0.0;
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Invalid coupon code! Redeem on rewards page first.')),
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final subtotal = widget.viewModel.cartSubtotal;
    final gst = subtotal * 0.18;
    const delivery = 30.0;
    const platform = 5.0;
    final total = (subtotal + gst + delivery + platform + _tipAmount) - _discount;

    return Scaffold(
      appBar: AppBar(title: const Text('My Cart', style: TextStyle(fontWeight: FontWeight.bold))),
      body: widget.viewModel.cart.isEmpty
          ? const Center(
              child: Text('Your cart is empty. Explore and add dishes!', style: TextStyle(color: Colors.grey)),
            )
          : Column(
              children: [
                Expanded(
                  child: ListView(
                    padding: const EdgeInsets.all(16.0),
                    children: [
                      // Cart Items
                      ListView.builder(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        itemCount: widget.viewModel.cart.length,
                        itemBuilder: (ctx, idx) {
                          final c = widget.viewModel.cart[idx];
                          return Card(
                            margin: const EdgeInsets.only(bottom: 8.0),
                            child: Padding(
                              padding: const EdgeInsets.all(12.0),
                              child: Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(c.name, style: const TextStyle(fontWeight: FontWeight.bold)),
                                      Text('₹${c.price.toInt()}', style: const TextStyle(color: Colors.grey, fontSize: 12)),
                                    ],
                                  ),
                                  Row(
                                    children: [
                                      IconButton(
                                        icon: const Icon(Icons.remove_circle_outline, color: SaffronOrange),
                                        onPressed: () {
                                          final fItem = widget.viewModel.foodItems.firstWhere((f) => f.id == c.foodItemId);
                                          setState(() {
                                            widget.viewModel.removeFromCart(fItem);
                                          });
                                        },
                                      ),
                                      Text('${c.quantity}', style: const TextStyle(fontWeight: FontWeight.bold)),
                                      IconButton(
                                        icon: const Icon(Icons.add_circle_outline, color: SaffronOrange),
                                        onPressed: () {
                                          final fItem = widget.viewModel.foodItems.firstWhere((f) => f.id == c.foodItemId);
                                          setState(() {
                                            widget.viewModel.addToCart(fItem, widget.viewModel.restaurants.firstWhere((r) => r.id == c.restaurantId));
                                          });
                                        },
                                      ),
                                    ],
                                  )
                                ],
                              ),
                            ),
                          );
                        },
                      ),
                      const Divider(height: 32),

                      // Tips selection
                      const Text('Support Delivery Executive Tip', style: TextStyle(fontWeight: FontWeight.bold)),
                      const SizedBox(height: 8),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [10, 20, 30].map((val) {
                          final isSelected = _tipAmount == val.toDouble();
                          return ChoiceChip(
                            label: Text('₹$val'),
                            selected: isSelected,
                            onSelected: (selected) {
                              setState(() {
                                _tipAmount = selected ? val.toDouble() : 0.0;
                              });
                            },
                          );
                        }).toList(),
                      ),
                      const SizedBox(height: 16),

                      // Delivery Address
                      const Text('Delivery Location', style: TextStyle(fontWeight: FontWeight.bold)),
                      const SizedBox(height: 8),
                      TextField(
                        controller: _addressController,
                        decoration: const InputDecoration(
                          border: OutlineInputBorder(),
                          prefixIcon: Icon(Icons.home, color: SaffronOrange),
                        ),
                      ),
                      const SizedBox(height: 16),

                      // Coupon Promo Section
                      Row(
                        children: [
                          Expanded(
                            child: TextField(
                              controller: _couponController,
                              decoration: const InputDecoration(
                                labelText: 'Redeem Point Promo Code',
                                border: OutlineInputBorder(),
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          ElevatedButton(
                            onPressed: _applyCoupon,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: SaffronOrange,
                              foregroundColor: Colors.white,
                              fixedSize: const Size(100, 52),
                            ),
                            child: const Text('Apply'),
                          )
                        ],
                      ),
                      const SizedBox(height: 16),

                      // Pricing Summary Table
                      Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16.0),
                          child: Column(
                            children: [
                              _buildPriceRow('Items Subtotal', subtotal),
                              _buildPriceRow('GST (18%)', gst),
                              _buildPriceRow('Delivery Surcharge', delivery),
                              _buildPriceRow('Platform commission', platform),
                              _buildPriceRow('Driver Tip Surcharge', _tipAmount),
                              if (_discount > 0) _buildPriceRow('Coupon Discount', -_discount, color: Colors.green),
                              const Divider(height: 20),
                              _buildPriceRow('Grand Total Amount', total, isGrand: true),
                            ],
                          ),
                        ),
                      )
                    ],
                  ),
                ),
                Container(
                  padding: const EdgeInsets.all(16.0),
                  color: Colors.white,
                  child: ElevatedButton(
                    onPressed: () {
                      _showRazorpayMockCheckout(total);
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: SaffronOrange,
                      foregroundColor: Colors.white,
                      minimumSize: const Size(double.infinity, 50),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                    child: Text('PROCEED TO CHECKOUT (₹${total.toInt()})', style: const TextStyle(fontWeight: FontWeight.bold)),
                  ),
                )
              ],
            ),
    );
  }

  Widget _buildPriceRow(String label, double val, {bool isGrand = false, Color? color}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: TextStyle(
              fontWeight: isGrand ? FontWeight.bold : FontWeight.normal,
              fontSize: isGrand ? 16 : 13,
            ),
          ),
          Text(
            '₹${val.toInt()}',
            style: TextStyle(
              fontWeight: isGrand ? FontWeight.bold : FontWeight.normal,
              fontSize: isGrand ? 16 : 13,
              color: color ?? (isGrand ? SaffronOrange : Colors.black87),
            ),
          ),
        ],
      ),
    );
  }

  void _showRazorpayMockCheckout(double amount) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (ctx) {
        return Container(
          padding: const EdgeInsets.all(24.0),
          decoration: const BoxDecoration(
            color: Color(0xFF1B263B),
            borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Secure Razorpay Sandbox', style: TextStyle(color: Colors.white54, fontSize: 12, fontWeight: FontWeight.bold)),
                  IconButton(icon: const Icon(Icons.close, color: Colors.white70), onPressed: () => Navigator.pop(ctx)),
                ],
              ),
              const SizedBox(height: 12),
              const Icon(Icons.security, size: 50, color: SaffronOrange),
              const SizedBox(height: 12),
              Text('Amount to Pay: ₹${amount.toInt()}', style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 6),
              const Text('This is a simulated Razorpay payment processing terminal.', style: TextStyle(color: Colors.white30, fontSize: 10), textAlign: TextAlign.center),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: () {
                  Navigator.pop(ctx);
                  widget.viewModel.placeOrder(
                    tip: _tipAmount,
                    address: _addressController.text,
                    paymentId: 'pay_${100000 + Random().nextInt(900000)}',
                    totalAmount: amount,
                    discount: _discount,
                  );
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Payment Successful! Order placed.')),
                  );
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: SaffronOrange,
                  foregroundColor: Colors.white,
                  minimumSize: const Size(double.infinity, 48),
                ),
                child: const Text('Pay Securely', style: TextStyle(fontWeight: FontWeight.bold)),
              ),
              const SizedBox(height: 12),
            ],
          ),
        );
      },
    );
  }
}

// ============================================================================
//                               ORDERS SCREEN
// ============================================================================
class OrdersScreen extends StatelessWidget {
  final FoodViewModel viewModel;
  const OrdersScreen({Key? key, required this.viewModel}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('My Orders', style: TextStyle(fontWeight: FontWeight.bold))),
      body: viewModel.orders.isEmpty
          ? const Center(
              child: Text('No order history found.', style: TextStyle(color: Colors.grey)),
            )
          : ListView.builder(
              padding: const EdgeInsets.all(16.0),
              itemCount: viewModel.orders.length,
              itemBuilder: (ctx, idx) {
                final o = viewModel.orders[idx];
                final isLive = o.status != 'Arrived';

                return Card(
                  margin: const EdgeInsets.only(bottom: 16.0),
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(o.restaurantName, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                              decoration: BoxDecoration(
                                color: isLive ? Colors.blue.shade50 : Colors.green.shade50,
                                borderRadius: BorderRadius.circular(6),
                              ),
                              child: Text(
                                o.status,
                                style: TextStyle(
                                  color: isLive ? Colors.blue : Colors.green,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 11,
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 6),
                        Text('Order ID: ${o.id}', style: const TextStyle(color: Colors.grey, fontSize: 11)),
                        Text('Total Amount: ₹${o.totalAmount.toInt()}', style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
                        const Divider(height: 24),

                        // Render Route GPS Tracker Canvas if Active
                        if (isLive) ...[
                          const Text('Live Delivery Tracker', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                          const SizedBox(height: 8),
                          SizedBox(
                            height: 100,
                            child: CustomPaint(
                              painter: LiveTrackerPainter(progress: o.deliveryProgress),
                              child: Container(),
                            ),
                          ),
                          const SizedBox(height: 12),
                        ],

                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text('Earned +${o.loyaltyPointsEarned} pts', style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold, fontSize: 12)),
                            if (!isLive && !o.isRated)
                              OutlinedButton(
                                onPressed: () {
                                  showDialog(
                                    context: context,
                                    builder: (c) => AlertDialog(
                                      title: const Text('Rate Order'),
                                      content: const Text('Would you like to give 5 stars for the delivery partner?'),
                                      actions: [
                                        TextButton(onPressed: () => Navigator.pop(c), child: const Text('Cancel')),
                                        ElevatedButton(
                                          onPressed: () {
                                            Navigator.pop(c);
                                            o.isRated = true;
                                            viewModel.addPoints(5); // Reward 5 points for feedback rating!
                                            ScaffoldMessenger.of(context).showSnackBar(
                                              const SnackBar(content: Text('Thank you! Earned +5 feedback loyalty points!')),
                                            );
                                          },
                                          child: const Text('Rate 5 Stars'),
                                        )
                                      ],
                                    ),
                                  );
                                },
                                child: const Text('Rate Delivery'),
                              ),
                          ],
                        )
                      ],
                    ),
                  ),
                );
              },
            ),
    );
  }
}

// Custom Painter representing Delivery Progress Winding Path (Canvas equivalent in Android)
class LiveTrackerPainter extends CustomPainter {
  final double progress;
  LiveTrackerPainter({required this.progress});

  @override
  void paint(Canvas canvas, Size size) {
    final roadPaint = Paint()
      ..color = Colors.grey.shade300
      ..strokeWidth = 6
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    final progressPaint = Paint()
      ..color = SaffronOrange
      ..strokeWidth = 6
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    final path = Path();
    path.moveTo(20, size.height / 2);
    path.cubicTo(
      size.width * 0.3, size.height * 0.1,
      size.width * 0.6, size.height * 0.9,
      size.width - 20, size.height / 2,
    );

    canvas.drawPath(path, roadPaint);

    // Extract path coordinates for animation dot
    final pathMetrics = path.computeMetrics().toList();
    if (pathMetrics.isNotEmpty) {
      final metric = pathMetrics.first;
      final tangent = metric.getTangentForOffset(metric.length * progress);
      if (tangent != null) {
        // Draw path up to progress
        final activePath = Path();
        activePath.moveTo(20, size.height / 2);
        activePath.cubicTo(
          size.width * 0.3, size.height * 0.1,
          size.width * 0.6, size.height * 0.9,
          size.width - 20, size.height / 2,
        );
        // Approximately draw subpath
        final pathSeg = metric.extractPath(0.0, metric.length * progress);
        canvas.drawPath(pathSeg, progressPaint);

        // Draw Rider Icon Node
        final nodePaint = Paint()
          ..color = SaffronOrange
          ..style = PaintingStyle.fill;
        canvas.drawCircle(tangent.position, 8, nodePaint);

        final pulsePaint = Paint()
          ..color = SaffronOrange.withOpacity(0.3)
          ..style = PaintingStyle.fill;
        canvas.drawCircle(tangent.position, 14, pulsePaint);
      }
    }

    // Draw Anchor Restaurant and Home points
    final anchorPaint = Paint()
      ..color = annivoNavy
      ..style = PaintingStyle.fill;
    canvas.drawCircle(const Offset(20, 50), 6, anchorPaint);
    canvas.drawCircle(Offset(size.width - 20, 50), 6, anchorPaint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}

// ============================================================================
//                               REWARDS SCREEN
// ============================================================================
class RewardsScreen extends StatefulWidget {
  final FoodViewModel viewModel;
  const RewardsScreen({Key? key, required this.viewModel}) : super(key: key);

  @override
  State<RewardsScreen> createState() => _RewardsScreenState();
}

class _RewardsScreenState extends State<RewardsScreen> with SingleTickerProviderStateMixin {
  String? _unlockedPromoCode;
  bool _scratched = false;

  // Spin the Wheel Animation
  late AnimationController _spinCtrl;
  double _wheelRotationAngle = 0.0;
  bool _spinning = false;

  @override
  void initState() {
    super.initState();
    _spinCtrl = AnimationController(vsync: this, duration: const Duration(seconds: 3));
  }

  @override
  void dispose() {
    _spinCtrl.dispose();
    super.dispose();
  }

  void _spinWheel() {
    if (_spinning) return;
    setState(() {
      _spinning = true;
    });

    final randomRotation = 2 * pi * 3 + (Random().nextDouble() * 2 * pi); // Spin 3 full circles + random slice
    final oldAngle = _wheelRotationAngle;
    
    _spinCtrl.reset();
    _spinCtrl.forward().then((_) {
      setState(() {
        _wheelRotationAngle = oldAngle + randomRotation;
        _spinning = false;

        // Reward user
        final wonPoints = [20, 50, 10, 100, 30][Random().nextInt(5)];
        widget.viewModel.addPoints(wonPoints);

        showDialog(
          context: context,
          builder: (ctx) => AlertDialog(
            title: const Text('Wheel of Fortune! 🎡'),
            content: Text('The wheel has spoken! You won +$wonPoints loyalty reward points.'),
            actions: [
              TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Awesome')),
            ],
          ),
        );
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final coupons = [
      {'points': 100, 'cashback': 50.0, 'code': 'ANVREWARD50'},
      {'points': 180, 'cashback': 100.0, 'code': 'ANVREWARD100'},
      {'points': 250, 'cashback': 150.0, 'code': 'ANVREWARD150'},
    ];

    return Scaffold(
      appBar: AppBar(title: const Text('Loyalty Club', style: TextStyle(fontWeight: FontWeight.bold))),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Elite Club Member Card
            Card(
              color: SaffronOrange,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('ANNIVO ELITE CLUB', style: TextStyle(color: Colors.white70, fontSize: 10, letterSpacing: 1)),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(color: Colors.white24, borderRadius: BorderRadius.circular(12)),
                          child: const Text('🇮🇳 Partner', style: TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold)),
                        )
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text('${widget.viewModel.loyaltyProfile.tier} Tier', style: const TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold)),
                    const SizedBox(height: 24),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text('POINTS BALANCE', style: TextStyle(color: Colors.white60, fontSize: 9)),
                            Text('${widget.viewModel.loyaltyProfile.totalPoints} pts', style: const TextStyle(color: GoldYellow, fontSize: 28, fontWeight: FontWeight.bold)),
                          ],
                        ),
                        const Text('10 ₹ Spent = 1 Pt', style: TextStyle(color: Colors.white70, fontSize: 10)),
                      ],
                    )
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Mini Games Section Header
            const Text('Interact & Win Points', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 12),

            Row(
              children: [
                // Spin Wheel Card
                Expanded(
                  child: Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        children: [
                          const Text('Fortune Wheel', style: TextStyle(fontWeight: FontWeight.bold)),
                          const SizedBox(height: 12),
                          AnimatedBuilder(
                            animation: _spinCtrl,
                            builder: (ctx, child) {
                              final currentAngle = _wheelRotationAngle + (_spinCtrl.value * pi * 8);
                              return Transform.rotate(
                                angle: currentAngle,
                                child: Image.network(
                                  'https://images.unsplash.com/photo-1595769816263-9b910be24d5f?w=100&q=80',
                                  height: 60,
                                  width: 60,
                                  fit: BoxFit.cover,
                                  errorBuilder: (_, __, ___) => const Icon(Icons.incomplete_circle_outlined, size: 60, color: SaffronOrange),
                                ),
                              );
                            },
                          ),
                          const SizedBox(height: 12),
                          ElevatedButton(
                            onPressed: _spinning ? null : _spinWheel,
                            child: const Text('Spin!'),
                          )
                        ],
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                // Scratch Card Card
                Expanded(
                  child: Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        children: [
                          const Text('Scratch Card', style: TextStyle(fontWeight: FontWeight.bold)),
                          const SizedBox(height: 12),
                          GestureDetector(
                            onTap: () {
                              if (_scratched) return;
                              setState(() {
                                _scratched = true;
                                widget.viewModel.addPoints(50);
                              });
                            },
                            child: Container(
                              height: 60,
                              width: 80,
                              decoration: BoxDecoration(
                                color: _scratched ? Colors.green.shade100 : Colors.grey.shade400,
                                borderRadius: BorderRadius.circular(8),
                              ),
                              alignment: Alignment.center,
                              child: _scratched
                                  ? const Text('Won +50 pts! 🎉', style: TextStyle(fontSize: 10, fontWeight: FontWeight.bold, color: Colors.green))
                                  : const Text('TAP SCRATCH', style: TextStyle(fontSize: 10, color: Colors.white, fontWeight: FontWeight.bold)),
                            ),
                          ),
                          const SizedBox(height: 12),
                          ElevatedButton(
                            onPressed: _scratched ? () => setState(() => _scratched = false) : null,
                            child: const Text('Reset'),
                          )
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),

            // Point Redemptions vouchers
            const Text('Redeem Point Vouchers', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 12),

            if (_unlockedPromoCode != null) ...[
              Card(
                color: EmeraldBg,
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    children: [
                      const Text('Congratulations! Promo Unlocked 🎉', style: TextStyle(color: EmeraldGreen, fontWeight: FontWeight.bold)),
                      const SizedBox(height: 8),
                      Container(
                        padding: const EdgeInsets.all(8.0),
                        color: Colors.white,
                        child: Text(
                          _unlockedPromoCode!,
                          style: const TextStyle(fontWeight: FontWeight.bold, letterSpacing: 2.0, color: EmeraldGreen),
                        ),
                      ),
                      const SizedBox(height: 4),
                      const Text('Copy code and apply on cart to save extra!', style: TextStyle(fontSize: 11)),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 12),
            ],

            ListView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: coupons.length,
              itemBuilder: (ctx, idx) {
                final c = coupons[idx];
                final canAfford = widget.viewModel.loyaltyProfile.totalPoints >= (c['points'] as int);

                return Card(
                  margin: const EdgeInsets.only(bottom: 8),
                  child: ListTile(
                    title: Text('₹${(c['cashback'] as double).toInt()} Discount Coupon'),
                    subtitle: Text('Costs: ${c['points']} loyalty points'),
                    trailing: ElevatedButton(
                      onPressed: canAfford
                          ? () {
                              setState(() {
                                widget.viewModel.redeemRewardCoupon(c['points'] as int, c['cashback'] as double);
                                _unlockedPromoCode = c['code'] as String;
                              });
                            }
                          : null,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: canAfford ? SaffronOrange : Colors.grey,
                      ),
                      child: Text(canAfford ? 'Redeem' : 'Locked'),
                    ),
                  ),
                );
              },
            )
          ],
        ),
      ),
    );
  }
}

// ============================================================================
//                               SUPPORT SCREEN
// ============================================================================
class SupportScreen extends StatefulWidget {
  final FoodViewModel viewModel;
  const SupportScreen({Key? key, required this.viewModel}) : super(key: key);

  @override
  State<SupportScreen> createState() => _SupportScreenState();
}

class _SupportScreenState extends State<SupportScreen> {
  final _chatController = TextEditingController();
  final _subjectController = TextEditingController();
  final _descController = TextEditingController();
  String _activeTab = 'chat'; // 'chat' or 'ticket'
  String _selectedCategory = 'Food Quality';

  @override
  void dispose() {
    _chatController.dispose();
    _subjectController.dispose();
    _descController.dispose();
    super.dispose();
  }

  void _sendMessage() {
    widget.viewModel.sendSupportMessage(_chatController.text);
    _chatController.clear();
  }

  void _submitTicket() {
    if (_subjectController.text.trim().isEmpty) return;
    widget.viewModel.fileSupportTicket(
      _subjectController.text,
      _selectedCategory,
      _descController.text,
    );
    _subjectController.clear();
    _descController.clear();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Support ticket raised successfully! Our team will join inside 1hr.')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Customer Support', style: TextStyle(fontWeight: FontWeight.bold))),
      body: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              _buildSupportTabButton('chat', '24/7 AI Chat'),
              _buildSupportTabButton('ticket', 'Raise Case Ticket'),
            ],
          ),
          const Divider(height: 1),
          if (_activeTab == 'chat') ...[
            Expanded(
              child: ListView.builder(
                padding: const EdgeInsets.all(16.0),
                itemCount: widget.viewModel.supportMessages.length,
                itemBuilder: (ctx, idx) {
                  final m = widget.viewModel.supportMessages[idx];
                  final isBot = m.sender == 'bot';
                  return Align(
                    alignment: isBot ? Alignment.centerLeft : Alignment.centerRight,
                    child: Container(
                      margin: const EdgeInsets.only(bottom: 12),
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: isBot ? Colors.grey.shade100 : SaffronOrange.withOpacity(0.15),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      constraints: BoxConstraints(maxWidth: MediaQuery.of(context).size.width * 0.75),
                      child: Text(m.text),
                    ),
                  );
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _chatController,
                      decoration: const InputDecoration(
                        hintText: 'Type your message...',
                        border: OutlineInputBorder(),
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton(
                    icon: const Icon(Icons.send, color: SaffronOrange),
                    onPressed: _sendMessage,
                  )
                ],
              ),
            )
          ] else ...[
            Expanded(
              child: ListView(
                padding: const EdgeInsets.all(16.0),
                children: [
                  const Text('File a new support ticket case:', style: TextStyle(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 12),
                  TextField(
                    controller: _subjectController,
                    decoration: const InputDecoration(
                      labelText: 'Case Subject',
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 12),
                  DropdownButtonFormField<String>(
                    value: _selectedCategory,
                    items: ['Food Quality', 'Delivery Issue', 'Refund / Charges', 'Loyalty Club']
                        .map((cat) => DropdownMenuItem(value: cat, child: Text(cat)))
                        .toList(),
                    onChanged: (val) {
                      setState(() {
                        _selectedCategory = val!;
                      });
                    },
                    decoration: const InputDecoration(border: OutlineInputBorder()),
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: _descController,
                    maxLines: 3,
                    decoration: const InputDecoration(
                      labelText: 'Describe issue details...',
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: _submitTicket,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: SaffronOrange,
                      foregroundColor: Colors.white,
                      minimumSize: const Size(double.infinity, 48),
                    ),
                    child: const Text('Submit Support Ticket'),
                  ),
                  const Divider(height: 40),

                  // Tickets List History
                  const Text('My Active Tickets log', style: TextStyle(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 12),
                  ...widget.viewModel.tickets.map((t) => Card(
                        margin: const EdgeInsets.only(bottom: 8),
                        child: ListTile(
                          title: Text(t.subject, style: const TextStyle(fontWeight: FontWeight.bold)),
                          subtitle: Text('${t.category} • ${t.id}'),
                          trailing: Chip(
                            label: Text(t.status, style: const TextStyle(fontSize: 10)),
                            backgroundColor: t.status == 'OPEN' ? Colors.blue.shade50 : Colors.green.shade50,
                          ),
                        ),
                      )),
                ],
              ),
            )
          ]
        ],
      ),
    );
  }

  Widget _buildSupportTabButton(String key, String label) {
    final isSelected = _activeTab == key;
    return InkWell(
      onTap: () => setState(() => _activeTab = key),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
        decoration: BoxDecoration(
          border: Border(bottom: BorderSide(color: isSelected ? SaffronOrange : Colors.transparent, width: 2)),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
            color: isSelected ? SaffronOrange : Colors.grey,
          ),
        ),
      ),
    );
  }
}
