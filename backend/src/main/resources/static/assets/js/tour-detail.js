(() => {
  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');
  if (!id) {
    document.body.innerHTML = '<div class="container"><div class="card">Missing tour id.</div></div>';
    return;
  }

  const el = (id) => document.getElementById(id);

  function escapeHtml(s) {
    return String(s)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  function renderPills(tour) {
    const pills = [];
    if (tour.transportType) pills.push(tour.transportType);
    if (tour.startLocation && tour.endLocation) pills.push(`${tour.startLocation} → ${tour.endLocation}`);
    el('pills').innerHTML = pills.map(p => `<span class="pill">${escapeHtml(p)}</span>`).join('');
  }

  function renderGallery(images) {
    const root = el('gallery');
    if (!images || images.length === 0) {
      root.innerHTML = '<div class="thumb" style="grid-column: span 2;">Featured Tour</div>';
      return;
    }
    // Main image + small thumbs
    const main = images[0];
    const others = images.slice(1, 3);
    let html = `<div class="gallery-main"><img src="${main}" alt="Tour image" /></div>`;
    others.forEach(img => {
      html += `<div class="gallery-item"><img src="${img}" alt="Tour image small" /></div>`;
    });
    // Fill remaining if needed
    if (others.length < 2) {
      for (let i = 0; i < 2 - others.length; i++) {
        html += `<div class="gallery-item" style="background:var(--bg-soft);display:flex;align-items:center;justify-content:center;color:var(--text-faint);">More images coming</div>`;
      }
    }
    root.innerHTML = html;
  }

  function renderHighlights(list) {
    const root = el('highlights');
    if (!list || list.length === 0) {
      root.innerHTML = '<div class="section-copy" style="color:var(--text-faint);">No highlights listed for this route yet.</div>';
      return;
    }
    root.innerHTML = list.map(h => `
      <div class="list-item" style="background:rgba(255,255,255,0.02); padding: 10px 14px; border-radius: 8px; border: 1px solid rgba(255,255,255,0.05);">
        ${escapeHtml(h)}
      </div>
    `).join('');
  }

  function renderSchedules(list) {
    const root = el('schedules');
    if (!list || list.length === 0) {
      root.innerHTML = '<div class="section-copy" style="color:var(--text-faint);">No upcoming departures found.</div>';
      return;
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
    root.innerHTML = list.map(s => {
      const isAvailable = String(s.status || '').toUpperCase() === 'AVAILABLE';
      return `
        <div class="card panel" style="box-shadow:none; border: 1px solid rgba(255,255,255,0.05); padding: 14px;">
          <div style="display:flex;justify-content:space-between;gap:10px;flex-wrap:wrap;align-items:center;">
            <div>
              <div style="font-weight:700;">${escapeHtml(s.startDate || '')}</div>
              <div style="font-size:0.8rem; color:var(--text-soft);">Ends ${escapeHtml(s.endDate || '')}</div>
            </div>
            <div class="pill ${isAvailable ? '' : 'btn-secondary'}" style="font-size:0.7rem;">${escapeHtml(s.status || 'N/A')}</div>
          </div>
          <div style="margin-top:12px; display:flex; justify-content:space-between; align-items:flex-end;">
            <div style="font-size:0.85rem; color:var(--text-soft);">
              Available: <strong style="color:var(--text);">${escapeHtml(s.availableSlots ?? 0)}</strong>
            </div>
          </div>
        </div>
      `;
    }).join('');
  }

  function addToCompare(tourId) {
    const set = new Set(JSON.parse(localStorage.getItem('compareIds') || '[]'));
    set.add(Number(tourId));
    localStorage.setItem('compareIds', JSON.stringify(Array.from(set)));
    // Subtle notification instead of alert could be better, but keeping for now per original
    alert('Tour successfully added to your comparison list.');
  }

  el('addCompareBtn').onclick = () => addToCompare(id);

  async function load() {
    const res = await TB.apiFetch(`/api/v1/tours/${encodeURIComponent(id)}`, { method: 'GET' });
    const t = res.data;
    el('title').textContent = t.tourName || '';
    el('desc').textContent = t.description || '';
    el('price').textContent = t.price ? `$${t.price}` : 'TBA';
    el('rating').textContent = t.rating ? `⭐ ${t.rating.toFixed(1)}` : 'No rating';
    el('duration').textContent = t.duration ? `${t.duration} days` : 'TBA';
    el('route').textContent = (t.startLocation && t.endLocation) ? `${t.startLocation} → ${t.endLocation}` : 'TBA';
    el('category').textContent = t.categoryName || 'General';

    renderGallery(t.imageUrls);
    renderPills(t);
    renderHighlights(t.highlights);
    renderSchedules(t.schedules);
  }

  load().catch(err => {
    document.body.innerHTML = `<div class="container"><div class="card" style="border-color:#fecaca;color:#991b1b;">${escapeHtml(err.message || 'Error')}</div></div>`;
  });
})();

