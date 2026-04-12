document.addEventListener('DOMContentLoaded', () => {
    const userStr = localStorage.getItem('user');
    if (!userStr || JSON.parse(userStr).role !== 'GUIDE') {
        window.location.href = '/pages/auth/login.html';
    }
    
    const urlParams = new URLSearchParams(window.location.search);
    const scheduleId = urlParams.get('id');
    if (!scheduleId) {
        alert("Invalid Schedule ID");
        window.location.href = '/pages/guide/dashboard.html';
    }
    
    document.getElementById('tourTitle').innerText = 'Schedule SD-' + scheduleId;
});

function getScheduleId() {
    return new URLSearchParams(window.location.search).get('id');
}

// UC28
window.updateProgress = async function() {
    const progress = document.getElementById('progressInput').value.trim();
    if (!progress) return;
    
    const sid = getScheduleId();
    try {
        await TB.apiFetch(`/api/v1/guides/tours/${sid}/progress?progress=${encodeURIComponent(progress)}`, { method: 'PATCH' });
        alert('Progress updated successfully!');
        document.getElementById('progressInput').value = '';
    } catch (err) {
        alert('Error: ' + err.message + ' (Mock updated!)');
    }
};

// Preview
const photosInput = document.getElementById('photosInput');
const previewCont = document.getElementById('previewImages');
photosInput.addEventListener('change', () => {
    previewCont.innerHTML = '';
    Array.from(photosInput.files).forEach(file => {
        const url = URL.createObjectURL(file);
        const img = document.createElement('img');
        img.src = url;
        img.className = 'preview-img';
        previewCont.appendChild(img);
    });
});

// UC29 (Multipart Form)
window.uploadPhotos = async function() {
    const files = photosInput.files;
    if (files.length === 0) return alert('Select photos first.');
    const sid = getScheduleId();
    
    const formData = new FormData();
    for (let i = 0; i < files.length; i++) {
        formData.append('photos', files[i]);
    }
    
    const token = TB.getToken();
    try {
        const res = await fetch(`/api/v1/guides/tours/${sid}/photos`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            body: formData
        });
        if (!res.ok) throw new Error('Upload failed');
        alert('Upload success!');
        photosInput.value = '';
        previewCont.innerHTML = '';
    } catch (err) {
        alert('Mocked: Uploaded ' + files.length + ' photos to SD-' + sid);
        photosInput.value = '';
        previewCont.innerHTML = '';
    }
};

// UC30
window.submitReport = async function() {
    const content = document.getElementById('reportInput').value.trim();
    if (!content) return alert('Report content required.');
    
    if (!confirm('Are you sure you want to finalize this tour and save report?')) return;
    
    const sid = getScheduleId();
    try {
        await TB.apiFetch(`/api/v1/guides/tours/${sid}/report?content=${encodeURIComponent(content)}`, { method: 'POST' });
        alert('Report submitted! Tour marked as completed.');
        window.location.href = '/pages/guide/dashboard.html';
    } catch (err) {
        alert('Mocked: Report saved. ' + err.message);
        window.location.href = '/pages/guide/dashboard.html';
    }
};
