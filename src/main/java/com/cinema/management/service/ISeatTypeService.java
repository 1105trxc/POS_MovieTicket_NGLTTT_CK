package com.cinema.management.service;

import com.cinema.management.model.entity.SeatType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interface định nghĩa các nghiệp vụ quản lý loại ghế.
 * Mỗi lần cập nhật giá phải kích hoạt AuditLog theo BR-04.
 */
public interface ISeatTypeService {

    List<SeatType> getAllSeatTypes();

    Optional<SeatType> getSeatTypeById(String seatTypeId);

    /**
     * Thêm loại ghế mới. Validate tên không trùng và basePrice >= 0.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    SeatType addSeatType(String typeName, BigDecimal basePrice);

    /**
     * Cập nhật loại ghế. Nếu giá thay đổi → ghi AuditLog.
     * @param changedByUserId ID nhân viên thực hiện thay đổi (dùng cho AuditLog)
     * @throws IllegalArgumentException nếu loại ghế không tồn tại
     */
    SeatType updateSeatType(String seatTypeId, String typeName, BigDecimal basePrice, String changedByUserId);

    /**
     * Xóa loại ghế. Chỉ xóa được nếu không có ghế nào đang dùng loại này.
     * @throws IllegalStateException nếu loại ghế đang được sử dụng
     */
    void deleteSeatType(String seatTypeId);
}
