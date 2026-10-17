package mirai.sale.seeder;

import java.util.List;
import mirai.sale.modules.contract.entity.Contract;
import mirai.sale.modules.contract.entity.ContractLocation;
import mirai.sale.modules.contract.repository.ContractLocationRepository;
import mirai.sale.modules.contract.repository.ContractRepository;
import mirai.sale.modules.location.entity.Location;
import mirai.sale.modules.location.entity.LocationShift;
import mirai.sale.modules.location.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(6)
@ConditionalOnProperty(name = "app.seed.admin.enabled", havingValue = "true", matchIfMissing = true)
public class LocationSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LocationSeeder.class);
    private final LocationRepository locationRepository;
    private final ContractRepository contractRepository;
    private final ContractLocationRepository contractLocationRepository;

    public LocationSeeder(
        LocationRepository locationRepository,
        ContractRepository contractRepository,
        ContractLocationRepository contractLocationRepository
    ) {
        this.locationRepository = locationRepository;
        this.contractRepository = contractRepository;
        this.contractLocationRepository = contractLocationRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Location loc1 = seedLocation("2026-01", "2026–Điểm khám 1", "Số 15 Phố Tràng Tiền, Quận Hoàn Kiếm, Hà Nội", "admin@phongkham.local");
        seedLocation("2025-05", "2025–Điểm khám 5", "Số 68 Đường Cầu Giấy, Quận Cầu Giấy, Hà Nội", "admin@phongkham.local");
        seedLocation("2025-04", "2025–Điểm khám 4", "Số 102 Đường Thái Hà, Quận Đống Đa, Hà Nội", "admin@phongkham.local");
        seedLocation("2025-03", "2025–Điểm khám 3", "Số 45 Đường Quang Trung, Quận Hà Đông, Hà Nội", "admin@phongkham.local");
        seedLocation("2025-02", "2025–Điểm khám 2", "Số 20 Đường Lê Duẩn, Quận 1, TP. Hồ Chí Minh", "admin@phongkham.local");
        seedLocation("2025-01", "2025–Điểm khám 1", "Số 88 Đường Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh", "admin@phongkham.local");

        // Seed contracts attached to 2026-01 if available
        if (loc1 != null) {
            List<Contract> contracts = contractRepository.findAll();
            for (int i = 0; i < Math.min(3, contracts.size()); i++) {
                Contract c = contracts.get(i);
                if (!contractLocationRepository.existsByContractIdAndLocationId(c.getId(), loc1.getId())) {
                    ContractLocation cl = new ContractLocation(
                        c,
                        loc1,
                        true,
                        "Ca 1 (06:00-12:00), Ca 2 (13:00-18:00)",
                        "admin@phongkham.local"
                    );
                    contractLocationRepository.save(cl);
                }
            }
        }
    }

    private Location seedLocation(String code, String name, String address, String author) {
        if (!locationRepository.existsByCodeAndIsDeletedFalse(code)) {
            Location loc = new Location();
            loc.setCode(code);
            loc.setName(name);
            loc.setAddress(address);
            loc.setActive(true);
            loc.setCreatedBy(author);
            loc.setUpdatedBy(author);
            loc.setDeleted(false);

            // Add standard shifts matching Image 1 & Image 2
            LocationShift s1 = new LocationShift(loc, 1, "Ca 1", "06:00", "12:00");
            LocationShift s2 = new LocationShift(loc, 2, "Ca 2", "13:00", "18:00");
            loc.getShifts().add(s1);
            loc.getShifts().add(s2);

            Location saved = locationRepository.save(loc);
            log.info("Đã seed địa điểm khám: {} - {}", code, name);
            return saved;
        } else {
            return locationRepository.findByCodeAndIsDeletedFalse(code).orElse(null);
        }
    }
}
