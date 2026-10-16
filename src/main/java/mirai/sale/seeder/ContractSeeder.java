package mirai.sale.seeder;

import mirai.sale.modules.contract.entity.Contract;
import mirai.sale.modules.contract.entity.ContractStatus;
import mirai.sale.modules.contract.repository.ContractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
@ConditionalOnProperty(name = "app.seed.admin.enabled", havingValue = "true", matchIfMissing = true)
public class ContractSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ContractSeeder.class);
    private final ContractRepository contractRepository;

    public ContractSeeder(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedContract("HD00001", "Hợp đồng khám sức khỏe định kỳ năm 2026 - Tập đoàn FPT", "CTY-FPT", "Công ty Cổ phần FPT", ContractStatus.HIEU_LUC, "admin@phongkham.local");
        seedContract("HD00002", "Hợp đồng khám sức khỏe đầu vào cán bộ nhân viên - VNG Corporation", "CTY-VNG", "Công ty Cổ phần VNG", ContractStatus.HIEU_LUC, "admin@phongkham.local");
        seedContract("HD00003", "Hợp đồng kiểm tra sức khỏe nghề nghiệp định kỳ - Viettel Telecom", "CTY-VIETTEL", "Tổng Công ty Viễn thông Viettel", ContractStatus.CHUA_HIEU_LUC, "admin@phongkham.local");
        seedContract("HD00004", "Hợp đồng khám sức khỏe toàn diện cán bộ quản lý năm 2025 - VinGroup", "CTY-VINGROUP", "Tập đoàn Vingroup", ContractStatus.HET_HIEU_LUC, "admin@phongkham.local");
        seedContract("HD00005", "Hợp đồng khám sức khỏe tầm soát nâng cao - Samsung Electronics", "CTY-SAMSUNG", "Công ty TNHH Samsung Electronics Việt Nam", ContractStatus.HIEU_LUC, "admin@phongkham.local");
    }

    private void seedContract(String code, String name, String companyCode, String companyName, ContractStatus status, String author) {
        if (!contractRepository.existsByContractCodeAndIsDeletedFalse(code)) {
            Contract c = new Contract();
            c.setContractCode(code);
            c.setContractName(name);
            c.setCompanyCode(companyCode);
            c.setCompanyName(companyName);
            c.setStatus(status);
            c.setCreatedBy(author);
            c.setUpdatedBy(author);
            c.setDeleted(false);
            contractRepository.save(c);
            log.info("Đã seed hợp đồng: {} - {}", code, name);
        }
    }
}
