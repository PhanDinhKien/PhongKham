package mirai.sale.modules.patient.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.common.exception.BadRequestException;
import mirai.sale.common.exception.NotFoundException;
import mirai.sale.modules.patient.dto.CreatePatientRequest;
import mirai.sale.modules.patient.dto.PatientResponse;
import mirai.sale.modules.patient.dto.UpdatePatientRequest;
import mirai.sale.modules.patient.entity.Gender;
import mirai.sale.modules.patient.entity.Patient;
import mirai.sale.modules.patient.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PatientResponse> search(String keyword, Gender gender, int page, int size) {
        Page<Patient> pageResult = patientRepository.searchPatients(
            keyword != null && !keyword.isBlank() ? keyword.trim() : null,
            gender,
            PageRequest.of(page, size)
        );

        List<PatientResponse> items = pageResult.getContent().stream()
            .map(PatientResponse::fromEntity)
            .toList();

        return new PageResponse<>(items, page, size, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PatientResponse getById(UUID id) {
        Patient patient = patientRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin người bệnh"));
        return PatientResponse.fromEntity(patient);
    }

    @Transactional
    public PatientResponse create(CreatePatientRequest req, String currentUser) {
        String cleanCode = req.patientCode().trim().toUpperCase();

        // 1. Kiểm tra trùng mã người bệnh
        if (patientRepository.existsByPatientCodeAndIsDeletedFalse(cleanCode)) {
            throw new BadRequestException("Mã người bệnh '" + cleanCode + "' đã tồn tại trong hệ thống. Vui lòng nhập mã khác!");
        }

        Patient patient = new Patient();
        patient.setPatientCode(cleanCode);
        patient.setFullName(req.fullName().trim());
        patient.setBirthYear(req.birthYear());
        patient.setGender(req.gender());
        patient.setPhone(req.phone() != null && !req.phone().isBlank() ? req.phone().trim() : null);
        patient.setAddress(req.address() != null && !req.address().isBlank() ? req.address().trim() : null);
        patient.setNote(req.note() != null && !req.note().isBlank() ? req.note().trim() : null);
        patient.setCreatedBy(currentUser != null ? currentUser : "Admin");
        patient.setUpdatedBy(currentUser != null ? currentUser : "Admin");
        patient.setDeleted(false);

        Patient saved = patientRepository.save(patient);
        return PatientResponse.fromEntity(saved);
    }

    @Transactional
    public PatientResponse update(UUID id, UpdatePatientRequest req, String currentUser) {
        Patient patient = patientRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin người bệnh"));

        String cleanCode = req.patientCode().trim().toUpperCase();

        // 1. Kiểm tra trùng mã người bệnh khi cập nhật
        if (patientRepository.existsByPatientCodeAndIdNotAndIsDeletedFalse(cleanCode, id)) {
            throw new BadRequestException("Mã người bệnh '" + cleanCode + "' đã được sử dụng bởi người bệnh khác. Vui lòng kiểm tra lại!");
        }

        patient.setPatientCode(cleanCode);
        patient.setFullName(req.fullName().trim());
        patient.setBirthYear(req.birthYear());
        patient.setGender(req.gender());
        patient.setPhone(req.phone() != null && !req.phone().isBlank() ? req.phone().trim() : null);
        patient.setAddress(req.address() != null && !req.address().isBlank() ? req.address().trim() : null);
        patient.setNote(req.note() != null && !req.note().isBlank() ? req.note().trim() : null);
        patient.setUpdatedBy(currentUser != null ? currentUser : "Admin");

        Patient saved = patientRepository.save(patient);
        return PatientResponse.fromEntity(saved);
    }

    @Transactional
    public void softDelete(UUID id, String currentUser) {
        Patient patient = patientRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin người bệnh"));

        patient.setDeleted(true);
        patient.setDeletedAt(LocalDateTime.now());
        patient.setDeletedBy(currentUser != null ? currentUser : "Admin");

        patientRepository.save(patient);
    }

    @Transactional(readOnly = true)
    public String generateNextPatientCode() {
        long count = patientRepository.countAllPatients();
        String nextCode = String.format("BN%05d", count + 1);
        while (patientRepository.existsByPatientCodeAndIsDeletedFalse(nextCode)) {
            count++;
            nextCode = String.format("BN%05d", count + 1);
        }
        return nextCode;
    }
}
