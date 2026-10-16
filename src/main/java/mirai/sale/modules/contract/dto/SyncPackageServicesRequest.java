package mirai.sale.modules.contract.dto;

import java.util.List;
import java.util.UUID;

public record SyncPackageServicesRequest(
    List<UUID> serviceIds
) {
}
