package mirai.sale.modules.discount.dto;

import java.util.UUID;

public record DiscountContractDto(
        UUID contractId,
        String contractCode,
        String contractName,
        String companyCode,
        String companyName
) {}
