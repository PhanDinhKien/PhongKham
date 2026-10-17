package mirai.sale.modules.location.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.modules.location.dto.*;
import mirai.sale.modules.location.service.LocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Tìm kiếm và phân trang danh sách địa điểm khám
     */
    @GetMapping("/search")
    public PageResponse<LocationResponse> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean active,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return locationService.search(keyword, active, page, size);
    }

    /**
     * Lấy danh sách địa điểm đang hoạt động và ĐÃ CÓ CẤU HÌNH CA LÀM VIỆC
     */
    @GetMapping("/active-with-shifts")
    public List<LocationResponse> getActiveLocationsWithShifts() {
        return locationService.getActiveLocationsWithShifts();
    }

    /**
     * Lấy thông tin chi tiết một địa điểm
     */
    @GetMapping("/{id}")
    public LocationResponse getById(@PathVariable UUID id) {
        return locationService.getById(id);
    }

    /**
     * Thêm mới địa điểm khám
     */
    @PostMapping
    public ResponseEntity<LocationResponse> create(@Valid @RequestBody CreateLocationRequest req, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        LocationResponse created = locationService.create(req, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật địa điểm khám
     */
    @PutMapping("/{id}")
    public LocationResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateLocationRequest req,
        Principal principal
    ) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        return locationService.update(id, req, currentUser);
    }

    /**
     * Xóa mềm địa điểm khám
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        locationService.softDelete(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Đã xóa địa điểm thành công"));
    }

    /**
     * Bật / tắt trạng thái hoạt động của địa điểm
     */
    @PatchMapping("/{id}/toggle-status")
    public LocationResponse toggleStatus(@PathVariable UUID id, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        return locationService.toggleStatus(id, currentUser);
    }

    // ==============================================================
    // CONTRACTS ASSIGNED TO LOCATION
    // ==============================================================

    /**
     * Lấy danh sách hợp đồng đã gán vào địa điểm khám
     */
    @GetMapping("/{id}/contracts")
    public List<LocationContractResponse> getContractsByLocation(@PathVariable UUID id) {
        return locationService.getContractsByLocation(id);
    }

    /**
     * Thêm hợp đồng vào địa điểm khám
     */
    @PostMapping("/{id}/contracts")
    public ResponseEntity<LocationContractResponse> addContractToLocation(
        @PathVariable UUID id,
        @Valid @RequestBody AssignContractToLocationRequest req,
        Principal principal
    ) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        LocationContractResponse res = locationService.addContractToLocation(id, req, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * Bật / tắt trạng thái áp dụng của hợp đồng tại địa điểm khám
     */
    @PatchMapping("/{id}/contracts/{contractLocationId}/toggle-status")
    public LocationContractResponse toggleContractLocationStatus(
        @PathVariable UUID id,
        @PathVariable UUID contractLocationId
    ) {
        return locationService.toggleContractLocationStatus(id, contractLocationId);
    }

    /**
     * Gỡ hợp đồng khỏi địa điểm khám
     */
    @DeleteMapping("/{id}/contracts/{contractLocationId}")
    public ResponseEntity<Map<String, String>> removeContractFromLocation(
        @PathVariable UUID id,
        @PathVariable UUID contractLocationId
    ) {
        locationService.removeContractFromLocation(id, contractLocationId);
        return ResponseEntity.ok(Map.of("message", "Đã gỡ hợp đồng khỏi địa điểm"));
    }
}
