package mirai.sale.modules.service.entity;

public enum ServiceType {
    XET_NGHIEM("Xét nghiệm"),
    CHAN_DOAN_HINH_ANH("Chẩn đoán hình ảnh"),
    KHAM_BENH("Khám chuyên khoa"),
    THU_THUAT("Thủ thuật"),
    KHAC("Khác");

    private final String label;

    ServiceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
