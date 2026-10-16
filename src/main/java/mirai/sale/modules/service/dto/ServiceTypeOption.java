package mirai.sale.modules.service.dto;

import mirai.sale.modules.service.entity.ServiceType;

public record ServiceTypeOption(
    String code,
    String label
) {
    public static ServiceTypeOption fromEnum(ServiceType type) {
        return new ServiceTypeOption(type.name(), type.getLabel());
    }
}
