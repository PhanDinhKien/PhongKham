package mirai.sale.modules.receipt.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;
import mirai.sale.modules.receipt.dto.CreateReceiptRequest;
import mirai.sale.modules.receipt.dto.ReceiptDto;
import mirai.sale.modules.receipt.service.ReceiptService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping("/search")
    public Page<ReceiptDto> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String cashier,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String syncStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        LocalDateTime fromTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toTime = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return receiptService.search(keyword, company, cashier, paymentMethod, syncStatus, fromTime, toTime, pageable);
    }

    @GetMapping("/{id}")
    public ReceiptDto getById(@PathVariable UUID id) {
        return receiptService.getById(id);
    }

    @PostMapping
    public ResponseEntity<ReceiptDto> create(@Valid @RequestBody CreateReceiptRequest request, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "NV. Thu ngân";
        ReceiptDto created = receiptService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/toggle-sync")
    public ResponseEntity<ReceiptDto> toggleSync(@PathVariable UUID id) {
        return ResponseEntity.ok(receiptService.toggleSync(id));
    }

    @GetMapping("/filters")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        return ResponseEntity.ok(receiptService.getFilterOptions());
    }
}
