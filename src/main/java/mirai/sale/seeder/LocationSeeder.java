package mirai.sale.seeder;

import mirai.sale.modules.location.entity.Location;
import mirai.sale.modules.location.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@ConditionalOnProperty(name = "app.seed.admin.enabled", havingValue = "true", matchIfMissing = true)
public class LocationSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LocationSeeder.class);
    private final LocationRepository locationRepository;

    public LocationSeeder(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedLocation("CS_01", "Phòng khám Đa khoa Mirai - Cơ sở Hoàn Kiếm", "Số 15 Phố Tràng Tiền, Phường Tràng Tiền, Quận Hoàn Kiếm, Hà Nội", "admin@phongkham.local");
        seedLocation("CS_02", "Phòng khám Đa khoa Mirai - Cơ sở Cầu Giấy", "Số 68 Đường Cầu Giấy, Phường Quan Hoa, Quận Cầu Giấy, Hà Nội", "admin@phongkham.local");
        seedLocation("CS_03", "Phòng khám Đa khoa Mirai - Cơ sở Đống Đa", "Số 102 Đường Thái Hà, Phường Trung Liệt, Quận Đống Đa, Hà Nội", "admin@phongkham.local");
        seedLocation("CS_04", "Phòng khám Đa khoa Mirai - Cơ sở Hà Đông", "Số 45 Đường Quang Trung, Phường Quang Trung, Quận Hà Đông, Hà Nội", "admin@phongkham.local");
        seedLocation("CS_HCM_01", "Phòng khám Đa khoa Mirai - Chi nhánh Quận 1 (TP.HCM)", "Số 20 Đường Lê Duẩn, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh", "admin@phongkham.local");
    }

    private void seedLocation(String code, String name, String address, String author) {
        if (!locationRepository.existsByCodeAndIsDeletedFalse(code)) {
            Location loc = new Location();
            loc.setCode(code);
            loc.setName(name);
            loc.setAddress(address);
            loc.setActive(true);
            loc.setCreatedBy(author);
            loc.setUpdatedBy(author);
            loc.setDeleted(false);
            locationRepository.save(loc);
            log.info("Đã seed địa điểm khám: {} - {}", code, name);
        }
    }
}
