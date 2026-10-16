package mirai.sale.modules.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import mirai.sale.modules.contract.entity.ContractStatus;

public record CreateContractRequest(
    @NotBlank(message = "Mã hợp đồng không được để trống")
    @Size(max = 50, message = "Mã hợp đồng không được vượt quá 50 ký tự")
    String contractCode,

    @NotBlank(message = "Tên hợp đồng không được để trống")
    @Size(max = 255, message = "Tên hợp đồng không được vượt quá 255 ký tự")
    String contractName,

    @NotBlank(message = "Mã công ty không được để trống")
    @Size(max = 50, message = "Mã công ty không được vượt quá 50 ký tự")
    String companyCode,

    @NotBlank(message = "Tên công ty không được để trống")
    @Size(max = 255, message = "Tên công ty không được vượt quá 255 ký tự")
    String companyName,

    ContractStatus status
) {}
