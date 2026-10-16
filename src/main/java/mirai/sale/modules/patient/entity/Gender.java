package mirai.sale.modules.patient.entity;

public enum Gender {
    NAM("Nam"),
    NU("Nữ"),
    KHAC("Khác");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
