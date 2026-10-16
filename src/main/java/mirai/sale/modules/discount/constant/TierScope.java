package mirai.sale.modules.discount.constant;

public enum TierScope {
    MAX_TIER_ALL("Áp dụng bậc lớn nhất cho toàn bộ dịch vụ"),
    PROGRESSIVE("Tính lũy tiến theo từng bậc");

    private final String label;

    TierScope(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
