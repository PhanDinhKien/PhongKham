/**
 * ==============================================================
 * ĐỊA ĐIỂM KHÁM - JAVASCRIPT CONTROLLER (LOCATIONS JS)
 * Matches Figma / UI Mockups Image 1 & Image 2
 * ==============================================================
 */

// State
let currentPage = 0;
let pageSize = 20;
let searchKeyword = '';
let totalElements = 0;
let locationDataMap = new Map();
let expandedLocationIds = new Set();
let locationContractsMap = new Map();

// Modal State
let currentModalShifts = [];
let allContracts = [];
let selectedContractIdForAssign = null;
let targetLocationForAssign = null;
let pendingDeleteAction = null;

// Toast Helper
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    const bg = type === 'success' ? '#00684a' : (type === 'error' ? '#dc2626' : '#2563eb');
    const icon = type === 'success' ? 'ph-check-circle' : (type === 'error' ? 'ph-warning-circle' : 'ph-info');

    toast.style.cssText = `
        background: ${bg};
        color: white;
        padding: 12px 20px;
        border-radius: 8px;
        box-shadow: 0 10px 15px -3px rgba(0,0,0,0.15);
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 14px;
        font-weight: 500;
        min-width: 260px;
        animation: slideIn 0.3s ease;
    `;
    toast.innerHTML = `<i class="ph-bold ${icon}" style="font-size: 20px;"></i> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Initialize Page
document.addEventListener('DOMContentLoaded', () => {
    initEvents();
    loadLocations();
});

function initEvents() {
    // Search input
    const searchInput = document.getElementById('searchInput');
    let debounceTimer;
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                searchKeyword = e.target.value.trim();
                currentPage = 0;
                loadLocations();
            }, 350);
        });
    }

    // Clear filter
    const btnClearFilter = document.getElementById('btnClearFilter');
    if (btnClearFilter) {
        btnClearFilter.addEventListener('click', () => {
            if (searchInput) searchInput.value = '';
            searchKeyword = '';
            currentPage = 0;
            loadLocations();
        });
    }

    // Add location button
    const btnOpenAddModal = document.getElementById('btnOpenAddModal');
    if (btnOpenAddModal) {
        btnOpenAddModal.addEventListener('click', openAddLocationModal);
    }

    // Page size dropdown
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', (e) => {
            pageSize = parseInt(e.target.value) || 20;
            currentPage = 0;
            loadLocations();
        });
    }

    // Execute Delete
    const btnExecuteDelete = document.getElementById('btnExecuteDelete');
    if (btnExecuteDelete) {
        btnExecuteDelete.addEventListener('click', executePendingDelete);
    }
}

// ==============================================================
// 1. FETCH & RENDER LOCATIONS (IMAGE 1)
// ==============================================================
async function loadLocations() {
    const tbody = document.getElementById('locationTableBody');
    if (!tbody) return;

    try {
        let url = `/api/locations/search?page=${currentPage}&size=${pageSize}`;
        if (searchKeyword) {
            url += `&keyword=${encodeURIComponent(searchKeyword)}`;
        }

        const res = await fetch(url);
        if (!res.ok) throw new Error('Không thể tải danh sách địa điểm khám');

        const data = await res.json();
        const items = data.items || data.content || [];
        totalElements = data.total || data.totalElements || 0;

        locationDataMap.clear();
        items.forEach(loc => locationDataMap.set(loc.id, loc));

        renderLocationTable(items);
        renderPagination();

    } catch (err) {
        console.error(err);
        tbody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align: center; padding: 40px; color: #ef4444;">
                    <i class="ph ph-warning-circle" style="font-size: 24px; margin-bottom: 8px;"></i>
                    <p>${err.message || 'Lỗi khi tải dữ liệu'}</p>
                </td>
            </tr>
        `;
    }
}

