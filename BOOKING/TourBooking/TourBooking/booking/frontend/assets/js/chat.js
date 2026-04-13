(() => {
  const chatBox = document.getElementById('chatBox');
  const form = document.getElementById('form');
  const text = document.getElementById('text');
  const who = document.getElementById('who');

  const user = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;
  const userId = user?.id ?? null;
  who.textContent = user ? (user.fullName || user.email) : 'Guest';
  if (user) {
    const logoutBtn = document.createElement('button');
    logoutBtn.className = 'btn btn-secondary';
    logoutBtn.style.marginLeft = '10px';
    logoutBtn.textContent = 'Logout';
    logoutBtn.onclick = () => TB.logout();
    const isAdmin = String(user.role || '').toUpperCase() === 'ADMIN';
    if (isAdmin) {
      const adminBtn = document.createElement('button');
      adminBtn.className = 'btn btn-secondary';
      adminBtn.textContent = 'Chat escalations';
      adminBtn.style.marginLeft = '10px';
      adminBtn.onclick = () => window.location.href = '/pages/admin/chat-escalations.html';
      who.parentElement.appendChild(adminBtn);
    }
    who.parentElement.appendChild(logoutBtn);
  }

  function escapeHtml(s) {
    return String(s)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  const box = document.getElementById('chatBox');
  const chatForm = document.getElementById('form');
  const escalateBtn = document.getElementById('escalateBtn');
  const endSessionBtn = document.getElementById('endSessionBtn');
  const escalationStatus = document.getElementById('escalationStatus');
  let currentEscalation = null;
  
  // UC11: Guest ID logic
  let guestId = localStorage.getItem('guestId');
  if (!user && !guestId) {
    guestId = 'guest_' + Math.random().toString(36).substring(2, 11);
    localStorage.setItem('guestId', guestId);
  }
  const isGuest = !user;

  function render(msgs) {
    box.innerHTML = '';
    if (msgs.length === 0) {
      proactiveGreeting();
      return;
    }
    msgs.forEach(m => {
      const div = document.createElement('div');
      const senderType = (m.senderType || 'GUEST').toUpperCase();
      const senderClass = senderType.toLowerCase();
      const isUserSender = senderType === 'GUEST' || senderType === 'USER';
      const fromMe = isUserSender && (
        (m.userId && userId && m.userId === userId)
        || (!m.userId && m.guestId && m.guestId === guestId)
      );
      div.className = `msg ${senderClass}`;
      if (fromMe) div.classList.add('me');

      const senderLabel = senderType === 'AI'
        ? 'Assistant'
        : (isUserSender ? 'You' : 'Staff');
      div.innerHTML = `
        <div style="font-size:0.75rem;opacity:0.7;margin-bottom:4px;">${senderLabel}</div>
        <div>${escapeHtml(m.message)}</div>
      `;
      box.appendChild(div);
    });
    box.scrollTop = box.scrollHeight;
  }

  function proactiveGreeting() {
    const div = document.createElement('div');
    div.className = 'msg ai';
    div.innerHTML = `
      <div style="font-size:0.75rem;opacity:0.7;margin-bottom:4px;">🤖 Trợ lý AI</div>
      <div>${formatAiText('👋 Xin chào! Tôi là trợ lý tư vấn tour du lịch của TourBooking.\n\nBạn muốn đi đâu? Hãy cho tôi biết về:\n📍 Địa điểm mong muốn (Đà Nẵng, Phú Quốc...)\n🌤️ Thời tiết bạn thích (nắng, mát, lạnh...)\n💰 Ngân sách (dưới 5 triệu, 5-10 triệu...)\n🕐 Thời gian (3 ngày 2 đêm, 1 tuần...)\n\nTôi sẽ gợi ý tour phù hợp nhất cho bạn! 😊')}</div>
    `;
    box.appendChild(div);
  }

  // Format AI text: xuống dòng, in đậm **text**
  function formatAiText(text) {
    if (!text) return '';
    return escapeHtml(text)
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/\n/g, '<br>');
  }

  async function load() {
    try {
      const q = buildUserQuery();
      const res = await TB.apiFetch(`/api/v1/chat/messages${q}`);
      render(res.data || []);
    } catch (err) {
      console.error('Failed to load chat history', err);
    }
  }

  chatForm.onsubmit = async (e) => {
    e.preventDefault();
    const input = document.getElementById('text');
    const content = input.value.trim();
    if (!content) return;

    // Local echo
    const myMsg = document.createElement('div');
    myMsg.className = 'msg user me';
    myMsg.innerHTML = `<div style="font-size:0.75rem;opacity:0.7;margin-bottom:4px;">You</div><div>${escapeHtml(content)}</div>`;
    box.appendChild(myMsg);
    box.scrollTop = box.scrollHeight;
    input.value = '';

    try {
      await TB.apiFetch('/api/v1/chat/messages', {
        method: 'POST',
        body: JSON.stringify({
          userId: isGuest ? null : user.id,
          guestId: isGuest ? guestId : null,
          message: content,
          senderType: 'USER'
        })
      });
      // Trigger AI response after a short delay
      setTimeout(() => getAiResponse(content), 600);
    } catch (err) {
      alert('Failed to send message');
    }
  };

  async function getAiResponse(userMsg) {
    // Hiện typing indicator
    const typingEl = document.createElement('div');
    typingEl.className = 'msg ai';
    typingEl.id = 'typing-indicator';
    typingEl.innerHTML = `<div style="font-size:0.75rem;opacity:0.7;margin-bottom:4px;">🤖 Trợ lý AI</div><div style="opacity:0.6;">⏳ Đang soạn tin nhắn...</div>`;
    box.appendChild(typingEl);
    box.scrollTop = box.scrollHeight;

    try {
      const res = await TB.apiFetch('/api/v1/ai/chat', {
        method: 'POST',
        body: JSON.stringify({
          message: userMsg,
          userId: isGuest ? null : user?.id ?? null,
          guestId: isGuest ? guestId : null
        })
      });
      // Xoá typing indicator
      document.getElementById('typing-indicator')?.remove();

      // Lấy đúng field reply từ backend
      const aiContent = res.data?.reply || res.data?.response || 'Xin lỗi, có lỗi xảy ra. Vui lòng thử lại!';
      const aiMsg = document.createElement('div');
      aiMsg.className = 'msg ai';
      aiMsg.innerHTML = `<div style="font-size:0.75rem;opacity:0.7;margin-bottom:4px;">🤖 Trợ lý AI</div><div>${formatAiText(aiContent)}</div>`;
      box.appendChild(aiMsg);
      box.scrollTop = box.scrollHeight;
    } catch (err) {
      document.getElementById('typing-indicator')?.remove();
      console.error('AI error', err);
      const errMsg = document.createElement('div');
      errMsg.className = 'msg ai';
      errMsg.innerHTML = `<div style="font-size:0.75rem;opacity:0.7;margin-bottom:4px;">🤖 Trợ lý AI</div><div>Xin lỗi, hiện tại tôi không thể trả lời. Vui lòng thử lại sau!</div>`;
      box.appendChild(errMsg);
      box.scrollTop = box.scrollHeight;
    }
  }

  function buildUserQuery() {
    if (isGuest) {
      return guestId ? `?guestId=${encodeURIComponent(guestId)}` : '';
    }
    return user?.id ? `?userId=${encodeURIComponent(user.id)}` : '';
  }

  async function loadEscalationStatus() {
    try {
      const q = buildUserQuery();
      const res = await TB.apiFetch(`/api/v1/chat/escalations/active${q}`);
      displayEscalationStatus(res.data);
    } catch (err) {
      console.error('Failed to load escalation status', err);
    }
  }

  load();
  loadEscalationStatus();

  // Poll
  setInterval(() => {
    load().catch(() => {});
    loadEscalationStatus().catch(() => {});
  }, 3000);

  if (escalateBtn) {
    escalateBtn.addEventListener('click', async () => {
      const note = (text.value || '').trim();
      const payloadNote = note || 'Customer requested staff support';
      const success = await sendEscalation(payloadNote);
      if (success) {
        text.value = '';
      }
    });
  }

  async function sendEscalation(note) {
    const payload = {
      userId: isGuest ? null : user?.id,
      guestId: isGuest ? guestId : null,
      requestNote: note
    };

    try {
      if (escalateBtn) escalateBtn.disabled = true;
      const res = await TB.apiFetch('/api/v1/chat/escalations', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      displayEscalationStatus(res.data);
      return true;
    } catch (err) {
      console.error('Escalation failed', err);
      return false;
    } finally {
      if (escalateBtn) escalateBtn.disabled = false;
    }
  }

  if (endSessionBtn) {
    endSessionBtn.addEventListener('click', async () => {
      if (!currentEscalation) return;
      if (!window.confirm('End this chat session? You can start a new one anytime.')) return;
      try {
        const payload = {
          userId: isGuest ? null : user?.id,
          guestId: isGuest ? guestId : null
        };
        const res = await TB.apiFetch('/api/v1/chat/escalations/close', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        displayEscalationStatus(res.data);
      } catch (err) {
        console.error('Escalation close failed', err);
        alert('Unable to end the chat session right now.');
      }
    });
  }

  function displayEscalationStatus(data) {
    currentEscalation = data;
    if (!escalationStatus) return;
    if (!data) {
      escalationStatus.hidden = true;
      toggleEndSession(false);
      return;
    }

    const label = {
      OPEN: 'Staff has been notified.',
      IN_REVIEW: 'Staff is replying.',
      RESOLVED: 'Conversation closed. Start a new chat anytime.',
      WAITING_STAFF: 'Staff has been notified.',
      STAFF_CHATTING: 'Staff joined the chat.',
      CLOSED: 'Conversation closed. Start a new chat anytime.'
    }[data.status] || 'Staff has been notified.';

    const suffix = data.requestNote ? ` · ${data.requestNote}` : '';
    escalationStatus.textContent = `${label}${suffix}`;
    escalationStatus.hidden = false;
    toggleEndSession(!['RESOLVED', 'CLOSED'].includes(data.status));
  }

  function toggleEndSession(show) {
    if (!endSessionBtn) return;
    endSessionBtn.hidden = !show;
  }
})();
