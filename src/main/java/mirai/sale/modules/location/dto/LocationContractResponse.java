package mirai.sale.modules.location.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import mirai.sale.modules.contract.entity.Contract;
import mirai.sale.modules.contract.entity.ContractLocation;

public record LocationContractResponse(
    UUID id,
    UUID contractId,
    String contractCode,
    String contractName,
    String companyName,
    String companyCode,
    boolean active,
    String shiftConfig,
    LocalDateTime createdAt
) {
    public static LocationContractResponse fromEntity(ContractLocation cl) {
        Contract c = cl.getContract();
        return new LocationContractResponse(
            cl.getId(),
            c.getId(),
            c.getContractCode(),
            c.getContractName(),
            c.getCompanyName(),
            c.getCompanyCode(),
            cl.isActive(),
            cl.getShiftConfig(),
            cl.getCreatedAt()
        );
    }
}
