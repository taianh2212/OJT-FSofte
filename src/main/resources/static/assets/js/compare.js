(() => {
  const grid = document.getElementById('grid');
  const empty = document.getElementById('empty');
  const navActions = document.getElementById('navActions');
  const user = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

  // Actions in top bar
  const clearBtn = document.createElement('button');
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
    tours.forEach(t => {
      const card = document.createElement('div');
      card.className = 'card';
      card.innerHTML = `
        <div class="title">${escapeHtml(t.tourName || '')}</div>
        <div class="row">
          <div><strong>Price:</strong> ${escapeHtml(t.price ?? '')}</div>
          <div><strong>Rating:</strong> ${escapeHtml(t.rating ?? '')}</div>
          <div><strong>Duration:</strong> ${escapeHtml(t.duration ?? '')}</div>
          <div><strong>Route:</strong> ${escapeHtml((t.startLocation||'') + ' → ' + (t.endLocation||''))}</div>
          <div><strong>Transport:</strong> ${escapeHtml(t.transportType ?? '')}</div>
          <div><strong>Category:</strong> ${escapeHtml(t.categoryName ?? '')}</div>
        </div>
        <div style="margin-top:10px;display:flex;gap:10px;flex-wrap:wrap;">
          <a class="btn" href="./tour-detail.html?id=${encodeURIComponent(t.id)}" style="text-decoration:none;display:inline-block;">Detail</a>
          <button class="btn" type="button" data-remove="${t.id}">Remove</button>
        </div>
      `;
      card.querySelector('[data-remove]').onclick = () => removeId(t.id);
      grid.appendChild(card);
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

  document.getElementById('clearBtn').onclick = () => {
    localStorage.removeItem('compareIds');
    load().catch(showErr);
  };

  load().catch(showErr);
})();