function renderLocationTable(items) {
    const tbody = document.getElementById('locationTableBody');
    tbody.innerHTML = '';

    if (items.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align: center; padding: 50px; color: #94a3b8;">
                    <i class="ph ph-map-pin" style="font-size: 36px; margin-bottom: 10px; display: block;"></i>
                    <p style="font-size: 15px; font-weight: 500;">Không tìm thấy địa điểm khám nào</p>
                </td>
            </tr>
        `;
        return;
    }

    items.forEach(loc => {
        const isExpanded = expandedLocationIds.has(loc.id);

        // Find Ca 1 and Ca 2
        let ca1Text = '-';
        let ca2Text = '-';
        if (loc.shifts && loc.shifts.length > 0) {
            const s1 = loc.shifts.find(s => s.shiftNumber === 1) || loc.shifts[0];
            if (s1) ca1Text = `${s1.startTime}-${s1.endTime}`;

            const s2 = loc.shifts.find(s => s.shiftNumber === 2) || (loc.shifts.length > 1 ? loc.shifts[1] : null);
            if (s2) ca2Text = `${s2.startTime}-${s2.endTime}`;
        }

        // Parent Row
        const trParent = document.createElement('tr');
        trParent.className = `parent-row ${isExpanded ? 'is-expanded' : ''}`;
        trParent.id = `row-loc-${loc.id}`;

        trParent.innerHTML = `
            <td style="text-align: center; width: 44px;">
                <button type="button" class="btn-expand-row" onclick="toggleExpandLocation('${loc.id}')" title="Mở rộng danh sách hợp đồng">
                    <i class="ph ph-caret-right"></i>
                </button>
            </td>
            <td style="font-weight: 600; color: #0f172a;">${loc.code || '---'}</td>
            <td style="font-weight: 500; color: #1e293b;">${loc.name || '---'}</td>
            <td style="color: #334155;">${ca1Text}</td>
            <td style="color: #334155;">${ca2Text}</td>
            <td style="text-align: right; padding-right: 20px;">
                <div class="action-btn-group">
                    <button type="button" class="action-btn btn-add-contract" onclick="openAddContractModal('${loc.id}')" title="Thêm hợp đồng vào địa điểm">
                        <i class="ph-bold ph-plus"></i>
                    </button>
                    <button type="button" class="action-btn" onclick="openEditLocationModal('${loc.id}')" title="Chỉnh sửa địa điểm">
                        <i class="ph ph-pencil-simple"></i>
                    </button>
                    <button type="button" class="action-btn btn-delete" onclick="promptDeleteLocation('${loc.id}', '${escapeHtml(loc.name)}')" title="Xóa địa điểm">
                        <i class="ph ph-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(trParent);

        // Child Container Row (if expanded)
        if (isExpanded) {
            const trChild = document.createElement('tr');
            trChild.className = 'subtable-container-row';
            trChild.id = `subrow-loc-${loc.id}`;

            const tdChild = document.createElement('td');
            tdChild.colSpan = 6;
            tdChild.style.padding = '0';

            const subWrapper = document.createElement('div');
            subWrapper.className = 'subtable-wrapper';
            subWrapper.id = `subwrapper-${loc.id}`;
            subWrapper.innerHTML = `
                <div style="text-align: center; padding: 20px; color: #64748b;">
                    <i class="ph ph-spinner ph-spin" style="font-size: 20px;"></i> Đang tải danh sách hợp đồng...
                </div>
            `;

            tdChild.appendChild(subWrapper);
            trChild.appendChild(tdChild);
            tbody.appendChild(trChild);

            // Fetch and populate contracts
            fetchAndRenderLocationContracts(loc.id);
        }
    });
}

// Toggle Expand Location Row
async function toggleExpandLocation(locationId) {
    if (expandedLocationIds.has(locationId)) {
        expandedLocationIds.delete(locationId);
    } else {
        expandedLocationIds.add(locationId);
    }

    const items = Array.from(locationDataMap.values());
    renderLocationTable(items);
}

