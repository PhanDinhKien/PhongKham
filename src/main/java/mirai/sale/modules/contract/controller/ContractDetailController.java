package mirai.sale.modules.contract.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.modules.contract.dto.*;
import mirai.sale.modules.contract.service.ContractDetailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts/{contractId}")
public class ContractDetailController {

    private final ContractDetailService contractDetailService;

    public ContractDetailController(ContractDetailService contractDetailService) {
        this.contractDetailService = contractDetailService;
    }

    private String getCurrentUser(Principal principal) {
        return principal != null ? principal.getName() : "system";
    }

    @GetMapping("/detail")
    public ContractDetailResponse getContractDetail(@PathVariable UUID contractId) {
        return contractDetailService.getContractDetail(contractId);
    }

    // =========================================================================
    // 1. PATIENTS
    // =========================================================================
    @GetMapping("/patients")
    public PageResponse<ContractPatientResponse> getContractPatients(
        @PathVariable UUID contractId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return contractDetailService.getContractPatients(contractId, keyword, status, page, size);
    }

    @PostMapping("/patients")
    public ResponseEntity<ContractPatientResponse> addPatientToContract(
        @PathVariable UUID contractId,
        @Valid @RequestBody AddPatientToContractRequest req,
        Principal principal
    ) {
        ContractPatientResponse res = contractDetailService.addPatientToContract(contractId, req, getCurrentUser(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @DeleteMapping("/patients/{patientId}")
    public ResponseEntity<Map<String, String>> removePatientFromContract(
        @PathVariable UUID contractId,
        @PathVariable UUID patientId
    ) {
        contractDetailService.removePatientFromContract(contractId, patientId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa bệnh nhân khỏi hợp đồng thành công"));
    }

    // =========================================================================
    // 2. LOCATIONS
    // =========================================================================
    @GetMapping("/locations")
    public List<ContractLocationResponse> getContractLocations(@PathVariable UUID contractId) {
        return contractDetailService.getContractLocations(contractId);
    }

    @PostMapping("/locations")
    public ResponseEntity<ContractLocationResponse> assignLocationToContract(
        @PathVariable UUID contractId,
        @Valid @RequestBody AssignLocationRequest req,
        Principal principal
    ) {
        ContractLocationResponse res = contractDetailService.assignLocationToContract(contractId, req, getCurrentUser(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @DeleteMapping("/locations/{locationId}")
    public ResponseEntity<Map<String, String>> removeLocationFromContract(
        @PathVariable UUID contractId,
        @PathVariable UUID locationId
    ) {
        contractDetailService.removeLocationFromContract(contractId, locationId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa địa điểm khỏi hợp đồng thành công"));
    }

    // =========================================================================
    // 3. EXAM PACKAGES
    // =========================================================================
    @GetMapping("/packages")
    public List<ExamPackageResponse> getContractPackages(@PathVariable UUID contractId) {
        return contractDetailService.getContractPackages(contractId);
    }

    @PostMapping("/packages")
    public ResponseEntity<ExamPackageResponse> createPackage(
        @PathVariable UUID contractId,
        @Valid @RequestBody CreateExamPackageRequest req,
        Principal principal
    ) {
        ExamPackageResponse res = contractDetailService.createPackage(contractId, req, getCurrentUser(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PutMapping("/packages/{packageId}")
    public ResponseEntity<ExamPackageResponse> updatePackage(
        @PathVariable UUID contractId,
        @PathVariable UUID packageId,
        @Valid @RequestBody UpdateExamPackageRequest req,
        Principal principal
    ) {
        ExamPackageResponse res = contractDetailService.updatePackage(contractId, packageId, req, getCurrentUser(principal));
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/packages/{packageId}")
    public ResponseEntity<Map<String, String>> deletePackage(
        @PathVariable UUID contractId,
        @PathVariable UUID packageId,
        Principal principal
    ) {
        contractDetailService.deletePackage(contractId, packageId, getCurrentUser(principal));
        return ResponseEntity.ok(Map.of("message", "Đã xóa gói khám thành công"));
    }

    @PostMapping("/packages/{packageId}/services")
    public ResponseEntity<PackageServiceResponse> assignServiceToPackage(
        @PathVariable UUID contractId,
        @PathVariable UUID packageId,
        @Valid @RequestBody AssignServiceToPackageRequest req,
        Principal principal
    ) {
        PackageServiceResponse res = contractDetailService.assignServiceToPackage(contractId, packageId, req, getCurrentUser(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @DeleteMapping("/packages/{packageId}/services/{serviceId}")
    public ResponseEntity<Map<String, String>> removeServiceFromPackage(
        @PathVariable UUID contractId,
        @PathVariable UUID packageId,
        @PathVariable UUID serviceId
    ) {
        contractDetailService.removeServiceFromPackage(contractId, packageId, serviceId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa dịch vụ khỏi gói khám thành công"));
    }

    @PutMapping("/packages/{packageId}/services/sync")
    public ResponseEntity<List<PackageServiceResponse>> syncPackageServices(
        @PathVariable UUID contractId,
        @PathVariable UUID packageId,
        @RequestBody SyncPackageServicesRequest req,
        Principal principal
    ) {
        List<PackageServiceResponse> res = contractDetailService.syncPackageServices(
            contractId,
            packageId,
            req != null ? req.serviceIds() : List.of(),
            getCurrentUser(principal)
        );
        return ResponseEntity.ok(res);
    }
}
