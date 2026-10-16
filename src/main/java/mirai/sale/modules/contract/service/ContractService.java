package mirai.sale.modules.contract.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.common.exception.BadRequestException;
import mirai.sale.common.exception.NotFoundException;
import mirai.sale.modules.contract.dto.ContractResponse;
import mirai.sale.modules.contract.dto.CreateContractRequest;
import mirai.sale.modules.contract.dto.UpdateContractRequest;
import mirai.sale.modules.contract.entity.Contract;
import mirai.sale.modules.contract.entity.ContractStatus;
import mirai.sale.modules.contract.repository.ContractRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractService {

    private final ContractRepository contractRepository;

    public ContractService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ContractResponse> search(String keyword, ContractStatus status, int page, int size) {
        Page<Contract> pageResult = contractRepository.searchContracts(
            keyword != null && !keyword.isBlank() ? keyword.trim() : null,
            status,
            PageRequest.of(page, size)
        );

        List<ContractResponse> items = pageResult.getContent().stream()
            .map(ContractResponse::fromEntity)
            .toList();

        return new PageResponse<>(items, page, size, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ContractResponse getById(UUID id) {
        Contract contract = contractRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin hợp đồng"));
        return ContractResponse.fromEntity(contract);
    }

    @Transactional
    public ContractResponse create(CreateContractRequest req, String currentUser) {
        String cleanCode = req.contractCode().trim().toUpperCase();

        // 1. Kiểm tra trùng mã hợp đồng
        if (contractRepository.existsByContractCodeAndIsDeletedFalse(cleanCode)) {
            throw new BadRequestException("Mã hợp đồng '" + cleanCode + "' đã tồn tại trong hệ thống. Vui lòng nhập mã khác!");
        }

        Contract contract = new Contract();
        contract.setContractCode(cleanCode);
        contract.setContractName(req.contractName().trim());
        contract.setCompanyCode(req.companyCode().trim().toUpperCase());
        contract.setCompanyName(req.companyName().trim());
        contract.setStatus(req.status() != null ? req.status() : ContractStatus.HIEU_LUC);
        contract.setCreatedBy(currentUser);
        contract.setUpdatedBy(currentUser);
        contract.setDeleted(false);

        Contract saved = contractRepository.save(contract);
        return ContractResponse.fromEntity(saved);
    }

    @Transactional
    public ContractResponse update(UUID id, UpdateContractRequest req, String currentUser) {
        Contract contract = contractRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy hợp đồng để cập nhật"));

        String cleanCode = req.contractCode().trim().toUpperCase();

        // 1. Kiểm tra trùng mã hợp đồng với bản ghi khác
        if (contractRepository.existsByContractCodeAndIdNotAndIsDeletedFalse(cleanCode, id)) {
            throw new BadRequestException("Mã hợp đồng '" + cleanCode + "' đã tồn tại ở hợp đồng khác!");
        }

        contract.setContractCode(cleanCode);
        contract.setContractName(req.contractName().trim());
        contract.setCompanyCode(req.companyCode().trim().toUpperCase());
        contract.setCompanyName(req.companyName().trim());
        if (req.status() != null) {
            contract.setStatus(req.status());
        }
        contract.setUpdatedBy(currentUser);

        Contract updated = contractRepository.save(contract);
        return ContractResponse.fromEntity(updated);
    }

    @Transactional
    public void delete(UUID id, String currentUser) {
        Contract contract = contractRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy hợp đồng để xóa"));

        contract.setDeleted(true);
        contract.setDeletedBy(currentUser);
        contract.setDeletedAt(LocalDateTime.now());
        contractRepository.save(contract);
    }

    /**
     * Tự động sinh mã hợp đồng tiếp theo dạng HD00001, HD00002...
     */
    @Transactional(readOnly = true)
    public String generateNextContractCode() {
        long count = contractRepository.countAllContracts() + 1;
        String candidate;
        do {
            candidate = String.format("HD%05d", count);
            count++;
        } while (contractRepository.existsByContractCodeAndIsDeletedFalse(candidate));
        return candidate;
    }
}
