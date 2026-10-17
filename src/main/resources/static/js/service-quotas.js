/**
 * ==============================================================
 * ĐỊNH MỨC DỊCH VỤ - JAVASCRIPT CONTROLLER (SERVICE QUOTAS JS)
 * ==============================================================
 */

// State
let currentPage = 0;
let pageSize = 20;
let searchKeyword = '';
let serviceTypeFilter = '';
let totalElements = 0;
let servicesMap = new Map();
let expandedServiceIds = new Set();
let serviceQuotasMap = new Map();

// Modal State
let activeLocations = []; // Only active locations that have configured shifts
let allServices = [];
let editingQuotaId = null;
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
    loadServicesWithQuotas();
    preloadAuxiliaryData();
});

function initEvents() {
    // Search input with debounce
    const searchInput = document.getElementById('searchInput');
    let debounceTimer;
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                searchKeyword = e.target.value.trim();
                currentPage = 0;
                loadServicesWithQuotas();
            }, 350);
        });
    }

    // Type filter
    const typeFilter = document.getElementById('typeFilter');
    if (typeFilter) {
        typeFilter.addEventListener('change', (e) => {
            serviceTypeFilter = e.target.value;
            currentPage = 0;
            loadServicesWithQuotas();
        });
    }

    // Clear filter
    const btnClearFilter = document.getElementById('btnClearFilter');
    if (btnClearFilter) {
        btnClearFilter.addEventListener('click', () => {
            if (searchInput) searchInput.value = '';
            if (typeFilter) typeFilter.value = '';
            searchKeyword = '';
            serviceTypeFilter = '';
            currentPage = 0;
            loadServicesWithQuotas();
        });
    }

    // Page size dropdown
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', (e) => {
            pageSize = parseInt(e.target.value) || 20;
            currentPage = 0;
            loadServicesWithQuotas();
        });
    }

    // Execute Delete Button
    const btnExecuteDelete = document.getElementById('btnExecuteDelete');
    if (btnExecuteDelete) {
        btnExecuteDelete.addEventListener('click', executePendingDelete);
    }
}

// Preload auxiliary data: ONLY active locations with shifts
async function preloadAuxiliaryData() {
    try {
        const [locRes, srvRes] = await Promise.all([
            fetch('/api/locations/active-with-shifts'),
            fetch('/api/services/search?page=0&size=200')
        ]);

        if (locRes.ok) {
            activeLocations = await locRes.json();
        }
        if (srvRes.ok) {
            const data = await srvRes.json();
            allServices = data.items || data.content || [];
        }
    } catch (e) {
        console.error('Error preloading auxiliary data:', e);
    }
}

