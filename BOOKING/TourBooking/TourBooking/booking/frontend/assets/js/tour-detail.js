(() => {
  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');
  if (!id) {
    document.body.innerHTML = '<div class="container"><div class="card">Missing tour id.</div></div>';
    return;
  }

  const el = (id) => document.getElementById(id);
  const selectedState = {
    scheduleId: null
  };

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

  function formatStars(n) {
    const r = Math.min(5, Math.max(0, Math.round(Number(n) || 0)));
    return '★'.repeat(r) + '☆'.repeat(5 - r);
  }

  function formatReviewDate(createdAt) {
    if (createdAt == null || createdAt === '') return '';
    if (Array.isArray(createdAt) && createdAt.length >= 3) {
      const y = createdAt[0];
      const mo = createdAt[1];
      const d = createdAt[2];
      const h = createdAt[3] ?? 0;
      const mi = createdAt[4] ?? 0;
      const dt = new Date(y, mo - 1, d, h, mi);
      return Number.isNaN(dt.getTime()) ? '' : dt.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
    }
    try {
      const dt = new Date(createdAt);
      if (Number.isNaN(dt.getTime())) return String(createdAt);
      return dt.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
    } catch (_) {
      return String(createdAt);
    }
  }

  function renderCustomerReviews(list) {
    const root = el('customerReviews');
    const summary = el('reviewsSummary');
    if (!list || list.length === 0) {
      summary.textContent = 'Chưa có đánh giá nào.';
      root.innerHTML = '<div class="section-copy" style="color:var(--text-faint);">Hãy là người đầu tiên chia sẻ trải nghiệm sau khi hoàn thành tour.</div>';
      return;
    }
    const sum = list.reduce((a, r) => a + Number(r.rating || 0), 0);
    const avg = sum / list.length;
    summary.textContent = `${list.length} đánh giá · Trung bình ${avg.toFixed(1)}/5`;
    el('rating').textContent = `⭐ ${avg.toFixed(1)} (${list.length})`;

    function reviewTimeMs(r) {
      const c = r && r.createdAt;
      if (Array.isArray(c) && c.length >= 3) {
        return new Date(c[0], c[1] - 1, c[2], c[3] || 0, c[4] || 0).getTime();
      }
      const t = new Date(c || 0).getTime();
      return Number.isNaN(t) ? 0 : t;
    }
    const sorted = [...list].sort((a, b) => reviewTimeMs(b) - reviewTimeMs(a));

    root.innerHTML = sorted.map((r) => {
      const name = escapeHtml(r.userName || 'Khách hàng');
      const rawComment = (r.comment || '').trim();
      const comment = rawComment
        ? escapeHtml(rawComment)
        : '<em style="color:var(--text-faint)">(Không có nhận xét)</em>';
      const when = formatReviewDate(r.createdAt);
      const stars = formatStars(r.rating);
      const metaLine = when ? escapeHtml(when) : '';
      return `
      <div class="review-card list-item">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:12px;flex-wrap:wrap;">
          <strong>${name}</strong>
          <span class="stars" title="${Number(r.rating) || 0}/5">${stars}</span>
        </div>
        <p style="margin:10px 0 0; line-height:1.6;">${comment}</p>
        ${metaLine ? `<div class="meta">${metaLine}</div>` : ''}
      </div>`;
    }).join('');
  }

  async function loadCustomerReviews(tourId) {
    try {
      const res = await TB.apiFetch(`/api/v1/reviews/tour/${encodeURIComponent(tourId)}`, { method: 'GET' });
      const list = Array.isArray(res?.data) ? res.data : [];
      renderCustomerReviews(list);
    } catch (e) {
      el('reviewsSummary').textContent = '';
      el('customerReviews').innerHTML = `<div class="section-copy" style="color:#fca5a5;">Không tải được đánh giá. ${escapeHtml(e.message || 'Lỗi')}</div>`;
    }
  }

  function renderSchedules(list) {
    const root = el('schedules');
    if (!list || list.length === 0) {
      root.innerHTML = '<div class="section-copy" style="color:var(--text-faint);">No upcoming departures found.</div>';
      return;
    }
    root.innerHTML = list.map(s => {
      const isAvailable = String(s.status || '').toUpperCase() === 'AVAILABLE';
      const isSelectable = isAvailable && Number(s.availableSlots || 0) > 0;
      return `
        <div class="card panel ${isSelectable ? 'schedule-card' : ''}" data-schedule-id="${escapeHtml(s.scheduleId || '')}" style="box-shadow:none; border: 1px solid rgba(255,255,255,0.05); padding: 14px; ${isSelectable ? 'cursor:pointer;' : ''}">
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

    root.querySelectorAll('.schedule-card').forEach(card => {
      card.addEventListener('click', () => {
        root.querySelectorAll('.schedule-card').forEach(c => {
          c.style.border = '1px solid rgba(255,255,255,0.05)';
          c.style.boxShadow = 'none';
        });
        card.style.border = '1px solid var(--primary)';
        card.style.boxShadow = '0 0 0 1px rgba(243, 141, 73, 0.35)';
        selectedState.scheduleId = card.getAttribute('data-schedule-id');
      });
    });
  }

  function addToCompare(tourId) {
    const set = new Set(JSON.parse(localStorage.getItem('compareIds') || '[]'));
    set.add(Number(tourId));
    localStorage.setItem('compareIds', JSON.stringify(Array.from(set)));
    // Subtle notification instead of alert could be better, but keeping for now per original
    alert('Tour successfully added to your comparison list.');
  }

  el('addCompareBtn').onclick = () => addToCompare(id);
  el('bookNowBtn').onclick = () => {
    const qs = new URLSearchParams();
    qs.set('tourId', String(id));
    if (selectedState.scheduleId) {
      qs.set('scheduleId', String(selectedState.scheduleId));
    }
    window.location.href = `./user/checkout.html?${qs.toString()}`;
  };

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
    await loadCustomerReviews(id);
  }

  load().catch(err => {
    document.body.innerHTML = `<div class="container"><div class="card" style="border-color:#fecaca;color:#991b1b;">${escapeHtml(err.message || 'Error')}</div></div>`;
  });
})();

