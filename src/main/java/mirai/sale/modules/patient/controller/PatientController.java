package mirai.sale.modules.patient.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.modules.patient.dto.CreatePatientRequest;
import mirai.sale.modules.patient.dto.PatientResponse;
import mirai.sale.modules.patient.dto.UpdatePatientRequest;
import mirai.sale.modules.patient.entity.Gender;
import mirai.sale.modules.patient.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Tìm kiếm và phân trang danh sách người bệnh
     */
    @GetMapping("/search")
    public PageResponse<PatientResponse> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Gender gender,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return patientService.search(keyword, gender, page, size);
    }

    /**
     * Lấy thông tin chi tiết một người bệnh
     */
    @GetMapping("/{id}")
    public PatientResponse getById(@PathVariable UUID id) {
        return patientService.getById(id);
    }

    /**
     * Lấy mã gợi ý tiếp theo cho người bệnh mới
     */
    @GetMapping("/next-code")
    public Map<String, String> getNextPatientCode() {
        return Map.of("nextCode", patientService.generateNextPatientCode());
    }

    /**
     * Thêm mới người bệnh (có kiểm tra trùng mã)
     */
    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody CreatePatientRequest req, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        PatientResponse created = patientService.create(req, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật thông tin người bệnh (có kiểm tra trùng mã)
     */
    @PutMapping("/{id}")
    public PatientResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePatientRequest req,
        Principal principal
    ) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        return patientService.update(id, req, currentUser);
    }

    /**
     * Xóa mềm người bệnh
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        patientService.softDelete(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Đã xóa người bệnh thành công"));
    }
}
