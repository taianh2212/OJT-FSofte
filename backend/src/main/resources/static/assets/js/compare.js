(() => {
  const grid = document.getElementById('grid');
  const empty = document.getElementById('empty');
<<<<<<< HEAD:backend/src/main/resources/static/assets/js/compare.js
=======
  const navActions = document.getElementById('navActions');
  const user = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;
  const clearBtn = document.createElement('button');

  // Actions in top bar
  clearBtn.id = 'clearBtn'; // Set ID so line 99 works
  clearBtn.className = 'btn btn-secondary';
  clearBtn.textContent = 'Clear All';
  clearBtn.onclick = () => {
    localStorage.removeItem('compareIds');
    location.reload();
  };
  navActions.appendChild(clearBtn);

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
>>>>>>> origin/integration-test:src/main/resources/static/assets/js/compare.js

  function escapeHtml(s) {
    return String(s)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  function getIds() {
    const ids = JSON.parse(localStorage.getItem('compareIds') || '[]')
      .map(n => Number(n))
      .filter(n => Number.isFinite(n));
    return Array.from(new Set(ids));
  }

  function render(tours) {
    grid.innerHTML = '';
    // Use a CSS grid with specific columns for comparison
    grid.style.display = 'grid';
    grid.style.gridAutoColumns = 'minmax(300px, 1fr)';
    grid.style.gridAutoFlow = 'column';
    grid.style.gap = '20px';
    grid.style.overflowX = 'auto';

    tours.forEach(t => {
      const col = document.createElement('div');
      col.className = 'card panel';
      col.style.minWidth = '300px';
      
      col.innerHTML = `
        <div style="margin-bottom:20px;">
          <div class="eyebrow">${escapeHtml(t.categoryName || 'Tour')}</div>
          <h3 style="font-size:1.4rem; color:var(--primary); margin-top:8px;">${escapeHtml(t.tourName || '')}</h3>
        </div>

        <div class="list" style="gap:15px;">
          <div class="list-item">
            <label style="display:block; font-size:0.75rem; color:var(--text-faint); text-transform:uppercase; font-weight:700;">Price</label>
            <div style="font-size:1.2rem; font-weight:800; color:var(--text);">${t.price ? Number(t.price).toLocaleString() : 'N/A'} VND</div>
          </div>
          
          <div class="list-item">
            <label style="display:block; font-size:0.75rem; color:var(--text-faint); text-transform:uppercase; font-weight:700;">Rating</label>
            <div style="font-weight:600; color:#fbbf24;">⭐ ${(t.rating || 0).toFixed(1)}</div>
          </div>

          <div class="list-item">
            <label style="display:block; font-size:0.75rem; color:var(--text-faint); text-transform:uppercase; font-weight:700;">Features</label>
            <div style="display:flex; flex-wrap:wrap; gap:5px; margin-top:5px;">
              ${t.hasPickup ? '<span class="pill" style="font-size:0.7rem; background:#e0f2fe; color:#0369a1;">🚐 Pickup</span>' : ''}
              ${t.hasLunch ? '<span class="pill" style="font-size:0.7rem; background:#fef3c7; color:#92400e;">🍱 Lunch</span>' : ''}
              ${t.isInstantConfirmation ? '<span class="pill" style="font-size:0.7rem; background:#dcfce7; color:#166534;">⚡ Instant</span>' : ''}
            </div>
          </div>

          <div class="list-item">
            <label style="display:block; font-size:0.75rem; color:var(--text-faint); text-transform:uppercase; font-weight:700;">Suitable For</label>
            <div style="font-size:0.9rem;">${escapeHtml(t.suitableAges || 'All ages')}</div>
          </div>

          <div class="list-item">
            <label style="display:block; font-size:0.75rem; color:var(--text-faint); text-transform:uppercase; font-weight:700;">Duration & Route</label>
            <div style="font-size:0.9rem;">${t.duration} days | ${escapeHtml(t.startLocation || '')} → ${escapeHtml(t.endLocation || '')}</div>
          </div>
        </div>

        <div style="margin-top:30px; display:flex; gap:10px;">
          <a class="btn" href="./tour-detail.html?id=${encodeURIComponent(t.id)}" style="flex:1; text-align:center;">View Details</a>
          <button class="btn btn-secondary" type="button" data-remove="${t.id}" style="padding:10px;">✕</button>
        </div>
      `;
      col.querySelector('[data-remove]').onclick = () => removeId(t.id);
      grid.appendChild(col);
    });
  }

  function removeId(id) {
    const ids = getIds().filter(x => x !== Number(id));
    localStorage.setItem('compareIds', JSON.stringify(ids));
    load().catch(showErr);
  }

  async function load() {
    const ids = getIds();
    if (ids.length < 2) {
      empty.style.display = 'block';
      grid.innerHTML = '';
      return;
    }
    empty.style.display = 'none';
    const res = await TB.apiFetch(`/api/v1/tours/compare?ids=${encodeURIComponent(ids.join(','))}`);
    render(res.data || []);
  }

  function showErr(err) {
    grid.innerHTML = `<div class="card" style="border-color:#fecaca;color:#991b1b;">${escapeHtml(err.message || 'Error')}</div>`;
  }

  load().catch(showErr);
})();
