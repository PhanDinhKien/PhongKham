package mirai.sale.modules.location.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.common.exception.BadRequestException;
import mirai.sale.common.exception.NotFoundException;
import mirai.sale.modules.location.dto.CreateLocationRequest;
import mirai.sale.modules.location.dto.LocationResponse;
import mirai.sale.modules.location.dto.UpdateLocationRequest;
import mirai.sale.modules.location.entity.Location;
import mirai.sale.modules.location.repository.LocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<LocationResponse> search(String keyword, Boolean active, int page, int size) {
        Page<Location> pageResult = locationRepository.searchLocations(
            keyword != null && !keyword.isBlank() ? keyword.trim() : null,
            active,
            PageRequest.of(page, size)
        );

        List<LocationResponse> items = pageResult.getContent().stream()
            .map(LocationResponse::fromEntity)
            .toList();

        return new PageResponse<>(items, page, size, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public LocationResponse getById(UUID id) {
        Location loc = locationRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm khám"));
        return LocationResponse.fromEntity(loc);
    }

    @Transactional
    public LocationResponse create(CreateLocationRequest req, String currentUser) {
        String cleanCode = req.code().trim().toUpperCase();
        if (locationRepository.existsByCodeAndIsDeletedFalse(cleanCode)) {
            throw new BadRequestException("Mã địa điểm '" + cleanCode + "' đã tồn tại trong hệ thống");
        }

        Location loc = new Location();
        loc.setCode(cleanCode);
        loc.setName(req.name().trim());
        loc.setAddress(req.address().trim());
        loc.setActive(req.active() != null ? req.active() : true);
        loc.setCreatedBy(currentUser != null ? currentUser : "Admin");
        loc.setUpdatedBy(currentUser != null ? currentUser : "Admin");
        loc.setDeleted(false);

        Location saved = locationRepository.save(loc);
        return LocationResponse.fromEntity(saved);
    }

    @Transactional
    public LocationResponse update(UUID id, UpdateLocationRequest req, String currentUser) {
        Location loc = locationRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm khám"));

        loc.setName(req.name().trim());
        loc.setAddress(req.address().trim());
        if (req.active() != null) {
            loc.setActive(req.active());
        }
        loc.setUpdatedBy(currentUser != null ? currentUser : "Admin");

        Location saved = locationRepository.save(loc);
        return LocationResponse.fromEntity(saved);
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
        return LocationResponse.fromEntity(saved);
    }
}