// ==============================================================
// 1. FETCH & RENDER SERVICES WITH QUOTAS (PARENT TABLE)
// ==============================================================
async function loadServicesWithQuotas() {
    const tbody = document.getElementById('quotaTableBody');
    if (!tbody) return;

    try {
        let url = `/api/service-quotas/services?page=${currentPage}&size=${pageSize}`;
        if (searchKeyword) {
            url += `&keyword=${encodeURIComponent(searchKeyword)}`;
        }
        if (serviceTypeFilter) {
            url += `&serviceType=${encodeURIComponent(serviceTypeFilter)}`;
        }

        const res = await fetch(url);
        if (!res.ok) throw new Error('Không thể tải danh sách định mức dịch vụ');

        const data = await res.json();
        const items = data.items || data.content || [];
        totalElements = data.total || data.totalElements || 0;

        servicesMap.clear();
        items.forEach(s => servicesMap.set(s.id, s));

        // Auto-expand first item if it's the first page load
        if (currentPage === 0 && !searchKeyword && items.length > 0 && expandedServiceIds.size === 0) {
            expandedServiceIds.add(items[0].id);
        }

        renderServicesTable(items);
        renderPagination();

    } catch (err) {
        console.error(err);
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; padding: 40px; color: #ef4444;">
                    <i class="ph ph-warning-circle" style="font-size: 24px; margin-bottom: 8px;"></i>
                    <p>${err.message || 'Lỗi khi tải dữ liệu'}</p>
                </td>
            </tr>
        `;
    }
}

function renderServicesTable(items) {
    const tbody = document.getElementById('quotaTableBody');
    tbody.innerHTML = '';

    if (items.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; padding: 50px; color: #94a3b8;">
                    <i class="ph ph-clipboard-text" style="font-size: 36px; margin-bottom: 10px; display: block;"></i>
                    <p style="font-size: 15px; font-weight: 500;">Không tìm thấy dịch vụ nào</p>
                </td>
            </tr>
        `;
        return;
    }

    items.forEach(s => {
        const isExpanded = expandedServiceIds.has(s.id);
        const formattedPrice = formatCurrency(s.price);

        const activeBadge = s.activeQuotaCount > 0 
            ? `<span class="badge-active-quota">${s.activeQuotaCount} đang áp dụng</span>` 
            : '';

        // Parent Row
        const trParent = document.createElement('tr');
        trParent.className = `parent-row ${isExpanded ? 'is-expanded' : ''}`;
        trParent.id = `row-srv-${s.id}`;

        trParent.innerHTML = `
            <td style="text-align: center; width: 44px;">
                <button type="button" class="btn-expand-row" onclick="toggleExpandService('${s.id}')" title="Mở rộng danh sách định mức">
                    <i class="ph ph-caret-right"></i>
                </button>
            </td>
            <td style="font-weight: 500; color: #475569;">${escapeHtml(s.code || '---')}</td>
            <td style="font-weight: 700; color: #0f172a;">${escapeHtml(s.name || '---')}</td>
            <td style="color: #475569;">${escapeHtml(s.serviceTypeLabel || '---')}</td>
            <td style="color: #1e293b; font-weight: 500;">${formattedPrice}</td>
            <td>${activeBadge}</td>
            <td style="text-align: right; padding-right: 20px;">
                <button type="button" class="action-btn btn-add-quota" onclick="openAddQuotaModal('${s.id}')" title="Thiết lập định mức cho dịch vụ này">
                    <i class="ph-bold ph-plus"></i>
                </button>
            </td>
        `;
        tbody.appendChild(trParent);

        // Child Container Row (if expanded)
        if (isExpanded) {
            const trChild = document.createElement('tr');
            trChild.className = 'subtable-container-row';
            trChild.id = `subrow-srv-${s.id}`;

            const tdChild = document.createElement('td');
            tdChild.colSpan = 7;
            tdChild.style.padding = '0';

            const subWrapper = document.createElement('div');
            subWrapper.className = 'subtable-wrapper';
            subWrapper.id = `subwrapper-${s.id}`;
            subWrapper.innerHTML = `
                <div style="text-align: center; padding: 20px; color: #64748b;">
                    <i class="ph ph-spinner ph-spin" style="font-size: 20px;"></i> Đang tải định mức tại các địa điểm...
                </div>
            `;

            tdChild.appendChild(subWrapper);
            trChild.appendChild(tdChild);
            tbody.appendChild(trChild);

            // Fetch and populate quotas
            fetchAndRenderQuotas(s.id);
        }
    });
}

// Toggle Expand Service Row
function toggleExpandService(serviceId) {
    if (expandedServiceIds.has(serviceId)) {
        expandedServiceIds.delete(serviceId);
    } else {
        expandedServiceIds.add(serviceId);
    }

    const items = Array.from(servicesMap.values());
    renderServicesTable(items);
}

