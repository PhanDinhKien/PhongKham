/**
 * Role Edit / Permission Configuration Script
 */
        // State
        let isEditMode = false;
        let roleId = null;
        let allPermissions = [];
        let selectedPermissionCodes = new Set();

        // DOM Elements
        const htmlTitle = document.getElementById('htmlTitle');
        const breadcrumbAction = document.getElementById('breadcrumbAction');
        const pageTitle = document.getElementById('pageTitle');
        const pageSubtitle = document.getElementById('pageSubtitle');
        const btnSubmitText = document.getElementById('btnSubmitText');
        const roleEditForm = document.getElementById('roleEditForm');
        const roleIdInput = document.getElementById('roleId');
        const roleNameInput = document.getElementById('roleName');
        const roleCodeInput = document.getElementById('roleCode');
        const roleCodeHelp = document.getElementById('roleCodeHelp');
        const roleDescriptionInput = document.getElementById('roleDescription');
        const roleActiveCheckbox = document.getElementById('roleActive');
        const activeStatusLabel = document.getElementById('activeStatusLabel');
        const permMatrixContainer = document.getElementById('permMatrixContainer');
        const permSearchInput = document.getElementById('permSearchInput');
        const selectedPermCount = document.getElementById('selectedPermCount');
        const totalPermCount = document.getElementById('totalPermCount');
        const btnSelectAllPerms = document.getElementById('btnSelectAllPerms');
        const btnDeselectAllPerms = document.getElementById('btnDeselectAllPerms');

        // Khởi tạo
        document.addEventListener('DOMContentLoaded', async () => {
            parseUrlMode();
            setupEventListeners();
            await loadPermissions();
            if (isEditMode) {
                await loadRoleDetail(roleId);
            } else {
                renderPermissionMatrix();
            }
        });

        // Phân tích URL xác định chế độ Thêm mới hay Chỉnh sửa
        function parseUrlMode() {
            const urlParams = new URLSearchParams(window.location.search);
            const queryId = urlParams.get('id');

            // Hỗ trợ cả /admin/roles/{id}/edit và /admin/roles/edit?id=...
            const pathParts = window.location.pathname.split('/').filter(Boolean);
            let pathId = null;
            if (pathParts.length >= 3 && pathParts[1] === 'roles' && pathParts[3] === 'edit') {
                pathId = pathParts[2];
            }

            roleId = queryId || pathId;
            isEditMode = !!roleId;

            if (isEditMode) {
                roleIdInput.value = roleId;
                htmlTitle.textContent = 'Chỉnh sửa vai trò & phân quyền | Mirai Healthcare';
                breadcrumbAction.textContent = 'Chỉnh sửa vai trò';
                pageTitle.textContent = 'Chỉnh sửa vai trò & phân quyền';
                pageSubtitle.textContent = 'Cấu hình thông tin vai trò và danh sách các quyền hạn truy cập tương ứng';
                btnSubmitText.textContent = 'Lưu thay đổi';
                roleCodeInput.readOnly = true;
                roleCodeHelp.style.display = 'block';
            } else {
                roleIdInput.value = '';
                htmlTitle.textContent = 'Thêm vai trò mới | Mirai Healthcare';
                breadcrumbAction.textContent = 'Thêm vai trò mới';
                pageTitle.textContent = 'Thêm vai trò mới';
                pageSubtitle.textContent = 'Khởi tạo vai trò mới và phân quyền hạn chức năng trong hệ thống';
                btnSubmitText.textContent = 'Tạo vai trò';
                roleCodeInput.readOnly = false;
                roleCodeHelp.style.display = 'none';
            }
        }

        function setupEventListeners() {
            roleEditForm.addEventListener('submit', handleFormSubmit);

            roleActiveCheckbox.addEventListener('change', (e) => {
                activeStatusLabel.textContent = e.target.checked
                    ? 'Vai trò đang được kích hoạt và cho phép gán cho tài khoản'
                    : 'Vai trò đang bị khóa và không thể gán cho tài khoản';
            });

            btnSelectAllPerms.addEventListener('click', () => {
                allPermissions.forEach(p => selectedPermissionCodes.add(p.code));
                updateAllCheckboxesFromState();
            });

            btnDeselectAllPerms.addEventListener('click', () => {
                selectedPermissionCodes.clear();
                updateAllCheckboxesFromState();
            });

            permSearchInput.addEventListener('input', () => {
                renderPermissionMatrix(permSearchInput.value.trim().toLowerCase());
            });
        }

        // Tải danh mục quyền hạn
        async function loadPermissions() {
            try {
                const response = await fetch('/api/roles/permissions');
                if (response.ok) {
                    allPermissions = await response.json();
                    totalPermCount.textContent = allPermissions.length;
                }
            } catch (err) {
                console.error('Lỗi khi tải danh sách quyền:', err);
            }
        }

        // Tải chi tiết vai trò khi ở chế độ chỉnh sửa
        async function loadRoleDetail(id) {
            try {
                const response = await fetch(`/api/roles/${id}`);
                if (response.status === 401) {
                    window.location.href = '/login';
                    return;
                }
                if (!response.ok) throw new Error('Không thể tải thông tin vai trò');
                const role = await response.json();

                roleNameInput.value = role.name || '';
                roleCodeInput.value = role.code || '';
                roleDescriptionInput.value = role.description || '';
                roleActiveCheckbox.checked = role.active;
                activeStatusLabel.textContent = role.active
                    ? 'Vai trò đang được kích hoạt và cho phép gán cho tài khoản'
                    : 'Vai trò đang bị khóa và không thể gán cho tài khoản';

                if (role.code === 'ADMIN') {
                    roleActiveCheckbox.disabled = true;
                }

                selectedPermissionCodes = new Set(role.permissionCodes || []);
                renderPermissionMatrix();
            } catch (err) {
                showToast('Lỗi: ' + err.message, true);
            }
        }

        // Render ma trận quyền hạn dạng Card cho từng Phân hệ
        function renderPermissionMatrix(filterKeyword = '') {
            const moduleLabels = {
                'USER': { name: 'Quản lý Người dùng & Tài khoản', icon: 'ph-users' },
                'ROLE': { name: 'Quản lý Vai trò & Phân quyền', icon: 'ph-shield-check' },
                'SERVICE': { name: 'Danh mục Dịch vụ y tế', icon: 'ph-first-aid' },
                'LOCATION': { name: 'Quản lý Địa điểm khám', icon: 'ph-buildings' },
                'PATIENT': { name: 'Hồ sơ Người bệnh', icon: 'ph-identification-card' },
                'CONTRACT': { name: 'Quản lý Hợp đồng khám', icon: 'ph-file-text' },
                'APPOINTMENT': { name: 'Lịch hẹn khám bệnh', icon: 'ph-calendar-check' },
                'RECORD': { name: 'Kết luận khám & Bệnh án', icon: 'ph-clipboard-text' }
            };

            // Lọc theo từ khóa tìm kiếm nếu có
            const filtered = allPermissions.filter(p => {
                if (!filterKeyword) return true;
                return (p.name && p.name.toLowerCase().includes(filterKeyword)) ||
                       (p.code && p.code.toLowerCase().includes(filterKeyword)) ||
                       (p.module && p.module.toLowerCase().includes(filterKeyword));
            });

            // Gom nhóm theo Module
            const grouped = {};
            filtered.forEach(p => {
                const mod = p.module || 'KHAC';
                if (!grouped[mod]) grouped[mod] = [];
                grouped[mod].push(p);
            });

            if (Object.keys(grouped).length === 0) {
                permMatrixContainer.innerHTML = `
                    <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: var(--slate-400);">
                        Không tìm thấy quyền hạn nào khớp với từ khóa tìm kiếm
                    </div>
                `;
                selectedPermCount.textContent = selectedPermissionCodes.size;
                return;
            }

            let html = '';
            for (const [mod, perms] of Object.entries(grouped)) {
                const modInfo = moduleLabels[mod] || { name: `Phân hệ ${mod}`, icon: 'ph-folder' };
                const allModuleCodes = perms.map(p => p.code);
                const isAllModuleChecked = allModuleCodes.every(code => selectedPermissionCodes.has(code));

                html += `
                    <div class="perm-card-box">
                        <div class="perm-card-header">
                            <div class="perm-card-header-title">
                                <i class="ph-bold ${modInfo.icon}"></i>
                                <span>${escapeHtml(modInfo.name)}</span>
                            </div>
                            <label style="font-size: 12.5px; cursor: pointer; display: flex; align-items: center; gap: 6px; user-select: none;">
                                <input type="checkbox" class="check-module-all" data-module="${escapeHtml(mod)}" ${isAllModuleChecked ? 'checked' : ''} onchange="toggleModuleAll('${escapeHtml(mod)}', this.checked)">
                                <span style="color: var(--slate-600); font-weight: 500;">Chọn cả nhóm</span>
                            </label>
                        </div>

                        <div class="perm-card-body">
                            ${perms.map(p => {
                                const isChecked = selectedPermissionCodes.has(p.code);
                                return `
                                    <label class="perm-item-row ${isChecked ? 'checked' : ''}" id="perm_row_${p.code}">
                                        <input type="checkbox" value="${escapeHtml(p.code)}" data-module="${escapeHtml(mod)}" ${isChecked ? 'checked' : ''} onchange="toggleSinglePermission('${escapeHtml(p.code)}', this.checked)">
                                        <div class="perm-item-details">
                                            <div class="perm-item-name">
                                                <span>${escapeHtml(p.name)}</span>
                                                <span class="perm-item-code">${escapeHtml(p.code)}</span>
                                            </div>
                                            ${p.description ? `<span class="perm-item-desc">${escapeHtml(p.description)}</span>` : ''}
                                        </div>
                                    </label>
                                `;
                            }).join('')}
                        </div>
                    </div>
                `;
            }

            permMatrixContainer.innerHTML = html;
            selectedPermCount.textContent = selectedPermissionCodes.size;
        }

        function toggleSinglePermission(code, isChecked) {
            if (isChecked) {
                selectedPermissionCodes.add(code);
            } else {
                selectedPermissionCodes.delete(code);
            }

            const row = document.getElementById(`perm_row_${code}`);
            if (row) {
                if (isChecked) row.classList.add('checked');
                else row.classList.remove('checked');
            }

            updateSelectedCount();
        }

        function toggleModuleAll(mod, isChecked) {
            const modulePerms = allPermissions.filter(p => (p.module || 'KHAC') === mod);
            modulePerms.forEach(p => {
                if (isChecked) {
                    selectedPermissionCodes.add(p.code);
                } else {
                    selectedPermissionCodes.delete(p.code);
                }
                const row = document.getElementById(`perm_row_${p.code}`);
                if (row) {
                    const cb = row.querySelector('input[type="checkbox"]');
                    if (cb) cb.checked = isChecked;
                    if (isChecked) row.classList.add('checked');
                    else row.classList.remove('checked');
                }
            });

            updateSelectedCount();
        }

        function updateAllCheckboxesFromState() {
            renderPermissionMatrix(permSearchInput.value.trim().toLowerCase());
        }

        function updateSelectedCount() {
            selectedPermCount.textContent = selectedPermissionCodes.size;

            // Kiểm tra và cập nhật các checkbox "Chọn cả nhóm"
            const moduleCheckAlls = permMatrixContainer.querySelectorAll('.check-module-all');
            moduleCheckAlls.forEach(headerCb => {
                const mod = headerCb.getAttribute('data-module');
                const modulePerms = allPermissions.filter(p => (p.module || 'KHAC') === mod);
                const allChecked = modulePerms.length > 0 && modulePerms.every(p => selectedPermissionCodes.has(p.code));
                headerCb.checked = allChecked;
            });
        }

        // Xử lý gửi Form Lưu vai trò
        async function handleFormSubmit(e) {
            e.preventDefault();

            const name = roleNameInput.value.trim();
            const code = roleCodeInput.value.trim().toUpperCase();
            const description = roleDescriptionInput.value.trim();
            const active = roleActiveCheckbox.checked;
            const permissionCodes = Array.from(selectedPermissionCodes);

            const payload = isEditMode
                ? { name, description: description || null, active, permissionCodes }
                : { code, name, description: description || null, active, permissionCodes };

            try {
                const url = isEditMode ? `/api/roles/${roleId}` : '/api/roles';
                const method = isEditMode ? 'PUT' : 'POST';

                const response = await fetch(url, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                if (!response.ok) {
                    const errData = await response.json().catch(() => ({}));
                    throw new Error(errData.message || (isEditMode ? 'Cập nhật vai trò thất bại' : 'Thêm vai trò thất bại'));
                }

                showToast(isEditMode ? 'Đã cập nhật vai trò & phân quyền thành công!' : 'Đã tạo vai trò mới thành công!');
                
                // Quay lại trang danh sách sau 1 giây
                setTimeout(() => {
                    window.location.href = '/admin/roles';
                }, 1000);

            } catch (err) {
                showToast(err.message, true);
            }
        }

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

        function escapeHtml(str) {
            if (str == null) return '';
            return String(str)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }
