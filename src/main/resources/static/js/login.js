/**
 * Login Page Interactive Script
 */
        // Bật/tắt hiện mật khẩu sử dụng Phosphor Icons
        const toggleBtn = document.getElementById('togglePasswordBtn');
        const pwdInput = document.getElementById('password');
        const eyeIcon = document.getElementById('eyeIcon');

        if (toggleBtn && pwdInput && eyeIcon) {
            toggleBtn.addEventListener('click', function () {
                const isPassword = pwdInput.getAttribute('type') === 'password';
                pwdInput.setAttribute('type', isPassword ? 'text' : 'password');
                
                if (isPassword) {
                    eyeIcon.className = 'ph ph-eye-slash';
                } else {
                    eyeIcon.className = 'ph ph-eye';
                }
            });
        }

        // Tự động điền tài khoản demo khi bấm vào link gợi ý
        const btnQuickDemo = document.getElementById('btnQuickDemo');
        const emailInput = document.getElementById('email');
        if (btnQuickDemo && emailInput) {
            btnQuickDemo.addEventListener('click', function () {
                emailInput.value = 'admin@phongkham.local';
                pwdInput.focus();
            });
        }

        // Hiệu ứng xoay icon loading khi gửi form
        const form = document.getElementById('loginForm');
        const btnSubmit = document.getElementById('btnSubmit');
        const spinner = document.getElementById('btnSpinner');
        const btnText = document.querySelector('.btn-text');

        if (form) {
            form.addEventListener('submit', function () {
                if (emailInput.value && pwdInput.value) {
                    btnSubmit.classList.add('loading');
                    btnSubmit.style.pointerEvents = 'none';
                    btnText.textContent = 'Đang đăng nhập...';
                    if (spinner) spinner.classList.remove('hidden');
                }
            });
        }