// Fetch & Render Sub-table of Contracts
async function fetchAndRenderLocationContracts(locationId) {
    const subWrapper = document.getElementById(`subwrapper-${locationId}`);
    if (!subWrapper) return;

    try {
        const res = await fetch(`/api/locations/${locationId}/contracts`);
        if (!res.ok) throw new Error('Không thể tải hợp đồng của địa điểm');

        const contracts = await res.json();
        locationContractsMap.set(locationId, contracts);

        renderSubtableContent(locationId, contracts);
    } catch (err) {
        console.error(err);
        subWrapper.innerHTML = `
            <div style="padding: 16px; color: #ef4444; font-size: 13px; text-align: center;">
                ${err.message || 'Lỗi khi tải hợp đồng'}
            </div>
        `;
    }
}

function renderSubtableContent(locationId, contracts) {
    const subWrapper = document.getElementById(`subwrapper-${locationId}`);
    if (!subWrapper) return;

    if (!contracts || contracts.length === 0) {
        subWrapper.innerHTML = `
            <div style="background: white; border: 1px dashed #cbd5e1; border-radius: 8px; padding: 24px; text-align: center; color: #64748b;">
                <i class="ph ph-files" style="font-size: 28px; margin-bottom: 6px; display: block; color: #94a3b8;"></i>
                <p style="margin-bottom: 12px; font-size: 13.5px;">Chưa có hợp đồng nào được gán vào địa điểm này.</p>
                <button type="button" class="btn-primary-green" style="padding: 6px 14px; font-size: 13px;" onclick="openAddContractModal('${locationId}')">
                    <i class="ph-bold ph-plus"></i> Thêm hợp đồng ngay
                </button>
            </div>
        `;
        return;
    }

    let rowsHtml = '';
    contracts.forEach(c => {
        const isChecked = c.active ? 'checked' : '';
        const shiftBadge = c.shiftConfig ? `<div style="font-size: 11.5px; color: #00684a; margin-top: 3px;"><i class="ph ph-clock"></i> ${escapeHtml(c.shiftConfig)}</div>` : '';

        rowsHtml += `
            <tr>
                <td style="font-weight: 600; color: #00684a; width: 200px;">
                    ${escapeHtml(c.contractCode || '---')}
                </td>
                <td>
                    <div style="font-weight: 500; color: #1e293b;">${escapeHtml(c.contractName || '---')}</div>
                    ${c.companyName ? `<div style="font-size: 12px; color: #64748b; margin-top: 2px;"><i class="ph ph-buildings"></i> ${escapeHtml(c.companyName)}</div>` : ''}
                    ${shiftBadge}
                </td>
                <td style="width: 170px;">
                    <label class="switch-status-toggle">
                        <input type="checkbox" ${isChecked} onchange="toggleContractStatus('${locationId}', '${c.id}', this)">
                        <span class="switch-slider"></span>
                    </label>
                </td>
                <td style="width: 80px; text-align: right; padding-right: 20px;">
                    <button type="button" class="action-btn btn-delete" onclick="promptRemoveContract('${locationId}', '${c.id}', '${escapeHtml(c.contractName)}')" title="Gỡ khỏi địa điểm">
                        <i class="ph ph-trash"></i>
                    </button>
                </td>
            </tr>
        `;
    });

    subWrapper.innerHTML = `
        <table class="subtable">
            <thead>
                <tr>
                    <th style="width: 200px;">Mã hợp đồng</th>
                    <th>Tên hợp đồng</th>
                    <th style="width: 170px;">Trạng thái áp dụng</th>
                    <th style="width: 80px; text-align: right; padding-right: 20px;">Tác vụ</th>
                </tr>
            </thead>
            <tbody>
                ${rowsHtml}
            </tbody>
        </table>
    `;
}

// Toggle contract active status
async function toggleContractStatus(locationId, contractLocationId, checkboxEl) {
    try {
        const res = await fetch(`/api/locations/${locationId}/contracts/${contractLocationId}/toggle-status`, {
            method: 'PATCH'
        });

        if (!res.ok) {
            checkboxEl.checked = !checkboxEl.checked;
            throw new Error('Lỗi khi cập nhật trạng thái áp dụng');
        }

        const updated = await res.json();
        showToast(updated.active ? 'Đã bật áp dụng hợp đồng tại địa điểm' : 'Đã tạm ngưng áp dụng hợp đồng tại địa điểm', 'success');

    } catch (err) {
        console.error(err);
        showToast(err.message || 'Có lỗi xảy ra', 'error');
    }
}

