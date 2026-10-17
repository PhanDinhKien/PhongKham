/**
 * Patients Management Script
 */
        // State
        let currentPage = 0;
        let pageSize = 20;
        let totalItems = 0;
        let deleteTargetId = null;
        let searchTimeout = null;
        let isEditMode = false;

        // DOM Elements
        const patientTableBody = document.getElementById('patientTableBody');
        const paginationInfo = document.getElementById('paginationInfo');
        const paginationControls = document.getElementById('paginationControls');
        const pageSizeSelect = document.getElementById('pageSizeSelect');
        const searchInput = document.getElementById('searchInput');
        const genderFilter = document.getElementById('genderFilter');
        const btnClearFilter = document.getElementById('btnClearFilter');

        // Modal Elements
        const patientModal = document.getElementById('patientModal');
        const btnOpenAddModal = document.getElementById('btnOpenAddModal');
        const btnCloseModal = document.getElementById('btnCloseModal');
        const btnCancelModal = document.getElementById('btnCancelModal');
        const patientForm = document.getElementById('patientForm');
        const modalTitle = document.getElementById('modalTitle');
        const patientIdInput = document.getElementById('patientId');
        const patientCodeInput = document.getElementById('patientCode');
        const patientCodeError = document.getElementById('patientCodeError');
        const btnAutoCode = document.getElementById('btnAutoCode');
        const fullNameInput = document.getElementById('fullName');
        const birthYearInput = document.getElementById('birthYear');
        const agePreview = document.getElementById('agePreview');
        const genderSelect = document.getElementById('gender');
        const phoneInput = document.getElementById('phone');
        const addressInput = document.getElementById('address');
        const noteInput = document.getElementById('note');
        const btnSavePatient = document.getElementById('btnSavePatient');

        // Delete Modal Elements
        const deleteConfirmModal = document.getElementById('deleteConfirmModal');
        const deletePatientName = document.getElementById('deletePatientName');
        const deletePatientCode = document.getElementById('deletePatientCode');
        const btnCloseDeleteModal = document.getElementById('btnCloseDeleteModal');
        const btnCancelDeleteModal = document.getElementById('btnCancelDeleteModal');
        const btnConfirmDelete = document.getElementById('btnConfirmDelete');

        // Khởi động
        document.addEventListener('DOMContentLoaded', () => {
            loadPatients();
            setupEventListeners();
        });

        function setupEventListeners() {
            btnOpenAddModal.addEventListener('click', openAddModal);
            btnCloseModal.addEventListener('click', closePatientModal);
            btnCancelModal.addEventListener('click', closePatientModal);

            btnCloseDeleteModal.addEventListener('click', closeDeleteModal);
            btnCancelDeleteModal.addEventListener('click', closeDeleteModal);
            btnConfirmDelete.addEventListener('click', executeDelete);

            btnAutoCode.addEventListener('click', generateAutoCode);

            patientForm.addEventListener('submit', handleFormSubmit);

            // Tính tuổi tự động khi gõ năm sinh
            birthYearInput.addEventListener('input', () => {
                const year = parseInt(birthYearInput.value);
                const currentYear = new Date().getFullYear();
                if (year && year >= 1900 && year <= currentYear) {
                    const age = currentYear - year;
                    agePreview.textContent = `Năm nay khoảng ${age} tuổi`;
                } else {
                    agePreview.textContent = '';
                }
            });

            // Reset lỗi trùng mã khi người dùng chỉnh sửa mã người bệnh
            patientCodeInput.addEventListener('input', () => {
                patientCodeError.style.display = 'none';
                patientCodeInput.style.borderColor = '';
            });

            // Tìm kiếm với debounce
            searchInput.addEventListener('input', () => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    currentPage = 0;
                    loadPatients();
                }, 350);
            });

            // Lọc giới tính
            genderFilter.addEventListener('change', () => {
                currentPage = 0;
                loadPatients();
            });

            // Xóa bộ lọc
            btnClearFilter.addEventListener('click', () => {
                searchInput.value = '';
                genderFilter.value = '';
                currentPage = 0;
                loadPatients();
            });

            // Đổi kích thước trang
            pageSizeSelect.addEventListener('change', () => {
                pageSize = parseInt(pageSizeSelect.value, 10);
                currentPage = 0;
                loadPatients();
            });

            // Đóng modal khi bấm ra ngoài
            window.addEventListener('click', (e) => {
                if (e.target === patientModal) closePatientModal();
                if (e.target === deleteConfirmModal) closeDeleteModal();
            });
        }

        // ==============================================================
        // TẢI DANH SÁCH NGƯỜI BỆNH
        // ==============================================================
        async function loadPatients() {
            const keyword = searchInput.value.trim();
            const gender = genderFilter.value;

            let url = `/api/patients/search?page=${currentPage}&size=${pageSize}`;
            if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
            if (gender) url += `&gender=${encodeURIComponent(gender)}`;

            try {
                const response = await fetch(url);
                if (response.status === 401) {
                    window.location.href = '/login';
                    return;
                }
                if (!response.ok) throw new Error('Không thể tải danh sách người bệnh');

                const data = await response.json();
                const items = data.items || data.content || [];
                const total = data.total ?? data.totalElements ?? 0;
                const page = data.page ?? currentPage;
                const size = data.size ?? pageSize;

                renderTable(items, page, size, total);
                renderPagination(page, size, total);
            } catch (error) {
                console.error('Error fetching patients:', error);
                patientTableBody.innerHTML = `
                    <tr>
                        <td colspan="10" style="text-align: center; padding: 40px; color: #dc2626;">
                            Có lỗi khi tải dữ liệu người bệnh. Vui lòng thử lại sau!
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
                patientTableBody.innerHTML = `
                    <tr>
                        <td colspan="10" style="text-align: center; padding: 50px 20px; color: var(--slate-400);">
                            <i class="ph ph-magnifying-glass" style="font-size: 32px; display: block; margin-bottom: 8px;"></i>
                            Không tìm thấy người bệnh nào phù hợp
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

            patientTableBody.innerHTML = items.map((p, index) => {
                const stt = startIndex + index + 1;
                
                // Giới tính badge
                let genderClass = 'gender-khac';
                let genderLabel = p.genderLabel || (p.gender === 'NAM' ? 'Nam' : (p.gender === 'NU' ? 'Nữ' : 'Khác'));
                let genderIcon = 'ph-gender-neuter';

                if (p.gender === 'NAM') {
                    genderClass = 'gender-nam';
                    genderLabel = 'Nam';
                    genderIcon = 'ph-gender-male';
                } else if (p.gender === 'NU') {
                    genderClass = 'gender-nu';
                    genderLabel = 'Nữ';
                    genderIcon = 'ph-gender-female';
                }

                // Tuổi
                const ageText = p.age != null ? `${p.age} tuổi` : '';

                // Ghi chú
                const noteHtml = p.note ? `<span class="patient-note-cell" title="${escapeHtml(p.note)}">${escapeHtml(p.note)}</span>` : '<span style="color: var(--slate-400); font-style: italic;">Không</span>';

                // Người sửa gần nhất
                const updaterName = p.updatedBy || p.createdBy || 'Hệ thống';

                return `
                    <tr>
                        <td style="color: var(--slate-400); font-weight: 500;">${stt}</td>
                        <td>
                            <div class="patient-code-wrapper">
                                <span class="code-badge">${escapeHtml(p.patientCode)}</span>
                                <button type="button" class="btn-copy-code" title="Sao chép nhanh mã người bệnh" onclick="copyPatientCode('${escapeHtml(p.patientCode)}', this, event)">
                                    <i class="ph ph-copy"></i>
                                </button>
                            </div>
                        </td>
                        <td>
                            <strong style="color: var(--slate-900); font-weight: 600;">${escapeHtml(p.fullName)}</strong>
                        </td>
                        <td>
                            <span class="birth-year-text">${p.birthYear}</span>
                            ${ageText ? `<span class="age-badge">${ageText}</span>` : ''}
                        </td>
                        <td>
                            <span class="gender-badge ${genderClass}">
                                <i class="ph ${genderIcon}"></i>
                                <span>${genderLabel}</span>
                            </span>
                        </td>
                        <td>
                            <a href="tel:${escapeHtml(p.phone)}" class="phone-link" title="Bấm để gọi">
                                <i class="ph ph-phone"></i>
                                <span>${escapeHtml(p.phone)}</span>
                            </a>
                        </td>
                        <td>
                            <div style="max-width: 240px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="${escapeHtml(p.address)}">
                                ${escapeHtml(p.address)}
                            </div>
                        </td>
                        <td>
                            ${noteHtml}
                        </td>
                        <td>
                            <span style="font-size: 12px; color: var(--slate-500);">${escapeHtml(updaterName)}</span>
                        </td>
                        <td style="text-align: right; padding-right: 20px; white-space: nowrap;">
                            <div class="table-actions-dropdown">
                                <button type="button" class="btn-action-icon" title="Chỉnh sửa thông tin" onclick="openEditModal('${p.id}')">
                                    <i class="ph ph-pencil-simple"></i>
                                </button>
                                <button type="button" class="btn-action-icon delete" title="Xóa người bệnh" onclick="openDeleteModal('${p.id}', '${escapeHtml(p.patientCode)}', '${escapeHtml(p.fullName)}')">
                                    <i class="ph ph-trash"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');
        }

        // ==============================================================
        // TÍNH NĂNG SAO CHÉP NHANH MÃ BỆNH NHÂN (COPY CLIPBOARD)
        // ==============================================================
        function copyPatientCode(code, btnElement, event) {
            if (event) event.stopPropagation();
            if (!code) return;

            const copySuccess = () => {
                showToast(`Đã sao chép mã người bệnh: ${code}`, 'success');
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
        // TỰ ĐỘNG SINH MÃ TIẾP THEO (GỌI API /api/patients/next-code)
        // ==============================================================
        async function generateAutoCode(silent = false) {
            try {
                btnAutoCode.disabled = true;
                btnAutoCode.innerHTML = '<i class="ph ph-spinner ph-spin"></i> Đang lấy...';
                const response = await fetch('/api/patients/next-code');
                if (response.ok) {
                    const data = await response.json();
                    if (data.nextCode) {
                        patientCodeInput.value = data.nextCode;
                        patientCodeError.style.display = 'none';
                        patientCodeInput.style.borderColor = '';
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
            loadPatients();
        }

        // ==============================================================
        // MODAL THÊM / CẬP NHẬT NGƯỜI BỆNH
        // ==============================================================
        function openAddModal() {
            isEditMode = false;
            patientForm.reset();
            patientIdInput.value = '';
            patientCodeInput.disabled = false;
            patientCodeInput.readOnly = false;
            btnAutoCode.style.display = 'inline-flex';
            modalTitle.textContent = 'Thêm mới người bệnh';
            btnSavePatient.disabled = false;
            btnSavePatient.textContent = 'Thêm người bệnh';
            patientCodeError.style.display = 'none';
            patientCodeInput.style.borderColor = '';
            agePreview.textContent = '';
            
            // Hiển thị modal (sử dụng cả show và active)
            patientModal.classList.add('show');
            patientModal.classList.add('active');

            // Tự động gợi ý mã tiếp theo (chế độ yên lặng, không pop toast làm phiền)
            generateAutoCode(true);

            setTimeout(() => {
                fullNameInput.focus();
            }, 100);
        }

        async function openEditModal(id) {
            isEditMode = true;
            patientForm.reset();
            patientCodeError.style.display = 'none';
            patientCodeInput.style.borderColor = '';
            patientCodeInput.disabled = false;
            patientCodeInput.readOnly = false;
            btnAutoCode.style.display = 'none';

            try {
                const response = await fetch(`/api/patients/${id}`);
                if (!response.ok) throw new Error('Không thể tải thông tin người bệnh');

                const data = await response.json();
                patientIdInput.value = data.id;
                patientCodeInput.value = data.patientCode;
                fullNameInput.value = data.fullName;
                birthYearInput.value = data.birthYear;
                genderSelect.value = data.gender;
                phoneInput.value = data.phone;
                addressInput.value = data.address;
                noteInput.value = data.note || '';

                const currentYear = new Date().getFullYear();
                if (data.birthYear) {
                    agePreview.textContent = `Năm nay khoảng ${currentYear - data.birthYear} tuổi`;
                }

                modalTitle.textContent = 'Chỉnh sửa hồ sơ người bệnh';
                btnSavePatient.disabled = false;
                btnSavePatient.textContent = 'Lưu thay đổi';

                patientModal.classList.add('show');
                patientModal.classList.add('active');
                setTimeout(() => {
                    fullNameInput.focus();
                }, 100);
            } catch (err) {
                console.error(err);
                showToast('Lỗi khi tải thông tin chi tiết người bệnh', 'error');
            }
        }

        function closePatientModal() {
            patientModal.classList.remove('show');
            patientModal.classList.remove('active');
        }

        // ==============================================================
        // SUBMIT FORM (CHECK TRÙNG MÃ VÀ CALL API)
        // ==============================================================
        async function handleFormSubmit(e) {
            e.preventDefault();

            const id = patientIdInput.value;
            const patientCode = patientCodeInput.value.trim().toUpperCase();
            const fullName = fullNameInput.value.trim();
            const birthYear = parseInt(birthYearInput.value, 10);
            const gender = genderSelect.value;
            const phone = phoneInput.value.trim();
            const address = addressInput.value.trim();
            const note = noteInput.value.trim();

            if (!patientCode) {
                showToast('Vui lòng nhập mã người bệnh', 'error');
                patientCodeInput.focus();
                return;
            }

            if (!fullName) {
                showToast('Vui lòng nhập họ và tên người bệnh', 'error');
                fullNameInput.focus();
                return;
            }

            if (!birthYear || birthYear < 1900 || birthYear > new Date().getFullYear()) {
                showToast('Năm sinh không hợp lệ (từ 1900 đến nay)', 'error');
                birthYearInput.focus();
                return;
            }

            const payload = {
                patientCode,
                fullName,
                birthYear,
                gender,
                phone,
                address,
                note
            };

            const isEdit = !!id;
            const url = isEdit ? `/api/patients/${id}` : '/api/patients';
            const method = isEdit ? 'PUT' : 'POST';

            btnSavePatient.disabled = true;
            btnSavePatient.textContent = isEdit ? 'Đang lưu...' : 'Đang thêm...';

            try {
                const response = await fetch(url, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                const result = await response.json();

                if (!response.ok) {
                    // Check nếu là lỗi trùng mã
                    const errorMsg = result.message || 'Có lỗi xảy ra khi lưu người bệnh';
                    if (errorMsg.includes('trùng') || errorMsg.includes('tồn tại') || response.status === 400) {
                        patientCodeError.textContent = errorMsg;
                        patientCodeError.style.display = 'block';
                        patientCodeInput.style.borderColor = '#dc2626';
                        patientCodeInput.focus();
                    }
                    showToast(errorMsg, 'error');
                    return;
                }

                showToast(isEdit ? 'Cập nhật hồ sơ thành công!' : 'Thêm mới người bệnh thành công!', 'success');
                closePatientModal();
                loadPatients();
            } catch (err) {
                console.error('Error submitting form:', err);
                showToast('Không thể kết nối đến máy chủ. Vui lòng thử lại!', 'error');
            } finally {
                btnSavePatient.disabled = false;
                btnSavePatient.textContent = isEdit ? 'Lưu thay đổi' : 'Thêm người bệnh';
            }
        }

        // ==============================================================
        // XÓA MỀM NGƯỜI BỆNH
        // ==============================================================
        function openDeleteModal(id, code, name) {
            deleteTargetId = id;
            deletePatientCode.textContent = code;
            deletePatientName.textContent = name;
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
                const response = await fetch(`/api/patients/${deleteTargetId}`, {
                    method: 'DELETE'
                });

                if (!response.ok) {
                    const result = await response.json().catch(() => ({}));
                    throw new Error(result.message || 'Không thể xóa người bệnh');
                }

                showToast('Đã xóa mềm hồ sơ người bệnh thành công!', 'success');
                closeDeleteModal();
                loadPatients();
            } catch (err) {
                console.error('Error deleting patient:', err);
                showToast(err.message || 'Lỗi khi xóa người bệnh', 'error');
            } finally {
                btnConfirmDelete.disabled = false;
                btnConfirmDelete.textContent = 'Xác nhận xóa';
            }
        }

        // ==============================================================
        // TIỆN ÍCH TOAST NOTIFICATION & ESCAPE HTML
        // ==============================================================
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
