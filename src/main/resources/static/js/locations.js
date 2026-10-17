/**
 * Locations Management Script
 */
        // State
        let currentPage = 0;
        let pageSize = 20;
        let totalItems = 0;
        let deleteTargetId = null;
        let searchTimeout = null;

        // DOM Elements
        const locationTableBody = document.getElementById('locationTableBody');
        const paginationInfo = document.getElementById('paginationInfo');
        const paginationControls = document.getElementById('paginationControls');
        const pageSizeSelect = document.getElementById('pageSizeSelect');
        const searchInput = document.getElementById('searchInput');
        const statusFilter = document.getElementById('statusFilter');
        const btnClearFilter = document.getElementById('btnClearFilter');

        // Modal Elements
        const locationModal = document.getElementById('locationModal');
        const btnOpenAddModal = document.getElementById('btnOpenAddModal');
        const btnCloseModal = document.getElementById('btnCloseModal');
        const btnCancelModal = document.getElementById('btnCancelModal');
        const locationForm = document.getElementById('locationForm');
        const modalTitle = document.getElementById('modalTitle');
        const locationIdInput = document.getElementById('locationId');
        const locationCodeInput = document.getElementById('locationCode');
        const locationCodeHelp = document.getElementById('locationCodeHelp');
        const locationNameInput = document.getElementById('locationName');
        const locationAddressInput = document.getElementById('locationAddress');
        const locationActiveCheckbox = document.getElementById('locationActive');
        const activeStatusLabel = document.getElementById('activeStatusLabel');

        // Delete Modal Elements
        const deleteConfirmModal = document.getElementById('deleteConfirmModal');
        const deleteLocationName = document.getElementById('deleteLocationName');
        const deleteLocationCode = document.getElementById('deleteLocationCode');
        const btnCloseDeleteModal = document.getElementById('btnCloseDeleteModal');
        const btnCancelDeleteModal = document.getElementById('btnCancelDeleteModal');
        const btnConfirmDelete = document.getElementById('btnConfirmDelete');

        // Khởi động
        document.addEventListener('DOMContentLoaded', () => {
            loadLocations();
            setupEventListeners();
        });

        function setupEventListeners() {
            btnOpenAddModal.addEventListener('click', openAddModal);
            btnCloseModal.addEventListener('click', closeLocationModal);
            btnCancelModal.addEventListener('click', closeLocationModal);

            btnCloseDeleteModal.addEventListener('click', closeDeleteModal);
            btnCancelDeleteModal.addEventListener('click', closeDeleteModal);
            btnConfirmDelete.addEventListener('click', executeDelete);

            locationForm.addEventListener('submit', handleFormSubmit);

            searchInput.addEventListener('input', () => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    currentPage = 0;
                    loadLocations();
                }, 350);
            });

            statusFilter.addEventListener('change', () => {
                currentPage = 0;
                loadLocations();
            });

            btnClearFilter.addEventListener('click', () => {
                searchInput.value = '';
                statusFilter.value = '';
                currentPage = 0;
                loadLocations();
            });

            pageSizeSelect.addEventListener('change', (e) => {
                pageSize = parseInt(e.target.value);
                currentPage = 0;
                loadLocations();
            });

            locationActiveCheckbox.addEventListener('change', (e) => {
                activeStatusLabel.textContent = e.target.checked ? 'Đang hoạt động' : 'Tạm dừng';
            });
        }

        // Tải danh sách địa điểm khám
        async function loadLocations() {
            const keyword = searchInput.value.trim();
            const activeVal = statusFilter.value;

            const params = new URLSearchParams({
                page: currentPage,
                size: pageSize
            });

            if (keyword) params.append('keyword', keyword);
            if (activeVal !== '') params.append('active', activeVal);

            try {
                const response = await fetch(`/api/locations/search?${params.toString()}`);
                if (response.status === 401) {
                    window.location.href = '/login';
                    return;
                }
                if (!response.ok) {
                    throw new Error('Lỗi khi tải danh sách địa điểm');
                }
                const data = await response.json();
                renderTable(data.items || [], data.page, data.size, data.total);
            } catch (err) {
                locationTableBody.innerHTML = `
                    <tr>
                        <td colspan="7" style="text-align: center; padding: 40px; color: #dc2626;">
                            Có lỗi khi tải dữ liệu: ${err.message}
                        </td>
                    </tr>
                `;
            }
        }

        // Render bảng dữ liệu
        function renderTable(locations, page, size, total) {
            totalItems = total;
            currentPage = page;

            if (!locations || locations.length === 0) {
                locationTableBody.innerHTML = `
                    <tr>
                        <td colspan="7" style="text-align: center; padding: 40px; color: var(--slate-400);">
                            Không tìm thấy địa điểm khám nào phù hợp
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
            locations.forEach((loc, index) => {
                const stt = String(page * size + index + 1).padStart(2, '0');

                // Trạng thái pill (trên cùng 1 dòng)
                const statusHtml = loc.active 
                    ? `<span class="status-pill active">Hoạt động</span>`
                    : `<span class="status-pill inactive">Tạm dừng</span>`;

                html += `
                    <tr>
                        <td style="color: var(--slate-500); font-size: 13px;">${stt}</td>
                        <td style="white-space: nowrap;">
                            <span class="code-badge">${escapeHtml(loc.code)}</span>
                        </td>
                        <td>
                            <div style="display: flex; align-items: center; gap: 8px;">
                                <i class="ph-bold ph-buildings" style="color: var(--primary-green); font-size: 16px;"></i>
                                <strong style="color: var(--slate-900); font-weight: 700;">${escapeHtml(loc.name)}</strong>
                            </div>
                        </td>
                        <td style="color: var(--slate-700);">
                            <div style="display: flex; align-items: flex-start; gap: 6px;">
                                <i class="ph-fill ph-map-pin" style="color: #ef4444; font-size: 15px; margin-top: 2px; flex-shrink: 0;"></i>
                                <span>${escapeHtml(loc.address)}</span>
                            </div>
                        </td>
                        <td style="color: var(--slate-600); font-size: 12.5px; white-space: nowrap;">
                            <div>${escapeHtml(loc.updatedBy)}</div>
                            <div style="font-size: 11px; color: var(--slate-400);">${formatDate(loc.updatedAt)}</div>
                        </td>
                        <td style="white-space: nowrap;">${statusHtml}</td>
                        <td style="text-align: right; padding-right: 16px; white-space: nowrap;">
                            <div class="action-btn-group" style="justify-content: flex-end;">
                                <button type="button" class="btn-action-icon" title="Chỉnh sửa địa điểm" onclick="openEditModal('${loc.id}')">
                                    <i class="ph ph-pencil-simple"></i>
                                </button>
                                <button type="button" class="btn-action-icon text-warning" title="${loc.active ? 'Tạm dừng địa điểm' : 'Kích hoạt địa điểm'}" onclick="toggleStatus('${loc.id}')">
                                    <i class="ph ${loc.active ? 'ph-lock' : 'ph-lock-open'}"></i>
                                </button>
                                <button type="button" class="btn-action-icon text-danger" title="Xóa mềm địa điểm" onclick="openDeleteModal('${loc.id}', '${escapeHtml(loc.name)}', '${escapeHtml(loc.code)}')">
                                    <i class="ph ph-trash"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            });

            locationTableBody.innerHTML = html;
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
            loadLocations();
        }

        // Mở Modal Thêm mới
        function openAddModal() {
            modalTitle.textContent = 'Thêm địa điểm khám';
            locationIdInput.value = '';
            locationForm.reset();
            locationCodeInput.readOnly = false;
            locationCodeHelp.style.display = 'none';
            locationActiveCheckbox.checked = true;
            activeStatusLabel.textContent = 'Đang hoạt động';

            locationModal.classList.add('show');
            locationCodeInput.focus();
        }

        // Mở Modal Sửa
        async function openEditModal(id) {
            try {
                const response = await fetch(`/api/locations/${id}`);
                if (!response.ok) throw new Error('Không thể tải thông tin địa điểm');
                const loc = await response.json();

                modalTitle.textContent = 'Cập nhật địa điểm khám';
                locationIdInput.value = loc.id;
                locationCodeInput.value = loc.code || '';
                locationCodeInput.readOnly = true;
                locationCodeHelp.style.display = 'block';
                locationNameInput.value = loc.name || '';
                locationAddressInput.value = loc.address || '';
                locationActiveCheckbox.checked = loc.active;
                activeStatusLabel.textContent = loc.active ? 'Đang hoạt động' : 'Tạm dừng';

                locationModal.classList.add('show');
                locationNameInput.focus();
            } catch (err) {
                showToast('Lỗi: ' + err.message, true);
            }
        }

        function closeLocationModal() {
            locationModal.classList.remove('show');
        }

        // Submit form
        async function handleFormSubmit(e) {
            e.preventDefault();

            const id = locationIdInput.value;
            const code = locationCodeInput.value.trim().toUpperCase();
            const name = locationNameInput.value.trim();
            const address = locationAddressInput.value.trim();
            const active = locationActiveCheckbox.checked;

            const isEdit = !!id;
            const payload = isEdit
                ? { name, address, active }
                : { code, name, address, active };

            try {
                const url = isEdit ? `/api/locations/${id}` : '/api/locations';
                const method = isEdit ? 'PUT' : 'POST';

                const response = await fetch(url, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                if (!response.ok) {
                    const errData = await response.json().catch(() => ({}));
                    throw new Error(errData.message || (isEdit ? 'Cập nhật thất bại' : 'Thêm địa điểm thất bại'));
                }

                showToast(isEdit ? 'Đã cập nhật địa điểm thành công!' : 'Đã thêm địa điểm mới thành công!');
                closeLocationModal();
                loadLocations();
            } catch (err) {
                showToast(err.message, true);
            }
        }

        // Toggle trạng thái
        async function toggleStatus(id) {
            try {
                const response = await fetch(`/api/locations/${id}/toggle-status`, {
                    method: 'PATCH'
                });
                if (!response.ok) {
                    const errData = await response.json().catch(() => ({}));
                    throw new Error(errData.message || 'Không thể đổi trạng thái địa điểm');
                }
                const updated = await response.json();
                showToast(`Đã chuyển địa điểm "${updated.name}" sang trạng thái: ${updated.active ? 'Hoạt động' : 'Tạm dừng'}!`);
                loadLocations();
            } catch (err) {
                showToast('Lỗi: ' + err.message, true);
            }
        }

        // Modal xác nhận xóa mềm
        function openDeleteModal(id, name, code) {
            deleteTargetId = id;
            deleteLocationName.textContent = name;
            deleteLocationCode.textContent = code;
            deleteConfirmModal.classList.add('show');
        }

        function closeDeleteModal() {
            deleteConfirmModal.classList.remove('show');
            deleteTargetId = null;
        }

        // Thực hiện xóa mềm
        async function executeDelete() {
            if (!deleteTargetId) return;

            try {
                const response = await fetch(`/api/locations/${deleteTargetId}`, {
                    method: 'DELETE'
                });

                if (!response.ok) {
                    const errData = await response.json().catch(() => ({}));
                    throw new Error(errData.message || 'Xóa địa điểm thất bại');
                }

                showToast('Đã xóa mềm địa điểm thành công!');
                closeDeleteModal();
                loadLocations();
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

        function formatDate(dateStr) {
            if (!dateStr) return '-';
            const d = new Date(dateStr);
            if (isNaN(d.getTime())) return dateStr;
            return d.toLocaleDateString('vi-VN', { hour: '2-digit', minute: '2-digit' });
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
