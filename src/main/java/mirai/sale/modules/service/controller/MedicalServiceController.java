package mirai.sale.modules.service.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.modules.service.dto.CreateServiceRequest;
import mirai.sale.modules.service.dto.ServiceResponse;
import mirai.sale.modules.service.dto.ServiceTypeOption;
import mirai.sale.modules.service.dto.UpdateServiceRequest;
import mirai.sale.modules.service.entity.ServiceType;
import mirai.sale.modules.service.service.MedicalServiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
public class MedicalServiceController {

    private final MedicalServiceService medicalServiceService;

    public MedicalServiceController(MedicalServiceService medicalServiceService) {
        this.medicalServiceService = medicalServiceService;
    }

    /**
     * Danh sách loại dịch vụ y tế để hiển thị lên dropdown
     */
    @GetMapping("/types")
    public List<ServiceTypeOption> getServiceTypes() {
        return Arrays.stream(ServiceType.values())
            .map(ServiceTypeOption::fromEnum)
            .toList();
    }

    /**
     * Tìm kiếm và phân trang danh mục dịch vụ
     */
    @GetMapping("/search")
    public PageResponse<ServiceResponse> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) ServiceType serviceType,
        @RequestParam(required = false) Boolean active,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return medicalServiceService.search(keyword, serviceType, active, page, size);
    }

    /**
     * Lấy thông tin chi tiết một dịch vụ
     */
    @GetMapping("/{id}")
    public ServiceResponse getById(@PathVariable UUID id) {
        return medicalServiceService.getById(id);
    }

    /**
     * Thêm mới dịch vụ y tế
     */
    @PostMapping
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody CreateServiceRequest req, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        ServiceResponse created = medicalServiceService.create(req, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật dịch vụ y tế
     */
    @PutMapping("/{id}")
    public ServiceResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateServiceRequest req,
        Principal principal
    ) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        return medicalServiceService.update(id, req, currentUser);
    }

    /**
     * Xóa mềm dịch vụ y tế
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        medicalServiceService.softDelete(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Đã xóa dịch vụ thành công"));
    }

    /**
     * Bật / tắt trạng thái hoạt động của dịch vụ
     */
    @PatchMapping("/{id}/toggle-status")
    public ServiceResponse toggleStatus(@PathVariable UUID id, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        return medicalServiceService.toggleStatus(id, currentUser);
    }
}
