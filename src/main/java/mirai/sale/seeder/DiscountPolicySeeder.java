package mirai.sale.seeder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import mirai.sale.modules.contract.entity.Contract;
import mirai.sale.modules.contract.repository.ContractRepository;
import mirai.sale.modules.discount.constant.DiscountType;
import mirai.sale.modules.discount.constant.TierScope;
import mirai.sale.modules.discount.dto.CreateDiscountPolicyRequest;
import mirai.sale.modules.discount.dto.DiscountTierDto;
import mirai.sale.modules.discount.repository.DiscountPolicyRepository;
import mirai.sale.modules.discount.service.DiscountPolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(8)
public class DiscountPolicySeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DiscountPolicySeeder.class);

    private final DiscountPolicyRepository discountPolicyRepository;
    private final DiscountPolicyService discountPolicyService;
    private final ContractRepository contractRepository;

    public DiscountPolicySeeder(
            DiscountPolicyRepository discountPolicyRepository,
            DiscountPolicyService discountPolicyService,
            ContractRepository contractRepository
    ) {
        this.discountPolicyRepository = discountPolicyRepository;
        this.discountPolicyService = discountPolicyService;
        this.contractRepository = contractRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (discountPolicyRepository.countActivePolicies() > 0) {
            return;
        }

        List<Contract> contracts = contractRepository.findAll();
        if (contracts.isEmpty()) {
            return;
        }

        Contract c1 = contracts.size() > 0 ? contracts.get(0) : null;
        Contract c2 = contracts.size() > 1 ? contracts.get(1) : null;
        Contract c3 = contracts.size() > 2 ? contracts.get(2) : null;
        Contract c4 = contracts.size() > 3 ? contracts.get(3) : null;

        // 1. Chính sách 1: Công ty Edufit - 2026 (Gán nhiều hợp đồng: c1 & c2)
        if (c1 != null) {
            List<java.util.UUID> contractIds = (c2 != null) ? List.of(c1.getId(), c2.getId()) : List.of(c1.getId());
            discountPolicyService.create(new CreateDiscountPolicyRequest(
                    "CK-2026-001",
                    "Công ty Edufit - 2026",
                    DiscountType.PERCENT,
                    TierScope.MAX_TIER_ALL,
                    new BigDecimal("1000000"),
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31),
                    true,
                    contractIds,
                    List.of(
                            new DiscountTierDto(null, 1, 1, 3, new BigDecimal("10")),
                            new DiscountTierDto(null, 2, 4, null, new BigDecimal("20"))
                    )
            ), "admin@phongkham.local");
        }

        // 2. Chính sách 2: Công ty XYZ - 2026 (Số tiền)
        if (c2 != null) {
            discountPolicyService.create(new CreateDiscountPolicyRequest(
                    "CK-2026-002",
                    "Công ty XYZ - 2026",
                    DiscountType.AMOUNT,
                    TierScope.MAX_TIER_ALL,
                    null,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31),
                    true,
                    List.of(c2.getId()),
                    List.of(
                            new DiscountTierDto(null, 1, 1, 3, new BigDecimal("200000")),
                            new DiscountTierDto(null, 2, 4, null, new BigDecimal("500000"))
                    )
            ), "admin@phongkham.local");
        }

        // 3. Chính sách 3: Giảm giá xét nghiệm 2026 (Phần trăm 7%)
        if (c3 != null) {
            discountPolicyService.create(new CreateDiscountPolicyRequest(
                    "CK-2026-013",
                    "Giảm giá xét nghiệm 2026",
                    DiscountType.PERCENT,
                    TierScope.PROGRESSIVE,
                    null,
                    LocalDate.of(2026, 7, 15),
                    LocalDate.of(2026, 9, 15),
                    true,
                    List.of(c3.getId()),
                    List.of(
                            new DiscountTierDto(null, 1, 1, null, new BigDecimal("7"))
                    )
            ), "admin@phongkham.local");
        }

        // 4. Chính sách 4: Công ty ABC 2026 (Ngừng áp dụng)
        if (c4 != null) {
            discountPolicyService.create(new CreateDiscountPolicyRequest(
                    "CK-2026-004",
                    "Công ty ABC 2026",
                    DiscountType.AMOUNT,
                    TierScope.MAX_TIER_ALL,
                    null,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31),
                    false,
                    List.of(c4.getId()),
                    List.of(
                            new DiscountTierDto(null, 1, 1, null, new BigDecimal("50000"))
                    )
            ), "admin@phongkham.local");
        }

        log.info("Đã seed xong dữ liệu mẫu cho Chính sách giảm giá");
    }
}
