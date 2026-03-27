package com.cinema.management.service.impl;

import com.cinema.management.model.entity.AuditLog;
import com.cinema.management.model.entity.SeatType;
import com.cinema.management.model.entity.User;
import com.cinema.management.repository.AuditLogRepository;
import com.cinema.management.repository.SeatTypeRepository;
import com.cinema.management.service.ISeatTypeService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Triển khai nghiệp vụ quản lý loại ghế.
 * Business rule: mỗi lần đổi giá → ghi AuditLog.
 */
public class SeatTypeServiceImpl implements ISeatTypeService {

    private final SeatTypeRepository seatTypeRepository;
    private final AuditLogRepository auditLogRepository;

    public SeatTypeServiceImpl() {
        this.seatTypeRepository = new SeatTypeRepository();
        this.auditLogRepository = new AuditLogRepository();
    }

    public SeatTypeServiceImpl(SeatTypeRepository seatTypeRepository,
                                AuditLogRepository auditLogRepository) {
        this.seatTypeRepository = seatTypeRepository;
        this.auditLogRepository = auditLogRepository;
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
        return seatTypeRepository.save(seatType);
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

        // Ghi AuditLog nếu giá thay đổi (Business Rule – Skill_agent)
        if (existing.getBasePrice().compareTo(basePrice) != 0) {
            writeAuditLog(changedByUserId, "SeatType", seatTypeId, "BasePrice",
                    existing.getBasePrice().toPlainString(), basePrice.toPlainString());
        }

        existing.setTypeName(typeName.trim());
        existing.setBasePrice(basePrice);
        return seatTypeRepository.save(existing);
    }

    @Override
    public void deleteSeatType(String seatTypeId) {
        SeatType st = seatTypeRepository.findById(seatTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Loại ghế không tồn tại: " + seatTypeId));
        if (st.getSeats() != null && !st.getSeats().isEmpty()) {
            throw new IllegalStateException(
                    "Không thể xóa loại ghế đang được sử dụng bởi " + st.getSeats().size() + " ghế.");
        }
        seatTypeRepository.deleteById(seatTypeId);
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private void writeAuditLog(String userId, String tableName, String recordId,
                                String fieldName, String oldValue, String newValue) {
        AuditLog log = AuditLog.builder()
                .tableName(tableName)
                .fieldName(fieldName)
                // Lưu recordId vào oldValue prefix để truy vết
                .oldValue("[ID:" + recordId + "] " + oldValue)
                .newValue(newValue)
                .changedAt(LocalDateTime.now())
                .build();
        if (userId != null && !userId.isEmpty()) {
            User u = new User();
            u.setUserId(userId);
            log.setChangedBy(u);
        }
        auditLogRepository.save(log);
    }

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
