package mirai.sale.modules.quota.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.modules.quota.dto.*;
import mirai.sale.modules.quota.service.ServiceQuotaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service-quotas")
public class ServiceQuotaController {

    private final ServiceQuotaService serviceQuotaService;

    public ServiceQuotaController(ServiceQuotaService serviceQuotaService) {
        this.serviceQuotaService = serviceQuotaService;
    }

    /**
     * Tìm kiếm và phân trang danh sách dịch vụ kèm tổng số định mức đang áp dụng (Bảng cha)
     */
    @GetMapping("/services")
    public PageResponse<ServiceWithQuotaResponse> searchServices(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String serviceType,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return serviceQuotaService.searchServicesWithQuotas(keyword, serviceType, page, size);
    }

    /**
     * Lấy danh sách định mức của một dịch vụ (Bảng con khi mở rộng)
     */
    @GetMapping("/services/{serviceId}/quotas")
    public List<ServiceQuotaResponse> getQuotasByService(@PathVariable UUID serviceId) {
        return serviceQuotaService.getQuotasByService(serviceId);
    }

    /**
     * Lấy thông tin chi tiết một định mức
     */
    @GetMapping("/{id}")
    public ServiceQuotaResponse getById(@PathVariable UUID id) {
        return serviceQuotaService.getById(id);
    }

    /**
     * Thêm mới định mức dịch vụ cho một địa điểm và hợp đồng
     */
    @PostMapping
    public ResponseEntity<ServiceQuotaResponse> create(
        @Valid @RequestBody CreateServiceQuotaRequest req,
        Principal principal
    ) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        ServiceQuotaResponse created = serviceQuotaService.create(req, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật thông tin một định mức
     */
    @PutMapping("/{id}")
    public ServiceQuotaResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateServiceQuotaRequest req,
        Principal principal
    ) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        return serviceQuotaService.update(id, req, currentUser);
    }

    /**
     * Bật / tắt trạng thái áp dụng của định mức
     */
    @PatchMapping("/{id}/toggle-status")
    public ServiceQuotaResponse toggleStatus(@PathVariable UUID id, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        return serviceQuotaService.toggleStatus(id, currentUser);
    }

    /**
     * Xóa định mức
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        serviceQuotaService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa định mức dịch vụ thành công"));
    }

    /**
     * Đồng bộ dữ liệu từ hệ thống HIS
     */
    @PostMapping("/sync-his")
    public ResponseEntity<Map<String, Object>> syncHis(Principal principal) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        int synced = serviceQuotaService.syncFromHis(currentUser);
        return ResponseEntity.ok(Map.of(
            "message", "Đồng bộ dữ liệu từ hệ thống HIS thành công",
            "syncedCount", synced
        ));
    }
}
