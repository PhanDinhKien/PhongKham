package mirai.sale.modules.location.dto;

import mirai.sale.modules.location.entity.LocationShift;

public record ShiftDto(
    Integer shiftNumber,
    String shiftName,
    String startTime,
    String endTime
) {
    public static ShiftDto fromEntity(LocationShift shift) {
        if (shift == null) return null;
        return new ShiftDto(
            shift.getShiftNumber(),
            shift.getShiftName() != null ? shift.getShiftName() : ("Ca " + shift.getShiftNumber()),
            shift.getStartTime(),
            shift.getEndTime()
        );
    }
}
