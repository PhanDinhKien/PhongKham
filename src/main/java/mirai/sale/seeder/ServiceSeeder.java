package mirai.sale.seeder;

import java.math.BigDecimal;
import mirai.sale.modules.service.entity.MedicalService;
import mirai.sale.modules.service.entity.ServiceType;
import mirai.sale.modules.service.repository.MedicalServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@ConditionalOnProperty(name = "app.seed.admin.enabled", havingValue = "true", matchIfMissing = true)
public class ServiceSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ServiceSeeder.class);
    private final MedicalServiceRepository serviceRepository;

    public ServiceSeeder(MedicalServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Seed các dịch vụ y tế mẫu
        seedService("XN_001", "Tổng phân tích tế bào máu ngoại vi (24 thông số)", new BigDecimal("120000"), ServiceType.XET_NGHIEM, "Xét nghiệm công thức máu bằng máy tự động laser", "admin@phongkham.local");
        seedService("XN_002", "Định lượng Glucose máu (Đường huyết)", new BigDecimal("45000"), ServiceType.XET_NGHIEM, "Đánh giá nồng độ đường huyết hỗ trợ chẩn đoán tiểu đường", "admin@phongkham.local");
        seedService("XN_003", "Định lượng Cholesterol toàn phần", new BigDecimal("55000"), ServiceType.XET_NGHIEM, "Xét nghiệm mỡ máu cơ bản đánh giá tim mạch", "admin@phongkham.local");
        seedService("XN_004", "Định lượng Ure & Creatinin máu", new BigDecimal("90000"), ServiceType.XET_NGHIEM, "Đánh giá chức năng lọc cầu thận", "admin@phongkham.local");
        seedService("XN_005", "Đo hoạt độ AST (GOT) & ALT (GPT)", new BigDecimal("90000"), ServiceType.XET_NGHIEM, "Đánh giá men gan và tế bào gan", "admin@phongkham.local");

        seedService("CDHA_001", "Chụp X-quang tim phổi thẳng", new BigDecimal("150000"), ServiceType.CHAN_DOAN_HINH_ANH, "Chụp kỹ thuật số DR đánh giá nhu mô phổi và kích thước bóng tim", "admin@phongkham.local");
        seedService("CDHA_002", "Siêu âm ổ bụng tổng quát màu 4D", new BigDecimal("200000"), ServiceType.CHAN_DOAN_HINH_ANH, "Khảo sát gan, mật, tụy, lách, thận, bàng quang và tử cung/tiền liệt tuyến", "admin@phongkham.local");
        seedService("CDHA_003", "Siêu âm Doppler tim màu", new BigDecimal("350000"), ServiceType.CHAN_DOAN_HINH_ANH, "Đánh giá hình thái, cấu trúc van tim và huyết động học dòng máu", "admin@phongkham.local");
        seedService("CDHA_004", "Chụp CLVT lồng ngực không tiêm thuốc cản quang", new BigDecimal("900000"), ServiceType.CHAN_DOAN_HINH_ANH, "Chụp cắt lớp vi tính đa dãy phát hiện tổn thương phổi sớm", "admin@phongkham.local");

        seedService("KB_001", "Khám Nội tổng quát & tư vấn sức khỏe", new BigDecimal("150000"), ServiceType.KHAM_BENH, "Bác sĩ chuyên khoa khám lâm sàng tổng quát toàn thân", "admin@phongkham.local");
        seedService("KB_002", "Khám Chuyên khoa Mắt (Soi đáy mắt, thị lực)", new BigDecimal("100000"), ServiceType.KHAM_BENH, "Đo khúc xạ tự động và khám bán phần trước - sau mắt", "admin@phongkham.local");
        seedService("KB_003", "Khám Chuyên khoa Tai - Mũi - Họng", new BigDecimal("100000"), ServiceType.KHAM_BENH, "Khám chuyên khoa phát hiện các bệnh lý vùng họng, mũi, tai", "admin@phongkham.local");

        seedService("TT_001", "Điện tâm đồ (ECG) 12 chuyển đạo", new BigDecimal("80000"), ServiceType.THU_THUAT, "Ghi điện học tim phát hiện rối loạn nhịp tim và thiếu máu cơ tim", "admin@phongkham.local");
        seedService("TT_002", "Nội soi Tai - Mũi - Họng ống mềm", new BigDecimal("250000"), ServiceType.THU_THUAT, "Quan sát rõ nét niêm mạc vòm họng và dây thanh âm", "admin@phongkham.local");
    }

    private void seedService(String code, String name, BigDecimal price, ServiceType type, String desc, String author) {
        if (!serviceRepository.existsByCodeAndIsDeletedFalse(code)) {
            MedicalService service = new MedicalService();
            service.setCode(code);
            service.setName(name);
            service.setPrice(price);
            service.setServiceType(type);
            service.setActive(true);
            service.setDescription(desc);
            service.setCreatedBy(author);
            service.setUpdatedBy(author);
            service.setDeleted(false);
            serviceRepository.save(service);
            log.info("Đã seed dịch vụ y tế: {} - {}", code, name);
        }
    }
}
