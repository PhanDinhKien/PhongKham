/**
 * ==============================================================
 * CHÍNH SÁCH GIẢM GIÁ - DANH SÁCH (DISCOUNTS LIST JS)
 * ==============================================================
 */

// State
let currentPage = 0;
let pageSize = 20;
let totalPages = 0;
let deleteTargetId = null;

// Toast Helper
function showToast(message, type = 'success') {
    const toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) return;

    const toast = document.createElement('div');
    const bg = type === 'success' ? '#059669' : (type === 'error' ? '#dc2626' : '#2563eb');
    const icon = type === 'success' ? 'ph-check-circle' : (type === 'error' ? 'ph-warning-circle' : 'ph-info');
    toast.style.cssText = `
        background: ${bg};
        color: white;
        padding: 12px 18px;
        border-radius: 8px;
        box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1);
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 14px;
        font-weight: 500;
        animation: slideIn 0.3s ease;
        min-width: 260px;
    `;
    toast.innerHTML = `<i class="ph-bold ${icon}" style="font-size: 20px;"></i> <span>${message}</span>`;
    toastContainer.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Format Date
function formatDate(dateStr) {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-');
    return `${d}/${m}/${y}`;
}

// Format Date Range
function formatDateRange(start, end) {
    if (!start && !end) return 'Vô thời hạn';
    if (start && !end) return `Từ ${formatDate(start)}`;
    if (!start && end) return `Đến ${formatDate(end)}`;
    return `${formatDate(start)} - ${formatDate(end)}`;
}

// Fetch & Render Policies
async function fetchPolicies(page = 0) {
    currentPage = page;
    const keyword = document.getElementById('keywordFilter').value.trim();
    const companyOrContract = document.getElementById('companyFilter').value.trim();
    const discountType = document.getElementById('discountTypeFilter').value;
    const active = document.getElementById('activeFilter').value;
    const fromDate = document.getElementById('fromDateFilter').value;
    const toDate = document.getElementById('toDateFilter').value;

    let url = `/api/discounts/search?page=${currentPage}&size=${pageSize}`;
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
    if (companyOrContract) url += `&companyOrContract=${encodeURIComponent(companyOrContract)}`;
    if (discountType) url += `&discountType=${encodeURIComponent(discountType)}`;
    if (active !== '') url += `&active=${encodeURIComponent(active)}`;
    if (fromDate) url += `&fromDate=${encodeURIComponent(fromDate)}`;
    if (toDate) url += `&toDate=${encodeURIComponent(toDate)}`;

    const tbody = document.getElementById('discountTableBody');

    try {
        const res = await fetch(url);
        if (!res.ok) throw new Error('Không thể tải dữ liệu');
        const data = await res.json();
        
        renderTable(data);
        renderPagination(data);
    } catch (err) {
        console.error(err);
        tbody.innerHTML = `
            <tr>
                <td colspan="9" style="text-align: center; padding: 40px; color: #ef4444;">
                    <i class="ph ph-warning" style="font-size: 28px; margin-bottom: 8px; display: inline-block;"></i>
                    <div>Đã có lỗi xảy ra khi tải dữ liệu chính sách giảm giá.</div>
                </td>
            </tr>
        `;
    }
}

