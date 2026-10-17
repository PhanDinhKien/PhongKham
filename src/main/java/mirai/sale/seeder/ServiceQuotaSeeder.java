package mirai.sale.seeder;

import java.time.LocalDate;
import java.util.Optional;
import mirai.sale.modules.location.entity.Location;
import mirai.sale.modules.location.repository.LocationRepository;
import mirai.sale.modules.quota.entity.ServiceQuota;
import mirai.sale.modules.quota.repository.ServiceQuotaRepository;
import mirai.sale.modules.service.entity.MedicalService;
import mirai.sale.modules.service.repository.MedicalServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(7)
@ConditionalOnProperty(name = "app.seed.admin.enabled", havingValue = "true", matchIfMissing = true)
public class ServiceQuotaSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ServiceQuotaSeeder.class);
    private final ServiceQuotaRepository quotaRepository;
    private final MedicalServiceRepository serviceRepository;
    private final LocationRepository locationRepository;
    private final mirai.sale.modules.contract.repository.ContractLocationRepository contractLocationRepository;

    public ServiceQuotaSeeder(
        ServiceQuotaRepository quotaRepository,
        MedicalServiceRepository serviceRepository,
        LocationRepository locationRepository,
        mirai.sale.modules.contract.repository.ContractLocationRepository contractLocationRepository
    ) {
        this.quotaRepository = quotaRepository;
        this.serviceRepository = serviceRepository;
        this.locationRepository = locationRepository;
        this.contractLocationRepository = contractLocationRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Optional<MedicalService> ns1Opt = serviceRepository.findByCodeAndIsDeletedFalse("DV-NS-0110");
        Optional<MedicalService> ns2Opt = serviceRepository.findByCodeAndIsDeletedFalse("DV-NS-0111");
        Optional<MedicalService> saOpt = serviceRepository.findByCodeAndIsDeletedFalse("DV-SA-0231");

        Optional<Location> loc1Opt = locationRepository.findByCodeAndIsDeletedFalse("2026-01");
        Optional<Location> loc2Opt = locationRepository.findByCodeAndIsDeletedFalse("2025-05");
        Optional<Location> loc3Opt = locationRepository.findByCodeAndIsDeletedFalse("2025-01");

        // Seed quotas for DV-NS-0110 matching Image 1
        if (ns1Opt.isPresent() && loc1Opt.isPresent()) {
            seedQuota(ns1Opt.get(), loc1Opt.get(), 200, 120, 80, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 11, 1), true);
        }
        if (ns1Opt.isPresent() && loc2Opt.isPresent()) {
            seedQuota(ns1Opt.get(), loc2Opt.get(), 150, 90, 60, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 31), true);
        }
        if (ns1Opt.isPresent() && loc3Opt.isPresent()) {
            seedQuota(ns1Opt.get(), loc3Opt.get(), 100, 60, 40, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 12, 31), false);
        }

        // Seed quotas for DV-NS-0111
        if (ns2Opt.isPresent() && loc1Opt.isPresent()) {
            seedQuota(ns2Opt.get(), loc1Opt.get(), 180, 100, 80, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true);
        }
        if (ns2Opt.isPresent() && loc2Opt.isPresent()) {
            seedQuota(ns2Opt.get(), loc2Opt.get(), 120, 70, 50, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true);
        }
        if (ns2Opt.isPresent() && loc3Opt.isPresent()) {
            seedQuota(ns2Opt.get(), loc3Opt.get(), 80, 50, 30, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true);
        }

        // Seed quota for DV-SA-0231
        if (saOpt.isPresent() && loc1Opt.isPresent()) {
            seedQuota(saOpt.get(), loc1Opt.get(), 300, 180, 120, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true);
        }
    }

    private void seedQuota(
        MedicalService service,
        Location location,
        int maxQty,
        int s1,
        int s2,
        LocalDate from,
        LocalDate to,
        boolean active
    ) {
        if (!quotaRepository.existsByServiceIdAndLocationId(service.getId(), location.getId())) {
            ServiceQuota sq = new ServiceQuota(
                service,
                location,
                "THEO_CA",
                maxQty,
                s1,
                s2,
                from,
                to,
                active,
                "admin@phongkham.local"
            );

            var cls = contractLocationRepository.findByLocationIdWithContract(location.getId());
            if (!cls.isEmpty()) {
                sq.setContract(cls.get(0).getContract());
            }

            sq.setShiftDetails(String.format(
                "[{\"shiftNumber\":1,\"shiftName\":\"Ca 1\",\"startTime\":\"06:00\",\"endTime\":\"12:00\",\"quantity\":%d},{\"shiftNumber\":2,\"shiftName\":\"Ca 2\",\"startTime\":\"13:00\",\"endTime\":\"19:00\",\"quantity\":%d}]",
                s1, s2
            ));

            quotaRepository.save(sq);
            log.info("Đã seed định mức dịch vụ: {} tại {}", service.getName(), location.getName());
        }
    }
}
