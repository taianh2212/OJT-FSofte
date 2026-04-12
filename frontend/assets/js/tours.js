(() => {
  const grid = document.getElementById('grid');
  const loginState = document.getElementById('loginState');

  const state = {
    page: 0,
    size: 12,
    totalPages: 0,
    selected: new Set(JSON.parse(localStorage.getItem('compareIds') || '[]'))
  };

  function setLoginState() {
    const user = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;
    loginState.textContent = user ? `Hi, ${user.fullName || user.email}` : 'Guest';
  }

  async function loadCategories() {
    try {
      const res = await TB.apiFetch('/api/v1/categories');
      const combo = document.getElementById('categoryId');
      if (res.data) {
        res.data.forEach(c => {
          const opt = document.createElement('option');
          opt.value = c.id;
          opt.textContent = c.categoryName;
          combo.appendChild(opt);
        });
      }
    } catch (_) {}
  }

  function readFilters() {
    const get = (id) => document.getElementById(id)?.value;
    const numOrNull = (v) => v === '' ? null : Number(v);
    return {
      keyword: get('keyword') || null,
      minPrice: get('minPrice') || null,
      maxPrice: get('maxPrice') || null,
      minRating: get('minRating') || null,
      startDate: get('startDate') || null,
      categoryId: get('categoryId') || null,
      transportType: get('transportType') || null,
      cityId: get('cityId') || null,
      lat: get('lat') || null,
      lng: get('lng') || null,
      hasPickup: document.getElementById('hasPickup')?.checked ?? null,
      hasLunch: document.getElementById('hasLunch')?.checked ?? null,
      isDaily: document.getElementById('isDaily')?.checked ?? null,
      isInstantConfirmation: document.getElementById('isInstantConfirmation')?.checked ?? null,
      sortBy: get('sortBy') || 'price',
      sortDir: get('sortDir') || 'asc',
      size: numOrNull(get('size')) || 12,
    };
  }

  function buildUrl(filters) {
    const p = new URLSearchParams();
    if (filters.keyword) p.set('keyword', filters.keyword);
    if (filters.minPrice) p.set('minPrice', filters.minPrice);
    if (filters.maxPrice) p.set('maxPrice', filters.maxPrice);
    if (filters.minRating) p.set('minRating', filters.minRating);
    if (filters.startDate) p.set('startDate', filters.startDate);
    if (filters.categoryId) p.set('categoryId', filters.categoryId);
    if (filters.transportType) p.set('transportType', filters.transportType);
    if (filters.cityId) p.set('cityId', filters.cityId);
    if (filters.lat) p.set('lat', filters.lat);
    if (filters.lng) p.set('lng', filters.lng);
    if (filters.hasPickup) p.set('hasPickup', 'true');
    if (filters.hasLunch) p.set('hasLunch', 'true');
    if (filters.isDaily) p.set('isDaily', 'true');
    if (filters.isInstantConfirmation) p.set('isInstantConfirmation', 'true');
    p.set('page', String(state.page));
    p.set('size', String(state.size));
    p.set('sortBy', filters.sortBy);
    p.set('sortDir', filters.sortDir);
    return `/api/v1/tours/browse?${p.toString()}`;
  }

  function renderCompareCount() {
    document.getElementById('compareCount').textContent = `${state.selected.size} selected`;
  }

  function renderTours(pageRes) {
    grid.innerHTML = '';
    const content = pageRes?.data?.content || [];
    if (content.length === 0) {
      grid.innerHTML = '<div class="card">No tours found.</div>';
      return;
    }

    content.forEach(t => {
      const card = document.createElement('div');
      card.className = 'card';
      const checked = state.selected.has(t.id);
      card.innerHTML = `
        <div class="thumb">Tour</div>
        <div class="tour-title">${escapeHtml(t.tourName || '')}</div>
        <div class="meta">
          <span><strong>${t.price ?? ''}</strong></span>
        <div class="thumb">
          ${t.imageUrl ? `<img src="${t.imageUrl}" alt="${escapeHtml(t.tourName)}" loading="lazy">` : 
            t.imageUrls && t.imageUrls.length > 0 ? `<img src="${t.imageUrls[0]}" alt="${escapeHtml(t.tourName)}" loading="lazy">` : 
            '<span>Tour</span>'}
        </div>
        <div class="tour-title">${escapeHtml(t.tourName || '')}</div>
        <div class="meta">
          <span><strong>${t.price ? Number(t.price).toLocaleString() : ''} VNĐ</strong></span>
          <span>⭐ ${(t.rating ?? 0).toFixed ? (t.rating ?? 0).toFixed(1) : (t.rating ?? 0)}</span>
        </div>
        <div class="meta" style="margin-top:10px;justify-content:flex-start;gap:10px;flex-wrap:wrap;">
          ${t.transportType ? `<span class="pill">${escapeHtml(t.transportType)}</span>` : ''}
          ${t.hasPickup ? `<span class="pill" style="background:#e0f2fe;color:#0369a1;">🚐 Đưa đón</span>` : ''}
          ${t.hasLunch ? `<span class="pill" style="background:#fef3c7;color:#92400e;">🍱 Ăn trưa</span>` : ''}
          ${t.isInstantConfirmation ? `<span class="pill" style="background:#dcfce7;color:#166534;">⚡ Xác nhận ngay</span>` : ''}
        </div>
        <div style="display:flex;gap:10px;margin-top:12px;align-items:center;">
          <label class="chk" style="flex:1;">
            <input type="checkbox" data-id="${t.id}" ${checked ? 'checked' : ''} />
            <span>Compare</span>
          </label>
          <a class="btn" href="./tour-detail.html?id=${encodeURIComponent(t.id)}" style="text-decoration:none;display:inline-block;">Detail</a>
        </div>
      `;
      card.querySelector('input[type="checkbox"]').addEventListener('change', (e) => {
        const id = Number(e.target.dataset.id);
        if (e.target.checked) state.selected.add(id);
        else state.selected.delete(id);
        localStorage.setItem('compareIds', JSON.stringify(Array.from(state.selected)));
        renderCompareCount();
      });
      grid.appendChild(card);
    });
  }

  function renderPaging(pageRes) {
    const data = pageRes?.data;
    const page = (data?.page ?? 0);
    const totalPages = (data?.totalPages ?? 0);
    const totalElements = (data?.totalElements ?? 0);
    state.totalPages = totalPages;
    document.getElementById('pageInfo').textContent = `page ${page + 1} / ${Math.max(totalPages, 1)}`;
    document.getElementById('totalInfo').textContent = `${totalElements} tours`;
    document.getElementById('prevBtn').disabled = page <= 0;
    document.getElementById('nextBtn').disabled = totalPages > 0 ? page >= totalPages - 1 : true;
  }

  async function load() {
    const filters = readFilters();
    state.size = filters.size;
    const url = buildUrl(filters);
    const res = await TB.apiFetch(url, { method: 'GET' });
    renderTours(res);
    renderPaging(res);
  }

  function escapeHtml(s) {
    return String(s)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  // wire events
  setLoginState();
  loadCategories();
  renderCompareCount();

  document.getElementById('applyBtn').onclick = () => { state.page = 0; load().catch(showErr); };
  document.getElementById('searchBtn').onclick = () => { state.page = 0; load().catch(showErr); };
  document.getElementById('prevBtn').onclick = () => { state.page = Math.max(0, state.page - 1); load().catch(showErr); };
  document.getElementById('nextBtn').onclick = () => { state.page = state.page + 1; load().catch(showErr); };

  document.getElementById('compareBtn').onclick = () => {
    if (state.selected.size < 2) {
      alert('Select at least 2 tours to compare.');
      return;
    }
    window.location.href = `./compare.html`;
  };

  function showErr(err) {
    grid.innerHTML = `<div class="card" style="border-color:#fecaca;color:#991b1b;">${escapeHtml(err.message || 'Error')}</div>`;
  }

  load().catch(showErr);
})();

