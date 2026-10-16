package mirai.sale.modules.service.dto;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import mirai.sale.modules.service.entity.MedicalService;

public record ServiceResponse(
    UUID id,
    String code,
    String name,
    BigDecimal price,
    String formattedPrice,
    String serviceType,
    String serviceTypeLabel,
    boolean active,
    String description,
    String createdBy,
    String updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    private static final DecimalFormat CURRENCY_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        CURRENCY_FORMAT = new DecimalFormat("#,###", symbols);
    }

    public static ServiceResponse fromEntity(MedicalService s) {
        String formatted = "0 đ";
        if (s.getPrice() != null) {
            formatted = CURRENCY_FORMAT.format(s.getPrice()) + " đ";
        }

        String typeCode = s.getServiceType() != null ? s.getServiceType().name() : "";
        String typeLabel = s.getServiceType() != null ? s.getServiceType().getLabel() : "";

        return new ServiceResponse(
            s.getId(),
            s.getCode(),
            s.getName(),
            s.getPrice(),
            formatted,
            typeCode,
            typeLabel,
            s.isActive(),
            s.getDescription(),
            s.getCreatedBy() != null ? s.getCreatedBy() : "-",
            s.getUpdatedBy() != null ? s.getUpdatedBy() : "-",
            s.getCreatedAt(),
            s.getUpdatedAt()
        );
    }
}
