(() => {
  const form = document.getElementById('form');
  const msg = document.getElementById('msg');

  function setMsg(text, color) {
    msg.textContent = text;
    msg.style.color = color || '#333';
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('email').value;
    setMsg('Subscribing...', '#333');
    try {
      const res = await TB.apiFetch('/api/v1/newsletters', {
        method: 'POST',
        body: JSON.stringify({ email })
      });
      setMsg(res.message || 'Subscribed!', 'green');
    } catch (err) {
      setMsg(err.message || 'Failed to subscribe', 'red');
    }
  });
})();

