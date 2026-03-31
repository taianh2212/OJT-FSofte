(() => {
  const fetchBtn = document.getElementById('fetchBtn');
  const cityInput = document.getElementById('cityInput');
  const statusRow = document.getElementById('statusRow');
  const statusMessage = document.getElementById('statusMessage');
  const insertedEl = document.getElementById('insertedCount');
  const skippedEl = document.getElementById('skippedCount');
  const lastStatus = document.getElementById('lastStatus');
  const lastRunEl = document.getElementById('lastRun');

  async function fetchData() {
    const city = cityInput.value.trim() || 'PARIS';
    fetchBtn.disabled = true;
    statusMessage.textContent = 'Calling OpenTripMap...';
    lastStatus.textContent = 'running';
    statusRow.hidden = true;
    try {
      const response = await TB.apiFetch(`/admin/fetch-opentripmap?city=${encodeURIComponent(city)}`);
      const data = response.data;
      if (data) {
        insertedEl.textContent = data.insertedCount ?? 0;
        skippedEl.textContent = data.skippedCount ?? 0;
        lastStatus.textContent = data.status ?? 'SUCCESS';
        statusMessage.textContent = data.message || 'Request completed.';
        statusRow.hidden = false;
      } else {
        statusMessage.textContent = 'Empty response from backend.';
      }
    } catch (err) {
      statusMessage.textContent = `Request failed: ${err.message || 'Unknown error'}`;
      lastStatus.textContent = 'error';
      statusRow.hidden = true;
    } finally {
      lastRunEl.textContent = `Last run: ${new Date().toLocaleString()}`;
      fetchBtn.disabled = false;
    }
  }

  fetchBtn.addEventListener('click', (event) => {
    event.preventDefault();
    fetchData();
  });

  cityInput.addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      fetchData();
    }
  });
})();
