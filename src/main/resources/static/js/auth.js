document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const errorMsg = document.getElementById('errorMessage');
    
    try {
        const response = await fetch('/api/v1/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            // Success: Store token and redirect
            localStorage.setItem('token', result.data.token);
            localStorage.setItem('user', JSON.stringify(result.data.user));
            
            // Redirect based on role
            if (result.data.user.role === 'ADMIN') {
                window.location.href = '/admin/dashboard.html';
            } else {
                window.location.href = '/user/index.html';
            }
        } else {
            errorMsg.innerText = result.message || 'Login failed';
            errorMsg.style.display = 'block';
        }
    } catch (error) {
        errorMsg.innerText = 'Something went wrong. Please try again.';
        errorMsg.style.display = 'block';
    }
});
