package mirai.sale.seeder;

import mirai.sale.modules.patient.entity.Gender;
import mirai.sale.modules.patient.entity.Patient;
import mirai.sale.modules.patient.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
@ConditionalOnProperty(name = "app.seed.admin.enabled", havingValue = "true", matchIfMissing = true)
public class PatientSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PatientSeeder.class);
    private final PatientRepository patientRepository;

    public PatientSeeder(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedPatient("BN00001", "Nguyễn Văn Nam", 1982, Gender.NAM, "0912345678", "Số 12 Phố Huế, Quận Hai Bà Trưng, Hà Nội", "Tiền sử tăng huyết áp vô căn, đang dùng Amlodipin 5mg", "admin@phongkham.local");
        seedPatient("BN00002", "Trần Thị Mai", 1995, Gender.NU, "0987654321", "Số 45 Ngõ 102 Khuất Duy Tiến, Quận Thanh Xuân, Hà Nội", "Dị ứng kháng sinh nhóm Beta-lactam (Penicillin)", "admin@phongkham.local");
        seedPatient("BN00003", "Lê Hoàng Phúc", 2008, Gender.NAM, "0903112233", "Căn hộ 1204 Tòa A, KĐT Times City, Hà Nội", "Tiền sử hen phế quản nhẹ từ nhỏ", "admin@phongkham.local");
        seedPatient("BN00004", "Phạm Thu Hương", 1968, Gender.NU, "0934556677", "Số 88 Đường Nguyễn Trãi, Quận Đống Đa, Hà Nội", "Đái tháo đường type 2 (10 năm), rối loạn lipid máu", "admin@phongkham.local");
        seedPatient("BN00005", "Đỗ Quốc Bảo", 1990, Gender.NAM, "0977889900", "Số 26 Phố Đinh Tiên Hoàng, Quận Hoàn Kiếm, Hà Nội", "Khám sức khỏe định kỳ doanh nghiệp, không ghi nhận bệnh nền", "admin@phongkham.local");
    }

    private void seedPatient(String code, String name, Integer birthYear, Gender gender, String phone, String address, String note, String author) {
        if (!patientRepository.existsByPatientCodeAndIsDeletedFalse(code)) {
            Patient p = new Patient();
            p.setPatientCode(code);
            p.setFullName(name);
            p.setBirthYear(birthYear);
            p.setGender(gender);
            p.setPhone(phone);
            p.setAddress(address);
            p.setNote(note);
            p.setCreatedBy(author);
            p.setUpdatedBy(author);
            p.setDeleted(false);
            patientRepository.save(p);
            log.info("Đã seed người bệnh: {} - {}", code, name);
        }
    }
}
