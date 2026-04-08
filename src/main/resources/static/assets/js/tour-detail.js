(() => {
  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');

  if (!id) {
    document.body.innerHTML =
      '<div class="container"><div class="card">Missing tour id.</div></div>';
    return;
  }

  const el = (id) => document.getElementById(id);

  function escapeHtml(s) {
    if (!s) return '';
    return String(s)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  // ── Pills ──────────────────────────────────────────────────────
  function renderPills(tour) {
    const pills = [];
    if (tour.transportType) pills.push(tour.transportType);
    if (tour.startLocation && tour.endLocation) {
      pills.push(`${tour.startLocation} → ${tour.endLocation}`);
    }
    el('pills').innerHTML = pills
      .map((p) => `<span class="pill">${escapeHtml(p)}</span>`)
      .join('');
  }

  // ── Gallery ────────────────────────────────────────────────────
  function renderGallery(images) {
    const root = el('gallery');

    if (!images || images.length === 0) {
      root.innerHTML = `
        <div class="thumb"
             style="grid-column: span 2; height: 320px;
                    display:flex; align-items:center; justify-content:center;">
          <span>Featured Tour</span>
        </div>
      `;
      return;
    }

    const main = images[0];
    const others = images.slice(1, 5);

    let html = `
      <div class="gallery-main">
        <img src="${main}" alt="Tour image"
             style="width:100%; height:100%; object-fit:cover; border-radius:18px;" />
      </div>
    `;

    others.forEach((img) => {
      html += `
        <div class="gallery-item">
          <img src="${img}" alt="Tour image small"
               style="width:100%; height:100%; object-fit:cover; border-radius:12px;" />
        </div>
      `;
    });

    root.innerHTML = html;
  }

  // ── Highlights ─────────────────────────────────────────────────
  function renderHighlights(list) {
    const root = el('highlights');

    if (!list?.length) {
      root.innerHTML = `
        <div class="section-copy" style="color:var(--text-faint);">
          No highlights listed for this route yet.
        </div>
      `;
      return;
    }

    root.innerHTML = list
      .map(
        (h) => `
        <div class="list-item"
             style="background:rgba(255,255,255,0.02);
                    padding:10px 14px;
                    border-radius:8px;
                    border:1px solid rgba(255,255,255,0.05);">
          ✨ ${escapeHtml(h)}
        </div>
      `
      )
      .join('');
  }

  // ── Itinerary (PREMIUM TIMELINE) ───────────────────────────────
  function renderAccordionItinerary(itinerary) {
    const root = el('itinerary');

    if (!itinerary) {
      root.innerHTML = `
        <div class="section-copy" style="color:var(--text-faint);">
          Chi tiết hành trình đang được cập nhật.
        </div>
      `;
      return;
    }

    // Split on [DAY N] — captures the day number in alternating positions
    const sections = itinerary
      .split(/\[DAY\s*(\d+)\]/g)
      .filter((part) => part && part.trim());

    if (sections.length < 2) {
      root.innerHTML = `
        <div class="card panel" style="padding:20px; line-height:1.8;">
          ${escapeHtml(itinerary)}
        </div>
      `;
      return;
    }

    let html = '';

    // If first segment is NOT a pure digit it's an overview/intro text
    const firstIsDayNum = /^\d+$/.test(sections[0].trim());

    if (!firstIsDayNum && sections[0].trim()) {
      const overviewText = sections[0]
        .replace(/^TỔNG QUAN\s*:\s*/i, '')
        .trim();
      html += `
        <div class="card panel"
             style="padding:24px; margin-bottom:24px; border-radius:20px;
                    background:rgba(255,255,255,0.03); line-height:1.8;">
          <div style="font-size:1.1rem; font-weight:800; margin-bottom:12px;">
            📝 Tổng quan hành trình
          </div>
          <div style="color:var(--text-soft);">
            ${escapeHtml(overviewText)}
          </div>
        </div>
      `;
    }

    // Day pairs start at index 1 if first segment was overview, else index 0
    const startIdx = firstIsDayNum ? 0 : 1;
    const dayOrder = [];
    const dayContents = {};

    for (let i = startIdx; i < sections.length; i += 2) {
      const dayNum = sections[i];
      const content = (sections[i + 1] || '').trim();

      if (!dayContents[dayNum]) {
        dayOrder.push(dayNum);
        dayContents[dayNum] = [];
      }

      if (content) {
        dayContents[dayNum].push(content);
      }
    }

    dayOrder.forEach((dayNum) => {
      const content = dayContents[dayNum].join('\n');

      const lines = content
        .split(/\n+/)
        .map((line) => line.trim())
        .filter(Boolean)
        .map((line) => {
          const match = line.match(/^-?\s*(\d{1,2}:\d{2})\s*:?\s*(.*)$/);

          if (match) {
            return `
              <div style="display:flex; gap:16px; align-items:flex-start;
                          padding:12px 0;
                          border-bottom:1px solid rgba(255,255,255,0.06);">
                <div style="min-width:72px; font-weight:800;
                            color:var(--primary); font-size:0.95rem; flex-shrink:0;">
                  ${escapeHtml(match[1])}
                </div>
                <div style="flex:1; color:var(--text-soft); line-height:1.7;">
                  ${escapeHtml(match[2])}
                </div>
              </div>
            `;
          }

          return `
            <div style="padding:10px 0 4px; font-weight:700;
                        color:var(--text-soft); font-size:0.9rem;
                        letter-spacing:0.02em;">
              ${escapeHtml(line)}
            </div>
          `;
        })
        .join('');

      html += `
        <div class="card panel"
             style="margin-bottom:24px; padding:26px; border-radius:20px;">
          <h3 style="margin-bottom:16px; font-size:1.25rem;
                     color:var(--primary); font-weight:800;">
            📅 Ngày ${escapeHtml(dayNum)}
          </h3>
          ${lines}
        </div>
      `;
    });

    root.innerHTML = html;
  }

  // ── Extra Info (policies) ──────────────────────────────────────
  function renderExtraInfo(t) {
    const root = el('extra-info');
    let html = '';

    const renderCard = (title, icon, color, content) => {
      if (!content) return '';
      return `
        <div class="card panel"
             style="background:${color.bg};
                    border:1px solid ${color.border};
                    margin-bottom:20px;
                    border-radius:18px;">
          <div style="color:${color.text};
                      font-weight:800;
                      margin-bottom:15px;
                      font-size:1.1rem;">
            ${icon} ${title}
          </div>
          <div style="line-height:1.8;">${escapeHtml(content)}</div>
        </div>
      `;
    };

    html += renderCard(
      'Dịch vụ bao gồm', '✅',
      { bg: 'rgba(74,222,128,0.05)', border: 'rgba(74,222,128,0.15)', text: '#166534' },
      t.inclusions
    );

    html += renderCard(
      'Chính sách trẻ em', '👶',
      { bg: 'rgba(59,130,246,0.05)', border: 'rgba(59,130,246,0.15)', text: '#1e40af' },
      t.childPolicy
    );

    html += renderCard(
      'Chính sách huỷ', '📝',
      { bg: 'rgba(245,158,11,0.05)', border: 'rgba(245,158,11,0.15)', text: '#92400e' },
      t.cancellationPolicy
    );

    root.innerHTML = html;
  }

  // ── Schedules ──────────────────────────────────────────────────
  function renderSchedules(list) {
    const root = el('schedules');

    if (!list?.length) {
      root.innerHTML =
        '<div class="section-copy">No upcoming departures found.</div>';
      return;
    }

    root.innerHTML = list
      .map(
        (s) => `
        <div class="card panel" style="padding:14px; margin-bottom:12px;">
          <div style="display:flex; justify-content:space-between;">
            <div>
              <strong>${escapeHtml(s.startDate)}</strong>
              <div style="font-size:0.8rem;">Khởi hành hàng ngày</div>
            </div>
            <div class="pill">${escapeHtml(s.status)}</div>
          </div>
          <div style="margin-top:12px; display:flex; justify-content:space-between;">
            <div>Còn ${escapeHtml(String(s.availableSlots))} chỗ</div>
            <button class="btn">Đặt ngay</button>
          </div>
        </div>
      `
      )
      .join('');
  }

  // ── Compare ────────────────────────────────────────────────────
  function addToCompare(tourId) {
    const set = new Set(
      JSON.parse(localStorage.getItem('compareIds') || '[]')
    );
    set.add(Number(tourId));
    localStorage.setItem('compareIds', JSON.stringify([...set]));
    alert('Đã thêm tour vào danh sách so sánh.');
  }

  el('addCompareBtn').onclick = () => addToCompare(id);

  // ── Load & render ──────────────────────────────────────────────
  async function load() {
    const res = await TB.apiFetch(
      `/api/v1/tours/${encodeURIComponent(id)}`,
      { method: 'GET' }
    );

    const t = res.data;

    document.title = `${t.tourName} - Detail`;

    el('title').textContent = t.tourName || '';
    el('desc').textContent = t.description || '';
    el('price').textContent = t.price
      ? `${Number(t.price).toLocaleString()} VND`
      : 'Liên hệ';
    el('rating').textContent = t.rating
      ? `⭐ ${t.rating.toFixed(1)}`
      : 'Chưa có đánh giá';
    el('duration').textContent = `${t.duration || 1} ngày`;
    el('route').textContent =
      t.startLocation && t.endLocation
        ? `${t.startLocation} → ${t.endLocation}`
        : '';
    el('category').textContent = t.categoryName || '';

    renderGallery(t.imageUrls || t.images);
    renderPills(t);
    renderHighlights(t.highlights);
    renderAccordionItinerary(t.itinerary);
    renderExtraInfo(t);
    renderSchedules(t.schedules);
  }

  load().catch((err) => {
    document.body.innerHTML = `
      <div class="container">
        <div class="card" style="color:red;">
          ${escapeHtml(err.message || 'Error loading tour.')}
        </div>
      </div>
    `;
  });
})();
