package mirai.sale.modules.discount.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import mirai.sale.common.exception.NotFoundException;
import mirai.sale.modules.contract.entity.Contract;
import mirai.sale.modules.contract.repository.ContractRepository;
import mirai.sale.modules.discount.constant.DiscountType;
import mirai.sale.modules.discount.constant.TierScope;
import mirai.sale.modules.discount.dto.*;
import mirai.sale.modules.discount.entity.DiscountPolicy;
import mirai.sale.modules.discount.entity.DiscountPolicyContract;
import mirai.sale.modules.discount.entity.DiscountPolicyTier;
import mirai.sale.modules.discount.repository.DiscountPolicyContractRepository;
import mirai.sale.modules.discount.repository.DiscountPolicyRepository;
import mirai.sale.modules.discount.repository.DiscountPolicyTierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DiscountPolicyService {

    private final DiscountPolicyRepository discountPolicyRepository;
    private final DiscountPolicyContractRepository contractLinkRepository;
    private final DiscountPolicyTierRepository tierRepository;
    private final ContractRepository contractRepository;

    public DiscountPolicyService(
            DiscountPolicyRepository discountPolicyRepository,
            DiscountPolicyContractRepository contractLinkRepository,
            DiscountPolicyTierRepository tierRepository,
            ContractRepository contractRepository
    ) {
        this.discountPolicyRepository = discountPolicyRepository;
        this.contractLinkRepository = contractLinkRepository;
        this.tierRepository = tierRepository;
        this.contractRepository = contractRepository;
    }

    @Transactional(readOnly = true)
    public Page<DiscountPolicyDto> search(
            String keyword,
            String companyOrContract,
            DiscountType discountType,
            Boolean active,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String comp = (companyOrContract != null && !companyOrContract.isBlank()) ? companyOrContract.trim() : null;

        return discountPolicyRepository.searchPolicies(kw, comp, discountType, active, fromDate, toDate, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public DiscountPolicyDto getById(UUID id) {
        DiscountPolicy policy = discountPolicyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy chính sách giảm giá"));
        return toDto(policy);
    }

    public String generateNextCode() {
        int year = LocalDate.now().getYear();
        long count = discountPolicyRepository.count();
        long nextIndex = count + 1;
        String code = String.format("CK-%d-%03d", year, nextIndex);
        while (discountPolicyRepository.existsByCodeAndIsDeletedFalse(code)) {
            nextIndex++;
            code = String.format("CK-%d-%03d", year, nextIndex);
        }
        return code;
    }

    public DiscountPolicyDto create(CreateDiscountPolicyRequest request, String currentUser) {
        String code = request.code();
        if (code == null || code.isBlank()) {
            code = generateNextCode();
        } else {
            code = code.trim();
            if (discountPolicyRepository.existsByCodeAndIsDeletedFalse(code)) {
                throw new IllegalArgumentException("Mã chính sách giảm giá '" + code + "' đã tồn tại");
            }
        }

        DiscountPolicy policy = new DiscountPolicy(
                code,
                request.name().trim(),
                request.discountType() != null ? request.discountType() : DiscountType.PERCENT,
                request.tierScope() != null ? request.tierScope() : TierScope.MAX_TIER_ALL,
                request.startDate(),
                request.endDate(),
                request.active() == null || request.active(),
                currentUser
        );
        policy.setMaxDiscountPerService(request.maxDiscountPerService());

        DiscountPolicy savedPolicy = discountPolicyRepository.save(policy);

        // Liên kết 1 hoặc nhiều hợp đồng
        if (request.contractIds() != null && !request.contractIds().isEmpty()) {
            for (UUID contractId : request.contractIds()) {
                Contract contract = contractRepository.findById(contractId)
                        .orElseThrow(() -> new NotFoundException("Hợp đồng không tồn tại: " + contractId));
                DiscountPolicyContract link = new DiscountPolicyContract(savedPolicy, contract, currentUser);
                savedPolicy.getContracts().add(link);
            }
        }

        // Bậc chiết khấu
        saveTiers(savedPolicy, request.tiers(), currentUser);

        savedPolicy = discountPolicyRepository.save(savedPolicy);
        return toDto(savedPolicy);
    }

    public DiscountPolicyDto update(UUID id, UpdateDiscountPolicyRequest request, String currentUser) {
        DiscountPolicy policy = discountPolicyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy chính sách giảm giá"));

        policy.setName(request.name().trim());
        policy.setDiscountType(request.discountType() != null ? request.discountType() : DiscountType.PERCENT);
        policy.setTierScope(request.tierScope() != null ? request.tierScope() : TierScope.MAX_TIER_ALL);
        policy.setMaxDiscountPerService(request.maxDiscountPerService());
        policy.setStartDate(request.startDate());
        policy.setEndDate(request.endDate());
        if (request.active() != null) {
            policy.setActive(request.active());
        }
        policy.setUpdatedBy(currentUser);

        // Cập nhật hợp đồng liên kết (1 hoặc nhiều)
        Set<UUID> targetContractIds = (request.contractIds() != null)
                ? new HashSet<>(request.contractIds())
                : Collections.emptySet();

        // Xóa những hợp đồng không còn được chọn
        policy.getContracts().removeIf(c -> !targetContractIds.contains(c.getContract().getId()));

        // Danh sách các hợp đồng hiện đã có liên kết
        Set<UUID> existingContractIds = policy.getContracts().stream()
                .map(c -> c.getContract().getId())
                .collect(Collectors.toSet());

        // Thêm các hợp đồng mới được chọn
        for (UUID contractId : targetContractIds) {
            if (!existingContractIds.contains(contractId)) {
                Contract contract = contractRepository.findById(contractId)
                        .orElseThrow(() -> new NotFoundException("Hợp đồng không tồn tại: " + contractId));
                policy.getContracts().add(new DiscountPolicyContract(policy, contract, currentUser));
            }
        }

        // Cập nhật bậc chiết khấu
        policy.getTiers().clear();
        discountPolicyRepository.saveAndFlush(policy);
        saveTiers(policy, request.tiers(), currentUser);

        DiscountPolicy updated = discountPolicyRepository.saveAndFlush(policy);
        return toDto(updated);
    }

    public boolean toggleActive(UUID id, String currentUser) {
        DiscountPolicy policy = discountPolicyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy chính sách giảm giá"));
        policy.setActive(!policy.isActive());
        policy.setUpdatedBy(currentUser);
        discountPolicyRepository.save(policy);
        return policy.isActive();
    }

    public void delete(UUID id, String currentUser) {
        DiscountPolicy policy = discountPolicyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy chính sách giảm giá"));
        policy.setDeleted(true);
        policy.setDeletedAt(LocalDateTime.now());
        policy.setDeletedBy(currentUser);
        discountPolicyRepository.save(policy);
    }

    private void saveTiers(DiscountPolicy policy, List<DiscountTierDto> tierDtos, String currentUser) {
        if (tierDtos == null || tierDtos.isEmpty()) {
            // Mặc định tạo bậc 1: từ 1 trở lên với mức 0
            policy.getTiers().add(new DiscountPolicyTier(policy, 1, 1, null, BigDecimal.ZERO, currentUser));
            return;
        }

        int tierIdx = 1;
        for (DiscountTierDto dto : tierDtos) {
            BigDecimal val = dto.discountValue() != null ? dto.discountValue() : BigDecimal.ZERO;
            int from = dto.fromQuantity() > 0 ? dto.fromQuantity() : 1;
            Integer to = (dto.toQuantity() != null && dto.toQuantity() > 0) ? dto.toQuantity() : null;
            policy.getTiers().add(new DiscountPolicyTier(policy, tierIdx++, from, to, val, currentUser));
        }
    }

    private DiscountPolicyDto toDto(DiscountPolicy policy) {
        List<DiscountContractDto> contractDtos = policy.getContracts().stream()
                .map(c -> new DiscountContractDto(
                        c.getContract().getId(),
                        c.getContract().getContractCode(),
                        c.getContract().getContractName(),
                        c.getContract().getCompanyCode(),
                        c.getContract().getCompanyName()
                ))
                .collect(Collectors.toList());

        String contractNames = contractDtos.stream()
                .map(c -> c.companyName() + " (" + c.contractCode() + ")")
                .collect(Collectors.joining(", "));

        List<DiscountTierDto> tierDtos = policy.getTiers().stream()
                .sorted(Comparator.comparingInt(DiscountPolicyTier::getTierNumber))
                .map(t -> new DiscountTierDto(
                        t.getId(),
                        t.getTierNumber(),
                        t.getFromQuantity(),
                        t.getToQuantity(),
                        t.getDiscountValue()
                ))
                .collect(Collectors.toList());

        String tierSummary = formatTierSummary(policy.getDiscountType(), tierDtos);

        return new DiscountPolicyDto(
                policy.getId(),
                policy.getCode(),
                policy.getName(),
                "Hợp đồng",
                policy.getDiscountType(),
                policy.getDiscountType() != null ? policy.getDiscountType().getLabel() : "Phần trăm",
                policy.getTierScope(),
                policy.getTierScope() != null ? policy.getTierScope().getLabel() : "Áp dụng bậc lớn nhất cho toàn bộ dịch vụ",
                policy.getMaxDiscountPerService(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.isActive(),
                tierSummary,
                contractDtos,
                contractNames,
                tierDtos,
                policy.getCreatedBy(),
                policy.getCreatedAt(),
                policy.getUpdatedAt()
        );
    }

    private String formatTierSummary(DiscountType discountType, List<DiscountTierDto> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            return "0";
        }
        DecimalFormat currencyFormat = new DecimalFormat("#,###");
        if (discountType == DiscountType.AMOUNT) {
            if (tiers.size() == 1) {
                return currencyFormat.format(tiers.get(0).discountValue()) + "đ";
            }
            BigDecimal min = tiers.stream().map(DiscountTierDto::discountValue).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal max = tiers.stream().map(DiscountTierDto::discountValue).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            if (min.compareTo(max) == 0) {
                return currencyFormat.format(min) + "đ";
            }
            return currencyFormat.format(min) + "đ - " + currencyFormat.format(max) + "đ";
        } else {
            // PERCENT
            if (tiers.size() == 1) {
                return stripTrailingZeros(tiers.get(0).discountValue()) + "%";
            }
            BigDecimal min = tiers.stream().map(DiscountTierDto::discountValue).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal max = tiers.stream().map(DiscountTierDto::discountValue).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            if (min.compareTo(max) == 0) {
                return stripTrailingZeros(min) + "%";
            }
            return stripTrailingZeros(min) + "% - " + stripTrailingZeros(max) + "%";
        }
    }

    private String stripTrailingZeros(BigDecimal val) {
        if (val == null) return "0";
        return val.stripTrailingZeros().toPlainString();
    }
}
