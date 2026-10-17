/**
 * Roles Management Script
 */
        // State quản lý trang
        let currentPage = 0;
        let pageSize = 20;
        let totalItems = 0;
        let deleteTargetId = null;
        let searchTimeout = null;

        // DOM Elements
        const roleTableBody = document.getElementById('roleTableBody');
        const paginationInfo = document.getElementById('paginationInfo');
        const paginationControls = document.getElementById('paginationControls');
        const pageSizeSelect = document.getElementById('pageSizeSelect');
        const searchInput = document.getElementById('searchInput');
        const statusFilter = document.getElementById('statusFilter');
        const btnClearFilter = document.getElementById('btnClearFilter');

        // Delete Modal Elements
        const deleteConfirmModal = document.getElementById('deleteConfirmModal');
        const deleteRoleNameSpan = document.getElementById('deleteRoleName');
        const deleteWarningBox = document.getElementById('deleteWarningBox');
        const deleteWarningText = document.getElementById('deleteWarningText');
        const btnCloseDeleteModal = document.getElementById('btnCloseDeleteModal');
        const btnCancelDeleteModal = document.getElementById('btnCancelDeleteModal');
        const btnConfirmDelete = document.getElementById('btnConfirmDelete');

        // Khởi động
        document.addEventListener('DOMContentLoaded', () => {
            loadRoles();
            setupEventListeners();
        });

        function setupEventListeners() {
            btnCloseDeleteModal.addEventListener('click', closeDeleteModal);
            btnCancelDeleteModal.addEventListener('click', closeDeleteModal);
            btnConfirmDelete.addEventListener('click', executeDelete);

            searchInput.addEventListener('input', () => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    currentPage = 0;
                    loadRoles();
                }, 350);
            });

            statusFilter.addEventListener('change', () => {
                currentPage = 0;
                loadRoles();
            });

            btnClearFilter.addEventListener('click', () => {
                searchInput.value = '';
                statusFilter.value = '';
                currentPage = 0;
                loadRoles();
            });

            pageSizeSelect.addEventListener('change', (e) => {
                pageSize = parseInt(e.target.value);
                currentPage = 0;
                loadRoles();
            });
        }

        // Tải danh sách vai trò
        async function loadRoles() {
            const keyword = searchInput.value.trim();
            const activeVal = statusFilter.value;

            const params = new URLSearchParams({
                page: currentPage,
                size: pageSize
            });

            if (keyword) params.append('keyword', keyword);
            if (activeVal !== '') params.append('active', activeVal);

            try {
                const response = await fetch(`/api/roles/search?${params.toString()}`);
                if (response.status === 401) {
                    window.location.href = '/login';
                    return;
                }
                if (!response.ok) {
                    throw new Error('Lỗi khi tải danh sách vai trò');
                }
                const data = await response.json();
                renderTable(data.items || data.content || [], data.page, data.size, data.total);
            } catch (err) {
                roleTableBody.innerHTML = `
                    <tr>
                        <td colspan="8" style="text-align: center; padding: 40px; color: #dc2626;">
                            Có lỗi khi tải dữ liệu: ${err.message}
                        </td>
                    </tr>
                `;
            }
        }

        // Render dữ liệu bảng
        function renderTable(roles, page, size, total) {
            totalItems = total;
            currentPage = page;

            if (!roles || roles.length === 0) {
                roleTableBody.innerHTML = `
                    <tr>
                        <td colspan="8" style="text-align: center; padding: 40px; color: var(--slate-400);">
                            Không tìm thấy vai trò nào phù hợp
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
            roles.forEach((role, index) => {
                const stt = String(page * size + index + 1).padStart(2, '0');
                const desc = role.description || '-';
                const isAdmin = role.code === 'ADMIN';

                const statusHtml = role.active 
                    ? `<span class="status-pill active">Hoạt động</span>`
                    : `<span class="status-pill inactive">Đã khóa</span>`;

                html += `
                    <tr>
                        <td style="color: var(--slate-500); font-size: 13px;">${stt}</td>
                        <td style="white-space: nowrap;">
                            <span class="role-code-tag">${escapeHtml(role.code)}</span>
                        </td>
                        <td>
                            <a href="/admin/roles/edit?id=${role.id}" style="color: var(--slate-900); font-weight: 700; text-decoration: none;" onmouseover="this.style.color='var(--primary-green)'" onmouseout="this.style.color='var(--slate-900)'">
                                ${escapeHtml(role.name)}
                            </a>
                        </td>
                        <td style="color: var(--slate-600); max-width: 280px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                            ${escapeHtml(desc)}
                        </td>
                        <td style="text-align: center; white-space: nowrap;">
                            <span class="count-pill">
                                <i class="ph ph-user"></i>
                                <span>${role.userCount} tài khoản</span>
                            </span>
                        </td>
                        <td style="text-align: center; white-space: nowrap;">
                            <span class="perm-count-pill">
                                <i class="ph ph-shield-check"></i>
                                <span>${role.permissionCount} quyền</span>
                            </span>
                        </td>
                        <td style="white-space: nowrap;">${statusHtml}</td>
                        <td style="text-align: right; padding-right: 16px; white-space: nowrap;">
                            <div class="action-btn-group" style="justify-content: flex-end;">
                                <a href="/admin/roles/edit?id=${role.id}" class="btn-action-icon" title="Chỉnh sửa vai trò & phân quyền">
                                    <i class="ph ph-pencil-simple"></i>
                                </a>
                                ${!isAdmin ? `
                                    <button type="button" class="btn-action-icon text-warning" title="${role.active ? 'Khóa vai trò' : 'Mở khóa vai trò'}" onclick="toggleStatus('${role.id}')">
                                        <i class="ph ${role.active ? 'ph-lock' : 'ph-lock-open'}"></i>
                                    </button>
                                    <button type="button" class="btn-action-icon text-danger" title="Xóa vai trò" onclick="openDeleteModal('${role.id}', '${escapeHtml(role.name)}', ${role.userCount})">
                                        <i class="ph ph-trash"></i>
                                    </button>
                                ` : `
                                    <button type="button" class="btn-action-icon" style="opacity: 0.3; cursor: not-allowed;" title="Vai trò Quản trị viên tối cao không thể xóa hoặc khóa" disabled>
                                        <i class="ph ph-lock-key"></i>
                                    </button>
                                `}
                            </div>
                        </td>
                    </tr>
                `;
            });

            roleTableBody.innerHTML = html;
            renderPagination(page, size, total);
        }

        // Render phân trang góc phải
        function renderPagination(page, size, total) {
            const totalPages = Math.ceil(total / size) || 1;
            let html = '';

            html += `
                <button type="button" class="page-btn ${page === 0 ? 'disabled' : ''}" 
                        onclick="goToPage(${page - 1})" ${page === 0 ? 'disabled' : ''}>
                    <i class="ph ph-caret-left"></i>
                </button>
            `;

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
            loadRoles();
        }

        // Toggle trạng thái
        async function toggleStatus(id) {
            try {
                const response = await fetch(`/api/roles/${id}/toggle-status`, {
                    method: 'PATCH'
                });
                if (!response.ok) {
                    const errData = await response.json().catch(() => ({}));
                    throw new Error(errData.message || 'Không thể thay đổi trạng thái vai trò');
                }
                const updated = await response.json();
                showToast(`Đã ${updated.active ? 'kích hoạt' : 'khóa'} vai trò ${updated.name}!`);
                loadRoles();
            } catch (err) {
                showToast('Lỗi: ' + err.message, true);
            }
        }

        // Modal xác nhận xóa
        function openDeleteModal(id, name, userCount) {
            deleteTargetId = id;
            deleteRoleNameSpan.textContent = name;

            if (userCount > 0) {
                deleteWarningText.textContent = `Vai trò này hiện đang có ${userCount} tài khoản được gán. Hãy chuyển vai trò cho các tài khoản đó trước khi thực hiện xóa.`;
                deleteWarningBox.style.display = 'block';
                btnConfirmDelete.disabled = true;
                btnConfirmDelete.style.opacity = '0.5';
                btnConfirmDelete.style.cursor = 'not-allowed';
            } else {
                deleteWarningBox.style.display = 'none';
                btnConfirmDelete.disabled = false;
                btnConfirmDelete.style.opacity = '1';
                btnConfirmDelete.style.cursor = 'pointer';
            }

            deleteConfirmModal.classList.add('show');
        }

        function closeDeleteModal() {
            deleteConfirmModal.classList.remove('show');
            deleteTargetId = null;
        }

        // Thực hiện xóa
        async function executeDelete() {
            if (!deleteTargetId) return;

            try {
                const response = await fetch(`/api/roles/${deleteTargetId}`, {
                    method: 'DELETE'
                });

                if (!response.ok) {
                    const errData = await response.json().catch(() => ({}));
                    throw new Error(errData.message || 'Xóa vai trò thất bại');
                }

                showToast('Đã xóa vai trò thành công!');
                closeDeleteModal();
                loadRoles();
            } catch (err) {
                showToast(err.message, true);
            }
        }

        // Toast notification
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
