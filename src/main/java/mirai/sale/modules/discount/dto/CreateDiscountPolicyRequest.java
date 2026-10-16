package mirai.sale.modules.discount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import mirai.sale.modules.discount.constant.DiscountType;
import mirai.sale.modules.discount.constant.TierScope;

public record CreateDiscountPolicyRequest(
        String code,
        @NotBlank(message = "Tên chính sách giảm giá không được để trống")
        String name,
        @NotNull(message = "Hình thức chiết khấu không được để trống")
        DiscountType discountType,
        TierScope tierScope,
        BigDecimal maxDiscountPerService,
        @NotNull(message = "Từ ngày không được để trống")
        LocalDate startDate,
        LocalDate endDate,
        Boolean active,
        List<UUID> contractIds,
        List<DiscountTierDto> tiers
) {}