// Render Pagination
function renderPagination() {
    const info = document.getElementById('paginationInfo');
    const controls = document.getElementById('paginationControls');
    if (!info || !controls) return;

    const totalPages = Math.ceil(totalElements / pageSize) || 1;
    const start = totalElements === 0 ? 0 : (currentPage * pageSize + 1);
    const end = Math.min((currentPage + 1) * pageSize, totalElements);

    info.textContent = `Đang xem ${start} - ${end} trong ${totalElements}`;

    let html = '';
    // Previous button
    html += `
        <button type="button" class="btn-expand-row" style="width: 32px; height: 32px; border: 1px solid #cbd5e1;" 
                ${currentPage === 0 ? 'disabled style="opacity: 0.4; cursor: not-allowed;"' : `onclick="goToPage(${currentPage - 1})"`}>
            <i class="ph ph-caret-left"></i>
        </button>
    `;

    // Page buttons
    for (let p = 0; p < totalPages; p++) {
        if (totalPages <= 7 || p === 0 || p === totalPages - 1 || Math.abs(p - currentPage) <= 1) {
            const isActive = p === currentPage;
            const bg = isActive ? '#00684a' : 'white';
            const color = isActive ? 'white' : '#334155';
            html += `
                <button type="button" onclick="goToPage(${p})" 
                        style="width: 32px; height: 32px; border-radius: 6px; border: 1px solid ${isActive ? '#00684a' : '#cbd5e1'}; background: ${bg}; color: ${color}; font-weight: ${isActive ? '700' : '500'}; font-size: 13px; cursor: pointer;">
                    ${p + 1}
                </button>
            `;
        } else if (p === 1 && currentPage > 3 || p === totalPages - 2 && currentPage < totalPages - 4) {
            html += `<span style="padding: 0 4px; color: #94a3b8;">...</span>`;
        }
    }

    // Next button
    html += `
        <button type="button" class="btn-expand-row" style="width: 32px; height: 32px; border: 1px solid #cbd5e1;" 
                ${currentPage >= totalPages - 1 ? 'disabled style="opacity: 0.4; cursor: not-allowed;"' : `onclick="goToPage(${currentPage + 1})"`}>
            <i class="ph ph-caret-right"></i>
        </button>
    `;

    controls.innerHTML = html;
}

function goToPage(page) {
    currentPage = page;
    loadLocations();
}

// ==============================================================
// 2. MODAL THÊM MỚI / CẬP NHẬT ĐỊA ĐIỂM (IMAGE 2)
// ==============================================================
function openAddLocationModal() {
    document.getElementById('locationModalTitle').textContent = 'Thêm mới địa điểm khám';
    document.getElementById('locationId').value = '';
    document.getElementById('locationCode').value = '';
    document.getElementById('locationCode').placeholder = 'Mã tự sinh';
    document.getElementById('locationName').value = '';
    document.getElementById('locationActive').checked = true;

    // Default 2 shifts matching Image 2
    currentModalShifts = [
        { shiftNumber: 1, startTime: '06:00', endTime: '12:00' },
        { shiftNumber: 2, startTime: '13:00', endTime: '19:00' }
    ];
    renderModalShifts();

    document.getElementById('locationModal').classList.add('active');
}

async function openEditLocationModal(locationId) {
    try {
        const res = await fetch(`/api/locations/${locationId}`);
        if (!res.ok) throw new Error('Không tìm thấy thông tin địa điểm');

        const loc = await res.json();
        document.getElementById('locationModalTitle').textContent = 'Cập nhật địa điểm khám';
        document.getElementById('locationId').value = loc.id;
        document.getElementById('locationCode').value = loc.code || '';
        document.getElementById('locationName').value = loc.name || '';
        document.getElementById('locationActive').checked = loc.active;

        if (loc.shifts && loc.shifts.length > 0) {
            currentModalShifts = loc.shifts.map(s => ({
                shiftNumber: s.shiftNumber,
                startTime: s.startTime,
                endTime: s.endTime
            }));
        } else {
            currentModalShifts = [
                { shiftNumber: 1, startTime: '06:00', endTime: '12:00' },
                { shiftNumber: 2, startTime: '13:00', endTime: '19:00' }
            ];
        }
        renderModalShifts();

        document.getElementById('locationModal').classList.add('active');

    } catch (err) {
        console.error(err);
        showToast(err.message || 'Lỗi khi tải thông tin địa điểm', 'error');
    }
}

