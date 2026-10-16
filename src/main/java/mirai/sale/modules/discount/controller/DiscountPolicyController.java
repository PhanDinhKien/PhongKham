package mirai.sale.modules.discount.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.modules.discount.constant.DiscountType;
import mirai.sale.modules.discount.dto.CreateDiscountPolicyRequest;
import mirai.sale.modules.discount.dto.DiscountPolicyDto;
import mirai.sale.modules.discount.dto.UpdateDiscountPolicyRequest;
import mirai.sale.modules.discount.service.DiscountPolicyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discounts")
public class DiscountPolicyController {

    private final DiscountPolicyService discountPolicyService;

    public DiscountPolicyController(DiscountPolicyService discountPolicyService) {
        this.discountPolicyService = discountPolicyService;
    }

    @GetMapping("/search")
    public Page<DiscountPolicyDto> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String companyOrContract,
            @RequestParam(required = false) DiscountType discountType,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return discountPolicyService.search(keyword, companyOrContract, discountType, active, fromDate, toDate, pageable);
    }

    @GetMapping("/{id}")
    public DiscountPolicyDto getById(@PathVariable UUID id) {
        return discountPolicyService.getById(id);
    }

    @GetMapping("/next-code")
    public Map<String, String> getNextCode() {
        return Map.of("nextCode", discountPolicyService.generateNextCode());
    }

    @PostMapping
    public ResponseEntity<DiscountPolicyDto> create(@Valid @RequestBody CreateDiscountPolicyRequest request, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "admin";
        DiscountPolicyDto created = discountPolicyService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscountPolicyDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDiscountPolicyRequest request,
            Principal principal
    ) {
        String currentUser = principal != null ? principal.getName() : "admin";
        DiscountPolicyDto updated = discountPolicyService.update(id, request, currentUser);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleStatus(@PathVariable UUID id, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "admin";
        boolean newStatus = discountPolicyService.toggleActive(id, currentUser);
        return ResponseEntity.ok(Map.of("id", id, "active", newStatus));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "admin";
        discountPolicyService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
