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

  function renderHighlights(list) {
    const root = el('highlights');
    if (!list || list.length === 0) {
      root.innerHTML = '<div style="color:var(--text-muted);">No highlights.</div>';
      return;
    }
    root.innerHTML = list.map(h => `<div>• ${escapeHtml(h)}</div>`).join('');
  }

  function renderSchedules(list) {
    const root = el('schedules');
    if (!list || list.length === 0) {
      root.innerHTML = '<div style="color:var(--text-muted);">No schedules.</div>';
      return;
    }
    root.innerHTML = list.map(s => `
      <div class="card" style="box-shadow:none;">
        <div style="display:flex;justify-content:space-between;gap:10px;flex-wrap:wrap;">
          <div><strong>${escapeHtml(s.startDate || '')}</strong> → ${escapeHtml(s.endDate || '')}</div>
          <div class="pill">${escapeHtml(s.status || '')}</div>
        </div>
        <div style="margin-top:8px;color:var(--text-muted);">Available slots: <strong>${escapeHtml(s.availableSlots ?? '')}</strong></div>
      </div>
    `).join('');
  }

  function addToCompare(tourId) {
    const set = new Set(JSON.parse(localStorage.getItem('compareIds') || '[]'));
    set.add(Number(tourId));
    localStorage.setItem('compareIds', JSON.stringify(Array.from(set)));
    alert('Added to compare.');
  }

  el('addCompareBtn').onclick = () => addToCompare(id);

  async function load() {
    const res = await TB.apiFetch(`/api/v1/tours/${encodeURIComponent(id)}`, { method: 'GET' });
    const t = res.data;
    el('title').textContent = t.tourName || '';
    el('desc').textContent = t.description || '';
    el('price').textContent = t.price ?? '';
    el('rating').textContent = t.rating ?? '';
    el('duration').textContent = t.duration ?? '';
    el('route').textContent = `${t.startLocation || ''} → ${t.endLocation || ''}`;
    el('category').textContent = t.categoryName || '';

    renderPills(t);
    renderHighlights(t.highlights);
    renderSchedules(t.schedules);
  }

  load().catch(err => {
    document.body.innerHTML = `<div class="container"><div class="card" style="border-color:#fecaca;color:#991b1b;">${escapeHtml(err.message || 'Error')}</div></div>`;
  });
})();

