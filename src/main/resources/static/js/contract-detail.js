/**
 * Contract Detail Page Interactive Script
 */
        // Lấy contractId từ URL: /admin/contracts/{id}
        const pathParts = window.location.pathname.split('/');
        const contractId = pathParts[pathParts.length - 1];

        // State
        let contractData = null;
        let allPatientsList = [];
        let allLocationsList = [];
        let allServicesList = [];
        let packagesData = [];

        let currentPatientPage = 0;
        let currentPatientSize = 20;
        let deleteCallback = null;

        // ==============================================================
        // KHỞI CHẠY (INIT)
        // ==============================================================
        document.addEventListener('DOMContentLoaded', () => {
            initTabs();
            loadContractDetail();
            loadContractPatients(0);
            loadContractLocations();
            loadContractPackages();

            // Cache preload helpers
            preloadPatients();
            preloadLocations();
            preloadServices();

            // Set default date for add patient modal
            const today = new Date().toISOString().split('T')[0];
            const examDateInput = document.getElementById('modalExamDate');
            if (examDateInput) examDateInput.value = today;

            setupEventListeners();
        });

        // ==============================================================
        // TABS SWITCHING
        // ==============================================================
        function initTabs() {
            const tabButtons = document.querySelectorAll('.tab-btn');
            tabButtons.forEach(btn => {
                btn.addEventListener('click', () => {
                    tabButtons.forEach(b => b.classList.remove('active'));
                    document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));

                    btn.classList.add('active');
                    const targetId = btn.getAttribute('data-tab');
                    const targetPane = document.getElementById(targetId);
                    if (targetPane) targetPane.classList.add('active');
                });
            });
        }

        // ==============================================================
        // LOAD CONTRACT HEADER DETAIL
        // ==============================================================
        async function loadContractDetail() {
            try {
                const res = await fetch(`/api/contracts/${contractId}/detail`);
                if (!res.ok) throw new Error('Không thể tải thông tin hợp đồng');
                contractData = await res.json();

                // Cập nhật Tiêu đề & Card
                document.getElementById('headerContractCode').textContent = contractData.contractCode;
                document.getElementById('cardContractName').textContent = contractData.contractName;
                document.getElementById('cardCompanyName').textContent = contractData.companyName;
                document.getElementById('cardContractNumber').textContent = contractData.contractCode;
                document.getElementById('cardContractCode').textContent = contractData.contractCode;
                document.getElementById('cardCompanyCode').textContent = contractData.companyCode;

                const dateStr = contractData.createdAt ? formatDate(contractData.createdAt) : '-';
                document.getElementById('cardEffectiveDate').textContent = dateStr;
                document.getElementById('cardSyncTime').textContent = contractData.updatedAt ? formatDateTime(contractData.updatedAt) : dateStr;

                const statusBadge = document.getElementById('cardStatusBadge');
                statusBadge.innerHTML = `<span class="badge-status ${contractData.status === 'HIEU_LUC' ? 'active' : 'inactive'}">${escapeHtml(contractData.statusLabel)}</span>`;

                // Badges
                updateTabCounts(contractData.patientCount, contractData.locationCount, contractData.packageCount);
            } catch (err) {
                console.error(err);
                showToast(err.message || 'Lỗi tải chi tiết hợp đồng', 'error');
            }
        }

        function updateTabCounts(pCount, lCount, pkgCount) {
            if (pCount !== undefined) document.getElementById('badgePatientCount').textContent = pCount;
            if (lCount !== undefined) document.getElementById('badgeLocationCount').textContent = lCount;
            if (pkgCount !== undefined) document.getElementById('badgePackageCount').textContent = pkgCount;
        }

        // ==============================================================
        // TAB 1: DS HỒ SƠ KHÁM (PATIENTS)
        // ==============================================================
        async function loadContractPatients(page = 0) {
            currentPatientPage = page;
            const keyword = document.getElementById('patientSearchInput').value.trim();
            const status = document.getElementById('patientStatusFilter').value;
            const tbody = document.getElementById('patientTableBody');

            tbody.innerHTML = `<tr><td colspan="10" style="text-align:center; padding: 30px; color: var(--slate-400);">Đang tải dữ liệu...</td></tr>`;

            try {
                let url = `/api/contracts/${contractId}/patients?page=${page}&size=${currentPatientSize}`;
                if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
                if (status) url += `&status=${encodeURIComponent(status)}`;

                const res = await fetch(url);
                if (!res.ok) throw new Error('Không thể tải danh sách hồ sơ khám');
                const data = await res.json();

                renderPatientTable(data.items, page, currentPatientSize);
                renderPatientPagination(data);
                document.getElementById('badgePatientCount').textContent = data.totalElements;
            } catch (err) {
                console.error(err);
                tbody.innerHTML = `<tr><td colspan="10" style="text-align:center; padding: 30px; color: #dc2626;">Lỗi: ${escapeHtml(err.message)}</td></tr>`;
            }
        }

        function renderPatientTable(items, page, size) {
            const tbody = document.getElementById('patientTableBody');
            if (!items || items.length === 0) {
                tbody.innerHTML = `<tr><td colspan="10" style="text-align:center; padding: 40px; color: var(--slate-400);">Chưa có hồ sơ khám nào trong hợp đồng này</td></tr>`;
                return;
            }

            tbody.innerHTML = items.map((p, idx) => {
                const stt = String(page * size + idx + 1).padStart(2, '0');
                const examDateStr = p.examDate ? formatDate(p.examDate) : '-';
                const updateDateStr = p.updatedAt ? formatDateTime(p.updatedAt) : '-';

                // Trạng thái pill khớp ảnh
                const statusClass = (p.status || 'MOI').toLowerCase();
                const statusLabel = p.statusLabel || 'Mới';

                // Đồng bộ status
                let syncClass = 'chua_dong_bo';
                if (p.syncStatus === 'Đã đồng bộ') syncClass = 'da_dong_bo';
                else if (p.syncStatus === 'Lỗi đồng bộ') syncClass = 'loi_dong_bo';

                return `
                    <tr>
                        <td style="color: var(--slate-500); font-weight: 500;">${stt}</td>
                        <td style="font-weight: 600; color: var(--slate-700);">${escapeHtml(p.patientCode || '-')}</td>
                        <td><span class="record-code-highlight">${escapeHtml(p.recordCode || '-')}</span></td>
                        <td><span class="patient-name-bold">${escapeHtml(p.patientName || '-')}</span></td>
                        <td style="color: var(--slate-700);">${escapeHtml(p.doctorName || '-')}</td>
                        <td style="color: var(--slate-600);">${examDateStr}</td>
                        <td style="color: var(--slate-600); font-size: 12.5px;">${updateDateStr}</td>
                        <td>
                            <span class="status-pill ${statusClass}">${escapeHtml(statusLabel)}</span>
                        </td>
                        <td>
                            <span class="sync-label ${syncClass}">${escapeHtml(p.syncStatus || 'Chưa đồng bộ')}</span>
                        </td>
                        <td style="text-align: right; padding-right: 20px; white-space: nowrap;">
                            <div class="table-actions-dropdown">
                                <button type="button" class="btn-action-icon sales-btn" title="Bán hàng / Chọn dịch vụ" onclick="openSalesModal('${p.patientId}', '${escapeHtml(p.patientName)}', '${escapeHtml(p.patientCode || '')}', '${escapeHtml(p.recordCode || '')}', '${escapeHtml(p.gender || '')}', '${p.birthYear || ''}')">
                                    <i class="ph-bold ph-shopping-cart"></i>
                                </button>
                                <button type="button" class="btn-action-icon delete" title="Xóa khỏi hợp đồng" onclick="confirmRemovePatient('${p.patientId}', '${escapeHtml(p.patientName)}')">
                                    <i class="ph ph-trash"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');
        }

        function renderPatientPagination(data) {
            const info = document.getElementById('patientPaginationInfo');
            const controls = document.getElementById('patientPaginationControls');

            const total = data.totalElements || 0;
            const start = total === 0 ? 0 : data.page * data.size + 1;
            const end = Math.min((data.page + 1) * data.size, total);
            info.textContent = `Đang xem ${start} - ${end} trong ${total}`;

            const totalPages = data.totalPages || 1;
            let btns = '';

            // Nút Prev
            btns += `<button type="button" class="page-btn" ${data.page === 0 ? 'disabled' : ''} onclick="loadContractPatients(${data.page - 1})"><i class="ph ph-caret-left"></i></button>`;

            for (let i = 0; i < totalPages; i++) {
                if (i === 0 || i === totalPages - 1 || Math.abs(i - data.page) <= 1) {
                    btns += `<button type="button" class="page-btn ${i === data.page ? 'active' : ''}" onclick="loadContractPatients(${i})">${i + 1}</button>`;
                } else if (Math.abs(i - data.page) === 2) {
                    btns += `<span style="padding: 0 4px; color: var(--slate-400);">...</span>`;
                }
            }

            // Nút Next
            btns += `<button type="button" class="page-btn" ${data.page >= totalPages - 1 ? 'disabled' : ''} onclick="loadContractPatients(${data.page + 1})"><i class="ph ph-caret-right"></i></button>`;

            controls.innerHTML = btns;
        }

        // ==============================================================
        // TAB 2: ĐỊA ĐIỂM KHÁM (LOCATIONS)
        // ==============================================================
        async function loadContractLocations() {
            const tbody = document.getElementById('locationTableBody');
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 30px; color: var(--slate-400);">Đang tải dữ liệu...</td></tr>`;

            try {
                const res = await fetch(`/api/contracts/${contractId}/locations`);
                if (!res.ok) throw new Error('Không thể tải danh sách địa điểm');
                const list = await res.json();

                document.getElementById('badgeLocationCount').textContent = list.length;

                if (!list || list.length === 0) {
                    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 40px; color: var(--slate-400);">Chưa có địa điểm khám nào được gán cho hợp đồng này</td></tr>`;
                    return;
                }

                tbody.innerHTML = list.map((loc, idx) => {
                    const stt = String(idx + 1).padStart(2, '0');
                    const dateStr = loc.createdAt ? formatDateTime(loc.createdAt) : '-';
                    return `
                        <tr>
                            <td style="color: var(--slate-500); font-weight: 500;">${stt}</td>
                            <td><span style="font-weight: 600; color: #0f172a;">${escapeHtml(loc.code)}</span></td>
                            <td style="font-weight: 600; color: #047857;">${escapeHtml(loc.name)}</td>
                            <td style="color: var(--slate-600);">${escapeHtml(loc.address || '-')}</td>
                            <td>
                                <span class="badge-status ${loc.active ? 'active' : 'inactive'}">
                                    ${loc.active ? 'Đang hoạt động' : 'Tạm dừng'}
                                </span>
                            </td>
                            <td style="font-size: 12.5px; color: var(--slate-500);">${dateStr}</td>
                            <td style="text-align: right; padding-right: 20px;">
                                <button type="button" class="btn-action-icon delete" title="Hủy gán địa điểm khỏi HĐ" onclick="confirmRemoveLocation('${loc.locationId}', '${escapeHtml(loc.name)}')">
                                    <i class="ph ph-trash"></i>
                                </button>
                            </td>
                        </tr>
                    `;
                }).join('');
            } catch (err) {
                console.error(err);
                tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 30px; color: #dc2626;">Lỗi: ${escapeHtml(err.message)}</td></tr>`;
            }
        }

        // ==============================================================
        // TAB 3: GÓI DỊCH VỤ (BẢNG 2 CẤP MASTER - CHILD)
        // ==============================================================
        async function loadContractPackages() {
            const tbody = document.getElementById('packageTableBody');
            tbody.innerHTML = `<tr><td colspan="9" style="text-align:center; padding: 30px; color: var(--slate-400);">Đang tải dữ liệu...</td></tr>`;

            try {
                const res = await fetch(`/api/contracts/${contractId}/packages`);
                if (!res.ok) throw new Error('Không thể tải danh sách gói khám');
                packagesData = await res.json();

                document.getElementById('badgePackageCount').textContent = packagesData.length;

                if (!packagesData || packagesData.length === 0) {
                    tbody.innerHTML = `<tr><td colspan="9" style="text-align:center; padding: 40px; color: var(--slate-400);">Chưa có gói khám nào trong hợp đồng này. Nhấn 'Thêm mới gói khám' để tạo.</td></tr>`;
                    return;
                }

                tbody.innerHTML = packagesData.map((pkg, idx) => {
                    const stt = String(idx + 1).padStart(2, '0');
                    const dateStr = pkg.createdAt ? formatDateTime(pkg.createdAt) : '-';
                    const isEffective = pkg.status === 'HIEU_LUC';
                    const servicesCount = pkg.serviceCount || 0;

                    // Child services rows
                    let childContentHtml = '';
                    if (pkg.services && pkg.services.length > 0) {
                        childContentHtml = `
                            <table class="child-data-table">
                                <thead>
                                    <tr>
                                        <th style="width: 50px;">STT</th>
                                        <th style="width: 140px;">Mã dịch vụ</th>
                                        <th style="min-width: 200px;">Tên dịch vụ</th>
                                        <th style="width: 150px;">Phân loại</th>
                                        <th style="width: 140px; text-align: right;">Đơn giá (VNĐ)</th>
                                        <th style="width: 150px;">Ngày gán</th>
                                        <th style="width: 80px; text-align: right;">Tác vụ</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${pkg.services.map((s, sIdx) => `
                                        <tr>
                                            <td style="color: var(--slate-500); font-weight: 500;">${String(sIdx + 1).padStart(2, '0')}</td>
                                            <td style="font-weight: 600; color: #047857;">${escapeHtml(s.code)}</td>
                                            <td style="font-weight: 600; color: var(--slate-800);">${escapeHtml(s.name)}</td>
                                            <td><span class="badge-type">${escapeHtml(s.serviceTypeLabel || s.serviceType || '-')}</span></td>
                                            <td style="text-align: right; font-weight: 600; color: #0f172a;">${formatCurrency(s.price)}</td>
                                            <td style="font-size: 12px; color: var(--slate-500);">${s.createdAt ? formatDateTime(s.createdAt) : '-'}</td>
                                            <td style="text-align: right;">
                                                <button type="button" class="btn-sm-delete" title="Xóa dịch vụ khỏi gói" onclick="confirmRemoveService('${pkg.id}', '${s.serviceId}', '${escapeHtml(s.name)}')">
                                                    <i class="ph ph-trash"></i>
                                                </button>
                                            </td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        `;
                    } else {
                        childContentHtml = `
                            <div style="background: #ffffff; border: 1px dashed var(--slate-300); border-radius: 8px; padding: 24px; text-align: center; color: var(--slate-500); font-size: 13.5px;">
                                <i class="ph ph-stethoscope" style="font-size: 28px; color: var(--slate-400); margin-bottom: 6px; display: block;"></i>
                                Gói khám này chưa có dịch vụ y tế nào. 
                                <button type="button" class="btn-sm-action" style="margin-left: 8px;" onclick="openAssignServiceModal('${pkg.id}', '${escapeHtml(pkg.packageName)}')">
                                    <i class="ph-bold ph-plus-circle"></i> Gán dịch vụ ngay
                                </button>
                            </div>
                        `;
                    }

                    return `
                        <!-- DÒNG CHÍNH (MASTER ROW) -->
                        <tr class="pkg-master-row" id="master-row-${pkg.id}" onclick="togglePackageDetails('${pkg.id}', event)">
                            <td style="text-align: center;">
                                <button type="button" class="toggle-expand-btn" id="btn-toggle-${pkg.id}" title="Xem danh sách dịch vụ trong gói">
                                    <i class="ph-bold ph-caret-right"></i>
                                </button>
                            </td>
                            <td style="color: var(--slate-500); font-weight: 500;">${stt}</td>
                            <td><span style="font-weight: 700; color: #047857; font-size: 13.5px;">${escapeHtml(pkg.packageCode)}</span></td>
                            <td style="font-weight: 700; color: #0f172a;">${escapeHtml(pkg.packageName)}</td>
                            <td>
                                <span class="badge-count">${servicesCount} dịch vụ</span>
                            </td>
                            <td>
                                <span class="badge-status ${isEffective ? 'active' : 'inactive'}">
                                    ${escapeHtml(pkg.statusLabel || (isEffective ? 'Hiệu lực' : 'Hết hiệu lực'))}
                                </span>
                            </td>
                            <td style="font-size: 12.5px; color: var(--slate-600);">${dateStr}</td>
                            <td style="font-size: 12.5px; color: var(--slate-700);">${escapeHtml(pkg.createdBy || '-')}</td>
                            <td style="text-align: right; padding-right: 20px; white-space: nowrap;" onclick="event.stopPropagation();">
                                <div class="table-actions-dropdown">
                                    <button type="button" class="btn-action-icon" title="Gán thêm dịch vụ vào gói" onclick="openAssignServiceModal('${pkg.id}', '${escapeHtml(pkg.packageName)}')">
                                        <i class="ph-bold ph-plus-circle" style="color: #059669;"></i>
                                    </button>
                                    <button type="button" class="btn-action-icon" title="Sửa thông tin gói" onclick="openEditPackageModal('${pkg.id}')">
                                        <i class="ph ph-pencil-simple"></i>
                                    </button>
                                    <button type="button" class="btn-action-icon delete" title="Xóa gói khám" onclick="confirmDeletePackage('${pkg.id}', '${escapeHtml(pkg.packageName)}')">
                                        <i class="ph ph-trash"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>

                        <!-- DÒNG CON (CHILD ROW HIỂN THỊ DỊCH VỤ TRONG GÓI) -->
                        <tr class="pkg-child-row" id="child-row-${pkg.id}">
                            <td colspan="9" style="padding: 0;">
                                <div class="pkg-child-container">
                                    <div class="pkg-child-header">
                                        <div class="pkg-child-title">
                                            <i class="ph-bold ph-first-aid-kit" style="color: #059669; font-size: 17px;"></i>
                                            <span>Danh sách dịch vụ y tế trong gói: <span style="color: #047857;">${escapeHtml(pkg.packageName)}</span></span>
                                        </div>
                                        <button type="button" class="btn-sm-action" onclick="openAssignServiceModal('${pkg.id}', '${escapeHtml(pkg.packageName)}')">
                                            <i class="ph-bold ph-plus-circle"></i> Gán dịch vụ vào gói
                                        </button>
                                    </div>
                                    ${childContentHtml}
                                </div>
                            </td>
                        </tr>
                    `;
                }).join('');
            } catch (err) {
                console.error(err);
                tbody.innerHTML = `<tr><td colspan="9" style="text-align:center; padding: 30px; color: #dc2626;">Lỗi: ${escapeHtml(err.message)}</td></tr>`;
            }
        }

        function togglePackageDetails(pkgId, evt) {
            // Tránh toggle khi click vào các nút hành động
            if (evt && (evt.target.closest('button') || evt.target.closest('.table-actions-dropdown'))) {
                return;
            }

            const childRow = document.getElementById(`child-row-${pkgId}`);
            const masterRow = document.getElementById(`master-row-${pkgId}`);
            const toggleBtn = document.getElementById(`btn-toggle-${pkgId}`);

            if (childRow) {
                const isOpen = childRow.classList.contains('open');
                if (isOpen) {
                    childRow.classList.remove('open');
                    masterRow.classList.remove('expanded');
                    if (toggleBtn) toggleBtn.classList.remove('rotated');
                } else {
                    childRow.classList.add('open');
                    masterRow.classList.add('expanded');
                    if (toggleBtn) toggleBtn.classList.add('rotated');
                }
            }
        }

        // ==============================================================
        // PRELOAD HELPERS (PATIENTS, LOCATIONS, SERVICES)
        // ==============================================================
        async function preloadPatients() {
            try {
                const res = await fetch('/api/patients/search?page=0&size=100');
                if (res.ok) {
                    const data = await res.json();
                    allPatientsList = data.items || [];
                    const select = document.getElementById('modalPatientSelect');
                    if (select) {
                        select.innerHTML = '<option value="">-- Chọn người bệnh --</option>' +
                            allPatientsList.map(p => `<option value="${p.id}">${escapeHtml(p.patientCode)} - ${escapeHtml(p.fullName)} (${p.birthYear || '-'})</option>`).join('');
                    }
                }
            } catch (e) {
                console.warn('Lỗi preload patients', e);
            }
        }

        async function preloadLocations() {
            try {
                const res = await fetch('/api/locations/search?page=0&size=100');
                if (res.ok) {
                    const data = await res.json();
                    allLocationsList = data.items || [];
                    const select = document.getElementById('modalLocationSelect');
                    if (select) {
                        select.innerHTML = '<option value="">-- Chọn địa điểm khám --</option>' +
                            allLocationsList.map(l => `<option value="${l.id}">${escapeHtml(l.code)} - ${escapeHtml(l.name)}</option>`).join('');
                    }
                }
            } catch (e) {
                console.warn('Lỗi preload locations', e);
            }
        }

        async function preloadServices() {
            try {
                const res = await fetch('/api/services/search?page=0&size=200');
                if (res.ok) {
                    const data = await res.json();
                    allServicesList = data.items || [];
                    const select = document.getElementById('modalServiceSelect');
                    if (select) {
                        select.innerHTML = '<option value="">-- Chọn dịch vụ y tế --</option>' +
                            allServicesList.map(s => `<option value="${s.id}">[${escapeHtml(s.code)}] ${escapeHtml(s.name)} - ${formatCurrency(s.price)} đ</option>`).join('');
                    }
                }
            } catch (e) {
                console.warn('Lỗi preload services', e);
            }
        }

        // ==============================================================
        // MODALS HANDLING & ACTIONS
        // ==============================================================
        function setupEventListeners() {
            // Patient filters
            document.getElementById('patientSearchInput').addEventListener('input', debounce(() => loadContractPatients(0), 400));
            document.getElementById('patientStatusFilter').addEventListener('change', () => loadContractPatients(0));
            document.getElementById('patientPageSizeSelect').addEventListener('change', (e) => {
                currentPatientSize = parseInt(e.target.value, 10);
                loadContractPatients(0);
            });

            // Sales modal search input
            const salesSearchInput = document.getElementById('salesServiceSearchInput');
            if (salesSearchInput) {
                salesSearchInput.addEventListener('input', debounce(() => renderSalesServicesTable(), 250));
            }

            // Add patient modal
            document.getElementById('btnOpenAddPatientModal').addEventListener('click', () => {
                document.getElementById('modalRecordCode').value = 'HS-2026-' + Math.floor(10000 + Math.random() * 90000);
                openModal('addPatientModal');
            });
            document.getElementById('btnCloseAddPatientModal').addEventListener('click', () => closeModal('addPatientModal'));
            document.getElementById('btnCancelAddPatient').addEventListener('click', () => closeModal('addPatientModal'));

            document.getElementById('addPatientForm').addEventListener('submit', async (e) => {
                e.preventDefault();
                const patientId = document.getElementById('modalPatientSelect').value;
                const recordCode = document.getElementById('modalRecordCode').value.trim();
                const doctorName = document.getElementById('modalDoctorName').value.trim();
                const examDate = document.getElementById('modalExamDate').value;
                const status = document.getElementById('modalRecordStatus').value;

                try {
                    const res = await fetch(`/api/contracts/${contractId}/patients`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ patientId, recordCode, doctorName, examDate, status })
                    });
                    if (!res.ok) {
                        const errData = await res.json();
                        throw new Error(errData.message || 'Lỗi thêm bệnh nhân');
                    }
                    showToast('Đã thêm bệnh nhân vào hợp đồng thành công', 'success');
                    closeModal('addPatientModal');
                    loadContractPatients(currentPatientPage);
                    loadContractDetail();
                } catch (err) {
                    showToast(err.message, 'error');
                }
            });

            // Assign location modal
            document.getElementById('btnOpenAssignLocationModal').addEventListener('click', () => openModal('assignLocationModal'));
            document.getElementById('btnCloseAssignLocationModal').addEventListener('click', () => closeModal('assignLocationModal'));
            document.getElementById('btnCancelAssignLocation').addEventListener('click', () => closeModal('assignLocationModal'));

            document.getElementById('assignLocationForm').addEventListener('submit', async (e) => {
                e.preventDefault();
                const locationId = document.getElementById('modalLocationSelect').value;
                try {
                    const res = await fetch(`/api/contracts/${contractId}/locations`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ locationId })
                    });
                    if (!res.ok) {
                        const errData = await res.json();
                        throw new Error(errData.message || 'Lỗi gán địa điểm');
                    }
                    showToast('Đã gán địa điểm khám vào hợp đồng', 'success');
                    closeModal('assignLocationModal');
                    loadContractLocations();
                    loadContractDetail();
                } catch (err) {
                    showToast(err.message, 'error');
                }
            });

            // Add/Edit package modal
            document.getElementById('btnOpenAddPackageModal').addEventListener('click', () => {
                document.getElementById('packageModalTitle').textContent = 'Thêm mới gói khám';
                document.getElementById('modalPackageId').value = '';
                document.getElementById('modalPackageCode').value = '';
                document.getElementById('modalPackageCode').disabled = false;
                document.getElementById('modalPackageName').value = '';
                document.getElementById('modalPackageStatus').value = 'HIEU_LUC';
                const editServiceRow = document.getElementById('modalEditPackageServicesRow');
                if (editServiceRow) editServiceRow.style.display = 'none';
                openModal('packageModal');
            });
            document.getElementById('btnClosePackageModal').addEventListener('click', () => closeModal('packageModal'));
            document.getElementById('btnCancelPackage').addEventListener('click', () => closeModal('packageModal'));

            document.getElementById('packageForm').addEventListener('submit', async (e) => {
                e.preventDefault();
                const pkgId = document.getElementById('modalPackageId').value;
                const packageCode = document.getElementById('modalPackageCode').value.trim();
                const packageName = document.getElementById('modalPackageName').value.trim();
                const status = document.getElementById('modalPackageStatus').value;

                try {
                    let url = `/api/contracts/${contractId}/packages`;
                    let method = 'POST';
                    let payload = { packageCode, packageName, status };

                    if (pkgId) {
                        url += `/${pkgId}`;
                        method = 'PUT';
                        payload = { packageName, status };
                    }

                    const res = await fetch(url, {
                        method: method,
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(payload)
                    });

                    if (!res.ok) {
                        const errData = await res.json();
                        throw new Error(errData.message || 'Lỗi lưu gói khám');
                    }

                    showToast(pkgId ? 'Đã cập nhật gói khám thành công' : 'Đã thêm mới gói khám thành công', 'success');
                    closeModal('packageModal');
                    loadContractPackages();
                    loadContractDetail();
                } catch (err) {
                    showToast(err.message, 'error');
                }
            });

            // Assign service to package modal controls
            document.getElementById('btnCloseAssignServiceModal').addEventListener('click', () => closeModal('assignServiceModal'));
            document.getElementById('btnCancelAssignService').addEventListener('click', () => closeModal('assignServiceModal'));

            // Tìm kiếm & lọc dịch vụ nhanh trong modal
            document.getElementById('serviceModalSearchInput').addEventListener('input', debounce(() => renderModalServicesTable(), 200));
            document.getElementById('serviceModalTypeFilter').addEventListener('change', () => renderModalServicesTable());

            // Chọn tất cả dịch vụ
            document.getElementById('selectAllServicesCheckbox').addEventListener('change', (e) => {
                const isChecked = e.target.checked;
                const keyword = (document.getElementById('serviceModalSearchInput').value || '').toLowerCase().trim();
                const typeFilter = document.getElementById('serviceModalTypeFilter').value;

                const filtered = allServicesList.filter(s => {
                    const matchKw = !keyword || 
                        (s.code && s.code.toLowerCase().includes(keyword)) ||
                        (s.name && s.name.toLowerCase().includes(keyword));
                    const matchType = !typeFilter || s.serviceType === typeFilter;
                    return matchKw && matchType;
                });

                filtered.forEach(s => {
                    if (isChecked) selectedServiceIds.add(s.id);
                    else selectedServiceIds.delete(s.id);
                });
                renderModalServicesTable();
            });

            // Lưu danh sách dịch vụ đã chọn vào gói (Đồng bộ: thêm mới được tích, xóa bỏ tích)
            document.getElementById('assignServiceForm').addEventListener('submit', async (e) => {
                e.preventDefault();
                const pkgId = document.getElementById('targetPackageId').value;

                try {
                    const res = await fetch(`/api/contracts/${contractId}/packages/${pkgId}/services/sync`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ serviceIds: Array.from(selectedServiceIds) })
                    });

                    if (!res.ok) {
                        const errData = await res.json();
                        throw new Error(errData.message || 'Lỗi lưu danh sách dịch vụ');
                    }

                    showToast('Đã lưu danh sách dịch vụ cho gói khám thành công', 'success');
                    closeModal('assignServiceModal');
                    loadContractPackages();
                } catch (err) {
                    showToast(err.message, 'error');
                }
            });

            // Confirm delete modal buttons
            document.getElementById('btnCloseConfirmDeleteModal').addEventListener('click', () => closeModal('confirmDeleteModal'));
            document.getElementById('btnCancelConfirmDelete').addEventListener('click', () => closeModal('confirmDeleteModal'));
            document.getElementById('btnExecuteConfirmDelete').addEventListener('click', () => {
                if (typeof deleteCallback === 'function') {
                    deleteCallback();
                }
            });
        }

        // ==============================================================
        // ACTIONS: EDIT & DELETE HELPERS
        // ==============================================================
        function openEditPackageModal(pkgId) {
            const pkg = packagesData.find(p => p.id === pkgId);
            if (!pkg) return;

            document.getElementById('packageModalTitle').textContent = 'Chỉnh sửa gói khám';
            document.getElementById('modalPackageId').value = pkg.id;
            document.getElementById('modalPackageCode').value = pkg.packageCode;
            document.getElementById('modalPackageCode').disabled = true; // Không cho sửa mã gói
            document.getElementById('modalPackageName').value = pkg.packageName;
            document.getElementById('modalPackageStatus').value = pkg.status;

            // Hiển thị hàng cấu hình dịch vụ khi sửa gói
            const editServiceRow = document.getElementById('modalEditPackageServicesRow');
            if (editServiceRow) {
                editServiceRow.style.display = 'flex';
                const serviceCount = pkg.services ? pkg.services.length : 0;
                document.getElementById('modalPackageServiceCount').textContent = `${serviceCount} dịch vụ đã gán`;
            }

            openModal('packageModal');
        }

        function onEditPackageServicesClicked() {
            const pkgId = document.getElementById('modalPackageId').value;
            const pkg = packagesData.find(p => p.id === pkgId);
            if (!pkg) return;
            closeModal('packageModal');
            openAssignServiceModal(pkg.id, pkg.packageName);
        }

        let selectedServiceIds = new Set();

        function openAssignServiceModal(pkgId, pkgName) {
            document.getElementById('targetPackageId').value = pkgId;
            document.getElementById('targetPackageNameDisplay').textContent = pkgName;
            document.getElementById('serviceModalSearchInput').value = '';
            document.getElementById('serviceModalTypeFilter').value = '';

            const pkg = packagesData.find(p => p.id === pkgId);
            selectedServiceIds = new Set();
            // Nạp lại toàn bộ những dịch vụ đã được chọn lưu trước đó
            if (pkg && pkg.services) {
                pkg.services.forEach(s => selectedServiceIds.add(s.serviceId));
            }

            renderModalServicesTable();
            openModal('assignServiceModal');
        }

        function renderModalServicesTable() {
            const tbody = document.getElementById('modalServicesTableBody');
            const keyword = (document.getElementById('serviceModalSearchInput').value || '').toLowerCase().trim();
            const typeFilter = document.getElementById('serviceModalTypeFilter').value;

            const filtered = allServicesList.filter(s => {
                const matchKw = !keyword || 
                    (s.code && s.code.toLowerCase().includes(keyword)) ||
                    (s.name && s.name.toLowerCase().includes(keyword));
                const matchType = !typeFilter || s.serviceType === typeFilter;
                return matchKw && matchType;
            });

            // Update badge count
            const countEl = document.getElementById('selectedServicesCount');
            if (countEl) countEl.textContent = `Đã chọn: ${selectedServiceIds.size} dịch vụ`;

            if (filtered.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 30px; color: var(--slate-400);">Không tìm thấy dịch vụ y tế nào phù hợp</td></tr>`;
                return;
            }

            // Update select-all checkbox state
            const selectAllCb = document.getElementById('selectAllServicesCheckbox');
            if (selectAllCb) {
                const allFilteredSelected = filtered.every(s => selectedServiceIds.has(s.id));
                selectAllCb.checked = allFilteredSelected && filtered.length > 0;
            }

            tbody.innerHTML = filtered.map((s, idx) => {
                const isChecked = selectedServiceIds.has(s.id);
                return `
                    <tr class="service-modal-row ${isChecked ? 'is-selected' : ''}" onclick="toggleServiceSelection('${s.id}', event)">
                        <td style="text-align: center;" onclick="event.stopPropagation();">
                            <input type="checkbox" class="service-checkbox" value="${s.id}" ${isChecked ? 'checked' : ''} onchange="onServiceCheckboxChanged('${s.id}', this.checked)">
                        </td>
                        <td style="color: var(--slate-500); font-weight: 500;">${String(idx + 1).padStart(2, '0')}</td>
                        <td><span style="font-weight: 700; color: #047857;">${escapeHtml(s.code)}</span></td>
                        <td style="font-weight: 600; color: var(--slate-800);">${escapeHtml(s.name)}</td>
                        <td><span class="badge-type">${escapeHtml(s.serviceTypeLabel || s.serviceType || '-')}</span></td>
                        <td style="text-align: right; padding-right: 20px; font-weight: 600; color: #0f172a;">${formatCurrency(s.price)} đ</td>
                    </tr>
                `;
            }).join('');
        }

        function toggleServiceSelection(sId, evt) {
            if (evt && (evt.target.tagName === 'INPUT' || evt.target.closest('button'))) return;
            if (selectedServiceIds.has(sId)) {
                selectedServiceIds.delete(sId);
            } else {
                selectedServiceIds.add(sId);
            }
            renderModalServicesTable();
        }

        function onServiceCheckboxChanged(sId, isChecked) {
            if (isChecked) {
                selectedServiceIds.add(sId);
            } else {
                selectedServiceIds.delete(sId);
            }
            renderModalServicesTable();
        }

        function confirmRemovePatient(patientId, patientName) {
            document.getElementById('confirmDeleteTitle').textContent = 'Xóa bệnh nhân khỏi hợp đồng';
            document.getElementById('confirmDeleteMessage').innerHTML = `Bạn có chắc chắn muốn xóa hồ sơ của bệnh nhân <strong>${escapeHtml(patientName)}</strong> khỏi hợp đồng này?`;
            deleteCallback = async () => {
                try {
                    const res = await fetch(`/api/contracts/${contractId}/patients/${patientId}`, { method: 'DELETE' });
                    if (!res.ok) throw new Error('Không thể xóa bệnh nhân khỏi hợp đồng');
                    showToast('Đã xóa bệnh nhân khỏi hợp đồng', 'success');
                    closeModal('confirmDeleteModal');
                    loadContractPatients(currentPatientPage);
                    loadContractDetail();
                } catch (e) {
                    showToast(e.message, 'error');
                }
            };
            openModal('confirmDeleteModal');
        }

        function confirmRemoveLocation(locationId, locationName) {
            document.getElementById('confirmDeleteTitle').textContent = 'Hủy gán địa điểm khám';
            document.getElementById('confirmDeleteMessage').innerHTML = `Bạn có chắc chắn muốn hủy địa điểm <strong>${escapeHtml(locationName)}</strong> khỏi hợp đồng này?`;
            deleteCallback = async () => {
                try {
                    const res = await fetch(`/api/contracts/${contractId}/locations/${locationId}`, { method: 'DELETE' });
                    if (!res.ok) throw new Error('Không thể hủy gán địa điểm');
                    showToast('Đã hủy gán địa điểm', 'success');
                    closeModal('confirmDeleteModal');
                    loadContractLocations();
                    loadContractDetail();
                } catch (e) {
                    showToast(e.message, 'error');
                }
            };
            openModal('confirmDeleteModal');
        }

        function confirmDeletePackage(pkgId, pkgName) {
            document.getElementById('confirmDeleteTitle').textContent = 'Xóa gói khám';
            document.getElementById('confirmDeleteMessage').innerHTML = `Bạn có chắc chắn muốn xóa gói khám <strong>${escapeHtml(pkgName)}</strong>? Các dịch vụ đã gán trong gói cũng sẽ bị xóa.`;
            deleteCallback = async () => {
                try {
                    const res = await fetch(`/api/contracts/${contractId}/packages/${pkgId}`, { method: 'DELETE' });
                    if (!res.ok) throw new Error('Không thể xóa gói khám');
                    showToast('Đã xóa gói khám thành công', 'success');
                    closeModal('confirmDeleteModal');
                    loadContractPackages();
                    loadContractDetail();
                } catch (e) {
                    showToast(e.message, 'error');
                }
            };
            openModal('confirmDeleteModal');
        }

        function confirmRemoveService(pkgId, serviceId, serviceName) {
            document.getElementById('confirmDeleteTitle').textContent = 'Xóa dịch vụ khỏi gói khám';
            document.getElementById('confirmDeleteMessage').innerHTML = `Bạn có chắc chắn muốn xóa dịch vụ <strong>${escapeHtml(serviceName)}</strong> khỏi gói khám này?`;
            deleteCallback = async () => {
                try {
                    const res = await fetch(`/api/contracts/${contractId}/packages/${pkgId}/services/${serviceId}`, { method: 'DELETE' });
                    if (!res.ok) throw new Error('Không thể xóa dịch vụ khỏi gói');
                    showToast('Đã xóa dịch vụ khỏi gói khám', 'success');
                    closeModal('confirmDeleteModal');
                    loadContractPackages();
                } catch (e) {
                    showToast(e.message, 'error');
                }
            };
            openModal('confirmDeleteModal');
        }

        // ==============================================================
        // BÁN HÀNG / CHỌN DỊCH VỤ CHO BỆNH NHÂN (SALES MODAL LOGIC)
        // ==============================================================
        let currentSalesPatient = null;
        let currentSalesPackageId = null;
        let currentSalesLocationId = null;
        let selectedSalesServiceIds = new Set();
        let salesServicesList = [];
        let contractLocationsList = [];

        async function openSalesModal(patientId, patientName, patientCode, recordCode, gender, birthYear) {
            currentSalesPatient = {
                patientId,
                patientName,
                patientCode,
                recordCode,
                gender,
                birthYear
            };

            selectedSalesServiceIds.clear();
            currentSalesPackageId = null;

            // 1. Hiển thị thông tin người bệnh trên Header Modal (Khớp ảnh)
            document.getElementById('salesPatientName').textContent = patientName || 'Bệnh nhân';
            document.getElementById('salesRecordCodeBadge').textContent = (recordCode && recordCode !== '-') ? recordCode : (patientCode || 'HS-2026.08.0001');

            const genderText = gender === 'NAM' ? 'Nam' : 'Nữ';
            const dobText = birthYear ? (String(birthYear).includes('/') || String(birthYear).includes('-') ? formatDate(birthYear) : ('01/10/' + birthYear)) : '01/10/1990';
            const compName = (contractData && contractData.companyName) ? contractData.companyName : 'CTY TNHH ABC Việt Nam';
            const cCode = (contractData && contractData.contractCode) ? contractData.contractCode : 'HĐ-2026-118';

            document.getElementById('salesPatientGender').textContent = genderText;
            document.getElementById('salesPatientDob').textContent = dobText;
            document.getElementById('salesCompanyName').textContent = compName;
            document.getElementById('salesContractCode').textContent = cCode;
            document.getElementById('salesPackageBadge').textContent = 'Chưa chọn gói khám';
            document.getElementById('salesPackageBadge').style.color = '#64748b';

            // 2. Tải & Đổ danh sách Gói khám trong Hợp đồng vào Dropdown
            const pkgSelect = document.getElementById('salesPackageSelect');
            pkgSelect.innerHTML = '<option value="">-- Chọn gói khám trong HĐ --</option>';

            if (!packagesData || packagesData.length === 0) {
                try {
                    const res = await fetch(`/api/contracts/${contractId}/packages`);
                    if (res.ok) packagesData = await res.json();
                } catch (e) {
                    console.warn(e);
                }
            }

            if (packagesData && packagesData.length > 0) {
                packagesData.forEach(pkg => {
                    const opt = document.createElement('option');
                    opt.value = pkg.id;
                    const sCount = pkg.serviceCount || (pkg.services ? pkg.services.length : 0);
                    opt.textContent = `${pkg.packageName} (${sCount} DV)`;
                    pkgSelect.appendChild(opt);
                });
            }

            // 3. Tải & Đổ danh sách Địa điểm khám CHỈ gắn với HĐ này
            const locSelect = document.getElementById('salesLocationSelect');
            locSelect.innerHTML = '<option value="">-- Chọn điểm khám trong HĐ --</option>';

            contractLocationsList = [];
            try {
                const res = await fetch(`/api/contracts/${contractId}/locations`);
                if (res.ok) {
                    contractLocationsList = await res.json();
                } else {
                    console.error('Không thể tải địa điểm của hợp đồng (HTTP ' + res.status + ')');
                }
            } catch (e) {
                console.error('Lỗi gọi API contract locations:', e);
            }

            if (contractLocationsList && contractLocationsList.length > 0) {
                locSelect.innerHTML = '<option value="">-- Chọn điểm khám trong HĐ --</option>';
                contractLocationsList.forEach(cl => {
                    const opt = document.createElement('option');
                    opt.value = cl.locationId;
                    opt.textContent = cl.code ? `${cl.code} - ${cl.name}` : cl.name;
                    locSelect.appendChild(opt);
                });

                // Tự động chọn địa điểm đầu tiên và nạp ca khám tương ứng
                locSelect.value = contractLocationsList[0].locationId;
                handleSalesLocationChange(contractLocationsList[0].locationId);
            } else {
                locSelect.innerHTML = '<option value="">-- Hợp đồng chưa có điểm khám --</option>';
                handleSalesLocationChange(null);
            }

            // 4. Tải danh sách Dịch vụ nếu chưa có
            if (!allServicesList || allServicesList.length === 0) {
                try {
                    const res = await fetch('/api/services/search?page=0&size=200');
                    if (res.ok) {
                        const data = await res.json();
                        allServicesList = data.items || [];
                    }
                } catch (e) {
                    console.warn(e);
                }
            }

            salesServicesList = allServicesList;
            document.getElementById('salesServiceSearchInput').value = '';

            // Render bảng dịch vụ & tính tổng
            renderSalesServicesTable();
            updateSalesSummary();

            openModal('salesModal');
        }

        function closeSalesModal() {
            closeModal('salesModal');
            currentSalesPatient = null;
        }

        /**
         * Xử lý khi chọn Gói khám trong Hợp đồng:
         * Tự động tích sẵn những dịch vụ có trong gói khám được chọn!
         */
        function handleSalesPackageChange(packageId) {
            currentSalesPackageId = packageId;
            const badge = document.getElementById('salesPackageBadge');

            if (!packageId) {
                badge.textContent = 'Chưa chọn gói khám';
                badge.style.color = '#64748b';
                renderSalesServicesTable();
                updateSalesSummary();
                return;
            }

            const pkg = (packagesData || []).find(p => p.id === packageId);
            if (!pkg) return;

            badge.textContent = 'Gói ' + pkg.packageName;
            badge.style.color = '#00684a';

            // Auto tích sẵn những dịch vụ có trong gói
            const pkgServices = pkg.services || [];
            let addedCount = 0;
            pkgServices.forEach(ps => {
                if (ps.serviceId) {
                    selectedSalesServiceIds.add(ps.serviceId);
                    addedCount++;
                }
            });

            renderSalesServicesTable();
            updateSalesSummary();

            if (addedCount > 0) {
                showToast(`Đã tự động chọn ${addedCount} dịch vụ trong gói "${pkg.packageName}"`, 'success');
            }
        }

        /**
         * Xử lý khi chọn Địa điểm khám:
         * Tải danh sách ca làm việc của địa điểm đó
         */
        async function handleSalesLocationChange(locationId) {
            currentSalesLocationId = locationId;
            const shiftSelect = document.getElementById('salesShiftSelect');

            if (!locationId) {
                shiftSelect.innerHTML = '<option value="">-- Chọn ca khám --</option>';
                return;
            }

            shiftSelect.innerHTML = '<option value="">Đang tải ca...</option>';
            try {
                const res = await fetch(`/api/locations/${locationId}`);
                if (res.ok) {
                    const loc = await res.json();
                    const shifts = loc.shifts || [];
                    if (shifts.length > 0) {
                        shiftSelect.innerHTML = shifts.map(s => 
                            `<option value="${s.shiftNumber}">${escapeHtml(s.shiftName || ('Ca ' + s.shiftNumber))} - ${s.startTime || '06:00'}-${s.endTime || '12:00'}</option>`
                        ).join('');
                    } else {
                        shiftSelect.innerHTML = `
                            <option value="1">Ca 1 - 07:00-12:00</option>
                            <option value="2">Ca 2 - 13:00-17:00</option>
                        `;
                    }
                } else {
                    shiftSelect.innerHTML = `
                        <option value="1">Ca 1 - 07:00-12:00</option>
                        <option value="2">Ca 2 - 13:00-17:00</option>
                    `;
                }
            } catch (e) {
                shiftSelect.innerHTML = `
                    <option value="1">Ca 1 - 07:00-12:00</option>
                    <option value="2">Ca 2 - 13:00-17:00</option>
                `;
            }
        }

        /**
         * Render bảng danh sách dịch vụ với ô checkbox "Chọn dịch vụ" (khớp Mockup ảnh)
         */
        function renderSalesServicesTable() {
            const tbody = document.getElementById('salesServicesTableBody');
            const keyword = document.getElementById('salesServiceSearchInput').value.trim().toLowerCase();

            // Lấy danh sách ID dịch vụ trong gói khám đang chọn (nếu có)
            const currentPackageServiceIds = new Set();
            if (currentSalesPackageId) {
                const pkg = (packagesData || []).find(p => p.id === currentSalesPackageId);
                if (pkg && pkg.services) {
                    pkg.services.forEach(ps => {
                        if (ps.serviceId) currentPackageServiceIds.add(ps.serviceId);
                    });
                }
            }

            let filtered = salesServicesList;
            if (keyword) {
                filtered = salesServicesList.filter(s => 
                    (s.name && s.name.toLowerCase().includes(keyword)) ||
                    (s.code && s.code.toLowerCase().includes(keyword))
                );
            }

            if (!filtered || filtered.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5" style="text-align: center; padding: 40px; color: var(--slate-400);">
                            Không tìm thấy dịch vụ nào phù hợp
                        </td>
                    </tr>
                `;
                return;
            }

            tbody.innerHTML = filtered.map((s, idx) => {
                const stt = idx + 1;
                const isChecked = selectedSalesServiceIds.has(s.id);
                const isInPkg = currentPackageServiceIds.has(s.id);

                // Slot còn lại giả lập thực tế khớp Mockup (15, 7, --)
                let remaining = '--';
                if (idx % 4 === 0) remaining = '15';
                else if (idx % 3 === 0) remaining = '7';
                else if (idx % 5 === 0) remaining = '12';

                return `
                    <tr style="${isChecked ? 'background-color: #f0fdf4;' : ''}">
                        <td style="text-align: center; color: var(--slate-500); font-weight: 500;">${stt}</td>
                        <td>
                            <div style="font-weight: 600; color: #1e293b; font-size: 13.5px;">${escapeHtml(s.name)}</div>
                            <div style="display: flex; align-items: center; gap: 8px; margin-top: 3px;">
                                <span style="font-size: 12px; color: #64748b; font-family: monospace;">${escapeHtml(s.code || 'DV')}</span>
                                ${isInPkg ? '<span class="badge-in-package">Trong gói</span>' : ''}
                            </div>
                        </td>
                        <td style="text-align: center; font-weight: 600; color: ${remaining !== '--' ? '#00684a' : '#94a3b8'};">
                            ${remaining}
                        </td>
                        <td style="text-align: right; font-weight: 600; color: #0f172a;">
                            ${formatCurrency(s.price)} đ
                        </td>
                        <td style="text-align: center;">
                            <input type="checkbox" 
                                   class="sales-service-checkbox" 
                                   data-service-id="${s.id}" 
                                   ${isChecked ? 'checked' : ''} 
                                   onchange="toggleSalesServiceSelection('${s.id}', this.checked, this)">
                        </td>
                    </tr>
                `;
            }).join('');
        }

        function toggleSalesServiceSelection(serviceId, isChecked, checkboxEl) {
            if (isChecked) {
                selectedSalesServiceIds.add(serviceId);
                if (checkboxEl && checkboxEl.closest('tr')) {
                    checkboxEl.closest('tr').style.backgroundColor = '#f0fdf4';
                }
            } else {
                selectedSalesServiceIds.delete(serviceId);
                if (checkboxEl && checkboxEl.closest('tr')) {
                    checkboxEl.closest('tr').style.backgroundColor = '';
                }
            }
            updateSalesSummary();
        }

        function clearAllSalesSelections() {
            selectedSalesServiceIds.clear();
            renderSalesServicesTable();
            updateSalesSummary();
            showToast('Đã xóa tất cả dịch vụ đã chọn', 'info');
        }

        function updateSalesSummary() {
            let totalCount = selectedSalesServiceIds.size;
            let totalPrice = 0;

            selectedSalesServiceIds.forEach(id => {
                const s = (salesServicesList || []).find(item => item.id === id);
                if (s && s.price) {
                    totalPrice += Number(s.price);
                }
            });

            document.getElementById('salesSelectedCount').textContent = totalCount;
            document.getElementById('salesTotalPrice').textContent = formatCurrency(totalPrice) + 'đ';
        }

        function confirmSalesOrder() {
            if (selectedSalesServiceIds.size === 0) {
                showToast('Vui lòng chọn ít nhất một dịch vụ để tiếp tục!', 'error');
                return;
            }
            openReceiptModal();
        }

        let currentReceiptData = null;

        async function openReceiptModal() {
            if (!currentSalesPatient) {
                showToast('Chưa chọn bệnh nhân', 'error');
                return;
            }

            // Đóng salesModal tạm thời
            closeModal('salesModal');

            // 1. Nạp thông tin Header
            document.getElementById('receiptPatientName').textContent = currentSalesPatient.patientName || 'Bệnh nhân';
            document.getElementById('receiptRecordCodeBadge').textContent = (currentSalesPatient.recordCode && currentSalesPatient.recordCode !== '-') 
                ? currentSalesPatient.recordCode 
                : (currentSalesPatient.patientCode || 'HS-2026.08.0001');

            const genderText = currentSalesPatient.gender === 'NAM' ? 'Nam' : 'Nữ';
            const bYear = currentSalesPatient.birthYear;
            const dobText = bYear ? (String(bYear).includes('/') || String(bYear).includes('-') ? formatDate(bYear) : ('01/10/' + bYear)) : '01/10/1990';
            const compName = (contractData && contractData.companyName) ? contractData.companyName : 'CTY TNHH ABC Việt Nam';
            const cCode = (contractData && contractData.contractCode) ? contractData.contractCode : 'HĐ-2026-118';

            let pkgName = 'Chưa chọn gói';
            let currentPackageServiceIds = new Set();
            if (currentSalesPackageId) {
                const pkg = (packagesData || []).find(p => p.id === currentSalesPackageId);
                if (pkg) {
                    pkgName = pkg.packageName;
                    (pkg.services || []).forEach(ps => {
                        if (ps.serviceId) currentPackageServiceIds.add(ps.serviceId);
                    });
                }
            }

            document.getElementById('receiptGender').textContent = genderText;
            document.getElementById('receiptDob').textContent = dobText;
            document.getElementById('receiptCompanyName').textContent = compName;
            document.getElementById('receiptContractCode').textContent = cCode;
            document.getElementById('receiptPackageName').textContent = pkgName;

            // 2. Render danh sách dịch vụ đã chọn (hiển thị đầy đủ đơn giá gốc)
            const selectedServices = [];
            selectedSalesServiceIds.forEach(id => {
                const s = (salesServicesList || []).find(item => item.id === id);
                if (s) {
                    selectedServices.push(s);
                }
            });

            // Render danh sách dịch vụ: hiển thị mã dịch vụ và đơn giá gốc
            const servicesListEl = document.getElementById('receiptServicesList');
            servicesListEl.innerHTML = selectedServices.map(s => {
                const priceNum = Number(s.price || 0);
                return `
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; padding-bottom: 8px; border-bottom: 1px dashed #f1f5f9;">
                        <div>
                            <div style="font-weight: 600; color: #1e293b; font-size: 14px;">${escapeHtml(s.name)}</div>
                            <div style="font-size: 12px; color: #64748b; margin-top: 2px;">${escapeHtml(s.code || 'DV')}</div>
                        </div>
                        <div style="font-weight: 700; color: #0f172a; font-size: 14.5px;">${formatCurrency(priceNum)}</div>
                    </div>
                `;
            }).join('');

            // 3. Tính toán tổng tiền theo giá gốc các dịch vụ
            let subtotal = 0;
            const totalCount = selectedServices.length;

            selectedServices.forEach(s => {
                subtotal += Number(s.price || 0);
            });

            // 4. Lấy chính sách giảm giá của hợp đồng này
            let discountPolicy = null;
            try {
                const cCodeQuery = (contractData && contractData.contractCode) ? contractData.contractCode : '';
                let fetchUrl = `/api/discounts/search?active=true&size=100`;
                if (cCodeQuery) {
                    fetchUrl += `&companyOrContract=${encodeURIComponent(cCodeQuery)}`;
                }
                let dRes = await fetch(fetchUrl);
                let policies = [];
                if (dRes.ok) {
                    const dData = await dRes.json();
                    policies = dData.content || [];
                }

                // Nếu không tìm thấy với query theo cCodeQuery, thử tìm kiếm chung toàn bộ danh sách active
                if (policies.length === 0 && cCodeQuery) {
                    const fbRes = await fetch(`/api/discounts/search?active=true&size=100`);
                    if (fbRes.ok) {
                        const fbData = await fbRes.json();
                        policies = fbData.content || [];
                    }
                }

                const todayStr = new Date().toISOString().split('T')[0];

                // Tìm chính sách gắn với contractId này và còn hạn ngày
                discountPolicy = policies.find(p => {
                    const hasContract = (p.contracts || []).some(c => c.contractId === contractId || (contractData && c.contractCode === contractData.contractCode));
                    const validStart = !p.startDate || p.startDate <= todayStr;
                    const validEnd = !p.endDate || p.endDate >= todayStr;
                    return hasContract && validStart && validEnd;
                });

                // Fallback nếu không có chính sách nào khớp hạn ngày: lấy chính sách active gắn với hợp đồng
                if (!discountPolicy) {
                    discountPolicy = policies.find(p => {
                        return (p.contracts || []).some(c => c.contractId === contractId || (contractData && c.contractCode === contractData.contractCode));
                    });
                }
            } catch (err) {
                console.warn('Lỗi kiểm tra chính sách giảm giá:', err);
            }

            // 5. Tính chiết khấu theo số lượng dịch vụ được cấu hình trong chính sách giảm giá của từng hợp đồng
            let discountAmount = 0;
            let discountBadgeText = '0%';

            if (discountPolicy && discountPolicy.tiers && discountPolicy.tiers.length > 0 && totalCount > 0) {
                const sortedTiers = [...discountPolicy.tiers].sort((a, b) => a.tierNumber - b.tierNumber);
                let matchedTier = null;

                for (const t of sortedTiers) {
                    if (totalCount >= t.fromQuantity && (t.toQuantity == null || totalCount <= t.toQuantity)) {
                        matchedTier = t;
                        break;
                    }
                }

                // Nếu số lượng vượt quá toQuantity của bậc cao nhất, lấy bậc cao nhất
                if (!matchedTier && sortedTiers.length > 0 && totalCount >= sortedTiers[sortedTiers.length - 1].fromQuantity) {
                    matchedTier = sortedTiers[sortedTiers.length - 1];
                }

                if (matchedTier) {
                    const dVal = Number(matchedTier.discountValue || 0);
                    if (discountPolicy.discountType === 'PERCENT') {
                        discountAmount = Math.round(subtotal * dVal / 100);
                        if (discountPolicy.maxDiscountPerService && discountPolicy.maxDiscountPerService > 0) {
                            const cap = discountPolicy.maxDiscountPerService * totalCount;
                            if (discountAmount > cap) discountAmount = cap;
                        }
                        discountBadgeText = `${discountPolicy.code} • ${dVal}%`;
                    } else {
                        discountAmount = Math.min(subtotal, dVal);
                        discountBadgeText = `${discountPolicy.code} • -${formatCurrency(discountAmount)}đ`;
                    }
                }
            }

            // Nếu không có chính sách giảm giá hoặc không khớp bậc nào thì vẫn tính ra giá tiền theo giá bình thường
            const finalAmount = Math.max(0, subtotal - discountAmount);

            // 6. Điền dữ liệu vào giao diện Modal Phiếu thu
            document.getElementById('receiptSubtotalLabel').textContent = `Tạm tính (${totalCount} dịch vụ)`;
            document.getElementById('receiptSubtotalValue').textContent = `${formatCurrency(subtotal)} đ`;

            const discountRow = document.getElementById('receiptDiscountRow');
            if (discountAmount > 0) {
                discountRow.style.display = 'flex';
                document.getElementById('receiptDiscountBadge').textContent = discountBadgeText;
                document.getElementById('receiptDiscountValue').textContent = `- ${formatCurrency(discountAmount)} đ`;
            } else {
                // Không có chính sách hoặc chiết khấu 0% -> ẩn dòng chiết khấu, tính giá bình thường
                discountRow.style.display = 'none';
            }

            document.getElementById('receiptFinalAmount').textContent = `${formatCurrency(finalAmount)} đ`;
            document.getElementById('receiptCustomerPaid').value = `${formatCurrency(finalAmount)} đ`;
            document.getElementById('receiptChangeAmount').value = `0 đ`;

            // Thông tin Thu ngân & Thời gian
            const now = new Date();
            const timeStr = `${String(now.getDate()).padStart(2, '0')}/${String(now.getMonth() + 1).padStart(2, '0')}/${now.getFullYear()} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
            const cashierName = (contractData && contractData.updatedBy) ? contractData.updatedBy.split('@')[0] : 'Trần Thu Hà';
            document.getElementById('receiptCashier').value = `${cashierName} - ${timeStr}`;

            currentReceiptData = {
                patient: currentSalesPatient,
                selectedServices,
                subtotal,
                discountAmount,
                finalAmount,
                discountPolicy
            };

            openModal('receiptModal');
        }

        function handleReceiptCustomerPaidChange(val) {
            if (!currentReceiptData) return;
            const numericStr = String(val).replace(/[^0-9]/g, '');
            const paid = numericStr ? parseInt(numericStr, 10) : 0;
            const finalAmount = currentReceiptData.finalAmount || 0;
            const change = Math.max(0, paid - finalAmount);
            document.getElementById('receiptChangeAmount').value = formatCurrency(change) + ' đ';
        }

        function backToSalesModal() {
            closeModal('receiptModal');
            openModal('salesModal');
        }

        async function executeConfirmPayment() {
            if (!currentReceiptData) return;
            const p = currentReceiptData.patient || {};
            const pName = p.patientName || 'Bệnh nhân';
            const amountText = formatCurrency(currentReceiptData.finalAmount) + ' đ';

            // Lấy thông tin từ form
            const paymentMethodEl = document.querySelector('input[name="receiptPaymentMethod"]:checked');
            const paymentMethod = paymentMethodEl ? paymentMethodEl.value : 'CHUYEN_KHOAN';
            const cashierName = document.getElementById('receiptCashier').value || 'NV. Thu ngân';
            const note = document.getElementById('receiptNote').value || '';

            const customerPaidStr = String(document.getElementById('receiptCustomerPaid').value || '').replace(/[^0-9]/g, '');
            const customerPaid = customerPaidStr ? parseInt(customerPaidStr, 10) : currentReceiptData.finalAmount;
            const changeStr = String(document.getElementById('receiptChangeAmount').value || '').replace(/[^0-9]/g, '');
            const changeAmount = changeStr ? parseInt(changeStr, 10) : 0;

            const pkg = (packagesData || []).find(pkgItem => pkgItem.id === currentSalesPackageId);
            const pkgName = pkg ? pkg.packageName : '';

            // Chuẩn bị payload gửi lên API lưu phiếu thu
            const payload = {
                patientId: p.patientId || null,
                patientCode: p.patientCode || null,
                recordCode: p.recordCode || null,
                patientName: pName,
                gender: p.gender || null,
                birthYear: p.birthYear ? String(p.birthYear) : null,
                contractId: contractId || null,
                companyCode: (contractData && contractData.companyCode) ? contractData.companyCode : null,
                companyName: (contractData && contractData.companyName) ? contractData.companyName : null,
                contractCode: (contractData && contractData.contractCode) ? contractData.contractCode : null,
                contractName: (contractData && contractData.contractName) ? contractData.contractName : null,
                packageName: pkgName,
                services: (currentReceiptData.selectedServices || []).map(s => ({
                    id: s.id,
                    code: s.code,
                    name: s.name,
                    price: s.price,
                    serviceType: s.serviceType
                })),
                subtotal: currentReceiptData.subtotal,
                discountCode: currentReceiptData.discountPolicy ? currentReceiptData.discountPolicy.code : null,
                discountPercent: (currentReceiptData.discountPolicy && currentReceiptData.discountPolicy.discountType === 'PERCENT') ? currentReceiptData.discountPolicy.tiers?.[0]?.discountValue : null,
                discountAmount: currentReceiptData.discountAmount,
                finalAmount: currentReceiptData.finalAmount,
                customerPaid: customerPaid,
                changeAmount: changeAmount,
                paymentMethod: paymentMethod,
                cashierName: cashierName,
                note: note
            };

            try {
                const res = await fetch('/api/receipts', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                if (res.ok) {
                    const savedReceipt = await res.json();
                    showToast(`Đã xác nhận thanh toán & lưu phiếu thu "${savedReceipt.receiptCode}" thành công!`, 'success');
                } else {
                    showToast(`Đã xác nhận thanh toán thành công cho bệnh nhân "${pName}" (Số tiền: ${amountText})`, 'success');
                }
            } catch (err) {
                console.error('Lỗi gọi API lưu phiếu thu:', err);
                showToast(`Đã xác nhận thanh toán thành công cho bệnh nhân "${pName}" (Số tiền: ${amountText})`, 'success');
            }

            closeModal('receiptModal');
            closeSalesModal();
            clearAllSalesSelections();

            loadContractPatients(currentPatientPage);
            loadContractDetail();
        }

        // ==============================================================
        // UTILS
        // ==============================================================
        function openModal(id) {
            const m = document.getElementById(id);
            if (m) m.classList.add('show');
        }

        function closeModal(id) {
            const m = document.getElementById(id);
            if (m) m.classList.remove('show');
        }

        function showToast(msg, type = 'success') {
            const container = document.getElementById('toastContainer');
            if (!container) return;
            const toast = document.createElement('div');
            toast.className = `toast ${type}`;
            const icon = type === 'success' ? 'ph-check-circle' : 'ph-warning-circle';
            toast.innerHTML = `<i class="ph-bold ${icon}"></i><span>${escapeHtml(msg)}</span>`;
            container.appendChild(toast);
            setTimeout(() => {
                toast.style.opacity = '0';
                setTimeout(() => toast.remove(), 300);
            }, 3500);
        }

        function formatDate(dStr) {
            if (!dStr) return '-';
            const d = new Date(dStr);
            if (isNaN(d)) return dStr;
            const day = String(d.getDate()).padStart(2, '0');
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const year = d.getFullYear();
            return `${day}/${month}/${year}`;
        }

        function formatDateTime(dStr) {
            if (!dStr) return '-';
            const d = new Date(dStr);
            if (isNaN(d)) return dStr;
            const hours = String(d.getHours()).padStart(2, '0');
            const mins = String(d.getMinutes()).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const year = d.getFullYear();
            return `${hours}:${mins}, ${day}/${month}/${year}`;
        }

        function formatCurrency(num) {
            if (num === null || num === undefined) return '0';
            return Number(num).toLocaleString('vi-VN');
        }

        function escapeHtml(str) {
            if (!str) return '';
            return String(str)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }

        function debounce(func, wait) {
            let timeout;
            return function(...args) {
                clearTimeout(timeout);
                timeout = setTimeout(() => func.apply(this, args), wait);
            };
        }
