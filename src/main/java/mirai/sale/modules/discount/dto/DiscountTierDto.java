package mirai.sale.modules.discount.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DiscountTierDto(
        UUID id,
        int tierNumber,
        int fromQuantity,
        Integer toQuantity,
        BigDecimal discountValue
) {}
