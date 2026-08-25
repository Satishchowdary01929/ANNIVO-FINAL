/**
 * ANNIVO Landing Page Interactive Scripts
 * Controls Savings Calculator, FAQ Accordions, Mobile Drawer, QR Modal & Confetti
 */

document.addEventListener('DOMContentLoaded', () => {
  initCalculator();
  initMobileDrawer();
  initConfettiOnBadges();
});

/* ==========================================================================
   SAVINGS CALCULATOR
   ========================================================================== */
function initCalculator() {
  const ordersSlider = document.getElementById('ordersCountSlider');
  const spendSlider = document.getElementById('avgSpendSlider');
  const ordersVal = document.getElementById('ordersCountVal');
  const spendVal = document.getElementById('avgSpendVal');
  const monthlyDisplay = document.getElementById('monthlySavingsDisplay');
  const yearlyDisplay = document.getElementById('yearlySavingsDisplay');
  const coinsDisplay = document.getElementById('coinsEarnedDisplay');

  if (!ordersSlider || !spendSlider) return;

  function updateCalculations() {
    const ordersPerWeek = parseInt(ordersSlider.value, 10);
    const avgSpend = parseInt(spendSlider.value, 10);

    ordersVal.textContent = `${ordersPerWeek} order${ordersPerWeek > 1 ? 's' : ''}`;
    spendVal.textContent = `₹${avgSpend.toLocaleString('en-IN')}`;

    // Traditional apps add ~35% markup on average
    // Total monthly spend on old apps = ordersPerWeek * 4.33 * avgSpend
    // Savings = 35% of old spend
    const ordersPerMonth = ordersPerWeek * 4.33;
    const monthlySpendTotal = ordersPerMonth * avgSpend;
    const monthlySavings = Math.round(monthlySpendTotal * 0.35);
    const yearlySavings = monthlySavings * 12;

    // Coins: 1 Coin per ₹10 spent on ANNIVO + bonus rewards
    const monthlyCoins = Math.round((monthlySpendTotal / 10) * 1.5);
    const yearlyCoins = monthlyCoins * 12;

    monthlyDisplay.textContent = `₹${monthlySavings.toLocaleString('en-IN')}`;
    yearlyDisplay.textContent = `₹${yearlySavings.toLocaleString('en-IN')}`;
    coinsDisplay.textContent = `${yearlyCoins.toLocaleString('en-IN')} Pts`;
  }

  ordersSlider.addEventListener('input', updateCalculations);
  spendSlider.addEventListener('input', updateCalculations);

  // Initialize
  updateCalculations();
}

/* ==========================================================================
   MOBILE MENU DRAWER
   ========================================================================== */
function initMobileDrawer() {
  const menuBtn = document.getElementById('mobileMenuBtn');
  const drawer = document.getElementById('mobileDrawer');

  if (menuBtn && drawer) {
    menuBtn.addEventListener('click', () => {
      drawer.classList.toggle('open');
      const isOpen = drawer.classList.contains('open');
      menuBtn.innerHTML = isOpen ? '<i class="fa-solid fa-xmark"></i>' : '<i class="fa-solid fa-bars"></i>';
    });
  }
}

function closeDrawer() {
  const drawer = document.getElementById('mobileDrawer');
  const menuBtn = document.getElementById('mobileMenuBtn');
  if (drawer) drawer.classList.remove('open');
  if (menuBtn) menuBtn.innerHTML = '<i class="fa-solid fa-bars"></i>';
}

/* ==========================================================================
   FAQ ACCORDION
   ========================================================================== */
function toggleFaq(button) {
  const item = button.closest('.faq-item');
  if (!item) return;

  const isActive = item.classList.contains('active');

  // Close all open FAQs
  document.querySelectorAll('.faq-item').forEach(el => el.classList.remove('active'));

  // Toggle clicked
  if (!isActive) {
    item.classList.add('active');
  }
}

/* ==========================================================================
   QR SCANNER MODAL
   ========================================================================== */
function openQrModal() {
  const modal = document.getElementById('qrModal');
  if (modal) {
    modal.classList.remove('hidden');
  }
}

function closeQrModal(e) {
  if (e && e.target !== e.currentTarget && !e.target.closest('.modal-close-btn')) {
    return;
  }
  const modal = document.getElementById('qrModal');
  if (modal) {
    modal.classList.add('hidden');
  }
}

/* ==========================================================================
   CONFETTI CELEBRATION
   ========================================================================== */
function initConfettiOnBadges() {
  document.querySelectorAll('.store-badge').forEach(badge => {
    badge.addEventListener('click', () => {
      if (typeof confetti === 'function') {
        confetti({
          particleCount: 70,
          spread: 60,
          origin: { y: 0.8 },
          colors: ['#F4A261', '#E76F51', '#2E7D32', '#FFB703']
        });
      }
    });
  });
}