function renderTable(data) {
    const tbody = document.getElementById('discountTableBody');
    const items = data.content || data.items || [];

    if (items.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" style="text-align: center; padding: 50px 20px; color: #94a3b8;">
                    <i class="ph ph-tag" style="font-size: 36px; margin-bottom: 8px; display: inline-block; color: #cbd5e1;"></i>
                    <div style="font-size: 15px; font-weight: 500; color: #64748b;">Không tìm thấy chính sách giảm giá nào</div>
                    <div style="font-size: 13px; margin-top: 4px;">Hãy thử điều chỉnh bộ lọc hoặc bấm "Thêm mới" để tạo chính sách.</div>
                </td>
            </tr>
        `;
        return;
    }

    let html = '';
    items.forEach((item, index) => {
        const stt = currentPage * pageSize + index + 1;
        const contractsCount = item.contracts ? item.contracts.length : 0;
        let applyTypeHtml = 'Hợp đồng';
        if (contractsCount > 0) {
            const contractTitle = item.contracts.map(c => `• ${c.companyName} (${c.contractCode})`).join('\n');
            applyTypeHtml += ` <span class="contract-count-tag" title="${contractTitle}">${contractsCount} HĐ</span>`;
        }

        html += `
            <tr>
                <td style="text-align: center; color: #64748b;">${stt}</td>
                <td>
                    <a href="/admin/discounts/${item.id}/edit" class="discount-code-cell" title="Nhấn để chỉnh sửa">${item.code || '-'}</a>
                </td>
                <td style="font-weight: 600; color: #1e293b;">
                    <div>${item.name || '-'}</div>
                    ${item.contractNames ? `<div style="font-size: 12px; color: #64748b; font-weight: 400; margin-top: 2px;">Áp dụng: ${item.contractNames}</div>` : ''}
                </td>
                <td>${applyTypeHtml}</td>
                <td>${item.discountTypeLabel || (item.discountType === 'PERCENT' ? 'Phần trăm' : 'Số tiền')}</td>
                <td>
                    <span class="ck-value-badge">${item.tierSummary || '0'}</span>
                </td>
                <td style="color: #475569; font-size: 13px;">${formatDateRange(item.startDate, item.endDate)}</td>
                <td style="text-align: center;">
                    <label class="switch-toggle" title="${item.active ? 'Đang áp dụng (Nhấn để tắt)' : 'Ngừng áp dụng (Nhấn để bật)'}">
                        <input type="checkbox" ${item.active ? 'checked' : ''} onchange="togglePolicyActive('${item.id}', this)">
                        <span class="slider-round"></span>
                    </label>
                </td>
                <td style="text-align: right; padding-right: 20px; white-space: nowrap;">
                    <a href="/admin/discounts/${item.id}/edit" class="action-icon-btn" title="Chỉnh sửa">
                        <i class="ph ph-pencil-simple"></i>
                    </a>
                    <button type="button" class="action-icon-btn delete-btn" title="Xóa chính sách" onclick="openDeleteModal('${item.id}', '${item.code}')">
                        <i class="ph ph-trash"></i>
                    </button>
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

function renderPagination(data) {
    totalPages = data.totalPages || 0;
    const totalElements = data.totalElements || data.total || 0;
    const startItem = totalElements === 0 ? 0 : currentPage * pageSize + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, totalElements);

    document.getElementById('paginationInfo').innerText = `Đang xem ${startItem} - ${endItem} trong ${totalElements}`;

    const pagesContainer = document.getElementById('paginationPages');
    if (totalPages <= 1) {
        pagesContainer.innerHTML = '';
        return;
    }

    let html = `
        <button type="button" class="page-nav-btn ${currentPage === 0 ? 'disabled' : ''}" 
            onclick="fetchPolicies(${currentPage - 1})" ${currentPage === 0 ? 'disabled' : ''}>
            <i class="ph ph-caret-left"></i>
        </button>
    `;

    for (let i = 0; i < totalPages; i++) {
        if (i === 0 || i === totalPages - 1 || (i >= currentPage - 1 && i <= currentPage + 1)) {
            html += `
                <button type="button" class="page-num-btn ${i === currentPage ? 'active' : ''}" 
                    onclick="fetchPolicies(${i})">
                    ${i + 1}
                </button>
            `;
        } else if (i === currentPage - 2 || i === currentPage + 2) {
            html += `<span class="page-dots">...</span>`;
        }
    }

    html += `
        <button type="button" class="page-nav-btn ${currentPage >= totalPages - 1 ? 'disabled' : ''}" 
            onclick="fetchPolicies(${currentPage + 1})" ${currentPage >= totalPages - 1 ? 'disabled' : ''}>
            <i class="ph ph-caret-right"></i>
        </button>
    `;

    pagesContainer.innerHTML = html;
}

// Toggle Status
async function togglePolicyActive(id, checkbox) {
    try {
        const res = await fetch(`/api/discounts/${id}/toggle-status`, { method: 'PATCH' });
        if (!res.ok) throw new Error('Không thể cập nhật trạng thái');
        const result = await res.json();
        checkbox.checked = result.active;
        showToast(result.active ? 'Đã kích hoạt chính sách giảm giá' : 'Đã tạm dừng chính sách giảm giá', 'success');
    } catch (err) {
        console.error(err);
        checkbox.checked = !checkbox.checked;
        showToast('Lỗi cập nhật trạng thái chính sách', 'error');
    }
}

// Delete Modal Handlers
function openDeleteModal(id, code) {
    deleteTargetId = id;
    document.getElementById('deleteModalMsg').innerText = `Bạn có chắc chắn muốn xóa chính sách giảm giá "${code}" không? Thao tác này không thể hoàn tác.`;
    document.getElementById('deleteModal').classList.add('show');
}

// Event Listeners Initialization
document.addEventListener('DOMContentLoaded', () => {
    fetchPolicies(0);

    const btnCancelDelete = document.getElementById('btnCancelDelete');
    if (btnCancelDelete) {
        btnCancelDelete.addEventListener('click', () => {
            document.getElementById('deleteModal').classList.remove('show');
            deleteTargetId = null;
        });
    }

    const btnConfirmDelete = document.getElementById('btnConfirmDelete');
    if (btnConfirmDelete) {
        btnConfirmDelete.addEventListener('click', async () => {
            if (!deleteTargetId) return;
            try {
                const res = await fetch(`/api/discounts/${deleteTargetId}`, { method: 'DELETE' });
                if (!res.ok) throw new Error('Không thể xóa chính sách');
                document.getElementById('deleteModal').classList.remove('show');
                showToast('Đã xóa chính sách giảm giá thành công', 'success');
                fetchPolicies(currentPage);
            } catch (err) {
                console.error(err);
                showToast('Không thể xóa chính sách giảm giá', 'error');
            }
        });
    }

    let filterTimeout;
    function debounceFilter() {
        clearTimeout(filterTimeout);
        filterTimeout = setTimeout(() => {
            fetchPolicies(0);
        }, 300);
    }

    const keywordFilter = document.getElementById('keywordFilter');
    if (keywordFilter) keywordFilter.addEventListener('input', debounceFilter);

    const companyFilter = document.getElementById('companyFilter');
    if (companyFilter) companyFilter.addEventListener('input', debounceFilter);

    const discountTypeFilter = document.getElementById('discountTypeFilter');
    if (discountTypeFilter) discountTypeFilter.addEventListener('change', () => fetchPolicies(0));

    const activeFilter = document.getElementById('activeFilter');
    if (activeFilter) activeFilter.addEventListener('change', () => fetchPolicies(0));

    const fromDateFilter = document.getElementById('fromDateFilter');
    if (fromDateFilter) fromDateFilter.addEventListener('change', () => fetchPolicies(0));

    const toDateFilter = document.getElementById('toDateFilter');
    if (toDateFilter) toDateFilter.addEventListener('change', () => fetchPolicies(0));

    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', function() {
            pageSize = parseInt(this.value, 10);
            fetchPolicies(0);
        });
    }

    const btnClearFilter = document.getElementById('btnClearFilter');
    if (btnClearFilter) {
        btnClearFilter.addEventListener('click', () => {
            if (keywordFilter) keywordFilter.value = '';
            if (companyFilter) companyFilter.value = '';
            if (discountTypeFilter) discountTypeFilter.value = '';
            if (activeFilter) activeFilter.value = '';
            if (fromDateFilter) fromDateFilter.value = '';
            if (toDateFilter) toDateFilter.value = '';
            fetchPolicies(0);
        });
    }
});
