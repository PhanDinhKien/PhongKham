package mirai.sale.modules.contract.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import mirai.sale.modules.service.entity.MedicalService;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "package_services", uniqueConstraints = {
    @UniqueConstraint(name = "UC_PACKAGE_SERVICES", columnNames = {"package_id", "service_id"})
})
public class PackageService {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private ExamPackage examPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private MedicalService medicalService;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public PackageService() {
    }

    public PackageService(ExamPackage examPackage, MedicalService medicalService, String createdBy) {
        this.examPackage = examPackage;
        this.medicalService = medicalService;
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ExamPackage getExamPackage() {
        return examPackage;
    }

    public void setExamPackage(ExamPackage examPackage) {
        this.examPackage = examPackage;
    }

    public MedicalService getMedicalService() {
        return medicalService;
    }

    public void setMedicalService(MedicalService medicalService) {
        this.medicalService = medicalService;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
