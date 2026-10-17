/**
 * ==============================================================
 * CHÍNH SÁCH GIẢM GIÁ - THIẾT LẬP FORM (DISCOUNT FORM JS)
 * ==============================================================
 */

// Detect if editing
const pathParts = window.location.pathname.split('/');
let editId = null;
if (pathParts.includes('edit')) {
    const editIdx = pathParts.indexOf('edit');
    if (editIdx > 0) editId = pathParts[editIdx - 1];
}

// State
let allContracts = [];
let selectedContractIds = new Set();
let currentTiers = [
    { tierNumber: 1, fromQuantity: 1, toQuantity: 3, discountValue: 10 },
    { tierNumber: 2, fromQuantity: 4, toQuantity: null, discountValue: 20 }
];

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
        min-width: 260px;
        animation: slideIn 0.3s ease;
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

function updateActiveLabel(checked) {
    const label = document.getElementById('activeStatusText');
    if (!label) return;
    label.innerText = checked ? 'Bật' : 'Tắt';
    label.style.color = checked ? '#059669' : '#64748b';
}

// Tiers Table Management
function renderTierTable() {
    const tbody = document.getElementById('tierTableBody');
    if (!tbody) return;

    const isPercent = document.getElementById('discountType').value === 'PERCENT';
    const unitLabel = isPercent ? '%' : 'đ';

    let html = '';
    currentTiers.forEach((tier, idx) => {
        const tierNum = idx + 1;
        tier.tierNumber = tierNum;
        const isLast = idx === currentTiers.length - 1;
        const canDelete = currentTiers.length > 1;

        html += `
            <tr>
                <td style="text-align: center;">
                    <span class="tier-num-badge">${tierNum}</span>
                </td>
                <td>
                    <div class="tier-input-wrap">
                        <input type="number" min="1" value="${tier.fromQuantity}" 
                            onchange="updateTierValue(${idx}, 'fromQuantity', this.value)" required>
                    </div>
                </td>
                <td>
                    <div class="tier-input-wrap">
                        <input type="number" min="1" value="${tier.toQuantity !== null && tier.toQuantity !== undefined ? tier.toQuantity : ''}" 
                            placeholder="${isLast ? 'trở lên' : ''}"
                            onchange="updateTierValue(${idx}, 'toQuantity', this.value)">
                    </div>
                </td>
                <td>
                    <div class="tier-input-wrap">
                        <input type="number" step="${isPercent ? '0.1' : '1000'}" min="0" value="${tier.discountValue}" 
                            onchange="updateTierValue(${idx}, 'discountValue', this.value)" required>
                        <span class="tier-addon">${unitLabel}</span>
                    </div>
                </td>
                <td style="text-align: center;">
                    ${canDelete ? `
                        <button type="button" class="tier-delete-btn" title="Xóa bậc" onclick="removeTierRow(${idx})">
                            <i class="ph-bold ph-x"></i>
                        </button>
                    ` : ''}
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

function updateTierValue(idx, field, value) {
    if (field === 'fromQuantity') {
        currentTiers[idx].fromQuantity = parseInt(value, 10) || 1;
    } else if (field === 'toQuantity') {
        currentTiers[idx].toQuantity = value !== '' ? parseInt(value, 10) : null;
    } else if (field === 'discountValue') {
        currentTiers[idx].discountValue = parseFloat(value) || 0;
    }
}

function addNewTierRow() {
    const lastTier = currentTiers[currentTiers.length - 1];
    let nextFrom = 1;
    if (lastTier) {
        if (lastTier.toQuantity) {
            nextFrom = lastTier.toQuantity + 1;
        } else {
            lastTier.toQuantity = lastTier.fromQuantity + 2;
            nextFrom = lastTier.toQuantity + 1;
        }
    }
    currentTiers.push({
        tierNumber: currentTiers.length + 1,
        fromQuantity: nextFrom,
        toQuantity: null,
        discountValue: lastTier ? lastTier.discountValue : 10
    });
    renderTierTable();
}

function removeTierRow(idx) {
    if (currentTiers.length <= 1) return;
    currentTiers.splice(idx, 1);
    currentTiers.forEach((t, i) => t.tierNumber = i + 1);
    renderTierTable();
}

function handleDiscountTypeChange() {
    renderTierTable();
}

// Multi-contract selector modal & chips
async function loadContracts() {
    try {
        const res = await fetch('/api/contracts/search?page=0&size=200');
        if (res.ok) {
            const data = await res.json();
            allContracts = data.items || data.content || [];
        }
    } catch (err) {
        console.error('Error fetching contracts:', err);
    }
}

function renderSelectedContractChips() {
    const container = document.getElementById('selectedContractChips');
    const placeholder = document.getElementById('contractPickerPlaceholder');
    if (!container || !placeholder) return;

    container.innerHTML = '';

    if (selectedContractIds.size === 0) {
        placeholder.style.display = 'inline';
        return;
    }

    placeholder.style.display = 'none';
    selectedContractIds.forEach(id => {
        const c = allContracts.find(item => item.id === id);
        if (c) {
            const chip = document.createElement('div');
            chip.className = 'contract-chip';
            chip.innerHTML = `
                <span>${c.companyName} — ${c.contractCode}</span>
                <span class="remove-chip-btn" onclick="event.stopPropagation(); removeContractChip('${c.id}')" title="Bỏ chọn">
                    <i class="ph-bold ph-x"></i>
                </span>
            `;
            container.appendChild(chip);
        }
    });
}

function removeContractChip(id) {
    selectedContractIds.delete(id);
    renderSelectedContractChips();
}

function openContractModal() {
    renderContractList();
    document.getElementById('contractModal').classList.add('show');
}

function closeContractModal() {
    document.getElementById('contractModal').classList.remove('show');
}

function renderContractList() {
    const container = document.getElementById('contractListContainer');
    const searchInput = document.getElementById('contractSearchInput');
    const search = searchInput ? searchInput.value.toLowerCase().trim() : '';

    const filtered = allContracts.filter(c => {
        if (!search) return true;
        return (c.contractCode && c.contractCode.toLowerCase().includes(search)) ||
               (c.contractName && c.contractName.toLowerCase().includes(search)) ||
               (c.companyName && c.companyName.toLowerCase().includes(search));
    });

    if (filtered.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #94a3b8;">
                <i class="ph ph-magnifying-glass" style="font-size: 32px; margin-bottom: 6px; display: inline-block;"></i>
                <div style="font-size: 14px; font-weight: 500;">Không tìm thấy hợp đồng nào phù hợp</div>
            </div>
        `;
        updateModalCount();
        return;
    }

    let html = '';
    filtered.forEach(c => {
        const isSelected = selectedContractIds.has(c.id);
        html += `
            <div class="contract-row-item ${isSelected ? 'selected' : ''}" onclick="toggleContractRow('${c.id}')">
                <input type="checkbox" value="${c.id}" ${isSelected ? 'checked' : ''} 
                    onclick="event.stopPropagation(); toggleContractInModal('${c.id}', this.checked)"
                    style="accent-color: #059669; width: 18px; height: 18px; cursor: pointer;">
                <div style="flex: 1;">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span style="background: #ecfdf5; color: #047857; font-weight: 700; font-size: 12px; padding: 2px 8px; border-radius: 6px; border: 1px solid #a7f3d0;">
                            ${c.contractCode}
                        </span>
                        <span style="font-weight: 700; font-size: 14px; color: #1e293b;">${c.companyName}</span>
                    </div>
                    <div style="font-size: 13px; color: #64748b; margin-top: 3px;">${c.contractName}</div>
                </div>
                <div>
                    <span style="font-size: 12px; font-weight: 600; padding: 3px 9px; border-radius: 12px; background: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0;">
                        Hiệu lực
                    </span>
                </div>
            </div>
        `;
    });
    container.innerHTML = html;
    updateModalCount();
}

function toggleContractRow(id) {
    if (selectedContractIds.has(id)) {
        selectedContractIds.delete(id);
    } else {
        selectedContractIds.add(id);
    }
    renderContractList();
}

function toggleContractInModal(id, checked) {
    if (checked) {
        selectedContractIds.add(id);
    } else {
        selectedContractIds.delete(id);
    }
    renderContractList();
}

function selectAllContracts(selectAll) {
    allContracts.forEach(c => {
        if (selectAll) selectedContractIds.add(c.id);
        else selectedContractIds.delete(c.id);
    });
    renderContractList();
}

function updateModalCount() {
    const el = document.getElementById('modalSelectedCount');
    if (el) {
        el.innerText = `Đã chọn ${selectedContractIds.size} / ${allContracts.length} hợp đồng`;
    }
}

function confirmContractSelection() {
    renderSelectedContractChips();
    closeContractModal();
}

// Initialize Form
async function initPage() {
    await loadContracts();

    const currentYear = new Date().getFullYear();
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    if (startDateInput && !startDateInput.value) startDateInput.value = `${currentYear}-01-01`;
    if (endDateInput && !endDateInput.value) endDateInput.value = `${currentYear}-12-31`;

    if (editId) {
        const breadcrumbTitle = document.getElementById('breadcrumbPageTitle');
        const mainHeading = document.getElementById('pageMainHeading');
        const submitText = document.getElementById('btnSubmitText');
        if (breadcrumbTitle) breadcrumbTitle.innerText = 'Cập nhật chính sách giảm giá';
        if (mainHeading) mainHeading.innerText = 'Cập nhật chính sách giảm giá';
        if (submitText) submitText.innerText = 'Lưu thay đổi';
        document.title = 'Cập nhật chính sách giảm giá | Mirai Healthcare';

        await loadPolicyForEdit(editId);
    } else {
        renderTierTable();
        try {
            const res = await fetch('/api/discounts/next-code');
            if (res.ok) {
                const data = await res.json();
                if (data.nextCode) {
                    const codeInput = document.getElementById('policyCode');
                    if (codeInput) codeInput.value = data.nextCode;
                }
            }
        } catch (e) {
            console.log('Cannot fetch next code', e);
        }
    }
}

async function loadPolicyForEdit(id) {
    try {
        const res = await fetch(`/api/discounts/${id}`);
        if (!res.ok) throw new Error('Không thể tải thông tin chính sách');
        const data = await res.json();

        const codeInput = document.getElementById('policyCode');
        if (codeInput) {
            codeInput.value = data.code || '';
            codeInput.disabled = true;
        }

        const nameInput = document.getElementById('policyName');
        if (nameInput) nameInput.value = data.name || '';

        const discountTypeSelect = document.getElementById('discountType');
        if (discountTypeSelect) discountTypeSelect.value = data.discountType || 'PERCENT';

        const tierScopeSelect = document.getElementById('tierScope');
        if (tierScopeSelect) tierScopeSelect.value = data.tierScope || 'MAX_TIER_ALL';

        const maxDiscountInput = document.getElementById('maxDiscount');
        if (maxDiscountInput && data.maxDiscountPerService !== null && data.maxDiscountPerService !== undefined) {
            maxDiscountInput.value = data.maxDiscountPerService;
        }

        const startDateInput = document.getElementById('startDate');
        if (startDateInput) startDateInput.value = data.startDate || '';

        const endDateInput = document.getElementById('endDate');
        if (endDateInput) endDateInput.value = data.endDate || '';

        const activeCheckbox = document.getElementById('policyActive');
        if (activeCheckbox) {
            activeCheckbox.checked = data.active;
            updateActiveLabel(data.active);
        }

        // Contracts
        selectedContractIds.clear();
        if (data.contracts && data.contracts.length > 0) {
            data.contracts.forEach(c => selectedContractIds.add(c.contractId));
        }
        renderSelectedContractChips();

        // Tiers
        if (data.tiers && data.tiers.length > 0) {
            currentTiers = data.tiers.map(t => ({
                tierNumber: t.tierNumber,
                fromQuantity: t.fromQuantity,
                toQuantity: t.toQuantity,
                discountValue: t.discountValue
            }));
        }
        renderTierTable();

    } catch (err) {
        console.error(err);
        showToast('Lỗi khi tải thông tin chính sách giảm giá', 'error');
    }
}

// Form Submit
async function handleFormSubmit(event) {
    event.preventDefault();

    const name = document.getElementById('policyName').value.trim();
    if (!name) {
        showToast('Vui lòng nhập tên chính sách giảm giá', 'error');
        return;
    }

    if (selectedContractIds.size === 0) {
        showToast('Vui lòng chọn ít nhất một hợp đồng áp dụng', 'error');
        openContractModal();
        return;
    }

    const code = document.getElementById('policyCode').value.trim();
    const discountType = document.getElementById('discountType').value;
    const tierScope = document.getElementById('tierScope').value;
    const maxDiscount = document.getElementById('maxDiscount').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const active = document.getElementById('policyActive').checked;

    if (!startDate) {
        showToast('Vui lòng chọn ngày bắt đầu hiệu lực', 'error');
        return;
    }

    // Thu thập chính xác giá trị các bậc chiết khấu từ bảng
    const rows = document.querySelectorAll('#tierTableBody tr');
    const tiers = [];
    rows.forEach((row, idx) => {
        const inputs = row.querySelectorAll('input');
        if (inputs.length >= 3) {
            const fromVal = parseInt(inputs[0].value, 10) || 1;
            const toVal = inputs[1].value !== '' ? parseInt(inputs[1].value, 10) : null;
            const discVal = parseFloat(inputs[2].value) || 0;
            tiers.push({
                tierNumber: idx + 1,
                fromQuantity: fromVal,
                toQuantity: toVal,
                discountValue: discVal
            });
        }
    });
    if (tiers.length > 0) {
        currentTiers = tiers;
    }

    const btnSubmit = document.getElementById('btnSubmitForm');
    btnSubmit.disabled = true;
    btnSubmit.innerHTML = `<i class="ph ph-spinner ph-spin"></i> <span>Đang lưu...</span>`;

    const payload = {
        code: code || null,
        name: name,
        discountType: discountType,
        tierScope: tierScope,
        maxDiscountPerService: maxDiscount ? parseFloat(maxDiscount) : null,
        startDate: startDate,
        endDate: endDate || null,
        active: active,
        contractIds: Array.from(selectedContractIds),
        tiers: currentTiers
    };

    try {
        let url = '/api/discounts';
        let method = 'POST';
        if (editId) {
            url = `/api/discounts/${editId}`;
            method = 'PUT';
        }

        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            throw new Error(errData.message || 'Lỗi khi lưu chính sách giảm giá');
        }

        showToast(editId ? 'Cập nhật chính sách thành công!' : 'Tạo mới chính sách thành công!', 'success');
        setTimeout(() => {
            window.location.href = '/admin/discounts';
        }, 700);

    } catch (err) {
        console.error(err);
        showToast(err.message || 'Có lỗi xảy ra khi lưu chính sách', 'error');
        btnSubmit.disabled = false;
        btnSubmit.innerHTML = `<i class="ph-bold ph-floppy-disk"></i> <span>Lưu</span>`;
    }
}

// Event Listeners Initialization
document.addEventListener('DOMContentLoaded', () => {
    initPage();

    const searchInput = document.getElementById('contractSearchInput');
    if (searchInput) {
        searchInput.addEventListener('input', () => {
            renderContractList();
        });
    }
});
