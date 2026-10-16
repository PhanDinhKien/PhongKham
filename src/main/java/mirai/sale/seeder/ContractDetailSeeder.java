package mirai.sale.seeder;

import java.time.LocalDate;
import java.util.List;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(6)
@ConditionalOnProperty(name = "app.seed.admin.enabled", havingValue = "true", matchIfMissing = true)
public class ContractDetailSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ContractDetailSeeder.class);

    private final ContractRepository contractRepository;
    private final ContractPatientRepository contractPatientRepository;
    private final ContractLocationRepository contractLocationRepository;
    private final ExamPackageRepository examPackageRepository;
    private final PackageServiceRepository packageServiceRepository;
    private final PatientRepository patientRepository;
    private final LocationRepository locationRepository;
    private final MedicalServiceRepository medicalServiceRepository;

    public ContractDetailSeeder(
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

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        contractRepository.findByContractCodeAndIsDeletedFalse("HD00001").ifPresent(contract -> {
            seedPatients(contract);
            seedLocations(contract);
            seedPackagesAndServices(contract);
        });
    }

    private void seedPatients(Contract contract) {
        if (contractPatientRepository.countByContractId(contract.getId()) == 0) {
            List<Patient> patients = patientRepository.findAll();
            String[] statuses = {"MOI", "CHO_DUYET", "DANG_DUYET", "DA_DUYET"};
            int idx = 1;
            for (Patient p : patients) {
                String recordCode = String.format("HS-2026-%05d", 890 + idx);
                String status = statuses[(idx - 1) % statuses.length];
                ContractPatient cp = new ContractPatient(
                    contract,
                    p,
                    recordCode,
                    "BS. Nguyễn Văn A",
                    LocalDate.now().minusDays(idx),
                    status,
                    "admin@phongkham.local"
                );
                contractPatientRepository.save(cp);
                idx++;
                if (idx > 10) break;
            }
            log.info("Đã seed hồ sơ bệnh nhân cho hợp đồng HD00001");
        }
    }

    private void seedLocations(Contract contract) {
        if (contractLocationRepository.countByContractId(contract.getId()) == 0) {
            List<Location> locations = locationRepository.findAll();
            for (Location loc : locations) {
                ContractLocation cl = new ContractLocation(contract, loc, "admin@phongkham.local");
                contractLocationRepository.save(cl);
                break; // Gán 1-2 địa điểm mẫu
            }
            log.info("Đã seed địa điểm khám cho hợp đồng HD00001");
        }
    }

    private void seedPackagesAndServices(Contract contract) {
        if (examPackageRepository.countByContractIdAndIsDeletedFalse(contract.getId()) == 0) {
            // Gói 1: GK-001
            ExamPackage pkg1 = new ExamPackage(
                contract,
                "GK-001",
                "Gói khám sức khỏe định kỳ tiêu chuẩn 2026",
                "HIEU_LUC",
                "admin@phongkham.local"
            );
            ExamPackage savedPkg1 = examPackageRepository.save(pkg1);

            // Gói 2: GK-002
            ExamPackage pkg2 = new ExamPackage(
                contract,
                "GK-002",
                "Gói khám tầm soát chuyên sâu VIP",
                "HIEU_LUC",
                "admin@phongkham.local"
            );
            ExamPackage savedPkg2 = examPackageRepository.save(pkg2);

            // Gán dịch vụ vào gói
            List<MedicalService> services = medicalServiceRepository.findAll();
            int count = 0;
            for (MedicalService s : services) {
                if (count < 3) {
                    packageServiceRepository.save(new PackageService(savedPkg1, s, "admin@phongkham.local"));
                }
                if (count >= 1 && count < 5) {
                    packageServiceRepository.save(new PackageService(savedPkg2, s, "admin@phongkham.local"));
                }
                count++;
            }
            log.info("Đã seed gói khám và dịch vụ trong gói cho hợp đồng HD00001");
        }
    }
}
