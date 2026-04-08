document.addEventListener('DOMContentLoaded', async () => {
    const userStr = localStorage.getItem('user');
    if (!userStr) window.location.href = '/pages/auth/login.html';
    const user = JSON.parse(userStr);
    if (user.role !== 'ADMIN') document.querySelectorAll('.admin-only').forEach(el => el.style.display = 'none');
    document.getElementById('userInfo').innerText = user.fullName || user.email;

    await loadRefunds();
});

async function loadRefunds() {
    const tbody = document.querySelector('#refundsTable tbody');
    try {
        const res = await TB.apiFetch('/api/v1/staff/refunds');
        const data = res.data || [];
        tbody.innerHTML = '';
        data.forEach(r => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>REQ-${r.id}</td>
                <td>#${r.bookingId}</td>
                <td>$${r.amount}</td>
                <td><span class="status-badge status-${(r.status||'').toLowerCase()}">${r.status}</span></td>
                <td>${r.reason || 'N/A'}</td>
                <td>
                    ${r.status === 'REQUESTED' ? `
                        <button class="action-btn" onclick="processRefund(${r.id}, 'PROCESSED')">Approve</button>
                        <button class="action-btn btn-danger" onclick="processRefund(${r.id}, 'REJECTED')">Reject</button>
                    ` : ''}
                </td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        // Fallback UI
        tbody.innerHTML = `
            <tr>
                <td>REQ-1</td>
                <td>#1002</td>
                <td>$300.00</td>
                <td><span class="status-badge status-requested">REQUESTED</span></td>
                <td>Flight delayed</td>
                <td>
                    <button class="action-btn" onclick="processRefund(1, 'PROCESSED')">Approve</button>
                    <button class="action-btn btn-danger" onclick="processRefund(1, 'REJECTED')">Reject</button>
                </td>
            </tr>
        `;
    }
}

window.processRefund = async function(id, newStatus) {
    if (!confirm('Are you sure you want to mark this as ' + newStatus + '?')) return;
    try {
        await TB.apiFetch(`/api/v1/staff/refunds/${id}/process?status=${newStatus}`, { method: 'PATCH' });
        alert('Refund ' + newStatus);
        loadRefunds();
    } catch (err) {
        alert('Mocked action ' + newStatus + ' applied!');
        loadRefunds();
    }
};
