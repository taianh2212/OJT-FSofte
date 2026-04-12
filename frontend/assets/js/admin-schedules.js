document.addEventListener('DOMContentLoaded', async () => {
    const userStr = localStorage.getItem('user');
    if (!userStr) window.location.href = '/pages/auth/login.html';
    const user = JSON.parse(userStr);
    if (user.role !== 'ADMIN' && user.role !== 'STAFF') {
        window.location.href = '/pages/index.html';
        return;
    }
    document.getElementById('userInfo').innerText = user.fullName || user.email;

    await loadSchedules();
});

let currentScheduleId = null;

async function loadSchedules() {
    const tbody = document.querySelector('#schedulesTable tbody');
    try {
        const res = await TB.apiFetch('/api/v1/schedules'); 
        const data = res.data || [];
        tbody.innerHTML = '';
        data.forEach(s => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>SD-${s.id}</td>
                <td>${s.tourName || 'Basic Tour'}</td>
                <td>${s.startDate} - ${s.endDate}</td>
                <td>${s.guideId ? 'Guide #' + s.guideId : '<span style="color:#d97706">Unassigned</span>'}</td>
                <td>${s.status}</td>
                <td><button class="action-btn" onclick="openAssignModal(${s.id})">Assign</button></td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" style="color:red">Error: ${e.message}</td></tr>`;
    }
}

window.openAssignModal = async function(scheduleId) {
    currentScheduleId = scheduleId;
    document.getElementById('targetScheduleId').innerText = 'Selected Schedule: SD-' + scheduleId;
    document.getElementById('assignModal').classList.add('active');
    
    const select = document.getElementById('guideSelect');
    select.innerHTML = '<option>Loading guides...</option>';
    try {
        const res = await TB.apiFetch('/api/v1/staff/guides');
        select.innerHTML = '';
        if (res.data && res.data.length) {
            res.data.forEach(g => {
                select.innerHTML += `<option value="${g.id}">${g.fullName} (${g.email})</option>`;
            });
        } else {
            select.innerHTML = '<option>No guides found</option>';
        }
    } catch (e) {
         select.innerHTML = '<option style="color:red">Error loading guides</option>';
    }
};

window.closeModal = function() {
    document.getElementById('assignModal').classList.remove('active');
};

window.submitAssignment = async function() {
    const guideId = document.getElementById('guideSelect').value;
    try {
        await TB.apiFetch(`/api/v1/staff/schedules/${currentScheduleId}/assign-guide?guideId=${guideId}`, { method: 'PATCH' });
        alert('Guide Assigned Successfully!');
        closeModal();
        loadSchedules();
    } catch (err) {
        alert('Mocked: Error -> ' + err.message + '. But Guide ID ' + guideId + ' assigned in UI.');
        closeModal();
    }
};
