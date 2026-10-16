package mirai.sale.modules.discount.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import mirai.sale.modules.discount.constant.DiscountType;
import mirai.sale.modules.discount.constant.TierScope;

public record DiscountPolicyDto(
        UUID id,
        String code,
        String name,
        String applyType,
        DiscountType discountType,
        String discountTypeLabel,
        TierScope tierScope,
        String tierScopeLabel,
        BigDecimal maxDiscountPerService,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        String tierSummary,
        List<DiscountContractDto> contracts,
        String contractNames,
        List<DiscountTierDto> tiers,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
