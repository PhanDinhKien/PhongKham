/**
 * Contracts Management Script
 */
        // State
        let currentPage = 0;
        let pageSize = 20;
        let totalItems = 0;
        let deleteTargetId = null;
        let searchTimeout = null;
        let isEditMode = false;

        // DOM Elements
        const contractTableBody = document.getElementById('contractTableBody');
        const paginationInfo = document.getElementById('paginationInfo');
        const paginationControls = document.getElementById('paginationControls');
        const pageSizeSelect = document.getElementById('pageSizeSelect');
        const searchInput = document.getElementById('searchInput');
        const statusFilter = document.getElementById('statusFilter');
        const btnClearFilter = document.getElementById('btnClearFilter');

        // Modal Elements
        const contractModal = document.getElementById('contractModal');
        const btnOpenAddModal = document.getElementById('btnOpenAddModal');
        const btnCloseModal = document.getElementById('btnCloseModal');
        const btnCancelModal = document.getElementById('btnCancelModal');
        const contractForm = document.getElementById('contractForm');
        const modalTitle = document.getElementById('modalTitle');
        const contractIdInput = document.getElementById('contractId');
        const contractCodeInput = document.getElementById('contractCode');
        const contractCodeError = document.getElementById('contractCodeError');
        const btnAutoCode = document.getElementById('btnAutoCode');
        const contractNameInput = document.getElementById('contractName');
        const companyCodeInput = document.getElementById('companyCode');
        const companyNameInput = document.getElementById('companyName');
        const contractStatusSelect = document.getElementById('contractStatus');
        const btnSaveContract = document.getElementById('btnSaveContract');

        // Delete Modal Elements
        const deleteConfirmModal = document.getElementById('deleteConfirmModal');
        const deleteContractName = document.getElementById('deleteContractName');
        const deleteContractCode = document.getElementById('deleteContractCode');
        const btnCloseDeleteModal = document.getElementById('btnCloseDeleteModal');
        const btnCancelDeleteModal = document.getElementById('btnCancelDeleteModal');
        const btnConfirmDelete = document.getElementById('btnConfirmDelete');

        // Khởi động
        document.addEventListener('DOMContentLoaded', () => {
            loadContracts();
            setupEventListeners();
        });

        function setupEventListeners() {
            btnOpenAddModal.addEventListener('click', openAddModal);
            btnCloseModal.addEventListener('click', closeContractModal);
            btnCancelModal.addEventListener('click', closeContractModal);

            btnCloseDeleteModal.addEventListener('click', closeDeleteModal);
            btnCancelDeleteModal.addEventListener('click', closeDeleteModal);
            btnConfirmDelete.addEventListener('click', executeDelete);

            btnAutoCode.addEventListener('click', () => generateAutoCode(false));

            contractForm.addEventListener('submit', handleFormSubmit);

            // Reset lỗi trùng mã khi người dùng chỉnh sửa mã hợp đồng
            contractCodeInput.addEventListener('input', () => {
                contractCodeError.style.display = 'none';
                contractCodeInput.style.borderColor = '';
            });

            // Tìm kiếm với debounce
            searchInput.addEventListener('input', () => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    currentPage = 0;
                    loadContracts();
                }, 350);
            });

            // Lọc trạng thái
            statusFilter.addEventListener('change', () => {
                currentPage = 0;
                loadContracts();
            });

            // Xóa bộ lọc
            btnClearFilter.addEventListener('click', () => {
                searchInput.value = '';
                statusFilter.value = '';
                currentPage = 0;
                loadContracts();
            });

            // Đổi kích thước trang
            pageSizeSelect.addEventListener('change', () => {
                pageSize = parseInt(pageSizeSelect.value, 10);
                currentPage = 0;
                loadContracts();
            });

            // Đóng modal khi bấm ra ngoài
            window.addEventListener('click', (e) => {
                if (e.target === contractModal) closeContractModal();
                if (e.target === deleteConfirmModal) closeDeleteModal();
            });
        }

        // ==============================================================
        // TẢI DANH SÁCH HỢP ĐỒNG
        // ==============================================================
        async function loadContracts() {
            const keyword = searchInput.value.trim();
            const status = statusFilter.value;

            let url = `/api/contracts/search?page=${currentPage}&size=${pageSize}`;
            if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
            if (status) url += `&status=${encodeURIComponent(status)}`;

            try {
                const response = await fetch(url);
                if (response.status === 401) {
                    window.location.href = '/login';
                    return;
                }
                if (!response.ok) throw new Error('Không thể tải danh sách hợp đồng');

                const data = await response.json();
                const items = data.items || data.content || [];
                const total = data.total ?? data.totalElements ?? 0;
                const page = data.page ?? currentPage;
                const size = data.size ?? pageSize;

                renderTable(items, page, size, total);
                renderPagination(page, size, total);
            } catch (error) {
                console.error('Error fetching contracts:', error);
                contractTableBody.innerHTML = `
                    <tr>
                        <td colspan="9" style="text-align: center; padding: 40px; color: #dc2626;">
                            Có lỗi khi tải dữ liệu hợp đồng: ${escapeHtml(error.message)}
                        </td>
                    </tr>
                `;
            }
        }

        // ==============================================================
        // RENDER DỮ LIỆU BẢNG VỚI NÚT COPY MÃ NHANH
        // ==============================================================
        function renderTable(items, page, size, total) {
            totalItems = total;
            currentPage = page;

            if (!items || items.length === 0) {
                contractTableBody.innerHTML = `
                    <tr>
                        <td colspan="9" style="text-align: center; padding: 50px 20px; color: var(--slate-400);">
                            <i class="ph ph-magnifying-glass" style="font-size: 32px; display: block; margin-bottom: 8px;"></i>
                            Không tìm thấy hợp đồng nào phù hợp
                        </td>
                    </tr>
                `;
                paginationInfo.textContent = 'Đang xem 0 - 0 trong 0';
                paginationControls.innerHTML = '';
                return;
            }

            const startIndex = page * size;
            const startItem = startIndex + 1;
            const endItem = Math.min((page + 1) * size, total);
            paginationInfo.textContent = `Đang xem ${startItem} - ${endItem} trong ${total}`;

            contractTableBody.innerHTML = items.map((c, index) => {
                const stt = startIndex + index + 1;
                
                // Trạng thái badge
                let statusClass = 'status-hieu-luc';
                let statusIcon = 'ph-check-circle';
                if (c.status === 'CHUA_HIEU_LUC') {
                    statusClass = 'status-chua-hieu-luc';
                    statusIcon = 'ph-clock';
                } else if (c.status === 'HET_HIEU_LUC') {
                    statusClass = 'status-het-hieu-luc';
                    statusIcon = 'ph-hourglass-low';
                } else if (c.status === 'DA_HUY') {
                    statusClass = 'status-da-huy';
                    statusIcon = 'ph-x-circle';
                }

                // Người tạo & Thời gian tạo
                const creatorText = c.createdBy || 'Hệ thống';
                const createdDateText = formatDate(c.createdAt);

                // Người sửa & Thời gian sửa
                const updaterText = c.updatedBy || c.createdBy || 'Hệ thống';
                const updatedDateText = formatDate(c.updatedAt);

                return `
                    <tr>
                        <td style="color: var(--slate-400); font-weight: 500;">${stt}</td>
                        <td>
                            <div class="patient-code-wrapper">
                                <span class="code-badge">${escapeHtml(c.contractCode)}</span>
                                <button type="button" class="btn-copy-code" title="Sao chép nhanh mã hợp đồng" onclick="copyContractCode('${escapeHtml(c.contractCode)}', this, event)">
                                    <i class="ph ph-copy"></i>
                                </button>
                            </div>
                        </td>
                        <td>
                            <strong style="color: var(--slate-900); font-weight: 600;">${escapeHtml(c.contractName)}</strong>
                        </td>
                        <td>
                            <span class="company-code-badge">${escapeHtml(c.companyCode)}</span>
                        </td>
                        <td>
                            <span style="color: var(--slate-700); font-weight: 500;">${escapeHtml(c.companyName)}</span>
                        </td>
                        <td>
                            <span class="contract-status-badge ${statusClass}">
                                <i class="ph ${statusIcon}"></i>
                                <span>${escapeHtml(c.statusLabel)}</span>
                            </span>
                        </td>
                        <td>
                            <div style="font-size: 12.5px; color: var(--slate-700);">${escapeHtml(creatorText)}</div>
                            <div style="font-size: 11px; color: var(--slate-400);">${createdDateText}</div>
                        </td>
                        <td>
                            <div style="font-size: 12.5px; color: var(--slate-700);">${escapeHtml(updaterText)}</div>
                            <div style="font-size: 11px; color: var(--slate-400);">${updatedDateText}</div>
                        </td>
                        <td style="text-align: right; padding-right: 20px; white-space: nowrap;">
                            <div class="table-actions-dropdown">
                                <button type="button" class="btn-action-icon" title="Chi tiết hợp đồng" onclick="window.location.href='/admin/contracts/' + encodeURIComponent('${c.id}')">
                                    <i class="ph ph-gear"></i>
                                </button>
                                <button type="button" class="btn-action-icon" title="Chỉnh sửa thông tin" onclick="openEditModal('${c.id}')">
                                    <i class="ph ph-pencil-simple"></i>
                                </button>
                                <button type="button" class="btn-action-icon delete" title="Xóa hợp đồng" onclick="openDeleteModal('${c.id}', '${escapeHtml(c.contractCode)}', '${escapeHtml(c.contractName)}')">
                                    <i class="ph ph-trash"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');
        }

        // ==============================================================
        // TÍNH NĂNG SAO CHÉP NHANH MÃ HỢP ĐỒNG (COPY CLIPBOARD)
        // ==============================================================
        function copyContractCode(code, btnElement, event) {
            if (event) event.stopPropagation();
            if (!code) return;

            const copySuccess = () => {
                showToast(`Đã sao chép mã hợp đồng: ${code}`, 'success');
                if (btnElement) {
                    btnElement.classList.add('copied');
                    btnElement.innerHTML = '<i class="ph ph-check" style="color: #10b981;"></i>';
                    setTimeout(() => {
                        btnElement.classList.remove('copied');
                        btnElement.innerHTML = '<i class="ph ph-copy"></i>';
                    }, 1500);
                }
            };

            if (navigator.clipboard && window.isSecureContext) {
                navigator.clipboard.writeText(code).then(copySuccess).catch(() => fallbackCopy(code, copySuccess));
            } else {
                fallbackCopy(code, copySuccess);
            }
        }

        function fallbackCopy(text, callback) {
            const textArea = document.createElement("textarea");
            textArea.value = text;
            textArea.style.position = "fixed";
            textArea.style.top = "-9999px";
            textArea.style.left = "-9999px";
            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();
            try {
                const successful = document.execCommand('copy');
                if (successful && callback) callback();
            } catch (err) {
                console.error('Fallback copy error', err);
            }
            document.body.removeChild(textArea);
        }

        // ==============================================================
        // TỰ ĐỘNG SINH MÃ TIẾP THEO (GỌI API /api/contracts/next-code)
        // ==============================================================
        async function generateAutoCode(silent = false) {
            try {
                btnAutoCode.disabled = true;
                btnAutoCode.innerHTML = '<i class="ph ph-spinner ph-spin"></i> Đang lấy...';
                const response = await fetch('/api/contracts/next-code');
                if (response.ok) {
                    const data = await response.json();
                    if (data.nextCode) {
                        contractCodeInput.value = data.nextCode;
                        contractCodeError.style.display = 'none';
                        contractCodeInput.style.borderColor = '';
                        if (!silent) {
                            showToast(`Đã lấy mã tự động: ${data.nextCode}`, 'info');
                        }
                    }
                }
            } catch (err) {
                console.error('Lỗi khi lấy mã tự động', err);
            } finally {
                btnAutoCode.disabled = false;
                btnAutoCode.innerHTML = '<i class="ph ph-magic-wand"></i> Lấy mã tự động';
            }
        }

        // ==============================================================
        // RENDER PHÂN TRANG (PAGINATION)
        // ==============================================================
        function renderPagination(page, size, total) {
            const totalPages = Math.ceil(total / size) || 1;

            if (totalPages <= 1) {
                paginationControls.innerHTML = '';
                return;
            }

            let html = '';
            
            // Nút Prev
            html += `
                <button type="button" class="btn-page-nav" ${page === 0 ? 'disabled' : ''} onclick="goToPage(${page - 1})">
                    <i class="ph ph-caret-left"></i>
                </button>
            `;

            // Các nút trang
            for (let i = 0; i < totalPages; i++) {
                if (i === 0 || i === totalPages - 1 || (i >= page - 1 && i <= page + 1)) {
                    html += `
                        <button type="button" class="btn-page-number ${i === page ? 'active' : ''}" onclick="goToPage(${i})">
                            ${i + 1}
                        </button>
                    `;
                } else if (i === page - 2 || i === page + 2) {
                    html += `<span class="page-ellipsis">...</span>`;
                }
            }

            // Nút Next
            html += `
                <button type="button" class="btn-page-nav" ${page >= totalPages - 1 ? 'disabled' : ''} onclick="goToPage(${page + 1})">
                    <i class="ph ph-caret-right"></i>
                </button>
            `;

            paginationControls.innerHTML = html;
        }

        function goToPage(page) {
            currentPage = page;
            loadContracts();
        }

        // ==============================================================
        // MODAL THÊM / CẬP NHẬT HỢP ĐỒNG
        // ==============================================================
        function openAddModal() {
            isEditMode = false;
            contractForm.reset();
            contractIdInput.value = '';
            contractCodeInput.disabled = false;
            contractCodeInput.readOnly = false;
            btnAutoCode.style.display = 'inline-flex';
            modalTitle.textContent = 'Thêm mới hợp đồng';
            btnSaveContract.disabled = false;
            btnSaveContract.textContent = 'Thêm hợp đồng';
            contractCodeError.style.display = 'none';
            contractCodeInput.style.borderColor = '';
            contractStatusSelect.value = 'HIEU_LUC';
            
            // Hiển thị modal
            contractModal.classList.add('show');
            contractModal.classList.add('active');

            // Tự động gợi ý mã tiếp theo ngầm
            generateAutoCode(true);

            setTimeout(() => {
                contractNameInput.focus();
            }, 100);
        }

        async function openEditModal(id) {
            isEditMode = true;
            contractForm.reset();
            contractCodeError.style.display = 'none';
            contractCodeInput.style.borderColor = '';
            contractCodeInput.disabled = false;
            contractCodeInput.readOnly = false;
            btnAutoCode.style.display = 'none';

            try {
                const response = await fetch(`/api/contracts/${id}`);
                if (!response.ok) throw new Error('Không thể tải thông tin hợp đồng');

                const data = await response.json();
                contractIdInput.value = data.id;
                contractCodeInput.value = data.contractCode;
                contractNameInput.value = data.contractName;
                companyCodeInput.value = data.companyCode;
                companyNameInput.value = data.companyName;
                contractStatusSelect.value = data.status;

                modalTitle.textContent = 'Chỉnh sửa thông tin hợp đồng';
                btnSaveContract.disabled = false;
                btnSaveContract.textContent = 'Lưu thay đổi';

                contractModal.classList.add('show');
                contractModal.classList.add('active');
                setTimeout(() => {
                    contractNameInput.focus();
                }, 100);
            } catch (err) {
                console.error(err);
                showToast('Lỗi khi tải thông tin chi tiết hợp đồng', 'error');
            }
        }

        function closeContractModal() {
            contractModal.classList.remove('show');
            contractModal.classList.remove('active');
        }

        // ==============================================================
        // SUBMIT FORM (CHECK TRÙNG MÃ VÀ CALL API)
        // ==============================================================
        async function handleFormSubmit(e) {
            e.preventDefault();

            const id = contractIdInput.value;
            const contractCode = contractCodeInput.value.trim().toUpperCase();
            const contractName = contractNameInput.value.trim();
            const companyCode = companyCodeInput.value.trim().toUpperCase();
            const companyName = companyNameInput.value.trim();
            const status = contractStatusSelect.value;

            if (!contractCode) {
                showToast('Vui lòng nhập mã hợp đồng', 'error');
                contractCodeInput.focus();
                return;
            }

            if (!contractName) {
                showToast('Vui lòng nhập tên hợp đồng', 'error');
                contractNameInput.focus();
                return;
            }

            if (!companyCode) {
                showToast('Vui lòng nhập mã công ty', 'error');
                companyCodeInput.focus();
                return;
            }

            if (!companyName) {
                showToast('Vui lòng nhập tên công ty', 'error');
                companyNameInput.focus();
                return;
            }

            const payload = {
                contractCode,
                contractName,
                companyCode,
                companyName,
                status
            };

            const isEdit = !!id;
            const url = isEdit ? `/api/contracts/${id}` : '/api/contracts';
            const method = isEdit ? 'PUT' : 'POST';

            btnSaveContract.disabled = true;
            btnSaveContract.textContent = isEdit ? 'Đang lưu...' : 'Đang thêm...';

            try {
                const response = await fetch(url, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                const result = await response.json();

                if (!response.ok) {
                    const errorMsg = result.message || 'Có lỗi xảy ra khi lưu hợp đồng';
                    if (errorMsg.includes('trùng') || errorMsg.includes('tồn tại') || response.status === 400) {
                        contractCodeError.textContent = errorMsg;
                        contractCodeError.style.display = 'block';
                        contractCodeInput.style.borderColor = '#dc2626';
                        contractCodeInput.focus();
                    }
                    showToast(errorMsg, 'error');
                    return;
                }

                showToast(isEdit ? 'Cập nhật hợp đồng thành công!' : 'Thêm mới hợp đồng thành công!', 'success');
                closeContractModal();
                loadContracts();
            } catch (err) {
                console.error('Error submitting form:', err);
                showToast('Không thể kết nối đến máy chủ. Vui lòng thử lại!', 'error');
            } finally {
                btnSaveContract.disabled = false;
                btnSaveContract.textContent = isEdit ? 'Lưu thay đổi' : 'Thêm hợp đồng';
            }
        }

        // ==============================================================
        // XÓA MỀM HỢP ĐỒNG
        // ==============================================================
        function openDeleteModal(id, code, name) {
            deleteTargetId = id;
            deleteContractCode.textContent = code;
            deleteContractName.textContent = name;
            deleteConfirmModal.classList.add('show');
            deleteConfirmModal.classList.add('active');
        }

        function closeDeleteModal() {
            deleteConfirmModal.classList.remove('show');
            deleteConfirmModal.classList.remove('active');
            deleteTargetId = null;
        }

        async function executeDelete() {
            if (!deleteTargetId) return;

            btnConfirmDelete.disabled = true;
            btnConfirmDelete.textContent = 'Đang xóa...';

            try {
                const response = await fetch(`/api/contracts/${deleteTargetId}`, {
                    method: 'DELETE'
                });

                if (!response.ok) {
                    const result = await response.json().catch(() => ({}));
                    throw new Error(result.message || 'Không thể xóa hợp đồng');
                }

                showToast('Đã xóa mềm hợp đồng thành công!', 'success');
                closeDeleteModal();
                loadContracts();
            } catch (err) {
                console.error('Error deleting contract:', err);
                showToast(err.message || 'Lỗi khi xóa hợp đồng', 'error');
            } finally {
                btnConfirmDelete.disabled = false;
                btnConfirmDelete.textContent = 'Xác nhận xóa';
            }
        }

        // ==============================================================
        // TIỆN ÍCH TOAST NOTIFICATION & ESCAPE HTML & FORMAT DATE
        // ==============================================================
        function formatDate(dateStr) {
            if (!dateStr) return '-';
            try {
                const d = new Date(dateStr);
                const dd = String(d.getDate()).padStart(2, '0');
                const mm = String(d.getMonth() + 1).padStart(2, '0');
                const yyyy = d.getFullYear();
                const hh = String(d.getHours()).padStart(2, '0');
                const min = String(d.getMinutes()).padStart(2, '0');
                return `${dd}/${mm}/${yyyy} ${hh}:${min}`;
            } catch (e) {
                return dateStr;
            }
        }

        function showToast(message, type = 'info') {
            const container = document.getElementById('toastContainer');
            const toast = document.createElement('div');
            toast.className = `toast toast-${type}`;

            let icon = 'ph-info';
            if (type === 'success') icon = 'ph-check-circle';
            if (type === 'error') icon = 'ph-x-circle';

            toast.innerHTML = `
                <i class="ph-bold ${icon}" style="font-size: 18px;"></i>
                <span>${escapeHtml(message)}</span>
            `;

            container.appendChild(toast);

            setTimeout(() => {
                toast.classList.add('fade-out');
                setTimeout(() => toast.remove(), 300);
            }, 3000);
        }

        function escapeHtml(text) {
            if (!text) return '';
            return String(text)
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        }
