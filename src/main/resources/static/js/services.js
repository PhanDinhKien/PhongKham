/**
 * Medical Services Management Script
 */
        // State
        let currentPage = 0;
        let pageSize = 20;
        let totalItems = 0;
        let serviceTypes = [];
        let deleteTargetId = null;
        let searchTimeout = null;

        // DOM Elements
        const serviceTableBody = document.getElementById('serviceTableBody');
        const paginationInfo = document.getElementById('paginationInfo');
        const paginationControls = document.getElementById('paginationControls');
        const pageSizeSelect = document.getElementById('pageSizeSelect');
        const searchInput = document.getElementById('searchInput');
        const typeFilter = document.getElementById('typeFilter');
        const statusFilter = document.getElementById('statusFilter');
        const btnClearFilter = document.getElementById('btnClearFilter');

        // Modal Elements
        const serviceModal = document.getElementById('serviceModal');
        const btnOpenAddModal = document.getElementById('btnOpenAddModal');
        const btnCloseModal = document.getElementById('btnCloseModal');
        const btnCancelModal = document.getElementById('btnCancelModal');
        const serviceForm = document.getElementById('serviceForm');
        const modalTitle = document.getElementById('modalTitle');
        const serviceIdInput = document.getElementById('serviceId');
        const serviceCodeInput = document.getElementById('serviceCode');
        const serviceCodeHelp = document.getElementById('serviceCodeHelp');
        const serviceNameInput = document.getElementById('serviceName');
        const serviceTypeSelect = document.getElementById('serviceType');
        const servicePriceInput = document.getElementById('servicePrice');
        const pricePreviewText = document.getElementById('pricePreviewText');
        const serviceDescriptionInput = document.getElementById('serviceDescription');
        const serviceActiveCheckbox = document.getElementById('serviceActive');
        const activeStatusLabel = document.getElementById('activeStatusLabel');

        // Delete Modal Elements
        const deleteConfirmModal = document.getElementById('deleteConfirmModal');
        const deleteServiceName = document.getElementById('deleteServiceName');
        const deleteServiceCode = document.getElementById('deleteServiceCode');
        const btnCloseDeleteModal = document.getElementById('btnCloseDeleteModal');
        const btnCancelDeleteModal = document.getElementById('btnCancelDeleteModal');
        const btnConfirmDelete = document.getElementById('btnConfirmDelete');

        // Khởi động
        document.addEventListener('DOMContentLoaded', async () => {
            await loadServiceTypes();
            loadServices();
            setupEventListeners();
        });

        function setupEventListeners() {
            btnOpenAddModal.addEventListener('click', openAddModal);
            btnCloseModal.addEventListener('click', closeServiceModal);
            btnCancelModal.addEventListener('click', closeServiceModal);

            btnCloseDeleteModal.addEventListener('click', closeDeleteModal);
            btnCancelDeleteModal.addEventListener('click', closeDeleteModal);
            btnConfirmDelete.addEventListener('click', executeDelete);

            serviceForm.addEventListener('submit', handleFormSubmit);

            searchInput.addEventListener('input', () => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    currentPage = 0;
                    loadServices();
                }, 350);
            });

            typeFilter.addEventListener('change', () => {
                currentPage = 0;
                loadServices();
            });

            statusFilter.addEventListener('change', () => {
                currentPage = 0;
                loadServices();
            });

            btnClearFilter.addEventListener('click', () => {
                searchInput.value = '';
                typeFilter.value = '';
                statusFilter.value = '';
                currentPage = 0;
                loadServices();
            });

            pageSizeSelect.addEventListener('change', (e) => {
                pageSize = parseInt(e.target.value);
                currentPage = 0;
                loadServices();
            });

            serviceActiveCheckbox.addEventListener('change', (e) => {
                activeStatusLabel.textContent = e.target.checked ? 'Đang áp dụng' : 'Tạm dừng';
            });

            servicePriceInput.addEventListener('input', (e) => {
                const val = parseFloat(e.target.value) || 0;
                pricePreviewText.textContent = formatCurrency(val);
            });
        }

        // Tải danh mục loại dịch vụ
        async function loadServiceTypes() {
            try {
                const res = await fetch('/api/services/types');
                if (res.ok) {
                    serviceTypes = await res.json();
                    
                    // Điền vào Filter dropdown
                    let filterOptions = '<option value="">Tất cả loại dịch vụ</option>';
                    serviceTypes.forEach(t => {
                        filterOptions += `<option value="${escapeHtml(t.code)}">${escapeHtml(t.label)}</option>`;
                    });
                    typeFilter.innerHTML = filterOptions;

                    // Điền vào Modal select
                    let modalOptions = '<option value="">-- Chọn loại dịch vụ --</option>';
                    serviceTypes.forEach(t => {
                        modalOptions += `<option value="${escapeHtml(t.code)}">${escapeHtml(t.label)}</option>`;
                    });
                    serviceTypeSelect.innerHTML = modalOptions;
                }
            } catch (err) {
                console.error('Không thể tải loại dịch vụ:', err);
            }
        }

        // Tải danh sách dịch vụ
        async function loadServices() {
            const keyword = searchInput.value.trim();
            const serviceType = typeFilter.value;
            const activeVal = statusFilter.value;

            const params = new URLSearchParams({
                page: currentPage,
                size: pageSize
            });

            if (keyword) params.append('keyword', keyword);
            if (serviceType) params.append('serviceType', serviceType);
            if (activeVal !== '') params.append('active', activeVal);

            try {
                const response = await fetch(`/api/services/search?${params.toString()}`);
                if (response.status === 401) {
                    window.location.href = '/login';
                    return;
                }
                if (!response.ok) {
                    throw new Error('Lỗi khi tải danh sách dịch vụ');
                }
                const data = await response.json();
                renderTable(data.items || [], data.page, data.size, data.total);
            } catch (err) {
                serviceTableBody.innerHTML = `
                    <tr>
                        <td colspan="9" style="text-align: center; padding: 40px; color: #dc2626;">
                            Có lỗi khi tải dữ liệu: ${err.message}
                        </td>
                    </tr>
                `;
            }
        }

        // Render bảng dữ liệu dịch vụ
        function renderTable(services, page, size, total) {
            totalItems = total;
            currentPage = page;

            if (!services || services.length === 0) {
                serviceTableBody.innerHTML = `
                    <tr>
                        <td colspan="9" style="text-align: center; padding: 40px; color: var(--slate-400);">
                            Không tìm thấy dịch vụ nào phù hợp
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
            services.forEach((s, index) => {
                const stt = String(page * size + index + 1).padStart(2, '0');
                const desc = s.description || '-';

                // Badge loại dịch vụ
                let typeBadgeClass = 'type-khac';
                let typeIcon = 'ph-folder';
                if (s.serviceType === 'XET_NGHIEM') {
                    typeBadgeClass = 'type-xet-nghiem';
                    typeIcon = 'ph-flask';
                } else if (s.serviceType === 'CHAN_DOAN_HINH_ANH') {
                    typeBadgeClass = 'type-cdha';
                    typeIcon = 'ph-camera';
                } else if (s.serviceType === 'KHAM_BENH') {
                    typeBadgeClass = 'type-kham-benh';
                    typeIcon = 'ph-first-aid';
                } else if (s.serviceType === 'THU_THUAT') {
                    typeBadgeClass = 'type-thu-thuat';
                    typeIcon = 'ph-scissors';
                }

                // Trạng thái pill (trên cùng 1 dòng)
                const statusHtml = s.active 
                    ? `<span class="status-pill active">Đang áp dụng</span>`
                    : `<span class="status-pill inactive">Tạm dừng</span>`;

                html += `
                    <tr>
                        <td style="color: var(--slate-500); font-size: 13px;">${stt}</td>
                        <td style="white-space: nowrap;">
                            <span class="code-badge">${escapeHtml(s.code)}</span>
                        </td>
                        <td>
                            <strong class="service-name">${escapeHtml(s.name)}</strong>
                        </td>
                        <td style="white-space: nowrap;">
                            <span class="type-badge ${typeBadgeClass}">
                                <i class="ph ${typeIcon}"></i>
                                <span>${escapeHtml(s.serviceTypeLabel || s.serviceType)}</span>
                            </span>
                        </td>
                        <td style="text-align: right; white-space: nowrap;">
                            <span class="price-pill">${escapeHtml(s.formattedPrice)}</span>
                        </td>
                        <td style="color: var(--slate-600); max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${escapeHtml(desc)}">
                            ${escapeHtml(desc)}
                        </td>
                        <td style="color: var(--slate-600); font-size: 12.5px; white-space: nowrap;">
                            <div>${escapeHtml(s.updatedBy)}</div>
                            <div style="font-size: 11px; color: var(--slate-400);">${formatDate(s.updatedAt)}</div>
                        </td>
                        <td style="white-space: nowrap;">${statusHtml}</td>
                        <td style="text-align: right; padding-right: 16px; white-space: nowrap;">
                            <div class="action-btn-group" style="justify-content: flex-end;">
                                <button type="button" class="btn-action-icon" title="Chỉnh sửa dịch vụ" onclick="openEditModal('${s.id}')">
                                    <i class="ph ph-pencil-simple"></i>
                                </button>
                                <button type="button" class="btn-action-icon text-warning" title="${s.active ? 'Tạm dừng dịch vụ' : 'Kích hoạt dịch vụ'}" onclick="toggleStatus('${s.id}')">
                                    <i class="ph ${s.active ? 'ph-lock' : 'ph-lock-open'}"></i>
                                </button>
                                <button type="button" class="btn-action-icon text-danger" title="Xóa mềm dịch vụ" onclick="openDeleteModal('${s.id}', '${escapeHtml(s.name)}', '${escapeHtml(s.code)}')">
                                    <i class="ph ph-trash"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            });

            serviceTableBody.innerHTML = html;
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
            loadServices();
        }

        // Mở Modal Thêm mới
        function openAddModal() {
            modalTitle.textContent = 'Thêm dịch vụ mới';
            serviceIdInput.value = '';
            serviceForm.reset();
            serviceCodeInput.readOnly = false;
            serviceCodeHelp.style.display = 'none';
            serviceActiveCheckbox.checked = true;
            activeStatusLabel.textContent = 'Đang áp dụng';
            pricePreviewText.textContent = '0 đ';

            serviceModal.classList.add('show');
            serviceCodeInput.focus();
        }

        // Mở Modal Sửa
        async function openEditModal(id) {
            try {
                const response = await fetch(`/api/services/${id}`);
                if (!response.ok) throw new Error('Không thể tải thông tin dịch vụ');
                const s = await response.json();

                modalTitle.textContent = 'Cập nhật dịch vụ y tế';
                serviceIdInput.value = s.id;
                serviceCodeInput.value = s.code || '';
                serviceCodeInput.readOnly = true;
                serviceCodeHelp.style.display = 'block';
                serviceNameInput.value = s.name || '';
                serviceTypeSelect.value = s.serviceType || '';
                servicePriceInput.value = s.price || 0;
                pricePreviewText.textContent = s.formattedPrice || '0 đ';
                serviceDescriptionInput.value = s.description || '';
                serviceActiveCheckbox.checked = s.active;
                activeStatusLabel.textContent = s.active ? 'Đang áp dụng' : 'Tạm dừng';

                serviceModal.classList.add('show');
                serviceNameInput.focus();
            } catch (err) {
                showToast('Lỗi: ' + err.message, true);
            }
        }

        function closeServiceModal() {
            serviceModal.classList.remove('show');
        }

        // Submit form
        async function handleFormSubmit(e) {
            e.preventDefault();

            const id = serviceIdInput.value;
            const code = serviceCodeInput.value.trim().toUpperCase();
            const name = serviceNameInput.value.trim();
            const serviceType = serviceTypeSelect.value;
            const price = parseFloat(servicePriceInput.value) || 0;
            const description = serviceDescriptionInput.value.trim();
            const active = serviceActiveCheckbox.checked;

            if (!serviceType) {
                showToast('Vui lòng chọn loại dịch vụ', true);
                return;
            }

            const isEdit = !!id;
            const payload = isEdit
                ? { name, price, serviceType, active, description: description || null }
                : { code, name, price, serviceType, active, description: description || null };

            try {
                const url = isEdit ? `/api/services/${id}` : '/api/services';
                const method = isEdit ? 'PUT' : 'POST';

                const response = await fetch(url, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                if (!response.ok) {
                    const errData = await response.json().catch(() => ({}));
                    throw new Error(errData.message || (isEdit ? 'Cập nhật thất bại' : 'Thêm dịch vụ thất bại'));
                }

                showToast(isEdit ? 'Đã cập nhật dịch vụ thành công!' : 'Đã thêm dịch vụ mới thành công!');
                closeServiceModal();
                loadServices();
            } catch (err) {
                showToast(err.message, true);
            }
        }

        // Toggle trạng thái
        async function toggleStatus(id) {
            try {
                const response = await fetch(`/api/services/${id}/toggle-status`, {
                    method: 'PATCH'
                });
                if (!response.ok) {
                    const errData = await response.json().catch(() => ({}));
                    throw new Error(errData.message || 'Không thể đổi trạng thái dịch vụ');
                }
                const updated = await response.json();
                showToast(`Đã chuyển dịch vụ "${updated.name}" sang trạng thái: ${updated.active ? 'Đang áp dụng' : 'Tạm dừng'}!`);
                loadServices();
            } catch (err) {
                showToast('Lỗi: ' + err.message, true);
            }
        }

        // Modal xác nhận xóa mềm
        function openDeleteModal(id, name, code) {
            deleteTargetId = id;
            deleteServiceName.textContent = name;
            deleteServiceCode.textContent = code;
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
                const response = await fetch(`/api/services/${deleteTargetId}`, {
                    method: 'DELETE'
                });

                if (!response.ok) {
                    const errData = await response.json().catch(() => ({}));
                    throw new Error(errData.message || 'Xóa dịch vụ thất bại');
                }

                showToast('Đã xóa mềm dịch vụ thành công!');
                closeDeleteModal();
                loadServices();
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

        function formatCurrency(val) {
            return new Intl.NumberFormat('vi-VN').format(val) + ' đ';
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
