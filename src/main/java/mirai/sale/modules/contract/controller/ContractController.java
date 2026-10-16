package mirai.sale.modules.contract.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.modules.contract.dto.ContractResponse;
import mirai.sale.modules.contract.dto.CreateContractRequest;
import mirai.sale.modules.contract.dto.UpdateContractRequest;
import mirai.sale.modules.contract.entity.ContractStatus;
import mirai.sale.modules.contract.service.ContractService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    /**
     * Tìm kiếm và phân trang danh sách hợp đồng
     */
    @GetMapping("/search")
    public PageResponse<ContractResponse> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) ContractStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return contractService.search(keyword, status, page, size);
    }

    /**
     * Lấy thông tin chi tiết một hợp đồng
     */
    @GetMapping("/{id}")
    public ContractResponse getById(@PathVariable UUID id) {
        return contractService.getById(id);
    }

    /**
     * Lấy mã gợi ý tiếp theo cho hợp đồng mới
     */
    @GetMapping("/next-code")
    public Map<String, String> getNextContractCode() {
        return Map.of("nextCode", contractService.generateNextContractCode());
    }

    /**
     * Thêm mới hợp đồng
     */
    @PostMapping
    public ResponseEntity<ContractResponse> create(@Valid @RequestBody CreateContractRequest req, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        ContractResponse created = contractService.create(req, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật thông tin hợp đồng
     */
    @PutMapping("/{id}")
    public ContractResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateContractRequest req,
        Principal principal
    ) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        return contractService.update(id, req, currentUser);
    }

    /**
     * Xóa mềm hợp đồng
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "Admin";
        contractService.delete(id, currentUser);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đã xóa mềm hợp đồng thành công"
        ));
    }
}
