(() => {
  const params = new URLSearchParams(window.location.search);
  const tourId = params.get('id');
  if (!tourId) {
    window.location.href = './tours.html';
    return;
  }

  const el = (id) => document.getElementById(id);
  const user = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

  function escapeHtml(s) {
    if (!s) return '';
    return String(s)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  // --- Header Logic ---
  const navActions = el('navActions');
  if (user) {
    const pill = document.createElement('span');
    pill.className = 'pill';
    pill.textContent = `Hi, ${user.fullName || user.email}`;
    navActions.appendChild(pill);

    const logoutBtn = document.createElement('button');
    logoutBtn.className = 'btn btn-secondary';
    logoutBtn.textContent = 'Logout';
    logoutBtn.onclick = () => TB.logout();
    navActions.appendChild(logoutBtn);
  } else {
    const loginLink = document.createElement('a');
    loginLink.className = 'btn btn-secondary';
    loginLink.href = '/pages/auth/login.html';
    loginLink.textContent = 'Login';
    navActions.appendChild(loginLink);
  }

  // --- Compare Logic ---
  const compareIds = new Set(JSON.parse(localStorage.getItem('compareIds') || '[]'));
  const addBtn = el('addCompareBtn');
  if (addBtn) {
    if (compareIds.has(Number(tourId))) {
      addBtn.disabled = true;
      addBtn.textContent = 'Added to compare';
    }
    addBtn.onclick = () => {
      compareIds.add(Number(tourId));
      localStorage.setItem('compareIds', JSON.stringify(Array.from(compareIds)));
      addBtn.disabled = true;
      addBtn.textContent = 'Added to compare';
    };
  }

  // --- Render Functions ---
  function renderCarousel(images) {
    const container = el('carousel');
    if (!images || images.length === 0) {
      container.innerHTML = '<div class="thumb">No gallery images</div>';
      return;
    }
    container.innerHTML = `<img src="${images[0]}" class="carousel-img" alt="Tour image">`;
    if (images.length > 1) {
      let idx = 0;
      setInterval(() => {
        idx = (idx + 1) % images.length;
        const img = container.querySelector('img');
        if (img) img.src = images[idx];
      }, 5000);
    }
  }

  async function loadReviews(id) {
    const list = el('reviews');
    try {
      const res = await TB.apiFetch(`/api/v1/reviews/tour/${id}`);
      const reviews = res.data || [];
      if (reviews.length === 0) return;
      list.innerHTML = '';
      reviews.forEach(r => {
        const div = document.createElement('div');
        div.className = 'review-card';
        div.innerHTML = `
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <strong>${escapeHtml(r.userFullName || 'Traveler')}</strong>
            <span style="color:var(--sun);">⭐ ${r.rating}</span>
          </div>
          <p style="margin:10px 0 0;font-size:0.92rem;color:var(--text-soft);line-height:1.6;">${escapeHtml(r.comment)}</p>
        `;
        list.appendChild(div);
      });
    } catch (err) {
      console.error('Failed to load reviews', err);
    }
  }

  function render(t) {
    el('title').textContent = t.tourName;
    el('desc').textContent = t.description;
    el('price').textContent = `$${t.price}`;
    el('rating').textContent = `⭐ ${t.rating ? t.rating.toFixed(1) : '0.0'}`;
    el('duration').textContent = `${t.duration} days`;
    el('route').textContent = `${t.startLocation} → ${t.endLocation}`;
    el('category').textContent = t.categoryName;

    renderCarousel(t.imageUrls);
    loadReviews(t.id);

    // Pills
    const pContainer = el('pills');
    const pArr = [];
    if (t.transportType) pArr.push(t.transportType);
    if (t.startLocation && t.endLocation) pArr.push(`${t.startLocation} → ${t.endLocation}`);
    pContainer.innerHTML = pArr.map(p => `<span class="pill">${escapeHtml(p)}</span>`).join('');

    // Highlights
    const hContainer = el('highlights');
    if (t.highlights && t.highlights.length > 0) {
      hContainer.innerHTML = t.highlights.map(h => `<div class="list-item">• ${escapeHtml(h)}</div>`).join('');
    } else {
      hContainer.innerHTML = '<div class="list-item">No specific highlights listed.</div>';
    }

    // Schedules
    const sContainer = el('schedules');
    if (t.schedules && t.schedules.length > 0) {
      sContainer.innerHTML = t.schedules.map(s => {
        const canBook = user && s.status === 'AVAILABLE' && s.availableSlots > 0;
        return `
          <div class="card" style="box-shadow:none;border-color:rgba(31,41,51,0.06); padding:18px;">
            <div style="display:flex;justify-content:space-between;gap:10px;flex-wrap:wrap;align-items:center;">
              <div style="font-weight:700;">${escapeHtml(s.startDate)} → ${escapeHtml(s.endDate)}</div>
              <div class="pill" style="font-size:0.75rem;">${escapeHtml(s.status)}</div>
            </div>
            <div style="display:flex; justify-content:space-between; align-items:center; margin-top:12px;">
              <div style="font-size:0.9rem;color:var(--text-soft);">Available: <strong>${s.availableSlots}</strong></div>
              ${canBook ? `<button class="btn btn-sm" onclick="window.openBooking(${s.id}, '${s.startDate}', ${t.price})">Book Now</button>` : ''}
            </div>
          </div>
        `;
      }).join('');
    } else {
      sContainer.innerHTML = '<div class="empty-state">No upcoming departures.</div>';
    }
  }

  // --- Booking Modal Logic ---
  let currentScheduleId = null;
  let basePrice = 0;
  let currentDiscount = null;

  window.openBooking = (sid, date, price) => {
    currentScheduleId = sid;
    basePrice = price;
    currentDiscount = null;
    el('selectedSchedule').textContent = date;
    el('discountInput').value = '';
    el('discountMsg').textContent = '';
    el('discountAppliedRow').style.display = 'none';
    updatePrice();
    el('bookingModal').classList.add('active');
  };

  function updatePrice() {
    const people = parseInt(el('peopleInput').value) || 1;
    let total = basePrice * people;
    const original = total;

    if (currentDiscount) {
      if (currentDiscount.discountType === 'PERCENTAGE') {
        total = total * (1 - currentDiscount.value / 100);
      } else {
        total = total - currentDiscount.value;
      }
      if (total < 0) total = 0;
      el('discountAppliedRow').style.display = 'block';
      el('originalPrice').textContent = `$${original.toFixed(2)}`;
    } else {
      el('discountAppliedRow').style.display = 'none';
    }
    el('finalPrice').textContent = `$${total.toFixed(2)}`;
  }

  el('peopleInput').oninput = updatePrice;

  el('applyDiscountBtn').onclick = async () => {
    const code = el('discountInput').value.trim();
    if (!code) return;
    
    el('discountMsg').style.color = 'var(--text-soft)';
    el('discountMsg').textContent = 'Validating...';

    try {
      const res = await TB.apiFetch(`/api/v1/public/discounts/validate/${encodeURIComponent(code)}?amount=${basePrice * (parseInt(el('peopleInput').value)||1)}`);
      if (res.data) {
        currentDiscount = res.data;
        el('discountMsg').style.color = 'var(--accent-deep)';
        el('discountMsg').textContent = `Applied: ${currentDiscount.discountType === 'PERCENTAGE' ? currentDiscount.value+'%' : '$'+currentDiscount.value} off`;
        updatePrice();
      } else {
        throw new Error('Invalid code');
      }
    } catch (err) {
      currentDiscount = null;
      el('discountMsg').style.color = '#dc2626';
      el('discountMsg').textContent = err.message || 'Invalid or expired code';
      updatePrice();
    }
  };

  el('confirmBookingBtn').onclick = async () => {
    if (!user) {
      alert('Please login first');
      return;
    }
    
    const people = parseInt(el('peopleInput').value);
    const body = {
      userId: user.id,
      scheduleId: currentScheduleId,
      numberOfPeople: people,
      totalPrice: basePrice * people, // Backend will recalculate with discount
      discountCode: currentDiscount ? currentDiscount.code : null,
      status: 'PENDING'
    };

    try {
      el('confirmBookingBtn').disabled = true;
      el('confirmBookingBtn').textContent = 'Processing...';
      const res = await TB.apiFetch('/api/v1/bookings', 'POST', body);
      alert('Booking successful! Your request is pending approval.');
      window.location.reload();
    } catch (err) {
      alert(err.message || 'Booking failed');
      el('confirmBookingBtn').disabled = false;
      el('confirmBookingBtn').textContent = 'Confirm Booking';
    }
  };

  async function loadDetail() {
    try {
      const res = await TB.apiFetch(`/api/v1/tours/${encodeURIComponent(tourId)}`);
      render(res.data);
    } catch (err) {
      document.body.innerHTML = `<div class="container"><div class="card" style="border-color:#fecaca;color:#991b1b;">${escapeHtml(err.message || 'Error loading tour detail')}</div></div>`;
    }
  }

  // --- Init ---
  loadDetail();

})();
