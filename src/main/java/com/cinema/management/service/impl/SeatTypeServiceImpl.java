package com.cinema.management.service.impl;

import com.cinema.management.model.entity.SeatType;
import com.cinema.management.repository.SeatTypeRepository;
import com.cinema.management.service.IAuditLogService;
import com.cinema.management.service.ISeatTypeService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Triển khai nghiệp vụ quản lý loại ghế.
 * Business rule: mỗi lần đổi giá → ghi AuditLog.
 */
public class SeatTypeServiceImpl implements ISeatTypeService {

    private final SeatTypeRepository seatTypeRepository;
    private final IAuditLogService auditLogService;

    public SeatTypeServiceImpl() {
        this.seatTypeRepository = new SeatTypeRepository();
        this.auditLogService = null;
    }

    public SeatTypeServiceImpl(SeatTypeRepository seatTypeRepository,
                                IAuditLogService auditLogService) {
        this.seatTypeRepository = seatTypeRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<SeatType> getAllSeatTypes() {
        return seatTypeRepository.findAll();
    }

    @Override
    public Optional<SeatType> getSeatTypeById(String seatTypeId) {
        return seatTypeRepository.findById(seatTypeId);
    }

    @Override
    public SeatType addSeatType(String typeName, BigDecimal basePrice) {
        validateTypeName(typeName);
        validateBasePrice(basePrice);
        if (seatTypeRepository.existsByTypeName(typeName.trim())) {
            throw new IllegalArgumentException("Loại ghế '" + typeName + "' đã tồn tại.");
        }
        SeatType seatType = SeatType.builder()
                .seatTypeId(UUID.randomUUID().toString())
                .typeName(typeName.trim())
                .basePrice(basePrice)
                .build();
        SeatType saved = seatTypeRepository.save(seatType);

        if (auditLogService != null) {
            auditLogService.logAction("CREATE", "SeatType", "TypeName",
                    "N/A", typeName.trim() + " (" + basePrice.toPlainString() + "đ)");
        }
        return saved;
    }

    @Override
    public SeatType updateSeatType(String seatTypeId, String typeName,
                                    BigDecimal basePrice, String changedByUserId) {
        validateTypeName(typeName);
        validateBasePrice(basePrice);
        SeatType existing = seatTypeRepository.findById(seatTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Loại ghế không tồn tại: " + seatTypeId));

        if (!existing.getTypeName().equalsIgnoreCase(typeName.trim())
                && seatTypeRepository.existsByTypeName(typeName.trim())) {
            throw new IllegalArgumentException("Tên loại ghế '" + typeName + "' đã tồn tại.");
        }

        // Lưu dữ liệu cũ
        String oldName = existing.getTypeName();
        BigDecimal oldPrice = existing.getBasePrice();

        existing.setTypeName(typeName.trim());
        existing.setBasePrice(basePrice);
        SeatType saved = seatTypeRepository.save(existing);

        // Ghi AuditLog khi tên thay đổi
        if (auditLogService != null && !oldName.equals(typeName.trim())) {
            auditLogService.logAction("UPDATE", "SeatType", "TypeName",
                    oldName, typeName.trim());
        }

        // Ghi AuditLog khi giá thay đổi
        if (auditLogService != null && oldPrice.compareTo(basePrice) != 0) {
            auditLogService.logAction("UPDATE", "SeatType", "BasePrice",
                    oldPrice.toPlainString(), basePrice.toPlainString());
        }

        return saved;
    }

    @Override
    public void deleteSeatType(String seatTypeId) {
        SeatType st = seatTypeRepository.findById(seatTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Loại ghế không tồn tại: " + seatTypeId));
        if (st.getSeats() != null && !st.getSeats().isEmpty()) {
            throw new IllegalStateException(
                    "Không thể xóa loại ghế đang được sử dụng bởi " + st.getSeats().size() + " ghế.");
        }

        String typeName = st.getTypeName();
        seatTypeRepository.deleteById(seatTypeId);

        if (auditLogService != null) {
            auditLogService.logAction("DELETE", "SeatType", "TypeName",
                    typeName, "Đã xóa");
        }
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private void validateTypeName(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại ghế không được để trống.");
        }
        if (typeName.trim().length() > 100) {
            throw new IllegalArgumentException("Tên loại ghế không được vượt quá 100 ký tự.");
        }
    }

    private void validateBasePrice(BigDecimal basePrice) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá cơ bản phải >= 0.");
        }
    }
}
