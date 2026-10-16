package mirai.sale.modules.contract.entity;

public enum ContractStatus {
    HIEU_LUC("Hiệu lực"),
    CHUA_HIEU_LUC("Chưa hiệu lực"),
    HET_HIEU_LUC("Hết hiệu lực"),
    DA_HUY("Đã hủy");

    private final String label;

    ContractStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
