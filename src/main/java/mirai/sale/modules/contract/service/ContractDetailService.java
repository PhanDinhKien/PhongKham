package mirai.sale.modules.contract.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.common.exception.BadRequestException;
import mirai.sale.common.exception.NotFoundException;
import mirai.sale.modules.contract.dto.*;
import mirai.sale.modules.contract.entity.Contract;
import mirai.sale.modules.contract.entity.ContractLocation;
import mirai.sale.modules.contract.entity.ContractPatient;
import mirai.sale.modules.contract.entity.ExamPackage;
import mirai.sale.modules.contract.entity.PackageService;
import mirai.sale.modules.contract.repository.ContractLocationRepository;
import mirai.sale.modules.contract.repository.ContractPatientRepository;
import mirai.sale.modules.contract.repository.ContractRepository;
import mirai.sale.modules.contract.repository.ExamPackageRepository;
import mirai.sale.modules.contract.repository.PackageServiceRepository;
import mirai.sale.modules.location.entity.Location;
import mirai.sale.modules.location.repository.LocationRepository;
import mirai.sale.modules.patient.entity.Patient;
import mirai.sale.modules.patient.repository.PatientRepository;
import mirai.sale.modules.service.entity.MedicalService;
import mirai.sale.modules.service.repository.MedicalServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractDetailService {

    private final ContractRepository contractRepository;
    private final ContractPatientRepository contractPatientRepository;
    private final ContractLocationRepository contractLocationRepository;
    private final ExamPackageRepository examPackageRepository;
    private final PackageServiceRepository packageServiceRepository;
    private final PatientRepository patientRepository;
    private final LocationRepository locationRepository;
    private final MedicalServiceRepository medicalServiceRepository;

    public ContractDetailService(
        ContractRepository contractRepository,
        ContractPatientRepository contractPatientRepository,
        ContractLocationRepository contractLocationRepository,
        ExamPackageRepository examPackageRepository,
        PackageServiceRepository packageServiceRepository,
        PatientRepository patientRepository,
        LocationRepository locationRepository,
        MedicalServiceRepository medicalServiceRepository
    ) {
        this.contractRepository = contractRepository;
        this.contractPatientRepository = contractPatientRepository;
        this.contractLocationRepository = contractLocationRepository;
        this.examPackageRepository = examPackageRepository;
        this.packageServiceRepository = packageServiceRepository;
        this.patientRepository = patientRepository;
        this.locationRepository = locationRepository;
        this.medicalServiceRepository = medicalServiceRepository;
    }

    private Contract getActiveContract(UUID contractId) {
        return contractRepository.findByIdAndIsDeletedFalse(contractId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin hợp đồng"));
    }

    @Transactional(readOnly = true)
    public ContractDetailResponse getContractDetail(UUID contractId) {
        Contract contract = getActiveContract(contractId);
        long patientCount = contractPatientRepository.countByContractId(contractId);
        long locationCount = contractLocationRepository.countByContractId(contractId);
        long packageCount = examPackageRepository.countByContractIdAndIsDeletedFalse(contractId);
        return ContractDetailResponse.from(contract, patientCount, locationCount, packageCount);
    }

    // =========================================================================
    // 1. PATIENTS (DS HỒ SƠ KHÁM)
    // =========================================================================
    @Transactional(readOnly = true)
    public PageResponse<ContractPatientResponse> getContractPatients(
        UUID contractId,
        String keyword,
        String status,
        int page,
        int size
    ) {
        getActiveContract(contractId);
        Page<ContractPatient> result = contractPatientRepository.searchContractPatients(
            contractId,
            keyword != null && !keyword.isBlank() ? keyword.trim() : null,
            status != null && !status.isBlank() ? status.trim() : null,
            PageRequest.of(page, size)
        );

        List<ContractPatientResponse> items = result.getContent().stream()
            .map(ContractPatientResponse::from)
            .toList();

        return new PageResponse<>(items, page, size, result.getTotalElements());
    }

    @Transactional
    public ContractPatientResponse addPatientToContract(UUID contractId, AddPatientToContractRequest req, String currentUser) {
        Contract contract = getActiveContract(contractId);
        Patient patient = patientRepository.findByIdAndIsDeletedFalse(req.patientId())
            .orElseThrow(() -> new NotFoundException("Không tìm thấy người bệnh"));

        if (contractPatientRepository.existsByContractIdAndPatientId(contractId, patient.getId())) {
            throw new BadRequestException("Người bệnh này đã được thêm vào hợp đồng!");
        }

        ContractPatient cp = new ContractPatient(
            contract,
            patient,
            req.recordCode() != null && !req.recordCode().isBlank() ? req.recordCode().trim() : null,
            req.doctorName() != null && !req.doctorName().isBlank() ? req.doctorName().trim() : null,
            req.examDate(),
            req.status() != null && !req.status().isBlank() ? req.status().trim() : "MOI",
            currentUser
        );

        ContractPatient saved = contractPatientRepository.save(cp);
        return ContractPatientResponse.from(saved);
    }

    @Transactional
    public void removePatientFromContract(UUID contractId, UUID patientId) {
        getActiveContract(contractId);
        ContractPatient cp = contractPatientRepository.findByContractIdAndPatientId(contractId, patientId)
            .orElseThrow(() -> new NotFoundException("Bệnh nhân chưa có trong hợp đồng này"));
        contractPatientRepository.delete(cp);
    }

    // =========================================================================
    // 2. LOCATIONS (ĐỊA ĐIỂM KHÁM)
    // =========================================================================
    @Transactional(readOnly = true)
    public List<ContractLocationResponse> getContractLocations(UUID contractId) {
        getActiveContract(contractId);
        return contractLocationRepository.findByContractIdWithLocation(contractId).stream()
            .map(ContractLocationResponse::from)
            .toList();
    }

    @Transactional
    public ContractLocationResponse assignLocationToContract(UUID contractId, AssignLocationRequest req, String currentUser) {
        Contract contract = getActiveContract(contractId);
        Location location = locationRepository.findByIdAndIsDeletedFalse(req.locationId())
            .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm khám"));

        if (contractLocationRepository.existsByContractIdAndLocationId(contractId, location.getId())) {
            throw new BadRequestException("Địa điểm khám này đã được gán vào hợp đồng!");
        }

        ContractLocation cl = new ContractLocation(contract, location, currentUser);
        ContractLocation saved = contractLocationRepository.save(cl);
        return ContractLocationResponse.from(saved);
    }

    @Transactional
    public void removeLocationFromContract(UUID contractId, UUID locationId) {
        getActiveContract(contractId);
        ContractLocation cl = contractLocationRepository.findByContractIdAndLocationId(contractId, locationId)
            .orElseThrow(() -> new NotFoundException("Địa điểm chưa có trong hợp đồng này"));
        contractLocationRepository.delete(cl);
    }

    // =========================================================================
    // 3. EXAM PACKAGES (GÓI KHÁM & DỊCH VỤ TRONG GÓI)
    // =========================================================================
    @Transactional(readOnly = true)
    public List<ExamPackageResponse> getContractPackages(UUID contractId) {
        getActiveContract(contractId);
        List<ExamPackage> packages = examPackageRepository.findByContractIdAndIsDeletedFalseOrderByCreatedAtDesc(contractId);

        return packages.stream().map(pkg -> {
            List<PackageServiceResponse> services = packageServiceRepository.findByPackageIdWithService(pkg.getId())
                .stream()
                .map(PackageServiceResponse::from)
                .toList();
            return ExamPackageResponse.from(pkg, services);
        }).toList();
    }

    @Transactional
    public ExamPackageResponse createPackage(UUID contractId, CreateExamPackageRequest req, String currentUser) {
        Contract contract = getActiveContract(contractId);
        String cleanCode = req.packageCode().trim().toUpperCase();

        if (examPackageRepository.existsByContractIdAndPackageCodeAndIsDeletedFalse(contractId, cleanCode)) {
            throw new BadRequestException("Mã gói khám '" + cleanCode + "' đã tồn tại trong hợp đồng này!");
        }

        ExamPackage pkg = new ExamPackage(
            contract,
            cleanCode,
            req.packageName().trim(),
            req.status() != null && !req.status().isBlank() ? req.status().trim() : "HIEU_LUC",
            currentUser
        );

        ExamPackage saved = examPackageRepository.save(pkg);
        return ExamPackageResponse.from(saved, List.of());
    }

    @Transactional
    public ExamPackageResponse updatePackage(UUID contractId, UUID packageId, UpdateExamPackageRequest req, String currentUser) {
        getActiveContract(contractId);
        ExamPackage pkg = examPackageRepository.findByIdAndIsDeletedFalse(packageId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy gói khám"));

        if (!pkg.getContract().getId().equals(contractId)) {
            throw new BadRequestException("Gói khám không thuộc hợp đồng này");
        }

        pkg.setPackageName(req.packageName().trim());
        if (req.status() != null && !req.status().isBlank()) {
            pkg.setStatus(req.status().trim());
        }
        pkg.setUpdatedBy(currentUser);
        pkg.setUpdatedAt(LocalDateTime.now());

        ExamPackage saved = examPackageRepository.save(pkg);
        List<PackageServiceResponse> services = packageServiceRepository.findByPackageIdWithService(saved.getId())
            .stream()
            .map(PackageServiceResponse::from)
            .toList();

        return ExamPackageResponse.from(saved, services);
    }

    @Transactional
    public void deletePackage(UUID contractId, UUID packageId, String currentUser) {
        getActiveContract(contractId);
        ExamPackage pkg = examPackageRepository.findByIdAndIsDeletedFalse(packageId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy gói khám"));

        if (!pkg.getContract().getId().equals(contractId)) {
            throw new BadRequestException("Gói khám không thuộc hợp đồng này");
        }

        pkg.setDeleted(true);
        pkg.setDeletedAt(LocalDateTime.now());
        pkg.setDeletedBy(currentUser);
        examPackageRepository.save(pkg);
    }

    @Transactional
    public PackageServiceResponse assignServiceToPackage(
        UUID contractId,
        UUID packageId,
        AssignServiceToPackageRequest req,
        String currentUser
    ) {
        getActiveContract(contractId);
        ExamPackage pkg = examPackageRepository.findByIdAndIsDeletedFalse(packageId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy gói khám"));

        if (!pkg.getContract().getId().equals(contractId)) {
            throw new BadRequestException("Gói khám không thuộc hợp đồng này");
        }

        MedicalService service = medicalServiceRepository.findByIdAndIsDeletedFalse(req.serviceId())
            .orElseThrow(() -> new NotFoundException("Không tìm thấy dịch vụ"));

        if (packageServiceRepository.existsByExamPackageIdAndMedicalServiceId(packageId, service.getId())) {
            throw new BadRequestException("Dịch vụ này đã được gán vào gói khám!");
        }

        PackageService ps = new PackageService(pkg, service, currentUser);
        PackageService saved = packageServiceRepository.save(ps);
        return PackageServiceResponse.from(saved);
    }

    @Transactional
    public void removeServiceFromPackage(UUID contractId, UUID packageId, UUID serviceId) {
        getActiveContract(contractId);
        ExamPackage pkg = examPackageRepository.findByIdAndIsDeletedFalse(packageId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy gói khám"));

        if (!pkg.getContract().getId().equals(contractId)) {
            throw new BadRequestException("Gói khám không thuộc hợp đồng này");
        }

        PackageService ps = packageServiceRepository.findByExamPackageIdAndMedicalServiceId(packageId, serviceId)
            .orElseThrow(() -> new NotFoundException("Dịch vụ chưa được gán vào gói này"));

        packageServiceRepository.delete(ps);
    }

    @Transactional
    public List<PackageServiceResponse> syncPackageServices(
        UUID contractId,
        UUID packageId,
        List<UUID> targetServiceIds,
        String currentUser
    ) {
        getActiveContract(contractId);
        ExamPackage pkg = examPackageRepository.findByIdAndIsDeletedFalse(packageId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy gói khám"));

        if (!pkg.getContract().getId().equals(contractId)) {
            throw new BadRequestException("Gói khám không thuộc hợp đồng này");
        }

        List<UUID> validTargetIds = targetServiceIds != null ? targetServiceIds : List.of();
        List<PackageService> currentList = packageServiceRepository.findByPackageIdWithService(packageId);

        // Map serviceId -> PackageService
        java.util.Map<UUID, PackageService> currentMap = new java.util.HashMap<>();
        for (PackageService ps : currentList) {
            currentMap.put(ps.getMedicalService().getId(), ps);
        }

        // Xóa những dịch vụ bị bỏ tích
        for (java.util.Map.Entry<UUID, PackageService> entry : currentMap.entrySet()) {
            if (!validTargetIds.contains(entry.getKey())) {
                packageServiceRepository.delete(entry.getValue());
            }
        }

        // Thêm những dịch vụ mới được tích
        for (UUID sId : validTargetIds) {
            if (!currentMap.containsKey(sId)) {
                MedicalService ms = medicalServiceRepository.findByIdAndIsDeletedFalse(sId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy dịch vụ y tế: " + sId));
                PackageService newPs = new PackageService(pkg, ms, currentUser);
                packageServiceRepository.save(newPs);
            }
        }

        return packageServiceRepository.findByPackageIdWithService(packageId).stream()
            .map(PackageServiceResponse::from)
            .toList();
    }
}
