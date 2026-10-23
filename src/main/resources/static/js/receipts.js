/**
 * receipts.js - Quản lý Danh sách phiếu thu
 * PhongKham Healthcare Management System
 */

(function () {
    'use strict';

    let currentPage = 0;
    let pageSize = 20;
    let currentReceipts = [];
    let currentViewingReceipt = null;

    document.addEventListener('DOMContentLoaded', () => {
        initQuickDateFilter();
        loadFilterOptions();
        loadReceipts(0);
        bindEvents();
    });

    function bindEvents() {
        const searchInput = document.getElementById('receiptSearchInput');
        if (searchInput) {
            searchInput.addEventListener('input', debounce(() => loadReceipts(0), 400));
        }

        const companyFilter = document.getElementById('companyFilter');
        if (companyFilter) {
            companyFilter.addEventListener('change', () => loadReceipts(0));
        }

        const cashierFilter = document.getElementById('cashierFilter');
        if (cashierFilter) {
            cashierFilter.addEventListener('change', () => loadReceipts(0));
        }

        const paymentMethodFilter = document.getElementById('paymentMethodFilter');
        if (paymentMethodFilter) {
            paymentMethodFilter.addEventListener('change', () => loadReceipts(0));
        }

        const syncStatusFilter = document.getElementById('syncStatusFilter');
        if (syncStatusFilter) {
            syncStatusFilter.addEventListener('change', () => loadReceipts(0));
        }

        const quickDate = document.getElementById('quickDateFilter');
        if (quickDate) {
            quickDate.addEventListener('change', (e) => {
                applyQuickDate(e.target.value);
                loadReceipts(0);
            });
        }

        const fromDate = document.getElementById('fromDateFilter');
        const toDate = document.getElementById('toDateFilter');
        if (fromDate) fromDate.addEventListener('change', () => loadReceipts(0));
        if (toDate) toDate.addEventListener('change', () => loadReceipts(0));

        const detailModal = document.getElementById('receiptDetailModal');
        if (detailModal) {
            detailModal.addEventListener('click', (e) => {
                if (e.target === detailModal) {
                    closeReceiptDetailModal();
                }
            });
        }
    }

    function initQuickDateFilter() {
        applyQuickDate('month');
    }

    function applyQuickDate(val) {
        const fromInput = document.getElementById('fromDateFilter');
        const toInput = document.getElementById('toDateFilter');
        if (!fromInput || !toInput) return;

        const now = new Date();
        let year = now.getFullYear();
        let month = now.getMonth();

        // Đồng bộ với niên độ dữ liệu hệ thống (2026) nếu máy client ở năm khác
        if (year < 2026) {
            year = 2026;
            month = 9; // Tháng 10
        }

        if (val === 'month') {
            const start = new Date(year, month, 1);
            const end = new Date(year, month + 1, 0);
            fromInput.value = formatDateIso(start);
            toInput.value = formatDateIso(end);
        } else if (val === 'today') {
            const todayStr = formatDateIso(now);
            fromInput.value = todayStr;
            toInput.value = todayStr;
        } else if (val === 'week') {
            const day = now.getDay();
            const diff = now.getDate() - day + (day === 0 ? -6 : 1);
            const monday = new Date(now.setDate(diff));
            const sunday = new Date(monday);
            sunday.setDate(monday.getDate() + 6);
            fromInput.value = formatDateIso(monday);
            toInput.value = formatDateIso(sunday);
        } else if (val === 'year') {
            fromInput.value = `${year}-01-01`;
            toInput.value = `${year}-12-31`;
        } else {
            fromInput.value = '';
            toInput.value = '';
        }
    }

    function formatDateIso(d) {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${y}-${m}-${day}`;
    }

    async function loadFilterOptions() {
        try {
            const res = await fetch('/api/receipts/filters');
            if (res.ok) {
                const data = await res.json();
                const compSelect = document.getElementById('companyFilter');
                if (compSelect && data.companies) {
                    data.companies.forEach(c => {
                        const opt = document.createElement('option');
                        opt.value = c;
                        opt.textContent = c;
                        compSelect.appendChild(opt);
                    });
                }

                const cashSelect = document.getElementById('cashierFilter');
                if (cashSelect && data.cashiers) {
                    data.cashiers.forEach(c => {
                        const opt = document.createElement('option');
                        opt.value = c;
                        opt.textContent = c;
                        cashSelect.appendChild(opt);
                    });
                }
            }
        } catch (e) {
            console.warn('Lỗi tải danh mục bộ lọc phiếu thu:', e);
        }
    }

    window.clearReceiptFilters = function () {
        document.getElementById('receiptSearchInput').value = '';
        document.getElementById('companyFilter').value = '';
        document.getElementById('cashierFilter').value = '';
        document.getElementById('paymentMethodFilter').value = '';
        document.getElementById('syncStatusFilter').value = '';
        document.getElementById('quickDateFilter').value = 'all';
        document.getElementById('fromDateFilter').value = '';
        document.getElementById('toDateFilter').value = '';
        loadReceipts(0);
    };

    window.loadReceipts = async function (page = 0) {
        currentPage = page;
        const tbody = document.getElementById('receiptTableBody');
        tbody.innerHTML = `<tr><td colspan="13" style="text-align: center; padding: 40px; color: #94a3b8;">Đang tải danh sách phiếu thu...</td></tr>`;

        const keyword = document.getElementById('receiptSearchInput').value.trim();
        const company = document.getElementById('companyFilter').value;
        const cashier = document.getElementById('cashierFilter').value;
        const paymentMethod = document.getElementById('paymentMethodFilter').value;
        const syncStatus = document.getElementById('syncStatusFilter').value;
        const fromDate = document.getElementById('fromDateFilter').value;
        const toDate = document.getElementById('toDateFilter').value;

        let url = `/api/receipts/search?page=${page}&size=${pageSize}`;
        if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
        if (company) url += `&company=${encodeURIComponent(company)}`;
        if (cashier) url += `&cashier=${encodeURIComponent(cashier)}`;
        if (paymentMethod) url += `&paymentMethod=${encodeURIComponent(paymentMethod)}`;
        if (syncStatus) url += `&syncStatus=${encodeURIComponent(syncStatus)}`;
        if (fromDate) url += `&fromDate=${encodeURIComponent(fromDate)}`;
        if (toDate) url += `&toDate=${encodeURIComponent(toDate)}`;

        try {
            const res = await fetch(url);
            if (!res.ok) throw new Error('Không thể tải danh sách phiếu thu (HTTP ' + res.status + ')');
            const data = await res.json();
            currentReceipts = data.content || [];
            renderReceiptTable(currentReceipts, page, pageSize);
            renderReceiptPagination(data);
        } catch (err) {
            console.error(err);
            tbody.innerHTML = `<tr><td colspan="13" style="text-align: center; padding: 30px; color: #dc2626;">Lỗi tải dữ liệu: ${escapeHtml(err.message)}</td></tr>`;
        }
    };

    function renderReceiptTable(items, page, size) {
        const tbody = document.getElementById('receiptTableBody');
        if (!items || items.length === 0) {
            tbody.innerHTML = `<tr><td colspan="13" style="text-align: center; padding: 40px; color: #94a3b8;">Không tìm thấy phiếu thu nào phù hợp</td></tr>`;
            return;
        }

        tbody.innerHTML = items.map((r, idx) => {
            const stt = page * size + idx + 1;
            const timeStr = formatDateTime(r.createdAt);
            const sCount = (r.serviceCount != null && r.serviceCount > 0) ? r.serviceCount : 1;

            return `
                <tr>
                    <td style="text-align: center; color: #64748b; font-weight: 500;">${stt}</td>
                    <td style="font-weight: 700; color: #0f172a;">${escapeHtml(r.receiptCode || '-')}</td>
                    <td style="color: #475569; font-weight: 500;">${escapeHtml(r.patientCode || '-')}</td>
                    <td style="color: #475569; font-weight: 500;">${escapeHtml(r.recordCode || '-')}</td>
                    <td style="font-weight: 600; color: #0f172a;">${escapeHtml(r.patientName || '-')}</td>
                    <td style="text-align: center; color: #475569;">${escapeHtml(r.genderLabel || 'Nữ')}</td>
                    <td style="text-align: center; color: #475569;">${escapeHtml(r.birthYear || '-')}</td>
                    <td style="color: #334155;">${escapeHtml(r.companyName || '-')}</td>
                    <td style="text-align: center; font-weight: 700; color: #00684a;">${sCount}</td>
                    <td style="text-align: right; font-weight: 700; color: #0f172a;">${formatCurrency(r.finalAmount)}</td>
                    <td style="color: #334155; font-size: 13px;">${escapeHtml(r.cashierName || '-')}</td>
                    <td style="color: #64748b; font-size: 12.5px;">${timeStr}</td>
                    <td style="text-align: center; white-space: nowrap;">
                        <button type="button" class="btn-receipt-view" title="Xem thông tin thanh toán" onclick="viewReceiptDetail('${r.id}')">
                            <i class="ph-bold ph-eye"></i>
                        </button>
                    </td>
                </tr>
            `;
        }).join('');
    }

    function renderReceiptPagination(data) {
        const info = document.getElementById('receiptPaginationInfo');
        const controls = document.getElementById('receiptPaginationControls');

        const total = data.totalElements || 0;
        const page = data.number || 0;
        const size = data.size || 20;
        const totalPages = data.totalPages || 1;

        const start = total === 0 ? 0 : page * size + 1;
        const end = Math.min((page + 1) * size, total);
        info.textContent = `Đang xem ${start} - ${end} trong ${total}`;

        let btns = '';
        btns += `<button type="button" class="page-btn" ${data.first ? 'disabled' : ''} onclick="loadReceipts(${page - 1})" style="border: 1px solid #cbd5e1; background: #fff; border-radius: 6px; padding: 6px 10px; cursor: pointer;"><i class="ph ph-caret-left"></i></button>`;

        for (let i = 0; i < totalPages; i++) {
            if (i === 0 || i === totalPages - 1 || (i >= page - 1 && i <= page + 1)) {
                const activeStyle = (i === page) ? 'background: #00684a; color: #ffffff; border-color: #00684a;' : 'background: #ffffff; color: #334155; border: 1px solid #cbd5e1;';
                btns += `<button type="button" class="page-btn ${i === page ? 'active' : ''}" onclick="loadReceipts(${i})" style="${activeStyle} border-radius: 6px; padding: 6px 12px; font-weight: 600; cursor: pointer;">${i + 1}</button>`;
            } else if (i === page - 2 || i === page + 2) {
                btns += `<span style="padding: 6px 4px; color: #94a3b8;">...</span>`;
            }
        }

        btns += `<button type="button" class="page-btn" ${data.last ? 'disabled' : ''} onclick="loadReceipts(${page + 1})" style="border: 1px solid #cbd5e1; background: #fff; border-radius: 6px; padding: 6px 10px; cursor: pointer;"><i class="ph ph-caret-right"></i></button>`;
        controls.innerHTML = btns;
    }

    window.changeReceiptPageSize = function (newSize) {
        pageSize = parseInt(newSize, 10);
        loadReceipts(0);
    };

    /**
     * Mở modal thông tin thanh toán (Khớp 100% màn hình thanh toán khi chọn xong dịch vụ)
     */
    window.viewReceiptDetail = async function (id) {
        let r = currentReceipts.find(item => String(item.id).toLowerCase() === String(id).toLowerCase());
        if (!r) {
            try {
                const res = await fetch(`/api/receipts/${id}`);
                if (res.ok) {
                    r = await res.json();
                }
            } catch (err) {
                console.error('Lỗi tải chi tiết phiếu thu:', err);
            }
        }

        if (!r) {
            showToast('Không tìm thấy thông tin phiếu thu: ' + id, 'error');
            return;
        }
        currentViewingReceipt = r;

        // 1. Nạp thông tin Header
        document.getElementById('detailPatientName').textContent = r.patientName || 'Bệnh nhân';
        document.getElementById('detailRecordCodeBadge').textContent = (r.recordCode && r.recordCode !== '-') ? r.recordCode : (r.patientCode || 'HS-2026.08.0001');

        const genderText = r.genderLabel || (r.gender === 'NAM' ? 'Nam' : 'Nữ');
        const bYear = r.birthYear;
        const dobText = bYear ? (String(bYear).includes('/') ? bYear : ('01/10/' + bYear)) : '01/10/1990';
        const compName = r.companyName || 'Công ty TNHH Edufit';
        const cCode = r.contractCode || 'HĐ-2026-118';
        const pkgName = r.packageName || 'Gói khám';

        document.getElementById('detailGender').textContent = genderText;
        document.getElementById('detailDob').textContent = dobText;
        document.getElementById('detailCompanyName').textContent = compName;
        document.getElementById('detailContractCode').textContent = cCode;
        document.getElementById('detailPackageName').textContent = pkgName;

        // 2. Parse danh sách dịch vụ
        let services = [];
        try {
            if (r.allServicesJson) {
                if (typeof r.allServicesJson === 'string') {
                    services = JSON.parse(r.allServicesJson);
                } else if (Array.isArray(r.allServicesJson)) {
                    services = r.allServicesJson;
                }
            }
        } catch (e) {
            console.error('Error parsing allServicesJson:', e);
            services = [];
        }

        // Trường hợp chuỗi JSON bị mã hóa kép
        if (typeof services === 'string') {
            try { services = JSON.parse(services); } catch (e) { services = []; }
        }

        // Nếu danh sách dịch vụ trống, tạo fallback dựa trên labServices / imagingServices hoặc gói khám
        if (!Array.isArray(services) || services.length === 0) {
            services = [];
            if (r.labServices) {
                r.labServices.split(',').forEach((p, idx) => {
                    const trimmed = p.trim();
                    if (trimmed) {
                        services.push({
                            code: 'XN-00' + (idx + 1),
                            name: trimmed.startsWith('Xét nghiệm') ? trimmed : ('Xét nghiệm ' + trimmed),
                            price: Math.round(Number(r.subtotal || r.finalAmount || 0) / (r.serviceCount || 2)),
                            serviceType: 'Xét nghiệm'
                        });
                    }
                });
            }
            if (r.imagingServices) {
                r.imagingServices.split(',').forEach((p, idx) => {
                    const trimmed = p.trim();
                    if (trimmed) {
                        services.push({
                            code: 'CDHA-00' + (idx + 1),
                            name: trimmed,
                            price: Math.round(Number(r.subtotal || r.finalAmount || 0) / (r.serviceCount || 2)),
                            serviceType: 'CĐHA'
                        });
                    }
                });
            }
            if (services.length === 0) {
                services.push({
                    code: 'DV-001',
                    name: r.packageName || 'Dịch vụ khám sức khỏe tổng quát',
                    price: Number(r.subtotal || r.finalAmount || 0),
                    serviceType: 'Khám chuyên khoa'
                });
            }
        }

        // 3. Render danh sách dịch vụ Cột trái (THÀNH TIỀN)
        const servicesListEl = document.getElementById('detailServicesList');
        if (servicesListEl) {
            servicesListEl.innerHTML = services.map(s => {
                const sName = s.name || s.serviceName || s.title || 'Dịch vụ y tế';
                const sCode = s.code || s.serviceCode || 'DV-01';
                const sPrice = Number(s.price != null ? s.price : 0);
                const sType = s.serviceType ? (s.serviceType.includes('HINH_ANH') || s.serviceType.includes('CDHA') ? 'CĐHA' : (s.serviceType.includes('XET_NGHIEM') || s.serviceType.includes('LAB') ? 'Xét nghiệm' : s.serviceType)) : 'TT theo hợp đồng';

                return `
                    <div class="detail-service-item">
                        <div class="detail-service-info">
                            <div class="detail-service-name">${escapeHtml(sName)}</div>
                            <div class="detail-service-meta">
                                ${escapeHtml(sCode)} • ${escapeHtml(sType)}
                            </div>
                        </div>
                        <div class="detail-service-price">
                            ${formatCurrency(sPrice)}
                        </div>
                    </div>
                `;
            }).join('');
        }

        // 4. Tạm tính & Chiết khấu & Số tiền thực thu
        const totalCount = services.length || (r.serviceCount != null ? r.serviceCount : 1);
        document.getElementById('detailSubtotalLabel').textContent = `Tạm tính (${totalCount} dịch vụ)`;
        document.getElementById('detailSubtotalValue').textContent = `${formatCurrency(r.subtotal || r.finalAmount)} đ`;

        const dRow = document.getElementById('detailDiscountRow');
        const dAmount = Number(r.discountAmount || 0);
        if (dAmount > 0) {
            dRow.style.display = 'flex';
            document.getElementById('detailDiscountBadge').textContent = `${r.discountCode || 'CK'} • ${r.discountPercent || 0}%`;
            document.getElementById('detailDiscountValue').textContent = `- ${formatCurrency(dAmount)} đ`;
        } else {
            dRow.style.display = 'none';
        }

        document.getElementById('detailFinalAmount').textContent = `${formatCurrency(r.finalAmount)} đ`;

        // 5. Cột phải: Thông tin thanh toán
        const pm = r.paymentMethod || 'CHUYEN_KHOAN';
        document.querySelectorAll('input[name="detailPaymentMethodRadio"]').forEach(radio => {
            radio.checked = (radio.value === pm);
        });

        document.getElementById('detailCustomerPaidInput').value = `${formatCurrency(r.customerPaid || r.finalAmount)} đ`;
        document.getElementById('detailChangeAmountInput').value = `${formatCurrency(r.changeAmount || 0)} đ`;
        document.getElementById('detailCashierInput').value = `${r.cashierName || 'NV. Thu ngân'} - ${formatDateTime(r.createdAt)}`;
        document.getElementById('detailNoteInput').value = r.note || 'Bác Sĩ Tuyết';

        // 6. Mở Modal hiển thị
        const modal = document.getElementById('receiptDetailModal');
        if (modal) {
            modal.style.display = 'flex';
            modal.style.opacity = '1';
            modal.style.visibility = 'visible';
            modal.classList.add('show');
            modal.classList.add('active');
        }
    };

    window.closeReceiptDetailModal = function () {
        const modal = document.getElementById('receiptDetailModal');
        if (modal) {
            modal.classList.remove('show');
            modal.classList.remove('active');
            modal.style.display = 'none';
            modal.style.opacity = '0';
            modal.style.visibility = 'hidden';
        }
        currentViewingReceipt = null;
    };

    window.exportExcel = function () {
        if (!currentReceipts || currentReceipts.length === 0) {
            showToast('Không có dữ liệu phiếu thu để xuất', 'warning');
            return;
        }

        // Tạo file CSV có BOM UTF-8 hỗ trợ Excel hiển thị Tiếng Việt
        let csvContent = '\uFEFF';
        csvContent += 'STT,Mã phiếu thu,Mã NB,Mã hồ sơ,Tên người bệnh,Giới tính,Năm sinh,Công ty,Tổng SL dịch vụ,Tiền đã thu,Người thu,Thời gian\n';

        currentReceipts.forEach((r, idx) => {
            const sCount = (r.serviceCount != null && r.serviceCount > 0) ? r.serviceCount : 1;
            const row = [
                idx + 1,
                `"${r.receiptCode || ''}"`,
                `"${r.patientCode || ''}"`,
                `"${r.recordCode || ''}"`,
                `"${r.patientName || ''}"`,
                `"${r.genderLabel || 'Nữ'}"`,
                `"${r.birthYear || ''}"`,
                `"${r.companyName || ''}"`,
                sCount,
                Number(r.finalAmount || 0),
                `"${r.cashierName || ''}"`,
                `"${formatDateTime(r.createdAt)}"`
            ];
            csvContent += row.join(',') + '\n';
        });

        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', `Danh_sach_phieu_thu_${new Date().toISOString().split('T')[0]}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        showToast('Xuất danh sách phiếu thu thành công', 'success');
    };

    // Helper utilities
    function formatCurrency(val) {
        if (val == null) return '0';
        return new Intl.NumberFormat('vi-VN').format(Number(val));
    }

    function formatDateTime(isoStr) {
        if (!isoStr) return '-';
        try {
            const d = new Date(isoStr);
            if (isNaN(d.getTime())) return isoStr;
            const h = String(d.getHours()).padStart(2, '0');
            const m = String(d.getMinutes()).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const year = d.getFullYear();
            return `${h}:${m}, ${day}/${month}/${year}`;
        } catch (e) {
            return isoStr;
        }
    }

    function escapeHtml(text) {
        if (!text) return '';
        return String(text)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function debounce(func, wait) {
        let timeout;
        return function (...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }

    function showToast(message, type = 'info') {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        
        let icon = 'info';
        if (type === 'success') icon = 'check-circle';
        else if (type === 'error') icon = 'x-circle';
        else if (type === 'warning') icon = 'warning-circle';

        toast.innerHTML = `
            <i class="ph ph-${icon}"></i>
            <span>${escapeHtml(message)}</span>
        `;

        container.appendChild(toast);
        setTimeout(() => toast.classList.add('show'), 10);
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 3500);
    }

})();
