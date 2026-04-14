(() => {
  const ids = JSON.parse(localStorage.getItem('compareIds') || '[]');
  const grid = document.getElementById('grid');
  const empty = document.getElementById('empty');

  if (ids.length === 0) {
    empty.style.display = 'block';
    return;
  }

  async function load() {
    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 50px;"><div class="loader"></div></div>';

    try {
      console.log('Loading comparison for IDs:', ids);
      const idString = ids.join(',');
      const res = await TB.apiFetch(`/api/v1/tours/compare?ids=${idString}`);
      const tours = res.data || [];

      if (tours.length === 0 && ids.length > 0) {
        console.warn('Stale IDs found in localStorage. Clearing compare list.');
        localStorage.removeItem('compareIds');
        empty.style.display = 'block';
        grid.innerHTML = '';
        return;
      }

      if (tours.length === 0) {
        empty.style.display = 'block';
        grid.innerHTML = '';
        return;
      }

      grid.innerHTML = tours.map(t => `
        <div class="compare-card">
          <div style="position: relative; aspect-ratio: 16/10; overflow: hidden;">
            <img src="${(t.imageUrls && t.imageUrls[0]) || 'https://images.unsplash.com/photo-1552074284-5e88ef1aef18?auto=format&fit=crop&w=800'}" style="width:100%; height:100%; object-fit:cover;" alt="${t.tourName}">
            <button class="remove-btn" data-id="${t.id}" style="position:absolute; top:10px; right:10px; background:rgba(0,0,0,0.5); color:white; border:none; width:30px; height:30px; border-radius:50%; cursor:pointer;">✕</button>
          </div>
          
          <div class="compare-section">
            <h3 style="margin:0; font-size:1.2rem; color:var(--primary); line-height:1.4;">${t.tourName}</h3>
          </div>

          <div class="compare-section">
            <span class="section-label">Giá trọn gói</span>
            <span class="price-value">${t.price ? Number(t.price).toLocaleString() + 'đ' : 'Liên hệ'}</span>
          </div>

          <div class="compare-section">
            <span class="section-label">Thời lượng</span>
            <span class="section-value">🕒 ${t.duration ? t.duration + ' Ngày' : 'Liên hệ'}</span>
          </div>

          <div class="compare-section">
            <span class="section-label">Khởi hành</span>
            <span class="section-value">📍 ${t.startLocation || 'Đà Nẵng'}</span>
          </div>

          <div class="compare-section">
            <span class="section-label">Phương tiện</span>
            <span class="section-value">✈️ ${t.transportType || 'Xe du lịch'}</span>
          </div>

          <div class="compare-section">
            <span class="section-label">Điểm nổi bật</span>
            <ul style="margin: 8px 0 0; padding-left: 18px; font-size: 0.85rem; color: var(--text-soft); line-height: 1.6;">
              ${(t.highlights || []).slice(0, 3).map(h => `<li>${h}</li>`).join('') || '<li>Đang cập nhật...</li>'}
            </ul>
          </div>

          <div class="compare-section">
            <span class="section-label">Đánh giá</span>
            <span class="section-value">⭐ ${(t.rating || 5.0).toFixed(1)}</span>
          </div>

          <div class="compare-section">
            <span class="section-label">Đối tượng</span>
            <span class="section-value">👥 ${t.suitableAges || 'Mọi lứa tuổi'}</span>
          </div>

          <div class="compare-section">
            <span class="section-label">Trẻ em</span>
            <span class="section-value">🧒 ${t.childPolicy || 'Theo chính sách chung'}</span>
          </div>

          <div class="compare-section">
            <span class="section-label">Lý do chọn</span>
            <span class="section-value" style="font-size: 0.85em;">✨ ${t.whyChooseUs || 'Chất lượng đảm bảo'}</span>
          </div>

          <div class="compare-section" style="background: var(--bg-section);">
            <a href="./tour-detail.html?id=${t.id}" class="btn" style="width:100%; height:44px; border-radius:10px;">XEM CHI TIẾT</a>
          </div>
        </div>
      `).join('');

      // Add remove handler - Normalize to String
      grid.querySelectorAll('.remove-btn').forEach(btn => {
        btn.onclick = () => {
          const idToRemove = String(btn.dataset.id);
          const currentIds = JSON.parse(localStorage.getItem('compareIds') || '[]');
          const newIds = currentIds.map(String).filter(id => id !== idToRemove);
          localStorage.setItem('compareIds', JSON.stringify(newIds));
          window.location.reload();
        };
      });

    } catch (err) {
      console.error('Error fetching comparison data:', err);
      grid.innerHTML = '<p style="text-align:center; color:var(--price);">Có lỗi xảy ra khi tải dữ liệu so sánh. Thử xóa dữ liệu cũ và chọn lại.</p>';
    }
  }

  load();
})();
