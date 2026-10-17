package mirai.sale.modules.quota.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.common.exception.BadRequestException;
import mirai.sale.common.exception.NotFoundException;
import mirai.sale.modules.contract.entity.Contract;
import mirai.sale.modules.contract.repository.ContractRepository;
import mirai.sale.modules.location.entity.Location;
import mirai.sale.modules.location.repository.LocationRepository;
import mirai.sale.modules.quota.dto.*;
import mirai.sale.modules.quota.entity.ServiceQuota;
import mirai.sale.modules.quota.repository.ServiceQuotaRepository;
import mirai.sale.modules.service.entity.MedicalService;
import mirai.sale.modules.service.entity.ServiceType;
import mirai.sale.modules.service.repository.MedicalServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceQuotaService {

    private final ServiceQuotaRepository serviceQuotaRepository;
    private final MedicalServiceRepository medicalServiceRepository;
    private final LocationRepository locationRepository;
    private final ContractRepository contractRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ServiceQuotaService(
        ServiceQuotaRepository serviceQuotaRepository,
        MedicalServiceRepository medicalServiceRepository,
        LocationRepository locationRepository,
        ContractRepository contractRepository
    ) {
        this.serviceQuotaRepository = serviceQuotaRepository;
        this.medicalServiceRepository = medicalServiceRepository;
        this.locationRepository = locationRepository;
        this.contractRepository = contractRepository;
    }

    /**
     * Tìm kiếm danh sách dịch vụ kèm số lượng định mức đang áp dụng (Bảng cha)
     */
    @Transactional(readOnly = true)
    public PageResponse<ServiceWithQuotaResponse> searchServicesWithQuotas(
        String keyword,
        String serviceTypeStr,
        int page,
        int size
    ) {
        ServiceType serviceType = null;
        if (serviceTypeStr != null && !serviceTypeStr.isBlank()) {
            try {
                serviceType = ServiceType.valueOf(serviceTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Keep null if invalid
            }
        }

        Page<MedicalService> pageResult = medicalServiceRepository.searchServices(
            keyword != null && !keyword.isBlank() ? keyword.trim() : null,
            serviceType,
            null,
            PageRequest.of(page, size)
        );

        List<ServiceWithQuotaResponse> items = pageResult.getContent().stream()
            .map(s -> {
                long activeCount = serviceQuotaRepository.countActiveByServiceId(s.getId());
                long totalCount = serviceQuotaRepository.countTotalByServiceId(s.getId());
                return ServiceWithQuotaResponse.from(s, activeCount, totalCount);
            })
            .toList();

        return new PageResponse<>(items, page, size, pageResult.getTotalElements());
    }

    /**
     * Lấy danh sách định mức của một dịch vụ (Bảng con khi mở rộng dòng)
     */
    @Transactional(readOnly = true)
    public List<ServiceQuotaResponse> getQuotasByService(UUID serviceId) {
        if (!medicalServiceRepository.existsById(serviceId)) {
            throw new NotFoundException("Không tìm thấy dịch vụ y tế");
        }
        return serviceQuotaRepository.findByServiceIdWithDetails(serviceId).stream()
            .map(ServiceQuotaResponse::fromEntity)
            .toList();
    }

    /**
     * Lấy thông tin chi tiết một định mức
     */
    @Transactional(readOnly = true)
    public ServiceQuotaResponse getById(UUID id) {
        ServiceQuota sq = serviceQuotaRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy định mức dịch vụ"));
        return ServiceQuotaResponse.fromEntity(sq);
    }

    /**
     * Thêm mới hoặc cập nhật định mức dịch vụ cho Địa điểm và Hợp đồng
     */
    @Transactional
    public ServiceQuotaResponse create(CreateServiceQuotaRequest req, String currentUser) {
        MedicalService service = medicalServiceRepository.findByIdAndIsDeletedFalse(req.serviceId())
            .orElseThrow(() -> new NotFoundException("Không tìm thấy dịch vụ y tế"));

        Location loc = locationRepository.findByIdAndIsDeletedFalse(req.locationId())
            .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm khám ID: " + req.locationId()));

        if (!loc.isActive() || loc.getShifts() == null || loc.getShifts().isEmpty()) {
            throw new BadRequestException("Địa điểm khám này chưa có cấu hình ca làm việc hoặc không hoạt động");
        }

        Contract contract = null;
        if (req.contractId() != null) {
            contract = contractRepository.findByIdAndIsDeletedFalse(req.contractId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hợp đồng được chọn"));
        }

        int s1 = req.shift1Quantity() != null ? req.shift1Quantity() : 0;
        int s2 = req.shift2Quantity() != null ? req.shift2Quantity() : 0;
        int totalMax = s1 + s2;

        if (req.shiftDetails() != null && !req.shiftDetails().isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(req.shiftDetails());
                if (root.isArray()) {
                    int sum = 0;
                    for (JsonNode node : root) {
                        int q = node.has("quantity") ? node.get("quantity").asInt(0) : 0;
                        sum += q;
                        int sNum = node.has("shiftNumber") ? node.get("shiftNumber").asInt(0) : 0;
                        if (sNum == 1) s1 = q;
                        if (sNum == 2) s2 = q;
                    }
                    totalMax = sum;
                }
            } catch (Exception ignored) {
            }
        }

        Optional<ServiceQuota> existingOpt = serviceQuotaRepository.findExistingQuota(
            service.getId(),
            loc.getId(),
            req.contractId()
        );

        ServiceQuota sq;
        if (existingOpt.isPresent()) {
            sq = existingOpt.get();
            sq.setShift1Quantity(s1);
            sq.setShift2Quantity(s2);
            sq.setMaxQuantity(totalMax);
            sq.setShiftDetails(req.shiftDetails());
            sq.setValidFrom(req.validFrom());
            sq.setValidTo(req.validTo());
            sq.setActive(req.active() != null ? req.active() : true);
            sq.setUpdatedBy(currentUser != null ? currentUser : "Admin");
        } else {
            sq = new ServiceQuota(
                service,
                loc,
                "THEO_CA",
                totalMax,
                s1,
                s2,
                req.validFrom(),
                req.validTo(),
                req.active() != null ? req.active() : true,
                currentUser != null ? currentUser : "Admin"
            );
            sq.setContract(contract);
            sq.setShiftDetails(req.shiftDetails());
        }

        ServiceQuota saved = serviceQuotaRepository.save(sq);
        return ServiceQuotaResponse.fromEntity(saved);
    }

    /**
     * Cập nhật một định mức dịch vụ
     */
    @Transactional
    public ServiceQuotaResponse update(UUID id, UpdateServiceQuotaRequest req, String currentUser) {
        ServiceQuota sq = serviceQuotaRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy định mức dịch vụ"));

        Location loc = sq.getLocation();
        if (req.locationId() != null && !req.locationId().equals(loc.getId())) {
            loc = locationRepository.findByIdAndIsDeletedFalse(req.locationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm khám"));

            if (!loc.isActive() || loc.getShifts() == null || loc.getShifts().isEmpty()) {
                throw new BadRequestException("Địa điểm khám này chưa có cấu hình ca làm việc hoặc không hoạt động");
            }
            sq.setLocation(loc);
        }

        Contract contract = sq.getContract();
        if (req.contractId() != null) {
            contract = contractRepository.findByIdAndIsDeletedFalse(req.contractId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hợp đồng"));
            sq.setContract(contract);
        }

        if (serviceQuotaRepository.existsExistingQuotaExcludingId(
            sq.getService().getId(),
            loc.getId(),
            contract != null ? contract.getId() : null,
            id
        )) {
            throw new BadRequestException("Định mức dịch vụ cho địa điểm và hợp đồng này đã tồn tại");
        }

        int s1 = req.shift1Quantity() != null ? req.shift1Quantity() : 0;
        int s2 = req.shift2Quantity() != null ? req.shift2Quantity() : 0;
        int totalMax = s1 + s2;

        if (req.shiftDetails() != null && !req.shiftDetails().isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(req.shiftDetails());
                if (root.isArray()) {
                    int sum = 0;
                    for (JsonNode node : root) {
                        int q = node.has("quantity") ? node.get("quantity").asInt(0) : 0;
                        sum += q;
                        int sNum = node.has("shiftNumber") ? node.get("shiftNumber").asInt(0) : 0;
                        if (sNum == 1) s1 = q;
                        if (sNum == 2) s2 = q;
                    }
                    totalMax = sum;
                }
            } catch (Exception ignored) {
            }
            sq.setShiftDetails(req.shiftDetails());
        }

        sq.setShift1Quantity(s1);
        sq.setShift2Quantity(s2);
        sq.setMaxQuantity(totalMax);

        sq.setValidFrom(req.validFrom());
        sq.setValidTo(req.validTo());
        if (req.active() != null) {
            sq.setActive(req.active());
        }
        sq.setUpdatedBy(currentUser != null ? currentUser : "Admin");

        ServiceQuota saved = serviceQuotaRepository.save(sq);
        return ServiceQuotaResponse.fromEntity(saved);
    }


    /**
     * Bật / tắt trạng thái áp dụng của định mức
     */
    @Transactional
    public ServiceQuotaResponse toggleStatus(UUID id, String currentUser) {
        ServiceQuota sq = serviceQuotaRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy định mức dịch vụ"));

        sq.setActive(!sq.isActive());
        sq.setUpdatedBy(currentUser != null ? currentUser : "Admin");

        ServiceQuota saved = serviceQuotaRepository.save(sq);
        return ServiceQuotaResponse.fromEntity(saved);
    }

    /**
     * Xóa định mức dịch vụ
     */
    @Transactional
    public void delete(UUID id) {
        ServiceQuota sq = serviceQuotaRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy định mức dịch vụ"));
        serviceQuotaRepository.delete(sq);
    }

    /**
     * Đồng bộ dữ liệu định mức dịch vụ từ hệ thống bệnh viện HIS (Mock)
     */
    @Transactional
    public int syncFromHis(String currentUser) {
        return (int) serviceQuotaRepository.count();
    }
}
