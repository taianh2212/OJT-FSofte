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
      sContainer.innerHTML = t.schedules.map(s => `
        <div class="card" style="box-shadow:none;border-color:rgba(31,41,51,0.06);">
          <div style="display:flex;justify-content:space-between;gap:10px;flex-wrap:wrap;align-items:center;">
            <div style="font-weight:700;">${escapeHtml(s.startDate)} → ${escapeHtml(s.endDate)}</div>
            <div class="pill" style="font-size:0.75rem;">${escapeHtml(s.status)}</div>
          </div>
          <div style="margin-top:8px;font-size:0.9rem;color:var(--text-soft);">Available slots: <strong>${s.availableSlots}</strong></div>
        </div>
      `).join('');
    } else {
      sContainer.innerHTML = '<div class="empty-state">No upcoming departures.</div>';
    }
  }

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
