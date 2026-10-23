package mirai.sale.modules.receipt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import mirai.sale.common.exception.NotFoundException;
import mirai.sale.modules.receipt.dto.CreateReceiptRequest;
import mirai.sale.modules.receipt.dto.ReceiptDto;
import mirai.sale.modules.receipt.entity.Receipt;
import mirai.sale.modules.receipt.repository.ReceiptRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    @Transactional(readOnly = true)
    public Page<ReceiptDto> search(
            String keyword,
            String company,
            String cashier,
            String paymentMethod,
            String syncStatus,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    ) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String comp = (company != null && !company.isBlank() && !"ALL".equalsIgnoreCase(company)) ? company.trim() : null;
        String cash = (cashier != null && !cashier.isBlank() && !"ALL".equalsIgnoreCase(cashier)) ? cashier.trim() : null;
        String pm = (paymentMethod != null && !paymentMethod.isBlank() && !"ALL".equalsIgnoreCase(paymentMethod)) ? paymentMethod.trim() : null;
        String sync = (syncStatus != null && !syncStatus.isBlank() && !"ALL".equalsIgnoreCase(syncStatus)) ? syncStatus.trim() : null;

        return receiptRepository.searchReceipts(kw, comp, cash, pm, sync, fromDate, toDate, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ReceiptDto getById(UUID id) {
        Receipt receipt = receiptRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu thu: " + id));
        return toDto(receipt);
    }

    public synchronized String generateReceiptCode() {
        LocalDate today = LocalDate.now();
        String datePrefix = today.format(DateTimeFormatter.ofPattern("yyMMdd"));
        long countToday = receiptRepository.count();
        long seq = countToday + 1;
        String code = String.format("%s%05d", datePrefix, seq);
        while (receiptRepository.existsByReceiptCodeAndIsDeletedFalse(code)) {
            seq++;
            code = String.format("%s%05d", datePrefix, seq);
        }
        return code;
    }

    public ReceiptDto create(CreateReceiptRequest req, String currentUser) {
        Receipt receipt = new Receipt();
        receipt.setReceiptCode(generateReceiptCode());
        receipt.setPatientId(req.patientId());
        receipt.setPatientCode(req.patientCode());
        receipt.setRecordCode(req.recordCode());
        receipt.setPatientName(req.patientName());
        receipt.setGender(req.gender());
        receipt.setBirthYear(req.birthYear());
        receipt.setContractId(req.contractId());
        receipt.setCompanyCode(req.companyCode());
        receipt.setCompanyName(req.companyName());
        receipt.setContractCode(req.contractCode());
        receipt.setContractName(req.contractName());
        receipt.setPackageName(req.packageName());
        receipt.setServiceCount(req.services() != null ? req.services().size() : 0);

        // Phân loại DV Xét nghiệm & DV CĐHA
        List<String> labList = new ArrayList<>();
        List<String> imgList = new ArrayList<>();
        if (req.services() != null) {
            for (var s : req.services()) {
                String type = s.serviceType() != null ? s.serviceType().toUpperCase() : "";
                String nameOrCode = (s.code() != null && !s.code().isBlank()) ? s.code() : s.name();
                if (type.contains("XET_NGHIEM") || type.contains("LAB")) {
                    labList.add(nameOrCode);
                } else if (type.contains("HINH_ANH") || type.contains("CDHA") || type.contains("NOI_SOI") || type.contains("X_QUANG")) {
                    imgList.add(s.name() != null ? s.name() : nameOrCode);
                } else {
                    // Nếu tên dịch vụ có chứa từ khóa
                    String sNameLower = s.name() != null ? s.name().toLowerCase() : "";
                    if (sNameLower.contains("xét nghiệm") || sNameLower.contains("máu") || sNameLower.contains("nước tiểu") || sNameLower.contains("shm") || sNameLower.contains("tsh")) {
                        labList.add(nameOrCode);
                    } else if (sNameLower.contains("nội soi") || sNameLower.contains("x-quang") || sNameLower.contains("clvt") || sNameLower.contains("siêu âm") || sNameLower.contains("mri") || sNameLower.contains("chụp")) {
                        imgList.add(s.name() != null ? s.name() : nameOrCode);
                    }
                }
            }
        }

        receipt.setLabServices(labList.isEmpty() ? null : String.join(", ", labList));
        receipt.setImagingServices(imgList.isEmpty() ? null : String.join(", ", imgList));

        // Lưu JSON toàn bộ dịch vụ đã chọn
        try {
            receipt.setAllServicesJson(objectMapper.writeValueAsString(req.services()));
        } catch (Exception e) {
            receipt.setAllServicesJson(null);
        }

        receipt.setSubtotal(req.subtotal());
        receipt.setDiscountCode(req.discountCode());
        receipt.setDiscountPercent(req.discountPercent());
        receipt.setDiscountAmount(req.discountAmount());
        receipt.setFinalAmount(req.finalAmount());
        receipt.setCustomerPaid(req.customerPaid() != null ? req.customerPaid() : req.finalAmount());
        receipt.setChangeAmount(req.changeAmount() != null ? req.changeAmount() : BigDecimal.ZERO);
        receipt.setPaymentMethod(req.paymentMethod() != null ? req.paymentMethod() : "CHUYEN_KHOAN");
        receipt.setCashierName(req.cashierName() != null ? req.cashierName() : (currentUser != null ? currentUser : "NV. Thu ngân"));
        receipt.setNote(req.note());
        receipt.setPrintStatus("DA_IN");
        receipt.setSyncStatus("DA_DONG_BO");
        receipt.setCreatedBy(currentUser);
        receipt.setCreatedAt(LocalDateTime.now());

        Receipt saved = receiptRepository.save(receipt);
        return toDto(saved);
    }

    public ReceiptDto toggleSync(UUID id) {
        Receipt r = receiptRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu thu: " + id));
        if ("DA_DONG_BO".equalsIgnoreCase(r.getSyncStatus())) {
            r.setSyncStatus("CHUA_DONG_BO");
        } else {
            r.setSyncStatus("DA_DONG_BO");
        }
        r.setUpdatedAt(LocalDateTime.now());
        return toDto(receiptRepository.save(r));
    }

    public Map<String, Object> getFilterOptions() {
        List<String> companies = receiptRepository.findDistinctCompanies();
        List<String> cashiers = receiptRepository.findDistinctCashiers();
        return Map.of(
                "companies", companies,
                "cashiers", cashiers
        );
    }

    private ReceiptDto toDto(Receipt r) {
        String genderLabel = "Nữ";
        if ("NAM".equalsIgnoreCase(r.getGender())) {
            genderLabel = "Nam";
        }

        String pmLabel = "Chuyển khoản";
        if ("TIEN_MAT".equalsIgnoreCase(r.getPaymentMethod())) pmLabel = "Tiền mặt";
        else if ("QUET_THE".equalsIgnoreCase(r.getPaymentMethod())) pmLabel = "Quẹt thẻ";

        String printLabel = "Đã in";
        if ("LUU_NHAP".equalsIgnoreCase(r.getPrintStatus())) printLabel = "Lưu nháp";
        else if ("LOI_IN".equalsIgnoreCase(r.getPrintStatus())) printLabel = "Lỗi in";

        String syncLabel = "Đã đồng bộ";
        if ("CHUA_DONG_BO".equalsIgnoreCase(r.getSyncStatus())) syncLabel = "Chưa đồng bộ";
        else if ("LOI_DONG_BO".equalsIgnoreCase(r.getSyncStatus())) syncLabel = "Lỗi đồng bộ";

        return new ReceiptDto(
                r.getId(),
                r.getReceiptCode(),
                r.getPatientId(),
                r.getPatientCode(),
                r.getRecordCode(),
                r.getPatientName(),
                r.getGender(),
                genderLabel,
                r.getBirthYear(),
                r.getContractId(),
                r.getCompanyCode(),
                r.getCompanyName(),
                r.getContractCode(),
                r.getContractName(),
                r.getPackageName(),
                r.getServiceCount(),
                r.getLabServices(),
                r.getImagingServices(),
                r.getAllServicesJson(),
                r.getSubtotal(),
                r.getDiscountCode(),
                r.getDiscountPercent(),
                r.getDiscountAmount(),
                r.getFinalAmount(),
                r.getCustomerPaid(),
                r.getChangeAmount(),
                r.getPaymentMethod(),
                pmLabel,
                r.getCashierName(),
                r.getNote(),
                r.getPrintStatus(),
                printLabel,
                r.getSyncStatus(),
                syncLabel,
                r.getCreatedBy(),
                r.getCreatedAt()
        );
    }
}
