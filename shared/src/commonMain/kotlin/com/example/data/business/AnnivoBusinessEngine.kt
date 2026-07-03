package com.example.data.business

import com.example.data.model.LoyaltyProfile

object AnnivoBusinessEngine {

    /**
     * Calculates the loyalty points earned based on spending (every ₹10 spent = 1 point)
     */
    fun calculateLoyaltyPoints(totalAmount: Double): Int {
        return (totalAmount / 10.0).toInt()
    }

    /**
     * Calculates the commission saved for the restaurant by choosing ANNIVO
     * ANNIVO is 100% commission-free (0% fee). Other aggregators charge 25% commission.
     */
    fun calculateSavedCommission(totalAmount: Double): Double {
        return totalAmount * 0.25
    }

    /**
     * Updates the user's loyalty profile with newly earned points, recalculates tier,
     * and accumulates the total commission saved.
     */
    fun updateProfileWithOrder(
        currentProfile: LoyaltyProfile,
        pointsEarned: Int,
        savedCommission: Double
    ): LoyaltyProfile {
        val newPoints = currentProfile.totalPoints + pointsEarned
        val newSavedCommission = currentProfile.totalSavedCommissionRs + savedCommission
        val newTier = when {
            newPoints >= 1000 -> "Platinum"
            newPoints >= 500 -> "Gold"
            newPoints >= 250 -> "Silver"
            else -> "Bronze"
        }
        return currentProfile.copy(
            totalPoints = newPoints,
            tier = newTier,
            totalSavedCommissionRs = newSavedCommission
        )
    }

    /**
     * Simulates the support bot replies, completely platform-agnostic
     */
    fun generateSupportReply(query: String): String {
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
}