function closeLocationModal() {
    document.getElementById('locationModal').classList.remove('active');
}

function renderModalShifts() {
    const tbody = document.getElementById('modalShiftsTbody');
    tbody.innerHTML = '';

    currentModalShifts.forEach((shift, index) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td style="text-align: center; font-weight: 600; color: #475569;">
                ${index + 1}
            </td>
            <td>
                <input type="time" class="shifts-time-input" value="${shift.startTime || '06:00'}" 
                       onchange="updateModalShiftValue(${index}, 'startTime', this.value)">
            </td>
            <td>
                <input type="time" class="shifts-time-input" value="${shift.endTime || '12:00'}" 
                       onchange="updateModalShiftValue(${index}, 'endTime', this.value)">
            </td>
            <td style="text-align: center;">
                ${currentModalShifts.length > 1 ? `
                    <button type="button" class="action-btn btn-delete" onclick="removeModalShift(${index})" title="Xóa ca này">
                        <i class="ph ph-trash" style="font-size: 16px;"></i>
                    </button>
                ` : ''}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function updateModalShiftValue(index, field, value) {
    if (currentModalShifts[index]) {
        currentModalShifts[index][field] = value;
    }
}

function addNewShiftRow() {
    const nextNum = currentModalShifts.length + 1;
    currentModalShifts.push({
        shiftNumber: nextNum,
        startTime: '08:00',
        endTime: '17:00'
    });
    renderModalShifts();
}

function removeModalShift(index) {
    currentModalShifts.splice(index, 1);
    currentModalShifts.forEach((s, idx) => s.shiftNumber = idx + 1);
    renderModalShifts();
}

async function handleSaveLocation(e) {
    e.preventDefault();

    const id = document.getElementById('locationId').value;
    const code = document.getElementById('locationCode').value.trim();
    const name = document.getElementById('locationName').value.trim();
    const active = document.getElementById('locationActive').checked;

    if (!name) {
        showToast('Vui lòng nhập tên địa điểm khám', 'error');
        return;
    }

    const payload = {
        code: code || null,
        name: name,
        active: active,
        shifts: currentModalShifts.map((s, idx) => ({
            shiftNumber: idx + 1,
            shiftName: `Ca ${idx + 1}`,
            startTime: s.startTime || '06:00',
            endTime: s.endTime || '12:00'
        }))
    };

    const btn = document.getElementById('btnSaveLocation');
    btn.disabled = true;
    btn.textContent = 'Đang lưu...';

    try {
        const url = id ? `/api/locations/${id}` : '/api/locations';
        const method = id ? 'PUT' : 'POST';

        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            throw new Error(errData.message || 'Lỗi khi lưu địa điểm khám');
        }

        showToast(id ? 'Cập nhật địa điểm thành công!' : 'Tạo mới địa điểm thành công!', 'success');
        closeLocationModal();
        loadLocations();

    } catch (err) {
        console.error(err);
        showToast(err.message || 'Có lỗi xảy ra', 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Lưu';
    }
}

// ==============================================================
// 3. MODAL THÊM HỢP ĐỒNG VÀO ĐỊA ĐIỂM
// ==============================================================
async function openAddContractModal(locationId) {
    targetLocationForAssign = locationDataMap.get(locationId);
    if (!targetLocationForAssign) return;

    document.getElementById('assignTargetLocationId').value = locationId;
    document.getElementById('addContractTargetLocationName').textContent = 
        `Địa điểm: ${targetLocationForAssign.code || ''} - ${targetLocationForAssign.name || ''}`;
    document.getElementById('contractSearchFilter').value = '';
    document.getElementById('assignContractActive').checked = true;
    selectedContractIdForAssign = null;

    // Render shifts checkbox configuration
    const shiftsBox = document.getElementById('contractShiftsConfigBox');
    const shifts = targetLocationForAssign.shifts || [];
    if (shifts.length === 0) {
        shiftsBox.innerHTML = `
            <div style="font-size: 13px; color: #64748b;">
                Địa điểm này chưa thiết lập ca riêng, áp dụng ca tiêu chuẩn (Ca 1: 06:00-12:00, Ca 2: 13:00-18:00).
            </div>
        `;
    } else {
        let shiftsCheckHtml = '<div style="display: flex; flex-direction: column; gap: 8px;">';
        shifts.forEach((s, idx) => {
            shiftsCheckHtml += `
                <label style="display: flex; align-items: center; gap: 8px; font-size: 13.5px; color: #1e293b; cursor: pointer;">
                    <input type="checkbox" name="assignContractShiftCheck" value="Ca ${s.shiftNumber} (${s.startTime}-${s.endTime})" checked style="width: 16px; height: 16px; accent-color: #00684a;">
                    <span><strong>Ca ${s.shiftNumber}:</strong> ${s.startTime} - ${s.endTime}</span>
                </label>
            `;
        });
        shiftsCheckHtml += '</div>';
        shiftsBox.innerHTML = shiftsCheckHtml;
    }

    // Load contracts
    await loadContractsForPicker();

    document.getElementById('addContractModal').classList.add('active');
}

function closeAddContractModal() {
    document.getElementById('addContractModal').classList.remove('active');
}

async function loadContractsForPicker() {
    const container = document.getElementById('contractPickerList');
    container.innerHTML = `<div style="text-align: center; padding: 20px; color: #64748b;">Đang tải danh sách hợp đồng...</div>`;

    try {
        if (allContracts.length === 0) {
            const res = await fetch('/api/contracts/search?page=0&size=100');
            if (res.ok) {
                const data = await res.json();
                allContracts = data.items || data.content || [];
            }
        }
        renderContractPickerList(allContracts);
    } catch (e) {
        console.error(e);
        container.innerHTML = `<div style="padding: 14px; color: #ef4444;">Lỗi khi tải hợp đồng</div>`;
    }
}

function renderContractPickerList(list) {
    const container = document.getElementById('contractPickerList');
    container.innerHTML = '';

    if (list.length === 0) {
        container.innerHTML = `<div style="padding: 20px; text-align: center; color: #94a3b8;">Không tìm thấy hợp đồng nào</div>`;
        return;
    }

    list.forEach(c => {
        const item = document.createElement('div');
        const isSelected = selectedContractIdForAssign === c.id;
        item.className = `contract-pick-item ${isSelected ? 'selected' : ''}`;
        item.onclick = () => selectContractForAssign(c.id, item);

        item.innerHTML = `
            <input type="radio" name="selectedContractRadio" ${isSelected ? 'checked' : ''} style="accent-color: #00684a;">
            <div style="flex: 1;">
                <div style="display: flex; align-items: center; gap: 8px;">
                    <span style="font-weight: 700; color: #00684a; font-size: 13px;">${escapeHtml(c.contractCode || '---')}</span>
                </div>
                <div style="font-size: 13px; font-weight: 600; color: #0f172a;">${escapeHtml(c.contractName || '---')}</div>
                ${c.companyName ? `<div style="font-size: 11.5px; color: #64748b;"><i class="ph ph-buildings"></i> ${escapeHtml(c.companyName)}</div>` : ''}
            </div>
        `;
        container.appendChild(item);
    });
}

function selectContractForAssign(contractId, rowEl) {
    selectedContractIdForAssign = contractId;
    document.querySelectorAll('.contract-pick-item').forEach(el => {
        el.classList.remove('selected');
        const r = el.querySelector('input[type="radio"]');
        if (r) r.checked = false;
    });
    rowEl.classList.add('selected');
    const radio = rowEl.querySelector('input[type="radio"]');
    if (radio) radio.checked = true;
}

function filterContractPickerList(keyword) {
    const kw = keyword.toLowerCase().trim();
    if (!kw) {
        renderContractPickerList(allContracts);
        return;
    }
    const filtered = allContracts.filter(c => 
        (c.contractCode && c.contractCode.toLowerCase().includes(kw)) ||
        (c.contractName && c.contractName.toLowerCase().includes(kw)) ||
        (c.companyName && c.companyName.toLowerCase().includes(kw))
    );
    renderContractPickerList(filtered);
}

async function handleAssignContractSubmit(e) {
    e.preventDefault();

    const locationId = document.getElementById('assignTargetLocationId').value;
    if (!selectedContractIdForAssign) {
        showToast('Vui lòng chọn một hợp đồng để thêm vào địa điểm', 'error');
        return;
    }

    const active = document.getElementById('assignContractActive').checked;

    // Collect selected shift names
    const shiftCheckboxes = document.querySelectorAll('input[name="assignContractShiftCheck"]:checked');
    const selectedShifts = Array.from(shiftCheckboxes).map(cb => cb.value);
    const shiftConfig = selectedShifts.join(', ');

    const payload = {
        contractId: selectedContractIdForAssign,
        active: active,
        shiftConfig: shiftConfig || null
    };

    const btn = document.getElementById('btnSubmitAssignContract');
    btn.disabled = true;
    btn.textContent = 'Đang thêm...';

    try {
        const res = await fetch(`/api/locations/${locationId}/contracts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            throw new Error(errData.message || 'Lỗi khi gán hợp đồng vào địa điểm');
        }

        showToast('Đã thêm hợp đồng vào địa điểm thành công!', 'success');
        closeAddContractModal();

        // Expand row and reload its contracts
        expandedLocationIds.add(locationId);
        loadLocations();

    } catch (err) {
        console.error(err);
        showToast(err.message || 'Có lỗi xảy ra', 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Xác nhận thêm';
    }
}

// ==============================================================
// 4. CONFIRM DELETE OPERATIONS
// ==============================================================
function promptDeleteLocation(locationId, locationName) {
    document.getElementById('deleteModalTitle').textContent = 'Xác nhận xóa địa điểm khám?';
    document.getElementById('deleteModalDesc').textContent = 
        `Bạn có chắc chắn muốn xóa địa điểm "${locationName}"? Các liên kết hợp đồng thuộc địa điểm này sẽ bị ảnh hưởng.`;

    pendingDeleteAction = async () => {
        const res = await fetch(`/api/locations/${locationId}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Không thể xóa địa điểm khám');
        showToast('Đã xóa địa điểm thành công!', 'success');
        loadLocations();
    };

    document.getElementById('deleteConfirmModal').classList.add('active');
}

function promptRemoveContract(locationId, contractLocationId, contractName) {
    document.getElementById('deleteModalTitle').textContent = 'Gỡ hợp đồng khỏi địa điểm?';
    document.getElementById('deleteModalDesc').textContent = 
        `Bạn có chắc chắn muốn gỡ hợp đồng "${contractName}" khỏi địa điểm này?`;

    pendingDeleteAction = async () => {
        const res = await fetch(`/api/locations/${locationId}/contracts/${contractLocationId}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Không thể gỡ hợp đồng khỏi địa điểm');
        showToast('Đã gỡ hợp đồng khỏi địa điểm thành công!', 'success');
        fetchAndRenderLocationContracts(locationId);
    };

    document.getElementById('deleteConfirmModal').classList.add('active');
}

function closeDeleteModal() {
    document.getElementById('deleteConfirmModal').classList.remove('active');
    pendingDeleteAction = null;
}

async function executePendingDelete() {
    if (!pendingDeleteAction) return;
    const btn = document.getElementById('btnExecuteDelete');
    btn.disabled = true;
    btn.textContent = 'Đang xử lý...';

    try {
        await pendingDeleteAction();
        closeDeleteModal();
    } catch (err) {
        console.error(err);
        showToast(err.message || 'Lỗi khi xử lý thao tác', 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Xác nhận xóa';
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;')
              .replace(/'/g, '&#039;');
}