// Fetch & Render Sub-table of Quotas
async function fetchAndRenderQuotas(serviceId) {
    const subWrapper = document.getElementById(`subwrapper-${serviceId}`);
    if (!subWrapper) return;

    try {
        const res = await fetch(`/api/service-quotas/services/${serviceId}/quotas`);
        if (!res.ok) throw new Error('Không thể tải định mức dịch vụ');

        const quotas = await res.json();
        serviceQuotasMap.set(serviceId, quotas);

        renderSubtableContent(serviceId, quotas);
    } catch (err) {
        console.error(err);
        subWrapper.innerHTML = `
            <div style="padding: 16px; color: #ef4444; font-size: 13px; text-align: center;">
                ${err.message || 'Lỗi khi tải định mức'}
            </div>
        `;
    }
}

function renderSubtableContent(serviceId, quotas) {
    const subWrapper = document.getElementById(`subwrapper-${serviceId}`);
    if (!subWrapper) return;

    if (!quotas || quotas.length === 0) {
        subWrapper.innerHTML = `
            <div style="background: white; border: 1px dashed #cbd5e1; border-radius: 8px; padding: 24px; text-align: center; color: #64748b;">
                <i class="ph ph-sliders" style="font-size: 28px; margin-bottom: 6px; display: block; color: #94a3b8;"></i>
                <p style="margin-bottom: 12px; font-size: 13.5px;">Chưa có định mức nào được thiết lập cho dịch vụ này.</p>
                <button type="button" class="btn-primary-green" style="padding: 6px 14px; font-size: 13px;" onclick="openAddQuotaModal('${serviceId}')">
                    <i class="ph-bold ph-plus"></i> Thiết lập định mức ngay
                </button>
            </div>
        `;
        return;
    }

    let rowsHtml = '';
    quotas.forEach(q => {
        const isChecked = q.active ? 'checked' : '';
        const dateRangeStr = formatDateRange(q.validFrom, q.validTo);

        // Contract column info
        const contractInfo = q.contractCode
            ? `<div style="display: flex; flex-direction: column; gap: 2px;">
                 <span style="font-weight: 600; color: #0369a1; font-size: 12.5px;">${escapeHtml(q.contractCode)}</span>
                 <span style="font-size: 12px; color: #64748b;">${escapeHtml(q.contractName || '')}</span>
               </div>`
            : `<span style="color: #94a3b8; font-style: italic; font-size: 12.5px;">Áp dụng chung</span>`;

        // Shift breakdown details
        let shiftsBreakdown = '';
        if (q.shiftDetails) {
            try {
                const parsed = JSON.parse(q.shiftDetails);
                if (Array.isArray(parsed) && parsed.length > 0) {
                    shiftsBreakdown = parsed.map(item => `
                        <span style="display: inline-block; background: #f1f5f9; color: #334155; font-size: 11.5px; padding: 2px 7px; border-radius: 4px; margin-right: 4px; margin-bottom: 2px;">
                            ${escapeHtml(item.shiftName)}: <b>${item.quantity}</b>
                        </span>
                    `).join('');
                }
            } catch (e) {}
        }
        if (!shiftsBreakdown && (q.shift1Quantity > 0 || q.shift2Quantity > 0)) {
            shiftsBreakdown = `
                <span style="display: inline-block; background: #f1f5f9; color: #334155; font-size: 11.5px; padding: 2px 7px; border-radius: 4px; margin-right: 4px;">
                    Ca 1: <b>${q.shift1Quantity}</b>
                </span>
                <span style="display: inline-block; background: #f1f5f9; color: #334155; font-size: 11.5px; padding: 2px 7px; border-radius: 4px;">
                    Ca 2: <b>${q.shift2Quantity}</b>
                </span>
            `;
        }

        rowsHtml += `
            <tr>
                <td style="font-weight: 600; color: #1e293b; width: 220px;">
                    ${escapeHtml(q.locationName || q.locationCode || '---')}
                </td>
                <td style="width: 220px;">
                    ${contractInfo}
                </td>
                <td style="font-weight: 700; color: #00684a; width: 130px; font-size: 14px;">
                    ${q.maxQuantity} <span style="font-weight: 400; font-size: 12px; color: #64748b;">lượt</span>
                </td>
                <td style="width: 220px;">
                    <div style="font-size: 12.5px; color: #334155; margin-bottom: 3px; font-weight: 500;">Theo ca</div>
                    <div style="display: flex; flex-wrap: wrap; gap: 4px;">${shiftsBreakdown}</div>
                </td>
                <td style="color: #475569; width: 190px; font-size: 12.5px;">
                    ${dateRangeStr}
                </td>
                <td style="width: 130px;">
                    <label class="switch-status-toggle">
                        <input type="checkbox" ${isChecked} onchange="toggleQuotaStatus('${serviceId}', '${q.id}', this)">
                        <span class="switch-slider"></span>
                    </label>
                </td>
                <td style="width: 90px; text-align: right; padding-right: 20px;">
                    <div class="action-btn-group">
                        <button type="button" class="action-btn" onclick="openEditQuotaModal('${q.id}')" title="Sửa định mức">
                            <i class="ph ph-pencil-simple"></i>
                        </button>
                        <button type="button" class="action-btn btn-delete" onclick="promptDeleteQuota('${serviceId}', '${q.id}', '${escapeHtml(q.locationName)}')" title="Xóa định mức">
                            <i class="ph ph-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    });

    subWrapper.innerHTML = `
        <table class="subtable">
            <thead>
                <tr>
                    <th style="width: 220px;">Địa điểm</th>
                    <th style="width: 220px;">Hợp đồng</th>
                    <th style="width: 130px;">Tổng định mức</th>
                    <th style="width: 220px;">Phân bổ ca</th>
                    <th style="width: 190px;">Thời gian áp dụng</th>
                    <th style="width: 130px;">Trạng thái áp dụng</th>
                    <th style="width: 90px; text-align: right; padding-right: 20px;">Tác vụ</th>
                </tr>
            </thead>
            <tbody>
                ${rowsHtml}
            </tbody>
        </table>
    `;
}

// Toggle Quota status
async function toggleQuotaStatus(serviceId, quotaId, checkboxEl) {
    try {
        const res = await fetch(`/api/service-quotas/${quotaId}/toggle-status`, {
            method: 'PATCH'
        });

        if (!res.ok) {
            checkboxEl.checked = !checkboxEl.checked;
            throw new Error('Lỗi khi cập nhật trạng thái định mức');
        }

        const updated = await res.json();
        showToast(updated.active ? 'Đã bật áp dụng định mức' : 'Đã tạm dừng áp dụng định mức', 'success');
        
        // Reload parent service counts
        loadServicesWithQuotas();

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
    loadServicesWithQuotas();
}

// ==============================================================
// 2. MODAL THIẾT LẬP ĐỊNH MỨC DỊCH VỤ
// ==============================================================
async function openAddQuotaModal(preselectedServiceId = null) {
    editingQuotaId = null;
    document.getElementById('quotaModalTitle').textContent = 'Thiết lập định mức dịch vụ';
    document.getElementById('editingQuotaId').value = '';

    // Populate services dropdown
    populateServiceSelect(preselectedServiceId);
    document.getElementById('serviceSelect').disabled = false;

    // Refresh active locations with shifts if needed
    if (activeLocations.length === 0) {
        try {
            const res = await fetch('/api/locations/active-with-shifts');
            if (res.ok) activeLocations = await res.json();
        } catch (e) {
            console.error(e);
        }
    }

    // Populate locations dropdown (ONLY active locations with shifts)
    populateLocationSelect();
    document.getElementById('locationSelect').disabled = false;
    document.getElementById('locationSelect').value = '';

    // Reset contract dropdown
    const contractSelect = document.getElementById('contractSelect');
    contractSelect.innerHTML = '<option value="">-- Chọn hợp đồng áp dụng (hoặc để áp dụng chung) --</option>';
    contractSelect.disabled = true;
    document.getElementById('contractSelectNotice').textContent = 'Vui lòng chọn địa điểm khám để tải danh sách hợp đồng';

    // Reset dynamic shifts container - NO hardcoded values!
    const shiftsContainer = document.getElementById('dynamicShiftsContainer');
    shiftsContainer.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 18px 10px; color: #94a3b8; font-size: 13px;">
            <i class="ph ph-calendar-blank" style="font-size: 20px; display: block; margin-bottom: 4px;"></i>
            Vui lòng chọn địa điểm khám để cấu hình số lượt theo ca
        </div>
    `;
    calculateTotalAllocation();

    // Default dates: Today -> +3 months
    const today = new Date();
    const future = new Date();
    future.setMonth(future.getMonth() + 3);
    document.getElementById('validFrom').value = today.toISOString().split('T')[0];
    document.getElementById('validTo').value = future.toISOString().split('T')[0];

    document.getElementById('quotaActive').checked = true;

    document.getElementById('quotaModal').classList.add('active');
}

async function openEditQuotaModal(quotaId) {
    try {
        const res = await fetch(`/api/service-quotas/${quotaId}`);
        if (!res.ok) throw new Error('Không tìm thấy định mức dịch vụ');

        const q = await res.json();
        editingQuotaId = q.id;
        document.getElementById('quotaModalTitle').textContent = 'Cập nhật định mức dịch vụ';
        document.getElementById('editingQuotaId').value = q.id;

        // Populate service select and set value (disabled)
        populateServiceSelect(q.serviceId);
        document.getElementById('serviceSelect').disabled = true;

        // Ensure activeLocations is loaded
        if (activeLocations.length === 0) {
            const locRes = await fetch('/api/locations/active-with-shifts');
            if (locRes.ok) activeLocations = await locRes.json();
        }

        // Populate locations select and set value
        populateLocationSelect(q.locationId);
        document.getElementById('locationSelect').disabled = false;

        // Parse existing shifts allocation
        const shiftValues = {};
        if (q.shiftDetails) {
            try {
                const parsed = JSON.parse(q.shiftDetails);
                if (Array.isArray(parsed)) {
                    parsed.forEach(item => {
                        if (item.shiftNumber) {
                            shiftValues[item.shiftNumber] = item.quantity;
                        }
                    });
                }
            } catch (e) {
                console.warn('Could not parse shiftDetails JSON', e);
            }
        }
        // Fallback if shiftDetails was empty
        if (Object.keys(shiftValues).length === 0) {
            if (q.shift1Quantity) shiftValues[1] = q.shift1Quantity;
            if (q.shift2Quantity) shiftValues[2] = q.shift2Quantity;
        }

        // Trigger handleLocationChange to load contracts and populate dynamic shifts
        await handleLocationChange(q.locationId, q.contractId, shiftValues);

        // Dates
        document.getElementById('validFrom').value = q.validFrom || '';
        document.getElementById('validTo').value = q.validTo || '';

        // Active
        document.getElementById('quotaActive').checked = q.active;

        document.getElementById('quotaModal').classList.add('active');

    } catch (err) {
        console.error(err);
        showToast(err.message || 'Lỗi khi tải thông tin định mức', 'error');
    }
}

function closeQuotaModal() {
    document.getElementById('quotaModal').classList.remove('active');
    document.getElementById('serviceSelect').disabled = false;
    editingQuotaId = null;
}

function populateServiceSelect(selectedId = null) {
    const select = document.getElementById('serviceSelect');
    select.innerHTML = '<option value="">Chọn dịch vụ y tế...</option>';

    allServices.forEach(s => {
        const opt = document.createElement('option');
        opt.value = s.id;
        opt.textContent = `${s.name} (${s.code || '---'})`;
        if (selectedId && s.id === selectedId) {
            opt.selected = true;
        }
        select.appendChild(opt);
    });
}

function populateLocationSelect(selectedLocationId = null) {
    const select = document.getElementById('locationSelect');
    select.innerHTML = '<option value="">-- Chọn địa điểm khám --</option>';

    if (!activeLocations || activeLocations.length === 0) {
        select.innerHTML = '<option value="">Không có địa điểm nào có ca hoạt động</option>';
        return;
    }

    activeLocations.forEach(loc => {
        const opt = document.createElement('option');
        opt.value = loc.id;
        const shiftCount = loc.shifts ? loc.shifts.length : 0;
        opt.textContent = `${loc.name} (${loc.code || '---'}) - ${shiftCount} ca làm việc`;
        if (selectedLocationId && loc.id === selectedLocationId) {
            opt.selected = true;
        }
        select.appendChild(opt);
    });
}

/**
 * Xử lý khi người dùng chọn Địa điểm:
 * 1. Tải danh sách hợp đồng gán với địa điểm đó
 * 2. Render động danh sách ca làm việc thực tế của địa điểm đó (bỏ hoàn toàn fix cứng mặc định 60, 40)
 */
async function handleLocationChange(locationId, preserveContractId = null, existingShiftValues = null) {
    const contractSelect = document.getElementById('contractSelect');
    const noticeEl = document.getElementById('contractSelectNotice');
    const shiftsContainer = document.getElementById('dynamicShiftsContainer');

    if (!locationId) {
        contractSelect.innerHTML = '<option value="">-- Chọn hợp đồng áp dụng (hoặc để áp dụng chung) --</option>';
        contractSelect.disabled = true;
        if (noticeEl) noticeEl.textContent = 'Vui lòng chọn địa điểm khám để tải danh sách hợp đồng';
        shiftsContainer.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 18px 10px; color: #94a3b8; font-size: 13px;">
                <i class="ph ph-calendar-blank" style="font-size: 20px; display: block; margin-bottom: 4px;"></i>
                Vui lòng chọn địa điểm khám để cấu hình số lượt theo ca
            </div>
        `;
        calculateTotalAllocation();
        return;
    }

    const loc = activeLocations.find(l => l.id === locationId);
    if (!loc) {
        console.warn('Location not found in active list:', locationId);
        return;
    }

    // 1. Fetch contracts of this location
    contractSelect.disabled = true;
    contractSelect.innerHTML = '<option value="">Đang tải danh sách hợp đồng...</option>';
    if (noticeEl) noticeEl.textContent = 'Đang tải hợp đồng...';

    try {
        const res = await fetch(`/api/locations/${locationId}/contracts`);
        if (res.ok) {
            const contracts = await res.json();
            contractSelect.innerHTML = '<option value="">-- Chọn hợp đồng áp dụng (hoặc để áp dụng chung) --</option>';
            
            contracts.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.contractId;
                opt.textContent = `${c.contractCode} - ${c.contractName} (${c.companyName || ''})`;
                if (preserveContractId && c.contractId === preserveContractId) {
                    opt.selected = true;
                }
                contractSelect.appendChild(opt);
            });

            contractSelect.disabled = false;
            if (noticeEl) {
                noticeEl.textContent = contracts.length > 0 
                    ? `Đã tải ${contracts.length} hợp đồng gắn với địa điểm này`
                    : 'Địa điểm này chưa gắn hợp đồng nào (sẽ áp dụng chung)';
            }
        } else {
            contractSelect.innerHTML = '<option value="">-- Chọn hợp đồng áp dụng (hoặc để áp dụng chung) --</option>';
            contractSelect.disabled = false;
            if (noticeEl) noticeEl.textContent = 'Không tải được danh sách hợp đồng';
        }
    } catch (err) {
        console.error('Error fetching location contracts:', err);
        contractSelect.innerHTML = '<option value="">-- Chọn hợp đồng áp dụng (hoặc để áp dụng chung) --</option>';
        contractSelect.disabled = false;
        if (noticeEl) noticeEl.textContent = 'Lỗi kết nối khi tải hợp đồng';
    }

    // 2. Render dynamic shifts of this location
    shiftsContainer.innerHTML = '';
    const shifts = loc.shifts || [];

    if (shifts.length === 0) {
        shiftsContainer.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 18px 10px; color: #ef4444; font-size: 13px;">
                <i class="ph ph-warning-circle" style="font-size: 20px; display: block; margin-bottom: 4px;"></i>
                Địa điểm này chưa được cấu hình ca làm việc nào! Vui lòng cấu hình ca trong quản lý Địa điểm.
            </div>
        `;
        calculateTotalAllocation();
        return;
    }

    shifts.forEach(shift => {
        const shiftNum = shift.shiftNumber;
        const shiftName = shift.shiftName || `Ca ${shiftNum}`;
        const timeRange = (shift.startTime && shift.endTime) ? `(${shift.startTime} - ${shift.endTime})` : '';

        // Value: if existingShiftValues provided (editing), use it; otherwise EMPTY string! NO HARDCODED 60 OR 40!
        let initialVal = '';
        if (existingShiftValues && existingShiftValues[shiftNum] !== undefined) {
            initialVal = existingShiftValues[shiftNum];
        }

        const shiftCol = document.createElement('div');
        shiftCol.innerHTML = `
            <label style="display: block; font-size: 13px; font-weight: 600; color: #1e293b; margin-bottom: 6px;">
                ${escapeHtml(shiftName)} <span style="font-weight: 400; color: #64748b; font-size: 12px; margin-left: 4px;">${escapeHtml(timeRange)}</span>
            </label>
            <div class="shift-input-wrap">
                <input type="number" 
                       min="0" 
                       class="modal-form-input dynamic-shift-input" 
                       data-shift-num="${shiftNum}" 
                       data-shift-name="${escapeHtml(shiftName)}"
                       data-start="${escapeHtml(shift.startTime || '')}"
                       data-end="${escapeHtml(shift.endTime || '')}"
                       placeholder="Nhập số lượt" 
                       value="${initialVal !== '' ? initialVal : ''}" 
                       oninput="calculateTotalAllocation()">
                <span class="shift-unit-label">lượt</span>
            </div>
        `;
        shiftsContainer.appendChild(shiftCol);
    });

    calculateTotalAllocation();
}

/**
 * Tính tổng định mức phân bổ tự động dựa trên các ô nhập ca thực tế
 */
function calculateTotalAllocation() {
    let total = 0;
    const inputs = document.querySelectorAll('.dynamic-shift-input');
    inputs.forEach(input => {
        const val = parseInt(input.value);
        if (!isNaN(val) && val > 0) {
            total += val;
        }
    });

    const badge = document.getElementById('totalAllocatedBadge');
    if (badge) {
        badge.textContent = `Tổng phân bổ: ${total} lượt`;
    }
}

/**
 * Lưu cấu hình định mức dịch vụ
 */
async function handleSaveQuota(e) {
    e.preventDefault();

    const serviceId = document.getElementById('serviceSelect').value;
    if (!serviceId) {
        showToast('Vui lòng chọn dịch vụ áp dụng', 'error');
        return;
    }

    const locationId = document.getElementById('locationSelect').value;
    if (!locationId) {
        showToast('Vui lòng chọn địa điểm khám', 'error');
        return;
    }

    const contractId = document.getElementById('contractSelect').value || null;

    // Collect dynamic shifts
    const shiftInputs = document.querySelectorAll('.dynamic-shift-input');
    const shiftDetailsArray = [];
    let s1 = 0;
    let s2 = 0;
    let totalMax = 0;

    shiftInputs.forEach(input => {
        const shiftNum = parseInt(input.dataset.shiftNum) || 0;
        const shiftName = input.dataset.shiftName || `Ca ${shiftNum}`;
        const startTime = input.dataset.start || '';
        const endTime = input.dataset.end || '';
        const quantity = parseInt(input.value) || 0;

        shiftDetailsArray.push({
            shiftNumber: shiftNum,
            shiftName: shiftName,
            startTime: startTime,
            endTime: endTime,
            quantity: quantity
        });

        totalMax += quantity;
        if (shiftNum === 1) s1 = quantity;
        if (shiftNum === 2) s2 = quantity;
    });

    const validFrom = document.getElementById('validFrom').value || null;
    const validTo = document.getElementById('validTo').value || null;
    const active = document.getElementById('quotaActive').checked;

    if (validFrom && validTo && new Date(validFrom) > new Date(validTo)) {
        showToast('Ngày bắt đầu không được lớn hơn ngày kết thúc', 'error');
        return;
    }

    const btn = document.getElementById('btnSaveQuota');
    btn.disabled = true;
    btn.textContent = 'Đang lưu...';

    try {
        const payload = {
            locationId: locationId,
            contractId: contractId,
            shift1Quantity: s1,
            shift2Quantity: s2,
            shiftDetails: JSON.stringify(shiftDetailsArray),
            validFrom: validFrom,
            validTo: validTo,
            active: active
        };

        if (editingQuotaId) {
            const res = await fetch(`/api/service-quotas/${editingQuotaId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                const errData = await res.json().catch(() => ({}));
                throw new Error(errData.message || 'Lỗi khi cập nhật định mức');
            }

            showToast('Cập nhật định mức dịch vụ thành công!', 'success');
        } else {
            payload.serviceId = serviceId;

            const res = await fetch('/api/service-quotas', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                const errData = await res.json().catch(() => ({}));
                throw new Error(errData.message || 'Lỗi khi lưu định mức dịch vụ');
            }

            showToast('Thiết lập định mức dịch vụ thành công!', 'success');
        }

        closeQuotaModal();

        // Auto-expand service row and reload table
        expandedServiceIds.add(serviceId);
        loadServicesWithQuotas();

    } catch (err) {
        console.error(err);
        showToast(err.message || 'Có lỗi xảy ra', 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Lưu';
    }
}

// ==============================================================
// 3. ĐỒNG BỘ DỮ LIỆU HIS
// ==============================================================
async function handleSyncHis() {
    const btn = document.getElementById('btnSyncHis');
    const originalHtml = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = `<i class="ph ph-spinner ph-spin"></i> <span>Đang đồng bộ...</span>`;

    try {
        const res = await fetch('/api/service-quotas/sync-his', { method: 'POST' });
        if (!res.ok) throw new Error('Không thể kết nối hệ thống bệnh viện HIS');

        const data = await res.json();
        showToast(data.message || 'Đồng bộ dữ liệu định mức HIS thành công!', 'success');
        loadServicesWithQuotas();

    } catch (err) {
        console.error(err);
        showToast(err.message || 'Lỗi đồng bộ HIS', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalHtml;
    }
}

// ==============================================================
// 4. CONFIRM DELETE QUOTA
// ==============================================================
function promptDeleteQuota(serviceId, quotaId, locationName) {
    document.getElementById('deleteModalDesc').textContent = 
        `Bạn có chắc chắn muốn xóa định mức dịch vụ tại địa điểm "${locationName}"?`;

    pendingDeleteAction = async () => {
        const res = await fetch(`/api/service-quotas/${quotaId}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Không thể xóa định mức dịch vụ');
        showToast('Đã xóa định mức dịch vụ thành công!', 'success');
        fetchAndRenderQuotas(serviceId);
        loadServicesWithQuotas();
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
        showToast(err.message || 'Lỗi khi xóa định mức', 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Xác nhận xóa';
    }
}

// ==============================================================
// UTILS
// ==============================================================
function formatCurrency(val) {
    if (val === null || val === undefined) return '0đ';
    return new Intl.NumberFormat('vi-VN').format(val) + 'đ';
}

function formatDateRange(from, to) {
    if (!from && !to) return 'Không giới hạn';
    const fStr = from ? formatDate(from) : '...';
    const tStr = to ? formatDate(to) : '...';
    return `${fStr} - ${tStr}`;
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const parts = dateStr.split('-');
    if (parts.length === 3) {
        return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateStr;
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;')
              .replace(/'/g, '&#039;');
}
