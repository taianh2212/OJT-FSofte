(() => {
  function getToken() {
    return localStorage.getItem('token') || '';
  }

  async function apiFetch(path, options = {}) {
    const headers = new Headers(options.headers || {});
    if (!headers.has('Content-Type') && options.body) {
      headers.set('Content-Type', 'application/json');
    }
    const token = getToken();
    if (token) headers.set('Authorization', `Bearer ${token}`);

    const res = await fetch(path, { ...options, headers });
    const text = await res.text();
    let json = null;
    try { json = text ? JSON.parse(text) : null; } catch (_) {}
    if (!res.ok) {
      const msg = (json && json.message) ? json.message : `HTTP ${res.status}`;
      const err = new Error(msg);
      err.status = res.status;
      err.body = json;
      throw err;
    }
    return json;
  }

  window.TB = window.TB || {};
  window.TB.apiFetch = apiFetch;
})();

