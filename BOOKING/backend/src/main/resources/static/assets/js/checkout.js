(async () => {
  const params = new URLSearchParams(window.location.search);
  const tourId = params.get('tourId');
  const scheduleId = params.get('scheduleId');
  const isCanceled = params.get('cancel') === 'true';

  if (isCanceled) {
    alert('Bạn đã hủy thanh toán. Đơn hàng chưa được hoàn tất.');
    window.location.href = './tours.html';
    return;
  }

  if (!tourId || !scheduleId) {
    alert('Thông tin không hợp lệ!');
    window.location.href = './tours.html';
    return;
  }

  const user = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;
  if (!user) {
    TB.goToLogin('Vui lòng đăng nhập để tiếp tục đặt tour.');
    return;
  }

  // UI Elements
  const loading = document.getElementById('checkoutLoading');
  const content = document.getElementById('checkoutContent');
  const numPeopleInput = document.getElementById('numPeople');
  const summaryTourName = document.getElementById('summaryTourName');
  const summaryDate = document.getElementById('summaryDate');
  const summaryUnitPrice = document.getElementById('summaryUnitPrice');
  const summaryPeople = document.getElementById('summaryPeople');
  const summaryTotalPrice = document.getElementById('summaryTotalPrice');
  const confirmBtn = document.getElementById('confirmBtn');

  // Load User Data
  document.getElementById('custName').value = user.fullName || '';
  document.getElementById('custEmail').value = user.email || '';

  let tourData = null;
  let scheduleData = null;
  let currentPrice = 0;

  try {
    const [tourRes, scheduleRes] = await Promise.all([
      TB.apiFetch(`/api/v1/tours/${tourId}`),
      TB.apiFetch(`/api/v1/tours/schedules/${scheduleId}`)
    ]);

    tourData = tourRes.data;
    scheduleData = scheduleRes.data;
    currentPrice = tourData.price;

    summaryTourName.textContent = tourData.tourName;
    summaryDate.textContent = new Date(scheduleData.startDate).toLocaleDateString('vi-VN');
    summaryUnitPrice.textContent = tourData.price.toLocaleString('vi-VN') + ' đ';

    updateTotals();
    
    loading.style.display = 'none';
    content.style.display = 'grid';
  } catch (err) {
    console.error(err);
    alert('Không thể tải thông tin tour. Vui lòng thử lại.');
    window.location.href = './tours.html';
  }

  function updateTotals() {
    const count = parseInt(numPeopleInput.value) || 1;
    summaryPeople.textContent = count;
    summaryTotalPrice.textContent = (count * currentPrice).toLocaleString('vi-VN');
  }

  document.getElementById('plusBtn').onclick = () => {
    numPeopleInput.value = parseInt(numPeopleInput.value) + 1;
    updateTotals();
  };

  document.getElementById('minusBtn').onclick = () => {
    if (numPeopleInput.value > 1) {
      numPeopleInput.value = parseInt(numPeopleInput.value) - 1;
      updateTotals();
    }
  };

  numPeopleInput.onchange = updateTotals;

  // Toggle Payment Method
  const methodCards = document.querySelectorAll('.method-card');
  let selectedMethod = 'PAYOS';

  methodCards.forEach(card => {
    card.onclick = () => {
      methodCards.forEach(c => c.classList.remove('active'));
      card.classList.add('active');
      selectedMethod = card.dataset.method;
    };
  });

  confirmBtn.onclick = async () => {
    confirmBtn.disabled = true;
    confirmBtn.textContent = 'ĐANG XỬ LÝ...';

    try {
      // 1. Create Booking
      const bookingReq = {
        userId: user.id,
        scheduleId: parseInt(scheduleId),
        numberOfPeople: parseInt(numPeopleInput.value)
      };

      const bookingRes = await TB.apiFetch('/api/v1/bookings', {
        method: 'POST',
        body: JSON.stringify(bookingReq)
      });

      const bookingId = bookingRes.data.id;

      // 2. Handle Payment
      if (selectedMethod === 'PAYOS') {
        const paymentReq = {
          bookingId: bookingId,
          paymentMethod: 'PAYOS'
        };

        const paymentRes = await TB.apiFetch('/api/v1/payments/payos/create', {
          method: 'POST',
          body: JSON.stringify(paymentReq)
        });

        if (paymentRes.data?.checkoutUrl) {
          window.location.href = paymentRes.data.checkoutUrl;
        } else {
          alert('Không thể tạo liên kết thanh toán. Vui lòng liên hệ hỗ trợ.');
        }
      } else {
        // Cash / Manual
        alert('Đặt tour thành công! Vui lòng đến văn phòng Danangbest để hoàn tất thanh toán.');
        window.location.href = './index.html';
      }
    } catch (err) {
      console.error(err);
      alert('Đã xảy ra lỗi: ' + err.message);
      confirmBtn.disabled = false;
      confirmBtn.textContent = 'XÁC NHẬN ĐẶT TOUR';
    }
  };
})();
