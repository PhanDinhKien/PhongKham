/**
 * Users Management Script
 */
        // State quản lý trang
        let currentPage = 0;
        let pageSize = 20;
        let totalItems = 0;
        let currentRoles = [];
        let deleteTargetId = null;
        let searchTimeout = null;

        // DOM Elements
        const userTableBody = document.getElementById('userTableBody');
        const paginationInfo = document.getElementById('paginationInfo');
        const paginationControls = document.getElementById('paginationControls');
        const pageSizeSelect = document.getElementById('pageSizeSelect');
        const searchInput = document.getElementById('searchInput');
        const roleFilter = document.getElementById('roleFilter');
        const statusFilter = document.getElementById('statusFilter');
        const btnClearFilter = document.getElementById('btnClearFilter');

        // Modal Elements
        const userModal = document.getElementById('userModal');
        const btnOpenAddModal = document.getElementById('btnOpenAddModal');
        const btnCloseModal = document.getElementById('btnCloseModal');
        const btnCancelModal = document.getElementById('btnCancelModal');
        const userForm = document.getElementById('userForm');
        const modalTitle = document.getElementById('modalTitle');
        const userIdInput = document.getElementById('userId');
        const userNameInput = document.getElementById('userName');
        const userStaffCodeInput = document.getElementById('userStaffCode');
        const userEmailInput = document.getElementById('userEmail');
        const userPhoneInput = document.getElementById('userPhone');
        const userPasswordInput = document.getElementById('userPassword');
        const passwordRequiredStar = document.getElementById('passwordRequiredStar');
        const passwordHelpText = document.getElementById('passwordHelpText');
        const userRoleCodeSelect = document.getElementById('userRoleCode');
        const userActiveCheckbox = document.getElementById('userActive');
        const activeStatusLabel = document.getElementById('activeStatusLabel');
        const btnTogglePassword = document.getElementById('btnTogglePassword');
        const eyeIcon = document.getElementById('eyeIcon');

        // Delete Modal Elements
        const deleteConfirmModal = document.getElementById('deleteConfirmModal');
        const deleteUserNameSpan = document.getElementById('deleteUserName');
        const btnCloseDeleteModal = document.getElementById('btnCloseDeleteModal');
        const btnCancelDeleteModal = document.getElementById('btnCancelDeleteModal');
        const btnConfirmDelete = document.getElementById('btnConfirmDelete');

        // Khởi động trang
        document.addEventListener('DOMContentLoaded', () => {
            loadRoles();
            loadUsers();
            setupEventListeners();
        });

        // Thiết lập sự kiện tương tác
        function setupEventListeners() {
            // Nút mở modal thêm
            btnOpenAddModal.addEventListener('click', openAddModal);

            // Nút đóng modal
            btnCloseModal.addEventListener('click', closeUserModal);
            btnCancelModal.addEventListener('click', closeUserModal);

            // Nút đóng modal xóa
            btnCloseDeleteModal.addEventListener('click', closeDeleteModal);
            btnCancelDeleteModal.addEventListener('click', closeDeleteModal);
            btnConfirmDelete.addEventListener('click', executeDelete);

            // Submit form thêm/sửa
            userForm.addEventListener('submit', handleFormSubmit);

            // Tìm kiếm nhanh (debounce 350ms)
            searchInput.addEventListener('input', () => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    currentPage = 0;
                    loadUsers();
                }, 350);
            });

            // Lọc theo vai trò & trạng thái
            roleFilter.addEventListener('change', () => {
                currentPage = 0;
                loadUsers();
            });

            statusFilter.addEventListener('change', () => {
                currentPage = 0;
                loadUsers();
            });

            // Xóa bộ lọc
            btnClearFilter.addEventListener('click', () => {
                searchInput.value = '';
                roleFilter.value = '';
                statusFilter.value = '';
                currentPage = 0;
                loadUsers();
            });

            // Đổi số dòng / trang
            pageSizeSelect.addEventListener('change', (e) => {
                pageSize = parseInt(e.target.value);
                currentPage = 0;
                loadUsers();
            });

            // Toggle hiển thị mật khẩu
            btnTogglePassword.addEventListener('click', () => {
                if (userPasswordInput.type === 'password') {
                    userPasswordInput.type = 'text';
                    eyeIcon.className = 'ph ph-eye-slash';
                } else {
                    userPasswordInput.type = 'password';
                    eyeIcon.className = 'ph ph-eye';
                }
            });

            // Label toggle trạng thái
            userActiveCheckbox.addEventListener('change', (e) => {
                activeStatusLabel.textContent = e.target.checked ? 'Hoạt động' : 'Đã khóa';
            });
        }

        // Tải danh sách vai trò từ API
        async function loadRoles() {
            try {
                const response = await fetch('/api/roles');
                if (response.ok) {
                    currentRoles = await response.json();
                    
                    // Nạp vào dropdown bộ lọc
                    roleFilter.innerHTML = '<option value="">Tất cả vai trò</option>';
                    currentRoles.forEach(role => {
                        const opt = document.createElement('option');
                        opt.value = role.code;
                        opt.textContent = role.name;
                        roleFilter.appendChild(opt);
                    });

                    // Nạp vào dropdown modal
                    userRoleCodeSelect.innerHTML = '<option value="">Chọn vai trò</option>';
                    currentRoles.forEach(role => {
                        const opt = document.createElement('option');
                        opt.value = role.code;
                        opt.textContent = role.name;
                        userRoleCodeSelect.appendChild(opt);
                    });
                }
            } catch (err) {
                console.error('Không thể nạp danh sách vai trò:', err);
            }
        }

        // Tải danh sách tài khoản
        async function loadUsers() {
            const keyword = searchInput.value.trim();
            const roleCode = roleFilter.value;
            const activeVal = statusFilter.value;

            const params = new URLSearchParams({
                page: currentPage,
                size: pageSize
            });

            if (keyword) params.append('keyword', keyword);
            if (roleCode) params.append('roleCode', roleCode);
            if (activeVal !== '') params.append('active', activeVal);

            try {
                const response = await fetch(`/api/users?${params.toString()}`);
                if (response.status === 401) {
                    window.location.href = '/login';
                    return;
                }
                if (!response.ok) {
                    throw new Error('Lỗi khi tải danh sách tài khoản');
                }
                const data = await response.json();
                renderTable(data.items || data.content || [], data.page, data.size, data.total);
            } catch (err) {
                userTableBody.innerHTML = `
                    <tr>
                        <td colspan="7" style="text-align: center; padding: 40px; color: #dc2626;">
                            Có lỗi khi tải dữ liệu: ${err.message}
                        </td>
                    </tr>
                `;
            }
        }

        // Render dữ liệu lên bảng
        function renderTable(users, page, size, total) {
            totalItems = total;
            currentPage = page;

            if (!users || users.length === 0) {
                userTableBody.innerHTML = `
                    <tr>
                        <td colspan="7" style="text-align: center; padding: 40px; color: var(--slate-400);">
                            Không tìm thấy tài khoản nào phù hợp
                        </td>
                    </tr>
                `;
                paginationInfo.textContent = 'Đang xem 0 - 0 trong 0';
                paginationControls.innerHTML = '';
                return;
            }

            const startIndex = page * size + 1;
            const endIndex = Math.min((page + 1) * size, total);
            paginationInfo.textContent = `Đang xem ${startIndex} - ${endIndex} trong ${total}`;

            let html = '';
            users.forEach((user, index) => {
                const stt = String(page * size + index + 1).padStart(2, '0');
                const staffCode = user.staffCode || '-';
                const roleNames = user.roles && user.roles.length > 0 
                    ? user.roles.map(r => r.name).join(', ') 
                    : 'Chưa phân quyền';
                
                const statusHtml = user.active 
                    ? `<span class="status-pill active">Hoạt động</span>`
                    : `<span class="status-pill inactive">Đã khóa</span>`;

                html += `
                    <tr>
                        <td style="color: var(--slate-500); font-size: 13px;">${stt}</td>
                        <td style="color: var(--slate-600); font-weight: 500;">${escapeHtml(staffCode)}</td>
                        <td>
                            <strong style="color: var(--slate-900); font-weight: 700;">${escapeHtml(user.name)}</strong>
                        </td>
                        <td style="color: var(--slate-600);">${escapeHtml(user.email)}</td>
                        <td style="color: var(--slate-700); white-space: nowrap;">${escapeHtml(roleNames)}</td>
                        <td style="white-space: nowrap;">${statusHtml}</td>
                        <td style="text-align: right; padding-right: 16px; white-space: nowrap;">
                            <div class="action-btn-group" style="justify-content: flex-end;">
                                <button type="button" class="btn-action-icon" title="Sao chép tên đăng nhập" onclick="copyLogin('${escapeHtml(user.email)}')">
                                    <i class="ph ph-copy"></i>
                                </button>
                                <button type="button" class="btn-action-icon" title="Chỉnh sửa tài khoản" onclick="openEditModal('${user.id}')">
                                    <i class="ph ph-pencil-simple"></i>
                                </button>
                                <button type="button" class="btn-action-icon text-warning" title="${user.active ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}" onclick="toggleStatus('${user.id}')">
                                    <i class="ph ${user.active ? 'ph-lock' : 'ph-lock-open'}"></i>
                                </button>
                                <button type="button" class="btn-action-icon text-danger" title="Xóa tài khoản" onclick="openDeleteModal('${user.id}', '${escapeHtml(user.name)}')">
                                    <i class="ph ph-trash"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            });

            userTableBody.innerHTML = html;
            renderPagination(page, size, total);
        }

        // Render các nút phân trang
        function renderPagination(page, size, total) {
            const totalPages = Math.ceil(total / size) || 1;
            let html = '';

            // Nút Prev
            html += `
                <button type="button" class="page-btn ${page === 0 ? 'disabled' : ''}" 
                        onclick="goToPage(${page - 1})" ${page === 0 ? 'disabled' : ''}>
                    <i class="ph ph-caret-left"></i>
                </button>
            `;

            // Trang số
            for (let i = 0; i < totalPages; i++) {
                if (i === 0 || i === totalPages - 1 || (i >= page - 1 && i <= page + 1)) {
                    html += `
                        <button type="button" class="page-btn ${i === page ? 'active' : ''}" 
                                onclick="goToPage(${i})">
                            ${i + 1}
                        </button>
                    `;
                } else if (i === page - 2 || i === page + 2) {
                    html += `<span class="page-ellipsis">...</span>`;
                }
            }

            // Nút Next
            html += `
                <button type="button" class="page-btn ${page >= totalPages - 1 ? 'disabled' : ''}" 
                        onclick="goToPage(${page + 1})" ${page >= totalPages - 1 ? 'disabled' : ''}>
                    <i class="ph ph-caret-right"></i>
                </button>
            `;

            paginationControls.innerHTML = html;
        }

        function goToPage(targetPage) {
            if (targetPage < 0) return;
            const totalPages = Math.ceil(totalItems / pageSize) || 1;
            if (targetPage >= totalPages) return;
            currentPage = targetPage;
            loadUsers();
        }

        // Mở Modal Thêm mới
        function openAddModal() {
            modalTitle.textContent = 'Thêm tài khoản';
            userIdInput.value = '';
            userForm.reset();
            userActiveCheckbox.checked = true;
            activeStatusLabel.textContent = 'Hoạt động';
            
            userPasswordInput.required = true;
            passwordRequiredStar.style.display = 'inline';
            passwordHelpText.style.display = 'none';

            userModal.classList.add('show');
            userNameInput.focus();
        }

        // Mở Modal Chỉnh sửa
        async function openEditModal(id) {
            try {
                const response = await fetch(`/api/users/${id}`);
                if (!response.ok) throw new Error('Không thể tải thông tin tài khoản');
                const user = await response.json();

                modalTitle.textContent = 'Chỉnh sửa tài khoản';
                userIdInput.value = user.id;
                userNameInput.value = user.name || '';
                userStaffCodeInput.value = user.staffCode || '';
                userEmailInput.value = user.email || '';
                userPhoneInput.value = user.phone || '';
                userPasswordInput.value = '';
                userPasswordInput.required = false;
                passwordRequiredStar.style.display = 'none';
                passwordHelpText.style.display = 'block';

                userActiveCheckbox.checked = user.active;
                activeStatusLabel.textContent = user.active ? 'Hoạt động' : 'Đã khóa';

                if (user.roles && user.roles.length > 0) {
                    userRoleCodeSelect.value = user.roles[0].code;
                } else {
                    userRoleCodeSelect.value = '';
                }

                userModal.classList.add('show');
            } catch (err) {
                showToast('Lỗi: ' + err.message, true);
            }
        }

        function closeUserModal() {
            userModal.classList.remove('show');
        }

        // Xử lý Submit Form Thêm / Sửa
        async function handleFormSubmit(e) {
            e.preventDefault();

            const id = userIdInput.value;
            const name = userNameInput.value.trim();
            const staffCode = userStaffCodeInput.value.trim();
            const email = userEmailInput.value.trim();
            const phone = userPhoneInput.value.trim();
            const password = userPasswordInput.value;
            const roleCode = userRoleCodeSelect.value;
            const active = userActiveCheckbox.checked;

            if (!roleCode) {
                showToast('Vui lòng chọn vai trò cho tài khoản', true);
                return;
            }

            const payload = {
                name,
                staffCode: staffCode || null,
                email,
                phone: phone || null,
                active,
                roleCode: [roleCode]
            };

            const isEdit = !!id;

            if (isEdit) {
                if (password) {
                    payload.password = password;
                }
            } else {
                if (!password || password.length < 6) {
                    showToast('Mật khẩu tối thiểu 6 ký tự', true);
                    return;
                }
                payload.password = password;
            }

            try {
                const url = isEdit ? `/api/users/${id}` : '/api/users';
                const method = isEdit ? 'PUT' : 'POST';

                const response = await fetch(url, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                if (!response.ok) {
                    const errorData = await response.json().catch(() => ({}));
                    throw new Error(errorData.message || (isEdit ? 'Cập nhật thất bại' : 'Thêm tài khoản thất bại'));
                }

                showToast(isEdit ? 'Đã cập nhật tài khoản thành công!' : 'Đã thêm tài khoản mới thành công!');
                closeUserModal();
                loadUsers();
            } catch (err) {
                showToast(err.message, true);
            }
        }

        // Toggle trạng thái khóa / kích hoạt
        async function toggleStatus(id) {
            try {
                const response = await fetch(`/api/users/${id}/toggle-status`, {
                    method: 'PATCH'
                });
                if (!response.ok) throw new Error('Không thể thay đổi trạng thái');
                const updated = await response.json();
                showToast(`Đã ${updated.active ? 'mở khóa' : 'khóa'} tài khoản thành công!`);
                loadUsers();
            } catch (err) {
                showToast('Lỗi: ' + err.message, true);
            }
        }

        // Mở Modal xác nhận xóa
        function openDeleteModal(id, name) {
            deleteTargetId = id;
            deleteUserNameSpan.textContent = name;
            deleteConfirmModal.classList.add('show');
        }

        function closeDeleteModal() {
            deleteConfirmModal.classList.remove('show');
            deleteTargetId = null;
        }

        // Thực hiện xóa tài khoản
        async function executeDelete() {
            if (!deleteTargetId) return;

            try {
                const response = await fetch(`/api/users/${deleteTargetId}`, {
                    method: 'DELETE'
                });
                if (!response.ok) {
                    const errData = await response.json().catch(() => ({}));
                    throw new Error(errData.message || 'Xóa tài khoản thất bại');
                }

                showToast('Đã xóa tài khoản thành công!');
                closeDeleteModal();
                loadUsers();
            } catch (err) {
                showToast(err.message, true);
            }
        }

        // Sao chép tên đăng nhập vào clipboard
        function copyLogin(text) {
            navigator.clipboard.writeText(text).then(() => {
                showToast(`Đã sao chép: ${text}`);
            }).catch(() => {
                showToast(`Sao chép thất bại: ${text}`, true);
            });
        }

        // Hiển thị thông báo Toast
        function showToast(message, isError = false) {
            const toastContainer = document.getElementById('toastContainer');
            const toast = document.createElement('div');
            toast.className = `toast ${isError ? 'toast-error' : ''}`;
            toast.innerHTML = `
                <i class="ph ${isError ? 'ph-warning-circle' : 'ph-check-circle'}" style="font-size: 20px; color: ${isError ? '#dc2626' : 'var(--primary-green)'};"></i>
                <span>${escapeHtml(message)}</span>
            `;
            toastContainer.appendChild(toast);

            setTimeout(() => {
                toast.style.opacity = '0';
                toast.style.transform = 'translateX(100%)';
                toast.style.transition = 'all 0.3s ease';
                setTimeout(() => toast.remove(), 300);
            }, 3500);
        }

        function escapeHtml(str) {
            if (str == null) return '';
            return String(str)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }
