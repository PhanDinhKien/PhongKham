package mirai.sale.modules.location.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.common.exception.BadRequestException;
import mirai.sale.common.exception.NotFoundException;
import mirai.sale.modules.contract.entity.Contract;
import mirai.sale.modules.contract.entity.ContractLocation;
import mirai.sale.modules.contract.repository.ContractLocationRepository;
import mirai.sale.modules.contract.repository.ContractRepository;
import mirai.sale.modules.location.dto.*;
import mirai.sale.modules.location.entity.Location;
import mirai.sale.modules.location.entity.LocationShift;
import mirai.sale.modules.location.repository.LocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final ContractLocationRepository contractLocationRepository;
    private final ContractRepository contractRepository;

    public LocationService(
        LocationRepository locationRepository,
        ContractLocationRepository contractLocationRepository,
        ContractRepository contractRepository
    ) {
        this.locationRepository = locationRepository;
        this.contractLocationRepository = contractLocationRepository;
        this.contractRepository = contractRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<LocationResponse> search(String keyword, Boolean active, int page, int size) {
        Page<Location> pageResult = locationRepository.searchLocations(
            keyword != null && !keyword.isBlank() ? keyword.trim() : null,
            active,
            PageRequest.of(page, size)
        );

        List<LocationResponse> items = pageResult.getContent().stream()
            .map(loc -> {
                long count = contractLocationRepository.countByLocationId(loc.getId());
                return LocationResponse.fromEntity(loc, count);
            })
            .toList();

        return new PageResponse<>(items, page, size, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getActiveLocationsWithShifts() {
        return locationRepository.findAll().stream()
            .filter(l -> !l.isDeleted() && l.isActive() && l.getShifts() != null && !l.getShifts().isEmpty())
            .map(l -> LocationResponse.fromEntity(l, contractLocationRepository.countByLocationId(l.getId())))
            .toList();
    }

    @Transactional(readOnly = true)
    public LocationResponse getById(UUID id) {
        Location loc = locationRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm khám"));
        long count = contractLocationRepository.countByLocationId(loc.getId());
        return LocationResponse.fromEntity(loc, count);
    }

    @Transactional
    public LocationResponse create(CreateLocationRequest req, String currentUser) {
        String cleanCode = req.code() != null ? req.code().trim().toUpperCase() : null;
        if (cleanCode == null || cleanCode.isBlank()) {
            cleanCode = generateAutoLocationCode();
        } else if (locationRepository.existsByCodeAndIsDeletedFalse(cleanCode)) {
            throw new BadRequestException("Mã địa điểm '" + cleanCode + "' đã tồn tại trong hệ thống");
        }

        Location loc = new Location();
        loc.setCode(cleanCode);
        loc.setName(req.name().trim());
        loc.setAddress(req.address() != null ? req.address().trim() : "");
        loc.setActive(req.active() != null ? req.active() : true);
        loc.setCreatedBy(currentUser != null ? currentUser : "Admin");
        loc.setUpdatedBy(currentUser != null ? currentUser : "Admin");
        loc.setDeleted(false);

        // Shifts configuration
        applyShiftsToLocation(loc, req.shifts());

        Location saved = locationRepository.save(loc);
        return LocationResponse.fromEntity(saved, 0);
    }

    @Transactional
    public LocationResponse update(UUID id, UpdateLocationRequest req, String currentUser) {
        Location loc = locationRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm khám"));

        if (req.code() != null && !req.code().isBlank()) {
            String newCode = req.code().trim().toUpperCase();
            if (!newCode.equals(loc.getCode()) && locationRepository.existsByCodeAndIdNotAndIsDeletedFalse(newCode, id)) {
                throw new BadRequestException("Mã địa điểm '" + newCode + "' đã tồn tại ở địa điểm khác");
            }
            loc.setCode(newCode);
        }

        loc.setName(req.name().trim());
        if (req.address() != null) {
            loc.setAddress(req.address().trim());
        }
        if (req.active() != null) {
            loc.setActive(req.active());
        }
        loc.setUpdatedBy(currentUser != null ? currentUser : "Admin");

        // Update shifts
        if (req.shifts() != null) {
            loc.getShifts().clear();
            applyShiftsToLocation(loc, req.shifts());
        }

        Location saved = locationRepository.save(loc);
        long count = contractLocationRepository.countByLocationId(loc.getId());
        return LocationResponse.fromEntity(saved, count);
    }

    @Transactional
    public void softDelete(UUID id, String currentUser) {
        Location loc = locationRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm khám"));

        loc.setDeleted(true);
        loc.setDeletedAt(LocalDateTime.now());
        loc.setDeletedBy(currentUser != null ? currentUser : "Admin");

        locationRepository.save(loc);
    }

    @Transactional
    public LocationResponse toggleStatus(UUID id, String currentUser) {
        Location loc = locationRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm khám"));

        loc.setActive(!loc.isActive());
        loc.setUpdatedBy(currentUser != null ? currentUser : "Admin");

        Location saved = locationRepository.save(loc);
        long count = contractLocationRepository.countByLocationId(loc.getId());
        return LocationResponse.fromEntity(saved, count);
    }

    // ==============================================================
    // CONTRACTS ATTACHED TO LOCATION
    // ==============================================================

    @Transactional(readOnly = true)
    public List<LocationContractResponse> getContractsByLocation(UUID locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new NotFoundException("Không tìm thấy địa điểm khám");
        }
        return contractLocationRepository.findByLocationIdWithContract(locationId).stream()
            .map(LocationContractResponse::fromEntity)
            .toList();
    }

    @Transactional
    public LocationContractResponse addContractToLocation(
        UUID locationId,
        AssignContractToLocationRequest req,
        String currentUser
    ) {
        Location loc = locationRepository.findByIdAndIsDeletedFalse(locationId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm khám"));

        Contract contract = contractRepository.findByIdAndIsDeletedFalse(req.contractId())
            .orElseThrow(() -> new NotFoundException("Không tìm thấy hợp đồng"));

        if (contractLocationRepository.existsByContractIdAndLocationId(contract.getId(), loc.getId())) {
            throw new BadRequestException("Hợp đồng này đã được gán vào địa điểm khám");
        }

        ContractLocation cl = new ContractLocation(
            contract,
            loc,
            req.active() != null ? req.active() : true,
            req.shiftConfig(),
            currentUser != null ? currentUser : "Admin"
        );

        ContractLocation saved = contractLocationRepository.save(cl);
        return LocationContractResponse.fromEntity(saved);
    }

    @Transactional
    public LocationContractResponse toggleContractLocationStatus(UUID locationId, UUID contractLocationId) {
        ContractLocation cl = contractLocationRepository.findById(contractLocationId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy liên kết hợp đồng với địa điểm"));

        if (!cl.getLocation().getId().equals(locationId)) {
            throw new BadRequestException("Hợp đồng không thuộc địa điểm này");
        }

        cl.setActive(!cl.isActive());
        ContractLocation saved = contractLocationRepository.save(cl);
        return LocationContractResponse.fromEntity(saved);
    }

    @Transactional
    public void removeContractFromLocation(UUID locationId, UUID contractLocationId) {
        ContractLocation cl = contractLocationRepository.findById(contractLocationId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy liên kết hợp đồng với địa điểm"));

        if (!cl.getLocation().getId().equals(locationId)) {
            throw new BadRequestException("Hợp đồng không thuộc địa điểm này");
        }

        contractLocationRepository.delete(cl);
    }

    // ==============================================================
    // HELPER METHODS
    // ==============================================================

    private void applyShiftsToLocation(Location loc, List<ShiftDto> shiftDtos) {
        if (shiftDtos != null && !shiftDtos.isEmpty()) {
            int num = 1;
            for (ShiftDto s : shiftDtos) {
                if (s.startTime() != null && !s.startTime().isBlank() && s.endTime() != null && !s.endTime().isBlank()) {
                    LocationShift ls = new LocationShift(
                        loc,
                        s.shiftNumber() != null ? s.shiftNumber() : num,
                        s.shiftName() != null ? s.shiftName() : ("Ca " + (s.shiftNumber() != null ? s.shiftNumber() : num)),
                        s.startTime().trim(),
                        s.endTime().trim()
                    );
                    loc.getShifts().add(ls);
                    num++;
                }
            }
        } else {
            // Default 2 shifts matching mockup Image 2
            loc.getShifts().add(new LocationShift(loc, 1, "Ca 1", "06:00", "12:00"));
            loc.getShifts().add(new LocationShift(loc, 2, "Ca 2", "13:00", "19:00"));
        }
    }

    private String generateAutoLocationCode() {
        int year = Year.now().getValue();
        String prefix = year + "-";
        int seq = 1;
        while (true) {
            String candidate = String.format("%s%02d", prefix, seq);
            if (!locationRepository.existsByCodeAndIsDeletedFalse(candidate)) {
                return candidate;
            }
            seq++;
        }
    }
}
