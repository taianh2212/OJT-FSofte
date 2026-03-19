(() => {
  const chatBox = document.getElementById('chatBox');
  const form = document.getElementById('form');
  const text = document.getElementById('text');
  const who = document.getElementById('who');

  const user = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;
  const userId = user?.id ?? null;
  who.textContent = user ? (user.fullName || user.email) : 'Guest';

  function escapeHtml(s) {
    return String(s)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  function render(list) {
    chatBox.innerHTML = '';
    (list || []).forEach(m => {
      const div = document.createElement('div');
      const type = (m.senderType || 'GUEST').toUpperCase();
      div.className = `msg ${type === 'AI' ? 'ai' : (type === 'STAFF' ? 'staff' : 'me')}`;
      div.innerHTML = `
        <div>${escapeHtml(m.message || '')}</div>
        <div class="meta">${escapeHtml(type)}${m.sentAt ? ' · ' + escapeHtml(m.sentAt) : ''}</div>
      `;
      chatBox.appendChild(div);
    });
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  async function load() {
    const q = userId ? `?userId=${encodeURIComponent(userId)}` : '';
    const res = await TB.apiFetch(`/api/v1/chat/messages${q}`);
    render(res.data || []);
  }

  async function send(message) {
    // Save user message in chat
    await TB.apiFetch('/api/v1/chat/messages', {
      method: 'POST',
      body: JSON.stringify({
        userId,
        senderType: 'GUEST',
        message
      })
    });

    // Ask AI for suggestion (optional UX)
    try {
      await TB.apiFetch('/api/v1/ai/chat', {
        method: 'POST',
        body: JSON.stringify({ userId, message })
      });
    } catch (_) {
      // ignore AI failures
    }
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const msg = (text.value || '').trim();
    if (!msg) return;
    text.value = '';
    try {
      await send(msg);
      await load();
    } catch (err) {
      alert(err.message || 'Failed to send');
    }
  });

  // Poll
  load().catch(() => {});
  setInterval(() => load().catch(() => {}), 3000);
})();

