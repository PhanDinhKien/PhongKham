package mirai.sale.modules.service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.common.exception.BadRequestException;
import mirai.sale.common.exception.NotFoundException;
import mirai.sale.modules.service.dto.CreateServiceRequest;
import mirai.sale.modules.service.dto.ServiceResponse;
import mirai.sale.modules.service.dto.UpdateServiceRequest;
import mirai.sale.modules.service.entity.MedicalService;
import mirai.sale.modules.service.entity.ServiceType;
import mirai.sale.modules.service.repository.MedicalServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicalServiceService {

    private final MedicalServiceRepository medicalServiceRepository;

    public MedicalServiceService(MedicalServiceRepository medicalServiceRepository) {
        this.medicalServiceRepository = medicalServiceRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceResponse> search(String keyword, ServiceType serviceType, Boolean active, int page, int size) {
        Page<MedicalService> pageResult = medicalServiceRepository.searchServices(
            keyword != null && !keyword.isBlank() ? keyword.trim() : null,
            serviceType,
            active,
            PageRequest.of(page, size)
        );

        List<ServiceResponse> items = pageResult.getContent().stream()
            .map(ServiceResponse::fromEntity)
            .toList();

        return new PageResponse<>(items, page, size, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ServiceResponse getById(UUID id) {
        MedicalService service = medicalServiceRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy dịch vụ y tế"));
        return ServiceResponse.fromEntity(service);
    }

    @Transactional
    public ServiceResponse create(CreateServiceRequest req, String currentUser) {
        String cleanCode = req.code().trim().toUpperCase();
        if (medicalServiceRepository.existsByCodeAndIsDeletedFalse(cleanCode)) {
            throw new BadRequestException("Mã dịch vụ '" + cleanCode + "' đã tồn tại trong hệ thống");
        }

        MedicalService service = new MedicalService();
        service.setCode(cleanCode);
        service.setName(req.name().trim());
        service.setPrice(req.price());
        service.setServiceType(req.serviceType());
        service.setActive(req.active() != null ? req.active() : true);
        service.setDescription(req.description() != null ? req.description().trim() : null);
        service.setCreatedBy(currentUser != null ? currentUser : "Admin");
        service.setUpdatedBy(currentUser != null ? currentUser : "Admin");
        service.setDeleted(false);

        MedicalService saved = medicalServiceRepository.save(service);
        return ServiceResponse.fromEntity(saved);
    }

    @Transactional
    public ServiceResponse update(UUID id, UpdateServiceRequest req, String currentUser) {
        MedicalService service = medicalServiceRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy dịch vụ y tế"));

        service.setName(req.name().trim());
        service.setPrice(req.price());
        service.setServiceType(req.serviceType());
        if (req.active() != null) {
            service.setActive(req.active());
        }
        service.setDescription(req.description() != null ? req.description().trim() : null);
        service.setUpdatedBy(currentUser != null ? currentUser : "Admin");

        MedicalService saved = medicalServiceRepository.save(service);
        return ServiceResponse.fromEntity(saved);
    }

    @Transactional
    public void softDelete(UUID id, String currentUser) {
        MedicalService service = medicalServiceRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy dịch vụ y tế"));

        service.setDeleted(true);
        service.setDeletedAt(LocalDateTime.now());
        service.setDeletedBy(currentUser != null ? currentUser : "Admin");

        medicalServiceRepository.save(service);
    }

    @Transactional
    public ServiceResponse toggleStatus(UUID id, String currentUser) {
        MedicalService service = medicalServiceRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy dịch vụ y tế"));

        service.setActive(!service.isActive());
        service.setUpdatedBy(currentUser != null ? currentUser : "Admin");

        MedicalService saved = medicalServiceRepository.save(service);
        return ServiceResponse.fromEntity(saved);
    }
}
